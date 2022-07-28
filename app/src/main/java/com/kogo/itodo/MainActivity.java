package com.kogo.itodo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kogo.itodo.databinding.ActivityMainBinding;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding mainBinding;
    private WillDoAdapter willDoAdapter;
    private List<WillDo> willDoList = new ArrayList<>();
    private List<WillDo> trashList = new ArrayList<>();
    private ArrayList<WillDo> filteredlist = new ArrayList<>();
    private ProgressDialog loader;
    private FirebaseUser currentUser;
    private FirebaseDatabase database;
    private DatabaseReference reference, trashReference;
    private FirebaseAuth auth;
    private String onlineUserID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mainBinding.getRoot());

        setSupportActionBar(mainBinding.toolbar);
        getSupportActionBar().setTitle("itodo List");

        loader = new ProgressDialog(this);

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser(); // When initializing your Activity, check to see if the user is currently signed in:
        if (currentUser == null){
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        onlineUserID = currentUser.getUid();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference().child("tasks").child(onlineUserID);
        trashReference = database.getReference().child("trash").child(onlineUserID);

        if (isNetworkAvailable(MainActivity.this)){
            loader.dismiss();
            mainBinding.imageViewAddTask.setVisibility(View.INVISIBLE);
        }
        else {
            loader.setMessage("No internet, check your internet connection!");
            loader.show();
            mainBinding.imageViewAddTask.setVisibility(View.VISIBLE);
            mainBinding.imageViewAddTask.setImageResource(R.drawable.no_internet);
            return;
        }

        holdRecyclerView();
        getDatas();

        mainBinding.floatingActionButtonAddWillDo.setOnClickListener(view -> { addTask();});
        new ItemTouchHelper(simpleCallback).attachToRecyclerView(mainBinding.recyclerViewWillDo);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_layout,menu);
        MenuItem searchItem = menu.findItem(R.id.item_search);

        // getting search view of our item.
        SearchView searchView = (SearchView) searchItem.getActionView();

        // below line is to call set on query text listener method.
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.item_done:
                Intent intent = new Intent(MainActivity.this,TrashActivity.class);
                startActivity(intent);
                return true;
            case R.id.item_logout:
                logoutUser();
                return true;

            default: return super.onOptionsItemSelected(item);
        }
    }

    private void filter(String text) {

        filteredlist.clear();

        for (WillDo item : willDoList) {
            if (item.getWillDoText().toLowerCase().contains(text.toLowerCase())) {
                filteredlist.add(item);
            }
        }
        if (filteredlist.isEmpty()) {
            Toast.makeText(this, "No Data Found..", Toast.LENGTH_SHORT).show();
            willDoAdapter.filterList(filteredlist);
        } else {
            willDoAdapter.filterList(filteredlist);
        }
    }

    public void holdRecyclerView(){
        mainBinding.recyclerViewWillDo.setHasFixedSize(true);
        mainBinding.recyclerViewWillDo.setLayoutManager(new LinearLayoutManager(MainActivity.this, LinearLayoutManager.VERTICAL, false));
        willDoAdapter = null;
    }

    public void getDatas(){
        reference = database.getReference().child("tasks").child(onlineUserID);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                willDoList.clear();     // this is important, because if we dont clear the list same object again add
                for(DataSnapshot ds : snapshot.getChildren()) {
                    String id = ds.child("id").getValue(String.class);
                    String willDoText = ds.child("willDoText").getValue(String.class);
                    String createdDate = ds.child("createdDate").getValue(String.class);
                    String deadLine = ds.child("deadLine").getValue(String.class);

                    WillDo willDoItem = new WillDo(id, willDoText, createdDate, deadLine);
                    willDoList.add(willDoItem);
                }

                if (filteredlist.isEmpty()){
                    willDoAdapter = new WillDoAdapter(MainActivity.this, willDoList);
                }
                else {
                    SearchView simpleSearchView = (SearchView) findViewById(R.id.item_search); // inititate a search view
                    CharSequence query = simpleSearchView.getQuery();
                    //getDatas();
                    filter(query.toString());
                    Log.e("query",query.toString());
                }

                mainBinding.recyclerViewWillDo.setAdapter(willDoAdapter);
                if (willDoList.isEmpty()){
                    mainBinding.imageViewAddTask.setVisibility(View.VISIBLE);
                }
                else {
                    mainBinding.imageViewAddTask.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Fail to get data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addTask(){
        reference = database.getReference().child("tasks").child(onlineUserID);

        AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View myView = inflater.inflate(R.layout.add_task,null);
        myDialog.setView(myView);
        final AlertDialog dialog = myDialog.create();
        dialog.setCancelable(false);

        final EditText editTextAddTask = myView.findViewById(R.id.editTextAddTask);
        final EditText editTextDeadline = myView.findViewById(R.id.editTextDeadline);
        TextView textViewSave = myView.findViewById(R.id.textViewSave);
        TextView textViewCancel = myView.findViewById(R.id.textViewCancel);

        editTextDeadline.setOnClickListener(view -> {
            final Calendar newCalendar = Calendar.getInstance();
            final DatePickerDialog  StartTime = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    Calendar newDate = Calendar.getInstance();
                    newDate.set(year, monthOfYear, dayOfMonth);
                    DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getApplicationContext());
                    editTextDeadline.setText(dateFormat.format(newDate.getTime()));
                }

            }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));

            StartTime.show();
        });
        textViewCancel.setOnClickListener(view -> {  dialog.dismiss(); });

        textViewSave.setOnClickListener(view -> {
            String task = editTextAddTask.getText().toString().trim();
            DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getApplicationContext());
            String createdDate = dateFormat.format(new Date());
            String deadline = editTextDeadline.getText().toString().trim();
            String id = reference.push().getKey();

            if (task.isEmpty()){
                Toast.makeText(this, "Task must be filled", Toast.LENGTH_SHORT).show();
                return;
            }
            else {
                loader.setMessage("Adding your data");
                loader.setCanceledOnTouchOutside(false);
                loader.show();

                WillDo willDoItem = new WillDo(id,task, createdDate, deadline);
                Log.e("add task id: ", id);
                reference.child(id).setValue(willDoItem);
                loader.dismiss();
                dialog.dismiss();
            }
        });

        dialog.show();
    }



    WillDo deleteData;

    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getAdapterPosition();
            if (filteredlist.isEmpty()){
                deleteData = willDoList.get(position);
            }
            else {
                deleteData = filteredlist.get(position);
            }

            reference = database.getReference().child("tasks").child(onlineUserID).child(deleteData.getId());

            Snackbar.make(mainBinding.recyclerViewWillDo, "Moving to trash . . .", Snackbar.LENGTH_LONG).setAction("Cancel", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                }
            }).setActionTextColor(Color.WHITE).addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {
                @Override
                public void onDismissed(Snackbar transientBottomBar, int event) {
                    super.onDismissed(transientBottomBar, event);
                    Log.e("onDismissed", String.valueOf(event));    // 1 cancel basıldı - 2 basılmadı
                    if(event == 2){
                        trashReference.child(deleteData.getId()).setValue(deleteData);  // save the data to trash
                        database.getReference().child("tasks").child(onlineUserID).child(deleteData.getId()).removeValue(); // remove data from tasks
                    }
                    if (event == 1){
                        willDoAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onShown(Snackbar transientBottomBar) {
                    super.onShown(transientBottomBar);
                    Log.e("onShown", "onShown");
                }
            }).show();

        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

            new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    .addSwipeLeftBackgroundColor(ContextCompat.getColor(MainActivity.this,R.color.deleteColor))
                    .addSwipeLeftActionIcon(R.drawable.ic_baseline_delete_24)
                    .addSwipeLeftLabel("Move to trash")
                    .setSwipeLeftLabelColor(ContextCompat.getColor(MainActivity.this,R.color.white))
                    .create()
                    .decorate();

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }

    };

    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }

    private void logoutUser(){
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}