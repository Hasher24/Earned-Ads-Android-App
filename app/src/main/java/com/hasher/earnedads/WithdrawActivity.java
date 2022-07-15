package com.hasher.earnedads;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleObserver;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;

public class WithdrawActivity extends AppCompatActivity {

    AdView mAdView,mAdView2;
    FirebaseAuth fAuth;
    TextView balance,paymentDetails,withdrawPoints,paymentMethod;
    String userID;
    FirebaseFirestore fStore;
    Button withdraw;
    InterstitialAd mInterstitial;

    public void init(){
        fAuth = FirebaseAuth.getInstance();
        balance = findViewById(R.id.balanceValue);
        fStore = FirebaseFirestore.getInstance();
        withdraw = findViewById(R.id.withdraw);
        withdrawPoints = findViewById(R.id.withdrawPoints);
        paymentDetails = findViewById(R.id.paymentDetails);
        paymentMethod = findViewById(R.id.withdrawMethod);
        loadInterstitialAd();


        userID = fAuth.getCurrentUser().getUid();

        DocumentReference documentReference = fStore.collection("users").document(userID);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                balance.setText(value.getString("balance"));
            }
        });
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_withdraw);


        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        mAdView2 = findViewById(R.id.adView2);
        AdRequest adRequest2 = new AdRequest.Builder().build();
        mAdView2.loadAd(adRequest2);

        init();

        mAdView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });


        withdraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String details = paymentDetails.getText().toString();
                String points = withdrawPoints.getText().toString();
                String oBalance = balance.getText().toString();
                String method = paymentMethod.getText().toString();

                Integer oldBalance = Integer.parseInt(oBalance);


                if (details.isEmpty()){
                    paymentDetails.setError("Enter the payment details");
                }
                if (points.isEmpty()) {
                    withdrawPoints.setError("Enter the withdraw amount");
                }
                if (method.isEmpty()){
                    paymentMethod.setError("Please check the payment method");
                }
                Integer withdrawBalance = Integer.parseInt(points);
                if (Integer.parseInt(points) < 100) {
                    Toast.makeText(WithdrawActivity.this, "Withdraw must be minimum of 100 points", Toast.LENGTH_SHORT).show();
                }
                if (oldBalance < withdrawBalance) {
                    Toast.makeText(WithdrawActivity.this, "Check your balance", Toast.LENGTH_SHORT).show();
                }
                if (oldBalance >= withdrawBalance) {
                    Integer newBalance = oldBalance - withdrawBalance;
                    DocumentReference request = fStore.collection("withdrawRequests").document(userID);
                    Map<String, Object> req = new HashMap<>();
                    req.put("paymentDetails", details);
                    req.put("withdrawPoints", points);
                    req.put("withdrawMethod", method);
                    request.set(req).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(WithdrawActivity.this, "Withdraw request sent", Toast.LENGTH_SHORT).show();
                        }
                    });
                    DocumentReference documentReference = fStore.collection("users").document(userID);
                    Map<String, Object> map = new HashMap<>();
                    map.put("balance", newBalance.toString());
                    documentReference.update(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(WithdrawActivity.this, "Points Updated", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            }
        });

    }

    private void loadInterstitialAd() {
        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(this,"ca-app-pub-1793254949687292/7618578839", adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                // The mInterstitialAd reference will be null until
                // an ad is loaded.
                mInterstitial = interstitialAd;
                Log.d("TAG", "onAdLoaded");

                mInterstitial.setFullScreenContentCallback(new FullScreenContentCallback(){
                    @Override
                    public void onAdClicked() {
                        // Called when a click is recorded for an ad.
                        Log.d("TAG", "Ad was clicked.");
                    }

                    @Override
                    public void onAdDismissedFullScreenContent() {
                        // Called when ad is dismissed.
                        // Set the ad reference to null so you don't show the ad a second time.
                        Log.d("TAG", "Ad dismissed fullscreen content.");
                        mInterstitial = null;
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                        // Called when ad fails to show.
                        Log.e("TAG", "Ad failed to show fullscreen content.");
                        mInterstitial = null;
                    }

                    @Override
                    public void onAdImpression() {
                        // Called when an impression is recorded for an ad.
                        Log.d("TAG", "Ad recorded an impression.");
                    }

                    @Override
                    public void onAdShowedFullScreenContent() {
                        // Called when ad is shown.
                        Log.d("TAG", "Ad showed fullscreen content.");
                    }
                });
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                // Handle the error
                Log.d("TAG", loadAdError.toString());
                mInterstitial = null;
            }
        });
    }

    private void showInterstitialAd() {
        if (mInterstitial != null) {
            mInterstitial.show(WithdrawActivity.this);
        } else {
            Log.d("TAG", "The interstitial ad wasn't ready yet.");
        }
    }



    @Override
    public void onBackPressed() {
        super.onBackPressed();
        showInterstitialAd();
    }
}