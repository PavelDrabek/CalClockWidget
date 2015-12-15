package cz.pazzi.clockwidget.Activities;

import android.accounts.AccountManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import cz.pazzi.clockwidget.R;
import cz.pazzi.clockwidget.Providers.GoogleProvider;

public class ChooseAccountActivity extends AppCompatActivity {

    public static final int REQUEST_ACCOUNT_PICKER = 1000;
    public static final int REQUEST_AUTHORIZATION = 1001;
    public static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;

    TextView txtStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_account);

        txtStatus = (TextView)findViewById(R.id.txtStatus);

        GoogleProvider.getInstance().accountActivity = this;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        chooseAccount();
    }

    private void chooseAccount() {
        Log.d(getClass().getName(), "chooseAccount");

        if(isGooglePlayServicesAvailable()) {
            GoogleProvider gProvider = GoogleProvider.getInstance();
            if (gProvider.IsInit()) {
                startActivityForResult(gProvider.NewChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
            } else {
                Log.w(getClass().getName(), "gProvider is not initialized");
            }
        } else {
            txtStatus.setText("Google Play Services required: \nafter installing, choose account again in widget settings.");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(getClass().getName(), "onActivityResult");


        switch(requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                Log.d(getClass().getName(), "REQUEST_GOOGLE_PLAY_SERVICES");
                if (resultCode != RESULT_OK) {
                    isGooglePlayServicesAvailable();
                }
                break;

            case REQUEST_ACCOUNT_PICKER:
                Log.d(getClass().getName(), "REQUEST_ACCOUNT_PICKER");
                if (resultCode == RESULT_OK && data != null && data.getExtras() != null) {
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
//                    if (accountName != null) {
                    GoogleProvider gProvider = GoogleProvider.getInstance();
                    gProvider.SetAccountName(accountName);
//                    finish();
//                    }
                } else if (resultCode == RESULT_CANCELED) {
//                    finish();
                    Log.w(getClass().getName(), "Account unspecified.");
                }
                finish();
                break;

            case REQUEST_AUTHORIZATION:
                Log.d(getClass().getName(), "REQUEST_AUTHORIZATION");
                if (resultCode != RESULT_OK) {
                    chooseAccount();
                }
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Check that Google Play services APK is installed and up to date. Will
     * launch an error dialog for the user to update Google Play Services if
     * possible.
     * @return true if Google Play Services is available and up to
     *     date on this device; false otherwise.
     */
    private boolean isGooglePlayServicesAvailable() {
        final int connectionStatusCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (GooglePlayServicesUtil.isUserRecoverableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
            return false;
        } else if (connectionStatusCode != ConnectionResult.SUCCESS ) {
            return false;
        }
        return true;
    }

    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     * @param connectionStatusCode code describing the presence (or lack of)
     *     Google Play Services on this device.
     */
    void showGooglePlayServicesAvailabilityErrorDialog(final int connectionStatusCode) {
        Log.d(getClass().getName(), "showGooglePlayServiceAvailabilityErrorDialog");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Dialog dialog = GooglePlayServicesUtil.getErrorDialog(connectionStatusCode, ChooseAccountActivity.this, REQUEST_GOOGLE_PLAY_SERVICES);
                dialog.show();
            }
        });
    }
}
