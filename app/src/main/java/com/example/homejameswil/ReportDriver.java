package com.example.homejameswil;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.util.ArrayList;

public class ReportDriver extends Fragment
{
    View view;
    AutoCompleteTextView ReportDriver;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.fragment_report_driver, container, false);

        DropDown();
        return view;
    }

    private void DropDown()
    {
        ReportDriver = view.findViewById(R.id.DropDown);

        String[] Reason = new String[]{"Sexual Assault", "Bad Driving", "Inappropriate Behaviour", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item, Reason);
        ReportDriver.setAdapter(adapter);

        ReportDriver.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                Toast.makeText(getActivity(), "" + ReportDriver.getText().toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}