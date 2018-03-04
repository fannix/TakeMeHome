package fannix.com.takemehome;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;

import io.nayuki.bitcoin.crypto.Ripemd160;
import kotlin.Pair;
import neowallet.Neowallet;
import neowallet.Wallet;
import network.o3.o3wallet.API.NEO.NeoNodeRPC;

import static network.o3.o3wallet.CryptoExtensionsKt.byteArrayToHex;

/**
 * TrackingActivity is the workhorse of this App. It implements the following functionality
 *
 * 1. Tracking the movement of the beacon
 * 2. Submit the beacon location
 * 3. Call the smart contract to get rewards
 */

public class TrackingActivity extends AppCompatActivity implements BeaconConsumer{

    protected static final String TAG = "TrackingActivity";

    private BeaconManager beaconManager = BeaconManager.getInstanceForApplication(this);

    private long lastTransactionTime = -1;
    private final long  TRANSACTION_INTERVAL = 60 * 5;

    private Location curLocation = null;
    public String emailAddr = null;
    public String url = null;
    public static String rpcAddr = "http://seed2.neo.org:20332";
    public static final String REGISTRY_ADDRESS = "6478509f833cccbbc5a9f70e6d8183065b54b48f";
    public static final String PERSONAL_ADDRESS = "0000000000000000000000000000000000000001";

    private FusedLocationProviderClient mFusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        beaconManager.bind(this);

        if (url != null) {
            WebView infoView = findViewById(R.id.beaconInfo);
            infoView.getSettings().setJavaScriptEnabled(false);
            infoView.loadUrl(url);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (beaconManager.isBound(this))
            beaconManager.setBackgroundMode(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (beaconManager.isBound(this))
            beaconManager.setBackgroundMode(false);
    }

    /**
     * This callback function will be called when a beacon is detected.
     */
    @Override
    public void onBeaconServiceConnect() {
        beaconManager.setRangeNotifier((beacons, region) -> {
            if (beacons.size() > 0) {
                for (Beacon beacon: beacons) {
                    String beaconStr = beacon.toString();
                    Log.i(TAG, "beacon detected: " + beaconStr);
                    submitLocation(beaconStr);

                    String log = beaconStr + " is about " + beacon.getDistance() + " meters away";
                    show(log);
                    if (this.url != null) {
                        runOnUiThread(() -> {
                            WebView infoView = findViewById(R.id.beaconInfo);
                            infoView.getSettings().setJavaScriptEnabled(false);
                            infoView.loadUrl(this.url);
                        });
                    }
                }
            }
        });

        try {
            beaconManager.startRangingBeaconsInRegion((new Region("TrackingID", null, null, null)));
        } catch (RemoteException e) {
            Log.e(TAG, "Error tracking beacons");
        }

    }

    /**
     *  Show real-time log of the beacon movement
     * @param update
     */
    private void show(final String update) {
        runOnUiThread(() -> {
            TextView trackingText = TrackingActivity.this.findViewById(R.id.tracking);
            trackingText.setText(update);
        });
    }

    /**
     * Get the last location of the device and submit
     */
    private void submitLocation(String beaconStr) {
        if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                // call the smart contract once per TRANSACTION_INTERVAL seconds
                long curTime = System.currentTimeMillis() / 1000L;
                if (curTime - lastTransactionTime > TRANSACTION_INTERVAL) {

                    this.curLocation = location;
                    Log.d(TAG, location.toString());

                    Params params = new Params(this, location, beaconStr);
                    new Task().execute(params);
                    this.lastTransactionTime = curTime;
                }
            });
        }
    }

    private void sendEmail(String email, Location location) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setType("text/plain");
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, email);
        String subject = "We found it at " + location.toString();
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    public void onclickSendEmail(View view) {
        Log.d(TAG, "sending emails to " + emailAddr);
        sendEmail(emailAddr, curLocation);
    }
}

class Task extends AsyncTask<Params, Void, Void> {

    // This is the wif used for transaction
    private static String wif = "5KfFjSyuuuNEMqvbGxxca8GNGDzDKyTK44NgSgZjVokqmQ4zY12";
    /**
     * Override this method to perform a computation on a background thread. The
     * specified parameters are the parameters passed to {@link #execute}
     * by the caller of this task.
     * <p>
     * This method can call {@link #publishProgress} to publish updates
     * on the UI thread.
     *
     * @param paramsList The parameters of the task.
     * @return A result, defined by the subclass of this task.
     * @see #onPreExecute()
     * @see #onPostExecute
     * @see #publishProgress
     */
    @Override
    protected Void doInBackground(Params... paramsList) {
        for (Params params: paramsList) {
            TrackingActivity activity = params.activity;
            Location location = params.location;
            String beaconID = params.beaconID;

            API api = new API(activity.rpcAddr, activity.REGISTRY_ADDRESS);
            // convert the beacon string to hash160 type
            String beaconKey =  byteArrayToHex(Ripemd160.getHash(beaconID.getBytes()));
            String scAddr = API.maskAddress(api.getContractAddress(beaconKey), beaconID);

            // ignore beacons that haven't been registered
            if (scAddr == null) {
                return null;
            }

            API scApi = new API(activity.rpcAddr, scAddr);
            String email = scApi.getEmail();
            activity.emailAddr = email;

            String pageUrl = scApi.getUrl();
            activity.url = pageUrl;

            getReward(scAddr, location);

            Button button = activity.findViewById(R.id.sendEmail);
            if(email != null) {
                button.setEnabled(true);
            } else {
                button.setEnabled(false);
            }

        }
        return null;
    }

    private void getReward(String scAddr, Location location) {
        NeoNodeRPC neoNodeRPC = new NeoNodeRPC(TrackingActivity.rpcAddr);

        Wallet wallet = null;

        try {
            wallet = Neowallet.generateFromWIF(wif);

            ArrayList ar = new ArrayList();
            String loc = API.toHex(location.getLatitude() + "," + location.getLongitude());
            ar.add(loc);
            ar.add(TrackingActivity.PERSONAL_ADDRESS);

            neoNodeRPC.invoke(wallet, scAddr, "submit", ar,
                    (Pair<Boolean, ? extends Error> pair) -> {
                        Log.d(TrackingActivity.TAG, pair.getFirst().toString());
                        return null;
                    });
        } catch (Exception e) {
            Log.e(TrackingActivity.TAG, "wallet is invalid");
            e.printStackTrace();
        }
    }
}

class Params {

    public TrackingActivity activity;
    public Location location;
    public String beaconID;

    public Params(TrackingActivity activity, Location location, String beaconID) {
        this.activity = activity;
        this.location = location;
        this.beaconID = beaconID;
    }
}
