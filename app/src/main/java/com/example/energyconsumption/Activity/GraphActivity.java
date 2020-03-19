package com.example.energyconsumption.Activity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.energyconsumption.Fragment.AddKwhPriceFragment;
import com.example.energyconsumption.Fragment.MetaMensualFragment;
import com.example.energyconsumption.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.Utils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import androidx.annotation.RequiresApi;

public class GraphActivity extends AppCompatActivity {

    ArrayList<Entry> entries = new ArrayList<>();
    private LineChart chart;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private float kwhPrice = 1;
    private float metaMensual = 0;
    private FragmentManager fragmentManager;
    private float accumulated = 0;

    private TextView textConsumoActual;
    private TextView textConsumoAcumulado;
    private TextView textMetaMensual;
    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("graph");

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        fragmentManager = getSupportFragmentManager();

        chart = findViewById(R.id.chart);

        textConsumoActual = findViewById(R.id.consumo_actual);
        textConsumoAcumulado = findViewById(R.id.consumo_acumulado);
        textMetaMensual = findViewById(R.id.meta_mensual);

        ValueEventListener postListener = new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                //signal = dataSnapshot.getValue(ArrayList.class);
                accumulated = 0;
                entries = new ArrayList<Entry>();
                int i = 0;
                float lastValue = 0;
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    if (postSnapshot.getValue() != null) {
                        Float newValue = postSnapshot.getValue(Float.class);
                        entries.add(new Entry(i, newValue));

                        sumAccumulated(newValue);
                        lastValue = newValue;
                    }
                    // signal.add(new Entry(i, postSnapshot.getValue(Float.class)));
                    i++;
                }

                updateChart();
                updateLastConsum(lastValue);
                updateAccumlateConsum();
                if(calKwhPrice() > metaMensual){
                    addNotification();
                }
                Log.i("entries", String.valueOf(entries.size()));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w("GraphActivity", "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        };


        mDatabase.addValueEventListener(postListener);


        LineDataSet dataSet = new LineDataSet(entries, "Customized values");
        dataSet.setColor(ContextCompat.getColor(this, R.color.colorPrimary));
        dataSet.setValueTextColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));

        //****
        // Controlling X axis
        XAxis xAxis = chart.getXAxis();
        // Set the xAxis position to bottom. Default is top
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        //Customizing x axis value
        final String[] months = new String[]{"Jan", "Feb", "Mar", "Apr"};

        ValueFormatter formatter = new ValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return months[(int) value];
            }
        };
        xAxis.setGranularity(1f); // minimum axis-step (interval) is 1
        xAxis.setValueFormatter(formatter);

        //***
        // Controlling right side of y axis
        YAxis yAxisRight = chart.getAxisRight();
        yAxisRight.setEnabled(false);

        //***
        // Controlling left side of y axis
        YAxis yAxisLeft = chart.getAxisLeft();
        yAxisLeft.setGranularity(1f);

        // Setting Data
        LineData data = new LineData(dataSet);
        chart.setData(data);
        //refresh
        chart.invalidate();
    }

    @Override
    public void onStart() {
        super.onStart();
        sharedPref = getApplicationContext().getSharedPreferences(
                getString(R.string.kwh_price), Context.MODE_PRIVATE);
        kwhPrice = sharedPref.getFloat(getString(R.string.kwh_price), 1000);
        metaMensual = sharedPref.getFloat(getString(R.string.meta_mensual), 1000);
        saveMetaMensual((int)metaMensual);
    }

    private void addNotification() {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
                        .setContentTitle("Notifications Example")
                        .setContentText("This is a test notification");

        Intent notificationIntent = new Intent(this, GraphActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        // Add as notification
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, builder.build());
    }

    private void updateAccumlateConsum() {
        String text  = "Consumo Acumulado:" +(int)(calKwhPrice()) + "Pesos";
        textConsumoAcumulado.setText(text);
    }

    private float calKwhPrice(){
        Double kwh = accumulated * 0.00138889;
        double kwhPrice = kwh * this.kwhPrice;
        return (float) kwhPrice;
    }

    private void sumAccumulated(float value) {
        accumulated = accumulated + value;
    }

    public void  updateLastConsum(float kwh){
        String text  = "Consumo Actual:" + (int)(kwh) + "W";
        textConsumoActual.setText(text);
    }

    public void saveKWhPrice(float kwhPrice){
        this.kwhPrice = kwhPrice;
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putFloat(getString(R.string.kwh_price), kwhPrice);
        editor.apply();
        updateAccumlateConsum();

    }

    public void saveMetaMensual(int metaMensual){
        this.metaMensual = metaMensual;
        String text = "Meta mensual: "+ metaMensual + "Pesos";
        textMetaMensual.setText(text) ;
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putFloat(getString(R.string.meta_mensual), metaMensual);
        editor.apply();
    }


    private void updateChart() {
        LineDataSet dataSet = new LineDataSet(entries, "KWH");
        LineData data = new LineData(dataSet);
        chart.setData(data);
        chart.animateX(100);
        //refresh
        chart.invalidate();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.settings_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_kwh:
                // User chose the "Settings" item, show the app settings UI...
                AddKwhPriceFragment addOrderFragment = new AddKwhPriceFragment();
                addOrderFragment.show(fragmentManager, "Add Order Fragment");
                return true;
            case R.id.item_logout:
                // User chose the "Settings" item, show the app settings UI...
                mAuth.signOut();
                finish();
                return true;
            case R.id.item_meta:
                // User chose the "Settings" item, show the app settings UI...
                MetaMensualFragment metaMensualFragment = new MetaMensualFragment();
                metaMensualFragment.show(fragmentManager, "Add Order Fragment");
                return true;
            case R.id.item_month:
                Intent intent = new Intent(getApplicationContext(), MonthActivity.class);
                intent.putExtra("ACTUAL_MONTH", calKwhPrice());
                startActivity(intent);
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }





}
