package com.example.cliff.fitnessapp;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import javax.microedition.khronos.egl.EGLDisplay;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment implements View.OnClickListener {


    private EditText ageText, genderText, heightText, weightText;

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View v = inflater.inflate(R.layout.fragment_profile, container, false);

        Button b = (Button) v.findViewById(R.id.submit);
        b.setOnClickListener(this);

        ageText = (EditText) v.findViewById(R.id.age_text_view);
        genderText = (EditText) v.findViewById(R.id.gender_text_view);
        heightText = (EditText) v.findViewById(R.id.height_text_view);
        weightText = (EditText) v.findViewById(R.id.weight_edit_text);

        updateEditText();

        return v;
    }

    public void onClick(View v)
    {
        int id = v.getId();

        if (id == R.id.submit)
        {
            int age = Integer.valueOf((((EditText)getView().findViewById(R.id.age_text_view)).getText().toString()));
            String gender = (String)(((EditText)getView().findViewById(R.id.gender_text_view)).getText().toString());
            String height = (String)(((EditText)getView().findViewById(R.id.height_text_view)).getText().toString());
            String weight = (String)(((EditText)getView().findViewById(R.id.weight_text_view)).getText().toString());

            System.out.println("AGE IS " + age);

            //pasted here
            SQLiteOpenHelper helper = new FitnessAppHelper(getActivity());
            SQLiteDatabase db = helper.getWritableDatabase();

            ContentValues value = new ContentValues();
            value.put("AGE", age);
            value.put("GENDER", gender);
            value.put("HEIGHT", height);
            value.put("WEIGHT", weight);


           // db.insert("PROFILE", null, value);
            db.update("PROFILE", value, null, null);

            Cursor cursor = db.rawQuery("SELECT * FROM PROFILE ORDER BY _id DESC LIMIT 1;", null);
            cursor.moveToFirst();

            int databaseAge = cursor.getInt(cursor.getColumnIndex("AGE"));
            System.out.println("THE AGE IN DATABASE IS " + databaseAge);

        }
    }

    private void updateEditText()
    {
        SQLiteOpenHelper helper = new FitnessAppHelper(getActivity());
        SQLiteDatabase db = helper.getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM PROFILE ORDER BY _id DESC LIMIT 1;", null);
        cursor.moveToFirst();

        int databaseAge = cursor.getInt(cursor.getColumnIndex("AGE"));

        ageText.setText(Integer.toString(databaseAge));

    }

}
