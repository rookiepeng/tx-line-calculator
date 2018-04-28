package com.rookiedev.microwavetools.fragments;

import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
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

import com.rookiedev.microwavetools.R;
import com.rookiedev.microwavetools.libs.CmlinCalculator;
import com.rookiedev.microwavetools.libs.Constants;
import com.rookiedev.microwavetools.libs.CmlinModel;

import java.math.BigDecimal;

public class CmlinFragment extends Fragment {
    private final static String CMLIN_W = "CMLIN_W";
    private final static String CMLIN_W_UNIT = "CMLIN_W_UNIT";
    private final static String CMLIN_S = "CMLIN_S";
    private final static String CMLIN_S_UNIT = "CMLIN_S_UNIT";
    private final static String CMLIN_L = "CMLIN_L";
    private final static String CMLIN_L_UNIT = "CMLIN_L_UNIT";
    private final static String CMLIN_Z0 = "CMLIN_Z0";
    private final static String CMLIN_Z0_UNIT = "CMLIN_Z0_UNIT";
    private final static String CMLIN_k = "CMLIN_k";
    private final static String CMLIN_Z0o = "CMLIN_Z0o";
    private final static String CMLIN_Z0o_UNIT = "CMLIN_Z0o_UNIT";
    private final static String CMLIN_Z0e = "CMLIN_Z0e";
    private final static String CMLIN_Z0e_UNIT = "CMLIN_Z0e_UNIT";
    private final static String CMLIN_Eeff = "CMLIN_Eeff";
    private final static String CMLIN_Eeff_UNIT = "CMLIN_Eeff_UNIT";
    private final static String CMLIN_Freq = "CMLIN_Freq";
    private final static String CMLIN_Freq_UNIT = "CMLIN_Freq_UNIT";
    private final static String CMLIN_er = "CMLIN_er";
    private final static String CMLIN_H = "CMLIN_H";
    private final static String CMLIN_H_UNIT = "CMLIN_H_UNIT";
    private final static String CMLIN_T = "CMLIN_T";
    private final static String CMLIN_T_UNIT = "CMLIN_T_UNIT";
    private final static String CMLIN_USEZ0k = "CMLIN_USEZ0k";
    private int DecimalLength; // the length of the Decimal, accurate of the result
    private View rootView;
    private CardView electricalCard, physicalCard;
    private SpannableString error_er, error_Z0, error_Z0e, error_Z0o;
    private TextView text_er, text_Z0, text_Z0o, text_Z0e, text_Phs; // strings which include the subscript
    private EditText edittext_W, // the width
            edittext_S, //
            edittext_L, // the length
            edittext_Z0, // the impedance
            edittext_k, edittext_Z0o, //
            edittext_Z0e, //
            edittext_Phs, // the electrical length
            edittext_Freq, // the frequency
            edittext_T, // the thickness of the metal
            edittext_H, // the thickness of the dielectric
            edittext_er; // the relative dielectric constant
    //private double W, L, S, Z0, k, Z0o, Z0e, Eeff, Freq, T, H, er;
    private CmlinModel line;
    private Button cmlin_syn,// button synthesize
            cmlin_ana;// button analyze
    private Spinner spinner_W, spinner_S, spinner_L, spinner_T, spinner_H,
            spinner_Z0, spinner_Z0o, spinner_Z0e, spinner_Eeff, spinner_Freq;// the units of each parameter
    private RadioButton radioBtn_Z0, radioBtn_Z0o;
    private boolean use_z0k;

    public CmlinFragment() {
        // Empty constructor required for fragment subclasses
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_cmlin, container, false);

        initUI();
        readSharedPref();
        setRadioBtn();

