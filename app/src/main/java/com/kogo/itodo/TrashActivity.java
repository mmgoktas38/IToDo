package com.kogo.itodo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

    WillDo deleteData;

    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getAdapterPosition();
            deleteData = trashList.get(position);
           // trashList.remove(deleteData);
            //willDoAdapter.notifyDataSetChanged();

            reference = database.getReference().child("trash").child(onlineUserID).child(deleteData.getId());

            Snackbar.make(trashBinding.recyclerViewTrash, "Deleting task . . .", Snackbar.LENGTH_LONG).setAction("Cancel", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                 //   trashList.add(position,deleteData);
              //      willDoAdapter.notifyDataSetChanged();
                }
            }).setActionTextColor(Color.WHITE).addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {
                @Override
                public void onDismissed(Snackbar transientBottomBar, int event) {
                    super.onDismissed(transientBottomBar, event);
                    Log.e("onDismissed", String.valueOf(event));    // 1 come back basıldı - 2 basılmadı
                    if(event == 2){
                        reference.removeValue();
                      //  trashAdapter.notifyDataSetChanged();
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

                trashAdapter = new TrashAdapter(TrashActivity.this, trashList);
              //  trashAdapter.notifyDataSetChanged();
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

}