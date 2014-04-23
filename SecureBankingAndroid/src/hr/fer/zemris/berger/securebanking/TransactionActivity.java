package hr.fer.zemris.berger.securebanking;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import hr.fer.zemris.berger.securebanking.model.Footprint;
import hr.fer.zemris.berger.securebanking.model.Transaction;
import hr.fer.zemris.berger.securebanking.util.Util;

public class TransactionActivity extends Activity implements OnClickListener {

	public static final String TAG = "Trans";

    private static String serviceURL = "http://securebankingweb.appspot.com/hashdb";
	private static byte[] hash;
    private static String signature;
    private static String deviceID;

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

        try {
            hash = Util.footprint("SHA-1", this);
        } catch (IOException e) {
            Log.d(TAG, "citanje", e);
        } catch (NoSuchAlgorithmException e) {
            Log.d(TAG, "algoritam", e);
        }

        deviceID = Util.getDeviceID(this);

        try {
            signature = Util.signature(this);
        } catch (PackageManager.NameNotFoundException e) {
            Log.d(TAG, "potpis", e);
        }

        Footprint footprint = new Footprint(deviceID,Util.toHexString(hash),signature);

    }


	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.buttonYes:
			Log.d(TAG, Util.toHexString(hash));
			new ServerCommunicator()
					.execute(serviceURL);
			Toast.makeText(this, Util.toHexString(hash), Toast.LENGTH_SHORT).show();
			break;
		case R.id.buttonNo:
			Toast.makeText(this, "No", Toast.LENGTH_SHORT).show();
			break;
		}

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
					transaction.getSenderAccount());
			jsonObject.accumulate("recipient",
					transaction.getRecipientAccount());
			jsonObject.accumulate("amount", transaction.getAmount());
			jsonObject.accumulate("hash", Util.toHexString(hash));
            jsonObject.accumulate("signature",signature);
            jsonObject.accumulate("deviceID",deviceID);
			
			// create string from json object
			String json = jsonObject.toString();

			// create entity from json string for http POST request
			BasicNameValuePair param = new BasicNameValuePair("info",json);
			List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
			params.add(param);
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params);	

			// set http post entity to previously created one
			postRequest.setEntity(entity);

			// set headers
			//postRequest.setHeader("Accept", "application/json");
			//postRequest.setHeader("Content-type", "application/json");

			// execute request and receive response
			HttpResponse postResponse = client.execute(postRequest);

			// get input stream from response
			inputstream = postResponse.getEntity().getContent();

			StringBuilder sb = new StringBuilder();

			if (inputstream != null) {
				BufferedReader bufferedReader = new BufferedReader(
						new InputStreamReader(inputstream));
				result = "Received response!";
				try {
					String line = "";
					while ((line = bufferedReader.readLine()) != null) {
						sb.append(line);
					}

					result = sb.toString();
				} catch (Exception e) {
					result = "iznimka";
				}
			}

		} catch (Exception e) {
			Log.d("InputStream", e.getMessage());
		}
		return result;

	}

	private class ServerCommunicator extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... urls) {
			Transaction transaction = new Transaction();

			transaction.setSenderAccount(editTextFrom.getText()
					.toString());
			transaction.setRecipientAccount(editTextTo.getText()
					.toString());
			transaction.setAmount(editTextAmount.getText().toString());
			// Log.d(TAG, urls[0]);
			return send(urls[0], transaction);
		}

		@Override
		protected void onPostExecute(String result) {
			Toast.makeText(getBaseContext(), result, Toast.LENGTH_LONG).show();
		}

	}

}
