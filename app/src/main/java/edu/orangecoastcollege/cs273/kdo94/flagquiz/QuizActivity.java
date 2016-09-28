package edu.orangecoastcollege.cs273.kdo94.flagquiz;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.Set;

import static android.R.attr.key;

public class QuizActivity extends AppCompatActivity {

    // keys for reading data from SharedPreferences
    public static final String CHOICES = "pref_numberOfChoices";
    public static final String REGIONS = "pref_regionsToInclude";

    private boolean phoneDevice = true; // used to force portrait mode
    private boolean preferenceChanged = true; // Did preferences change?

    /**
     * onCreate generates the appropriate layout to infalte, depending on the
     * screen size. If the device is large or x-large, it will load the content_main.xml
     * (sw700dp-land) which includes the standard content_main.xml with the fragment_quiz.
     *
     * All default preferences are set using the preferences.xml file.
     * @param savedInstanceState The saved state to restore(not being used)
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // set default values in the apps SharedPreferences
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        // Register listener for SharedPreferences changes
        PreferenceManager.getDefaultSharedPreferences(this).
                registerOnSharedPreferenceChangeListener(
                        preferencesChangedListener);

        // Determine the screen size
        int screenSize = getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK;

        // If device is a tablet, set phoneDevice to false
        if (screenSize == Configuration.SCREENLAYOUT_SIZE_XLARGE ||
                screenSize == Configuration.SCREENLAYOUT_SIZE_LARGE)
            phoneDevice = false; // A tablet sized phone

        // If running on a phone-sized device, allow only portrait orientation
        if(phoneDevice)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        FloatingActionButton fab = (FloatingActionButton) findViewById(fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(preferenceChanged){
            // Now that the default preferences have been set up,
            // initialize QuizActivityFragment and start the quiz
            QuizActivityFragment quizFragment = (QuizActivityFragment)
                    getSupportFragmentManager().findFragmentById(R.id.quizFragment);
            quizFragment.updateGuessRows(
                    PreferenceManager.getDefaultSharedPreferences(this));
            quizFragment.updateRegions(
                    PreferenceManager.getDefaultSharedPreferences(this));
            quizFragment.resetQuiz();
            preferenceChanged = false;

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Get teh device's current orientation
        int orientation = getResources().getConfiguration().orientation;

        // Display teh app's menu only in portrait orientation
        if(orientation == Configuration.ORIENTATION_PORTRAIT){
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.menu_quiz, menu);
            return true;
        }
        else
            return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
       Intent preferencesIntent = new Intent(this, SettingsActivity.class);
        startActivity(preferencesIntent);

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private SharedPreferences.OnSharedPreferenceChangeListener preferencesChangedListener =
            new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
                    preferenceChanged = true; // user changed app setting

                    QuizActivityFragment quizFragment = (QuizActivityFragment)
                            getSupportFragmentManager().findFragmentById(R.id.quizFragment);

                    if (key.equals(CHOICES)){ // # of choices to display
                        quizFragment.resetQuiz();
                    }
                    else if(key.equals(REGIONS)){
                        Set<String> regions =
                                sharedPreferences.getStringSet(REGIONS, null);

                        if (regions != null && regions.size() > 0){
                            quizFragment.updateRegions(sharedPreferences);
                            quizFragment.resetQuiz();
                        }
                        else{
                            // must select one region--set North America as default
                            SharedPreferences.Editor editor =
                                    sharedPreferences.edit();
                            regions.add(getString(R.string.default_region));
                            editor.putStringSet(REGIONS, regions);
                            editor.apply();

                            Toast.makeText(QuizActivity.this, R.string.default_region_message,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            };

}
