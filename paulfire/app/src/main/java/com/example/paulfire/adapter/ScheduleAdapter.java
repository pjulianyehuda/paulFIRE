package com.example.paulfire.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.RecyclerView;

import com.example.paulfire.Fragment.ScheduleFragment;
import com.example.paulfire.Model.Course;
import com.example.paulfire.Model.Lecturer;
import com.example.paulfire.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.CardViewViewHolder> {

    private Context context;
    private ArrayList<Course> listCourses;
    DatabaseReference dbLecturer = FirebaseDatabase.getInstance().getReference("lecturer");
    DatabaseReference dbStudent = FirebaseDatabase.getInstance().getReference("student").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("courses");
    MutableLiveData<String> lecturerName = new MutableLiveData<>();
    Lecturer lecturer;

    private ArrayList<Course> getListCourses() {
        return listCourses;
    }

    public void setListSchedule(ArrayList<Course> listCourses) {
        this.listCourses = listCourses;
    }

    public ScheduleAdapter(Context context) {
        this.context = context;
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(@NonNull final ScheduleAdapter.CardViewViewHolder holder, final int position) {
        final Course course = getListCourses().get(position);
        dbLecturer.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    lecturer = childSnapshot.getValue(Lecturer.class);
                    if (course.getLecturer().equals(lecturer.getId())) {
                        lecturerName.setValue(lecturer.getName());
                        break;
                    }
                }
                if (lecturerName !=null) {
                    holder.lbl_subject.setText(course.getSubject());
                    holder.lbl_subject.setText(course.getSubject());
                    holder.lbl_schedule.setText(course.getDay() + ", " + course.getStart() + " - " + course.getEnd());
                    holder.lbl_lecturer.setText(lecturerName.getValue());
                    holder.btn_delete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            new AlertDialog.Builder(context)
                                    .setTitle("Konfirmasi")
                                    .setMessage("Are you sure to unenroll from " + course.getSubject() + " ?")
                                    .setCancelable(false)
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(final DialogInterface dialogInterface, int i) {
                                            dbStudent.child(course.getId()).removeValue(new DatabaseReference.CompletionListener() {
                                                @Override
                                                public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                                    Toast.makeText(context, "Success!", Toast.LENGTH_SHORT).show();
                                                    ScheduleFragment.listCourses.remove(position);
                                                    notifyItemRemoved(position);
                                                }
                                            });
                                        }
                                    })
                                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                        }
                                    })
                                    .create()
                                    .show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @NonNull
    @Override
    public ScheduleAdapter.CardViewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.schedule_adapter, parent, false);
        return new ScheduleAdapter.CardViewViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return getListCourses().size();
    }

    class CardViewViewHolder extends RecyclerView.ViewHolder {
        TextView lbl_subject, lbl_schedule, lbl_lecturer;
        Button btn_delete;

        CardViewViewHolder(View itemView) {
            super(itemView);
            lbl_subject = itemView.findViewById(R.id.scheduleAdapterName);
            lbl_schedule = itemView.findViewById(R.id.scheduleAdapterSchedule);
            lbl_lecturer = itemView.findViewById(R.id.scheduleAdapterLecturer);
            btn_delete = itemView.findViewById(R.id.scheduleAdapterDeleteButton);
        }
    }
}