        cmlin_ana.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //int temp;
                Preference_SharedPref();
                if (!analysisInputCheck()) {
                    edittext_Z0.setText(""); // clear the Z0 and Eeff outputs
                    edittext_Phs.setText("");
                    edittext_Z0o.setText(""); //
                    edittext_Z0e.setText(""); //
                    edittext_k.setText("");
                } else {
                    line.setMetalWidth(Double.parseDouble(edittext_W.getText().toString()), spinner_W.getSelectedItemPosition());  // get the header_parameters
                    line.setMetalSpace(Double.parseDouble(edittext_S.getText().toString()), spinner_S.getSelectedItemPosition());
                    line.setFrequency(Double.parseDouble(edittext_Freq.getText().toString()), spinner_Freq.getSelectedItemPosition());
                    line.setSubEpsilon(Double.parseDouble(edittext_er.getText().toString()));
                    line.setSubHeight(Double.parseDouble(edittext_H.getText().toString()), spinner_H.getSelectedItemPosition());
                    line.setMetalThick(Double.parseDouble(edittext_T.getText().toString()), spinner_T.getSelectedItemPosition());

                    if (edittext_L.length() != 0) {
                        line.setMetalLength(Double.parseDouble(edittext_L.getText().toString()), spinner_L.getSelectedItemPosition());

                        CmlinCalculator cmlin = new CmlinCalculator();
                        line = cmlin.getAnaResult(line);

                        BigDecimal Eeff_temp = new BigDecimal(line.getElectricalLength()); // cut the decimal of the Eeff
                        double Eeff = Eeff_temp.setScale(DecimalLength, BigDecimal.ROUND_HALF_UP).doubleValue();
                        edittext_Phs.setText(String.valueOf(Eeff));
                    } else {
                        CmlinCalculator cmlin = new CmlinCalculator();
                        line = cmlin.getAnaResult(line);
                        edittext_Phs.setText(""); // if the L input is empty, clear the Eeff

                    }
                    BigDecimal Z0_temp = new BigDecimal(line.getImpedance());
                    double Z0 = Z0_temp.setScale(DecimalLength,
                            BigDecimal.ROUND_HALF_UP).doubleValue();
                    edittext_Z0.setText(String.valueOf(Z0)); // cut the decimal
                    // of the Z0
                    BigDecimal k_temp = new BigDecimal(line.getCouplingFactor());
                    double k = k_temp
                            .setScale(DecimalLength, BigDecimal.ROUND_HALF_UP)
                            .doubleValue();
                    edittext_k.setText(String.valueOf(k));

                    BigDecimal Z0o_temp = new BigDecimal(line.getImpedanceOdd());
                    double Z0o = Z0o_temp.setScale(DecimalLength,
                            BigDecimal.ROUND_HALF_UP).doubleValue();
                    edittext_Z0o.setText(String.valueOf(Z0o));

                    BigDecimal Z0e_temp = new BigDecimal(line.getImpedanceEven());
                    double Z0e = Z0e_temp.setScale(DecimalLength, BigDecimal.ROUND_HALF_UP).doubleValue();
                    edittext_Z0e.setText(String.valueOf(Z0e));
                }
                forceRippleAnimation(electricalCard);
            }
        });

        cmlin_syn.setOnClickListener(new View.OnClickListener() {
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
                        double Z0e, Z0o;

                        BigDecimal Z0o_temp = new BigDecimal(line.getImpedanceOdd());
                        Z0o = Z0o_temp.setScale(DecimalLength,
                                BigDecimal.ROUND_HALF_UP).doubleValue();
                        edittext_Z0o.setText(String.valueOf(Z0o));

                        BigDecimal Z0e_temp = new BigDecimal(line.getImpedanceEven());
                        Z0e = Z0e_temp.setScale(DecimalLength,
                                BigDecimal.ROUND_HALF_UP).doubleValue();
                        edittext_Z0e.setText(String.valueOf(Z0e));
                    } else {
                        double Z0, k;

                        BigDecimal Z0_temp = new BigDecimal(line.getImpedance());
                        Z0 = Z0_temp.setScale(DecimalLength,
                                BigDecimal.ROUND_HALF_UP).doubleValue();
                        edittext_Z0.setText(String.valueOf(Z0)); // cut the decimal of the Z0
                        BigDecimal k_temp = new BigDecimal(line.getCouplingFactor());
                        k = k_temp.setScale(DecimalLength,
                                BigDecimal.ROUND_HALF_UP).doubleValue();
                        edittext_k.setText(String.valueOf(k));
                    }
                }
                forceRippleAnimation(physicalCard);
            }
        });

        return rootView;
    }

    private void initUI() {
        //View parameter_width = rootView.findViewById(R.id.parameter_width);
        //parameter_width.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.green_shadow));
        //View parameter_space = rootView.findViewById(R.id.parameter_space);
        //parameter_space.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.green_shadow));
        //View parameter_length = rootView.findViewById(R.id.parameter_length);
        //parameter_length.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.green_shadow));
        //View z0_input_radio = rootView.findViewById(R.id.z0_input_radio);
        //z0_input_radio.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.blue_shadow));
        //View k_input_radio = rootView.findViewById(R.id.k_input_radio);
        //k_input_radio.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.blue_shadow));
        //View z0o_input_radio = rootView.findViewById(R.id.z0o_input_radio);
        //z0o_input_radio.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.blue_shadow));
        //View z0e_input_radio = rootView.findViewById(R.id.z0e_input_radio);
        //z0e_input_radio.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.blue_shadow));
        //View eeff_input_radio = rootView.findViewById(R.id.eeff_input_radio);
        //eeff_input_radio.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.blue_shadow));

        physicalCard = (CardView) rootView.findViewById(R.id.physicalParaCard);
        electricalCard = (CardView) rootView.findViewById(R.id.electricalParaCard);

        error_er = new SpannableString(this.getString(R.string.Error_er_empty));
        error_Z0 = new SpannableString(this.getString(R.string.Error_Z0_empty));
        error_Z0e = new SpannableString(this.getString(R.string.Error_Z0e_empty));
        error_Z0o = new SpannableString(this.getString(R.string.Error_Z0o_empty));

        // find the elements
        text_er = (TextView) rootView.findViewById(R.id.text_er);
        text_Z0 = (TextView) rootView.findViewById(R.id.text_Z0);
        text_Phs = (TextView) rootView.findViewById(R.id.text_Phs);
        text_Z0o = (TextView) rootView.findViewById(R.id.text_Z0o);
        text_Z0e = (TextView) rootView.findViewById(R.id.text_Z0e);

        // Subscript strings
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

        //SpannableString spanEeff = new SpannableString(
        //        this.getString(R.string.text_Eeff));
        //spanEeff.setSpan(new SubscriptSpan(), 1, 4,
        //        Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        //text_Phs.append(spanEeff);

        // edittext elements
        edittext_W = (EditText) rootView.findViewById(R.id.editText_W);
        edittext_S = (EditText) rootView.findViewById(R.id.editText_S);
        edittext_L = (EditText) rootView.findViewById(R.id.editText_L);
        edittext_Z0 = (EditText) rootView.findViewById(R.id.editText_Z0);
        edittext_k = (EditText) rootView.findViewById(R.id.editText_k);
        edittext_Z0o = (EditText) rootView.findViewById(R.id.editText_Z0o);
        edittext_Z0e = (EditText) rootView.findViewById(R.id.editText_Z0e);
        edittext_Phs = (EditText) rootView.findViewById(R.id.editText_Phs);
        edittext_Freq = (EditText) rootView.findViewById(R.id.editText_Freq);
        edittext_T = (EditText) rootView.findViewById(R.id.editText_T);
        edittext_H = (EditText) rootView.findViewById(R.id.editText_H);
        edittext_er = (EditText) rootView.findViewById(R.id.editText_er);

        // button elements
        cmlin_ana = (Button) rootView.findViewById(R.id.button_ana);
        cmlin_syn = (Button) rootView.findViewById(R.id.button_syn);

        // spinner elements
        spinner_W = (Spinner) rootView.findViewById(R.id.spinner_W);
        spinner_S = (Spinner) rootView.findViewById(R.id.spinner_S);
        spinner_L = (Spinner) rootView.findViewById(R.id.spinner_L);
        spinner_Z0 = (Spinner) rootView.findViewById(R.id.spinner_Z0);
        spinner_Z0o = (Spinner) rootView.findViewById(R.id.spinner_Z0o);
        spinner_Z0e = (Spinner) rootView.findViewById(R.id.spinner_Z0e);
        spinner_Eeff = (Spinner) rootView.findViewById(R.id.spinner_Phs);
        spinner_Freq = (Spinner) rootView.findViewById(R.id.spinner_Freq);
        spinner_T = (Spinner) rootView.findViewById(R.id.spinner_T);
        spinner_H = (Spinner) rootView.findViewById(R.id.spinner_H);

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

        radioBtn_Z0 = (RadioButton) rootView.findViewById(R.id.radioBtn_Z0);
        radioBtn_Z0o = (RadioButton) rootView.findViewById(R.id.radioBtn_Z0o);
        line = new CmlinModel();
    }

    private void setRadioBtn() {
        radioBtn_Z0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                radioBtn_Z0.setChecked(true);
                radioBtn_Z0o.setChecked(false);
                use_z0k = true;
                edittext_Z0.setEnabled(true);
                edittext_k.setEnabled(true);
                edittext_Z0o.setEnabled(false);
                edittext_Z0e.setEnabled(false);
            }
        });
        radioBtn_Z0o.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                radioBtn_Z0o.setChecked(true);
                radioBtn_Z0.setChecked(false);
                use_z0k = false;
                edittext_Z0.setEnabled(false);
                edittext_k.setEnabled(false);
                edittext_Z0o.setEnabled(true);
                edittext_Z0e.setEnabled(true);
            }
        });
        if (use_z0k) {
            radioBtn_Z0.setChecked(true);
            radioBtn_Z0o.setChecked(false);
            edittext_Z0.setEnabled(true);
            edittext_k.setEnabled(true);
            edittext_Z0o.setEnabled(false);
            edittext_Z0e.setEnabled(false);
        } else {
            radioBtn_Z0.setChecked(false);
            radioBtn_Z0o.setChecked(true);
            edittext_Z0.setEnabled(false);
            edittext_k.setEnabled(false);
            edittext_Z0o.setEnabled(true);
            edittext_Z0e.setEnabled(true);
        }
    }

    private void readSharedPref() {
        SharedPreferences prefs = getActivity().getSharedPreferences(Constants.SHARED_PREFS_NAME,
                AppCompatActivity.MODE_PRIVATE);// get the header_parameters from the Shared
        // Preferences in the device

        // read values from the shared preferences

        // fragment_cmlin header_parameters
        edittext_W.setText(prefs.getString(CMLIN_W, "20.00"));
        spinner_W.setSelection(Integer.parseInt(prefs.getString(CMLIN_W_UNIT,
                "0")));

        edittext_S.setText(prefs.getString(CMLIN_S, "20.00"));
        spinner_S.setSelection(Integer.parseInt(prefs.getString(CMLIN_S_UNIT,
                "0")));

        edittext_L.setText(prefs.getString(CMLIN_L, "1000.00"));
        spinner_L.setSelection(Integer.parseInt(prefs.getString(CMLIN_L_UNIT,
                "0")));

        edittext_Z0.setText(prefs.getString(CMLIN_Z0, "50.6"));
        spinner_Z0.setSelection(Integer.parseInt(prefs.getString(
                CMLIN_Z0_UNIT, "0")));

        edittext_k.setText(prefs.getString(CMLIN_k, "0.06"));

        edittext_Z0o.setText(prefs.getString(CMLIN_Z0o, "47.65"));
        spinner_Z0o.setSelection(Integer.parseInt(prefs.getString(
                CMLIN_Z0o_UNIT, "0")));

        edittext_Z0e.setText(prefs.getString(CMLIN_Z0e, "53.73"));
        spinner_Z0e.setSelection(Integer.parseInt(prefs.getString(
                CMLIN_Z0e_UNIT, "0")));

        edittext_Phs.setText(prefs.getString(CMLIN_Eeff, "53.33"));
        spinner_Eeff.setSelection(Integer.parseInt(prefs.getString(
                CMLIN_Eeff_UNIT, "0")));

        edittext_Freq.setText(prefs.getString(CMLIN_Freq, "1.00"));
        spinner_Freq.setSelection(Integer.parseInt(prefs.getString(
                CMLIN_Freq_UNIT, "1")));

        edittext_er.setText(prefs.getString(CMLIN_er, "4.00"));

        edittext_H.setText(prefs.getString(CMLIN_H, "10.00"));
        spinner_H.setSelection(Integer.parseInt(prefs.getString(CMLIN_H_UNIT,
                "0")));

        edittext_T.setText(prefs.getString(CMLIN_T, "1.40"));
        spinner_T.setSelection(Integer.parseInt(prefs.getString(CMLIN_T_UNIT,
                "0")));

        use_z0k = prefs.getString(CMLIN_USEZ0k, "true").equals("true");
    }

    private void Preference_SharedPref() {
        SharedPreferences prefs = getActivity().getSharedPreferences(Constants.SHARED_PREFS_NAME,
                AppCompatActivity.MODE_PRIVATE);// get the header_parameters from the Shared Preferences in the device universal header_parameters
        DecimalLength = Integer.parseInt(prefs.getString("DecimalLength", "2"));
    }

    @Override
    public void onStop() {
        super.onStop();

        String cmlin_W, cmlin_S, cmlin_L, cmlin_Z0, cmlin_k, cmlin_Z0e, cmlin_Z0o, cmlin_Eeff, cmlin_Freq, cmlin_er, cmlin_H, cmlin_T;
        String cmlin_W_unit, cmlin_S_unit, cmlin_L_unit, cmlin_Z0_unit, cmlin_Z0e_unit, cmlin_Z0o_unit, cmlin_Eeff_unit, cmlin_Freq_unit, cmlin_H_unit, cmlin_T_unit;
        String cmlin_use_z0k;

        SharedPreferences prefs = getActivity().getSharedPreferences(Constants.SHARED_PREFS_NAME,
                AppCompatActivity.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        cmlin_W = edittext_W.getText().toString();
        cmlin_W_unit = Integer.toString(spinner_W.getSelectedItemPosition());
        cmlin_S = edittext_S.getText().toString();
        cmlin_S_unit = Integer.toString(spinner_S.getSelectedItemPosition());
        cmlin_L = edittext_L.getText().toString();
        cmlin_L_unit = Integer.toString(spinner_L.getSelectedItemPosition());
        cmlin_Z0 = edittext_Z0.getText().toString();
        cmlin_Z0_unit = Integer.toString(spinner_Z0.getSelectedItemPosition());
        cmlin_k = edittext_k.getText().toString();
        cmlin_Z0e = edittext_Z0e.getText().toString();
        cmlin_Z0e_unit = Integer
                .toString(spinner_Z0e.getSelectedItemPosition());
        cmlin_Z0o = edittext_Z0o.getText().toString();
        cmlin_Z0o_unit = Integer
                .toString(spinner_Z0o.getSelectedItemPosition());
        cmlin_Eeff = edittext_Phs.getText().toString();
        cmlin_Eeff_unit = Integer.toString(spinner_Eeff
                .getSelectedItemPosition());
        cmlin_Freq = edittext_Freq.getText().toString();
        cmlin_Freq_unit = Integer.toString(spinner_Freq
                .getSelectedItemPosition());
        cmlin_er = edittext_er.getText().toString();
        cmlin_H = edittext_H.getText().toString();
        cmlin_H_unit = Integer.toString(spinner_H.getSelectedItemPosition());
        cmlin_T = edittext_T.getText().toString();
        cmlin_T_unit = Integer.toString(spinner_T.getSelectedItemPosition());
        if (use_z0k) {
            cmlin_use_z0k = "true";
        } else {
            cmlin_use_z0k = "false";
        }

        editor.putString(CMLIN_W, cmlin_W);
        editor.putString(CMLIN_W_UNIT, cmlin_W_unit);
        editor.putString(CMLIN_S, cmlin_S);
        editor.putString(CMLIN_S_UNIT, cmlin_S_unit);
        editor.putString(CMLIN_L, cmlin_L);
        editor.putString(CMLIN_L_UNIT, cmlin_L_unit);
        editor.putString(CMLIN_Z0, cmlin_Z0);
        editor.putString(CMLIN_Z0_UNIT, cmlin_Z0_unit);
        editor.putString(CMLIN_k, cmlin_k);
        editor.putString(CMLIN_Z0e, cmlin_Z0e);
        editor.putString(CMLIN_Z0e_UNIT, cmlin_Z0e_unit);
        editor.putString(CMLIN_Z0o, cmlin_Z0o);
        editor.putString(CMLIN_Z0o_UNIT, cmlin_Z0o_unit);
        editor.putString(CMLIN_Eeff, cmlin_Eeff);
        editor.putString(CMLIN_Eeff_UNIT, cmlin_Eeff_unit);
        editor.putString(CMLIN_Freq, cmlin_Freq);
        editor.putString(CMLIN_Freq_UNIT, cmlin_Freq_unit);
        editor.putString(CMLIN_er, cmlin_er);
        editor.putString(CMLIN_H, cmlin_H);
        editor.putString(CMLIN_H_UNIT, cmlin_H_unit);
        editor.putString(CMLIN_T, cmlin_T);
        editor.putString(CMLIN_T_UNIT, cmlin_T_unit);
        editor.putString(CMLIN_USEZ0k, cmlin_use_z0k);

        editor.apply();
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
            line.setImpedance(Double.parseDouble(edittext_Z0.getText().toString()));
            line.setCouplingFactor(Double.parseDouble(edittext_k.getText().toString()));
            line.setImpedanceOdd(0);
            line.setImpedanceEven(0);
        } else {
            line.setImpedanceEven(Double.parseDouble(edittext_Z0e.getText().toString()));
            line.setImpedanceOdd(Double.parseDouble(edittext_Z0o.getText().toString()));
            line.setImpedance(0);
            line.setCouplingFactor(0);
        }

        line.setFrequency(Double.parseDouble(edittext_Freq.getText().toString()), spinner_Freq.getSelectedItemPosition());
        line.setSubEpsilon(Double.parseDouble(edittext_er.getText().toString()));
        line.setSubHeight(Double.parseDouble(edittext_H.getText().toString()), spinner_H.getSelectedItemPosition());
        line.setMetalThick(Double.parseDouble(edittext_T.getText().toString()), spinner_T.getSelectedItemPosition());

        double W, S, L;
        if (edittext_Phs.length() != 0) { // check if the Eeff is empty
            line.setElectricalLength(Double.parseDouble(edittext_Phs.getText().toString()));
            CmlinCalculator cmlin = new CmlinCalculator();
            line = cmlin.getSynResult(line, use_z0k);
            L = line.getMetalLength();
            W = line.getMetalWidth();
            S = line.getMetalSpace();

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
            CmlinCalculator cmlin = new CmlinCalculator();
            line = cmlin.getSynResult(line, use_z0k);
            W = line.getMetalWidth();
            S = line.getMetalSpace();
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

    protected void forceRippleAnimation(View view) {
        if (Build.VERSION.SDK_INT >= 23) {
            view.setClickable(true);
            Drawable background = view.getForeground();
            final RippleDrawable rippleDrawable = (RippleDrawable) background;

            rippleDrawable.setState(new int[]{android.R.attr.state_pressed, android.R.attr.state_enabled});

            view.setClickable(false);
            rippleDrawable.setState(new int[]{});

            /*
            Handler handler = new Handler();

            handler.postDelayed(new Runnable()
            {
                @Override public void run()
                {
                    rippleDrawable.setState(new int[]{});
                    physicalCard.setClickable(false);
                    electricalCard.setClickable(false);
                }
            }, 200);*/
        }
    }
}
