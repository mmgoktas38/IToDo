package com.kogo.itodo;

import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.List;

public class TrashAdapter extends RecyclerView.Adapter<TrashAdapter.CardViewHolder>{

    private Context mContext;
    private List<WillDo> willDoArrayList;

    public TrashAdapter(Context mContext, List<WillDo> willDoArrayList){
        this.mContext = mContext;
        this.willDoArrayList = willDoArrayList;
    }

    @NonNull
    @Override
    public TrashAdapter.CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_will_do, parent, false);

        return new TrashAdapter.CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrashAdapter.CardViewHolder holder, int position) {

        WillDo willDoItem = willDoArrayList.get(position);

        FirebaseUser currentUser = null;
        FirebaseDatabase database;
        DatabaseReference reference;
        String onlineUserID;
        FirebaseAuth auth;

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        onlineUserID = currentUser.getUid();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference().child("trash").child(onlineUserID).child(willDoItem.getId());

        holder.textViewWillDo.setText(willDoItem.getWillDoText());
        holder.textViewWillDo.setPaintFlags(holder.textViewWillDo.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        holder.textViewCreatedDate.setText(willDoItem.getCreatedDate().toString());
        holder.textViewDeadline.setText(willDoItem.getDeadLine().toString());

    }

    @Override
    public int getItemCount() {
        return willDoArrayList.size();
    }

    public class CardViewHolder extends RecyclerView.ViewHolder{

        private CardView cardViewWillDo;
        private ConstraintLayout cLayoutWillDo;
        private TextView textViewWillDo, textViewCreatedDate, textViewDeadline;

        public CardViewHolder(@NonNull View itemView) {
            super(itemView);
            cardViewWillDo = itemView.findViewById(R.id.cardViewWillDo);
            cLayoutWillDo = itemView.findViewById(R.id.cLayoutWillDo);
            textViewWillDo = itemView.findViewById(R.id.textViewWillDo);
            textViewCreatedDate = itemView.findViewById(R.id.textViewCreatedDate);
            textViewDeadline = itemView.findViewById(R.id.textViewDeadline);
        }
    }
}
