package org.udg.pds.todoandroid.activity;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.udg.pds.todoandroid.R;
import org.udg.pds.todoandroid.TodoApp;
import org.udg.pds.todoandroid.entity.IdObject;
import org.udg.pds.todoandroid.entity.Task;
import org.udg.pds.todoandroid.rest.TodoApi;
import org.udg.pds.todoandroid.util.Global;

import java.util.Calendar;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created with IntelliJ IDEA.
 * User: imartin
 * Date: 29/03/13
 * Time: 0:45
 * To change this template use File | Settings | File Templates.
 */

// Fragment used to create a new task
public class AddTask extends AppCompatActivity implements Callback<IdObject> {

  TodoApi mTodoService;

  // We will use a Handler to return time from the time selection dialog to the Activity
  Handler mHandlerT = new Handler() {
    @Override
    public void handleMessage(Message msg) {
      Bundle b = msg.getData();
      Integer hour = b.getInt("hour");
      Integer minute = b.getInt("minute");
      TextView tl = (TextView) findViewById(R.id.at_time_limit);
      Calendar cal = Calendar.getInstance();
      cal.set(Calendar.HOUR, hour);
      cal.set(Calendar.MINUTE, minute);
      tl.setText(Global.TIME_ONLY_FORMAT.format(cal.getTime()));
    }
  };

  // We will use a Handler to return date from the date selection dialog to the Activity
  Handler mHandlerD = new Handler() {
    @Override
    public void handleMessage(Message msg) {
      Bundle b = msg.getData();
      Integer day = b.getInt("day");
      Integer month = b.getInt("month");
      Integer year = b.getInt("year");

      TextView tl = (TextView) findViewById(R.id.at_date_limit);
      Calendar cal = Calendar.getInstance();
      cal.set(Calendar.DAY_OF_MONTH, day);
      cal.set(Calendar.MONTH, month);
      cal.set(Calendar.YEAR, year);
      tl.setText(Global.DATE_ONLY_FORMAT.format(cal.getTime()));
    }
  };

  @Override
  public void onResponse(Call<IdObject> call, Response<IdObject> response) {
    if (response.isSuccessful()) {
      finish();
    } else {
      Toast.makeText(this, "Error adding task", Toast.LENGTH_LONG).show();
    }
  }

  @Override
  public void onFailure(Call<IdObject> call, Throwable t) {

  }

  // This class is a Dialog used by the user to introduce a time (HH::mm)
  // It is shown when the user presses the "Set" button
  // in the "time limit" field
  public static class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

    Handler mH;

    public void setHandler(Handler h) {
      mH = h;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
      // Use the current time as the default values for the picker
      final Calendar c = Calendar.getInstance();
      int hour = c.get(Calendar.HOUR_OF_DAY);
      int minute = c.get(Calendar.MINUTE);

      // Create a new instance of TimePickerDialog and return it
      return new TimePickerDialog(getActivity(), this, hour, minute,
          DateFormat.is24HourFormat(getActivity()));
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
      Message msg = new Message();
      Bundle data = new Bundle();
      data.putInt("hour", hourOfDay);
      data.putInt("minute", minute);
      msg.setData(data);
      mH.sendMessage(msg);
    }
  }

  // This class is a Dialog user by the user to introduce a time (dd/mm/yyyy)
  // It is shown when the user presses the "Set" button
  // in the "date limit" field
  public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    Handler mH;

    public void setHandler(Handler h) {
      mH = h;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
      // Use the current date as the default values for the picker
      final Calendar c = Calendar.getInstance();
      int day = c.get(Calendar.DAY_OF_MONTH);
      int month = c.get(Calendar.MONTH);
      int year = c.get(Calendar.YEAR);

      // Create a new instance of TimePickerDialog and return it
      return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
      Message msg = new Message();
      Bundle data = new Bundle();
      data.putInt("day", day);
      data.putInt("month", month);
      data.putInt("year", year);

      msg.setData(data);
      mH.sendMessage(msg);
    }
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.add_task);

    mTodoService = ((TodoApp)this.getApplication()).getAPI();

    FragmentManager fm = getFragmentManager();

    Button timeButton = (Button) findViewById(R.id.at_time_limit_button);
    // Show the time selection dialog when the "Set" button is pressed
    timeButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        TimePickerFragment dialog = new TimePickerFragment();
        dialog.setHandler(mHandlerT);
        dialog.show(getFragmentManager(), "timepickerdialog");
      }
    });

    Button dateButton = (Button) findViewById(R.id.at_date_limit_button);
    // Show the date selection dialog when the "Set" button is pressed
    dateButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        DatePickerFragment dialog = new DatePickerFragment();
        dialog.setHandler(mHandlerD);
        dialog.show(getFragmentManager(), "timepickerdialog");
      }
    });

    Button save = (Button) findViewById(R.id.at_save_button);
    // When the "Save" button is pressed, we make the call to the responder
    save.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        TextView datev = (TextView) findViewById(R.id.at_date_limit);
        TextView timev = (TextView) findViewById(R.id.at_time_limit);
        TextView textv = (TextView) findViewById(R.id.at_text);

        try {
          Date dateLimit = Global.TIME_DATE_FORMAT.parse(datev.getText().toString() + " " + timev.getText().toString());
          String text = textv.getText().toString();
          Task task = new Task();
          task.text = text;
          task.dateLimit = dateLimit;
          task.dateCreated = new Date();
          Call<IdObject> call = mTodoService.addTask(task);
          call.enqueue(AddTask.this);
        } catch (Exception ex) {
          return;
        }

      }
    });

  }
}