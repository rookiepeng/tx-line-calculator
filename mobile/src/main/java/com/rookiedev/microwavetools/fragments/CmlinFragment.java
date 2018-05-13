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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.rookiedev.microwavetools.R;
import com.rookiedev.microwavetools.libs.CmlinCalculator;
import com.rookiedev.microwavetools.libs.CmlinModel;
import com.rookiedev.microwavetools.libs.Constants;

import java.math.BigDecimal;

public class CmlinFragment extends Fragment {
    private Context mContext;
    private View viewRoot;
    //private TextView textZ0, textK, textZ0o, textZ0e;
    private TextInputEditText edittextW, edittextS,edittextL,edittextZ0,edittextK, edittextZ0o,edittextZ0e,edittextPhs,edittextFreq, edittextH,edittextEr;
    private TextInputLayout textInputLayoutT,textInputLayoutH, textInputLayoutW,textInputLayoutS,textInputLayoutZ0,textInputLayoutK,textInputLayoutZ0o,textInputLayoutZ0e,textInputLayoutEr,textInputLayoutF;
    private CmlinModel line;
    private Button buttonSynthesize, buttonAnalyze;
    private Spinner spinnerW, spinnerS, spinnerL, spinnerT, spinnerH, spinnerZ0, spinnerZ0o, spinnerZ0e, spinnerPhs,
            spinnerFreq;
    private RadioButton radioButtonZ0, radioButtonK, radioButtonZ0o, radioButtonZ0e;
    private boolean useZ0k; // calculate with Z0, k, or Z0e, Z0o
    private AdFragment adFragment = null;
    private FragmentManager fragmentManager = null;

    private boolean isAdFree;

