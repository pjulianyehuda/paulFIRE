package com.example.paulfire.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.RecyclerView;

import com.example.paulfire.Model.Course;
import com.example.paulfire.Model.Lecturer;
import com.example.paulfire.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class EnrollAdapter extends RecyclerView.Adapter<EnrollAdapter.CardViewViewHolder> {

    private Context context;
    private ArrayList<Course> listcourse = new ArrayList<>();
    DatabaseReference dbLecturer = FirebaseDatabase.getInstance().getReference("lecturer");
    DatabaseReference dbCourse = FirebaseDatabase.getInstance().getReference("course");
    MutableLiveData<String> lecturerName = new MutableLiveData<>();
    Lecturer lecturer;

    private ArrayList<Course> getListCourse() {
        return listcourse;
    }

    public void setListCourse(ArrayList<Course> listcourse) {
        this.listcourse = listcourse;
    }

    public EnrollAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public EnrollAdapter.CardViewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.enroll_adapter, parent, false);
        return new EnrollAdapter.CardViewViewHolder(view);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(@NonNull final EnrollAdapter.CardViewViewHolder holder, final int position) {
        final DatabaseReference dbStudent = FirebaseDatabase.getInstance().getReference("student").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("courses");
        final Course course = getListCourse().get(position);
        dbLecturer.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    lecturer = childSnapshot.getValue(Lecturer.class);
                    if (course.getLecturer().equals(lecturer.getId())) {
                        lecturerName.setValue(lecturer.getName());
                        Log.d("lectname", "" + lecturerName.getValue());
                        break;
                    }
                }
                Log.d("lectid", "" + course.getLecturer());
                Log.d("subjname", "" + course.getSubject());
                if (lecturerName != null) {
                    holder.lbl_subject.setText(course.getSubject());
                    holder.lbl_schedule.setText(course.getDay() + ", " + course.getStart() + " - " + course.getEnd());
                    holder.lbl_lecturer.setText(lecturerName.getValue());
                    holder.btn_enroll.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            new AlertDialog.Builder(context)
                                    .setTitle("Konfirmasi")
                                    .setMessage("Are you sure to enroll in " + course.getSubject() + " ?")
                                    .setCancelable(false)
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(final DialogInterface dialogInterface, int i) {
                                            checkStudentCourses(dbStudent, dbCourse, course);
                                            dialogInterface.cancel();
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


    private void checkStudentCourses(final DatabaseReference dbStudent, final DatabaseReference dbCourse, final Course course) {
        final ArrayList<String> coursesTaken = new ArrayList<>();
        dbStudent.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    coursesTaken.add((String) childSnapshot.child("id").getValue());
                }
                checkCourseTimeConflict(dbStudent, dbCourse, course, coursesTaken);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void checkCourseTimeConflict(final DatabaseReference dbStudent, DatabaseReference dbCourse, final Course selectedCourse, final ArrayList<String> coursesTaken) {
        final MutableLiveData<Boolean> checkOverlap = new MutableLiveData<>();
        final int selectedCourseStart = Integer.parseInt(selectedCourse.getStart().replace(":", ""));
        final int selectedCourseEnd = Integer.parseInt(selectedCourse.getEnd().replace(":", ""));
        dbCourse.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                checkOverlap.setValue(false);
                int i = 0;
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    i++;
                    Course course = childSnapshot.getValue(Course.class);
                    for (int j = 0; j < coursesTaken.size(); j++) {
                        if (coursesTaken.get(j).equals(course.getId())) {
                            if (selectedCourse.getDay().equals(course.getDay())) {
                                int checkCoursesDataStart = Integer.parseInt(selectedCourse.getStart().replace(":", ""));
                                int checkCoursesDataEnd = Integer.parseInt(selectedCourse.getEnd().replace(":", ""));
                                if ((checkCoursesDataStart > selectedCourseStart && selectedCourseEnd > checkCoursesDataEnd) || (checkCoursesDataStart <= selectedCourseStart && selectedCourseStart < checkCoursesDataEnd) || (checkCoursesDataStart < selectedCourseEnd && selectedCourseEnd <= checkCoursesDataEnd)) {
                                    checkOverlap.setValue(true);
                                    break;
                                }
                            }
                        }
                    }
                    if (snapshot.getChildrenCount() == i && checkOverlap.getValue() == null) {
                        checkOverlap.setValue(false);
                    }
                }
                if (checkOverlap.getValue() != null) {
                    if (checkOverlap.getValue()) {
                        Toast.makeText(context, "You are already enrolled in a Course during those hours!", Toast.LENGTH_SHORT).show();
                    } else {
                        dbStudent.child(selectedCourse.getId()).child("id").setValue(selectedCourse.getId()).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(context, "Course added successfully!", Toast.LENGTH_SHORT).show();

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(context, "Failed to add course!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
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
        Button btn_enroll;

        CardViewViewHolder(View itemView) {
            super(itemView);
            lbl_subject = itemView.findViewById(R.id.enrollAdapterName);
            lbl_schedule = itemView.findViewById(R.id.enrollAdapterSchedule);
            lbl_lecturer = itemView.findViewById(R.id.enrollAdapterLecturer);
            btn_enroll = itemView.findViewById(R.id.enrollAdapterEnrollButton);
        }
    }
}
