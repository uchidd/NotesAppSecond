package id.uchidd.notesapp.view;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import id.uchidd.notesapp.R;
import id.uchidd.notesapp.database.DatabaseHelper;
import id.uchidd.notesapp.database.model.Note;
import id.uchidd.notesapp.utils.RecyclerTouchListener;

public class MainActivity extends AppCompatActivity {

    private NotesAdapter adapterNote;
    private List<Note> noteList = new ArrayList<>();
    private CoordinatorLayout coordinatorLayout;
    private RecyclerView recyclerView;
    private TextView noNotesTextView;

    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar)findViewById(R.id.tbMain);
        setSupportActionBar(toolbar);

        coordinatorLayout = (CoordinatorLayout)findViewById(R.id.clMain);
        recyclerView = (RecyclerView)findViewById(R.id.rv_list);
        noNotesTextView = (TextView)findViewById(R.id.txt_emptyfound);

        db = new DatabaseHelper(this);
        noteList.addAll(db.getAllNotes());

        FloatingActionButton fabAddNewNote = (FloatingActionButton)findViewById(R.id.fabAdd);
        fabAddNewNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNoteDialog(false, null, -1);
            }
        });

        adapterNote = new NotesAdapter(this, noteList);
        RecyclerView.LayoutManager rv_layoutmanager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(rv_layoutmanager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapterNote);

        toogleEmptyNotes();

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(this, recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {

            }

            @Override
            public void onLongClick(View view, int position) {
                showActionDialog(position);
            }
        }));

    }

    private void showActionDialog(final int position) {

        CharSequence color[] = new CharSequence[]{
                "Edit",
                "Delete"
        };

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Option");
        builder.setItems(color, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (which == 0){
                    showNoteDialog(true, noteList.get(position), position);
                } else {
                    builder.setTitle("Attention")
                            .setMessage("Apakah anda yakin ingin menghapus data ini?")
                            .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    deleteNote(position);
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });
                    AlertDialog showAlertDialog = builder.create();
                    showAlertDialog.show();
                }
            }
        });
        builder.show();

    }

    private void deleteNote(int position) {
        db.deleteNote(noteList.get(position));
        noteList.remove(position);
        adapterNote.notifyItemRemoved(position);

        toogleEmptyNotes();
    }


    private void showNoteDialog(final boolean shouldUpdate, final Note note, final int position) {
        LayoutInflater layoutInflater = LayoutInflater.from(getApplicationContext());
        View view = layoutInflater.inflate(R.layout.add_note_dialog, null);

        final AlertDialog.Builder alertDialogBuilderInput = new AlertDialog.Builder(this);
        alertDialogBuilderInput.setView(view);

        final EditText inputNote = view.findViewById(R.id.etNewNote);
        TextView dialogTitle = view.findViewById(R.id.tvDialogAddTitle);
        dialogTitle.setText(!shouldUpdate ? getString(R.string.lbl_new_notetitle):getString(R.string.lbl_edit_notetitle));

        if (shouldUpdate && note != null){
            inputNote.setText(note.getNote());
        }

        alertDialogBuilderInput
                .setCancelable(false)
                .setPositiveButton(shouldUpdate ? "Update" : "Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogBox, int id) {

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogBox, int id) {

                    }
                });

        final AlertDialog alertDialog = alertDialogBuilderInput.create();
        alertDialog.show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(inputNote.getText().toString())){
                    Toast.makeText(MainActivity.this, "Inputan Note Belom Diisi!", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    alertDialog.dismiss();
                }

                if (shouldUpdate && note != null){
                    updateNote(inputNote.getText().toString(), position);
                } else {
                    createNote(inputNote.getText().toString());
                }
            }
        });
    }

    private void createNote(String note) {

        long id = db.insertNote(note);
        Note noteCreate = db.getNote(id);

        if (noteCreate != null){
            noteList.add(0, noteCreate);
            adapterNote.notifyDataSetChanged();
            toogleEmptyNotes();
        }

    }

    private void updateNote(String note, int position) {

        Note noteUpdate = noteList.get(position);
        noteUpdate.setNote(note);
        db.updateNote(noteUpdate);
        noteList.set(position, noteUpdate);
        adapterNote.notifyItemChanged(position);
        toogleEmptyNotes();
    }

    private void toogleEmptyNotes() {
        if (db.getNoteCount() > 0){
            noNotesTextView.setVisibility(View.GONE);
        } else {
            noNotesTextView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.about_apps){
            showAboutApps();
        } else if (id == R.id.exit_app){
            showExitApps();
        }

        return super.onOptionsItemSelected(item);
    }

    private void showExitApps() {

        LayoutInflater getExitLayoutInflater = LayoutInflater.from(getApplicationContext());
        View getLayout = getExitLayoutInflater.inflate(R.layout.exit_apps, null);

        AlertDialog.Builder builderExitDialog = new AlertDialog.Builder(this);
        builderExitDialog.setView(getLayout);

        builderExitDialog.setPositiveButton("Exit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builderExitDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        final AlertDialog showExitDialog = builderExitDialog.create();
        showExitDialog.show();

    }

    private void showAboutApps() {

        LayoutInflater  getAboutLayoutInfalter = LayoutInflater.from(getApplicationContext());
        View getLayout = getAboutLayoutInfalter.inflate(R.layout.about_apps, null);

        AlertDialog.Builder builderAboutDialog = new AlertDialog.Builder(this);
        builderAboutDialog.setView(getLayout);

        final AlertDialog showAlertDialog = builderAboutDialog.create();
        showAlertDialog.show();

    }
}