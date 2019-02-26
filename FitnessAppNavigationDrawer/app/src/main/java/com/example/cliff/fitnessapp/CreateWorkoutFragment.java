package com.example.cliff.fitnessapp;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class CreateWorkoutFragment extends Fragment implements View.OnClickListener {


    private ArrayList<WeightExercise> exerciseList;
    private String workoutNames = "";

    //Required empty public constructor
    public CreateWorkoutFragment() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        exerciseList = new ArrayList<>();

        View v = inflater.inflate(R.layout.fragment_create_workout, container, false);

        Button addExerciseButton = (Button) v.findViewById(R.id.add_exercise_button);
        addExerciseButton.setOnClickListener(this);

        Button createWorkoutButton = (Button) v.findViewById(R.id.create_workout_button);
        createWorkoutButton.setOnClickListener(this);

        return v;
    }

    @Override
    public void onStart()
    {
        Spinner spinner = (Spinner) getView().findViewById(R.id.name_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.workouts_array, R.layout.support_simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        super.onStart();
    }

    //TODO make it where two workouts with the same name cannot be created.
    public void onClick(View v)
    {
        int id = v.getId();

        if (id == R.id.add_exercise_button)
        {
            //Get data from necessary fields and add it to the exercise ArrayList
            String workoutName = (String)(((Spinner)getView().findViewById(R.id.name_spinner)).getSelectedItem().toString());
            int reps = Integer.valueOf((((EditText)getView().findViewById(R.id.rep_edit_text)).getText().toString()));
            int repCount = Integer.valueOf((((EditText)getView().findViewById(R.id.rep_count_edit_text)).getText().toString()));
            int weight = Integer.valueOf((((EditText)getView().findViewById(R.id.weight_edit_text)).getText().toString()));

            WeightExercise exerciseToAdd = new WeightExercise(workoutName, reps, repCount, weight);
            exerciseList.add(exerciseToAdd);

            displayAddedExercises();
        }
        else if (id == R.id.create_workout_button)
        {
            System.out.println("CREATE WORKOUT IS PRESSED");

            SQLiteOpenHelper helper = new FitnessAppHelper(getActivity());
            SQLiteDatabase db = helper.getWritableDatabase();

            EditText workoutNameTextView = getView().findViewById(R.id.workout_name);
            String workoutName = workoutNameTextView.getText().toString();

            ContentValues value = new ContentValues();
            value.put("NAME", workoutName);

            db.insert("WORKOUT", null, value);

            Cursor cursor = db.rawQuery("SELECT * FROM WORKOUT ORDER BY _id DESC LIMIT 1;", null);
            cursor.moveToFirst();

            String name = cursor.getString(cursor.getColumnIndex("NAME"));
            int workoutID = cursor.getInt(0);

            System.out.println("WORKOUT ID: " + workoutID);

            addExercisesToDatabase(workoutID);
            exerciseList.clear();

            Cursor workoutCursor = db.rawQuery("SELECT * FROM EXERCISE WHERE WORKOUT = " + workoutID + "", null);

            displayAddedWorkouts(workoutCursor);
        }
    }

    private void addExercisesToDatabase(int workoutId)
    {
        SQLiteOpenHelper helper = new FitnessAppHelper(getActivity());

        for (int i = 0; i < exerciseList.size(); i ++)
        {
            Exercise exercise = exerciseList.get(i);

            String name = exercise.getName();
            int reps = exercise.getReps();
            int repCount = exercise.getNumberOfReps();

            ContentValues value = new ContentValues();
            value.put("NAME", name);
            value.put("REPS", reps);
            value.put("REPCOUNT", repCount);
            value.put("WORKOUT", workoutId);

            try
            {
                SQLiteDatabase db = helper.getWritableDatabase();
                db.insert("EXERCISE", null, value);
            }
            catch(SQLiteException e)
            {
                Toast toast = Toast.makeText(getActivity(), "Database Unavailable", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }


    private void displayAddedExercises()
    {
        String exerciseNames = "";

        for (int i = 0; i < exerciseList.size(); i ++)
        {
            exerciseNames += " " + exerciseList.get(i).getName();
        }

        TextView addedExercises = getView().findViewById(R.id.exercise_database_contents);
        addedExercises.setText(exerciseNames);
    }

    private void displayAddedWorkouts(Cursor cursor)
    {
        cursor.moveToFirst();

        do
        {
            workoutNames += cursor.getString(cursor.getColumnIndex("NAME"));
        }
        while(cursor.moveToNext());

        workoutNames += "\n";

        TextView workoutView = getView().findViewById(R.id.workout_database_contents);
        workoutView.setText(workoutNames);

    }
}
