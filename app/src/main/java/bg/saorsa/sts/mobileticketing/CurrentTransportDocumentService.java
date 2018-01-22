package bg.saorsa.sts.mobileticketing;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.Handler;
import android.provider.SyncStateContract;
import android.support.v4.content.LocalBroadcastManager;

import java.nio.charset.Charset;
import java.util.Random;

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

    // TODO: Rename parameters
    public static final String DOCUMENT_TYPE_MONTHLY_CARD_FULL = "bg.saorsa.sts.mobileticketing.extra.DOCUMENT_TYPE_MONTHLY_CARD_FULL";
    public static final String EXTRA_DOCUMENT_TYPE = "bg.saorsa.sts.mobileticketing.extra.EXTRA_DOCUMENT_TYPE";
    //Fragment communication
    public static final String BROADCAST_ACTION = "bg.saorsa.sts.mobileticketing.extra.BROADCAST_ACTION";
    public static final String TRANSPORT_DOCUMENT_RECEIVED_SUCCESSFULLY = "bg.saorsa.sts.mobileticketing.extra.TRANSPORT_DOCUMENT_RECEIVED_SUCCESSFULLY";
    public static final String TRANSPORT_DOCUMENT_ALREADY_USED = "bg.saorsa.sts.mobileticketing.extra.TRANSPORT_DOCUMENT_ALREADY_USED";

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
                startDocumentPropagation(documentType);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void startDocumentPropagation(String documentType) {
        // TODO: Handle action Foo
        String transportDocumentPayload = getTransportDocument(documentType);
        Intent localIntent =
                new Intent(BROADCAST_ACTION)
                        // Puts the status into the Intent
                        .putExtra(TRANSPORT_DOCUMENT_RECEIVED_SUCCESSFULLY, transportDocumentPayload);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }
    private String getTransportDocument(String documentType) {
        byte[] array = new byte[25]; // length is bounded by 25
        new Random().nextBytes(array);
        return new String(array, Charset.forName("UTF-8"));
    }

}
