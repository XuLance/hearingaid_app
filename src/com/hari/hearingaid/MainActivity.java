package com.hari.hearingaid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		final ListView listview = (ListView) findViewById(R.id.listview);
		// listview.text
		// Items on the list
		String[] values = new String[] { "Talk & Listen",
				"Classification using Visualizer", "Train", "Compare" };

		final ArrayList<String> list = new ArrayList<String>();
		for (int i = 0; i < values.length; ++i) {
			list.add(values[i]);
		}
		final StableArrayAdapter adapter = new StableArrayAdapter(this,
				android.R.layout.simple_list_item_1, list);
		listview.setAdapter(adapter);

		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, final View view,
					int position, long id) {
				if (position == 0) {
					// Call the TTS activity when the first item is pressed
					speakText(getCurrentFocus());
				} else if (position == 1) {
					classify(getCurrentFocus());
				} else if (position == 2) {
					train(getCurrentFocus());
				} else if (position == 3) {
					compare(getCurrentFocus());
				}
			}

		});
	}

	// Function with a Speech-to-Text and Text-to-Speech interface
	public void speakText(View view) {
		Intent intent = new Intent(this, TTS_STT.class);
		startActivity(intent);
	}

	// Trivial Classification where we know the frequency bands where the
	public void classify(View view) {
		Intent intent = new Intent(this, Classify.class);
		startActivity(intent);
	}
	// Train and store the frequency components
	public void train(View view) {
		Intent intent = new Intent(this, Train.class);
		startActivity(intent);
	}
	
	//Compare mic input with trained data
	public void compare(View view) {
		Intent intent = new Intent(this, Compare.class);
		startActivity(intent);
	}

	private class StableArrayAdapter extends ArrayAdapter<String> {

		HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

		public StableArrayAdapter(Context context, int textViewResourceId,
				List<String> objects) {
			super(context, textViewResourceId, objects);
			for (int i = 0; i < objects.size(); ++i) {
				mIdMap.put(objects.get(i), i);
			}
		}

		@Override
		public long getItemId(int position) {
			String item = getItem(position);
			return mIdMap.get(item);
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

	}

}