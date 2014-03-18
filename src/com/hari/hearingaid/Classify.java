package com.hari.hearingaid;

import android.app.Activity;
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
import android.widget.ImageView;
import android.widget.TextView;
import ca.uol.aig.fftpack.RealDoubleFFT;

public class Classify extends Activity {

	int frequency = 16000;
	@SuppressWarnings("deprecation")
	int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
	int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
	private RealDoubleFFT transformer;
	int blockSize = 512;
	boolean police = false, fire = false, carhorn = false;
	public AudioRecord audioRecord;
	boolean started = false;
	RecordAudio recordTask;
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
		setContentView(R.layout.activity_classify);

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
		TextView tv  = (TextView) findViewById(R.id.tv1);
		TextView tv1 = (TextView) findViewById(R.id.tv2); //TextView for Fire Alarm
		TextView tv2 = (TextView) findViewById(R.id.tv3); //TextView for Police Siren
		TextView tv3 = (TextView) findViewById(R.id.tv4); //TextView for Car Horn

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

			for (int i = 0; i < toTransform[0].length; i++) {
				int x = i;

				if (toTransform[0][i] > 30) {
					dummy = i;
					if (toTransform[0][i] > toTransform[0][0])
						max = toTransform[0][i];
					// Check for Police Siren
					// 57 => 900Hz and 78 => 1200Hz
					if (i >= 57 && i <= 78) {
						police = true;
						count1++;
						Log.d("This Message", "Count1 : " + count1);
					}
					else 
						count1--;
					// Check for Fire Alarm
					// 225 => 3500Hz and 237 => 3700Hz
					if (i >= 225 && i <= 237) {
						fire = true;
						count3++;
						Log.d("This Message", "Count3 : " + count3);
					}
				}

				int downy = (int) (200 - (toTransform[0][i] * 10));
				int upy = 200;

				canvas.drawLine(x, downy, x, upy, paint);
			}

			tv.setText("Peak Frequency : " + ((double) (dummy)) / 512 * 8000
					+ " Hz" + "    Magnitude : " + max + "Count 1,2,3 : "
					+ count1 + "," + count2 + "," + count3);
			if (count3 > 20) {
				tv2.setText("");
				tv3.setText("");
				tv1.setText("Fire Alarm Detected");
			}
			if (count1 > 30) {
				tv1.setText("");
				tv3.setText("");
				tv2.setText("Police Siren Detected");
			}
			if (count2 > 30) {
				tv1.setText("");
				tv2.setText("");
				tv3.setText("Car Horn Detected");
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