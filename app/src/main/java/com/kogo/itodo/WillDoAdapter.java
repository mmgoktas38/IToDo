package com.kogo.itodo;

import android.app.DatePickerDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class WillDoAdapter extends RecyclerView.Adapter<WillDoAdapter.CardViewHolder>{

    private Context mContext;
    private List<WillDo> willDoArrayList;

    public WillDoAdapter(Context mContext, List<WillDo> willDoArrayList){
        this.mContext = mContext;
        this.willDoArrayList = willDoArrayList;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_will_do, parent, false);

        return new CardViewHolder(view);
    }

    // method for filtering our recyclerview items.
    public void filterList(ArrayList<WillDo> filterllist) {
        willDoArrayList = filterllist;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {

        WillDo willDoItem = willDoArrayList.get(position);
        Log.e("size", String.valueOf(willDoArrayList.size()));
        Log.e("size", String.valueOf(willDoItem.getWillDoText()));
        FirebaseUser currentUser = null;
        FirebaseDatabase database;
        DatabaseReference reference;
        String onlineUserID;
        FirebaseAuth auth;

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        onlineUserID = currentUser.getUid();
        database = FirebaseDatabase.getInstance();
        System.out.println("id: : " + String.valueOf(onlineUserID)  + "   getid  " + willDoItem.getId());
        // reference = database.getReference().child("tasks").child(onlineUserID).child(willDoItem.getId());

        holder.textViewWillDo.setText(willDoItem.getWillDoText());
        holder.textViewCreatedDate.setText(willDoItem.getCreatedDate().toString());
        holder.textViewDeadline.setText(willDoItem.getDeadLine().toString());
        holder.cardViewWillDo.setOnClickListener(view -> {

            AlertDialog.Builder myDialog = new AlertDialog.Builder(mContext);
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View myView = inflater.inflate(R.layout.add_task,null);
            myDialog.setView(myView);
            final AlertDialog dialog = myDialog.create();
            dialog.setCancelable(false);

            final EditText editTextAddTask = myView.findViewById(R.id.editTextAddTask);
            final EditText editTextDeadline = myView.findViewById(R.id.editTextDeadline);
            TextView textViewSave = myView.findViewById(R.id.textViewSave);
            TextView textViewCancel = myView.findViewById(R.id.textViewCancel);
            textViewSave.setText("Update");

            editTextAddTask.setText(willDoItem.getWillDoText());
            editTextDeadline.setText(willDoItem.getDeadLine());

            editTextDeadline.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final Calendar newCalendar = Calendar.getInstance();
                    final DatePickerDialog StartTime = new DatePickerDialog(mContext, new DatePickerDialog.OnDateSetListener() {
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                            Calendar newDate = Calendar.getInstance();
                            newDate.set(year, monthOfYear, dayOfMonth);
                            DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(mContext);
                            editTextDeadline.setText(dateFormat.format(newDate.getTime()));
                        }

                    }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));

                    StartTime.show();
                }
            });
            textViewCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });

            dialog.show();

        });
        holder.cardViewWillDo.setOnLongClickListener(view -> {
           // reference.removeValue();
            Log.e("silindi", String.valueOf(willDoItem.getId()));
            return true;
        });

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
