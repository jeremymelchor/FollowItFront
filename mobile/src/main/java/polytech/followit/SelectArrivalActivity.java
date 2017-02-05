package polytech.followit;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.estimote.sdk.SystemRequirementsChecker;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

import polytech.followit.adapter.MyCustomPOIListAdapter;
import polytech.followit.model.POI;
import polytech.followit.rest.SocketCallBack;
import polytech.followit.utility.PathSingleton;

public class SelectArrivalActivity extends AppCompatActivity implements
        SocketCallBack,
        View.OnClickListener {

    private static final String TAG = SelectArrivalActivity.class.getSimpleName();

    //POI DE DEPART ET DESTINATION
    public String depart;
    public String arrivee;

    ProgressDialog progressDialog;

    MyCustomPOIListAdapter dataAdapter = null;

    //==============================================================================================
    // Lifecycle
    //==============================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.arrival_activity);

        SystemRequirementsChecker.checkWithDefaultDialogs(this);

        PathSingleton.getInstance().setSocketCallBack(this);

        Intent intent= getIntent();
        Bundle b = intent.getExtras();

        if(b!=null)
        {
            depart =(String) b.get("source");
        }
        Log.d(TAG, depart);


        Button getPathButton = (Button) findViewById(R.id.get_path_button);
        Button goBackButton = (Button) findViewById(R.id.go_back_button);

        Log.d(TAG, "GET POI LIST");
        //GET POI LIST
        PathSingleton.getInstance().askPOIList();

        // Set listeners
        getPathButton.setOnClickListener(this);
        goBackButton.setOnClickListener(this);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setIndeterminate(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(true);
        progressDialog.show();
    }


    @Override
    protected void onResume() {
        super.onResume();
        //On réaffiche le bouton au lieu du progressba

        SystemRequirementsChecker.checkWithDefaultDialogs(this);
    }

    //==============================================================================================
    // Listeners implementations
    //==============================================================================================

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.get_path_button:
                Log.d(TAG, "click");
                progressDialog.show();
                onPathClicked();
                break;
            case R.id.go_back_button:
                Intent mainIntent = new Intent(SelectArrivalActivity.this, SelectDepartureActivity.class);
                startActivity(mainIntent);
                progressDialog.dismiss();
                finish();
            break;
        }
    }

    /**
     * Fired when we received the path
     *
     * @throws JSONException
     */
    @Override
    public void onPathFetched() throws JSONException {
        Log.d(TAG, "ONPATHFETCHED");
        // Dismiss progressDialog hence avoid leak windows
        progressDialog.dismiss();
        startActivity(new Intent(this, NavigationActivity.class));
    }

    @Override
    public void onPOIListFetched() {
        Log.d(TAG, "NOTIF: POI LIST" + PathSingleton.getInstance().getListAllPoi().toString());

        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Liste Dropdown pour le départ

                //Generate list View from ArrayList
                displayListView(PathSingleton.getInstance().getListAllPoi());

                progressDialog.hide();
            }
        });
    }

    @Override
    public void onSendNotificationRequest(String action) {

    }

    @Override
    public void onArrival() {

    }

    @Override
    public void onBroadcastNotification(final String message) {
    }


    //==============================================================================================
    // Private utils functions
    //==============================================================================================

    private void getPath() {
        //NOEUD DEPART ET ARRIVEE
        String nodeSource = "";
        String nodeDestination = "";

        //Pour chaque POI cliqué par l'user, on cherche son noeud correspondant
        for (POI p : PathSingleton.getInstance().getListAllPoi()) {
            if (Objects.equals(p.getName(), depart)) {
                nodeSource = p.getNode();
            } else if (Objects.equals(p.getName(), arrivee)) {
                nodeDestination = p.getNode();
            }
        }
        Log.d(TAG, "GETPATH: src " + nodeSource + "dest  " + nodeDestination);
        //si nos deux noeuds ont été bien récupérés, on créé le JSON
        if (!Objects.equals(nodeSource, "") && !Objects.equals(nodeDestination, "")) {
            //on en profite pour enregistrer les noeuds dans PATH
            //PathSingleton.getInstance().getPath().setSource(nodeSource);
            //PathSingleton.getInstance().getPath().setDestination(nodeDestination);
            JSONObject itinerary = new JSONObject();
            try {
                itinerary.put("source", nodeSource);
                itinerary.put("destination", nodeDestination);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            //On appelle le socket.emit demandant le chemin
            Log.d(TAG, "GETPATH JSON : " + itinerary.toString());
            PathSingleton.getInstance().askForPath(itinerary);
        }
    }

    private void onPathClicked() {
        //on recupere le val du radiobutton
        if (depart != null) {
            arrivee = getSelectedRadioButton();
            if (arrivee != null) {
                getPath();
            }
        }
    }

    //==============================================================================================
    // List view implementation
    //==============================================================================================

    private void displayListView(ArrayList<POI> POIList) {
        //create an ArrayAdaptar from the String Array
        dataAdapter = new MyCustomPOIListAdapter(this,
                R.layout.poi_info, POIList);
        ListView listView = (ListView) findViewById(R.id.poi_list_arrival);
        // Assign adapter to ListView
        listView.setAdapter(dataAdapter);

    }

    private String getSelectedRadioButton() {
        String selected = "";

        ArrayList<POI> POIList = dataAdapter.getPOIList();
        for (int i = 0; i < POIList.size(); i++) {

            POI p = POIList.get(i);
            if (p.isSelected()) {
                Log.d(TAG, "SELECTEDCHECKBOX :" + p.getName());
                selected = p.getName();
                break;
            }
        }
        return selected;
    }
}