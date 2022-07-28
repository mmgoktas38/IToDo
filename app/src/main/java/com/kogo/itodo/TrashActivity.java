package com.kogo.itodo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kogo.itodo.databinding.ActivityTrashBinding;

import java.util.ArrayList;
import java.util.List;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class TrashActivity extends AppCompatActivity {

    private ActivityTrashBinding trashBinding;
    private TrashAdapter trashAdapter;
    private List<WillDo> trashList = new ArrayList<>();
    private ArrayList<WillDo> filteredlist = new ArrayList<>();
    private FirebaseUser currentUser;
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private FirebaseAuth auth;
    private String onlineUserID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        trashBinding = ActivityTrashBinding.inflate(getLayoutInflater());
        setContentView(trashBinding.getRoot());

        setSupportActionBar(trashBinding.toolbar);
        getSupportActionBar().setTitle("Trash List");

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

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
        reference = database.getReference().child("trash").child(onlineUserID);

        holdRecyclerView();
        getDatas();

        new ItemTouchHelper(simpleCallback).attachToRecyclerView(trashBinding.recyclerViewTrash);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.trash_menu_layout,menu);
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
            case R.id.item_main:
                Intent intent = new Intent(TrashActivity.this,MainActivity.class);
                startActivity(intent);
                finish();
                return true;
            case R.id.item_logout:
                logoutUser();
            default: return super.onOptionsItemSelected(item);
        }

    }

    private void filter(String text) {
        filteredlist.clear();

        for (WillDo item : trashList) {
            if (item.getWillDoText().toLowerCase().contains(text.toLowerCase())) {
                filteredlist.add(item);
            }
        }
        if (filteredlist.isEmpty()) {
            Toast.makeText(this, "No Data Found..", Toast.LENGTH_SHORT).show();
            trashAdapter.filterList(filteredlist);
        } else {
            trashAdapter.filterList(filteredlist);
        }
    }

    public void holdRecyclerView(){
        trashBinding.recyclerViewTrash.setHasFixedSize(true);
        trashBinding.recyclerViewTrash.setLayoutManager(new LinearLayoutManager(TrashActivity.this, LinearLayoutManager.VERTICAL, false));
        trashAdapter = null;
    }

    public void getDatas(){
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                trashList.clear();     // this is important, because if we dont clear the list same object again add

                for(DataSnapshot ds : snapshot.getChildren()) {
                    String id = ds.child("id").getValue(String.class);
                    String willDoText = ds.child("willDoText").getValue(String.class);
                    String createdDate = ds.child("createdDate").getValue(String.class);
                    String deadLine = ds.child("deadLine").getValue(String.class);

                    WillDo willDoItem = new WillDo(id, willDoText, createdDate, deadLine);
                    trashList.add(willDoItem);
                }

                if (filteredlist.isEmpty()){
                    trashAdapter = new TrashAdapter(TrashActivity.this, trashList);
                }
                else {
                    SearchView simpleSearchView = (SearchView) findViewById(R.id.item_search); // inititate a search view
                    CharSequence query = simpleSearchView.getQuery();
                    filter(query.toString());
                }

                trashBinding.recyclerViewTrash.setAdapter(trashAdapter);
                if (trashList.isEmpty()){
                    trashBinding.imageViewEmptyTrash.setVisibility(View.VISIBLE);
                }
                else {
                    trashBinding.imageViewEmptyTrash.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // calling on cancelled method when we receive
                // any error or we are not able to get the data.
                Toast.makeText(TrashActivity.this, "Fail to get data.", Toast.LENGTH_SHORT).show();
            }
        });
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
                deleteData = trashList.get(position);
            }
            else {
                deleteData = filteredlist.get(position);
            }

            reference = database.getReference().child("trash").child(onlineUserID).child(deleteData.getId());

            Snackbar.make(trashBinding.recyclerViewTrash, "Deleting task . . .", Snackbar.LENGTH_LONG).setAction("Cancel", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                }
            }).setActionTextColor(Color.WHITE).addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {
                @Override
                public void onDismissed(Snackbar transientBottomBar, int event) {
                    super.onDismissed(transientBottomBar, event);
                    Log.e("onDismissed", String.valueOf(event));    // 1 come back basıldı - 2 basılmadı
                    if(event == 2){
                        reference.removeValue();
                    }
                    if (event == 1){
                        trashAdapter.notifyDataSetChanged();
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
                    .addSwipeLeftBackgroundColor(ContextCompat.getColor(TrashActivity.this,R.color.deleteColor))
                    .addSwipeLeftActionIcon(R.drawable.ic_baseline_delete_24)
                    .addSwipeLeftLabel("Delete")
                    .setSwipeLeftLabelColor(ContextCompat.getColor(TrashActivity.this,R.color.white))
                    .create()
                    .decorate();

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    };



    private void logoutUser(){
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(TrashActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}