package com.rookiedev.microwavetools.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.SubscriptSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.rookiedev.microwavetools.MainActivity;
import com.rookiedev.microwavetools.R;
import com.rookiedev.microwavetools.libs.MLIN;

import java.math.BigDecimal;

/**
 * Created by rookie on 8/11/13.
 */
public class MLINFragment extends Fragment {
    public final static String MLIN_W = "MLIN_W";
    public final static String MLIN_W_UNIT = "MLIN_W_UNIT";
    public final static String MLIN_L = "MLIN_L";
    public final static String MLIN_L_UNIT = "MLIN_L_UNIT";
    public final static String MLIN_Z0 = "MLIN_Z0";
    public final static String MLIN_Z0_UNIT = "MLIN_Z0_UNIT";
    public final static String MLIN_Eeff = "MLIN_Eeff";
    public final static String MLIN_Eeff_UNIT = "MLIN_Eeff_UNIT";
    public final static String MLIN_Freq = "MLIN_Freq";
    public final static String MLIN_Freq_UNIT = "MLIN_Freq_UNIT";
    public final static String MLIN_er = "MLIN_er";
    public final static String MLIN_H = "MLIN_H";
    public final static String MLIN_H_UNIT = "MLIN_H_UNIT";
    public final static String MLIN_T = "MLIN_T";
    public final static String MLIN_T_UNIT = "MLIN_T_UNIT";
    private View rootView;
    private SpannableString error_er, error_Z0;
    private int DecimalLength; // the length of the Decimal, accurate of the result
    private TextView text_epsilon, text_z0, text_eeff; // strings which include the subscript
    private EditText edittext_W, // the width
            edittext_L, // the length
            edittext_Z0, // the impedance
            edittext_Eeff, // the electrical length
            edittext_Freq, // the frequency
            edittext_T, // the thickness of the metal
            edittext_H, // the thickness of the dielectric
            edittext_er; // the relative dielectric constant
    private double W, L, Z0, Eeff, Freq, T, H, er;
    private Button mlin_syn,// button synthesize
            mlin_ana;// button analyze
    private Spinner spinner_W, spinner_L, spinner_T, spinner_H, spinner_Z0,
            spinner_Eeff, spinner_Freq;// the units of each parameter

    public MLINFragment() {
        // Empty constructor required for fragment subclasses
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.mlin, container, false);
        initUI();
        readSharedPref(); // read shared preferences

