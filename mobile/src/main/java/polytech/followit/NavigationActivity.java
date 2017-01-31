package polytech.followit;

import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import polytech.followit.model.Instruction;
import polytech.followit.model.Node;
import polytech.followit.model.POI;
import polytech.followit.rest.SocketCallBack;
import polytech.followit.service.BeaconMonitoringService;
import polytech.followit.utility.PathSingleton;

public class NavigationActivity extends AppCompatActivity implements
        View.OnClickListener,
        SocketCallBack {

    private static final String TAG = NavigationActivity.class.getSimpleName();

    //recupere la liste en RAW de la navigation
    ArrayList<Node> listNavigation = null;

    //liste des etapes en Instruction
    ArrayList<Instruction> navigationSteps = null;

    //index position sur quelle étape on se trouve
    int index = 0;

    //Instruction en cours
    Instruction ongoingInstruction;

    //==============================================================================================
    // Lifecycle
    //==============================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "ON create - Navigation Activity");
        setContentView(R.layout.navigation_activity);

        PathSingleton.getInstance().setSocketCallBack(this);
        prepareNavigationList();

        //button listeners
        Button b = (Button) findViewById(R.id.previous_button);
        b.setOnClickListener(this);
        b = (Button) findViewById(R.id.next_button);
        b.setOnClickListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    //==============================================================================================
    // Listeners implementation
    //==============================================================================================

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.previous_button:
                if (index > 0) {
                    index--;
                    TextView t = (TextView) findViewById(R.id.instructions_textView);
                    t.setText(navigationSteps.get(index).instruction);
                    ongoingInstruction = navigationSteps.get(index);
                }
                break;
            case R.id.next_button:
                if (index < navigationSteps.size() - 1) {
                    index++;
                    TextView t = (TextView) findViewById(R.id.instructions_textView);
                    t.setText(navigationSteps.get(index).instruction);
                    ongoingInstruction = navigationSteps.get(index);
                }
                break;
        }
    }


    @Override
    public void onPathFetched() throws JSONException {
        Log.d(TAG, "ON PATH FETCHED NavigationActivity");

        syncDataWithService();
        prepareNavigationList();
    }


    @Override
    public void onBroadcastNotification(String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(NavigationActivity.this)
                        .setSmallIcon(R.drawable.ic_stat_name)
                        .setContentTitle("Mise `a jour de la carte")
                        .setContentText("La carte a ete mise a jour, nous avons synchronise votre chemin");
                NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                // notificationID allows you to update the notification later on.
                mNotificationManager.notify(1, mBuilder.build());

                //TODO: ADD action on notification click
                //ON redemande le chemin
                //on cherche l'étape active
                Log.d(TAG, "dEBUT DEMANDE DE CHEMIN");

                Log.d(TAG, "source: " + ongoingInstruction.nodeToGoTo + " dest " + PathSingleton.getInstance().getPath().getDestination());
                JSONObject o = new JSONObject();
                try {
                    o.put("source", ongoingInstruction.nodeToGoTo);
                    o.put("destination", PathSingleton.getInstance().getPath().getDestination());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                PathSingleton.getInstance().askForPath(o);
            }
        });
    }

    @Override
    public void onPOIListFetched() {
    }

    //==============================================================================================
    // List view implementation
    //==============================================================================================

    private void prepareNavigationList() {
        //ON PREPARE LA LISTE DES ETAPES
        navigationSteps = new ArrayList<>();
        listNavigation = PathSingleton.getInstance().getPath().getListNodes();
        Log.d(TAG, "PREPARENAVIGATIONLIST");

        for (int i = 0; i < listNavigation.size(); i++) {

            Node n = listNavigation.get(i);
            String text = "Instructions\n";
            //si il y a des instructions
            if (!"".equals(n.getInstruction())) {
                text += n.getInstruction().getInstruction() + "\n";
            }
            //si il y a des POI
            if (n.getPoi() != null && !n.getPoi().isEmpty()) {
                text += "Points d'interets à proximité: \n";
                for (POI s : n.getPoi()) {
                    text += "\t- " + s.getName();
                }
            }
            //SI on est pas arrivé a la fin du tableau, on rentre le noeud/beacon ou on va arriver
            if (i < listNavigation.size() - 1) {
                Node nplusun = listNavigation.get(i + 1);
                navigationSteps.add(new Instruction(n.getName(), nplusun.getName(), text));
            } else {
                //sinon juste le noeud/beacon de depart
                navigationSteps.add(new Instruction(null, n.getName(), text));
            }
        }
        //On affiche la 1ere etape
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                index = 0;
                TextView t = (TextView) findViewById(R.id.instructions_textView);
                t.setText(navigationSteps.get(index).instruction);
                ongoingInstruction = navigationSteps.get(index);
            }
        });
    }

    //==============================================================================================
    // Utils
    //==============================================================================================

    private void  syncDataWithService() {
        Intent serviceIntent = new Intent(this,BeaconMonitoringService.class);
        serviceIntent.putExtra("path", PathSingleton.getInstance().getPath());
        startService(serviceIntent);
    }
}
