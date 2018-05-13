package com.rookiedev.microwavetools.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
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
import com.rookiedev.microwavetools.libs.CpwCalculator;
import com.rookiedev.microwavetools.libs.CpwModel;

import java.math.BigDecimal;
import java.util.Objects;

public class CpwFragment extends Fragment {
    private Context mContext;
    private View viewRoot;
    // private TextView textH, textW, textS;
    private TextInputEditText edittextW, edittextS, edittextL, edittextZ0, edittextPhs, edittextFreq, edittextT,
            edittextH, edittextEr;
    private TextInputLayout textInputLayoutT, textInputLayoutH, textInputLayoutW, textInputLayoutS, textInputLayoutZ0,
            textInputLayoutEr, textInputLayoutF;
    private Button buttonSynthesize, buttonAnalyze;
    private Spinner spinnerW, spinnerS, spinnerL, spinnerT, spinnerH, spinnerZ0, spinnerPhs, spinnerFreq;
    private int target;
    private RadioButton radioButtonW, radioButtonS, radioButtonH;
    private boolean withGround;
    private CpwModel line;
    private ColorStateList defaultEdittextColor;
    private AdFragment adFragment = null;
    private FragmentManager fragmentManager = null;

    private boolean isAdFree;

    public CpwFragment() {
        // Empty constructor required for fragment subclasses
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            withGround = Objects.equals(getArguments().getString(Constants.PARAMS_CPW), Constants.GROUNDED_CPW);
            isAdFree = getArguments().getBoolean(Constants.IS_AD_FREE, true);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        viewRoot = inflater.inflate(R.layout.fragment_cpw, container, false);
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
                    if (!withGround) {
                        line.setMetalThick(Double.parseDouble(edittextT.getText().toString()),
                                spinnerT.getSelectedItemPosition());
                    } else {
                        line.setMetalThick(0, spinnerT.getSelectedItemPosition());
                    }

                    if (edittextL.length() != 0) {
                        line.setMetalLength(Double.parseDouble(edittextL.getText().toString()),
                                spinnerL.getSelectedItemPosition());
                    } else {
                        line.setMetalLength(0, spinnerL.getSelectedItemPosition());
                    }

                    CpwCalculator cpwg = new CpwCalculator();
                    line = cpwg.getAnaResult(line, withGround);

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
                    line.setImpedance(Double.parseDouble(edittextZ0.getText().toString()));
                    line.setFrequency(Double.parseDouble(edittextFreq.getText().toString()),
                            spinnerFreq.getSelectedItemPosition());
                    if (!withGround) {
                        line.setMetalThick(Double.parseDouble(edittextT.getText().toString()),
                                spinnerT.getSelectedItemPosition());
                    } else {
                        line.setMetalThick(0, spinnerT.getSelectedItemPosition());
                    }

                    if (target == Constants.Synthesize_Width) {
                        line.setSubHeight(Double.parseDouble(edittextH.getText().toString()),
                                spinnerH.getSelectedItemPosition());
                        line.setMetalSpace(Double.parseDouble(edittextS.getText().toString()),
                                spinnerS.getSelectedItemPosition());
                        line.setSubEpsilon(Double.parseDouble(edittextEr.getText().toString()));

                        // W = 0;
                    } else if (target == Constants.Synthesize_Gap) {
                        line.setSubHeight(Double.parseDouble(edittextH.getText().toString()),
                                spinnerH.getSelectedItemPosition());
                        line.setMetalWidth(Double.parseDouble(edittextW.getText().toString()),
                                spinnerW.getSelectedItemPosition());
                        line.setSubEpsilon(Double.parseDouble(edittextEr.getText().toString()));

                        // S = 0;
                    } else if (target == Constants.Synthesize_Height) {
                        line.setMetalSpace(Double.parseDouble(edittextS.getText().toString()),
                                spinnerS.getSelectedItemPosition());
                        line.setMetalWidth(Double.parseDouble(edittextW.getText().toString()),
                                spinnerW.getSelectedItemPosition());
                        line.setSubEpsilon(Double.parseDouble(edittextEr.getText().toString()));

                        // H = 0;
                    }

                    if (edittextPhs.length() != 0) {
                        line.setPhase(Double.parseDouble(edittextPhs.getText().toString()));
                    } else {
                        line.setPhase(0);
                    }

                    CpwCalculator cpwg = new CpwCalculator();
                    line = cpwg.getSynResult(line, target, withGround);

                    if (line.getErrorCode() == Constants.ERROR.NO_ERROR) {
                        if (edittextPhs.length() != 0) {
                            BigDecimal L_temp = new BigDecimal(
                                    Constants.meter2others(line.getMetalLength(), spinnerL.getSelectedItemPosition()));
                            double L = L_temp.setScale(Constants.DecimalLength, BigDecimal.ROUND_HALF_UP).doubleValue();
                            edittextL.setText(String.valueOf(L));
                        } else {
                            edittextL.setText("");
                        }

                        if (target == Constants.Synthesize_Width) {
                            if ((Double.isNaN(line.getMetalWidth()) || Double.isInfinite(line.getMetalWidth()))) {
                                edittextW.setText("");
                                textInputLayoutW.setError(getString(R.string.synthesize_failed));
                                edittextW.requestFocus();
                            } else {
                                BigDecimal W_temp = new BigDecimal(Constants.meter2others(line.getMetalWidth(),
                                        spinnerW.getSelectedItemPosition()));
                                double W = W_temp.setScale(Constants.DecimalLength, BigDecimal.ROUND_HALF_UP)
                                        .doubleValue();
                                edittextW.setText(String.valueOf(W));
                            }
                        } else if (target == Constants.Synthesize_Gap) {
                            if ((Double.isNaN(line.getMetalSpace()) || Double.isInfinite(line.getMetalSpace()))) {
                                edittextS.setText("");
                                textInputLayoutS.setError(getString(R.string.synthesize_failed));
                                edittextS.requestFocus();
                            } else {
                                BigDecimal S_temp = new BigDecimal(Constants.meter2others(line.getMetalSpace(),
                                        spinnerS.getSelectedItemPosition()));
                                double S = S_temp.setScale(Constants.DecimalLength, BigDecimal.ROUND_HALF_UP)
                                        .doubleValue();
                                edittextS.setText(String.valueOf(S));
                            }
                        } else if (target == Constants.Synthesize_Height) {
                            if ((Double.isNaN(line.getSubHeight()) || Double.isInfinite(line.getSubHeight()))) {
                                edittextH.setText("");
                                textInputLayoutH.setError(getString(R.string.synthesize_failed));
                                edittextH.requestFocus();
                            } else {
                                BigDecimal H_temp = new BigDecimal(Constants.meter2others(line.getSubHeight(),
                                        spinnerH.getSelectedItemPosition()));
                                double H = H_temp.setScale(Constants.DecimalLength, BigDecimal.ROUND_HALF_UP)
                                        .doubleValue();
                                edittextH.setText(String.valueOf(H));
                            }
                        }
                    } else {
                        if (target == Constants.Synthesize_Width) {
                            edittextW.setText("");
                            textInputLayoutW.setError(getString(R.string.synthesize_failed));
                            edittextW.requestFocus();
                        } else if (target == Constants.Synthesize_Gap) {
                            edittextS.setText("");
                            textInputLayoutS.setError(getString(R.string.synthesize_failed));
                            edittextS.requestFocus();
                        } else if (target == Constants.Synthesize_Height) {
                            edittextH.setText("");
                            textInputLayoutH.setError(getString(R.string.synthesize_failed));
                            edittextH.requestFocus();
                        }
                    }
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
        line = new CpwModel();

        RadioButton radioButtonL = viewRoot.findViewById(R.id.radioBtn_L);
        radioButtonL.setVisibility(View.VISIBLE);
        radioButtonL.setChecked(true);

        RadioButton radioButtonPhs = viewRoot.findViewById(R.id.radioBtn_Phs);
        radioButtonPhs.setVisibility(View.VISIBLE);
        radioButtonPhs.setChecked(true);

        RadioButton radioButtonZ0 = viewRoot.findViewById(R.id.radioBtn_Z0);
        radioButtonZ0.setVisibility(View.VISIBLE);
        radioButtonZ0.setChecked(true);

        radioButtonW = viewRoot.findViewById(R.id.radioBtn_W);
        radioButtonW.setVisibility(View.VISIBLE);
        radioButtonS = viewRoot.findViewById(R.id.radioBtn_S);
        radioButtonS.setVisibility(View.VISIBLE);
        radioButtonH = viewRoot.findViewById(R.id.radioBtn_H);
        radioButtonH.setVisibility(View.VISIBLE);

        textInputLayoutW = viewRoot.findViewById(R.id.text_input_layout_W);
        edittextW = viewRoot.findViewById(R.id.editText_W);
        defaultEdittextColor = edittextW.getTextColors();
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
        edittextZ0.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor));
        edittextZ0.setBackgroundTintList(getResources().getColorStateList(R.color.background_tint_analyze));
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
        if (withGround) {
            edittextT.setText("0");
            edittextT.setEnabled(false);
        }
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

        // configure the electrical length units
        spinnerPhs.setAdapter(Constants.adapterPhaseUnits(mContext));

        // configure the frequency units
        spinnerFreq.setAdapter(Constants.adapterFrequencyUnits(mContext));
    }

    private void readSharedPref() {
        SharedPreferences prefs = mContext.getSharedPreferences(Constants.SHARED_PREFS_NAME,
                AppCompatActivity.MODE_PRIVATE);

        if (withGround) {
            edittextW.setText(prefs.getString(Constants.GCPW_W, "1"));
            spinnerW.setSelection(Integer
                    .parseInt(prefs.getString(Constants.GCPW_W_UNIT, Integer.toString(Constants.LengthUnit_mm))));

            edittextS.setText(prefs.getString(Constants.GCPW_S, "0.15"));
            spinnerS.setSelection(Integer
                    .parseInt(prefs.getString(Constants.GCPW_S_UNIT, Integer.toString(Constants.LengthUnit_mm))));

            edittextL.setText(prefs.getString(Constants.GCPW_L, "44.38"));
            spinnerL.setSelection(Integer
                    .parseInt(prefs.getString(Constants.GCPW_L_UNIT, Integer.toString(Constants.LengthUnit_mm))));

            edittextZ0.setText(prefs.getString(Constants.GCPW_Z0, "50.00"));
            spinnerZ0.setSelection(Integer
                    .parseInt(prefs.getString(Constants.GCPW_Z0_UNIT, Integer.toString(Constants.ImpedanceUnit_Ohm))));

            edittextPhs.setText(prefs.getString(Constants.GCPW_PHS, "90.00"));
            spinnerPhs.setSelection(Integer
                    .parseInt(prefs.getString(Constants.GCPW_PHS_UNIT, Integer.toString(Constants.PhaseUnit_Degree))));

            edittextFreq.setText(prefs.getString(Constants.GCPW_FREQ, "1.00"));
            spinnerFreq.setSelection(Integer
                    .parseInt(prefs.getString(Constants.GCPW_FREQ_UNIT, Integer.toString(Constants.FreqUnit_GHz))));

            edittextEr.setText(prefs.getString(Constants.GCPW_ER, "4.6"));

            edittextH.setText(prefs.getString(Constants.GCPW_H, "1.6"));
            spinnerH.setSelection(Integer
                    .parseInt(prefs.getString(Constants.GCPW_H_UNIT, Integer.toString(Constants.LengthUnit_mm))));

            // edittextT.setText(prefs.getString(Constants.GCPW_T, "0"));
            // spinnerT.setSelection(Integer.parseInt(prefs.getString(Constants.GCPW_T_UNIT,
            // Integer.toString(Constants.LengthUnit_mm))));
            target = Integer.parseInt(prefs.getString(Constants.GCPW_TARGET, "0"));
        } else {
            edittextW.setText(prefs.getString(Constants.CPW_W, "1"));
            spinnerW.setSelection(
                    Integer.parseInt(prefs.getString(Constants.CPW_W_UNIT, Integer.toString(Constants.LengthUnit_mm))));

            edittextS.setText(prefs.getString(Constants.CPW_S, "0.244"));
            spinnerS.setSelection(
                    Integer.parseInt(prefs.getString(Constants.CPW_S_UNIT, Integer.toString(Constants.LengthUnit_mm))));

            edittextL.setText(prefs.getString(Constants.CPW_L, "44.464"));
            spinnerL.setSelection(
                    Integer.parseInt(prefs.getString(Constants.CPW_L_UNIT, Integer.toString(Constants.LengthUnit_mm))));

            edittextZ0.setText(prefs.getString(Constants.CPW_Z0, "50"));
            spinnerZ0.setSelection(Integer
                    .parseInt(prefs.getString(Constants.CPW_Z0_UNIT, Integer.toString(Constants.ImpedanceUnit_Ohm))));

            edittextPhs.setText(prefs.getString(Constants.CPW_PHS, "90"));
            spinnerPhs.setSelection(Integer
                    .parseInt(prefs.getString(Constants.CPW_PHS_UNIT, Integer.toString(Constants.PhaseUnit_Degree))));

            edittextFreq.setText(prefs.getString(Constants.CPW_FREQ, "1.00"));
            spinnerFreq.setSelection(Integer
                    .parseInt(prefs.getString(Constants.CPW_FREQ_UNIT, Integer.toString(Constants.FreqUnit_GHz))));

            edittextEr.setText(prefs.getString(Constants.CPW_ER, "4.6"));

            edittextH.setText(prefs.getString(Constants.CPW_H, "1.6"));
            spinnerH.setSelection(
                    Integer.parseInt(prefs.getString(Constants.CPW_H_UNIT, Integer.toString(Constants.LengthUnit_mm))));

            edittextT.setText(prefs.getString(Constants.CPW_T, "0.035"));
            spinnerT.setSelection(
                    Integer.parseInt(prefs.getString(Constants.CPW_T_UNIT, Integer.toString(Constants.LengthUnit_mm))));
            target = Integer.parseInt(prefs.getString(Constants.CPW_TARGET, "0"));
        }
    }

    private void setRadioBtn() {
        if (target == Constants.Synthesize_Width) {
            radioButtonW.setChecked(true);
            edittextW.setBackgroundTintList(getResources().getColorStateList(R.color.background_tint_synthersize));
            edittextW.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
            radioButtonS.setChecked(false);
            edittextS.setBackgroundTintList(getResources().getColorStateList(R.color.background_tint_default));
            edittextS.setTextColor(defaultEdittextColor);
            radioButtonH.setChecked(false);
            edittextH.setBackgroundTintList(getResources().getColorStateList(R.color.background_tint_default));
            edittextH.setTextColor(defaultEdittextColor);
        } else if (target == Constants.Synthesize_Gap) {
            radioButtonW.setChecked(false);
            edittextW.setBackgroundTintList(getResources().getColorStateList(R.color.background_tint_default));
            edittextW.setTextColor(defaultEdittextColor);
            radioButtonS.setChecked(true);
            edittextS.setBackgroundTintList(getResources().getColorStateList(R.color.background_tint_synthersize));
            edittextS.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
            radioButtonH.setChecked(false);
            edittextH.setBackgroundTintList(getResources().getColorStateList(R.color.background_tint_default));
            edittextH.setTextColor(defaultEdittextColor);
        } else if (target == Constants.Synthesize_Height) {
            radioButtonW.setChecked(false);
            edittextW.setBackgroundTintList(getResources().getColorStateList(R.color.background_tint_default));
            edittextW.setTextColor(defaultEdittextColor);
            radioButtonS.setChecked(false);
            edittextS.setBackgroundTintList(getResources().getColorStateList(R.color.background_tint_default));
            edittextS.setTextColor(defaultEdittextColor);
            radioButtonH.setChecked(true);
            edittextH.setBackgroundTintList(getResources().getColorStateList(R.color.background_tint_synthersize));
            edittextH.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
        } else {
            radioButtonW.setChecked(true);
            edittextW.setBackgroundTintList(getResources().getColorStateList(R.color.background_tint_synthersize));
            edittextW.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
            radioButtonS.setChecked(false);
            edittextS.setBackgroundTintList(getResources().getColorStateList(R.color.background_tint_default));
            edittextS.setTextColor(defaultEdittextColor);
            radioButtonH.setChecked(false);
            edittextH.setBackgroundTintList(getResources().getColorStateList(R.color.background_tint_default));
            edittextH.setTextColor(defaultEdittextColor);
            target = Constants.Synthesize_Width;
        }
        radioButtonW.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                radioButtonW.setChecked(true);
                edittextW.setBackgroundTintList(getResources().getColorStateList(R.color.background_tint_synthersize));
                edittextW.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
                radioButtonS.setChecked(false);
                edittextS.setBackgroundTintList(getResources().getColorStateList(R.color.background_tint_default));
                edittextS.setTextColor(defaultEdittextColor);
                radioButtonH.setChecked(false);
                edittextH.setBackgroundTintList(getResources().getColorStateList(R.color.background_tint_default));
                edittextH.setTextColor(defaultEdittextColor);
                target = Constants.Synthesize_Width;
            }
        });
        radioButtonS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                radioButtonW.setChecked(false);
                edittextW.setBackgroundTintList(getResources().getColorStateList(R.color.background_tint_default));
                edittextW.setTextColor(defaultEdittextColor);
                radioButtonS.setChecked(true);
                edittextS.setBackgroundTintList(getResources().getColorStateList(R.color.background_tint_synthersize));
                edittextS.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
                radioButtonH.setChecked(false);
                edittextH.setBackgroundTintList(getResources().getColorStateList(R.color.background_tint_default));
                edittextH.setTextColor(defaultEdittextColor);
                target = Constants.Synthesize_Gap;
            }
        });
        radioButtonH.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                radioButtonW.setChecked(false);
                edittextW.setBackgroundTintList(getResources().getColorStateList(R.color.background_tint_default));
                edittextW.setTextColor(defaultEdittextColor);
                radioButtonS.setChecked(false);
                edittextS.setBackgroundTintList(getResources().getColorStateList(R.color.background_tint_default));
                edittextS.setTextColor(defaultEdittextColor);
                radioButtonH.setChecked(true);
                edittextH.setBackgroundTintList(getResources().getColorStateList(R.color.background_tint_synthersize));
                edittextH.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
                target = Constants.Synthesize_Height;
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        SharedPreferences prefs = mContext.getSharedPreferences(Constants.SHARED_PREFS_NAME,
                AppCompatActivity.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        if (withGround) {
            editor.putString(Constants.GCPW_W, edittextW.getText().toString());
            editor.putString(Constants.GCPW_W_UNIT, Integer.toString(spinnerW.getSelectedItemPosition()));
            editor.putString(Constants.GCPW_S, edittextS.getText().toString());
            editor.putString(Constants.GCPW_S_UNIT, Integer.toString(spinnerS.getSelectedItemPosition()));
            editor.putString(Constants.GCPW_H, edittextH.getText().toString());
            editor.putString(Constants.GCPW_H_UNIT, Integer.toString(spinnerH.getSelectedItemPosition()));
            editor.putString(Constants.GCPW_ER, edittextEr.getText().toString());
            editor.putString(Constants.GCPW_L, edittextL.getText().toString());
            editor.putString(Constants.GCPW_L_UNIT, Integer.toString(spinnerL.getSelectedItemPosition()));
            editor.putString(Constants.GCPW_Z0, edittextZ0.getText().toString());
            editor.putString(Constants.GCPW_Z0_UNIT, Integer.toString(spinnerZ0.getSelectedItemPosition()));
            editor.putString(Constants.GCPW_PHS, edittextPhs.getText().toString());
            editor.putString(Constants.GCPW_PHS_UNIT, Integer.toString(spinnerPhs.getSelectedItemPosition()));
            editor.putString(Constants.GCPW_FREQ, edittextFreq.getText().toString());
            editor.putString(Constants.GCPW_FREQ_UNIT, Integer.toString(spinnerFreq.getSelectedItemPosition()));
            editor.putString(Constants.GCPW_T, edittextT.getText().toString());
            editor.putString(Constants.GCPW_T_UNIT, Integer.toString(spinnerT.getSelectedItemPosition()));
            editor.putString(Constants.GCPW_TARGET, Integer.toString(target));
        } else {
            editor.putString(Constants.CPW_W, edittextW.getText().toString());
            editor.putString(Constants.CPW_W_UNIT, Integer.toString(spinnerW.getSelectedItemPosition()));
            editor.putString(Constants.CPW_S, edittextS.getText().toString());
            editor.putString(Constants.CPW_S_UNIT, Integer.toString(spinnerS.getSelectedItemPosition()));
            editor.putString(Constants.CPW_H, edittextH.getText().toString());
            editor.putString(Constants.CPW_H_UNIT, Integer.toString(spinnerH.getSelectedItemPosition()));
            editor.putString(Constants.CPW_ER, edittextEr.getText().toString());
            editor.putString(Constants.CPW_L, edittextL.getText().toString());
            editor.putString(Constants.CPW_L_UNIT, Integer.toString(spinnerL.getSelectedItemPosition()));
            editor.putString(Constants.CPW_Z0, edittextZ0.getText().toString());
            editor.putString(Constants.CPW_Z0_UNIT, Integer.toString(spinnerZ0.getSelectedItemPosition()));
            editor.putString(Constants.CPW_PHS, edittextPhs.getText().toString());
            editor.putString(Constants.CPW_PHS_UNIT, Integer.toString(spinnerPhs.getSelectedItemPosition()));
            editor.putString(Constants.CPW_FREQ, edittextFreq.getText().toString());
            editor.putString(Constants.CPW_FREQ_UNIT, Integer.toString(spinnerFreq.getSelectedItemPosition()));
            editor.putString(Constants.CPW_T, edittextT.getText().toString());
            editor.putString(Constants.CPW_T_UNIT, Integer.toString(spinnerT.getSelectedItemPosition()));
            editor.putString(Constants.CPW_TARGET, Integer.toString(target));
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
        if (!withGround) {
            if (edittextT.length() == 0) {
                textInputLayoutT.setError(getText(R.string.Error_T_empty));
                checkResult = false;
            }
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
        }

        return checkResult;
    }

    private boolean synthesizeInputCheck() {
        boolean checkResult = true;
        if (edittextZ0.length() == 0) {
            textInputLayoutZ0.setError(Constants.errorZ0Empty(mContext));
            checkResult = false;
        } else if (Double.parseDouble(edittextZ0.getText().toString()) == 0) {
            textInputLayoutZ0.setError(getText(R.string.unreasonable_value));
            checkResult = false;
        }

        if (edittextFreq.length() == 0) {
            textInputLayoutF.setError(getText(R.string.Error_Freq_empty));
            checkResult = false;
        } else if (Double.parseDouble(edittextFreq.getText().toString()) == 0) {
            textInputLayoutF.setError(getText(R.string.error_zero_frequency));
            checkResult = false;
        }

        if (edittextEr.length() == 0) {
            textInputLayoutEr.setError(Constants.errorErEmpty(mContext));
            checkResult = false;
        } else if (Double.parseDouble(edittextEr.getText().toString()) < 1) {
            textInputLayoutEr.setError(getText(R.string.unreasonable_value));
            checkResult = false;
        }

        if (!withGround) {
            if (edittextT.length() == 0) {
                textInputLayoutT.setError(getText(R.string.Error_T_empty));
                checkResult = false;
            }
        }

        if (target == Constants.Synthesize_Width) {
            if (edittextS.length() == 0) {
                textInputLayoutS.setError(getText(R.string.Error_S_empty));
                checkResult = false;
            } else if (Constants.value2meter(Double.parseDouble(edittextS.getText().toString()),
                    spinnerS.getSelectedItemPosition()) < Constants.MINI_LIMIT) {
                textInputLayoutS.setError(getText(R.string.unreasonable_value));
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

            if (!checkResult) {
                edittextW.setText("");
            }
        } else if (target == Constants.Synthesize_Gap) {
            if (edittextW.length() == 0) {
                textInputLayoutW.setError(getText(R.string.Error_W_empty));
                checkResult = false;
            } else if (Constants.value2meter(Double.parseDouble(edittextW.getText().toString()),
                    spinnerW.getSelectedItemPosition()) < Constants.MINI_LIMIT) {
                textInputLayoutW.setError(getText(R.string.unreasonable_value));
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

            if (!checkResult) {
                edittextS.setText("");
            }
        } else if (target == Constants.Synthesize_Height) {
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

            if (!checkResult) {
                edittextH.setText("");
            }
        }

        return checkResult;
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
        textInputLayoutEr.setError(null);
        textInputLayoutEr.setErrorEnabled(false);
        textInputLayoutF.setError(null);
        textInputLayoutF.setErrorEnabled(false);
        textInputLayoutT.setError(null);
        textInputLayoutT.setErrorEnabled(false);
    }
}