        mlin_ana.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int temp;
                Preference_SharedPref();
                if (!analysisInputCheck()) {
                    edittext_Z0.setText(""); // clear the Z0 and Eeff outputs
                    edittext_Eeff.setText("");
                } else {
                    W = Double.parseDouble(edittext_W.getText().toString()); // get the parameters
                    Freq = Double.parseDouble(edittext_Freq.getText().toString());
                    er = Double.parseDouble(edittext_er.getText().toString());
                    H = Double.parseDouble(edittext_H.getText().toString());
                    T = Double.parseDouble(edittext_T.getText().toString());

                    temp = spinner_W.getSelectedItemPosition(); // convert unit to mil
                    if (temp == 1) {
                        W = W * 39.37007874;
                    } else if (temp == 2) {
                        W = W * 10 * 39.37007874;
                    }

                    temp = spinner_Freq.getSelectedItemPosition(); // convert unit to GHz
                    if (temp == 0) {
                        Freq = Freq / 1000;
                    }

                    temp = spinner_H.getSelectedItemPosition(); // convert unit to mil
                    if (temp == 1) {
                        H = H * 39.37007874;
                    } else if (temp == 2) {
                        H = H * 10 * 39.37007874;
                    }

                    temp = spinner_T.getSelectedItemPosition(); // convert unit to mil
                    if (temp == 1) {
                        T = T * 39.37007874;
                    } else if (temp == 2) {
                        T = T * 10 * 39.37007874;
                    }

                    if (edittext_L.length() != 0) { // check the L input
                        L = Double.parseDouble(edittext_L.getText().toString());
                        temp = spinner_L.getSelectedItemPosition(); // convert unit to mil
                        if (temp == 1) {
                            L = L * 39.37007874;
                        } else if (temp == 2) {
                            L = L * 10 * 39.37007874;
                        }
                        MLIN mlin = new MLIN(W, L, 0, 0, Freq, er, H, T);
                        Z0 = mlin.getZ0();

                        BigDecimal Z0_temp = new BigDecimal(Z0);
                        Z0 = Z0_temp.setScale(DecimalLength,
                                BigDecimal.ROUND_HALF_UP).doubleValue();
                        edittext_Z0.setText(String.valueOf(Z0)); // cut the decimal

                        Eeff = mlin.getEeff(); // calculate the Eeff

                        BigDecimal Eeff_temp = new BigDecimal(Eeff); // cut the decimal of the Eeff
                        Eeff = Eeff_temp.setScale(DecimalLength,
                                BigDecimal.ROUND_HALF_UP).doubleValue();
                        edittext_Eeff.setText(String.valueOf(Eeff));
                    } else {
                        MLIN mlin = new MLIN(W, L, 0, 0, Freq, er, H, T);
                        Z0 = mlin.getZ0();

                        BigDecimal Z0_temp = new BigDecimal(Z0);
                        Z0 = Z0_temp.setScale(DecimalLength,
                                BigDecimal.ROUND_HALF_UP).doubleValue();
                        edittext_Z0.setText(String.valueOf(Z0)); // cut the decimal

                        edittext_Eeff.setText(""); // if the L input is empty, clear the Eeff
                    }
                }
            }
        });

        mlin_syn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int temp;
                Preference_SharedPref();
                if (!synthesizeInputCheck()) {
                    edittext_L.setText(""); // clear the L and W outputs
                    edittext_W.setText("");
                } else {
                    Z0 = Double.parseDouble(edittext_Z0.getText().toString()); // get the parameters
                    Freq = Double.parseDouble(edittext_Freq.getText().toString());
                    er = Double.parseDouble(edittext_er.getText().toString());
                    H = Double.parseDouble(edittext_H.getText().toString());
                    T = Double.parseDouble(edittext_T.getText().toString());

					/*
                     * temp = spinner_W.getSelectedItemPosition(); // mil if
					 * (temp == 1) { W = W * 39.37007874; } else if (temp == 2)
					 * { W = W * 10 * 39.37007874; }
					 */

					/*
                     * temp = spinner_L.getSelectedItemPosition(); if (temp ==
					 * 1) { L = L * 39.37007874; } else if (temp == 2) { L = L *
					 * 10 * 39.37007874; }
					 */

                    temp = spinner_Freq.getSelectedItemPosition(); // convert the unit to GHz
                    if (temp == 0) {
                        Freq = Freq / 1000;
                    }

                    temp = spinner_H.getSelectedItemPosition(); // convert the unit to mil
                    if (temp == 1) {
                        H = H * 39.37007874;
                    } else if (temp == 2) {
                        H = H * 10 * 39.37007874;
                    }

                    temp = spinner_T.getSelectedItemPosition(); // convert the unit to mil
                    if (temp == 1) {
                        T = T * 39.37007874;
                    } else if (temp == 2) {
                        T = T * 10 * 39.37007874;
                    }

                    if (edittext_Eeff.length() != 0) {
                        Eeff = Double.parseDouble(edittext_Eeff.getText().toString());
                        MLIN mlin = new MLIN(0, 0, Z0, Eeff, Freq, er, H, T);
                        W = mlin.getW();
                        mlin.setW(W);
                        L = mlin.getL();
                        temp = spinner_L.getSelectedItemPosition();
                        if (temp == 1) {
                            L = L / 39.37007874;
                        } else if (temp == 2) {
                            L = L / 10 / 39.37007874;
                        }
                        BigDecimal L_temp = new BigDecimal(L); // cut the decimal of L
                        L = L_temp.setScale(DecimalLength,
                                BigDecimal.ROUND_HALF_UP).doubleValue();
                        edittext_L.setText(String.valueOf(L));
                    } else {
                        MLIN mlin = new MLIN(0, 0, Z0, 0, Freq, er, H, T);
                        W = mlin.getW();
                        edittext_L.setText(""); // clear the L if the Eeff input is empty
                    }
                    temp = spinner_W.getSelectedItemPosition();
                    if (temp == 1) {
                        W = W / 39.37007874;
                    } else if (temp == 2) {
                        W = W / 10 / 39.37007874;
                    }

                    BigDecimal W_temp = new BigDecimal(W); // cut the decimal of W
                    W = W_temp
                            .setScale(DecimalLength, BigDecimal.ROUND_HALF_UP)
                            .doubleValue();
                    edittext_W.setText(String.valueOf(W));
                }

            }
        });

        /** Look up the AdView as a resource and load a request. */
        AdView adView = (AdView) rootView.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR) // All emulators
                .addTestDevice("015d172c791c0215") // my test device
                .addTestDevice("04afa117002e7ebc") // my test device
                .build();
        adView.loadAd(adRequest);

        return rootView;
    }

    /*
     * initialize the UI
	 */
    private void initUI() {
        error_er = new SpannableString(
                this.getString(R.string.Error_er_empty));
        error_Z0 = new SpannableString(this.getString(R.string.Error_Z0_empty));

        /** find the elements */

        // Subscript strings
        text_epsilon = (TextView) rootView.findViewById(R.id.mlin_text_er);
        text_z0 = (TextView) rootView.findViewById(R.id.mlin_text_Z0);
        text_eeff = (TextView) rootView.findViewById(R.id.mlin_text_Eeff);

        SpannableString spanEpsilon = new SpannableString(
                this.getString(R.string.text_er));
        spanEpsilon.setSpan(new SubscriptSpan(), 1, 2,
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        text_epsilon.append(spanEpsilon);

        SpannableString spanZ0 = new SpannableString(
                this.getString(R.string.text_Z0));
        spanZ0.setSpan(new SubscriptSpan(), 1, 2,
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        text_z0.append(spanZ0);

        SpannableString spanEeff = new SpannableString(
                this.getString(R.string.text_Eeff));
        spanEeff.setSpan(new SubscriptSpan(), 1, 4,
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        text_eeff.append(spanEeff);

        // show the epsilon with html style
        // text_epsilon.setText(Html.fromHtml(string_epsilon));

        // edittext elements
        edittext_W = (EditText) rootView.findViewById(R.id.mlin_editText_W);
        edittext_L = (EditText) rootView.findViewById(R.id.mlin_editText_L);
        edittext_Z0 = (EditText) rootView.findViewById(R.id.mlin_editText_Z0);
        edittext_Eeff = (EditText) rootView.findViewById(R.id.mlin_editText_Eeff);
        edittext_Freq = (EditText) rootView.findViewById(R.id.mlin_editText_Freq);
        edittext_T = (EditText) rootView.findViewById(R.id.mlin_editText_T);
        edittext_H = (EditText) rootView.findViewById(R.id.mlin_editText_H);
        edittext_er = (EditText) rootView.findViewById(R.id.mlin_editText_er);

        // button elements
        mlin_ana = (Button) rootView.findViewById(R.id.mlin_ana);
        mlin_syn = (Button) rootView.findViewById(R.id.mlin_syn);

        // spinner elements
        spinner_W = (Spinner) rootView.findViewById(R.id.mlin_spinner_W);
        spinner_L = (Spinner) rootView.findViewById(R.id.mlin_spinner_L);
        spinner_Z0 = (Spinner) rootView.findViewById(R.id.mlin_spinner_Z0);
        spinner_Eeff = (Spinner) rootView.findViewById(R.id.mlin_spinner_Eeff);
        spinner_Freq = (Spinner) rootView.findViewById(R.id.mlin_spinner_Freq);
        spinner_T = (Spinner) rootView.findViewById(R.id.mlin_spinner_T);
        spinner_H = (Spinner) rootView.findViewById(R.id.mlin_spinner_H);

        // configure the length units
        ArrayAdapter<CharSequence> adapterLength = ArrayAdapter
                .createFromResource(this.getActivity(), R.array.list_units_Length,
                        android.R.layout.simple_spinner_item);
        adapterLength
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_W.setAdapter(adapterLength);
        spinner_L.setAdapter(adapterLength);
        spinner_T.setAdapter(adapterLength);
        spinner_H.setAdapter(adapterLength);

        // configure the impedance units
        ArrayAdapter<CharSequence> adapterImpedance = ArrayAdapter
                .createFromResource(this.getActivity(), R.array.list_units_Impedance,
                        android.R.layout.simple_spinner_item);
        adapterImpedance
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_Z0.setAdapter(adapterImpedance);

        // configure the electrical length units
        ArrayAdapter<CharSequence> adapterEleLength = ArrayAdapter
                .createFromResource(this.getActivity(), R.array.list_units_Ele_length,
                        android.R.layout.simple_spinner_item);
        adapterEleLength
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_Eeff.setAdapter(adapterEleLength);

        // configure the frequency units
        ArrayAdapter<CharSequence> adapterFreq = ArrayAdapter
                .createFromResource(this.getActivity(), R.array.list_units_Frequency,
                        android.R.layout.simple_spinner_item);
        adapterFreq
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_Freq.setAdapter(adapterFreq);
    }

    private void readSharedPref() {
        SharedPreferences prefs = getActivity().getSharedPreferences(MainActivity.SHARED_PREFS_NAME,
                ActionBarActivity.MODE_PRIVATE);// get the parameters from the Shared
        // Preferences in the device

        // read values from the shared preferences

        // mlin parameters
        edittext_W.setText(prefs.getString(MLIN_W, "19.23"));
        spinner_W.setSelection(Integer.parseInt(prefs.getString(MLIN_W_UNIT,
                "0")));

        edittext_L.setText(prefs.getString(MLIN_L, "1000.00"));
        spinner_L.setSelection(Integer.parseInt(prefs.getString(MLIN_L_UNIT,
                "0")));

        edittext_Z0.setText(prefs.getString(MLIN_Z0, "50.0"));
        spinner_Z0.setSelection(Integer.parseInt(prefs.getString(
                MLIN_Z0_UNIT, "0")));

        edittext_Eeff.setText(prefs.getString(MLIN_Eeff, "52.58"));
        spinner_Eeff.setSelection(Integer.parseInt(prefs.getString(
                MLIN_Eeff_UNIT, "0")));

        edittext_Freq.setText(prefs.getString(MLIN_Freq, "1.00"));
        spinner_Freq.setSelection(Integer.parseInt(prefs.getString(
                MLIN_Freq_UNIT, "1")));

        edittext_er.setText(prefs.getString(MLIN_er, "4.00"));

        edittext_H.setText(prefs.getString(MLIN_H, "10.00"));
        spinner_H.setSelection(Integer.parseInt(prefs.getString(MLIN_H_UNIT,
                "0")));

        edittext_T.setText(prefs.getString(MLIN_T, "1.40"));
        spinner_T.setSelection(Integer.parseInt(prefs.getString(MLIN_T_UNIT,
                "0")));
    }

    private void Preference_SharedPref() {
        SharedPreferences prefs = getActivity().getSharedPreferences(MainActivity.SHARED_PREFS_NAME,
                ActionBarActivity.MODE_PRIVATE);// get the parameters from the Shared
        // Preferences in the device
        // universal parameters
        DecimalLength = Integer.parseInt(prefs.getString("DecimalLength", "2"));
    }

    @Override
    public void onStop() {
        super.onStop();

        String mlin_W, mlin_T, mlin_H, mlin_epsilon, mlin_L, mlin_Z0, mlin_Eeff, mlin_Freq, mlin_mu;
        String mlin_W_unit, mlin_L_unit, mlin_T_unit, mlin_H_unit, mlin_Z0_unit, mlin_Eeff_unit, mlin_Freq_unit;

        SharedPreferences prefs = getActivity().getSharedPreferences(MainActivity.SHARED_PREFS_NAME,
                ActionBarActivity.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        mlin_W = edittext_W.getText().toString();
        mlin_W_unit = Integer.toString(spinner_W.getSelectedItemPosition());
        mlin_L = edittext_L.getText().toString();
        mlin_L_unit = Integer.toString(spinner_L.getSelectedItemPosition());
        mlin_Z0 = edittext_Z0.getText().toString();
        mlin_Z0_unit = Integer.toString(spinner_Z0.getSelectedItemPosition());
        mlin_Eeff = edittext_Eeff.getText().toString();
        mlin_Eeff_unit = Integer.toString(spinner_Eeff
                .getSelectedItemPosition());
        mlin_Freq = edittext_Freq.getText().toString();
        mlin_Freq_unit = Integer.toString(spinner_Freq
                .getSelectedItemPosition());
        mlin_epsilon = edittext_er.getText().toString();
        mlin_H = edittext_H.getText().toString();
        mlin_H_unit = Integer.toString(spinner_H.getSelectedItemPosition());
        mlin_T = edittext_T.getText().toString();
        mlin_T_unit = Integer.toString(spinner_T.getSelectedItemPosition());

        editor.putString(MLIN_W, mlin_W);
        editor.putString(MLIN_W_UNIT, mlin_W_unit);
        editor.putString(MLIN_L, mlin_L);
        editor.putString(MLIN_L_UNIT, mlin_L_unit);
        editor.putString(MLIN_Z0, mlin_Z0);
        editor.putString(MLIN_Z0_UNIT, mlin_Z0_unit);
        editor.putString(MLIN_Eeff, mlin_Eeff);
        editor.putString(MLIN_Eeff_UNIT, mlin_Eeff_unit);
        editor.putString(MLIN_Freq, mlin_Freq);
        editor.putString(MLIN_Freq_UNIT, mlin_Freq_unit);
        editor.putString(MLIN_er, mlin_epsilon);
        editor.putString(MLIN_H, mlin_H);
        editor.putString(MLIN_H_UNIT, mlin_H_unit);
        editor.putString(MLIN_T, mlin_T);
        editor.putString(MLIN_T_UNIT, mlin_T_unit);

        editor.commit();
    }

    private boolean analysisInputCheck() {
        boolean checkResult = true;
        if (edittext_W.length() == 0) {
            edittext_W.setError(getText(R.string.Error_W_empty));
            checkResult = false;
        }
        if (edittext_Freq.length() == 0) {
            edittext_Freq.setError(getText(R.string.Error_Freq_empty));
            checkResult = false;
        }
        if (edittext_H.length() == 0) {
            edittext_H.setError(getText(R.string.Error_H_empty));
            checkResult = false;
        }
        if (edittext_T.length() == 0) {
            edittext_T.setError(getText(R.string.Error_T_empty));
            checkResult = false;
        }
        if (edittext_er.length() == 0) {
            error_er.setSpan(new SubscriptSpan(), 13, 14,
                    Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            edittext_er.setError(error_er);
            checkResult = false;
        }
        return checkResult;
    }

    private boolean synthesizeInputCheck() {
        boolean checkResult = true;
        if (edittext_Z0.length() == 0) {
            error_Z0.setSpan(new SubscriptSpan(), 13, 14,
                    Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            edittext_Z0.setError(error_Z0);
            checkResult = false;
        }
        if (edittext_Freq.length() == 0) {
            edittext_Freq.setError(getText(R.string.Error_Freq_empty));
            checkResult = false;
        }
        if (edittext_H.length() == 0) {
            edittext_H.setError(getText(R.string.Error_H_empty));
            checkResult = false;
        }
        if (edittext_T.length() == 0) {
            edittext_T.setError(getText(R.string.Error_T_empty));
            checkResult = false;
        }
        if (edittext_er.length() == 0) {
            error_er.setSpan(new SubscriptSpan(), 13, 14,
                    Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            edittext_er.setError(error_er);
            checkResult = false;
        }
        return checkResult;
    }
}
