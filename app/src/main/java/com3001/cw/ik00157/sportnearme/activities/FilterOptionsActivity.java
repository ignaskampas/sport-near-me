package com3001.cw.ik00157.sportnearme.activities;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import com3001.cw.ik00157.sportnearme.R;

public class FilterOptionsActivity extends AppCompatActivity {

    private static final String TAG = "FILTER_OPTIONS_ACTIVITY";

    private static String SHARED_PREFS;
    private static final String FILTER_APPLIED = "filterApplied";
    private static final String FILTER_BY_LOCATION = "filterByLocation";
    private static final String FILTER_BY_SPORT = "filterBySport";
    private static final String RADIUS = "radius";
    private static final String LENGTH_UNIT = "lengthUnit";
    private static final String SPORT = "sport";
    private static final int SPORT_LENGTH_MIN = 2;
    private static final int SPORT_LENGTH_MAX = 32;

    private Spinner spLengthUnit;
    private Button btnSaveFilterOptions;
    private CheckBox cbLocation, cbSport;
    private EditText etRadius, etSport;
    private String sport;
    private boolean filterBySport = false;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_options);

        mAuth = FirebaseAuth.getInstance();
        SHARED_PREFS = "filterOptions-" + mAuth.getCurrentUser().getUid();
        setUpSpLengthUnit();
        cbLocation = findViewById(R.id.cb_location);
        cbSport = findViewById(R.id.cb_sport);
        etRadius = findViewById(R.id.et_radius);
        etSport = findViewById(R.id.et_sport);
        setUpBtnSaveFilterOptions();

        loadFilterOptionsFromSharedPrefs();
    }

    private void setUpSpLengthUnit(){
        spLengthUnit = findViewById(R.id.spinner_length_unit);
        ArrayAdapter<CharSequence> spAdapter = ArrayAdapter.createFromResource(this,
                R.array.length_units, R.layout.spinner_item);
        spAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spLengthUnit.setAdapter(spAdapter);
        spLengthUnit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void setUpBtnSaveFilterOptions(){
        btnSaveFilterOptions = findViewById(R.id.btn_save_filter_options);
        btnSaveFilterOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "etRadius text: " + etRadius.getText().toString());
                String strRadius = etRadius.getText().toString();
                filterBySport = cbSport.isChecked();
                sport = etSport.getText().toString().trim().toLowerCase();
                if(!radiusInputIsValid(strRadius)){
                    showInvalidRadiusInputToastMessage();
                } else if(filterBySport && !sportInputIsValidWhenCBChecked()){
                    showInvalidSportInputToastMessage();
                } else {
                    boolean filterByLocation = cbLocation.isChecked();
                    // Ignor this variable for now, I think i wont need it
                    boolean filterApplied = false;
                    if(filterByLocation){
                        filterApplied = true;
                    }
                    Log.i(TAG, "filterByLocation: " + filterByLocation);
                    // save radius here
                    String usersSelectedLengthUnit = spLengthUnit.getSelectedItem().toString();

                    // MODE_PRIVATE means no other app can change this shared preferences
                    SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean(FILTER_APPLIED, filterApplied);
                    editor.putBoolean(FILTER_BY_LOCATION, filterByLocation);
                    editor.putInt(RADIUS, Integer.valueOf(strRadius));
                    editor.putString(LENGTH_UNIT, spLengthUnit.getSelectedItem().toString());
                    editor.putBoolean(FILTER_BY_SPORT, filterBySport);
                    editor.putString(SPORT, sport);

                    editor.apply();

                    showToast("Saved preferences");
                }

            }
        });
    }

    private boolean sportInputIsValidWhenCBChecked(){
        if(sport.length() < SPORT_LENGTH_MIN || sport.length() > SPORT_LENGTH_MAX){
            return false;
        } else {
            return true;
        }
    }

    private void loadFilterOptionsFromSharedPrefs(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        boolean filterByLocation = sharedPreferences.getBoolean(FILTER_BY_LOCATION, false);
        cbLocation.setChecked(filterByLocation);

        String strRadius = String.valueOf(sharedPreferences.getInt(RADIUS, 0));
        etRadius.setText(strRadius);

        String lengthUnit = sharedPreferences.getString(LENGTH_UNIT, "miles");
        spLengthUnit.setSelection((((ArrayAdapter<CharSequence>)spLengthUnit.getAdapter()).getPosition(lengthUnit)));

        boolean filterBySportLoadedFromPrefs = sharedPreferences.getBoolean(FILTER_BY_SPORT, false);
        cbSport.setChecked(filterBySportLoadedFromPrefs);

        String sportLoadedFromPrefs = sharedPreferences.getString(SPORT, "");
        etSport.setText(sportLoadedFromPrefs);
    }

    private boolean radiusInputIsValid(String strRadius){
        int intRadius;
        try{
            intRadius = Integer.valueOf(strRadius);
        } catch(Exception e){
            // Failed to convert string radius to an integer.
            return false;
        }
        if(intRadius <= 0){
            return false;
        }

        if(spLengthUnit.getSelectedItem().toString().equals("kilometers")){
            if(intRadius > 20100){
                return false;
            }
        } else if(spLengthUnit.getSelectedItem().toString().equals("miles")){
            if(intRadius > 12562){
                return false;
            }
        }
        return true;
    }

    private void showToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showInvalidSportInputToastMessage(){
        showToast("Preferences not saved. The sport length must be between " + SPORT_LENGTH_MIN + " and " + SPORT_LENGTH_MAX + " 32 characters.");
    }

    private void showInvalidRadiusInputToastMessage(){
        showToast("Preferences not saved. The radius must be a whole number greater than 0.");
    }
}
