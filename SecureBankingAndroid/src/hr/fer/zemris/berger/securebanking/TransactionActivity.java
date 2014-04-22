package hr.fer.zemris.berger.securebanking;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import dalvik.system.DexFile;

public class TransactionActivity extends Activity implements OnClickListener {

	public static final String TAG = "Trans";

	private static byte[] hash;

	private EditText editTextTo;
	private EditText editTextFrom;
	private EditText editTextAmount;
	private Button buttonYes;
	private Button buttonNo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		editTextTo = (EditText) findViewById(R.id.editTextTo);
		editTextFrom = (EditText) findViewById(R.id.editTextFrom);
		editTextAmount = (EditText) findViewById(R.id.editTextAmount);

		buttonYes = (Button) findViewById(R.id.buttonYes);
		buttonNo = (Button) findViewById(R.id.buttonNo);

		buttonYes.setOnClickListener(this);
		buttonNo.setOnClickListener(this);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public static String convertStreamToString(InputStream is) throws Exception {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		String line = null;
		while ((line = reader.readLine()) != null) {
			sb.append(line).append("\n");
		}
		reader.close();
		return sb.toString();
	}

	public static String getStringFromFile(String filePath) throws Exception {
		File fl = new File(filePath);
		FileInputStream fin = new FileInputStream(fl);
		String ret = convertStreamToString(fin);
		// Make sure you close all streams.
		fin.close();
		return ret;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.buttonYes:
			try {
				hash = footprint("SHA-1");
				Log.d(TAG, toHexString(hash));
			} catch (NoSuchAlgorithmException e) {
				Log.d(TAG, "algoritam", e);
			} catch (IOException e) {
				Log.d(TAG, "citanje", e);
			}

			new ServerCommunicator()
					.execute("http://localhost:8080/SecureBankingWeb/Dispatcher");

			
			Toast.makeText(this, toHexString(hash), Toast.LENGTH_SHORT).show();
			break;
		case R.id.buttonNo:
			Toast.makeText(this, "No", Toast.LENGTH_SHORT).show();
			Log.d(TAG, "No button pressed");

			break;
		}

	}

	private byte[] footprint(String algorithm) throws IOException,
			NoSuchAlgorithmException {

		String output = "";
		String name = null;
		// load file input stream of this class
		String sourceDir = this.getApplicationInfo().sourceDir;
		try {
			DexFile dexFile = new DexFile(sourceDir);
			name = dexFile.getName();

			Log.d(TAG, name);
		} catch (IOException e) {
			Log.d(TAG, "NEMOS", e);
		}

		File file = new File(name);
		byte[] buffer = new byte[4096];

		MessageDigest md = MessageDigest.getInstance(algorithm);

		BufferedInputStream stream = new BufferedInputStream(
				new FileInputStream(file));

		int len = 0;

		while ((len = stream.read(buffer)) != -1) {
			md.update(buffer, 0, len);
		}

		byte[] finalDigest = new byte[md.getDigestLength()];
		finalDigest = md.digest(buffer);

		// read bytes from input stream
		// byte[] byteArray = toByteArray(stream);

		// digest those bytes with SHA-1 algorithm

		// convert SHA-1 digest byte array to HEX string
		// output = toHexString(md.digest(byteArray));
		output = toHexString(finalDigest);

		return finalDigest;
	}

	public static byte[] toByteArray(InputStream stream) throws IOException {

		// create new byte array output stream
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		int nextByte;

		// read bytes from the input stream and store them to the
		// byte array output stream
		while ((nextByte = stream.read()) != -1) {
			output.write(nextByte);
		}

		return output.toByteArray();
	}

	final private static char[] hexArray = "0123456789ABCDEF".toCharArray();

	public static String toHexString(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}

	public static String send(String url, Transaction transaction) {
		InputStream inputstream;
		String result = "Didn't receive response!";
		try {
			// create client
			HttpClient client = new DefaultHttpClient();

			// create post request
			HttpPost postRequest = new HttpPost(url);

			JSONObject jsonObject = new JSONObject();
			jsonObject.accumulate("sender",
					transaction.getSenderAccountNumber());
			jsonObject.accumulate("recipient",
					transaction.getRecipientAccountNumber());
			jsonObject.accumulate("amount", transaction.getAmount());
			jsonObject.accumulate("hash", hash);

			// create string from json object
			String json = jsonObject.toString();

			// create entity from json string for http POST request
			StringEntity entity = new StringEntity(json);

			// set http post entity to previously created one
			postRequest.setEntity(entity);

			// set headers
			postRequest.setHeader("Accept", "application/json");
			postRequest.setHeader("Content-type", "application/json");

			// execute request and receive response
			HttpResponse postResponse = client.execute(postRequest);

			// get input stream from response
			inputstream = postResponse.getEntity().getContent();

			if (inputstream != null) {
				BufferedReader bufferedReader = new BufferedReader(
						new InputStreamReader(inputstream));
				result = "Received response!";
				try {
					result = bufferedReader.readLine();
				} catch (Exception e) {
					result = "iznimka";
				}
			}

		} catch (Exception e) {
			Log.d("InputStream", e.getMessage());
		}
		return result;

	}

	class ServerCommunicator extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... urls) {
			Transaction transaction = new Transaction();

			transaction.setSenderAccountNumber(editTextFrom.getText()
					.toString());
			transaction.setRecipientAccountNumber(editTextTo.getText()
					.toString());
			transaction.setAmount(editTextAmount.getText().toString());
			Log.d(TAG, urls[0]);
			return send(urls[0], transaction);
		}

		@Override
		protected void onPostExecute(String result) {
			 Toast.makeText(getBaseContext(), result,
			 Toast.LENGTH_SHORT).show();
		}

	}

}
