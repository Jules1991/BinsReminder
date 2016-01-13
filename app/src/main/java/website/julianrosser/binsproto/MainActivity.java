package website.julianrosser.binsproto;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    int dayOfReminder;

    TextView textNextCollection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        textNextCollection = (TextView) findViewById(R.id.textNextCollection);

        // Create spinner
        Spinner daySpinner = (Spinner) findViewById(R.id.collectionDaySpinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.day_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        daySpinner.setAdapter(adapter);

        daySpinner.setSelection(getCollectionPref());

        daySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                dayOfReminder = position + 1;

                textNextCollection.setText(getNextReminderDay(dayOfReminder));

                // Set SharedPreference value
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt(getString(R.string.collection_day_key), dayOfReminder);
                editor.apply();

                launchservice();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Hour Spinner
        Spinner hourSpinner = (Spinner) findViewById(R.id.collectionTimeSpinner);
        ArrayAdapter<CharSequence> hourAdapter = ArrayAdapter.createFromResource(this,
                R.array.hour_array, android.R.layout.simple_spinner_item);
        hourAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        hourSpinner.setAdapter(hourAdapter);
        hourSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                int hour = position + 17;

                // Set SharedPreference value
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt(getString(R.string.reminder_hour_key), hour);
                editor.apply();

                launchservice();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        hourSpinner.setSelection(getHourPref());
    }

    private void launchservice() {
        // Launch service to update alarms when data changed
        Intent serviceIntent = new Intent(getApplicationContext(), AlarmService.class);
        startService(serviceIntent);

    }

    public int getCollectionPref() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        return preferences.getInt(getString(R.string.collection_day_key), -1);
    }


    public int getHourPref() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        return preferences.getInt(getString(R.string.reminder_hour_key), 18)  -17;
    }

    public String getNextReminderDay(int dayOfReminder) {

        long dayMillis = 86400000;

        Calendar newCalendar = Calendar.getInstance();

        // If today happens to be the same day as desired reminder, skip to next week
        if (newCalendar.get(Calendar.DAY_OF_WEEK) == dayOfReminder) {
            newCalendar.add(Calendar.MILLISECOND, ((int) dayMillis * 7));

        } else { // Else, skip to next day occurrence. (Avoid using while loop due to view hanging)
            while (newCalendar.get(Calendar.DAY_OF_WEEK) != dayOfReminder) {
                newCalendar.add(Calendar.DATE, 1);
            }
        }

        return getCurrentDateInSpecificFormat(newCalendar);
    }

    private String getCurrentDateInSpecificFormat(Calendar currentCalDate) {
        //String dayNumberSuffix = getDayNumberSuffix(currentCalDate.get(Calendar.DAY_OF_MONTH));
        SimpleDateFormat format = new SimpleDateFormat("EEEE dd MMMM", Locale.getDefault());
        return format.format(currentCalDate.getTime());
    }

    private String getDayNumberSuffix(int day) {
        if (day >= 11 && day <= 13) {
            return "th";
        }
        switch (day % 10) {
            case 1:
                return "st";
            case 2:
                return "nd";
            case 3:
                return "rd";
            default:
                return "th";
        }
    }

    public String intToDay(int i) {

        switch (i) {
            case 1:
                return "Sunday";
            case 2:
                return "Monday";
            case 3:
                return "Tuesday";
            case 4:
                return "Wednesday";
            case 5:
                return "Thursday";
            case 6:
                return "Friday";
            case 7:
                return "Saturday";
            default:
                return "UNKNOWN:" + i;
        }
    }
}
