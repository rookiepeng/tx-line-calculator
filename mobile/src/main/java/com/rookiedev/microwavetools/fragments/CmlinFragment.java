package com.rookiedev.microwavetools.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;

import com.rookiedev.microwavetools.R;
import com.rookiedev.microwavetools.libs.CmlinCalculator;
import com.rookiedev.microwavetools.libs.CmlinModel;
import com.rookiedev.microwavetools.libs.Constants;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class CmlinFragment extends Fragment {
    private Context mContext;
    private View viewRoot;
    private TextInputEditText editTextW, editTextG, editTextL, editTextZ0, editTextK, editTextZ0o, editTextZ0e,
            editTextPhs, editTextFreq, editTextH, editTextEr;
    private TextInputLayout textInputLayoutH, textInputLayoutW, textInputLayoutG, textInputLayoutZ0, textInputLayoutK,
            textInputLayoutZ0o, textInputLayoutZ0e, textInputLayoutEr, textInputLayoutF;
    private CmlinModel line;
    private Button buttonSynthesize, buttonAnalyze;
    private AutoCompleteTextView spinnerW, spinnerG, spinnerL, spinnerT, spinnerH, spinnerZ0, spinnerZ0o, spinnerZ0e, spinnerPhs,
            spinnerFreq;
    private RadioButton radioButtonZ0, radioButtonK, radioButtonZ0o, radioButtonZ0e;
    private boolean useZ0k; // calculate with Z0, k, or Z0e, Z0o

    public CmlinFragment() {
        // Empty constructor required for fragment subclasses
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        viewRoot = inflater.inflate(R.layout.fragment_cmlin, container, false);
        mContext = this.getContext();

        initUI();
        readSharedPref();
        setRadioBtn();

        buttonAnalyze.setOnClickListener(view -> {
            Constants.refreshAnimation(mContext, viewRoot.findViewById(R.id.analyze_reveal),
                    Constants.ANALYZE);
            clearEditTextErrors();
            if (analysisInputCheck()) {
                line.setMetalWidth(Double.parseDouble(Objects.requireNonNull(editTextW.getText()).toString()),
                        spinnerW.getText().toString());
                line.setMetalSpace(Double.parseDouble(Objects.requireNonNull(editTextG.getText()).toString()),
                        spinnerG.getText().toString());
                line.setFrequency(Double.parseDouble(Objects.requireNonNull(editTextFreq.getText()).toString()),
                        spinnerFreq.getText().toString());
                line.setSubEpsilon(Double.parseDouble(Objects.requireNonNull(editTextEr.getText()).toString()));
                line.setSubHeight(Double.parseDouble(Objects.requireNonNull(editTextH.getText()).toString()),
                        spinnerH.getText().toString());
                line.setMetalThick(0, spinnerT.getText().toString());

                if (editTextL.length() != 0) {
                    line.setMetalLength(Double.parseDouble(Objects.requireNonNull(editTextL.getText()).toString()),
                            spinnerL.getText().toString());
                } else {
                    line.setMetalLength(0, spinnerL.getText().toString());
                }

                CmlinCalculator cmlin = new CmlinCalculator();
                line = cmlin.getAnaResult(line);

                if (editTextL.length() != 0) {
                    BigDecimal Eeff_temp = BigDecimal.valueOf(line.getPhase());
                    double Eeff = Eeff_temp.setScale(Constants.DecimalLength, RoundingMode.HALF_UP)
                            .doubleValue();
                    editTextPhs.setText(String.valueOf(Eeff));
                } else {
                    editTextPhs.setText("");
                }

                BigDecimal Z0_temp = BigDecimal.valueOf(line.getImpedance());
                double Z0 = Z0_temp.setScale(Constants.DecimalLength, RoundingMode.HALF_UP).doubleValue();
                editTextZ0.setText(String.valueOf(Z0));

                BigDecimal k_temp = BigDecimal.valueOf(line.getCouplingFactor());
                double k = k_temp.setScale(Constants.DecimalLength, RoundingMode.HALF_UP).doubleValue();
                editTextK.setText(String.valueOf(k));

                BigDecimal Z0o_temp = BigDecimal.valueOf(line.getImpedanceOdd());
                double Z0o = Z0o_temp.setScale(Constants.DecimalLength, RoundingMode.HALF_UP).doubleValue();
                editTextZ0o.setText(String.valueOf(Z0o));

                BigDecimal Z0e_temp = BigDecimal.valueOf(line.getImpedanceEven());
                double Z0e = Z0e_temp.setScale(Constants.DecimalLength, RoundingMode.HALF_UP).doubleValue();
                editTextZ0e.setText(String.valueOf(Z0e));
            }
        });

        buttonSynthesize.setOnClickListener(view -> {
            Constants.refreshAnimation(mContext, viewRoot.findViewById(R.id.synthesize_reveal),
                    Constants.SYNTHESIZE);
            clearEditTextErrors();
            if (synthesizeInputCheck()) {
                synthesizeButton();
            }
        });

        return viewRoot;
    }

    private void initUI() {
        line = new CmlinModel();

        RadioButton radioButtonW = viewRoot.findViewById(R.id.radioBtn_W);
        radioButtonW.setVisibility(View.VISIBLE);
        radioButtonW.setChecked(true);

        RadioButton radioButtonS = viewRoot.findViewById(R.id.radioBtn_g);
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
        editTextW = viewRoot.findViewById(R.id.editText_W);
        editTextW.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
        editTextW.setBackgroundTintList(AppCompatResources.getColorStateList(mContext, R.color.background_tint_synthesize));
        editTextW.addTextChangedListener(new TextWatcher() {
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

        textInputLayoutG = viewRoot.findViewById(R.id.text_input_layout_g);
        editTextG = viewRoot.findViewById(R.id.editText_g);
        editTextG.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
        editTextG.setBackgroundTintList(AppCompatResources.getColorStateList(mContext, R.color.background_tint_synthesize));
        editTextG.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                textInputLayoutG.setError("");
                textInputLayoutG.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        editTextL = viewRoot.findViewById(R.id.editText_L);
        editTextL.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
        editTextL.setBackgroundTintList(AppCompatResources.getColorStateList(mContext, R.color.background_tint_synthesize));

        textInputLayoutZ0 = viewRoot.findViewById(R.id.text_input_layout_Z0);
        editTextZ0 = viewRoot.findViewById(R.id.editText_Z0);
        editTextZ0.addTextChangedListener(new TextWatcher() {
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
        editTextK = viewRoot.findViewById(R.id.editText_k);
        editTextK.addTextChangedListener(new TextWatcher() {
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
        editTextZ0o = viewRoot.findViewById(R.id.editText_Z0o);
        editTextZ0o.addTextChangedListener(new TextWatcher() {
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
        editTextZ0e = viewRoot.findViewById(R.id.editText_Z0e);
        editTextZ0e.addTextChangedListener(new TextWatcher() {
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

        editTextPhs = viewRoot.findViewById(R.id.editText_Phs);
        editTextPhs.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor));
        editTextPhs.setBackgroundTintList(AppCompatResources.getColorStateList(mContext, R.color.background_tint_analyze));

        textInputLayoutF = viewRoot.findViewById(R.id.text_input_layout_Freq);
        editTextFreq = viewRoot.findViewById(R.id.editText_Freq);
        editTextFreq.addTextChangedListener(new TextWatcher() {
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

        EditText editTextT = viewRoot.findViewById(R.id.editText_T);
        editTextT.setText("0");
        editTextT.setEnabled(false);

        textInputLayoutH = viewRoot.findViewById(R.id.text_input_layout_H);
        editTextH = viewRoot.findViewById(R.id.editText_H);
        editTextH.addTextChangedListener(new TextWatcher() {
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
        editTextEr = viewRoot.findViewById(R.id.editText_er);
        editTextEr.addTextChangedListener(new TextWatcher() {
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
        spinnerW = viewRoot.findViewById(R.id.menu_W);
        spinnerG = viewRoot.findViewById(R.id.menu_g);
        spinnerL = viewRoot.findViewById(R.id.menu_L);
        spinnerZ0 = viewRoot.findViewById(R.id.menu_Z0);
        spinnerZ0o = viewRoot.findViewById(R.id.menu_Z0o);
        spinnerZ0e = viewRoot.findViewById(R.id.menu_Z0e);
        spinnerPhs = viewRoot.findViewById(R.id.menu_Phs);
        spinnerFreq = viewRoot.findViewById(R.id.menu_Freq);
        spinnerT = viewRoot.findViewById(R.id.menu_T);
        spinnerH = viewRoot.findViewById(R.id.menu_H);

        // configure the length units
        spinnerW.setAdapter(Constants.adapterDimensionUnits(mContext));
        spinnerG.setAdapter(Constants.adapterDimensionUnits(mContext));
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
        radioButtonZ0.setOnClickListener(arg0 -> {
            radioButtonZ0.setChecked(true);
            radioButtonK.setChecked(true);
            radioButtonZ0o.setChecked(false);
            radioButtonZ0e.setChecked(false);
            useZ0k = true;
            editTextZ0.setEnabled(true);
            editTextZ0.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor));
            editTextK.setEnabled(true);
            editTextK.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor));
            editTextZ0o.setEnabled(false);
            editTextZ0o.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColorLight));
            editTextZ0e.setEnabled(false);
            editTextZ0e.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColorLight));
            editTextZ0.setBackgroundTintList(AppCompatResources.getColorStateList(mContext, R.color.background_tint_analyze));
            editTextK.setBackgroundTintList(AppCompatResources.getColorStateList(mContext, R.color.background_tint_analyze));
            editTextZ0e
                    .setBackgroundTintList(AppCompatResources.getColorStateList(mContext, R.color.background_tint_analyze_light));
            editTextZ0o
                    .setBackgroundTintList(AppCompatResources.getColorStateList(mContext, R.color.background_tint_analyze_light));
        });
        radioButtonK.setOnClickListener(arg0 -> {
            radioButtonZ0.setChecked(true);
            radioButtonK.setChecked(true);
            radioButtonZ0o.setChecked(false);
            radioButtonZ0e.setChecked(false);
            useZ0k = true;
            editTextZ0.setEnabled(true);
            editTextZ0.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor));
            editTextK.setEnabled(true);
            editTextK.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor));
            editTextZ0o.setEnabled(false);
            editTextZ0o.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColorLight));
            editTextZ0e.setEnabled(false);
            editTextZ0e.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColorLight));
            editTextZ0.setBackgroundTintList(AppCompatResources.getColorStateList(mContext, R.color.background_tint_analyze));
            editTextK.setBackgroundTintList(AppCompatResources.getColorStateList(mContext, R.color.background_tint_analyze));
            editTextZ0e
                    .setBackgroundTintList(AppCompatResources.getColorStateList(mContext, R.color.background_tint_analyze_light));
            editTextZ0o
                    .setBackgroundTintList(AppCompatResources.getColorStateList(mContext, R.color.background_tint_analyze_light));
        });
        radioButtonZ0o.setOnClickListener(arg0 -> {
            radioButtonZ0o.setChecked(true);
            radioButtonZ0e.setChecked(true);
            radioButtonZ0.setChecked(false);
            radioButtonK.setChecked(false);
            useZ0k = false;
            editTextZ0.setEnabled(false);
            editTextZ0.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColorLight));
            editTextK.setEnabled(false);
            editTextK.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColorLight));
            editTextZ0o.setEnabled(true);
            editTextZ0o.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor));
            editTextZ0e.setEnabled(true);
            editTextZ0e.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor));
            editTextZ0
                    .setBackgroundTintList(AppCompatResources.getColorStateList(mContext, R.color.background_tint_analyze_light));
            editTextK
                    .setBackgroundTintList(AppCompatResources.getColorStateList(mContext, R.color.background_tint_analyze_light));
            editTextZ0e.setBackgroundTintList(AppCompatResources.getColorStateList(mContext, R.color.background_tint_analyze));
            editTextZ0o.setBackgroundTintList(AppCompatResources.getColorStateList(mContext, R.color.background_tint_analyze));
        });
        radioButtonZ0e.setOnClickListener(arg0 -> {
            radioButtonZ0o.setChecked(true);
            radioButtonZ0e.setChecked(true);
            radioButtonZ0.setChecked(false);
            radioButtonK.setChecked(false);
            useZ0k = false;
            editTextZ0.setEnabled(false);
            editTextZ0.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColorLight));
            editTextK.setEnabled(false);
            editTextK.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColorLight));
            editTextZ0o.setEnabled(true);
            editTextZ0o.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor));
            editTextZ0e.setEnabled(true);
            editTextZ0e.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor));
            editTextZ0
                    .setBackgroundTintList(AppCompatResources.getColorStateList(mContext, R.color.background_tint_analyze_light));
            editTextK
                    .setBackgroundTintList(AppCompatResources.getColorStateList(mContext, R.color.background_tint_analyze_light));
            editTextZ0e.setBackgroundTintList(AppCompatResources.getColorStateList(mContext, R.color.background_tint_analyze));
            editTextZ0o.setBackgroundTintList(AppCompatResources.getColorStateList(mContext, R.color.background_tint_analyze));
        });
        if (useZ0k) {
            radioButtonZ0.setChecked(true);
            radioButtonK.setChecked(true);
            radioButtonZ0o.setChecked(false);
            radioButtonZ0e.setChecked(false);
            editTextZ0.setEnabled(true);
            editTextZ0.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor));
            editTextK.setEnabled(true);
            editTextK.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor));
            editTextZ0o.setEnabled(false);
            editTextZ0o.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColorLight));
            editTextZ0e.setEnabled(false);
            editTextZ0e.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColorLight));
            editTextZ0.setBackgroundTintList(AppCompatResources.getColorStateList(mContext, R.color.background_tint_analyze));
            editTextK.setBackgroundTintList(AppCompatResources.getColorStateList(mContext, R.color.background_tint_analyze));
            editTextZ0e.setBackgroundTintList(AppCompatResources.getColorStateList(mContext, R.color.background_tint_analyze_light));
            editTextZ0o.setBackgroundTintList(AppCompatResources.getColorStateList(mContext, R.color.background_tint_analyze_light));
        } else {
            radioButtonZ0.setChecked(false);
            radioButtonK.setChecked(false);
            radioButtonZ0o.setChecked(true);
            radioButtonZ0e.setChecked(true);
            editTextZ0.setEnabled(false);
            editTextZ0.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColorLight));
            editTextK.setEnabled(false);
            editTextK.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColorLight));
            editTextZ0o.setEnabled(true);
            editTextZ0o.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor));
            editTextZ0e.setEnabled(true);
            editTextZ0e.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor));
            editTextZ0.setBackgroundTintList(AppCompatResources.getColorStateList(mContext, R.color.background_tint_analyze_light));
            editTextK.setBackgroundTintList(AppCompatResources.getColorStateList(mContext, R.color.background_tint_analyze_light));
            editTextZ0e.setBackgroundTintList(AppCompatResources.getColorStateList(mContext, R.color.background_tint_analyze));
            editTextZ0o.setBackgroundTintList(AppCompatResources.getColorStateList(mContext, R.color.background_tint_analyze));
        }
    }

    private void readSharedPref() {
        SharedPreferences prefs = mContext.getSharedPreferences(Constants.SHARED_PREFS_NAME,
                AppCompatActivity.MODE_PRIVATE);

        editTextW.setText(prefs.getString(Constants.CMLIN_W, "2.7"));
        spinnerW.setText(Constants.validateUnit(Constants.adapterDimensionUnits(mContext), prefs.getString(Constants.CMLIN_W_UNIT, Constants.LengthUnit_mm)), false);

        editTextG.setText(prefs.getString(Constants.CMLIN_S, "0.8"));
        spinnerG.setText(Constants.validateUnit(Constants.adapterDimensionUnits(mContext), prefs.getString(Constants.CMLIN_S_UNIT, Constants.LengthUnit_mm)), false);

        editTextL.setText(prefs.getString(Constants.CMLIN_L, "40.9"));
        spinnerL.setText(Constants.validateUnit(Constants.adapterDimensionUnits(mContext), prefs.getString(Constants.CMLIN_L_UNIT, Constants.LengthUnit_mm)), false);

        editTextZ0.setText(prefs.getString(Constants.CMLIN_Z0, "50"));
        spinnerZ0.setText(Constants.validateUnit(Constants.adapterImpedanceUnits(mContext), prefs.getString(Constants.CMLIN_Z0_UNIT, Constants.ImpedanceUnit_Ohm)), false);

        editTextK.setText(prefs.getString(Constants.CMLIN_K, "0.2"));

        editTextZ0o.setText(prefs.getString(Constants.CMLIN_Z0O, "41.044"));
        spinnerZ0o.setText(Constants.validateUnit(Constants.adapterImpedanceUnits(mContext), prefs.getString(Constants.CMLIN_Z0O_UNIT, Constants.ImpedanceUnit_Ohm)), false);

        editTextZ0e.setText(prefs.getString(Constants.CMLIN_Z0E, "62.341"));
        spinnerZ0e.setText(Constants.validateUnit(Constants.adapterImpedanceUnits(mContext), prefs.getString(Constants.CMLIN_Z0E_UNIT, Constants.ImpedanceUnit_Ohm)), false);

        editTextPhs.setText(prefs.getString(Constants.CMLIN_PHS, "90"));
        spinnerPhs.setText(Constants.validateUnit(Constants.adapterPhaseUnits(mContext), prefs.getString(Constants.CMLIN_PHS_UNIT, Constants.PhaseUnit_Degree)), false);

        editTextFreq.setText(prefs.getString(Constants.CMLIN_FREQ, "1.00"));
        spinnerFreq.setText(Constants.validateUnit(Constants.adapterFrequencyUnits(mContext), prefs.getString(Constants.CMLIN_FREQ_UNIT, Constants.FreqUnit_GHz)), false);

        editTextEr.setText(prefs.getString(Constants.CMLIN_ER, "4.60"));

        editTextH.setText(prefs.getString(Constants.CMLIN_H, "1.6"));
        spinnerH.setText(Constants.validateUnit(Constants.adapterDimensionUnits(mContext), prefs.getString(Constants.CMLIN_H_UNIT, Constants.LengthUnit_mm)), false);

        useZ0k = prefs.getString(Constants.CMLIN_USEZ0K, "true").equals("true");
    }

    @Override
    public void onStop() {
        super.onStop();
        SharedPreferences prefs = mContext.getSharedPreferences(Constants.SHARED_PREFS_NAME,
                AppCompatActivity.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString(Constants.CMLIN_W, Objects.requireNonNull(editTextW.getText()).toString());
        editor.putString(Constants.CMLIN_W_UNIT, spinnerW.getText().toString());
        editor.putString(Constants.CMLIN_S, Objects.requireNonNull(editTextG.getText()).toString());
        editor.putString(Constants.CMLIN_S_UNIT, spinnerG.getText().toString());
        editor.putString(Constants.CMLIN_L, Objects.requireNonNull(editTextL.getText()).toString());
        editor.putString(Constants.CMLIN_L_UNIT, spinnerL.getText().toString());
        editor.putString(Constants.CMLIN_Z0, Objects.requireNonNull(editTextZ0.getText()).toString());
        editor.putString(Constants.CMLIN_Z0_UNIT, spinnerZ0.getText().toString());
        editor.putString(Constants.CMLIN_K, Objects.requireNonNull(editTextK.getText()).toString());
        editor.putString(Constants.CMLIN_Z0E, Objects.requireNonNull(editTextZ0e.getText()).toString());
        editor.putString(Constants.CMLIN_Z0E_UNIT, spinnerZ0e.getText().toString());
        editor.putString(Constants.CMLIN_Z0O_UNIT, spinnerZ0o.getText().toString());
        editor.putString(Constants.CMLIN_PHS, Objects.requireNonNull(editTextPhs.getText()).toString());
        editor.putString(Constants.CMLIN_PHS_UNIT, spinnerPhs.getText().toString());
        editor.putString(Constants.CMLIN_FREQ, Objects.requireNonNull(editTextFreq.getText()).toString());
        editor.putString(Constants.CMLIN_FREQ_UNIT, spinnerFreq.getText().toString());
        editor.putString(Constants.CMLIN_ER, Objects.requireNonNull(editTextEr.getText()).toString());
        editor.putString(Constants.CMLIN_H, Objects.requireNonNull(editTextH.getText()).toString());
        editor.putString(Constants.CMLIN_H_UNIT, spinnerH.getText().toString());

        if (useZ0k) {
            editor.putString(Constants.CMLIN_USEZ0K, "true");
        } else {
            editor.putString(Constants.CMLIN_USEZ0K, "false");
        }
        editor.apply();
    }

    private boolean analysisInputCheck() {
        boolean checkResult = true;
        if (editTextW.length() == 0) {
            textInputLayoutW.setError(getText(R.string.Error_W_empty));
            checkResult = false;
        } else if (Constants.value2meter(Double.parseDouble(Objects.requireNonNull(editTextW.getText()).toString()),
                spinnerW.getText().toString()) < Constants.MINI_LIMIT) {
            textInputLayoutW.setError(getText(R.string.unreasonable_value));
            checkResult = false;
        }

        if (editTextG.length() == 0) {
            textInputLayoutG.setError(getText(R.string.Error_S_empty));
            checkResult = false;
        } else if (Constants.value2meter(Double.parseDouble(Objects.requireNonNull(editTextG.getText()).toString()),
                spinnerG.getText().toString()) < Constants.MINI_LIMIT) {
            textInputLayoutG.setError(getText(R.string.unreasonable_value));
            checkResult = false;
        }

        if (editTextFreq.length() == 0) {
            textInputLayoutF.setError(getText(R.string.Error_Freq_empty));
            checkResult = false;
        } else if (Double.parseDouble(Objects.requireNonNull(editTextFreq.getText()).toString()) == 0) {
            textInputLayoutF.setError(getText(R.string.error_zero_frequency));
            checkResult = false;
        }

        if (editTextH.length() == 0) {
            textInputLayoutH.setError(getText(R.string.Error_H_empty));
            checkResult = false;
        } else if (Constants.value2meter(Double.parseDouble(Objects.requireNonNull(editTextH.getText()).toString()),
                spinnerH.getText().toString()) < Constants.MINI_LIMIT) {
            textInputLayoutH.setError(getText(R.string.unreasonable_value));
            checkResult = false;
        }

        if (editTextEr.length() == 0) {
            textInputLayoutEr.setError(getText(R.string.error_er_empty));
            checkResult = false;
        } else if (Double.parseDouble(Objects.requireNonNull(editTextEr.getText()).toString()) < 1) {
            textInputLayoutEr.setError(getText(R.string.unreasonable_value));
            checkResult = false;
        }

        if (!checkResult) {
            editTextZ0.setText("");
            editTextPhs.setText("");
            editTextZ0o.setText("");
            editTextZ0e.setText("");
            editTextK.setText("");
        }
        return checkResult;
    }

    private boolean synthesizeInputCheck() {
        boolean checkResult = true;
        if (editTextFreq.length() == 0) {
            textInputLayoutF.setError(getText(R.string.Error_Freq_empty));
            checkResult = false;
        } else if (Double.parseDouble(Objects.requireNonNull(editTextFreq.getText()).toString()) == 0) {
            textInputLayoutF.setError(getText(R.string.error_zero_frequency));
            checkResult = false;
        }

        if (editTextH.length() == 0) {
            textInputLayoutH.setError(getText(R.string.Error_H_empty));
            checkResult = false;
        } else if (Constants.value2meter(Double.parseDouble(Objects.requireNonNull(editTextH.getText()).toString()),
                spinnerH.getText().toString()) < Constants.MINI_LIMIT) {
            textInputLayoutH.setError(getText(R.string.unreasonable_value));
            checkResult = false;
        }

        if (editTextEr.length() == 0) {
            textInputLayoutEr.setError(getText(R.string.error_er_empty));
            checkResult = false;
        } else if (Double.parseDouble(Objects.requireNonNull(editTextEr.getText()).toString()) < 1) {
            textInputLayoutEr.setError(getText(R.string.unreasonable_value));
            checkResult = false;
        }

        if (useZ0k) {
            if (editTextZ0.length() == 0) {
                textInputLayoutZ0.setError(getText(R.string.error_Z0_empty));
                checkResult = false;
            } else if (Double.parseDouble(Objects.requireNonNull(editTextZ0.getText()).toString()) == 0) {
                textInputLayoutZ0.setError(getText(R.string.unreasonable_value));
                checkResult = false;
            }

            if (editTextK.length() == 0) {
                textInputLayoutK.setError(getText(R.string.Error_k_empty));
                checkResult = false;
            } else if (Double.parseDouble(Objects.requireNonNull(editTextK.getText()).toString()) == 0) {
                textInputLayoutK.setError(getText(R.string.unreasonable_value));
                checkResult = false;
            }
        } else {
            if (editTextZ0e.length() == 0) {
                textInputLayoutZ0e.setError(getText(R.string.error_Z0e_empty));
                checkResult = false;
            } else if (Double.parseDouble(Objects.requireNonNull(editTextZ0e.getText()).toString()) == 0) {
                textInputLayoutZ0e.setError(getText(R.string.unreasonable_value));
                checkResult = false;
            }

            if (editTextZ0o.length() == 0) {
                textInputLayoutZ0o.setError(getText(R.string.error_Z0o_empty));
                checkResult = false;
            } else if (Double.parseDouble(Objects.requireNonNull(editTextZ0o.getText()).toString()) == 0) {
                textInputLayoutZ0o.setError(getText(R.string.unreasonable_value));
                checkResult = false;
            }
        }

        if (!checkResult) {
            editTextL.setText("");
            editTextG.setText("");
            editTextW.setText("");
        }
        return checkResult;
    }

    private void synthesizeButton() {
        if (useZ0k) {
            line.setImpedance(Double.parseDouble(Objects.requireNonNull(editTextZ0.getText()).toString()));
            line.setCouplingFactor(Double.parseDouble(Objects.requireNonNull(editTextK.getText()).toString()));
            line.setImpedanceOdd(0);
            line.setImpedanceEven(0);
        } else {
            line.setImpedanceEven(Double.parseDouble(Objects.requireNonNull(editTextZ0e.getText()).toString()));
            line.setImpedanceOdd(Double.parseDouble(Objects.requireNonNull(editTextZ0o.getText()).toString()));
            line.setImpedance(0);
            line.setCouplingFactor(0);
        }

        line.setFrequency(Double.parseDouble(Objects.requireNonNull(editTextFreq.getText()).toString()), spinnerFreq.getText().toString());
        line.setSubEpsilon(Double.parseDouble(Objects.requireNonNull(editTextEr.getText()).toString()));
        line.setSubHeight(Double.parseDouble(Objects.requireNonNull(editTextH.getText()).toString()), spinnerH.getText().toString());
        line.setMetalThick(0, spinnerT.getText().toString());

        double W, S, L;
        if (editTextPhs.length() != 0) {
            line.setPhase(Double.parseDouble(Objects.requireNonNull(editTextPhs.getText()).toString()));
        } else {
            line.setPhase(0);
        }
        CmlinCalculator cmlin = new CmlinCalculator();
        line = cmlin.getSynResult(line, useZ0k);

        if (line.getErrorCode() == Constants.ERROR.NO_ERROR) {
            if (editTextPhs.length() != 0) {
                BigDecimal L_temp = BigDecimal.valueOf(Constants.meter2others(line.getMetalLength(), spinnerL.getText().toString()));
                L = L_temp.setScale(Constants.DecimalLength, RoundingMode.HALF_UP).doubleValue();
                editTextL.setText(String.valueOf(L));
            } else {
                editTextL.setText("");
            }

            if ((Double.isNaN(line.getMetalWidth()) || Double.isInfinite(line.getMetalWidth()))
                    || (Double.isNaN(line.getMetalSpace()) || Double.isInfinite(line.getMetalSpace()))) {
                editTextW.setText("");
                textInputLayoutW.setError(getString(R.string.synthesize_failed));
                editTextW.requestFocus();
                editTextG.setText("");
                textInputLayoutG.setError(getString(R.string.synthesize_failed));
                editTextL.setText("");
            } else {
                BigDecimal W_temp = BigDecimal.valueOf(Constants.meter2others(line.getMetalWidth(), spinnerW.getText().toString()));
                W = W_temp.setScale(Constants.DecimalLength, RoundingMode.HALF_UP).doubleValue();
                editTextW.setText(String.valueOf(W));

                BigDecimal S_temp = BigDecimal.valueOf(Constants.meter2others(line.getMetalSpace(), spinnerG.getText().toString()));
                S = S_temp.setScale(Constants.DecimalLength, RoundingMode.HALF_UP).doubleValue();
                editTextG.setText(String.valueOf(S));
            }

            if (useZ0k) {
                double Z0e, Z0o;

                BigDecimal Z0o_temp = BigDecimal.valueOf(line.getImpedanceOdd());
                Z0o = Z0o_temp.setScale(Constants.DecimalLength, RoundingMode.HALF_UP).doubleValue();
                editTextZ0o.setText(String.valueOf(Z0o));

                BigDecimal Z0e_temp = BigDecimal.valueOf(line.getImpedanceEven());
                Z0e = Z0e_temp.setScale(Constants.DecimalLength, RoundingMode.HALF_UP).doubleValue();
                editTextZ0e.setText(String.valueOf(Z0e));
            } else {
                double Z0, k;

                BigDecimal Z0_temp = BigDecimal.valueOf(line.getImpedance());
                Z0 = Z0_temp.setScale(Constants.DecimalLength, RoundingMode.HALF_UP).doubleValue();
                editTextZ0.setText(String.valueOf(Z0)); // cut the decimal of the Z0
                BigDecimal k_temp = BigDecimal.valueOf(line.getCouplingFactor());
                k = k_temp.setScale(Constants.DecimalLength, RoundingMode.HALF_UP).doubleValue();
                editTextK.setText(String.valueOf(k));
            }
        } else if (line.getErrorCode() == Constants.ERROR.K_OUT_OF_RANGE) {
            editTextW.setText("");
            editTextG.setText("");
            editTextL.setText("");
            textInputLayoutK.setError(getString(R.string.k_out_of_range));
            editTextK.requestFocus();
        } else if (line.getErrorCode() == Constants.ERROR.Z0E_Z0O_MISTAKE) {
            editTextW.setText("");
            editTextG.setText("");
            editTextL.setText("");
            textInputLayoutZ0o.setError(getString(R.string.Z0e_larger_Z0o));
            editTextZ0o.requestFocus();
            textInputLayoutZ0e.setError(getString(R.string.Z0e_larger_Z0o));
        } else {
            editTextW.setText("");
            textInputLayoutW.setError(getString(R.string.synthesize_failed));
            editTextW.requestFocus();
            editTextG.setText("");
            textInputLayoutG.setError(getString(R.string.synthesize_failed));
        }
    }

    private void clearEditTextErrors() {
        textInputLayoutW.setError(null);
        textInputLayoutW.setErrorEnabled(false);
        textInputLayoutG.setError(null);
        textInputLayoutG.setErrorEnabled(false);
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
    }

    public void resetValues() {
        editTextW.setText("2.7");
        spinnerW.setText(Constants.LengthUnit_mm);
        editTextG.setText("0.8");
        spinnerG.setText(Constants.LengthUnit_mm);
        editTextL.setText("40.9");
        spinnerL.setText(Constants.LengthUnit_mm);
        editTextZ0.setText("50");
        spinnerZ0.setText(Constants.ImpedanceUnit_Ohm);
        editTextK.setText("0.2");
        editTextZ0o.setText("41.044");
        spinnerZ0o.setText(Constants.ImpedanceUnit_Ohm);
        editTextZ0e.setText("62.341");
        spinnerZ0e.setText(Constants.ImpedanceUnit_Ohm);
        editTextPhs.setText("90");
        spinnerPhs.setText(Constants.PhaseUnit_Degree);
        editTextFreq.setText("1.00");
        spinnerFreq.setText(Constants.FreqUnit_GHz);
        editTextEr.setText("4.60");
        editTextH.setText("1.6");
        spinnerH.setText(Constants.LengthUnit_mm);
        useZ0k = true;

        radioButtonZ0.setChecked(true);
        radioButtonK.setChecked(true);
        radioButtonZ0o.setChecked(false);
        radioButtonZ0e.setChecked(false);
        editTextZ0.setEnabled(true);
        editTextZ0.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor));
        editTextK.setEnabled(true);
        editTextK.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor));
        editTextZ0o.setEnabled(false);
        editTextZ0o.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColorLight));
        editTextZ0e.setEnabled(false);
        editTextZ0e.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColorLight));
        editTextZ0.setBackgroundTintList(AppCompatResources.getColorStateList(mContext, R.color.background_tint_analyze));
        editTextK.setBackgroundTintList(AppCompatResources.getColorStateList(mContext, R.color.background_tint_analyze));
        editTextZ0e.setBackgroundTintList(AppCompatResources.getColorStateList(mContext, R.color.background_tint_analyze_light));
        editTextZ0o.setBackgroundTintList(AppCompatResources.getColorStateList(mContext, R.color.background_tint_analyze_light));
    }
}
