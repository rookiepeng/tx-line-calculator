package com.rookiedev.microwavetools;

import android.os.Bundle;

import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;

public class about extends ActionBarActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);

		// ActionBar actionBar = getActionBar();
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			return false;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onBackPressed() {
		// do something what you want
		super.onBackPressed();
	}
}
