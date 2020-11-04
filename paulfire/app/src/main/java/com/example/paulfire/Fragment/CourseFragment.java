package com.example.paulfire.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.paulfire.Model.Course;
import com.example.paulfire.R;
import com.example.paulfire.adapter.EnrollAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;



public class CourseFragment extends Fragment {

    RecyclerView EnroolDataRV;
    RecyclerView courseDataRV;

    DatabaseReference dbCourse = FirebaseDatabase.getInstance().getReference("course");
    ArrayList<Course> listCourse = new ArrayList<>();

    public CourseFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_course, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        EnroolDataRV = view.findViewById(R.id.enrollDataRV);
        dbCourse.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listCourse.clear();
                EnroolDataRV.setAdapter(null);
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    Course course = childSnapshot.getValue(Course.class);
                    listCourse.add(course);
                }
                EnroolDataRV.setLayoutManager(new LinearLayoutManager(getActivity()));
                EnrollAdapter EnrollAdapter = new EnrollAdapter(getActivity());
                EnrollAdapter.setListCourse(listCourse);
                EnrollAdapter.notifyDataSetChanged();
                EnroolDataRV.setAdapter(EnrollAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}