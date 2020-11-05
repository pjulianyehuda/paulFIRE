package com.example.paulfire;

import android.app.ActivityOptions;
import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.paulfire.Model.Course;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddCourse extends AppCompatActivity {

    Toolbar bar;
    Dialog dialog;
    Spinner spinner_day, spinner_start, spinner_end, spinner_lecturer;
    TextInputEditText input_subject;
    String subject = "", day = "", start = "", end = "", lecturer = "" ,action = "";
    Button btn_add;
    Course course;
    private DatabaseReference mDatabase;
    FirebaseDatabase dbLecturer = FirebaseDatabase.getInstance();
    ArrayAdapter<CharSequence> adapterend;

    List<String> names;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_course);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        bar = findViewById(R.id.toolbar_AddCourse);

        setSupportActionBar(bar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        input_subject = findViewById(R.id.coursesub);
        input_subject.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        spinner_day = findViewById(R.id.spinner_day);
        ArrayAdapter<CharSequence> adapterday = ArrayAdapter.createFromResource(AddCourse.this,
                R.array.day, android.R.layout.simple_spinner_item);
        adapterday.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_day.setAdapter(adapterday);
        spinner_start = findViewById(R.id.spinner_time);
        spinner_end = findViewById(R.id.spinner_time_end);
        ArrayAdapter<CharSequence> adapterstart = ArrayAdapter.createFromResource(AddCourse.this,
                R.array.time, android.R.layout.simple_spinner_item);
        adapterstart.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_start.setAdapter(adapterstart);


        spinner_start.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                adapterend = null;
                setSpinner_end(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spinner_lecturer = findViewById(R.id.spinner_lecturer);

        btn_add = findViewById(R.id.button_add_course);

        //proses add dan edit
        Intent intent = getIntent();
        action = intent.getStringExtra("action");
        if (action.equals("add")){
            getSupportActionBar().setTitle(R.string.addcourse);
            btn_add.setText(R.string.addcourse);
            btn_add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    subject = input_subject.getText().toString().trim();
                    day = spinner_day.getSelectedItem().toString();
                    start = spinner_start.getSelectedItem().toString();
                    end = spinner_end.getSelectedItem().toString();
                    lecturer = spinner_lecturer.getSelectedItem().toString();
                    addCourse(subject,day,start,end,lecturer);
                }
            });
        }else if (action.equalsIgnoreCase("edit")){ //edit
            getSupportActionBar().setTitle(R.string.editcourse);
            course = intent.getParcelableExtra("editCourse");
            input_subject.setText(course.getSubject());
            int dayIndex = adapterday.getPosition(course.getDay());
            spinner_day.setSelection(dayIndex);
            int startIndex = adapterstart.getPosition(course.getStart());
            spinner_start.setSelection(startIndex);
            setSpinner_end(startIndex);
            final int endIndex = adapterend.getPosition(course.getEnd());
            spinner_end.setSelection(endIndex);

            btn_add.setText(R.string.editcourse);
            btn_add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    subject = input_subject.getText().toString().trim();
                    day = spinner_day.getSelectedItem().toString();
                    start = spinner_start.getSelectedItem().toString();
                    end = spinner_end.getSelectedItem().toString();
                    lecturer = spinner_lecturer.getSelectedItem().toString();
                    Map<String,Object> params = new HashMap<>();
                    params.put("subject", subject);
                    params.put("day", day);
                    params.put("start",start);
                    params.put("end",end);
                    params.put("lecturer",lecturer);
                    mDatabase.child("course").child(course.getId()).updateChildren(params).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                        @Override
                        public void onSuccess(Void aVoid) {
                            Intent intent;
                            intent = new Intent(AddCourse.this, CourseData.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(AddCourse.this);
                            startActivity(intent, options.toBundle());
                            finish();
                        }
                    });
                }
            });
        }
        names = new ArrayList<>();
        showSpinnerLecturer();
    }

    public void addCourse(String msubject, String mday, String mstart, String mend, String mlecture) {//add course
        String mid = mDatabase.child("course").push().getKey();
        Course course = new Course(mid,msubject, mday, mstart, mend, mlecture);
        mDatabase.child("course").child(mid).setValue(course).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(AddCourse.this, "Add Course Successfully", Toast.LENGTH_SHORT).show();
                input_subject.setText("");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialog.cancel();
                Toast.makeText(AddCourse.this, "Add Course Failed!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setSpinner_end(int position){
        if(position==0){
            adapterend = ArrayAdapter.createFromResource(AddCourse.this, R.array.jam_end0730, android.R.layout.simple_spinner_item);
        }else if(position==1){
            adapterend = ArrayAdapter.createFromResource(AddCourse.this, R.array.jam_end0800, android.R.layout.simple_spinner_item);
        }else if(position==2){
            adapterend = ArrayAdapter.createFromResource(AddCourse.this, R.array.jam_end0830, android.R.layout.simple_spinner_item);
        }else if(position==3){
            adapterend = ArrayAdapter.createFromResource(AddCourse.this, R.array.jam_end0900, android.R.layout.simple_spinner_item);
        }else if(position==4){
            adapterend = ArrayAdapter.createFromResource(AddCourse.this, R.array.jam_end0930, android.R.layout.simple_spinner_item);
        }else if(position==5){
            adapterend = ArrayAdapter.createFromResource(AddCourse.this, R.array.jam_end1000, android.R.layout.simple_spinner_item);
        }else if(position==6){
            adapterend = ArrayAdapter.createFromResource(AddCourse.this, R.array.jam_end1030, android.R.layout.simple_spinner_item);
        }else if(position==7){
            adapterend = ArrayAdapter.createFromResource(AddCourse.this, R.array.jam_end1100, android.R.layout.simple_spinner_item);
        }else if(position==8){
            adapterend = ArrayAdapter.createFromResource(AddCourse.this, R.array.jam_end1130, android.R.layout.simple_spinner_item);
        }else if(position==9){
            adapterend = ArrayAdapter.createFromResource(AddCourse.this, R.array.jam_end1200, android.R.layout.simple_spinner_item);
        }else if(position==10){
            adapterend = ArrayAdapter.createFromResource(AddCourse.this, R.array.jam_end1230, android.R.layout.simple_spinner_item);
        }else if(position==11){
            adapterend = ArrayAdapter.createFromResource(AddCourse.this, R.array.jam_end1300, android.R.layout.simple_spinner_item);
        }else if(position==12){
            adapterend = ArrayAdapter.createFromResource(AddCourse.this, R.array.jam_end1330, android.R.layout.simple_spinner_item);
        }else if(position==13){
            adapterend = ArrayAdapter.createFromResource(AddCourse.this, R.array.jam_end1400, android.R.layout.simple_spinner_item);
        }else if(position==14){
            adapterend = ArrayAdapter.createFromResource(AddCourse.this, R.array.jam_end1430, android.R.layout.simple_spinner_item);
        }else if(position==15){
            adapterend = ArrayAdapter.createFromResource(AddCourse.this, R.array.jam_end1500, android.R.layout.simple_spinner_item);
        }else if(position==16){
            adapterend = ArrayAdapter.createFromResource(AddCourse.this, R.array.jam_end1530, android.R.layout.simple_spinner_item);
        }else if(position==17){
            adapterend = ArrayAdapter.createFromResource(AddCourse.this, R.array.jam_end1600, android.R.layout.simple_spinner_item);
        }else if(position==18){
            adapterend = ArrayAdapter.createFromResource(AddCourse.this, R.array.jam_end1630, android.R.layout.simple_spinner_item);
        }

        adapterend.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_end.setAdapter(adapterend);
    }

    public void showSpinnerLecturer(){ //showing spinner lecturer
        mDatabase.child("lecturer").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot childSnapshot:snapshot.getChildren()) {
                    String spinnerName = childSnapshot.child("name").getValue(String.class);
                    names.add(spinnerName);
                    Log.d("lecturer",spinnerName);
                }
                ArrayAdapter<String> arrayAdapter=new ArrayAdapter<>(AddCourse.this, android.R.layout.simple_spinner_dropdown_item,names);
                arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
                spinner_lecturer.setAdapter(arrayAdapter);
                if (action.equalsIgnoreCase("edit")){
                    int lecturerIndex = arrayAdapter.getPosition(course.getLecturer());
                    spinner_lecturer.setSelection(lecturerIndex);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.course_menu, menu);
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home){
            Intent intent;
            intent = new Intent(AddCourse.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(AddCourse.this);
            startActivity(intent, options.toBundle());
            finish();
            return true;
        }else if(id == R.id.course_list){
            Intent intent;
            intent = new Intent(AddCourse.this, CourseData.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(AddCourse.this);
            startActivity(intent, options.toBundle());
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBackPressed() {
        Intent intent;
        intent = new Intent(AddCourse.this, Starter.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(AddCourse.this);
        startActivity(intent, options.toBundle());
        finish();
    }


}