package com.asha.md360player4android;

import android.app.Activity;
import android.util.SparseArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

/**
 * Created by hzqiujiadi on 16/6/24.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class SpinnerHelper {

    private Activity activity;
    private SparseArray<String> data;
    private ClickHandler clickHandler;
    private int defaultKey;

    public interface ClickHandler{
        void onSpinnerClicked(int index, int key, String value);
    }

    public SpinnerHelper(Activity activity) {
        this.activity = activity;
    }

    public SpinnerHelper setDefault(int key){
        defaultKey = key;
        return this;
    }

    public SpinnerHelper setData(SparseArray<String> data){
        this.data = data;
        return this;
    }

    public SpinnerHelper setClickHandler(ClickHandler clickHandler){
        this.clickHandler = clickHandler;
        return this;
    }

    public void init(int id){
        if (data == null){
            return;
        }

        Spinner spinner = (Spinner) activity.findViewById(id);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(activity, R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        for (int i = 0; i < data.size(); i++){
            String value = data.valueAt(i);
            adapter.add(value);
        }

        spinner.setAdapter(adapter);
        int index = data.indexOfKey(defaultKey);
        index = index == -1 ? 0 : index;
        spinner.setSelection(index);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int key = data.keyAt(position);
                String value = data.valueAt(position);
                if (clickHandler != null){
                    clickHandler.onSpinnerClicked(position,key,value);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public static SpinnerHelper with(Activity activity){
        return new SpinnerHelper(activity);
    }
}
