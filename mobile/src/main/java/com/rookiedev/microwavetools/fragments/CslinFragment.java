package com.rookiedev.microwavetools.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;

import com.rookiedev.microwavetools.R;
import com.rookiedev.microwavetools.libs.Constants;
import com.rookiedev.microwavetools.libs.CslinCalculator;
import com.rookiedev.microwavetools.libs.CslinModel;

import java.math.BigDecimal;

public class CslinFragment extends Fragment {
    private Context mContext;
    private View viewRoot;
    // private TextView textZ0, textK, textZ0o, textZ0e;
    private TextInputEditText edittextW, edittextS, edittextL, edittextZ0, edittextK, edittextZ0o, edittextZ0e,
            edittextPhs, edittextFreq, edittextT, edittextH, edittextEr;
    private TextInputLayout textInputLayoutT, textInputLayoutH, textInputLayoutW, textInputLayoutS, textInputLayoutZ0,
            textInputLayoutK, textInputLayoutZ0o, textInputLayoutZ0e, textInputLayoutEr, textInputLayoutF;
    private Button buttonSynthesize, buttonAnalyze;
    private Spinner spinnerW, spinnerS, spinnerL, spinnerT, spinnerH, spinnerZ0, spinnerZ0o, spinnerZ0e, spinnerPhs,
            spinnerFreq;
    private RadioButton radioButtonZ0, radioButtonZ0o, radioButtonK, radioButtonZ0e;
    private boolean useZ0k; // calculate with Z0, k, or Z0e, Z0o
    private CslinModel line;
    private AdFragment adFragment = null;
    private FragmentManager fragmentManager = null;

    private boolean isAdFree;

