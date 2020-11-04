package com.example.paulfire.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.RecyclerView;

import com.example.paulfire.AddCourse;
import com.example.paulfire.Model.Course;
import com.example.paulfire.Model.Lecturer;
import com.example.paulfire.Model.Student;
import com.example.paulfire.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class CourseAdapter  extends RecyclerView.Adapter<CourseAdapter.CardViewViewHolder> {

    private Context context;
    private ArrayList<Course> listCourse;

    private ArrayList<Course> getListCourse() {
        return listCourse;
    }

    public void setListCourse(ArrayList<Course> listCourse) {
        this.listCourse = listCourse;
    }

    public CourseAdapter(Context context) {
        this.context = context;
    }

    DatabaseReference dbCourse = FirebaseDatabase.getInstance().getReference("course");
    DatabaseReference dbLecturer = FirebaseDatabase.getInstance().getReference("lecturer");
    DatabaseReference dbStudent = FirebaseDatabase.getInstance().getReference("student");
    MutableLiveData<String> lecturerName = new MutableLiveData<>();
    Lecturer lecturer;
    Student student;

    @NonNull
    @Override
    public CourseAdapter.CardViewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_course_adapter, parent, false);
        return new CourseAdapter.CardViewViewHolder(view);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(@NonNull final CourseAdapter.CardViewViewHolder holder, int position) {
        final Course course = getListCourse().get(position);
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
                if (lecturerName != null) {
                    holder.lbl_subject.setText(course.getSubject());
                    holder.lbl_schedule.setText(course.getDay() + ", " + course.getStart() + " - " + course.getEnd());
                    holder.lbl_lecturer.setText(lecturerName.getValue());
                    holder.btn_del.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            new AlertDialog.Builder(context)
                                    .setTitle("Konfirmasi")
                                    .setMessage("Are you sure to delete this " + course.getSubject() + " ?")
                                    .setCancelable(false)
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(final DialogInterface dialogInterface, int i) {
                                            dbStudent.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                                                        student = childSnapshot.getValue(Student.class);
                                                        dbStudent.child(student.getUid()).child("courses").addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                                                                    String cID = (String) childSnapshot.child("id").getValue();
                                                                    Log.d("cekcid", cID);
                                                                    Log.d("cekkorsid", course.getId());
                                                                    if (cID.equals(course.getId())) {
                                                                        dbStudent.child(student.getUid()).child("courses").child(cID).removeValue();
                                                                    }
                                                                }
                                                            }

                                                            @Override
                                                            public void onCancelled(@NonNull DatabaseError error) {
                                                            }
                                                        });
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {
                                                }
                                            });
                                            dbCourse.child(course.getId()).removeValue(new DatabaseReference.CompletionListener() {
                                                @Override
                                                public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                                    Toast.makeText(context, "Delete success!", Toast.LENGTH_SHORT).show();
                                                    dialogInterface.cancel();
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

                    holder.btn_edit.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent in = new Intent(context, AddCourse.class);
                            in.putExtra("action", "edit");
                            in.putExtra("editCourse", course);
                            in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            context.startActivity(in);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    @Override
    public int getItemCount() {
        return getListCourse().size();
    }

    class CardViewViewHolder extends RecyclerView.ViewHolder {

        TextView lbl_subject, lbl_schedule, lbl_lecturer;
        ImageView btn_edit, btn_del;

        CardViewViewHolder(View itemView) {
            super(itemView);
            lbl_subject = itemView.findViewById(R.id.courseAdapterName);
            lbl_schedule = itemView.findViewById(R.id.courseAdapterSchedule);
            lbl_lecturer = itemView.findViewById(R.id.courseAdapterLecturer);
            btn_del = itemView.findViewById(R.id.courseAdapterDeleteButton);
            btn_edit = itemView.findViewById(R.id.courseAdapterEditButton);

        }
    }
}