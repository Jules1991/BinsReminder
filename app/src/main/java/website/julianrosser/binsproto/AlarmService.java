package website.julianrosser.binsproto;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class AlarmService extends Service {

    static Context mContext;

    int requestCode = 500;

    long dayMillis = 86400000;

    long weekMillis = dayMillis * 7;

    private AlarmManager mAlarmManager;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();

        // Get alarm manager if needed
        if (null == mAlarmManager) {
            mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        }

        setAlarm();

        // Service has to control its own life cycles, so call stopSelf here
        stopSelf();
    }

    private void setAlarm() {

        // CreateIntent to start the AlarmNotificationReceiver
        Intent mNotificationReceiverIntent = new Intent(getApplicationContext(), AlarmNotificationBuilder.class);

        // Build message String
        String messageString = "Green/Black bin will be collected tomorrow!";

        mNotificationReceiverIntent.putExtra(AlarmNotificationBuilder.STRING_MESSAGE_KEY, messageString);

        // Create pending Intent using Intent we just built
        PendingIntent mNotificationReceiverPendingIntent = PendingIntent
                .getBroadcast(mContext, requestCode,
                        mNotificationReceiverIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);

        mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, getReminderTime(), weekMillis, mNotificationReceiverPendingIntent);
    }

    private long getReminderTime() {
        // Get preferences, for collection day int
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        int dayOfReminder = preferences.getInt(getString(R.string.collection_day_key), -1);

        if (dayOfReminder < 1 || dayOfReminder > 7) {
            Log.i(getClass().getSimpleName(), "ReminderDay out of range: " + dayOfReminder);
        }

        // Get Calender set to day of next reminder
        Calendar reminderTime = getNextReminderDay(dayOfReminder);

        // Get users reminder time preference
        int hourOfReminder = preferences.getInt(getString(R.string.reminder_hour_key), 18);

        // Set time of reminder todo - Users time preference
        reminderTime.set(Calendar.HOUR_OF_DAY, hourOfReminder);
        reminderTime.set(Calendar.MINUTE, 0);
        reminderTime.set(Calendar.SECOND, 0);

        DateFormat dateTimeInstance = SimpleDateFormat.getDateTimeInstance();
        System.out.println(dateTimeInstance.format(reminderTime.getTime()));

        System.out.println("Reminder time: " + reminderTime.get(Calendar.HOUR_OF_DAY) + ":" + reminderTime.get(Calendar.MINUTE) + " - " + reminderTime.get(Calendar.DATE)+ "|" + reminderTime.get(Calendar.MONTH));

        System.out.println("Hours between: " + (reminderTime.getTimeInMillis() - Calendar.getInstance().getTimeInMillis() ) / 1000 / 60 / 60  );

        return reminderTime.getTimeInMillis();
    }

    /////////////////////////////////
    // TODO - Catch already passed times
    /////////////////////////////////

    public Calendar getNextReminderDay(int dayOfReminder) {

        Calendar newCalendar = Calendar.getInstance();

        // If today happens to be the same day as desired reminder, skip to next week
        if (newCalendar.get(Calendar.DAY_OF_WEEK) == dayOfReminder) {
            newCalendar.add(Calendar.MILLISECOND, ((int) dayMillis * 7));

        } else { // Else, skip to next day occurrence. (Avoid using while loop due to view hanging)
            while (newCalendar.get(Calendar.DAY_OF_WEEK) != dayOfReminder) {
                newCalendar.add(Calendar.DATE, 1);
            }
        }

        long millis = newCalendar.getTimeInMillis();
        newCalendar.setTimeInMillis(millis - dayMillis);

        return newCalendar;
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
