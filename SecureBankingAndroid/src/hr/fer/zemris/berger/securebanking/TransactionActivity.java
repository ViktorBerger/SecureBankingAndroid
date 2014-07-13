package hr.fer.zemris.berger.securebanking;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import hr.fer.zemris.berger.securebanking.model.Transaction;
import hr.fer.zemris.berger.securebanking.util.Util;

/**
 * Main application activity.
 * 
 * @author Viktor Berger
 * @version 1.0
 */
public class TransactionActivity extends Activity implements OnClickListener {

	public static final String TAG = "Trans";

	/** URL of the server. */
	private static String serviceURL = "http://securebankingweb2.appspot.com/communication";
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

        if (!isConnectedToInternet()) {
            showNoConnectionDialog(this);
        }


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

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.buttonYes:
            if(isConnectedToInternet()) {
                showNoConnectionDialog(this);
            }
            if(isFormValid()) {
                Log.d(TAG, Util.toHexString(hash));
                new ServerCommunicator().execute(serviceURL);
                Toast.makeText(this, Util.toHexString(hash), Toast.LENGTH_SHORT)
                        .show();
            }
			break;
		case R.id.buttonNo:
            finish();
            moveTaskToBack(true);
			break;
		}

	}

    /**
     * Checks if the phone is connected to the internet.
     * @return <code>true</code> if there is an active internet connection,
     *         <code>false</code> otherwise
     */
    private boolean isConnectedToInternet() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if ((cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected()))
            return true;
        else
            return false;
    }


    /**
     * Creates a dialog which enables user to jump directly to internet settings.
     * @param context application context
     */
    public static void showNoConnectionDialog(Context context) {
        final Context ctx = context;
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setCancelable(true);
        builder.setMessage("No connection");
        builder.setTitle("No internet connection");
        builder.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                ctx.startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                return;
            }
        });

        builder.show();
    }

    /**
     * Form validation method.
     * @return <code>true<code/> if form is valid, <code>false</code> otherwise
     */
    private boolean isFormValid() {
        boolean valid = true;
        if(editTextTo.getText().length() != 10) {
            editTextTo.setError("Recipient account has to be 10 digits long!");
            valid = false;
        }
        if(editTextFrom.getText().length() != 10) {
            editTextFrom.setError("Recipient account has to be 10 digits long!");
            valid = false;
        }
        if(editTextAmount.getText().length() == 0) {
            editTextAmount.setError("Amount is required!");
            valid = false;
        }
        return valid;
    }

	/**
	 * Sends transaction and footprint data to server.
	 * @param url server url
	 * @param transaction transaction object with information about transaction
	 * @return server response
	 */
	@SuppressLint("SimpleDateFormat")
	public static String send(String url, Transaction transaction) {
		InputStream inputstream;
		String result = "Didn't receive response!";
		try {
			// create client
			HttpClient client = new DefaultHttpClient();

			// create post request
			HttpPost postRequest = new HttpPost(url);

			SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
			Date date = new Date();

			JSONObject jsonObject = new JSONObject();
			jsonObject.accumulate("sender", transaction.getSenderAccount());
			jsonObject.accumulate("recipient",
					transaction.getRecipientAccount());
			jsonObject.accumulate("amount", transaction.getAmount());
			jsonObject.accumulate("hash", Util.toHexString(hash));
			jsonObject.accumulate("signature", signature);
			jsonObject.accumulate("deviceID", deviceID);
			jsonObject.accumulate("timestamp", sdf.format(date));

			// create string from json object
			String json = jsonObject.toString();

			// create entity from json string for http POST request
			BasicNameValuePair param = new BasicNameValuePair("info", json);
			List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
			params.add(param);
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params);

			// set http post entity to previously created one
			postRequest.setEntity(entity);

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

	/**
	 * Implements communication with server.
	 * @author Viktor Berger
	 * @version 1.0
	 */
	private class ServerCommunicator extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... urls) {
			Transaction transaction = new Transaction();

			transaction.setSenderAccount(editTextFrom.getText().toString());
			transaction.setRecipientAccount(editTextTo.getText().toString());
			transaction.setAmount(editTextAmount.getText().toString());
			return send(urls[0], transaction);
		}

		@Override
		protected void onPostExecute(String result) {
			Toast.makeText(getBaseContext(), result, Toast.LENGTH_LONG).show();
		}

	}

}
