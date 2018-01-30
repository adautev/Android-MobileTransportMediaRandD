package bg.saorsa.sts.mobileticketing;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.provider.SyncStateContract;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.EnumMap;
import java.util.Map;
import java.util.Random;

import javax.net.ssl.HttpsURLConnection;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.WHITE;

public class CurrentTransportDocumentService extends IntentService {
    public static final String ACTION_START_DOCUMENT_PROPAGATION = "bg.saorsa.sts.mobileticketing.action.START_DOCUMENT_PROPAGATION";

    // Extra parameters
    public static final String EXTRA_DOCUMENT_TYPE = "bg.saorsa.sts.mobileticketing.extra.EXTRA_DOCUMENT_TYPE";
    public static final String EXTRA_CONSUMER_ID = "bg.saorsa.sts.mobileticketing.extra.EXTRA_CONSUMER_ID";
    //Fragment communication
    public static final String BROADCAST_ACTION = "bg.saorsa.sts.mobileticketing.extra.BROADCAST_ACTION";
    public static final String TRANSPORT_DOCUMENT_RECEIVED_SUCCESSFULLY = "bg.saorsa.sts.mobileticketing.extra.TRANSPORT_DOCUMENT_RECEIVED_SUCCESSFULLY";
    public static final String UNABLE_TO_RETREIVE_TOKEN = "bg.saorsa.sts.mobileticketing.extra.UNABLE_TO_RETREIVE_TOKEN";

    public CurrentTransportDocumentService() {
        super("CurrentTransportDocumentService");
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
                } catch (WriterException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void startDocumentPropagation(String documentType, String consumerId) throws IOException, WriterException {
        String outcome = TRANSPORT_DOCUMENT_RECEIVED_SUCCESSFULLY;
        String outcomeInformation = "";
        try {
            outcomeInformation = getTransportDocumentToken(documentType, consumerId, outcomeInformation);

        } catch (Exception ex) {
            outcome = UNABLE_TO_RETREIVE_TOKEN;
            outcomeInformation = ex.getMessage();
        }
        if(outcome != UNABLE_TO_RETREIVE_TOKEN) {
            Intent localIntent =
                    new Intent(BROADCAST_ACTION)
                            // Puts the status into the Intent
                            .putExtra(UNABLE_TO_RETREIVE_TOKEN, true);
            localIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent. FLAG_INCLUDE_STOPPED_PACKAGES);
            LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
            return;
        }
        Bitmap bitmap = encodeAsBitmap(outcomeInformation,BarcodeFormat.QR_CODE, 400, 400);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        Intent localIntent =
                new Intent(BROADCAST_ACTION)
                        // Puts the status into the Intent
                        .putExtra(outcome, byteArray);
        localIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent. FLAG_INCLUDE_STOPPED_PACKAGES);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    private String getTransportDocumentToken(String documentType, String consumerId, String outcomeInformation) throws IOException {
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
        return outcomeInformation;
    }

    private Bitmap encodeAsBitmap(String contents, BarcodeFormat format, int img_width, int img_height) throws WriterException {
        String contentsToEncode = contents;
        if (contentsToEncode == null) {
            return null;
        }
        Map<EncodeHintType, Object> hints = null;
        hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.ERROR_CORRECTION, "M");
        hints.put(EncodeHintType.MARGIN, 4); /* default = 4 */
        hints.put(EncodeHintType.PDF417_COMPACT, "true"); /* default = 4 */
        MultiFormatWriter writer = new MultiFormatWriter();
        BitMatrix result;
        try {
            result = writer.encode(contentsToEncode, format, img_width, img_height, hints);
        } catch (IllegalArgumentException iae) {
            // Unsupported format
            return null;
        }

        int width = result.getWidth();
        int height = result.getHeight();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

}