    public CslinFragment() {
        // Empty constructor required for fragment subclasses
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            isAdFree = getArguments().getBoolean(Constants.IS_AD_FREE, true);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        viewRoot = inflater.inflate(R.layout.fragment_cslin, container, false);
        mContext = this.getContext();
        fragmentManager = getFragmentManager();

        initUI(); // initial the UI
        readSharedPref(); // read shared preferences
        setRadioBtn();

        buttonAnalyze.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Constants.refreshAnimation(mContext, (ImageView) viewRoot.findViewById(R.id.analyze_reveal),
                        Constants.ANALYZE);
                clearEditTextErrors();
                if (analysisInputCheck()) {
                    line.setMetalWidth(Double.parseDouble(edittextW.getText().toString()),
                            spinnerW.getSelectedItemPosition());
                    line.setMetalSpace(Double.parseDouble(edittextS.getText().toString()),
                            spinnerS.getSelectedItemPosition());
                    line.setFrequency(Double.parseDouble(edittextFreq.getText().toString()),
                            spinnerFreq.getSelectedItemPosition());
                    line.setSubEpsilon(Double.parseDouble(edittextEr.getText().toString()));
                    line.setSubHeight(Double.parseDouble(edittextH.getText().toString()),
                            spinnerH.getSelectedItemPosition());
                    line.setMetalThick(Double.parseDouble(edittextT.getText().toString()),
                            spinnerT.getSelectedItemPosition());

                    if (edittextL.length() != 0) {
                        line.setMetalLength(Double.parseDouble(edittextL.getText().toString()),
                                spinnerL.getSelectedItemPosition());
                    } else {
                        line.setMetalLength(0, spinnerL.getSelectedItemPosition());
                    }

                    CslinCalculator cslin = new CslinCalculator();
                    line = cslin.getAnaResult(line);

                    if (edittextL.length() != 0) {
                        BigDecimal Eeff_temp = new BigDecimal(line.getPhase());
                        double Eeff = Eeff_temp.setScale(Constants.DecimalLength, BigDecimal.ROUND_HALF_UP)
                                .doubleValue();
                        edittextPhs.setText(String.valueOf(Eeff));
                    } else {
                        edittextPhs.setText("");
                    }

                    BigDecimal Z0_temp = new BigDecimal(line.getImpedance());
                    double Z0 = Z0_temp.setScale(Constants.DecimalLength, BigDecimal.ROUND_HALF_UP).doubleValue();
                    edittextZ0.setText(String.valueOf(Z0));

                    BigDecimal k_temp = new BigDecimal(line.getCouplingFactor());
                    double k = k_temp.setScale(Constants.DecimalLength, BigDecimal.ROUND_HALF_UP).doubleValue();
                    edittextK.setText(String.valueOf(k));

                    BigDecimal Z0o_temp = new BigDecimal(line.getImpedanceOdd());
                    double Z0o = Z0o_temp.setScale(Constants.DecimalLength, BigDecimal.ROUND_HALF_UP).doubleValue();
                    edittextZ0o.setText(String.valueOf(Z0o));

                    BigDecimal Z0e_temp = new BigDecimal(line.getImpedanceEven());
                    double Z0e = Z0e_temp.setScale(Constants.DecimalLength, BigDecimal.ROUND_HALF_UP).doubleValue();
                    edittextZ0e.setText(String.valueOf(Z0e));
                }
            }
        });

        buttonSynthesize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Constants.refreshAnimation(mContext, (ImageView) viewRoot.findViewById(R.id.synthesize_reveal),
                        Constants.SYNTHESIZE);
                clearEditTextErrors();
                if (synthesizeInputCheck()) {
                    synthesizeButton();
                }
            }
        });

        if (!isAdFree) {
            adFragment = new AdFragment();
            FragmentManager fragmentManager = getFragmentManager();
            if (fragmentManager != null) {
                fragmentManager.beginTransaction().add(R.id.ad_frame, adFragment).commit();
            }
        }
        return viewRoot;
    }

    private void initUI() {
        line = new CslinModel();

        RadioButton radioButtonW = viewRoot.findViewById(R.id.radioBtn_W);
        radioButtonW.setVisibility(View.VISIBLE);
        radioButtonW.setChecked(true);

        RadioButton radioButtonS = viewRoot.findViewById(R.id.radioBtn_S);
        radioButtonS.setVisibility(View.VISIBLE);
        radioButtonS.setChecked(true);

        RadioButton radioButtonL = viewRoot.findViewById(R.id.radioBtn_L);
        radioButtonL.setVisibility(View.VISIBLE);
        radioButtonL.setChecked(true);

        RadioButton radioButtonPhs = viewRoot.findViewById(R.id.radioBtn_Phs);
        radioButtonPhs.setVisibility(View.VISIBLE);
        radioButtonPhs.setChecked(true);

        radioButtonZ0 = viewRoot.findViewById(R.id.radioBtn_Z0);
        radioButtonZ0.setVisibility(View.VISIBLE);
        radioButtonK = viewRoot.findViewById(R.id.radioBtn_k);
        radioButtonK.setVisibility(View.VISIBLE);
        radioButtonZ0o = viewRoot.findViewById(R.id.radioBtn_Z0o);
        radioButtonZ0o.setVisibility(View.VISIBLE);
        radioButtonZ0e = viewRoot.findViewById(R.id.radioBtn_Z0e);
        radioButtonZ0e.setVisibility(View.VISIBLE);

        textInputLayoutW = viewRoot.findViewById(R.id.text_input_layout_W);
        edittextW = viewRoot.findViewById(R.id.editText_W);
        edittextW.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
        edittextW.setBackgroundTintList(getResources().getColorStateList(R.color.background_tint_synthersize));
        edittextW.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                textInputLayoutW.setError("");
                textInputLayoutW.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        textInputLayoutS = viewRoot.findViewById(R.id.text_input_layout_S);
        edittextS = viewRoot.findViewById(R.id.editText_S);
        edittextS.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
        edittextS.setBackgroundTintList(getResources().getColorStateList(R.color.background_tint_synthersize));
        edittextS.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                textInputLayoutS.setError("");
                textInputLayoutS.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        edittextL = viewRoot.findViewById(R.id.editText_L);
        edittextL.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
        edittextL.setBackgroundTintList(getResources().getColorStateList(R.color.background_tint_synthersize));

        textInputLayoutZ0 = viewRoot.findViewById(R.id.text_input_layout_Z0);
        edittextZ0 = viewRoot.findViewById(R.id.editText_Z0);
        edittextZ0.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                textInputLayoutZ0.setError("");
                textInputLayoutZ0.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        textInputLayoutK = viewRoot.findViewById(R.id.text_input_layout_k);
        edittextK = viewRoot.findViewById(R.id.editText_k);
        edittextK.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                textInputLayoutK.setError("");
                textInputLayoutK.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        textInputLayoutZ0o = viewRoot.findViewById(R.id.text_input_layout_Z0o);
        edittextZ0o = viewRoot.findViewById(R.id.editText_Z0o);
        edittextZ0o.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                textInputLayoutZ0o.setError("");
                textInputLayoutZ0o.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        textInputLayoutZ0e = viewRoot.findViewById(R.id.text_input_layout_Z0e);
        edittextZ0e = viewRoot.findViewById(R.id.editText_Z0e);
        edittextZ0e.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                textInputLayoutZ0e.setError("");
                textInputLayoutZ0e.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        edittextPhs = viewRoot.findViewById(R.id.editText_Phs);
        edittextPhs.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor));
        edittextPhs.setBackgroundTintList(getResources().getColorStateList(R.color.background_tint_analyze));

        textInputLayoutF = viewRoot.findViewById(R.id.text_input_layout_Freq);
        edittextFreq = viewRoot.findViewById(R.id.editText_Freq);
        edittextFreq.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                textInputLayoutF.setError("");
                textInputLayoutF.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        textInputLayoutT = viewRoot.findViewById(R.id.text_input_layout_T);
        edittextT = viewRoot.findViewById(R.id.editText_T);
        edittextT.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                textInputLayoutT.setError("");
                textInputLayoutT.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        textInputLayoutH = viewRoot.findViewById(R.id.text_input_layout_H);
        edittextH = viewRoot.findViewById(R.id.editText_H);
        edittextH.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                textInputLayoutH.setError("");
                textInputLayoutH.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        textInputLayoutEr = viewRoot.findViewById(R.id.text_input_layout_er);
        edittextEr = viewRoot.findViewById(R.id.editText_er);
        edittextEr.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                textInputLayoutEr.setError("");
                textInputLayoutEr.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // button elements
        buttonAnalyze = viewRoot.findViewById(R.id.button_ana);
        buttonSynthesize = viewRoot.findViewById(R.id.button_syn);

        // spinner elements
        spinnerW = viewRoot.findViewById(R.id.spinner_W);
        spinnerS = viewRoot.findViewById(R.id.spinner_S);
        spinnerL = viewRoot.findViewById(R.id.spinner_L);
        spinnerZ0 = viewRoot.findViewById(R.id.spinner_Z0);
        spinnerZ0o = viewRoot.findViewById(R.id.spinner_Z0o);
        spinnerZ0e = viewRoot.findViewById(R.id.spinner_Z0e);
        spinnerPhs = viewRoot.findViewById(R.id.spinner_Phs);
        spinnerFreq = viewRoot.findViewById(R.id.spinner_Freq);
        spinnerT = viewRoot.findViewById(R.id.spinner_T);
        spinnerH = viewRoot.findViewById(R.id.spinner_H);

        // configure the length units
        spinnerW.setAdapter(Constants.adapterDimensionUnits(mContext));
        spinnerS.setAdapter(Constants.adapterDimensionUnits(mContext));
        spinnerL.setAdapter(Constants.adapterDimensionUnits(mContext));
        spinnerT.setAdapter(Constants.adapterDimensionUnits(mContext));
        spinnerH.setAdapter(Constants.adapterDimensionUnits(mContext));

        // configure the impedance units
        spinnerZ0.setAdapter(Constants.adapterImpedanceUnits(mContext));
        spinnerZ0o.setAdapter(Constants.adapterImpedanceUnits(mContext));
        spinnerZ0e.setAdapter(Constants.adapterImpedanceUnits(mContext));

        // configure the electrical length units
        spinnerPhs.setAdapter(Constants.adapterPhaseUnits(mContext));

        // configure the frequency units
        spinnerFreq.setAdapter(Constants.adapterFrequencyUnits(mContext));
    }

    private void setRadioBtn() {
        radioButtonZ0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                radioButtonZ0.setChecked(true);
                radioButtonK.setChecked(true);
                radioButtonZ0o.setChecked(false);
                radioButtonZ0e.setChecked(false);
                useZ0k = true;
                edittextZ0.setEnabled(true);
                edittextZ0.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor));
                edittextK.setEnabled(true);
                edittextK.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor));
                edittextZ0o.setEnabled(false);
                edittextZ0o.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColorLight));
                edittextZ0e.setEnabled(false);
                edittextZ0e.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColorLight));
                edittextZ0.setBackgroundTintList(getResources().getColorStateList(R.color.background_tint_analyze));
                edittextK.setBackgroundTintList(getResources().getColorStateList(R.color.background_tint_analyze));
                edittextZ0e
                        .setBackgroundTintList(getResources().getColorStateList(R.color.background_tint_analyze_light));
                edittextZ0o
                        .setBackgroundTintList(getResources().getColorStateList(R.color.background_tint_analyze_light));
            }
        });
        radioButtonK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                radioButtonZ0.setChecked(true);
                radioButtonK.setChecked(true);
                radioButtonZ0o.setChecked(false);
                radioButtonZ0e.setChecked(false);
                useZ0k = true;
                edittextZ0.setEnabled(true);
                edittextZ0.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor));
                edittextK.setEnabled(true);
                edittextK.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor));
                edittextZ0o.setEnabled(false);
                edittextZ0o.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColorLight));
                edittextZ0e.setEnabled(false);
                edittextZ0e.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColorLight));
                edittextZ0.setBackgroundTintList(getResources().getColorStateList(R.color.background_tint_analyze));
                edittextK.setBackgroundTintList(getResources().getColorStateList(R.color.background_tint_analyze));
                edittextZ0e
                        .setBackgroundTintList(getResources().getColorStateList(R.color.background_tint_analyze_light));
                edittextZ0o
                        .setBackgroundTintList(getResources().getColorStateList(R.color.background_tint_analyze_light));
            }
        });
        radioButtonZ0o.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                radioButtonZ0o.setChecked(true);
                radioButtonZ0e.setChecked(true);
                radioButtonZ0.setChecked(false);
                radioButtonK.setChecked(false);
                useZ0k = false;
                edittextZ0.setEnabled(false);
                edittextZ0.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColorLight));
                edittextK.setEnabled(false);
                edittextK.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColorLight));
                edittextZ0o.setEnabled(true);
                edittextZ0o.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor));
                edittextZ0e.setEnabled(true);
                edittextZ0e.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor));
                edittextZ0
                        .setBackgroundTintList(getResources().getColorStateList(R.color.background_tint_analyze_light));
                edittextK
                        .setBackgroundTintList(getResources().getColorStateList(R.color.background_tint_analyze_light));
                edittextZ0e.setBackgroundTintList(getResources().getColorStateList(R.color.background_tint_analyze));
                edittextZ0o.setBackgroundTintList(getResources().getColorStateList(R.color.background_tint_analyze));
            }
        });
        radioButtonZ0e.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                radioButtonZ0o.setChecked(true);
                radioButtonZ0e.setChecked(true);
                radioButtonZ0.setChecked(false);
                radioButtonK.setChecked(false);
                useZ0k = false;
                edittextZ0.setEnabled(false);
                edittextZ0.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColorLight));
                edittextK.setEnabled(false);
                edittextK.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColorLight));
                edittextZ0o.setEnabled(true);
                edittextZ0o.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor));
                edittextZ0e.setEnabled(true);
                edittextZ0e.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor));
                edittextZ0
                        .setBackgroundTintList(getResources().getColorStateList(R.color.background_tint_analyze_light));
                edittextK
                        .setBackgroundTintList(getResources().getColorStateList(R.color.background_tint_analyze_light));
                edittextZ0e.setBackgroundTintList(getResources().getColorStateList(R.color.background_tint_analyze));
                edittextZ0o.setBackgroundTintList(getResources().getColorStateList(R.color.background_tint_analyze));
            }
        });
        if (useZ0k) {
            radioButtonZ0.setChecked(true);
            radioButtonK.setChecked(true);
            radioButtonZ0o.setChecked(false);
            radioButtonZ0e.setChecked(false);
            edittextZ0.setEnabled(true);
            edittextZ0.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor));
            edittextK.setEnabled(true);
            edittextK.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor));
            edittextZ0o.setEnabled(false);
            edittextZ0o.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColorLight));
            edittextZ0e.setEnabled(false);
            edittextZ0e.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColorLight));
            edittextZ0.setBackgroundTintList(getResources().getColorStateList(R.color.background_tint_analyze));
            edittextK.setBackgroundTintList(getResources().getColorStateList(R.color.background_tint_analyze));
            edittextZ0e.setBackgroundTintList(getResources().getColorStateList(R.color.background_tint_analyze_light));
            edittextZ0o.setBackgroundTintList(getResources().getColorStateList(R.color.background_tint_analyze_light));
        } else {
            radioButtonZ0.setChecked(false);
            radioButtonK.setChecked(false);
            radioButtonZ0o.setChecked(true);
            radioButtonZ0e.setChecked(true);
            edittextZ0.setEnabled(false);
            edittextZ0.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColorLight));
            edittextK.setEnabled(false);
            edittextK.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColorLight));
            edittextZ0o.setEnabled(true);
            edittextZ0o.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor));
            edittextZ0e.setEnabled(true);
            edittextZ0e.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor));
            edittextZ0.setBackgroundTintList(getResources().getColorStateList(R.color.background_tint_analyze_light));
            edittextK.setBackgroundTintList(getResources().getColorStateList(R.color.background_tint_analyze_light));
            edittextZ0e.setBackgroundTintList(getResources().getColorStateList(R.color.background_tint_analyze));
            edittextZ0o.setBackgroundTintList(getResources().getColorStateList(R.color.background_tint_analyze));
        }
    }

    private void readSharedPref() {
        SharedPreferences prefs = mContext.getSharedPreferences(Constants.SHARED_PREFS_NAME,
                AppCompatActivity.MODE_PRIVATE);// get the header_parameters from the Shared

        edittextW.setText(prefs.getString(Constants.CSLIN_W, "0.59"));
        spinnerW.setSelection(
                Integer.parseInt(prefs.getString(Constants.CSLIN_W_UNIT, Integer.toString(Constants.LengthUnit_mm))));

        edittextS.setText(prefs.getString(Constants.CSLIN_S, "0.36"));
        spinnerS.setSelection(
                Integer.parseInt(prefs.getString(Constants.CSLIN_S_UNIT, Integer.toString(Constants.LengthUnit_mm))));

        edittextL.setText(prefs.getString(Constants.CSLIN_L, "34.9"));
        spinnerL.setSelection(
                Integer.parseInt(prefs.getString(Constants.CSLIN_L_UNIT, Integer.toString(Constants.LengthUnit_mm))));

        edittextZ0.setText(prefs.getString(Constants.CSLIN_Z0, "50"));
        spinnerZ0.setSelection(Integer
                .parseInt(prefs.getString(Constants.CSLIN_Z0_UNIT, Integer.toString(Constants.ImpedanceUnit_Ohm))));

        edittextK.setText(prefs.getString(Constants.CSLIN_K, "0.2"));

        edittextZ0o.setText(prefs.getString(Constants.CSLIN_Z0O, "40.8"));
        spinnerZ0o.setSelection(Integer
                .parseInt(prefs.getString(Constants.CSLIN_Z0O_UNIT, Integer.toString(Constants.ImpedanceUnit_Ohm))));

        edittextZ0e.setText(prefs.getString(Constants.CSLIN_Z0E, "61.2"));
        spinnerZ0e.setSelection(Integer
                .parseInt(prefs.getString(Constants.CSLIN_Z0E_UNIT, Integer.toString(Constants.ImpedanceUnit_Ohm))));

        edittextPhs.setText(prefs.getString(Constants.CSLIN_PHS, "90"));
        spinnerPhs.setSelection(Integer
                .parseInt(prefs.getString(Constants.CSLIN_PHS_UNIT, Integer.toString(Constants.PhaseUnit_Degree))));

        edittextFreq.setText(prefs.getString(Constants.CSLIN_FREQ, "1.00"));
        spinnerFreq.setSelection(
                Integer.parseInt(prefs.getString(Constants.CSLIN_FREQ_UNIT, Integer.toString(Constants.FreqUnit_GHz))));

        edittextEr.setText(prefs.getString(Constants.CSLIN_ER, "4.6"));

        edittextH.setText(prefs.getString(Constants.CSLIN_H, "1.6"));
        spinnerH.setSelection(
                Integer.parseInt(prefs.getString(Constants.CSLIN_H_UNIT, Integer.toString(Constants.LengthUnit_mm))));

        edittextT.setText(prefs.getString(Constants.CSLIN_T, "0.035"));
        spinnerT.setSelection(
                Integer.parseInt(prefs.getString(Constants.CSLIN_T_UNIT, Integer.toString(Constants.LengthUnit_mm))));

        useZ0k = prefs.getString(Constants.CSLIN_USEZ0K, "true").equals("true");
    }

    @Override
    public void onStop() {
        super.onStop();
        SharedPreferences prefs = mContext.getSharedPreferences(Constants.SHARED_PREFS_NAME,
                AppCompatActivity.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString(Constants.CSLIN_W, edittextW.getText().toString());
        editor.putString(Constants.CSLIN_W_UNIT, Integer.toString(spinnerW.getSelectedItemPosition()));
        editor.putString(Constants.CSLIN_S, edittextS.getText().toString());
        editor.putString(Constants.CSLIN_S_UNIT, Integer.toString(spinnerS.getSelectedItemPosition()));
        editor.putString(Constants.CSLIN_L, edittextL.getText().toString());
        editor.putString(Constants.CSLIN_L_UNIT, Integer.toString(spinnerL.getSelectedItemPosition()));
        editor.putString(Constants.CSLIN_Z0, edittextZ0.getText().toString());
        editor.putString(Constants.CSLIN_Z0_UNIT, Integer.toString(spinnerZ0.getSelectedItemPosition()));
        editor.putString(Constants.CSLIN_K, edittextK.getText().toString());
        editor.putString(Constants.CSLIN_Z0E, edittextZ0e.getText().toString());
        editor.putString(Constants.CSLIN_Z0E_UNIT, Integer.toString(spinnerZ0e.getSelectedItemPosition()));
        editor.putString(Constants.CSLIN_Z0O, edittextZ0o.getText().toString());
        editor.putString(Constants.CSLIN_Z0O_UNIT, Integer.toString(spinnerZ0o.getSelectedItemPosition()));
        editor.putString(Constants.CSLIN_PHS, edittextPhs.getText().toString());
        editor.putString(Constants.CSLIN_PHS_UNIT, Integer.toString(spinnerPhs.getSelectedItemPosition()));
        editor.putString(Constants.CSLIN_FREQ, edittextFreq.getText().toString());
        editor.putString(Constants.CSLIN_FREQ_UNIT, Integer.toString(spinnerFreq.getSelectedItemPosition()));
        editor.putString(Constants.CSLIN_ER, edittextEr.getText().toString());
        editor.putString(Constants.CSLIN_H, edittextH.getText().toString());
        editor.putString(Constants.CSLIN_H_UNIT, Integer.toString(spinnerH.getSelectedItemPosition()));
        editor.putString(Constants.CSLIN_T, edittextT.getText().toString());
        editor.putString(Constants.CSLIN_T_UNIT, Integer.toString(spinnerT.getSelectedItemPosition()));
        if (useZ0k) {
            editor.putString(Constants.CSLIN_USEZ0K, "true");
        } else {
            editor.putString(Constants.CSLIN_USEZ0K, "false");
        }
        editor.apply();
    }

    private boolean analysisInputCheck() {
        boolean checkResult = true;
        if (edittextW.length() == 0) {
            textInputLayoutW.setError(getText(R.string.Error_W_empty));
            checkResult = false;
        } else if (Constants.value2meter(Double.parseDouble(edittextW.getText().toString()),
                spinnerW.getSelectedItemPosition()) < Constants.MINI_LIMIT) {
            textInputLayoutW.setError(getText(R.string.unreasonable_value));
            checkResult = false;
        }

        if (edittextS.length() == 0) {
            textInputLayoutS.setError(getText(R.string.Error_S_empty));
            checkResult = false;
        } else if (Constants.value2meter(Double.parseDouble(edittextS.getText().toString()),
                spinnerS.getSelectedItemPosition()) < Constants.MINI_LIMIT) {
            textInputLayoutS.setError(getText(R.string.unreasonable_value));
            checkResult = false;
        }

        if (edittextFreq.length() == 0) {
            textInputLayoutF.setError(getText(R.string.Error_Freq_empty));
            checkResult = false;
        } else if (Double.parseDouble(edittextFreq.getText().toString()) == 0) {
            textInputLayoutF.setError(getText(R.string.error_zero_frequency));
            checkResult = false;
        }

        if (edittextH.length() == 0) {
            textInputLayoutH.setError(getText(R.string.Error_H_empty));
            checkResult = false;
        } else if (Constants.value2meter(Double.parseDouble(edittextH.getText().toString()),
                spinnerH.getSelectedItemPosition()) < Constants.MINI_LIMIT) {
            textInputLayoutH.setError(getText(R.string.unreasonable_value));
            checkResult = false;
        }

        if (edittextT.length() == 0) {
            textInputLayoutT.setError(getText(R.string.Error_T_empty));
            checkResult = false;
        }

        if (edittextEr.length() == 0) {
            textInputLayoutEr.setError(Constants.errorErEmpty(mContext));
            checkResult = false;
        } else if (Double.parseDouble(edittextEr.getText().toString()) < 1) {
            textInputLayoutEr.setError(getText(R.string.unreasonable_value));
            checkResult = false;
        }

        if (!checkResult) {
            edittextZ0.setText("");
            edittextPhs.setText("");
            edittextZ0o.setText("");
            edittextZ0e.setText("");
            edittextK.setText("");
        }

        return checkResult;
    }

    private boolean synthesizeInputCheck() {
        boolean checkResult = true;
        if (edittextFreq.length() == 0) {
            textInputLayoutF.setError(getText(R.string.Error_Freq_empty));
            checkResult = false;
        } else if (Double.parseDouble(edittextFreq.getText().toString()) == 0) {
            textInputLayoutF.setError(getText(R.string.error_zero_frequency));
            checkResult = false;
        }

        if (edittextH.length() == 0) {
            textInputLayoutH.setError(getText(R.string.Error_H_empty));
            checkResult = false;
        } else if (Constants.value2meter(Double.parseDouble(edittextH.getText().toString()),
                spinnerH.getSelectedItemPosition()) < Constants.MINI_LIMIT) {
            textInputLayoutH.setError(getText(R.string.unreasonable_value));
            checkResult = false;
        }

        if (edittextT.length() == 0) {
            textInputLayoutT.setError(getText(R.string.Error_T_empty));
            checkResult = false;
        }
        if (edittextEr.length() == 0) {
            textInputLayoutEr.setError(Constants.errorErEmpty(mContext));
            checkResult = false;
        } else if (Double.parseDouble(edittextEr.getText().toString()) < 1) {
            textInputLayoutEr.setError(getText(R.string.unreasonable_value));
            checkResult = false;
        }

        if (useZ0k) {
            if (edittextZ0.length() == 0) {
                textInputLayoutZ0.setError(Constants.errorZ0Empty(mContext));
                checkResult = false;
            } else if (Double.parseDouble(edittextZ0.getText().toString()) == 0) {
                textInputLayoutZ0.setError(getText(R.string.unreasonable_value));
                checkResult = false;
            }

            if (edittextK.length() == 0) {
                textInputLayoutK.setError(getText(R.string.Error_k_empty));
                checkResult = false;
            } else if (Double.parseDouble(edittextK.getText().toString()) == 0) {
                textInputLayoutK.setError(getText(R.string.unreasonable_value));
                checkResult = false;
            }
        } else {
            if (edittextZ0e.length() == 0) {
                textInputLayoutZ0e.setError(Constants.errorZ0eEmpty(mContext));
                checkResult = false;
            } else if (Double.parseDouble(edittextZ0e.getText().toString()) == 0) {
                textInputLayoutZ0e.setError(getText(R.string.unreasonable_value));
                checkResult = false;
            }

            if (edittextZ0o.length() == 0) {
                textInputLayoutZ0o.setError(Constants.errorZ0oEmpty(mContext));
                checkResult = false;
            } else if (Double.parseDouble(edittextZ0o.getText().toString()) == 0) {
                textInputLayoutZ0o.setError(getText(R.string.unreasonable_value));
                checkResult = false;
            }
        }

        if (!checkResult) {
            edittextL.setText("");
            edittextS.setText("");
            edittextW.setText("");
        }

        return checkResult;
    }

    private void synthesizeButton() {
        if (useZ0k) {
            line.setImpedance(Double.parseDouble(edittextZ0.getText().toString()));
            line.setCouplingFactor(Double.parseDouble(edittextK.getText().toString()));
            line.setImpedanceOdd(0);
            line.setImpedanceEven(0);
        } else {
            line.setImpedanceEven(Double.parseDouble(edittextZ0e.getText().toString()));
            line.setImpedanceOdd(Double.parseDouble(edittextZ0o.getText().toString()));
            line.setImpedance(0);
            line.setCouplingFactor(0);
        }

        line.setFrequency(Double.parseDouble(edittextFreq.getText().toString()), spinnerFreq.getSelectedItemPosition());
        line.setSubEpsilon(Double.parseDouble(edittextEr.getText().toString()));
        line.setSubHeight(Double.parseDouble(edittextH.getText().toString()), spinnerH.getSelectedItemPosition());
        line.setMetalThick(Double.parseDouble(edittextT.getText().toString()), spinnerT.getSelectedItemPosition());

        if (edittextPhs.length() != 0) {
            line.setPhase(Double.parseDouble(edittextPhs.getText().toString()));
        } else {
            line.setPhase(0);
        }
        CslinCalculator cslin = new CslinCalculator();
        line = cslin.getSynResult(line, useZ0k);

        if (line.getErrorCode() == Constants.ERROR.NO_ERROR) {
            if (edittextPhs.length() != 0) {
                BigDecimal L_temp = new BigDecimal(
                        Constants.meter2others(line.getMetalLength(), spinnerL.getSelectedItemPosition()));
                double L = L_temp.setScale(Constants.DecimalLength, BigDecimal.ROUND_HALF_UP).doubleValue();
                edittextL.setText(String.valueOf(L));
            } else {
                edittextL.setText("");
            }

            if ((Double.isNaN(line.getMetalWidth()) || Double.isInfinite(line.getMetalWidth()))
                    || (Double.isNaN(line.getMetalSpace()) || Double.isInfinite(line.getMetalSpace()))) {
                edittextW.setText("");
                textInputLayoutW.setError(getString(R.string.synthesize_failed));
                edittextW.requestFocus();
                edittextS.setText("");
                textInputLayoutS.setError(getString(R.string.synthesize_failed));
                edittextL.setText("");
            } else {
                BigDecimal W_temp = new BigDecimal(
                        Constants.meter2others(line.getMetalWidth(), spinnerW.getSelectedItemPosition()));
                double W = W_temp.setScale(Constants.DecimalLength, BigDecimal.ROUND_HALF_UP).doubleValue();
                edittextW.setText(String.valueOf(W));

                BigDecimal S_temp = new BigDecimal(
                        Constants.meter2others(line.getMetalSpace(), spinnerS.getSelectedItemPosition()));
                double S = S_temp.setScale(Constants.DecimalLength, BigDecimal.ROUND_HALF_UP).doubleValue();
                edittextS.setText(String.valueOf(S));
            }

            if (useZ0k) {
                BigDecimal Z0o_temp = new BigDecimal(line.getImpedanceOdd());
                double Z0o = Z0o_temp.setScale(Constants.DecimalLength, BigDecimal.ROUND_HALF_UP).doubleValue();
                edittextZ0o.setText(String.valueOf(Z0o));

                BigDecimal Z0e_temp = new BigDecimal(line.getImpedanceEven());
                double Z0e = Z0e_temp.setScale(Constants.DecimalLength, BigDecimal.ROUND_HALF_UP).doubleValue();
                edittextZ0e.setText(String.valueOf(Z0e));
            } else {
                BigDecimal Z0_temp = new BigDecimal(line.getImpedance());
                double Z0 = Z0_temp.setScale(Constants.DecimalLength, BigDecimal.ROUND_HALF_UP).doubleValue();
                edittextZ0.setText(String.valueOf(Z0));

                BigDecimal k_temp = new BigDecimal(line.getCouplingFactor());
                double k = k_temp.setScale(Constants.DecimalLength, BigDecimal.ROUND_HALF_UP).doubleValue();
                edittextK.setText(String.valueOf(k));
            }
        } else if (line.getErrorCode() == Constants.ERROR.K_OUT_OF_RANGE) {
            edittextW.setText("");
            edittextS.setText("");
            edittextL.setText("");
            textInputLayoutK.setError(getString(R.string.k_out_of_range));
            edittextK.requestFocus();
        } else if (line.getErrorCode() == Constants.ERROR.Z0E_Z0O_MISTAKE) {
            edittextW.setText("");
            edittextS.setText("");
            edittextL.setText("");
            textInputLayoutZ0o.setError(getString(R.string.Z0e_larger_Z0o));
            edittextZ0o.requestFocus();
            textInputLayoutZ0e.setError(getString(R.string.Z0e_larger_Z0o));
        } else {
            edittextW.setText("");
            textInputLayoutW.setError(getString(R.string.synthesize_failed));
            edittextW.requestFocus();
            edittextS.setText("");
            textInputLayoutS.setError(getString(R.string.synthesize_failed));
        }
    }

    public void addAdFragment() {
        adFragment = new AdFragment();
        if (fragmentManager != null) {
            fragmentManager.beginTransaction().replace(R.id.ad_frame, adFragment).commitAllowingStateLoss();
        }
    }

    public void removeAdFragment() {
        if (adFragment != null) {
            if (fragmentManager != null) {
                fragmentManager.beginTransaction().remove(adFragment).commit();
            }
        }
    }

    private void clearEditTextErrors() {
        textInputLayoutW.setError(null);
        textInputLayoutW.setErrorEnabled(false);
        textInputLayoutS.setError(null);
        textInputLayoutS.setErrorEnabled(false);
        textInputLayoutH.setError(null);
        textInputLayoutH.setErrorEnabled(false);
        textInputLayoutZ0.setError(null);
        textInputLayoutZ0.setErrorEnabled(false);
        textInputLayoutK.setError(null);
        textInputLayoutK.setErrorEnabled(false);
        textInputLayoutZ0e.setError(null);
        textInputLayoutZ0e.setErrorEnabled(false);
        textInputLayoutZ0o.setError(null);
        textInputLayoutZ0o.setErrorEnabled(false);
        textInputLayoutEr.setError(null);
        textInputLayoutEr.setErrorEnabled(false);
        textInputLayoutF.setError(null);
        textInputLayoutF.setErrorEnabled(false);
        textInputLayoutT.setError(null);
        textInputLayoutT.setErrorEnabled(false);
    }
}
