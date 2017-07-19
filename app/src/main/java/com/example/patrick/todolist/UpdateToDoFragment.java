package com.example.patrick.todolist;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.Calendar;

/**
 * Created by mark on 7/5/17.
 */

public class UpdateToDoFragment extends DialogFragment {

    private EditText toDo;
    private DatePicker dp;
    private Button add;
    private final String TAG = "updatetodofragment";
    private Spinner catSpin;
    private long id;


    public UpdateToDoFragment(){}

    public static UpdateToDoFragment newInstance(int year, int month, int day, String description, Integer completion,String category,long id) {
        UpdateToDoFragment f = new UpdateToDoFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putInt("year", year);
        args.putInt("month", month-1);
        args.putInt("day", day);
        args.putLong("id", id);
        args.putString("description", description);
        args.putInt("completion",completion);
        args.putString("category",category);

        f.setArguments(args);

        return f;
    }

    //To have a way for the activity to get the data from the dialog
    public interface OnUpdateDialogCloseListener {
        void closeUpdateDialog(int year, int month, int day, String description,Integer completion,String Category, long id);
    }

    //Creates the view for UpdateFragment
    //A spinner has been added just below where a user inputs the To Do
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_to_do_adder, container, false);
        toDo = (EditText) view.findViewById(R.id.toDo);
        dp = (DatePicker) view.findViewById(R.id.datePicker);
        add = (Button) view.findViewById(R.id.add);

        //Creates the spinner. Fills in from a string array in value.xml
        catSpin=(Spinner) view.findViewById(R.id.category_spinner);
        ArrayAdapter<CharSequence> adapter=ArrayAdapter.createFromResource(getActivity(),
                R.array.initial_categories,android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        catSpin.setAdapter(adapter);

        int year = getArguments().getInt("year");
        int month = getArguments().getInt("month");
        int day = getArguments().getInt("day");
        id = getArguments().getLong("id");
        String description = getArguments().getString("description");

        dp.updateDate(year, month, day);

        toDo.setText(description);

        add.setText("Update");
        catSpin.setSelection(findSpinnerPosition(getArguments().getString("category")));
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateToDoFragment.OnUpdateDialogCloseListener activity = (UpdateToDoFragment.OnUpdateDialogCloseListener) getActivity();
                Log.d(TAG, "id: " + id);
                activity.closeUpdateDialog(dp.getYear(), dp.getMonth(), dp.getDayOfMonth(),toDo.getText().toString(),getArguments().getInt("completion"),catSpin.getSelectedItem().toString(), id);
                UpdateToDoFragment.this.dismiss();
            }
        });

        return view;
    }


    //Sets the spinner in UpdateFragment to the correct category choice
    private int findSpinnerPosition(String category){
        int pos=0;

        for(int i=0;i<catSpin.getCount();i++) {
            if (catSpin.getItemAtPosition(i).toString().toUpperCase().equals(category.toUpperCase())){
                pos = i;
            }
        }
        return pos;
    }
}