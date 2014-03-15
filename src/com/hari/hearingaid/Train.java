package com.hari.hearingaid;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import ca.uol.aig.fftpack.RealDoubleFFT;

public class Train extends Activity {

	int frequency = 16000;
	@SuppressWarnings("deprecation")
	int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
	int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
	private RealDoubleFFT transformer;
	int blockSize = 512;
	boolean train = false;
	public AudioRecord audioRecord;
	boolean started = false;
	RecordAudio recordTask;
	public SharedPreferences prefs;
	int[] list = new int[blockSize]; //Array to be stored in SharedPreferences
	ImageView imageView;
	Bitmap bitmap;
	Canvas canvas;
	Paint paint;
	int count1 = 0, count2 = 0, count3 = 0; // Counts for classifying sounds
	// Count1 for Police Siren
	// Count2 for Car Horn
	// COunt3 for Fire Alarm
	int dummy = 0;
	double max;

	// AudioRecord audioRecord;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_train);
		prefs = getPreferences(MODE_PRIVATE);
		started = true;
		recordTask = new RecordAudio();
		recordTask.execute();

		transformer = new RealDoubleFFT(blockSize);

		imageView = (ImageView) this.findViewById(R.id.ImageView01);
		bitmap = Bitmap.createBitmap((int) 512, (int) 200,
				Bitmap.Config.ARGB_8888);
		canvas = new Canvas(bitmap);
		paint = new Paint();
		paint.setColor(Color.GREEN);
		imageView.setImageBitmap(bitmap);

	}

	public void onStop() {
		super.onStop();
		started = false;
		recordTask.cancel(true);
		audioRecord.release();
	}

	public class RecordAudio extends AsyncTask<Void, double[], Void> {
		TextView tv = (TextView) findViewById(R.id.tv1);
		TextView tv1 = (TextView) findViewById(R.id.tv2);
		EditText et = (EditText) findViewById(R.id.et1);
		String s= et.getText().toString();
		int counter = 0;

		@Override
		protected Void doInBackground(Void... arg0) {

			try {
				int bufferSize = AudioRecord.getMinBufferSize(frequency,
						channelConfiguration, audioEncoding);

				audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
						frequency, channelConfiguration, audioEncoding,
						bufferSize);

				short[] buffer = new short[blockSize];
				double[] toTransform = new double[blockSize];

				audioRecord.startRecording();

				while (started) {
					int bufferReadResult = audioRecord.read(buffer, 0,
							blockSize);

					for (int i = 0; i < blockSize && i < bufferReadResult; i++) {
						toTransform[i] = (double) buffer[i] / 32768.0;

					}

					transformer.ft(toTransform);

					publishProgress(toTransform);

				}

				audioRecord.stop();

			} catch (Throwable t) {
				t.printStackTrace();
				Log.e("AudioRecord", "Recording Failed");
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(double[]... toTransform) {

			canvas.drawColor(Color.BLACK);
			if (!train)
				dummy = 0;
			for (int i = 0; i < toTransform[0].length; i++) {
				int x = i;

				if (toTransform[0][i] > 25 && (!train)) {
					dummy++;
				}

				int downy = (int) (200 - (toTransform[0][i] * 10));
				int upy = 200;

				canvas.drawLine(x, downy, x, upy, paint);
			}
			tv.setText("|Peaks| > 30 : " + dummy + " Counter : "+counter);
			if (dummy >= 2 && train == false) {
				counter++;
				if (counter > 5)
					train = true;
				for (int i = 0; i < toTransform[0].length; i++) {
					if (toTransform[0][i] > 3) {
						if (list[i] == 0)
							list[i] = 1;

					}
				}
				StringBuilder str = new StringBuilder();
				for (int i = 0; i < list.length; i++) {
					str.append(list[i]).append(",");
				}
				prefs.edit().putString(s, str.toString());
				Log.d("Shared Preferences", "" + str);
			}

			imageView.invalidate();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}