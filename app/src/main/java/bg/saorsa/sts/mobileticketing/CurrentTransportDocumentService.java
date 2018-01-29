package bg.saorsa.sts.mobileticketing;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.Handler;
import android.provider.SyncStateContract;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Random;

import javax.net.ssl.HttpsURLConnection;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class CurrentTransportDocumentService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    public static final String ACTION_START_DOCUMENT_PROPAGATION = "bg.saorsa.sts.mobileticketing.action.START_DOCUMENT_PROPAGATION";

    // Extra parameters
    public static final String DOCUMENT_TYPE_MONTHLY_CARD_FULL = "bg.saorsa.sts.mobileticketing.extra.DOCUMENT_TYPE_MONTHLY_CARD_FULL";
    public static final String EXTRA_DOCUMENT_TYPE = "bg.saorsa.sts.mobileticketing.extra.EXTRA_DOCUMENT_TYPE";
    public static final String EXTRA_CONSUMER_ID = "bg.saorsa.sts.mobileticketing.extra.EXTRA_CONSUMER_ID";
    //Fragment communication
    public static final String BROADCAST_ACTION = "bg.saorsa.sts.mobileticketing.extra.BROADCAST_ACTION";
    public static final String TRANSPORT_DOCUMENT_RECEIVED_SUCCESSFULLY = "bg.saorsa.sts.mobileticketing.extra.TRANSPORT_DOCUMENT_RECEIVED_SUCCESSFULLY";
    public static final String TRANSPORT_DOCUMENT_ALREADY_USED = "bg.saorsa.sts.mobileticketing.extra.TRANSPORT_DOCUMENT_ALREADY_USED";
    public static final String UNABLE_TO_RETREIVE_TOKEN = "bg.saorsa.sts.mobileticketing.extra.UNABLE_TO_RETREIVE_TOKEN";

    public CurrentTransportDocumentService() {
        super("CurrentTransportDocumentService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startTransportDocumentPropagation(Context context, String transportDocumentType) {
        Intent intent = new Intent(context, CurrentTransportDocumentService.class);
        intent.setAction(ACTION_START_DOCUMENT_PROPAGATION);
        intent.putExtra(DOCUMENT_TYPE_MONTHLY_CARD_FULL, transportDocumentType);
        context.startService(intent);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_START_DOCUMENT_PROPAGATION.equals(action)) {
                final String documentType = intent.getStringExtra(EXTRA_DOCUMENT_TYPE);
                final String consumerId = intent.getStringExtra(EXTRA_CONSUMER_ID);
                try {
                    startDocumentPropagation(documentType, consumerId);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void startDocumentPropagation(String documentType, String consumerId) throws IOException {
        String outcome = TRANSPORT_DOCUMENT_RECEIVED_SUCCESSFULLY;
        String outcomeInformation = "";
        try {
            URL tokenEndpoint = new URL("http://cloud.dialogical.bg:8084/token/"+ documentType + "/" + consumerId);
            // Create connection
            HttpURLConnection urlConnection =
                    (HttpURLConnection) tokenEndpoint.openConnection();
                urlConnection.setReadTimeout(5000);
                urlConnection.setRequestMethod("GET");
            try {
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                StringBuilder result = new StringBuilder();
                String line;
                while((line = reader.readLine()) != null) {
                    result.append(line);
                }
                outcomeInformation = result.toString();
                
            } catch (Exception ex) {
                String message = ex.getMessage();
                message = "ds";
            }
            finally {
                    urlConnection.disconnect();
            }

        } catch (Exception ex) {
            outcome = UNABLE_TO_RETREIVE_TOKEN;
            outcomeInformation = ex.getMessage();
        }
        if(outcome != UNABLE_TO_RETREIVE_TOKEN) {
            String transportDocumentPayload = getTransportDocument(documentType);
        }
        Intent localIntent =
                new Intent(BROADCAST_ACTION)
                        // Puts the status into the Intent
                        .putExtra(outcome, outcomeInformation);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }
    @NonNull
    private String getTransportDocument(String documentType) {
        byte[] array = new byte[25]; // length is bounded by 25
        new Random().nextBytes(array);
        return new String(array, Charset.forName("UTF-8"));
    }

}
