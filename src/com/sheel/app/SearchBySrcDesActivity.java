package com.sheel.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class SearchBySrcDesActivity extends Activity{
	
	String searchStatus;
	String selectedDate;
	TextView textDisplay;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_by_src_des);
        
        Bundle extras = getIntent().getExtras();
        
        if(extras !=null){
        	searchStatus = extras.getString("searchStatus");
        	selectedDate = extras.getString("selectedDate");
       	}

        textDisplay = (TextView) findViewById(R.id.textView1);
        
        textDisplay.setText(
                new StringBuilder()
                        .append("I'm searching for users having ").append(searchStatus)
                        .append(" travelling on ").append(selectedDate));
	}

	public void onClick_filter (View v) 
	 {
		 //startActivity(new Intent(this, FilterPreferencesActivity.class));
	 }	
}
