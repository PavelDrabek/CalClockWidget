//package cz.pazzi.clockwidget.Activities;
//
//import android.accounts.AccountManager;
//import android.app.Dialog;
//import android.content.Context;
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.net.ConnectivityManager;
//import android.net.NetworkInfo;
//import android.support.v7.app.AppCompatActivity;
//import android.os.Bundle;
//import android.text.TextUtils;
//import android.util.Log;
//import android.view.Menu;
//import android.view.MenuItem;
//import android.widget.TextView;
//
//import com.google.android.gms.common.ConnectionResult;
//import com.google.android.gms.common.GooglePlayServicesUtil;
//import com.google.api.client.extensions.android.http.AndroidHttp;
//import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
//import com.google.api.client.http.HttpTransport;
//import com.google.api.client.json.JsonFactory;
//import com.google.api.client.json.jackson2.JacksonFactory;
//import com.google.api.client.util.ExponentialBackOff;
//import com.google.api.services.calendar.CalendarScopes;
//
//import java.util.Arrays;
//import java.util.List;
//
//import cz.pazzi.clockwidget.GCalApiAsyncTask;
//import cz.pazzi.clockwidget.R;
//import cz.pazzi.clockwidget.Providers.GoogleProvider;
//
//public class GoogleProviderActivity extends AppCompatActivity {
//
//    static final int REQUEST_ACCOUNT_PICKER = 1000;
//    static final int REQUEST_AUTHORIZATION = 1001;
//    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
//
//    private static final String PREF_ACCOUNT_NAME = "accountName";
//    private static final String[] SCOPES = { CalendarScopes.CALENDAR_READONLY };
//
//    final HttpTransport transport = AndroidHttp.newCompatibleTransport();
//    final JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
//
//    TextView mStatusText;
//    TextView mResultsText;
//
//    GoogleProvider gProvider;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_google_provider);
//
//        mStatusText = (TextView)findViewById(R.id.txtStatus);
//        mResultsText = (TextView)findViewById(R.id.txtResult);
//
//        gProvider = GoogleProvider.getInstaceWithoutCallActivity(this);
//        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
//        gProvider.credential = GoogleAccountCredential.usingOAuth2(
//                getApplicationContext(), Arrays.asList(SCOPES))
//                .setBackOff(new ExponentialBackOff())
//                .setSelectedAccountName(GetAccountName());
//
//        gProvider.mService = new com.google.api.services.calendar.Calendar.Builder(
//                transport, jsonFactory, gProvider.credential)
//                .setApplicationName("Google Calendar API Android Quickstart")
//                .build();
//
//        if(gProvider.credential.getSelectedAccountName() == null) {
//            startActivityForResult(gProvider.credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
//        }
//    }
//
//    /**
//     * Called whenever this activity is pushed to the foreground, such as after
//     * a call to onCreate().
//     */
//    @Override
//    protected void onResume() {
//        super.onResume();
//        Log.d(getClass().getName(), "OnResume");
////
////        if(gProvider.credential.getSelectedAccountName() == null) {
////            startActivityForResult(gProvider.credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
////        }
//
////        String accountName = GetAccountName();
////        Log.d(getClass().getName(), "account name = " + accountName);
////        if(accountName != null) {
////            Log.d(getClass().getName(), "account is not null");
////            SetAccountName(accountName);
////            finish();
////            return;
////        }
////
////        if (isGooglePlayServicesAvailable()) {
////            refreshResults();
////        } else {
////            mStatusText.setText("Google Play Services required: after installing, close and relaunch this app.");
////        }
//    }
//
//    /**
//     * Attempt to get a set of data from the Google Calendar API to display. If the
//     * email address isn't known yet, then call chooseAccount() method so the
//     * user can pick an account.
//     */
//    private void refreshResults() {
//        Log.d(getClass().getName(), "refreshResult");
//
//        if (gProvider.credential.getSelectedAccountName() == null) {
//            chooseAccount();
//        } else {
//            if (isDeviceOnline()) {
////                mProgress.show();
////                new GCalApiAsyncTask(this).execute();
//            } else {
//                mStatusText.setText("No network connection available.");
//            }
//        }
//    }
//
//    /**
//     * Called when an activity launched here (specifically, AccountPicker
//     * and authorization) exits, giving you the requestCode you started it with,
//     * the resultCode it returned, and any additional data from it.
//     * @param requestCode code indicating which activity result is incoming.
//     * @param resultCode code indicating the result of the incoming
//     *     activity result.
//     * @param data Intent (containing result data) returned by incoming
//     *     activity result.
//     */
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        Log.d(getClass().getName(), "onActivityResult");
//        switch(requestCode) {
//            case REQUEST_GOOGLE_PLAY_SERVICES:
//                Log.d(getClass().getName(), "REQUEST_GOOGLE_PLAY_SERVICES");
//                if (resultCode != RESULT_OK) {
//                    isGooglePlayServicesAvailable();
//                }
//                break;
//            case REQUEST_ACCOUNT_PICKER:
//                Log.d(getClass().getName(), "REQUEST_ACCOUNT_PICKER");
//                if (resultCode == RESULT_OK && data != null && data.getExtras() != null) {
//                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
//                    if (accountName != null) {
//                        SetAccountName(accountName);
//                        finish();
//                    }
//                } else if (resultCode == RESULT_CANCELED) {
//                    mStatusText.setText("Account unspecified.");
//                    finish();
//                }
//                break;
//            case REQUEST_AUTHORIZATION:
//                Log.d(getClass().getName(), "REQUEST_AUTHORIZATION");
//                if (resultCode != RESULT_OK) {
//                    chooseAccount();
//                }
//                break;
//        }
//
//        super.onActivityResult(requestCode, resultCode, data);
//    }
//
//    private void SetAccountName(String accountName) {
//        if(accountName == null) {
//            return;
//        }
//
//        gProvider.credential.setSelectedAccountName(accountName);
////        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
////        SharedPreferences.Editor editor = settings.edit();
////        editor.putString(PREF_ACCOUNT_NAME, accountName);
////        editor.commit();
//
//        SharedPreferences settings = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = settings.edit();
//        editor.putString(getString(R.string.preference_account_name), accountName);
//        editor.commit();
//        gProvider.accountName = accountName;
//    }
//
//    private String GetAccountName() {
//        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
//        return settings.getString(PREF_ACCOUNT_NAME, null);
//    }
//
//    /**
//     * Clear any existing Google Calendar API data from the TextView and update
//     * the header message; called from background threads and async tasks
//     * that need to update the UI (in the UI thread).
//     */
//    public void clearResultsText() {
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                mStatusText.setText("Retrieving data…");
//                mResultsText.setText("");
//            }
//        });
//    }
//
//    /**
//     * Fill the data TextView with the given List of Strings; called from
//     * background threads and async tasks that need to update the UI (in the
//     * UI thread).
//     * @param dataStrings a List of Strings to populate the main TextView with.
//     */
//    public void updateResultsText(final List<String> dataStrings) {
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                if (dataStrings == null) {
//                    mStatusText.setText("Error retrieving data!");
//                } else if (dataStrings.size() == 0) {
//                    mStatusText.setText("No data found.");
//                } else {
//                    mStatusText.setText("Data retrieved using" +
//                            " the Google Calendar API:");
//                    mResultsText.setText(TextUtils.join("\n\n", dataStrings));
//                }
//            }
//        });
//    }
//
//    /**
//     * Show a status message in the list header TextView; called from background
//     * threads and async tasks that need to update the UI (in the UI thread).
//     * @param message a String to display in the UI header TextView.
//     */
//    public void updateStatus(final String message) {
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                mStatusText.setText(message);
//            }
//        });
//    }
//
//    /**
//     * Starts an activity in Google Play Services so the user can pick an
//     * account.
//     */
//    private void chooseAccount() {
//        Log.d(getClass().getName(), "chooseAccount");
//        startActivityForResult(gProvider.credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
//    }
//
//    /**
//     * Checks whether the device currently has a network connection.
//     * @return true if the device has a network connection, false otherwise.
//     */
//    private boolean isDeviceOnline() {
//        Log.d(getClass().getName(), "isDeviceOnline");
//        ConnectivityManager connMgr =
//                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
//        return (networkInfo != null && networkInfo.isConnected());
//    }
//
//    /**
//     * Check that Google Play services APK is installed and up to date. Will
//     * launch an error dialog for the user to update Google Play Services if
//     * possible.
//     * @return true if Google Play Services is available and up to
//     *     date on this device; false otherwise.
//     */
//    private boolean isGooglePlayServicesAvailable() {
//        final int connectionStatusCode =
//                GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
//        if (GooglePlayServicesUtil.isUserRecoverableError(connectionStatusCode)) {
//            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
//            return false;
//        } else if (connectionStatusCode != ConnectionResult.SUCCESS ) {
//            return false;
//        }
//        return true;
//    }
//
//    /**
//     * Display an error dialog showing that Google Play Services is missing
//     * or out of date.
//     * @param connectionStatusCode code describing the presence (or lack of)
//     *     Google Play Services on this device.
//     */
//    void showGooglePlayServicesAvailabilityErrorDialog(final int connectionStatusCode) {
//        Log.d(getClass().getName(), "showGooglePlayServiceAvailabilityErrorDialog");
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
//                        connectionStatusCode,
//                        GoogleProviderActivity.this,
//                        REQUEST_GOOGLE_PLAY_SERVICES);
//                dialog.show();
//            }
//        });
//    }
//}
