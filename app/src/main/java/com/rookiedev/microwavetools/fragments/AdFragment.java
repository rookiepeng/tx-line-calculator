package com.rookiedev.microwavetools.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.rookiedev.microwavetools.R;

public class AdFragment extends Fragment {
    public AdFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View adView = inflater.inflate(R.layout.fragment_ad_banner, container, false);

        AdView banner = (AdView) adView.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR) // All emulators
                .build();
        banner.loadAd(adRequest);

        return adView;
    }
}
