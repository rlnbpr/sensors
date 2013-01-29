package ru.hobud.sensors;

import java.util.ArrayList;
import java.util.LinkedList;

import android.app.Activity;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Menu;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.PointLabelFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;

public class PressureMonitor extends Activity implements SensorEventListener {

  public class SmoothingWindow extends ArrayList<Double> {
    /**
	 * 
	 */
    private static final long serialVersionUID = 2876342446518129513L;
    private int winSize;

    public SmoothingWindow(int win) {
      winSize = win;
    }

    public Double push(Double val) {
      Double res = 0.0;
      if (size() >= winSize)
        remove(0);
      add(val);
      for (Double v : this) {
        res += v;
      }
      return res / size();
    }
  }

  private static final int HISTORY_SIZE = 1000;
  private SensorManager sensorManager;
  private Sensor pressuremeter;
  TextView textViewPressure = null;

  RadioGroup radioGroup = null;

  private XYPlot preHistoryPlot = null;
  private SimpleXYSeries preassureHistorySeries = null;
  private LinkedList<Double> preassureHistory;
  private SmoothingWindow preassureSmoothingWin = new SmoothingWindow(100);

  private LinkedList<Double> altitudeHistory;
  private SmoothingWindow altitudeSmoothingWin = new SmoothingWindow(100);

  private LinkedList<Double> tempHistory;
  private SmoothingWindow tempSmoothingWin = new SmoothingWindow(100);

  private int sensorId = 0;

  {
    preassureHistory = new LinkedList<Double>();
    preassureHistorySeries = new SimpleXYSeries("Preassure");

    altitudeHistory = new LinkedList<Double>();

    tempHistory = new LinkedList<Double>();
  }

  public PressureMonitor() {
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_pressure_monitor);
    sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
    pressuremeter = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
    textViewPressure = (TextView) findViewById(R.id.textViewPressure);
    radioGroup = (RadioGroup) findViewById(R.id.radioGroup1);
    radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

