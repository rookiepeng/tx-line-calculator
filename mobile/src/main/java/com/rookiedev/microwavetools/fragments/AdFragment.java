package com.rookiedev.microwavetools.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.rookiedev.microwavetools.R;

public class AdFragment extends Fragment {
    private AdView banner;
    public AdFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View adView = inflater.inflate(R.layout.fragment_ad_banner, container, false);

        banner = (AdView) adView.findViewById(R.id.adView);
        banner.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                banner.setVisibility(AdView.VISIBLE);
                // Code to be executed when an ad finishes loading.
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                // Code to be executed when an ad request fails.
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }

            @Override
            public void onAdLeftApplication() {
                // Code to be executed when the user has left the app.
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when when the user is about to return
                // to the app after tapping on an ad.
            }
        });
        AdRequest adRequest = new AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR) // All emulators
                .addTestDevice("2D302053F176402A9C59EB67575E6977")
                .build();
        banner.loadAd(adRequest);

        return adView;
    }
}
