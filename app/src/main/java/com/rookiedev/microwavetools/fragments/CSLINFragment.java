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
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.rookiedev.microwavetools.MainActivity;
import com.rookiedev.microwavetools.R;
import com.rookiedev.microwavetools.libs.CSLIN;

import java.math.BigDecimal;

/**
 * Created by rookie on 8/11/13.
 */
public class CSLINFragment extends Fragment {
    public final static String CSLIN_W = "CSLIN_W";
    public final static String CSLIN_W_UNIT = "CSLIN_W_UNIT";
    public final static String CSLIN_S = "CSLIN_S";
    public final static String CSLIN_S_UNIT = "CSLIN_S_UNIT";
    public final static String CSLIN_L = "CSLIN_L";
    public final static String CSLIN_L_UNIT = "CSLIN_L_UNIT";
    public final static String CSLIN_Z0 = "CSLIN_Z0";
    public final static String CSLIN_Z0_UNIT = "CSLIN_Z0_UNIT";
    public final static String CSLIN_k = "CSLIN_k";
    public final static String CSLIN_Z0o = "CSLIN_Z0o";
    public final static String CSLIN_Z0o_UNIT = "CSLIN_Z0o_UNIT";
    public final static String CSLIN_Z0e = "CSLIN_Z0e";
    public final static String CSLIN_Z0e_UNIT = "CSLIN_Z0e_UNIT";
    public final static String CSLIN_Eeff = "CSLIN_Eeff";
    public final static String CSLIN_Eeff_UNIT = "CSLIN_Eeff_UNIT";
    public final static String CSLIN_Freq = "CSLIN_Freq";
    public final static String CSLIN_Freq_UNIT = "CSLIN_Freq_UNIT";
    public final static String CSLIN_er = "CSLIN_er";
    public final static String CSLIN_H = "CSLIN_H";
    public final static String CSLIN_H_UNIT = "CSLIN_H_UNIT";
    public final static String CSLIN_T = "CSLIN_T";
    public final static String CSLIN_T_UNIT = "CSLIN_T_UNIT";
    public final static String CSLIN_USEZ0k = "CSLIN_USEZ0k";
    private View rootView;
    private int DecimalLength; // the length of the Decimal,
    // accurate of the result
    private SpannableString error_er, error_Z0, error_Z0e, error_Z0o;
    private TextView text_er, text_Z0, text_Z0o, text_Z0e, text_Eeff; // strings which include the subscript
    private EditText edittext_W, // the width
            edittext_S, //
            edittext_L, // the length
            edittext_Z0, // the impedance
            edittext_k, edittext_Z0o, //
            edittext_Z0e, //
            edittext_Eeff, // the electrical length
            edittext_Freq, // the frequency
            edittext_T, // the thickness of the metal
            edittext_H, // the thickness of the dielectric
            edittext_er; // the relative dielectric constant
    private double W, L, S, Z0, k, Z0o, Z0e, Eeff, Freq, T, H, er;
    private Button cslin_syn,// button synthesize
            cslin_ana;// button analyze
    private View.OnClickListener listener_ana = null;
    private View.OnClickListener listener_syn = null;
    private Spinner spinner_W, spinner_S, spinner_L, spinner_T, spinner_H,
            spinner_Z0, spinner_Z0o, spinner_Z0e, spinner_Eeff, spinner_Freq;// the units of each parameter
    private RadioButton radioBtn1, radioBtn2;
    private boolean use_z0k;

    public CSLINFragment() {
        // Empty constructor required for fragment subclasses
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.cslin, container, false);

        initUI(); // initial the UI
        readSharedPref(); // read shared preferences
        setRadioBtn();