      @Override
      public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (checkedId == R.id.preasure)
          sensorId = 0;
        else if (checkedId == R.id.altitude)
          sensorId = 1;
        else
          sensorId = 2;
        Toast.makeText(getApplicationContext(), String.format("CHECKED %d(%d)", checkedId, R.id.preasure), Toast.LENGTH_SHORT).show();
      }
    });
    setTitle("Preassure");

    // setup the APR History plot:
    preHistoryPlot = (XYPlot) findViewById(R.id.pressurePlot);
    preHistoryPlot.setRangeBoundaries(900, 1300, BoundaryMode.AUTO);
    preHistoryPlot.setDomainBoundaries(0, 100, BoundaryMode.AUTO);// FIXED);
    preHistoryPlot.addSeries(preassureHistorySeries, new LineAndPointFormatter(Color.BLACK, Color.RED, null, new PointLabelFormatter(
        Color.TRANSPARENT)));
    preHistoryPlot.setDomainStepValue(5);
    preHistoryPlot.setTicksPerRangeLabel(3);
    // preHistoryPlot.setDomainLabel("Sample Index");
    preHistoryPlot.getDomainLabelWidget().pack();
    preHistoryPlot.setRangeLabel("Pressure (hPa)");
    preHistoryPlot.getRangeLabelWidget().pack();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.activity_pressure_monitor, menu);
    return true;
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    float[] data = new float[preassureHistory.size()];
    for (int i = 0; i < preassureHistory.size(); i++) {
      data[i] = preassureHistory.get(i).floatValue();
    }
    outState.putFloatArray("Preassure", data);

    data = new float[preassureSmoothingWin.size()];
    for (int i = 0; i < preassureSmoothingWin.size(); i++) {
      data[i] = preassureSmoothingWin.get(i).floatValue();
    }
    outState.putFloatArray("Preassure Smoothing Window", data);

    data = new float[altitudeHistory.size()];
    for (int i = 0; i < altitudeHistory.size(); i++) {
      data[i] = altitudeHistory.get(i).floatValue();
    }
    outState.putFloatArray("Altitude", data);

    data = new float[altitudeSmoothingWin.size()];
    for (int i = 0; i < altitudeSmoothingWin.size(); i++) {
      data[i] = altitudeSmoothingWin.get(i).floatValue();
    }
    outState.putFloatArray("Altitude Smoothing Window", data);

    data = new float[tempHistory.size()];
    for (int i = 0; i < tempHistory.size(); i++) {
      data[i] = tempHistory.get(i).floatValue();
    }
    outState.putFloatArray("Temperature", data);

    data = new float[tempSmoothingWin.size()];
    for (int i = 0; i < tempSmoothingWin.size(); i++) {
      data[i] = tempSmoothingWin.get(i).floatValue();
    }
    outState.putFloatArray("Temperature Smoothing Window", data);

  }

  @Override
  protected void onRestoreInstanceState(Bundle inState) {
    float[] data = inState.getFloatArray("Preassure");
    if (data != null) {
      preassureHistory.clear();
      for (float f : data) {
        preassureHistory.add((double) f);
      }
    }
    data = inState.getFloatArray("Preassure Smoothing Window");
    if (data != null) {
      preassureSmoothingWin.clear();
      for (float f : data) {
        preassureSmoothingWin.push((double) f);
      }
    }

    data = inState.getFloatArray("Altitude");
    if (data != null) {
      altitudeHistory.clear();
      for (float f : data) {
        altitudeHistory.add((double) f);
      }
    }
    data = inState.getFloatArray("Altitude Smoothing Window");
    if (data != null) {
      altitudeSmoothingWin.clear();
      for (float f : data) {
        altitudeSmoothingWin.push((double) f);
      }
    }

    data = inState.getFloatArray("Temperature");
    if (data != null) {
      tempHistory.clear();
      for (float f : data) {
        tempHistory.add((double) f);
      }
    }
    data = inState.getFloatArray("Temperature Smoothing Window");
    if (data != null) {
      tempSmoothingWin.clear();
      for (float f : data) {
        tempSmoothingWin.push((double) f);
      }
    }
  }

  protected void onResume() {
    super.onResume();
    sensorManager.registerListener(this, pressuremeter, SensorManager.SENSOR_DELAY_NORMAL);// UI);
  }

  protected void onPause() {
    super.onPause();
    sensorManager.unregisterListener(this);
  }

  public void onAccuracyChanged(Sensor sensor, int accuracy) {
  }

  public void onSensorChanged(SensorEvent event) {
    String valuesString = "";
    int counter = 0;
    for (float value : event.values) {
      valuesString += String.format("%d) %f ", counter++, value);
    }
    valuesString += " , " + preassureHistory.size();
    textViewPressure.setText(valuesString);

    double value = preassureSmoothingWin.push((double) event.values[0]);
    // Number[] series1Numbers = {event.values[0], event.values[1],
    // event.values[2]};
    // get rid the oldest sample in history:
    if (preassureHistory.size() > HISTORY_SIZE) {
      preassureHistory.removeFirst();
    }
    // add the latest history sample:
    preassureHistory.addLast(value);// event.values[0]);

    value = altitudeSmoothingWin.push((double) event.values[1]);
    if (altitudeHistory.size() > HISTORY_SIZE) {
      altitudeHistory.removeFirst();
    }
    altitudeHistory.addLast(value);// event.values[0]);

    value = tempSmoothingWin.push((double) event.values[2]);
    if (tempHistory.size() > HISTORY_SIZE) {
      tempHistory.removeFirst();
    }
    tempHistory.addLast(value);// event.values[0]);

    // update the plot with the updated history Lists:
    if (sensorId == 0)
      preassureHistorySeries.setModel(preassureHistory, SimpleXYSeries.ArrayFormat.Y_VALS_ONLY);
    else if (sensorId == 1)
      preassureHistorySeries.setModel(altitudeHistory, SimpleXYSeries.ArrayFormat.Y_VALS_ONLY);
    else
      preassureHistorySeries.setModel(tempHistory, SimpleXYSeries.ArrayFormat.Y_VALS_ONLY);

    // redraw the Plots:
    preHistoryPlot.redraw();
  }

}