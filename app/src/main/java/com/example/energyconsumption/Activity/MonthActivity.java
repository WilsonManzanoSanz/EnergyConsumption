package com.example.energyconsumption.Activity;

import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.example.energyconsumption.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;

public class MonthActivity extends AppCompatActivity {
    private BarChart monthChart;
    private float currentConsum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_month);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getIntent().hasExtra("ACTUAL_MONTH")) {
            currentConsum = getIntent().getFloatExtra("ACTUAL_MONTH", 0);
        }

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        final ArrayList<String> Date = new ArrayList<>();
        Date.add("Feb");
        Date.add("Mar");
        Date.add("Abril");

        int i = 0;


        BarChart chart = (BarChart) findViewById(R.id.chart_month);

        BarData data = new BarData(getDataSet());

        data.setValueTextSize(15f);
        data.setBarWidth(8.9f);


        ValueFormatter formatter = new ValueFormatter() {

            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return Date.get((int)value);
            }

        };

        XAxis xAxis = chart.getXAxis();
        xAxis.setGranularity(1f); // minimum axis-step (interval) is 1
        xAxis.setValueFormatter(formatter);

        chart.setData(data);
        //chart.setDescription("My Chart");
        chart.animateXY(2000, 2000);
        chart.invalidate();
    }

    private ArrayList<IBarDataSet> getDataSet() {

        ArrayList<BarEntry> valueSet0 = new ArrayList<>();
        BarEntry v0e0 = new BarEntry(10.000f, 14360); // Feb
        valueSet0.add(v0e0);

        ArrayList<BarEntry> valueSet1 = new ArrayList<>();
        BarEntry v1e1 = new BarEntry(0.000f, 0); // Jan
        valueSet1.add(v1e1);
        BarEntry v1e2 = new BarEntry(20.000f, 11308); // Feb
        valueSet1.add(v1e2);

        ArrayList<BarEntry> valueSet2 = new ArrayList<>();
        BarEntry v2e2 = new BarEntry(30.000f, currentConsum); // Feb
        valueSet2.add(v2e2);

        BarDataSet barDataSet0 = new BarDataSet(valueSet0, "Feb");
        barDataSet0.setColor(Color.rgb(0, 0, 155));
        BarDataSet barDataSet1 = new BarDataSet(valueSet1, "March");
        barDataSet1.setColor(Color.rgb(0, 155, 0));
        BarDataSet barDataSet2 = new BarDataSet(valueSet2, "April");
        barDataSet2.setColors(ColorTemplate.COLORFUL_COLORS);

        ArrayList<IBarDataSet> dataSet = new ArrayList<>();

        dataSet.add((IBarDataSet) barDataSet0 );
        dataSet.add((IBarDataSet) barDataSet1 );
        dataSet.add((IBarDataSet) barDataSet2 );


        return dataSet;
    }

    private ArrayList<String> getXAxisValues() {
        ArrayList<String> xAxis = new ArrayList<>();
        xAxis.add("JAN");
        xAxis.add("FEB");
        xAxis.add("MAR");
        xAxis.add("APR");
        xAxis.add("MAY");
        xAxis.add("JUN");
        return xAxis;
    }

}
