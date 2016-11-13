package com.rookiedev.microwavetools.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.rookiedev.microwavetools.R;


public class AdFragment extends Fragment {
    public AdFragment()
    {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View adView=inflater.inflate(R.layout.banner_layout, container, false);

        AdView banner = (AdView) adView.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR) // All emulators
                .addTestDevice("015d172c791c0215") // my test device
                .addTestDevice("04afa117002e7ebc") // my test device
                .build();
        banner.loadAd(adRequest);

        return adView;
    };
}
