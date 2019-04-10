package com.example.cliff.fitnessapp;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.icu.text.SymbolTable;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class PerformWorkoutActivity extends AppCompatActivity {

    private String workoutName;
    private ArrayList<Exercise> exerciseList = new ArrayList<>();
    private boolean[] wasExerciseSkippedArray;
    private int currentExerciseIndex;

    private boolean isResting;

    private boolean isWorkoutFinished;

    private int repCounter;
    private int setCounter;
    private String counterButtonText;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        currentExerciseIndex = 0;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perform_workout);

        isResting = false;

        Toolbar toolbar = (Toolbar) findViewById(R.id.workout_toolbar);
        setSupportActionBar(toolbar);

        getWorkoutDetails();
        updateTextViews();

        //initially the user has done zero reps and is on the first set
        resetRepsAndSets();
    }

    private void resetRepsAndSets()
    {
        repCounter = 0;
        setCounter = 1;
    }

    //this will get the workouts name and populate the exercise ArrayList
    private void getWorkoutDetails()
    {
        int workoutID = getIntent().getIntExtra("workoutID", 0);

        FitnessAppHelper helper = new FitnessAppHelper(this);
        SQLiteDatabase db = helper.getReadableDatabase();

        //getting the workout name
        Cursor cursor = db.rawQuery("SELECT * FROM WORKOUT WHERE _id = " + workoutID, null);
        cursor.moveToFirst();
        workoutName = cursor.getString(cursor.getColumnIndex("NAME"));

        //populating the exercise ArrayList
        cursor = db.rawQuery("SELECT * FROM EXERCISE WHERE WORKOUT = " + workoutID, null);
        cursor.moveToFirst();
        do
        {
            String exerciseName = cursor.getString(cursor.getColumnIndex("NAME"));
            int reps = cursor.getInt(cursor.getColumnIndex("REPS"));
            int sets = cursor.getInt(cursor.getColumnIndex("SETS"));
            int weight = cursor.getInt(cursor.getColumnIndex("WEIGHT"));

            Exercise exercise = new Exercise(exerciseName, reps, sets, weight);
            exerciseList.add(exercise);
        }while (cursor.moveToNext());

       setSizeOfWasExerciseSkippedArray();
    }

    private void setSizeOfWasExerciseSkippedArray()
    {
        wasExerciseSkippedArray = new boolean[exerciseList.size()];
    }

    private void updateTextViews()
    {
        TextView nameTextView = findViewById(R.id.workout_name);
        nameTextView.setText(workoutName);

        Exercise currentExercise = exerciseList.get(currentExerciseIndex);

        TextView exerciseTextView = findViewById(R.id.exercise_instructions);
        String exerciseInstructions = String.format("%dx%d %s at %d lbs", currentExercise.getSets(), currentExercise.getReps(), currentExercise.getName(), currentExercise.getWeight());
        exerciseTextView.setText(exerciseInstructions);
    }

    public void incrementCounter(View v)
    {
        if (!isResting)
        {
            Exercise currentExercise = exerciseList.get(currentExerciseIndex);
            Button button = (Button) findViewById(R.id.counter_button);

            //if the user has completed all the reps in a set and is not on the last set
            //then the rep counter is set to 0 and the set is incremented
            if (repCounter == currentExercise.getReps() - 1 && setCounter < currentExercise.getSets())
            {
                repCounter = 0;
                setCounter ++;
            }
            //if the user has completed the reps in a set and is on the last set then the next exercise must be launched
            else if (repCounter == currentExercise.getReps() - 1 && setCounter == currentExercise.getSets())
            {
                resetRepsAndSets();
                markCurrentExerciseAsCompleted();
                goToNextExerciseOrGoToMainActivity();
            }
            //otherwise the user is still in the same set and the repCounter is incremented
            else
            {
                repCounter ++;
            }


            String buttonText = String.format("%dx%d", setCounter, repCounter);
            setCounterButtonText(buttonText);
            button.setText(buttonText);
        }

    }

    private String setCounterButtonText(String buttonText) {
        return this.counterButtonText = buttonText;
    }

    private void markCurrentExerciseAsCompleted()
    {
        wasExerciseSkippedArray[currentExerciseIndex] = false;
    }

    public void goToNextExercise()
    {
        currentExerciseIndex++;
        updateTextViews();
    }

    private void goToNextExerciseOrGoToMainActivity ()
    {
        if (isNotLastExercise())
        {
            goToNextExercise();
        }
        else
        {
            updateDatabaseWithCompletedExercises(getDatabase());
            returnToMainActivity();
        }
    }

    private SQLiteDatabase getDatabase()
    {
        FitnessAppHelper helper = new FitnessAppHelper(this);
        return helper.getReadableDatabase();
    }

    private void updateDatabaseWithCompletedExercises(SQLiteDatabase db)
    {
        getCompletedExercises();
        addCompletedExercisesToDatabase(getCompletedExercises(), db);
    }

    private ArrayList<Exercise> getCompletedExercises()
    {
        ArrayList<Exercise> completedExerciseList = new ArrayList<>();

        for (int i = 0; i < wasExerciseSkippedArray.length; i++)
        {
            if (exerciseWasCompleted(i))
            {
                completedExerciseList.add(exerciseList.get(i));
            }
        }

        return completedExerciseList;
    }

    private void addCompletedExercisesToDatabase(ArrayList<Exercise> exercisesToAdd, SQLiteDatabase db)
    {
        ContentValues exerciseResults = new ContentValues();

        for (Exercise e : exercisesToAdd)
        {
            int id = findCorrespondingIdOfExercise(e, db);

            exerciseResults.put("NAME", e.getName());
            exerciseResults.put("WEIGHT", e.getWeight());
            exerciseResults.put("REPS", e.getReps());
            exerciseResults.put("SETS", e.getSets());
            exerciseResults.put("DEFINEDEXERCISEID", id);

            db.insert("EXERCISERESULTS", null, exerciseResults);
            exerciseResults.clear();
        }
    }

    private int findCorrespondingIdOfExercise(Exercise e, SQLiteDatabase db)
    {
        Cursor cursor = db.rawQuery("SELECT * FROM DEFINEDEXERCISE WHERE NAME = ? ", new String[]{e.getName()});
        cursor.moveToFirst();
        return cursor.getInt(0);
    }

    private boolean exerciseWasCompleted(int exerciseIndex)
    {
        return !wasExerciseSkippedArray[exerciseIndex];
    }

    private boolean isNotLastExercise() {
        return (currentExerciseIndex < (exerciseList.size() - 1));
    }

    private void returnToMainActivity()
    {
        //TODO remove this
        String testString = "";
        for (boolean exerciseBoolean : wasExerciseSkippedArray)
        {
            testString += exerciseBoolean;
            testString += " ";
        }
        System.out.println("EXERCISE IS FINISHED!");
        System.out.println(testString);

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void skipExercise(View v)
    {
        resetRepsAndSets();
        markCurrentExerciseAsSkipped();
        goToNextExerciseOrGoToMainActivity();
    }

    private void markCurrentExerciseAsSkipped()
    {
        wasExerciseSkippedArray[currentExerciseIndex] = true;
    }

    //The back button needs to be disabled in this activity
    //If the user wants to quit a workout they will use the cancel workout button
    @Override
    public void onBackPressed()
    {
        Toast toast = Toast.makeText(getApplicationContext(),
                "Press cancel workout if you want to go back!",
                Toast.LENGTH_SHORT);
        toast.show();
    }

    //confirms whether or not the user wants to cancel the workout when they press the cancel button
    public void confirmCancelWorkout(View v)
    {
        AlertDialog diaBox = askOption();
        diaBox.show();
    }

    private void cancelWorkout()
    {
        //for now when the workout is cancelled it will just take you back to the main activity
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    //Used to create dialog box to confirm that the user wants to cancel a workout
    private AlertDialog askOption()
    {
        AlertDialog deleteDialogBox = new AlertDialog.Builder(this)
                //set message, title, and icon
                .setTitle("Cancel Workout")
                .setMessage("Are you sure you want to cancel this workout?")
                //TODO change this icon, it is just to test
                .setIcon(R.drawable.ic_menu_send)

                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        cancelWorkout();
                        dialog.dismiss();
                    }

                })

                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();

                    }
                })
                .create();

        return deleteDialogBox;
    }

    public void restTimer(View v)
    {
        setCounterTextResting();
        Button restButton = (Button) v;
        int restTime = Integer.parseInt(restButton.getText().toString());
        restTimerHandler(restTime);
        System.out.println("Clicked button: " + restTime);
    }

    private void restTimerHandler(final int restTime) {

        final Handler handler = new Handler();
        handler.post(new Runnable() {
            int timer = restTime;

            @Override
            public void run() {
                timer--;
                System.out.println("Timer: " + timer);
                if (timer > 0) {
                    handler.postDelayed(this, 1000);
                }
            }

        });
    }

    private void setCounterTextResting()
    {
        isResting = true;
        Button b = findViewById(R.id.counter_button);
        b.setText("Resting");
    }

    private void setCounterTextNotResting()
    {
        isResting = false;
        Button b = findViewById(R.id.counter_button);
        b.setText(counterButtonText);
    }


}
