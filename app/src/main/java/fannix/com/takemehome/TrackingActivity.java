package fannix.com.takemehome;

import kotlin.Pair;

import java.util.ArrayList;
import java.util.Collection;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import neowallet.Neowallet;
import neowallet.Wallet;
import network.o3.o3wallet.API.NEO.NeoNodeRPC;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

/**
 * TrackingActivity is the workhorse of this App. It implemnts the following functionality
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

    private String wif = "5KfFjSyuuuNEMqvbGxxca8GNGDzDKyTK44NgSgZjVokqmQ4zY12";
    private String rpcAddr = "http://seed3.neo.org:20332";
    private final String REGISTRY_ADDRESS = "8c6d018e9a89dbbb35cab3e421ae11b387c74aaf";
    private final String TEST_SC_ADDRESS = "7477474db9182e5f0bbc1622274f98ea6c661567";

    private FusedLocationProviderClient mFusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        beaconManager.bind(this);
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
        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if (beacons.size() > 0) {
                    Log.i(TAG, "beacon detected");
                    Beacon firstBeacon = beacons.iterator().next();
                    String beaconStr = firstBeacon.toString();
                    submitLocation(beaconStr);
                    show(beaconStr+ " is about " + firstBeacon.getDistance() + " meters away");
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
     * @param log
     */
    private void show(final String log) {
        runOnUiThread(() -> {
            TextView trackingText = TrackingActivity.this.findViewById(R.id.tracking);
            trackingText.setText(log);
        });
    }

    /**
     * Get the last location of the device and submit
     */
    private void submitLocation(String beaconStr) {
        if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                String sc_addr = getContractAddress(beaconStr);
                Log.d(TAG, location.toString());
                // 1. get the URL and submit the location

                // 2. call the contract to get reward
                getReward(sc_addr);
            });
        }
    }

    private void getBalance() {
        NeoNodeRPC neoNodeRPC = new NeoNodeRPC(rpcAddr);
        neoNodeRPC.getTokenBalanceOf("ecc6b20d3ccac1ee9ef109af5a7cdb85706b1df9",
                "AJShjraX4iMJjwVt8WYYzZyGvDMxw6Xfbe",
                (Pair<Long, ? extends Error> pair) -> {
                    Log.d(TAG, pair.getFirst().toString());
                    return null;
                });
    }

    private void getReward(String sc_addr) {
        NeoNodeRPC neoNodeRPC = new NeoNodeRPC(rpcAddr);

        Wallet wallet = null;

        try {
            wallet = Neowallet.generateFromWIF(wif);

            long curTime = System.currentTimeMillis() / 1000L;
            // call the smart contract once per TRANSACTION_INTERVAL seconds
            if (curTime - lastTransactionTime > TRANSACTION_INTERVAL){
                ArrayList ar = new ArrayList();
                ar.add("01");
                ar.add(REGISTRY_ADDRESS);

                neoNodeRPC.invoke(wallet, sc_addr, "submit", ar,
                        (Pair<Boolean, ? extends Error> pair) -> {
                            Log.d(TAG, pair.getFirst().toString());
                            return null;
                        });
                lastTransactionTime = curTime;
            }
        } catch (Exception e) {
            Log.e(TAG, "wallet is invalid");
            e.printStackTrace();
        }
    }

    /**
     * Retrieve the contract address from the main registry
     * @param beaconID
     */
    private String getContractAddress(String beaconID) {
//        try {
//            NeoNodeRPC node = new NeoNodeRPC(rpcAddr);
//
//            node.queryRegistry(REGISTRY_ADDRESS, beaconID,
//                    (Pair<String, ? extends Error> pair) -> {
//                        String contractAddr = pair.getFirst();
//                        Log.d(TAG, contractAddr);
//                        return null;
//                    });
//
//        } catch (Exception e) {
//            Log.e(TAG, "Failed to get the contract address for " + beaconID);
//            e.printStackTrace();
//        }

        return TEST_SC_ADDRESS;
    }

    /**
     * deploy the smart contract
     */
    private void deploy() {
        try {
            Wallet wallet = Neowallet.generateFromWIF(wif);
            NeoNodeRPC node = new NeoNodeRPC(rpcAddr);
            node.invoke(wallet, TEST_SC_ADDRESS, "deploy", null,
                    (Pair<Boolean, ? extends Error> pair) -> {
                        Log.d(TAG, "deploy result: " + pair.getFirst());
                        return null;
                    }
            );
        } catch (Exception e) {
            Log.e(TAG, "failed to open wallet");
            e.printStackTrace();
        }
    }
}
