package com.soni.RandomWorkout;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import android.content.Intent;

//This import was added because the original manifest package name (and App ID) was com.thesoni.sonstar.randomworkout
//For the 2020 rewrite, I simplified the Java package name to com.soni.RandomWorkout;
//But for the manifest.xml to have a different package, this R needs to be imported under the original package name
import com.thesoni.sonstar.randomworkout.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import android.content.res.AssetManager;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener,  AdapterView.OnItemClickListener {

    // AdapterView.OnItemSelectedListener is to respond to changing the Minutes in the Spinner.   To set maxMins variable
    // AdapterView.OnItemClickListener is to respond to selecting/clicking a routine in the ListView, to show the YouTube video
    // OnClickListener() is to respond to the Generate Workout button.   Declared/implemented inline, so "this" does not implement that interface

    XmlPullParserFactory pullParserFactory;
    XmlPullParser parser;

    //routinesList is an array of ALL routines
    //workoutRoutinesList is an array of SELECTED routines for the current workout
    ArrayList<Exercise> routinesList = new ArrayList<Exercise>();
    ArrayList<Exercise> workoutRoutinesList = new ArrayList<Exercise>();

    private static final String TAG = "Soni Debug";
    private static final String XML_FILE = "routines.xml";
    int maxMins = 0;
    String[] timeChoices = new String[]{"1", "4", "8", "12", "20"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //Auto generated.
        //Notice the layout matches the layout XML file name
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Load the minutes dropDown box
        loadMinutesSpinner();

        //Get reference to button so we can set the event listener
        Button btn = (Button) findViewById(R.id.btnGenerateWorkout);

        //This doesn't work b/c the default Spinner choice enables the button
        btn.setEnabled(false);

        btn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                //Display XML file (for test purposes)
                String xml = getXMLFile();
                Log.v(TAG, xml);
                Log.v(TAG, "Done Printing XML");

                readXML();
                Log.v(TAG, "Done Loading XML into Parser");

                parseXML();
                printArray(routinesList);
                Log.v(TAG, "Done Parsing XML into routinesList array");

                buildWorkout();
                Log.v(TAG, "Done adding selected routines into workoutRoutinesList array");

                displayWorkout();
                Log.v(TAG, "Done displaying workoutRoutinesList array in ListView");
            }
        });


    }

    private void loadMinutesSpinner() {
        Spinner dropdown = (Spinner) findViewById(R.id.spinnerMinutes);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, timeChoices);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dropdown.setAdapter(adapter);
        dropdown.setOnItemSelectedListener(this);
        //dropdown.setSelection(1);
    }

    // This is for the ListView to show YouTube video
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        String url;
        Exercise ex;

        //Get exercise object from array, based on selected position parm
        ex = workoutRoutinesList.get(position);
        url = ex.getURL();

        //open the URL
        try{
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), R.string.NoVid, Toast.LENGTH_LONG).show();
        }

        Log.e(TAG, "Clicked " + position);
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String val = (String) parent.getItemAtPosition(position);

        //Set the minutes to be the selected value in the Spinner
        maxMins = Integer.parseInt(val);
        Log.v(TAG, "maxMins: " + maxMins);

        //The old and long way
        /*
        switch (position) {
            case 0:
                maxMins = 1;
                break;
            case 1:
                maxMins = 4;
                break;
            case 2:
                maxMins = 8;
                break;
            case 3:
                maxMins = 12;
                break;
            case 4:
                maxMins = 20;
                break;
        }
        */

        //Enable the Submit button once they select a duration
        Button btn = (Button) findViewById(R.id.btnGenerateWorkout);
        btn.setEnabled(true);

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    private void readXML() {
        try {
            pullParserFactory = XmlPullParserFactory.newInstance();
            parser = pullParserFactory.newPullParser();

            AssetManager assetManager = getAssets();
            InputStream inputStream = null;
            inputStream = assetManager.open(XML_FILE);

            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(inputStream, null);

        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
    }

    //This method is called by printXMLFile
    //The app does not use these methods, it is just for testing readability of the XML file
    private String readTextFile(InputStream inputStream) {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int len;

        try {
            while ((len = inputStream.read(buf)) != -1) {
                outputStream.write(buf, 0, len);
            }
            outputStream.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outputStream.toString();
    }

    //Read XML file for simply printing to screen
    //The app does not use this method, just for test purposes
    private String getXMLFile() {

        InputStream inputStream;
        String s = null;
        AssetManager assetManager = getAssets();

        try {
            inputStream = assetManager.open(XML_FILE);
            s = readTextFile(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.v(TAG, s);
        return s;
    }

    private void parseXML() {
        int i = 0;

        try {

            int eventType = parser.getEventType();
            Exercise currentRoutine = null;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String name = null;
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        routinesList = new ArrayList<Exercise>();
                        break;
                    case XmlPullParser.START_TAG:
                        name = parser.getName();
                        if (name.equals("ROUTINE")) {
                            currentRoutine = new Exercise();
                        } else if (currentRoutine != null) {
                            switch (name) {
                                case "Name":
                                    currentRoutine.name = parser.nextText();
                                    break;
                                case "URL":
                                    currentRoutine.URL = parser.nextText();
                                    break;
                                case "Location":
                                    currentRoutine.location = parser.nextText();
                                    break;
                                case "Minutes":
                                    currentRoutine.minutes = Float.parseFloat(parser.nextText());
                                    break;
                                case "BodyPart":
                                    currentRoutine.bodyPart = parser.nextText();
                                    break;
                                case "Skip":
                                    currentRoutine.skip = parser.nextText();
                                    break;
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        name = parser.getName();
                        if (name.equalsIgnoreCase("routine") && currentRoutine != null) {
                            routinesList.add(currentRoutine);
                            Log.v(TAG, "Created routine #" + i++ + ":  " + currentRoutine.toString());
                        }
                }
                eventType = parser.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public void buildWorkout() {

        float totMins = 0;
        int shortWorkoutMins = 10;
        int i = 0;
        Exercise currRoutine = null;
        String body_part_code;
        String body_part_code_A = "y";
        String body_part_code_B = "z";
        String skip;
        String skip_code_A = "b";
        String skip_code_B = "c";
        RadioButton radIndoorWorkout;
        boolean indoorWorkout = true;


        //clear array in case they already generated a workout,
        // and are re-generating an alternate workout
        workoutRoutinesList.clear();

        //Shuffle Routines
        long seed = System.nanoTime();
        Collections.shuffle(routinesList, new Random(seed));


        //Determine location code
        radIndoorWorkout = (RadioButton) findViewById(R.id.radIndoor);
        indoorWorkout = radIndoorWorkout.isChecked();
        Log.v(TAG, "Is this an indoor workout? " + Boolean.toString(radIndoorWorkout.isChecked()));

        while (totMins < maxMins) {

            //get next routine
            try {
                currRoutine = routinesList.get(i);
            } catch (Exception e) {
                Log.e(TAG, "Could not get next exercise-->" + e.getMessage());
                parseXML();
                Log.e(TAG, "Reloaded Array");
                i = 0;
                continue;
            }
            Log.v(TAG, "Processing: " + currRoutine.getName());

            // Skip if 3 in a row are same body part  (lower, upper, all)
            // 2 body parts in a row are ok.
            body_part_code = currRoutine.getBodyPart();
            //Log.e(TAG, "Body part Code: " + body_part_code);
            if ((body_part_code_A.equals(body_part_code_B)) && (body_part_code_B.equals(body_part_code))) {
                Log.v(TAG, "Repeated Body Part: codeA=" + body_part_code_A + "|codeB=" + body_part_code_B + "|code=" + body_part_code);
                i++;
                continue;
            }

            // Deeper override.
            // Skip if routine has same "skip" bodypart code as either of previous 2 routines.
            // (eg:  pushup, core, shoulder)
            skip = currRoutine.getSkip();
            if ((skip.equals(skip_code_A)) || (skip.equals(skip_code_B))) {
                Log.v(TAG, "Repeated Skip Code: " + skip_code_B);
                i++;
                continue;
            }

            //For short workouts (under 10 mins), exclude longer routines (above 2 mins) to ensure variety
            if ((maxMins < shortWorkoutMins) && (currRoutine.getMinutes() > 2)) {
                Log.v(TAG, "Ignoring > 2min routine");
                i++;
                continue;
            }

            //For indoor workouts, ignore outdoor exercises
            if (indoorWorkout && (currRoutine.getlocation().equals("O"))) {
                Log.v(TAG, "Ignoring outdoor exercise:" + currRoutine.getName());
                i++;
                continue;
            }

            //Keep track of previous 2 moves, for skip purposes.
            body_part_code_A = body_part_code_B;
            body_part_code_B = body_part_code;
            skip_code_A = skip_code_B;
            skip_code_B = skip;

            //Add approved current routine into new arrayList
            workoutRoutinesList.add(currRoutine);
            Log.v(TAG, "Added: " + currRoutine.getName());
            Log.v(TAG, "i=: " + i);

            //Update total time
            totMins = totMins + currRoutine.getMinutes();
            Log.v(TAG, "TotMins is now " + totMins);

            //Update array counter
            i++;

        } //while loop


    }

    private void printArray(ArrayList<Exercise> a) {
        Log.v(TAG, "Start of Workout Routines");
        for (int i = 0; i < a.size(); i++) {
            Log.v(TAG, a.get(i).toString());
        }
        Log.v(TAG, "End of Workout Routines");
    }

    private void displayWorkout() {

        try {

            ArrayAdapter<Exercise> adapter = new ArrayAdapter<Exercise>(this, android.R.layout.simple_list_item_1, workoutRoutinesList);
            ListView listView = (ListView) findViewById(R.id.listViewExercises);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(this);

            Toast.makeText(getApplicationContext(), R.string.ClickVid, Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            e.printStackTrace();
            Log.v(TAG, e.getMessage());
        }
    }


}