        cslin_ana.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int temp;
                Preference_SharedPref();
                if (!analysisInputCheck()) {
                    edittext_Z0.setText(""); // clear the Z0 and Eeff outputs
                    edittext_Eeff.setText("");
                    edittext_Z0o.setText(""); //
                    edittext_Z0e.setText(""); //
                    edittext_k.setText("");
                } else {
                    W = Double.parseDouble(edittext_W.getText().toString()); // get the parameters
                    S = Double.parseDouble(edittext_S.getText().toString());
                    Freq = Double.parseDouble(edittext_Freq.getText().toString());
                    er = Double.parseDouble(edittext_er.getText().toString());
                    H = Double.parseDouble(edittext_H.getText().toString());
                    T = Double.parseDouble(edittext_T.getText().toString());

                    temp = spinner_W.getSelectedItemPosition(); // convert unit to meter
                    if (temp == 0) {
                        W = W / 39370.0787402;
                    } else if (temp == 1) {
                        W = W / 1000;
                    } else if (temp == 2) {
                        W = W / 100;
                    }

                    temp = spinner_S.getSelectedItemPosition(); // convert unit to meter
                    if (temp == 0) {
                        S = S / 39370.0787402;
                    } else if (temp == 1) {
                        S = S / 1000;
                    } else if (temp == 2) {
                        S = S / 100;
                    }

                    temp = spinner_Freq.getSelectedItemPosition(); // convert unit to Hz
                    if (temp == 0) {
                        Freq = Freq * 1e6;
                    } else if (temp == 1) {
                        Freq = Freq * 1e9;
                    }

                    temp = spinner_H.getSelectedItemPosition(); // convert unit to meter
                    if (temp == 0) {
                        H = H / 39370.0787402;
                    } else if (temp == 1) {
                        H = H / 1000;
                    } else if (temp == 2) {
                        H = H / 100;
                    }

                    temp = spinner_T.getSelectedItemPosition(); // convert unit to meter
                    if (temp == 0) {
                        T = T / 39370.0787402;
                    } else if (temp == 1) {
                        T = T / 1000;
                    } else if (temp == 2) {
                        T = T / 100;
                    }
                    if (edittext_L.length() != 0) {
                        L = Double.parseDouble(edittext_L.getText().toString());
                        temp = spinner_L.getSelectedItemPosition(); // convert unit to meter
                        if (temp == 0) {
                            L = L / 39370.0787402;
                        } else if (temp == 1) {
                            L = L / 1000;
                        } else if (temp == 2) {
                            L = L / 100;
                        }
                        CSLIN cslin = new CSLIN(W, S, L, 0, 0, 0, 0, 0, Freq, er, H, T, use_z0k);
                        Z0o = cslin.getZ0o();
                        Z0e = cslin.getZ0e();
                        Z0 = Math.sqrt(Z0o * Z0e); // calculate the Z0
                        k = (Z0e - Z0o) / (Z0e + Z0o);
                        Eeff = cslin.getEeff();// calculate the Eeff

                        BigDecimal Eeff_temp = new BigDecimal(Eeff); // cut the decimal of the Eeff
                        Eeff = Eeff_temp.setScale(DecimalLength,
                                BigDecimal.ROUND_HALF_UP).doubleValue();
                        edittext_Eeff.setText(String.valueOf(Eeff));

                    } else {
                        CSLIN cslin = new CSLIN(W, S, 0, 0, 0, 0, 0, 0, Freq, er, H, T, use_z0k);
                        Z0o = cslin.getZ0o();
                        Z0e = cslin.getZ0e();
                        Z0 = Math.sqrt(Z0o * Z0e); // calculate the Z0
                        k = (Z0e - Z0o) / (Z0e + Z0o);
                        edittext_Eeff.setText(""); // if the L input is empty, clear the Eeff
                    }

                    BigDecimal Z0_temp = new BigDecimal(Z0);
                    Z0 = Z0_temp.setScale(DecimalLength,
                            BigDecimal.ROUND_HALF_UP).doubleValue();
                    edittext_Z0.setText(String.valueOf(Z0)); // cut the decimal
                    // of the Z0
                    BigDecimal k_temp = new BigDecimal(k);
                    k = k_temp
                            .setScale(DecimalLength, BigDecimal.ROUND_HALF_UP)
                            .doubleValue();
                    edittext_k.setText(String.valueOf(k));

                    BigDecimal Z0o_temp = new BigDecimal(Z0o);
                    Z0o = Z0o_temp.setScale(DecimalLength,
                            BigDecimal.ROUND_HALF_UP).doubleValue();
                    edittext_Z0o.setText(String.valueOf(Z0o));

                    BigDecimal Z0e_temp = new BigDecimal(Z0e);
                    Z0e = Z0e_temp.setScale(DecimalLength,
                            BigDecimal.ROUND_HALF_UP).doubleValue();
                    edittext_Z0e.setText(String.valueOf(Z0e));
                }
            }
        });

        cslin_syn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Preference_SharedPref();
                if (!synthesizeInputCheck()) {
                    edittext_L.setText(""); // clear the L and W outputs
                    edittext_S.setText("");
                    edittext_W.setText("");
                } else {
                    synthesizeButton();
                    if (use_z0k) {
                        Z0o = Z0 * Math.sqrt((1.0 - k) / (1.0 + k));
                        Z0e = Z0 * Math.sqrt((1.0 + k) / (1.0 - k));

                        BigDecimal Z0o_temp = new BigDecimal(Z0o);
                        Z0o = Z0o_temp.setScale(DecimalLength,
                                BigDecimal.ROUND_HALF_UP).doubleValue();
                        edittext_Z0o.setText(String.valueOf(Z0o));

                        BigDecimal Z0e_temp = new BigDecimal(Z0e);
                        Z0e = Z0e_temp.setScale(DecimalLength,
                                BigDecimal.ROUND_HALF_UP).doubleValue();
                        edittext_Z0e.setText(String.valueOf(Z0e));
                    } else {
                        Z0 = Math.sqrt(Z0e * Z0o);
                        k = (Z0e - Z0o) / (Z0e + Z0o);

                        BigDecimal Z0_temp = new BigDecimal(Z0);
                        Z0 = Z0_temp.setScale(DecimalLength,
                                BigDecimal.ROUND_HALF_UP).doubleValue();
                        edittext_Z0.setText(String.valueOf(Z0)); // cut the decimal of the Z0
                        BigDecimal k_temp = new BigDecimal(k);
                        k = k_temp.setScale(DecimalLength,
                                BigDecimal.ROUND_HALF_UP).doubleValue();
                        edittext_k.setText(String.valueOf(k));
                    }
                }
            }
        });

        return rootView;
    }

    private void initUI() {
        error_er = new SpannableString(this.getString(R.string.Error_er_empty));
        error_Z0 = new SpannableString(this.getString(R.string.Error_Z0_empty));
        error_Z0e = new SpannableString(this.getString(R.string.Error_Z0e_empty));
        error_Z0o = new SpannableString(this.getString(R.string.Error_Z0o_empty));
        /** find the elements */

        // Subscript strings
        text_er = (TextView) rootView.findViewById(R.id.cslin_text_er);
        text_Z0 = (TextView) rootView.findViewById(R.id.cslin_text_Z0);
        text_Eeff = (TextView) rootView.findViewById(R.id.cslin_text_Eeff);
        text_Z0o = (TextView) rootView.findViewById(R.id.cslin_text_Z0o);
        text_Z0e = (TextView) rootView.findViewById(R.id.cslin_text_Z0e);

        SpannableString spanEr = new SpannableString(
                this.getString(R.string.text_er));
        spanEr.setSpan(new SubscriptSpan(), 1, 2,
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        text_er.append(spanEr);

        SpannableString spanZ0 = new SpannableString(
                this.getString(R.string.text_Z0));
        spanZ0.setSpan(new SubscriptSpan(), 1, 2,
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        text_Z0.append(spanZ0);

        SpannableString spanZ0o = new SpannableString(
                this.getString(R.string.text_Z0o));
        spanZ0o.setSpan(new SubscriptSpan(), 1, 3,
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        text_Z0o.append(spanZ0o);

        SpannableString spanZ0e = new SpannableString(
                this.getString(R.string.text_Z0e));
        spanZ0e.setSpan(new SubscriptSpan(), 1, 3,
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        text_Z0e.append(spanZ0e);

        SpannableString spanEeff = new SpannableString(
                this.getString(R.string.text_Eeff));
        spanEeff.setSpan(new SubscriptSpan(), 1, 4,
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        text_Eeff.append(spanEeff);

        // edittext elements
        edittext_W = (EditText) rootView.findViewById(R.id.cslin_editText_W);
        edittext_S = (EditText) rootView.findViewById(R.id.cslin_editText_S);
        edittext_L = (EditText) rootView.findViewById(R.id.cslin_editText_L);
        edittext_Z0 = (EditText) rootView.findViewById(R.id.cslin_editText_Z0);
        edittext_k = (EditText) rootView.findViewById(R.id.cslin_editText_k);
        edittext_Z0o = (EditText) rootView.findViewById(R.id.cslin_editText_Z0o);
        edittext_Z0e = (EditText) rootView.findViewById(R.id.cslin_editText_Z0e);
        edittext_Eeff = (EditText) rootView.findViewById(R.id.cslin_editText_Eeff);
        edittext_Freq = (EditText) rootView.findViewById(R.id.cslin_editText_Freq);
        edittext_T = (EditText) rootView.findViewById(R.id.cslin_editText_T);
        edittext_H = (EditText) rootView.findViewById(R.id.cslin_editText_H);
        edittext_er = (EditText) rootView.findViewById(R.id.cslin_editText_er);

        // button elements
        cslin_ana = (Button) rootView.findViewById(R.id.cslin_ana);
        cslin_syn = (Button) rootView.findViewById(R.id.cslin_syn);

        // spinner elements
        spinner_W = (Spinner) rootView.findViewById(R.id.cslin_spinner_W);
        spinner_S = (Spinner) rootView.findViewById(R.id.cslin_spinner_S);
        spinner_L = (Spinner) rootView.findViewById(R.id.cslin_spinner_L);
        spinner_Z0 = (Spinner) rootView.findViewById(R.id.cslin_spinner_Z0);
        spinner_Z0o = (Spinner) rootView.findViewById(R.id.cslin_spinner_Z0o);
        spinner_Z0e = (Spinner) rootView.findViewById(R.id.cslin_spinner_Z0e);
        spinner_Eeff = (Spinner) rootView.findViewById(R.id.cslin_spinner_Eeff);
        spinner_Freq = (Spinner) rootView.findViewById(R.id.cslin_spinner_Freq);
        spinner_T = (Spinner) rootView.findViewById(R.id.cslin_spinner_T);
        spinner_H = (Spinner) rootView.findViewById(R.id.cslin_spinner_H);

        // configure the length units
        ArrayAdapter<CharSequence> adapterLength = ArrayAdapter
                .createFromResource(this.getActivity(), R.array.list_units_Length,
                        android.R.layout.simple_spinner_item);
        adapterLength
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_W.setAdapter(adapterLength);
        spinner_S.setAdapter(adapterLength);
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
        spinner_Z0o.setAdapter(adapterImpedance);
        spinner_Z0e.setAdapter(adapterImpedance);

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
        radioBtn1 = (RadioButton) rootView.findViewById(R.id.radioBtn1);
        radioBtn2 = (RadioButton) rootView.findViewById(R.id.radioBtn2);
    }

    private void setRadioBtn() {
        radioBtn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                radioBtn1.setChecked(true);
                radioBtn2.setChecked(false);
                use_z0k = true;
                edittext_Z0.setEnabled(true);
                edittext_k.setEnabled(true);
                edittext_Z0o.setEnabled(false);
                edittext_Z0e.setEnabled(false);
            }
        });
        radioBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                radioBtn2.setChecked(true);
                radioBtn1.setChecked(false);
                use_z0k = false;
                edittext_Z0.setEnabled(false);
                edittext_k.setEnabled(false);
                edittext_Z0o.setEnabled(true);
                edittext_Z0e.setEnabled(true);
            }
        });
        if (use_z0k) {
            radioBtn1.setChecked(true);
            radioBtn2.setChecked(false);
            edittext_Z0.setEnabled(true);
            edittext_k.setEnabled(true);
            edittext_Z0o.setEnabled(false);
            edittext_Z0e.setEnabled(false);
        } else {
            radioBtn1.setChecked(false);
            radioBtn2.setChecked(true);
            edittext_Z0.setEnabled(false);
            edittext_k.setEnabled(false);
            edittext_Z0o.setEnabled(true);
            edittext_Z0e.setEnabled(true);
        }
    }

    private void readSharedPref() {
        SharedPreferences prefs = getActivity().getSharedPreferences(MainActivity.SHARED_PREFS_NAME,
                ActionBarActivity.MODE_PRIVATE);// get the parameters from the Shared
        // Preferences in the device

        // read values from the shared preferences

        // CSLIN parameters
        edittext_W.setText(prefs.getString(CSLIN_W, "50.00"));
        spinner_W.setSelection(Integer.parseInt(prefs.getString(CSLIN_W_UNIT,
                "0")));

        edittext_S.setText(prefs.getString(CSLIN_S, "20.00"));
        spinner_S.setSelection(Integer.parseInt(prefs.getString(CSLIN_S_UNIT,
                "0")));

        edittext_L.setText(prefs.getString(CSLIN_L, "1000.00"));
        spinner_L.setSelection(Integer.parseInt(prefs.getString(CSLIN_L_UNIT,
                "0")));

        edittext_Z0.setText(prefs.getString(CSLIN_Z0, "33.42"));
        spinner_Z0.setSelection(Integer.parseInt(prefs.getString(
                CSLIN_Z0_UNIT, "0")));

        edittext_k.setText(prefs.getString(CSLIN_k, "0.10"));

        edittext_Z0o.setText(prefs.getString(CSLIN_Z0o, "30.35"));
        spinner_Z0o.setSelection(Integer.parseInt(prefs.getString(
                CSLIN_Z0o_UNIT, "0")));

        edittext_Z0e.setText(prefs.getString(CSLIN_Z0e, "36.69"));
        spinner_Z0e.setSelection(Integer.parseInt(prefs.getString(
                CSLIN_Z0e_UNIT, "0")));

        edittext_Eeff.setText(prefs.getString(CSLIN_Eeff, "61.00"));
        spinner_Eeff.setSelection(Integer.parseInt(prefs.getString(
                CSLIN_Eeff_UNIT, "0")));

        edittext_Freq.setText(prefs.getString(CSLIN_Freq, "1.00"));
        spinner_Freq.setSelection(Integer.parseInt(prefs.getString(
                CSLIN_Freq_UNIT, "1")));

        edittext_er.setText(prefs.getString(CSLIN_er, "4.00"));

        edittext_H.setText(prefs.getString(CSLIN_H, "60.00"));
        spinner_H.setSelection(Integer.parseInt(prefs.getString(CSLIN_H_UNIT,
                "0")));

        edittext_T.setText(prefs.getString(CSLIN_T, "2.80"));
        spinner_T.setSelection(Integer.parseInt(prefs.getString(CSLIN_T_UNIT,
                "0")));

        use_z0k = prefs.getString(CSLIN_USEZ0k, "true").equals("true");
    }

    private void Preference_SharedPref() {
        SharedPreferences prefs = getActivity().getSharedPreferences(MainActivity.SHARED_PREFS_NAME,
                ActionBarActivity.MODE_PRIVATE);// get the parameters from the Shared Preferences in the device universal parameters
        DecimalLength = Integer.parseInt(prefs.getString("DecimalLength", "2"));
    }

    @Override
    public void onStop() {
        super.onStop();

        String cslin_W, cslin_S, cslin_L, cslin_Z0, cslin_k, cslin_Z0e, cslin_Z0o, cslin_Eeff, cslin_Freq, cslin_er, cslin_H, cslin_T;
        String cslin_W_unit, cslin_S_unit, cslin_L_unit, cslin_Z0_unit, cslin_Z0e_unit, cslin_Z0o_unit, cslin_Eeff_unit, cslin_Freq_unit, cslin_H_unit, cslin_T_unit;
        String cslin_use_z0k;

        SharedPreferences prefs = getActivity().getSharedPreferences(MainActivity.SHARED_PREFS_NAME,
                ActionBarActivity.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        cslin_W = edittext_W.getText().toString();
        cslin_W_unit = Integer.toString(spinner_W.getSelectedItemPosition());
        cslin_S = edittext_S.getText().toString();
        cslin_S_unit = Integer.toString(spinner_S.getSelectedItemPosition());
        cslin_L = edittext_L.getText().toString();
        cslin_L_unit = Integer.toString(spinner_L.getSelectedItemPosition());
        cslin_Z0 = edittext_Z0.getText().toString();
        cslin_Z0_unit = Integer.toString(spinner_Z0.getSelectedItemPosition());
        cslin_k = edittext_k.getText().toString();
        cslin_Z0e = edittext_Z0e.getText().toString();
        cslin_Z0e_unit = Integer
                .toString(spinner_Z0e.getSelectedItemPosition());
        cslin_Z0o = edittext_Z0o.getText().toString();
        cslin_Z0o_unit = Integer
                .toString(spinner_Z0o.getSelectedItemPosition());
        cslin_Eeff = edittext_Eeff.getText().toString();
        cslin_Eeff_unit = Integer.toString(spinner_Eeff
                .getSelectedItemPosition());
        cslin_Freq = edittext_Freq.getText().toString();
        cslin_Freq_unit = Integer.toString(spinner_Freq
                .getSelectedItemPosition());
        cslin_er = edittext_er.getText().toString();
        cslin_H = edittext_H.getText().toString();
        cslin_H_unit = Integer.toString(spinner_H.getSelectedItemPosition());
        cslin_T = edittext_T.getText().toString();
        cslin_T_unit = Integer.toString(spinner_T.getSelectedItemPosition());
        if (use_z0k) {
            cslin_use_z0k = "true";
        } else {
            cslin_use_z0k = "false";
        }

        editor.putString(CSLIN_W, cslin_W);
        editor.putString(CSLIN_W_UNIT, cslin_W_unit);
        editor.putString(CSLIN_S, cslin_S);
        editor.putString(CSLIN_S_UNIT, cslin_S_unit);
        editor.putString(CSLIN_L, cslin_L);
        editor.putString(CSLIN_L_UNIT, cslin_L_unit);
        editor.putString(CSLIN_Z0, cslin_Z0);
        editor.putString(CSLIN_Z0_UNIT, cslin_Z0_unit);
        editor.putString(CSLIN_k, cslin_k);
        editor.putString(CSLIN_Z0e, cslin_Z0e);
        editor.putString(CSLIN_Z0e_UNIT, cslin_Z0e_unit);
        editor.putString(CSLIN_Z0o, cslin_Z0o);
        editor.putString(CSLIN_Z0o_UNIT, cslin_Z0o_unit);
        editor.putString(CSLIN_Eeff, cslin_Eeff);
        editor.putString(CSLIN_Eeff_UNIT, cslin_Eeff_unit);
        editor.putString(CSLIN_Freq, cslin_Freq);
        editor.putString(CSLIN_Freq_UNIT, cslin_Freq_unit);
        editor.putString(CSLIN_er, cslin_er);
        editor.putString(CSLIN_H, cslin_H);
        editor.putString(CSLIN_H_UNIT, cslin_H_unit);
        editor.putString(CSLIN_T, cslin_T);
        editor.putString(CSLIN_T_UNIT, cslin_T_unit);
        editor.putString(CSLIN_USEZ0k, cslin_use_z0k);

        editor.commit();
    }

    private boolean analysisInputCheck() {
        boolean checkResult = true;
        if (edittext_W.length() == 0) {
            edittext_W.setError(getText(R.string.Error_W_empty));
            checkResult = false;
        }
        if (edittext_S.length() == 0) {
            edittext_S.setError(getText(R.string.Error_S_empty));
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
        if (use_z0k) {
            if (edittext_Z0.length() == 0) {
                error_Z0.setSpan(new SubscriptSpan(), 13, 14,
                        Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                edittext_Z0.setError(error_Z0);
                checkResult = false;
            }
            if (edittext_k.length() == 0) {
                edittext_k.setError(getText(R.string.Error_k_empty));
                checkResult = false;
            }
        } else {
            if (edittext_Z0e.length() == 0) {
                error_Z0e.setSpan(new SubscriptSpan(), 13, 15,
                        Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                edittext_Z0e.setError(error_Z0e);
                checkResult = false;
            }
            if (edittext_Z0o.length() == 0) {
                error_Z0o.setSpan(new SubscriptSpan(), 13, 15,
                        Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                edittext_Z0o.setError(error_Z0o);
                checkResult = false;
            }
        }
        return checkResult;
    }

    private void synthesizeButton() {
        int temp;
        if (use_z0k) {
            Z0 = Double.parseDouble(edittext_Z0.getText().toString()); // get the parameters
            k = Double.parseDouble(edittext_k.getText().toString());
            Z0e = 0;
            Z0o = 0;
        } else {
            Z0e = Double.parseDouble(edittext_Z0e.getText().toString());
            Z0o = Double.parseDouble(edittext_Z0o.getText().toString());
            Z0 = 0;
            k = 0;
        }

        Freq = Double.parseDouble(edittext_Freq.getText().toString());
        er = Double.parseDouble(edittext_er.getText().toString());
        H = Double.parseDouble(edittext_H.getText().toString());
        T = Double.parseDouble(edittext_T.getText().toString());

        temp = spinner_Freq.getSelectedItemPosition(); // convert the unit to Hz
        if (temp == 0) {
            Freq = Freq * 1000000;
        } else if (temp == 1) {
            Freq = Freq * 1000000000;
        }

        temp = spinner_H.getSelectedItemPosition(); // convert the unit to metre
        if (temp == 0) {
            H = H / 39370.0787402;
        } else if (temp == 1) {
            H = H / 1000;
        } else if (temp == 2) {
            H = H / 100;
        }

        temp = spinner_T.getSelectedItemPosition(); // convert the unit to metre
        if (temp == 0) {
            T = T / 39370.0787402;
        } else if (temp == 1) {
            T = T / 1000;
        } else if (temp == 2) {
            T = T / 100;
        }

        if (edittext_Eeff.length() != 0) { // check if the Eeff is empty
            Eeff = Double.parseDouble(edittext_Eeff.getText().toString());
            CSLIN cslin = new CSLIN(0, 0, 0, Z0, k, Z0o, Z0e, Eeff, Freq, er, H, T, use_z0k);
            cslin.cslin_syn();
            L = cslin.getL();
            W = cslin.getW();
            S = cslin.getS();

            temp = spinner_L.getSelectedItemPosition();
            if (temp == 0) {
                L = L * 1000 * 39.37007874;
            } else if (temp == 1) {
                L = L * 1000;
            } else if (temp == 2) {
                L = L * 100;
            }
            BigDecimal L_temp = new BigDecimal(L); // cut the decimal of L
            L = L_temp.setScale(DecimalLength, BigDecimal.ROUND_HALF_UP)
                    .doubleValue();
            edittext_L.setText(String.valueOf(L));
        } else {
            CSLIN cslin = new CSLIN(0, 0, 0, Z0, k, Z0o, Z0e, 0, Freq, er, H, T, use_z0k);
            cslin.cslin_syn();
            W = cslin.getW();
            S = cslin.getS();
            edittext_L.setText(""); // clear the L if the Eeff input is empty
        }

        temp = spinner_W.getSelectedItemPosition(); // W (m)
        if (temp == 0) {
            W = W * 1000 * 39.37007874;
        } else if (temp == 1) {
            W = W * 1000;
        } else if (temp == 2) {
            W = W * 100;
        }

        BigDecimal W_temp = new BigDecimal(W); // cut the decimal of W
        W = W_temp.setScale(DecimalLength, BigDecimal.ROUND_HALF_UP)
                .doubleValue();
        edittext_W.setText(String.valueOf(W));

        temp = spinner_S.getSelectedItemPosition();
        if (temp == 0) {
            S = S * 1000 * 39.37007874;
        } else if (temp == 1) {
            S = S * 1000;
        } else if (temp == 2) {
            S = S * 100;
        }

        BigDecimal S_temp = new BigDecimal(S); // cut the decimal of S
        S = S_temp.setScale(DecimalLength, BigDecimal.ROUND_HALF_UP)
                .doubleValue();
        edittext_S.setText(String.valueOf(S));
    }
}
