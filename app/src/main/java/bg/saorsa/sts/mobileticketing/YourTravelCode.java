package bg.saorsa.sts.mobileticketing;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.qrcode.encoder.ByteMatrix;
import com.google.zxing.qrcode.encoder.Encoder;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.encoder.QRCode;

import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.Map;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.WHITE;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragfment must implement the
 * {@link onTravelCodeUpdateListener} interface
 * to handle interaction events.
 * Use the {@link YourTravelCode#newInstance} factory method to
 * create an instance of this fragment.
 */
public class YourTravelCode extends Fragment {
    private onTravelCodeUpdateListener onTravelCodeUpdateListener;

    public YourTravelCode() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment YourTravelCode.
     */
    // TODO: Rename and change types and number of parameters
    public static YourTravelCode newInstance() {
        YourTravelCode fragment = new YourTravelCode();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }
    public void updateQRCode(String transportDocument) throws WriterException {
        Bitmap bitmap = encodeAsBitmap(transportDocument,BarcodeFormat.QR_CODE, 400, 400);
        ImageView aztecCodeView = (ImageView) getView().findViewById(R.id.iv_Aztec_Code);
        aztecCodeView.setImageBitmap(bitmap);
    }
    private Bitmap encodeAsBitmap(String contents, BarcodeFormat format, int img_width, int img_height) throws WriterException {
        String contentsToEncode = contents;
        if (contentsToEncode == null) {
            return null;
        }
        Map<EncodeHintType, Object> hints = null;
        hints = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 0); /* default = 4 */
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
                pixels[offset + x] = result.get(x, y) ? WHITE : BLACK;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent currentTransportDocumentService = new Intent(getActivity(), CurrentTransportDocumentService.class);
        currentTransportDocumentService.putExtra(CurrentTransportDocumentService.EXTRA_DOCUMENT_TYPE, CurrentTransportDocumentService.DOCUMENT_TYPE_MONTHLY_CARD_FULL);
        currentTransportDocumentService.setAction(CurrentTransportDocumentService.ACTION_START_DOCUMENT_PROPAGATION);

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                new BroadcastReceiver() {
                  @Override
                  public void onReceive(final Context context, final Intent intent) {
                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    updateQRCode(intent.getStringExtra(CurrentTransportDocumentService.TRANSPORT_DOCUMENT_RECEIVED_SUCCESSFULLY));
                                } catch (WriterException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                  }
              },
                new IntentFilter(CurrentTransportDocumentService.BROADCAST_ACTION));
        getActivity().startService(currentTransportDocumentService);


    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_your_travel_code, container, false);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof onTravelCodeUpdateListener) {
            onTravelCodeUpdateListener = (onTravelCodeUpdateListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement onTravelCodeUpdateListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onTravelCodeUpdateListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface onTravelCodeUpdateListener {
        // TODO: Update argument type and name
        void onTravelCodeUpdate(Uri uri);
    }
}
