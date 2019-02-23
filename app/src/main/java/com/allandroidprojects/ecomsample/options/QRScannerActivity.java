package com.allandroidprojects.ecomsample.options;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.allandroidprojects.ecomsample.R;
import com.allandroidprojects.ecomsample.product.ItemDetailsActivity;
import com.allandroidprojects.ecomsample.startup.MainActivity;
import com.allandroidprojects.ecomsample.utility.PrefManager;
import com.allandroidprojects.ecomsample.utility.RetrofitClient;
import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.allandroidprojects.ecomsample.fragments.ImageListFragment.ITEM_ID;

public class QRScannerActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {
    private LinearLayout linearLayout;
    private ZXingScannerView mScannerView;
    private PrefManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrscanner);
        prefManager = new PrefManager(QRScannerActivity.this);

        linearLayout = (LinearLayout) findViewById(R.id.qrScannerLayout);
        linearLayout.setVisibility(View.VISIBLE);

        mScannerView = new ZXingScannerView(this);
        linearLayout.addView(mScannerView);
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.startCamera();
        mScannerView.setResultHandler(this);

    }

    @Override
    public void handleResult(Result result) {
        mScannerView.stopCamera();
        String uid = result.getText();
        if (uid != null) {
            if (getIntent().getStringExtra("from").equals(SearchResultActivity.class.toString())) {
                Intent intent = new Intent(this, ItemDetailsActivity.class);
                intent.putExtra(ITEM_ID, uid);
                startActivity(intent);
                linearLayout.removeView(mScannerView);
                linearLayout.setVisibility(View.GONE);
                finish();
            } else if (getIntent().getStringExtra("from").equals(MainActivity.class.toString())) {
                Call<ResponseBody> call = RetrofitClient
                        .getInstance()
                        .getApi()
                        .authenticateQR(Integer.parseInt(uid));

                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.code() == 201) {
                            prefManager.setAuthenticate(true);
                            Toast.makeText(QRScannerActivity.this, "Welcome to ShopMart!", Toast.LENGTH_LONG).show();
                        } else if (response.code() == 400) {
                            Toast.makeText(QRScannerActivity.this, "Please Try Again!", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(QRScannerActivity.this, "QR not Scanned!", Toast.LENGTH_LONG).show();
                        }
                        linearLayout.removeView(mScannerView);
                        linearLayout.setVisibility(View.GONE);
                        finish();
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Toast.makeText(QRScannerActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            }
        }
        else{
            Toast.makeText(QRScannerActivity.this,"QR not Scanned",Toast.LENGTH_LONG).show();
        }
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
//        if (result != null) {
//            //if qrcode has nothing in it
//            if (result.getContents() == null) {
//                Toast.makeText(this, "Result Not Found", Toast.LENGTH_LONG).show();
//            } else {
//                //if qr contains data
//                //converting the data to json
////                System.out.println(result.getContents().getClass().getSimpleName());
////                try {
////                    JSONObject obj = new JSONObject(result.getContents());
////                } catch (JSONException e) {
////                    e.printStackTrace();
////                }
//                //setting values to textviews
////                    uid.setText(obj.getString("name"));
//                Call<ResponseBody> call = RetrofitClient
//                        .getInstance()
//                        .getApi()
//                        .authenticateQR(Integer.parseInt(result.getContents()));
//
//                call.enqueue(new Callback<ResponseBody>() {
//                    @Override
//                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//                        if (response.code() == 201) {
//                            prefManager.setAuthenticate(true);
//                            Toast.makeText(QRScannerActivity.this, "Welcome to ShopMart!", Toast.LENGTH_LONG).show();
//                        } else if (response.code() == 400) {
//                            Toast.makeText(QRScannerActivity.this, "Please Try Again!", Toast.LENGTH_LONG).show();
//                        } else {
//                            Toast.makeText(QRScannerActivity.this, "Hello!", Toast.LENGTH_LONG).show();
//
//                        }
//                        finish();
//                    }
//
//                    @Override
//                    public void onFailure(Call<ResponseBody> call, Throwable t) {
//                        Toast.makeText(QRScannerActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
//                    }
//                });
//            }
//        } else {
//            super.onActivityResult(requestCode, resultCode, data);
//        }
//    }


}