    public CmlinFragment() {
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
        viewRoot = inflater.inflate(R.layout.fragment_cmlin, container, false);
        mContext = this.getContext();
        fragmentManager = getFragmentManager();

        initUI();
        readSharedPref();
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
                    line.setMetalThick(0, spinnerT.getSelectedItemPosition());

                    if (edittextL.length() != 0) {
                        line.setMetalLength(Double.parseDouble(edittextL.getText().toString()),
                                spinnerL.getSelectedItemPosition());
                    } else {
                        line.setMetalLength(0, spinnerL.getSelectedItemPosition());
                    }

                    CmlinCalculator cmlin = new CmlinCalculator();
                    line = cmlin.getAnaResult(line);

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
        line = new CmlinModel();

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
        
        textInputLayoutW=viewRoot.findViewById(R.id.text_input_layout_W);
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

        textInputLayoutS=viewRoot.findViewById(R.id.text_input_layout_S);
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

        textInputLayoutZ0=viewRoot.findViewById(R.id.text_input_layout_Z0);
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

        textInputLayoutK=viewRoot.findViewById(R.id.text_input_layout_k);
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

        textInputLayoutZ0o=viewRoot.findViewById(R.id.text_input_layout_Z0o);
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

        textInputLayoutZ0e=viewRoot.findViewById(R.id.text_input_layout_Z0e);
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

        textInputLayoutF=viewRoot.findViewById(R.id.text_input_layout_Freq);
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

        EditText edittextT = viewRoot.findViewById(R.id.editText_T);
        edittextT.setText("0");
        edittextT.setEnabled(false);

        textInputLayoutH=viewRoot.findViewById(R.id.text_input_layout_H);
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

        textInputLayoutEr=viewRoot.findViewById(R.id.text_input_layout_er);
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
                //textZ0.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor));
                //textK.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor));
                //textZ0e.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColorLight));
                //textZ0o.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColorLight));
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
                //textZ0.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor));
                //textK.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor));
                //textZ0e.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColorLight));
                //textZ0o.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColorLight));
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
                //textZ0.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColorLight));
                //textK.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColorLight));
                //textZ0e.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor));
                //textZ0o.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor));
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
                //textZ0.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColorLight));
                //textK.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColorLight));
                //textZ0e.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor));
                //textZ0o.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor));
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
            //textZ0.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor));
            //textK.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor));
            //textZ0e.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColorLight));
            //textZ0o.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColorLight));
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
            //textZ0.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColorLight));
            //textK.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColorLight));
            //textZ0e.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor));
            //textZ0o.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor));
        }
    }

    private void readSharedPref() {
        SharedPreferences prefs = mContext.getSharedPreferences(Constants.SHARED_PREFS_NAME,
                AppCompatActivity.MODE_PRIVATE);// get the header_parameters from the Shared

        edittextW.setText(prefs.getString(Constants.CMLIN_W, "2.7"));
        spinnerW.setSelection(
                Integer.parseInt(prefs.getString(Constants.CMLIN_W_UNIT, Integer.toString(Constants.LengthUnit_mm))));

        edittextS.setText(prefs.getString(Constants.CMLIN_S, "0.8"));
        spinnerS.setSelection(
                Integer.parseInt(prefs.getString(Constants.CMLIN_S_UNIT, Integer.toString(Constants.LengthUnit_mm))));

        edittextL.setText(prefs.getString(Constants.CMLIN_L, "40.9"));
        spinnerL.setSelection(
                Integer.parseInt(prefs.getString(Constants.CMLIN_L_UNIT, Integer.toString(Constants.LengthUnit_mm))));

        edittextZ0.setText(prefs.getString(Constants.CMLIN_Z0, "50"));
        spinnerZ0.setSelection(Integer
                .parseInt(prefs.getString(Constants.CMLIN_Z0_UNIT, Integer.toString(Constants.ImpedanceUnit_Ohm))));

        edittextK.setText(prefs.getString(Constants.CMLIN_K, "0.2"));

        edittextZ0o.setText(prefs.getString(Constants.CMLIN_Z0O, "0.825"));
        spinnerZ0o.setSelection(Integer
                .parseInt(prefs.getString(Constants.CMLIN_Z0O_UNIT, Integer.toString(Constants.ImpedanceUnit_Ohm))));

        edittextZ0e.setText(prefs.getString(Constants.CMLIN_Z0E, "61.237"));
        spinnerZ0e.setSelection(Integer
                .parseInt(prefs.getString(Constants.CMLIN_Z0E_UNIT, Integer.toString(Constants.ImpedanceUnit_Ohm))));

        edittextPhs.setText(prefs.getString(Constants.CMLIN_PHS, "90"));
        spinnerPhs.setSelection(Integer
                .parseInt(prefs.getString(Constants.CMLIN_PHS_UNIT, Integer.toString(Constants.PhaseUnit_Degree))));

        edittextFreq.setText(prefs.getString(Constants.CMLIN_FREQ, "1.00"));
        spinnerFreq.setSelection(
                Integer.parseInt(prefs.getString(Constants.CMLIN_FREQ_UNIT, Integer.toString(Constants.FreqUnit_GHz))));

        edittextEr.setText(prefs.getString(Constants.CMLIN_ER, "4.60"));

        edittextH.setText(prefs.getString(Constants.CMLIN_H, "1.6"));
        spinnerH.setSelection(
                Integer.parseInt(prefs.getString(Constants.CMLIN_H_UNIT, Integer.toString(Constants.LengthUnit_mm))));

        useZ0k = prefs.getString(Constants.CMLIN_USEZ0K, "true").equals("true");
    }

    @Override
    public void onStop() {
        super.onStop();
        SharedPreferences prefs = mContext.getSharedPreferences(Constants.SHARED_PREFS_NAME,
                AppCompatActivity.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString(Constants.CMLIN_W, edittextW.getText().toString());
        editor.putString(Constants.CMLIN_W_UNIT, Integer.toString(spinnerW.getSelectedItemPosition()));
        editor.putString(Constants.CMLIN_S, edittextS.getText().toString());
        editor.putString(Constants.CMLIN_S_UNIT, Integer.toString(spinnerS.getSelectedItemPosition()));
        editor.putString(Constants.CMLIN_L, edittextL.getText().toString());
        editor.putString(Constants.CMLIN_L_UNIT, Integer.toString(spinnerL.getSelectedItemPosition()));
        editor.putString(Constants.CMLIN_Z0, edittextZ0.getText().toString());
        editor.putString(Constants.CMLIN_Z0_UNIT, Integer.toString(spinnerZ0.getSelectedItemPosition()));
        editor.putString(Constants.CMLIN_K, edittextK.getText().toString());
        editor.putString(Constants.CMLIN_Z0E, edittextZ0e.getText().toString());
        editor.putString(Constants.CMLIN_Z0E_UNIT, Integer.toString(spinnerZ0e.getSelectedItemPosition()));
        editor.putString(Constants.CMLIN_Z0O_UNIT, Integer.toString(spinnerZ0o.getSelectedItemPosition()));
        editor.putString(Constants.CMLIN_PHS, edittextPhs.getText().toString());
        editor.putString(Constants.CMLIN_PHS_UNIT, Integer.toString(spinnerPhs.getSelectedItemPosition()));
        editor.putString(Constants.CMLIN_FREQ, edittextFreq.getText().toString());
        editor.putString(Constants.CMLIN_FREQ_UNIT, Integer.toString(spinnerFreq.getSelectedItemPosition()));
        editor.putString(Constants.CMLIN_ER, edittextEr.getText().toString());
        editor.putString(Constants.CMLIN_H, edittextH.getText().toString());
        editor.putString(Constants.CMLIN_H_UNIT, Integer.toString(spinnerH.getSelectedItemPosition()));

        if (useZ0k) {
            editor.putString(Constants.CMLIN_USEZ0K, "true");
        } else {
            editor.putString(Constants.CMLIN_USEZ0K, "false");
        }
        editor.apply();
    }

    private boolean analysisInputCheck() {
        boolean checkResult = true;
        if (edittextW.length() == 0) {
            edittextW.setError(getText(R.string.Error_W_empty));
            checkResult = false;
        } else if (Constants.value2meter(Double.parseDouble(edittextW.getText().toString()),
                spinnerW.getSelectedItemPosition()) < Constants.MINI_LIMIT) {
            edittextW.setError(getText(R.string.unreasonable_value));
            checkResult = false;
        }

        if (edittextS.length() == 0) {
            edittextS.setError(getText(R.string.Error_S_empty));
            checkResult = false;
        } else if (Constants.value2meter(Double.parseDouble(edittextS.getText().toString()),
                spinnerS.getSelectedItemPosition()) < Constants.MINI_LIMIT) {
            edittextS.setError(getText(R.string.unreasonable_value));
            checkResult = false;
        }

        if (edittextFreq.length() == 0) {
            edittextFreq.setError(getText(R.string.Error_Freq_empty));
            checkResult = false;
        } else if (Double.parseDouble(edittextFreq.getText().toString()) == 0) {
            edittextFreq.setError(getText(R.string.error_zero_frequency));
            checkResult = false;
        }

        if (edittextH.length() == 0) {
            edittextH.setError(getText(R.string.Error_H_empty));
            checkResult = false;
        } else if (Constants.value2meter(Double.parseDouble(edittextH.getText().toString()),
                spinnerH.getSelectedItemPosition()) < Constants.MINI_LIMIT) {
            edittextH.setError(getText(R.string.unreasonable_value));
            checkResult = false;
        }

        if (edittextEr.length() == 0) {
            edittextEr.setError(Constants.errorErEmpty(mContext));
            checkResult = false;
        } else if (Double.parseDouble(edittextEr.getText().toString()) < 1) {
            edittextEr.setError(getText(R.string.unreasonable_value));
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
            edittextFreq.setError(getText(R.string.Error_Freq_empty));
            checkResult = false;
        } else if (Double.parseDouble(edittextFreq.getText().toString()) == 0) {
            edittextFreq.setError(getText(R.string.error_zero_frequency));
            checkResult = false;
        }

        if (edittextH.length() == 0) {
            edittextH.setError(getText(R.string.Error_H_empty));
            checkResult = false;
        } else if (Constants.value2meter(Double.parseDouble(edittextH.getText().toString()),
                spinnerH.getSelectedItemPosition()) < Constants.MINI_LIMIT) {
            edittextH.setError(getText(R.string.unreasonable_value));
            checkResult = false;
        }

        if (edittextEr.length() == 0) {
            edittextEr.setError(Constants.errorErEmpty(mContext));
            checkResult = false;
        } else if (Double.parseDouble(edittextEr.getText().toString()) < 1) {
            edittextEr.setError(getText(R.string.unreasonable_value));
            checkResult = false;
        }

        if (useZ0k) {
            if (edittextZ0.length() == 0) {
                edittextZ0.setError(Constants.errorZ0Empty(mContext));
                checkResult = false;
            } else if (Double.parseDouble(edittextZ0.getText().toString()) == 0) {
                edittextZ0.setError(getText(R.string.unreasonable_value));
                checkResult = false;
            }

            if (edittextK.length() == 0) {
                edittextK.setError(getText(R.string.Error_k_empty));
                checkResult = false;
            } else if (Double.parseDouble(edittextK.getText().toString()) == 0) {
                edittextK.setError(getText(R.string.unreasonable_value));
                checkResult = false;
            }
        } else {
            if (edittextZ0e.length() == 0) {
                edittextZ0e.setError(Constants.errorZ0eEmpty(mContext));
                checkResult = false;
            } else if (Double.parseDouble(edittextZ0e.getText().toString()) == 0) {
                edittextZ0e.setError(getText(R.string.unreasonable_value));
                checkResult = false;
            }

            if (edittextZ0o.length() == 0) {
                edittextZ0o.setError(Constants.errorZ0oEmpty(mContext));
                checkResult = false;
            } else if (Double.parseDouble(edittextZ0o.getText().toString()) == 0) {
                edittextZ0o.setError(getText(R.string.unreasonable_value));
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
        line.setMetalThick(0, spinnerT.getSelectedItemPosition());

        double W, S, L;
        if (edittextPhs.length() != 0) {
            line.setPhase(Double.parseDouble(edittextPhs.getText().toString()));
        } else {
            line.setPhase(0);
        }
        CmlinCalculator cmlin = new CmlinCalculator();
        line = cmlin.getSynResult(line, useZ0k);

        if (line.getErrorCode() == Constants.ERROR.NO_ERROR) {
            if (edittextPhs.length() != 0) {
                BigDecimal L_temp = new BigDecimal(
                        Constants.meter2others(line.getMetalLength(), spinnerL.getSelectedItemPosition()));
                L = L_temp.setScale(Constants.DecimalLength, BigDecimal.ROUND_HALF_UP).doubleValue();
                edittextL.setText(String.valueOf(L));
            } else {
                edittextL.setText("");
            }

            if ((Double.isNaN(line.getMetalWidth()) || Double.isInfinite(line.getMetalWidth()))
                    || (Double.isNaN(line.getMetalSpace()) || Double.isInfinite(line.getMetalSpace()))) {
                edittextW.setText("");
                edittextW.setError(getString(R.string.synthesize_failed));
                edittextW.requestFocus();
                edittextS.setText("");
                edittextS.setError(getString(R.string.synthesize_failed));
                edittextL.setText("");
            } else {
                BigDecimal W_temp = new BigDecimal(
                        Constants.meter2others(line.getMetalWidth(), spinnerW.getSelectedItemPosition()));
                W = W_temp.setScale(Constants.DecimalLength, BigDecimal.ROUND_HALF_UP).doubleValue();
                edittextW.setText(String.valueOf(W));

                BigDecimal S_temp = new BigDecimal(
                        Constants.meter2others(line.getMetalSpace(), spinnerS.getSelectedItemPosition()));
                S = S_temp.setScale(Constants.DecimalLength, BigDecimal.ROUND_HALF_UP).doubleValue();
                edittextS.setText(String.valueOf(S));
            }

            if (useZ0k) {
                double Z0e, Z0o;

                BigDecimal Z0o_temp = new BigDecimal(line.getImpedanceOdd());
                Z0o = Z0o_temp.setScale(Constants.DecimalLength, BigDecimal.ROUND_HALF_UP).doubleValue();
                edittextZ0o.setText(String.valueOf(Z0o));

                BigDecimal Z0e_temp = new BigDecimal(line.getImpedanceEven());
                Z0e = Z0e_temp.setScale(Constants.DecimalLength, BigDecimal.ROUND_HALF_UP).doubleValue();
                edittextZ0e.setText(String.valueOf(Z0e));
            } else {
                double Z0, k;

                BigDecimal Z0_temp = new BigDecimal(line.getImpedance());
                Z0 = Z0_temp.setScale(Constants.DecimalLength, BigDecimal.ROUND_HALF_UP).doubleValue();
                edittextZ0.setText(String.valueOf(Z0)); // cut the decimal of the Z0
                BigDecimal k_temp = new BigDecimal(line.getCouplingFactor());
                k = k_temp.setScale(Constants.DecimalLength, BigDecimal.ROUND_HALF_UP).doubleValue();
                edittextK.setText(String.valueOf(k));
            }
        } else if (line.getErrorCode() == Constants.ERROR.K_OUT_OF_RANGE) {
            edittextW.setText("");
            edittextS.setText("");
            edittextL.setText("");
            edittextK.setError(getString(R.string.k_out_of_range));
            edittextK.requestFocus();
        } else if (line.getErrorCode() == Constants.ERROR.Z0E_Z0O_MISTAKE) {
            edittextW.setText("");
            edittextS.setText("");
            edittextL.setText("");
            edittextZ0o.setError(getString(R.string.Z0e_larger_Z0o));
            edittextZ0o.requestFocus();
            edittextZ0e.setError(getString(R.string.Z0e_larger_Z0o));
        } else {
            edittextW.setText("");
            edittextW.setError(getString(R.string.synthesize_failed));
            edittextW.requestFocus();
            edittextS.setText("");
            edittextS.setError(getString(R.string.synthesize_failed));
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
        edittextW.setError(null);
        edittextS.setError(null);
        edittextH.setError(null);
        edittextZ0.setError(null);
        edittextK.setError(null);
        edittextZ0e.setError(null);
        edittextZ0o.setError(null);
        edittextEr.setError(null);
        edittextFreq.setError(null);
    }
}
