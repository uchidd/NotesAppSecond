package id.uchidd.notesapp.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import id.uchidd.notesapp.R;
import id.uchidd.notesapp.database.model.Note;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.MyViewHolder> {

    private Context context;
    private List<Note> noteList;

    public NotesAdapter(Context context, List<Note> noteList){
        this.context = context;
        this.noteList = noteList;
    }

    @Override
    public MyViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.note_list_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder( MyViewHolder holder, int position) {

        Note note = noteList.get(position);
        holder.dot.setText(Html.fromHtml("&#8226;"));
        holder.note.setText(note.getNote());
        holder.timestamp.setText(formatDate(note.getTimestamp()));

    }

    private String formatDate(String dateStr) {

        try {
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = fmt.parse(dateStr);
            SimpleDateFormat fmtOut = new SimpleDateFormat("d MMMM yyyy");
            return fmtOut.format(date);
        } catch (ParseException e) {

        }
        return "";

    }

    @Override
    public int getItemCount() {
        return noteList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView dot, note, timestamp;

        public MyViewHolder( View itemView) {
            super(itemView);

            dot = (TextView)itemView.findViewById(R.id.tvDot);
            note = (TextView)itemView.findViewById(R.id.tvNote);
            timestamp = (TextView)itemView.findViewById(R.id.tvTimestamp);

        }
    }
}
