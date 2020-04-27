package com.rookiedev.microwavetools.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
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
    private TextInputEditText editTextW, editTextG, editTextL, editTextZ0, editTextPhs, editTextFreq, editTextT,
            editTextH, editTextEr;
    private TextInputLayout textInputLayoutT, textInputLayoutH, textInputLayoutW, textInputLayoutG, textInputLayoutZ0,
            textInputLayoutEr, textInputLayoutF;
    private Button buttonSynthesize, buttonAnalyze;
    private AutoCompleteTextView spinnerW, spinnerG, spinnerL, spinnerT, spinnerH, spinnerZ0, spinnerPhs, spinnerFreq;
    private int target;
    private RadioButton radioButtonW, radioButtonG, radioButtonH;
    private boolean withGround;
    private CpwModel line;
    private ColorStateList defaultEditTextColor;
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
                    line.setMetalWidth(Double.parseDouble(editTextW.getText().toString()),
                            spinnerW.getText().toString());
                    line.setMetalSpace(Double.parseDouble(editTextG.getText().toString()),
                            spinnerG.getText().toString());
                    line.setFrequency(Double.parseDouble(editTextFreq.getText().toString()),
                            spinnerFreq.getText().toString());
                    line.setSubEpsilon(Double.parseDouble(editTextEr.getText().toString()));
                    line.setSubHeight(Double.parseDouble(editTextH.getText().toString()),
                            spinnerH.getText().toString());
                    if (!withGround) {
                        line.setMetalThick(Double.parseDouble(editTextT.getText().toString()),
                                spinnerT.getText().toString());
                    } else {
                        line.setMetalThick(0, spinnerT.getText().toString());
                    }

                    if (editTextL.length() != 0) {
                        line.setMetalLength(Double.parseDouble(editTextL.getText().toString()),
                                spinnerL.getText().toString());
                    } else {
                        line.setMetalLength(0, spinnerL.getText().toString());
                    }

                    CpwCalculator cpwg = new CpwCalculator();
                    line = cpwg.getAnaResult(line, withGround);

                    if (editTextL.length() != 0) {
                        BigDecimal Eeff_temp = new BigDecimal(line.getPhase());
                        double Eeff = Eeff_temp.setScale(Constants.DecimalLength, BigDecimal.ROUND_HALF_UP)
                                .doubleValue();
                        editTextPhs.setText(String.valueOf(Eeff));
                    } else {
                        editTextPhs.setText("");
                    }

                    BigDecimal Z0_temp = new BigDecimal(line.getImpedance());
                    double Z0 = Z0_temp.setScale(Constants.DecimalLength, BigDecimal.ROUND_HALF_UP).doubleValue();
                    editTextZ0.setText(String.valueOf(Z0));
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
                    line.setImpedance(Double.parseDouble(editTextZ0.getText().toString()));
                    line.setFrequency(Double.parseDouble(editTextFreq.getText().toString()),
                            spinnerFreq.getText().toString());
                    if (!withGround) {
                        line.setMetalThick(Double.parseDouble(editTextT.getText().toString()),
                                spinnerT.getText().toString());
                    } else {
                        line.setMetalThick(0, spinnerT.getText().toString());
                    }

                    if (target == Constants.Synthesize_Width) {
                        line.setSubHeight(Double.parseDouble(editTextH.getText().toString()),
                                spinnerH.getText().toString());
                        line.setMetalSpace(Double.parseDouble(editTextG.getText().toString()),
                                spinnerG.getText().toString());
                        line.setSubEpsilon(Double.parseDouble(editTextEr.getText().toString()));

                        // W = 0;
                    } else if (target == Constants.Synthesize_Gap) {
                        line.setSubHeight(Double.parseDouble(editTextH.getText().toString()),
                                spinnerH.getText().toString());
                        line.setMetalWidth(Double.parseDouble(editTextW.getText().toString()),
                                spinnerW.getText().toString());
                        line.setSubEpsilon(Double.parseDouble(editTextEr.getText().toString()));

                        // S = 0;
                    } else if (target == Constants.Synthesize_Height) {
                        line.setMetalSpace(Double.parseDouble(editTextG.getText().toString()),
                                spinnerG.getText().toString());
                        line.setMetalWidth(Double.parseDouble(editTextW.getText().toString()),
                                spinnerW.getText().toString());
                        line.setSubEpsilon(Double.parseDouble(editTextEr.getText().toString()));

                        // H = 0;
                    }

                    if (editTextPhs.length() != 0) {
                        line.setPhase(Double.parseDouble(editTextPhs.getText().toString()));
                    } else {
                        line.setPhase(0);
                    }

                    CpwCalculator cpwg = new CpwCalculator();
                    line = cpwg.getSynResult(line, target, withGround);

                    if (line.getErrorCode() == Constants.ERROR.NO_ERROR) {
                        if (editTextPhs.length() != 0) {
                            BigDecimal L_temp = new BigDecimal(
                                    Constants.meter2others(line.getMetalLength(), spinnerL.getText().toString()));
                            double L = L_temp.setScale(Constants.DecimalLength, BigDecimal.ROUND_HALF_UP).doubleValue();
                            editTextL.setText(String.valueOf(L));
                        } else {
                            editTextL.setText("");
                        }

                        if (target == Constants.Synthesize_Width) {
                            if ((Double.isNaN(line.getMetalWidth()) || Double.isInfinite(line.getMetalWidth()))) {
                                editTextW.setText("");
                                textInputLayoutW.setError(getString(R.string.synthesize_failed));
                                editTextW.requestFocus();
                            } else {
                                BigDecimal W_temp = new BigDecimal(Constants.meter2others(line.getMetalWidth(),
                                        spinnerW.getText().toString()));
                                double W = W_temp.setScale(Constants.DecimalLength, BigDecimal.ROUND_HALF_UP)
                                        .doubleValue();
                                editTextW.setText(String.valueOf(W));
                            }
                        } else if (target == Constants.Synthesize_Gap) {
                            if ((Double.isNaN(line.getMetalSpace()) || Double.isInfinite(line.getMetalSpace()))) {
                                editTextG.setText("");
                                textInputLayoutG.setError(getString(R.string.synthesize_failed));
                                editTextG.requestFocus();
                            } else {
                                BigDecimal S_temp = new BigDecimal(Constants.meter2others(line.getMetalSpace(),
                                        spinnerG.getText().toString()));
                                double S = S_temp.setScale(Constants.DecimalLength, BigDecimal.ROUND_HALF_UP)
                                        .doubleValue();
                                editTextG.setText(String.valueOf(S));
                            }
                        } else if (target == Constants.Synthesize_Height) {
                            if ((Double.isNaN(line.getSubHeight()) || Double.isInfinite(line.getSubHeight()))) {
                                editTextH.setText("");
                                textInputLayoutH.setError(getString(R.string.synthesize_failed));
                                editTextH.requestFocus();
                            } else {
                                BigDecimal H_temp = new BigDecimal(Constants.meter2others(line.getSubHeight(),
                                        spinnerH.getText().toString()));
                                double H = H_temp.setScale(Constants.DecimalLength, BigDecimal.ROUND_HALF_UP)
                                        .doubleValue();
                                editTextH.setText(String.valueOf(H));
                            }
                        }
                    } else {
                        if (target == Constants.Synthesize_Width) {
                            editTextW.setText("");
                            textInputLayoutW.setError(getString(R.string.synthesize_failed));
                            editTextW.requestFocus();
                        } else if (target == Constants.Synthesize_Gap) {
                            editTextG.setText("");
                            textInputLayoutG.setError(getString(R.string.synthesize_failed));
                            editTextG.requestFocus();
                        } else if (target == Constants.Synthesize_Height) {
                            editTextH.setText("");
                            textInputLayoutH.setError(getString(R.string.synthesize_failed));
                            editTextH.requestFocus();
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
        radioButtonG = viewRoot.findViewById(R.id.radioBtn_g);
        radioButtonG.setVisibility(View.VISIBLE);
        radioButtonH = viewRoot.findViewById(R.id.radioBtn_H);
        radioButtonH.setVisibility(View.VISIBLE);

        textInputLayoutW = viewRoot.findViewById(R.id.text_input_layout_W);
        editTextW = viewRoot.findViewById(R.id.editText_W);
        defaultEditTextColor = editTextW.getTextColors();
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
        editTextL.setBackgroundTintList(getResources().getColorStateList(R.color.background_tint_synthesize));

        textInputLayoutZ0 = viewRoot.findViewById(R.id.text_input_layout_Z0);
        editTextZ0 = viewRoot.findViewById(R.id.editText_Z0);
        editTextZ0.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor));
        editTextZ0.setBackgroundTintList(getResources().getColorStateList(R.color.background_tint_analyze));
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

        editTextPhs = viewRoot.findViewById(R.id.editText_Phs);
        editTextPhs.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor));
        editTextPhs.setBackgroundTintList(getResources().getColorStateList(R.color.background_tint_analyze));

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

        textInputLayoutT = viewRoot.findViewById(R.id.text_input_layout_T);
        editTextT = viewRoot.findViewById(R.id.editText_T);
        if (withGround) {
            editTextT.setText("0");
            editTextT.setEnabled(false);
        }
        editTextT.addTextChangedListener(new TextWatcher() {
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

        // configure the electrical length units
        spinnerPhs.setAdapter(Constants.adapterPhaseUnits(mContext));

        // configure the frequency units
        spinnerFreq.setAdapter(Constants.adapterFrequencyUnits(mContext));
    }

    private void readSharedPref() {
        SharedPreferences prefs = mContext.getSharedPreferences(Constants.SHARED_PREFS_NAME,
                AppCompatActivity.MODE_PRIVATE);

        if (withGround) {
            editTextW.setText(prefs.getString(Constants.GCPW_W, "1"));
            spinnerW.setSelection(Integer
                    .parseInt(prefs.getString(Constants.GCPW_W_UNIT, Integer.toString(Constants.LengthUnit_mm))));

            editTextG.setText(prefs.getString(Constants.GCPW_S, "0.15"));
            spinnerG.setSelection(Integer
                    .parseInt(prefs.getString(Constants.GCPW_S_UNIT, Integer.toString(Constants.LengthUnit_mm))));

            editTextL.setText(prefs.getString(Constants.GCPW_L, "44.38"));
            spinnerL.setSelection(Integer
                    .parseInt(prefs.getString(Constants.GCPW_L_UNIT, Integer.toString(Constants.LengthUnit_mm))));

            editTextZ0.setText(prefs.getString(Constants.GCPW_Z0, "50.00"));
            spinnerZ0.setSelection(Integer
                    .parseInt(prefs.getString(Constants.GCPW_Z0_UNIT, Integer.toString(Constants.ImpedanceUnit_Ohm))));

            editTextPhs.setText(prefs.getString(Constants.GCPW_PHS, "90.00"));
            spinnerPhs.setSelection(Integer
                    .parseInt(prefs.getString(Constants.GCPW_PHS_UNIT, Integer.toString(Constants.PhaseUnit_Degree))));

            editTextFreq.setText(prefs.getString(Constants.GCPW_FREQ, "1.00"));
            spinnerFreq.setSelection(Integer
                    .parseInt(prefs.getString(Constants.GCPW_FREQ_UNIT, Integer.toString(Constants.FreqUnit_GHz))));

            editTextEr.setText(prefs.getString(Constants.GCPW_ER, "4.6"));

            editTextH.setText(prefs.getString(Constants.GCPW_H, "1.6"));
            spinnerH.setSelection(Integer
                    .parseInt(prefs.getString(Constants.GCPW_H_UNIT, Integer.toString(Constants.LengthUnit_mm))));

            // editTextT.setText(prefs.getString(Constants.GCPW_T, "0"));
            // spinnerT.setSelection(Integer.parseInt(prefs.getString(Constants.GCPW_T_UNIT,
            // Integer.toString(Constants.LengthUnit_mm))));
            target = Integer.parseInt(prefs.getString(Constants.MLIN_TARGET, Integer.toString(Constants.Synthesize_Width)));
        } else {
            editTextW.setText(prefs.getString(Constants.CPW_W, "1"));
            spinnerW.setSelection(
                    Integer.parseInt(prefs.getString(Constants.CPW_W_UNIT, Integer.toString(Constants.LengthUnit_mm))));

            editTextG.setText(prefs.getString(Constants.CPW_S, "0.244"));
            spinnerG.setSelection(
                    Integer.parseInt(prefs.getString(Constants.CPW_S_UNIT, Integer.toString(Constants.LengthUnit_mm))));

            editTextL.setText(prefs.getString(Constants.CPW_L, "44.464"));
            spinnerL.setSelection(
                    Integer.parseInt(prefs.getString(Constants.CPW_L_UNIT, Integer.toString(Constants.LengthUnit_mm))));

            editTextZ0.setText(prefs.getString(Constants.CPW_Z0, "50"));
            spinnerZ0.setSelection(Integer
                    .parseInt(prefs.getString(Constants.CPW_Z0_UNIT, Integer.toString(Constants.ImpedanceUnit_Ohm))));

            editTextPhs.setText(prefs.getString(Constants.CPW_PHS, "90"));
            spinnerPhs.setSelection(Integer
                    .parseInt(prefs.getString(Constants.CPW_PHS_UNIT, Integer.toString(Constants.PhaseUnit_Degree))));

            editTextFreq.setText(prefs.getString(Constants.CPW_FREQ, "1.00"));
            spinnerFreq.setSelection(Integer
                    .parseInt(prefs.getString(Constants.CPW_FREQ_UNIT, Integer.toString(Constants.FreqUnit_GHz))));

            editTextEr.setText(prefs.getString(Constants.CPW_ER, "4.6"));

            editTextH.setText(prefs.getString(Constants.CPW_H, "1.6"));
            spinnerH.setSelection(
                    Integer.parseInt(prefs.getString(Constants.CPW_H_UNIT, Integer.toString(Constants.LengthUnit_mm))));

            editTextT.setText(prefs.getString(Constants.CPW_T, "0.035"));
            spinnerT.setSelection(
                    Integer.parseInt(prefs.getString(Constants.CPW_T_UNIT, Integer.toString(Constants.LengthUnit_mm))));
            target = Integer.parseInt(prefs.getString(Constants.MLIN_TARGET, Integer.toString(Constants.Synthesize_Width)));
        }
    }

    private void setRadioBtn() {
        if (target == Constants.Synthesize_Width) {
            radioButtonW.setChecked(true);
            editTextW.setBackgroundTintList(getResources().getColorStateList(R.color.background_tint_synthesize));
            editTextW.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
            radioButtonG.setChecked(false);
            editTextG.setBackgroundTintList(
                    getResources().getColorStateList(R.color.background_tint_default_synthesize));
            editTextG.setTextColor(defaultEditTextColor);
            radioButtonH.setChecked(false);
            editTextH.setBackgroundTintList(
                    getResources().getColorStateList(R.color.background_tint_default_synthesize));
            editTextH.setTextColor(defaultEditTextColor);
        } else if (target == Constants.Synthesize_Gap) {
            radioButtonW.setChecked(false);
            editTextW.setBackgroundTintList(
                    getResources().getColorStateList(R.color.background_tint_default_synthesize));
            editTextW.setTextColor(defaultEditTextColor);
            radioButtonG.setChecked(true);
            editTextG.setBackgroundTintList(getResources().getColorStateList(R.color.background_tint_synthesize));
            editTextG.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
            radioButtonH.setChecked(false);
            editTextH.setBackgroundTintList(
                    getResources().getColorStateList(R.color.background_tint_default_synthesize));
            editTextH.setTextColor(defaultEditTextColor);
        } else if (target == Constants.Synthesize_Height) {
            radioButtonW.setChecked(false);
            editTextW.setBackgroundTintList(
                    getResources().getColorStateList(R.color.background_tint_default_synthesize));
            editTextW.setTextColor(defaultEditTextColor);
            radioButtonG.setChecked(false);
            editTextG.setBackgroundTintList(
                    getResources().getColorStateList(R.color.background_tint_default_synthesize));
            editTextG.setTextColor(defaultEditTextColor);
            radioButtonH.setChecked(true);
            editTextH.setBackgroundTintList(getResources().getColorStateList(R.color.background_tint_synthesize));
            editTextH.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
        } else {
            radioButtonW.setChecked(true);
            editTextW.setBackgroundTintList(getResources().getColorStateList(R.color.background_tint_synthesize));
            editTextW.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
            radioButtonG.setChecked(false);
            editTextG.setBackgroundTintList(
                    getResources().getColorStateList(R.color.background_tint_default_synthesize));
            editTextG.setTextColor(defaultEditTextColor);
            radioButtonH.setChecked(false);
            editTextH.setBackgroundTintList(
                    getResources().getColorStateList(R.color.background_tint_default_synthesize));
            editTextH.setTextColor(defaultEditTextColor);
            target = Constants.Synthesize_Width;
        }
        radioButtonW.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                radioButtonW.setChecked(true);
                editTextW.setBackgroundTintList(getResources().getColorStateList(R.color.background_tint_synthesize));
                editTextW.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
                radioButtonG.setChecked(false);
                editTextG.setBackgroundTintList(
                        getResources().getColorStateList(R.color.background_tint_default_synthesize));
                editTextG.setTextColor(defaultEditTextColor);
                radioButtonH.setChecked(false);
                editTextH.setBackgroundTintList(
                        getResources().getColorStateList(R.color.background_tint_default_synthesize));
                editTextH.setTextColor(defaultEditTextColor);
                target = Constants.Synthesize_Width;
            }
        });
        radioButtonG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                radioButtonW.setChecked(false);
                editTextW.setBackgroundTintList(
                        getResources().getColorStateList(R.color.background_tint_default_synthesize));
                editTextW.setTextColor(defaultEditTextColor);
                radioButtonG.setChecked(true);
                editTextG.setBackgroundTintList(getResources().getColorStateList(R.color.background_tint_synthesize));
                editTextG.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
                radioButtonH.setChecked(false);
                editTextH.setBackgroundTintList(
                        getResources().getColorStateList(R.color.background_tint_default_synthesize));
                editTextH.setTextColor(defaultEditTextColor);
                target = Constants.Synthesize_Gap;
            }
        });
        radioButtonH.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                radioButtonW.setChecked(false);
                editTextW.setBackgroundTintList(
                        getResources().getColorStateList(R.color.background_tint_default_synthesize));
                editTextW.setTextColor(defaultEditTextColor);
                radioButtonG.setChecked(false);
                editTextG.setBackgroundTintList(
                        getResources().getColorStateList(R.color.background_tint_default_synthesize));
                editTextG.setTextColor(defaultEditTextColor);
                radioButtonH.setChecked(true);
                editTextH.setBackgroundTintList(getResources().getColorStateList(R.color.background_tint_synthesize));
                editTextH.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
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
            editor.putString(Constants.GCPW_W, editTextW.getText().toString());
            editor.putString(Constants.GCPW_W_UNIT, spinnerW.getText().toString());
            editor.putString(Constants.GCPW_S, editTextG.getText().toString());
            editor.putString(Constants.GCPW_S_UNIT, spinnerG.getText().toString());
            editor.putString(Constants.GCPW_H, editTextH.getText().toString());
            editor.putString(Constants.GCPW_H_UNIT, spinnerH.getText().toString());
            editor.putString(Constants.GCPW_ER, editTextEr.getText().toString());
            editor.putString(Constants.GCPW_L, editTextL.getText().toString());
            editor.putString(Constants.GCPW_L_UNIT, spinnerL.getText().toString());
            editor.putString(Constants.GCPW_Z0, editTextZ0.getText().toString());
            editor.putString(Constants.GCPW_Z0_UNIT, spinnerZ0.getText().toString());
            editor.putString(Constants.GCPW_PHS, editTextPhs.getText().toString());
            editor.putString(Constants.GCPW_PHS_UNIT, spinnerPhs.getText().toString());
            editor.putString(Constants.GCPW_FREQ, editTextFreq.getText().toString());
            editor.putString(Constants.GCPW_FREQ_UNIT, spinnerFreq.getText().toString());
            editor.putString(Constants.GCPW_T, editTextT.getText().toString());
            editor.putString(Constants.GCPW_T_UNIT, spinnerT.getText().toString());
            editor.putString(Constants.GCPW_TARGET, Integer.toString(target));
        } else {
            editor.putString(Constants.CPW_W, editTextW.getText().toString());
            editor.putString(Constants.CPW_W_UNIT, spinnerW.getText().toString());
            editor.putString(Constants.CPW_S, editTextG.getText().toString());
            editor.putString(Constants.CPW_S_UNIT, spinnerG.getText().toString());
            editor.putString(Constants.CPW_H, editTextH.getText().toString());
            editor.putString(Constants.CPW_H_UNIT, spinnerH.getText().toString());
            editor.putString(Constants.CPW_ER, editTextEr.getText().toString());
            editor.putString(Constants.CPW_L, editTextL.getText().toString());
            editor.putString(Constants.CPW_L_UNIT, spinnerL.getText().toString());
            editor.putString(Constants.CPW_Z0, editTextZ0.getText().toString());
            editor.putString(Constants.CPW_Z0_UNIT, spinnerZ0.getText().toString());
            editor.putString(Constants.CPW_PHS, editTextPhs.getText().toString());
            editor.putString(Constants.CPW_PHS_UNIT, spinnerPhs.getText().toString());
            editor.putString(Constants.CPW_FREQ, editTextFreq.getText().toString());
            editor.putString(Constants.CPW_FREQ_UNIT, spinnerFreq.getText().toString());
            editor.putString(Constants.CPW_T, editTextT.getText().toString());
            editor.putString(Constants.CPW_T_UNIT, spinnerT.getText().toString());
            editor.putString(Constants.CPW_TARGET, Integer.toString(target));
        }
        editor.apply();
    }

    private boolean analysisInputCheck() {
        boolean checkResult = true;
        if (editTextW.length() == 0) {
            textInputLayoutW.setError(getText(R.string.Error_W_empty));
            checkResult = false;
        } else if (Constants.value2meter(Double.parseDouble(editTextW.getText().toString()),
                spinnerW.getText().toString()) < Constants.MINI_LIMIT) {
            textInputLayoutW.setError(getText(R.string.unreasonable_value));
            checkResult = false;
        }

        if (editTextG.length() == 0) {
            textInputLayoutG.setError(getText(R.string.Error_S_empty));
            checkResult = false;
        } else if (Constants.value2meter(Double.parseDouble(editTextG.getText().toString()),
                spinnerG.getText().toString()) < Constants.MINI_LIMIT) {
            textInputLayoutG.setError(getText(R.string.unreasonable_value));
            checkResult = false;
        }

        if (editTextFreq.length() == 0) {
            textInputLayoutF.setError(getText(R.string.Error_Freq_empty));
            checkResult = false;
        } else if (Double.parseDouble(editTextFreq.getText().toString()) == 0) {
            textInputLayoutF.setError(getText(R.string.error_zero_frequency));
            checkResult = false;
        }

        if (editTextH.length() == 0) {
            textInputLayoutH.setError(getText(R.string.Error_H_empty));
            checkResult = false;
        } else if (Constants.value2meter(Double.parseDouble(editTextH.getText().toString()),
                spinnerH.getText().toString()) < Constants.MINI_LIMIT) {
            textInputLayoutH.setError(getText(R.string.unreasonable_value));
            checkResult = false;
        }
        if (!withGround) {
            if (editTextT.length() == 0) {
                textInputLayoutT.setError(getText(R.string.Error_T_empty));
                checkResult = false;
            }
        }

        if (editTextEr.length() == 0) {
            textInputLayoutEr.setError(getText(R.string.error_er_empty));
            checkResult = false;
        } else if (Double.parseDouble(editTextEr.getText().toString()) < 1) {
            textInputLayoutEr.setError(getText(R.string.unreasonable_value));
            checkResult = false;
        }

        if (!checkResult) {
            editTextZ0.setText("");
            editTextPhs.setText("");
        }

        return checkResult;
    }

    private boolean synthesizeInputCheck() {
        boolean checkResult = true;
        if (editTextZ0.length() == 0) {
            textInputLayoutZ0.setError(getText(R.string.error_Z0_empty));
            checkResult = false;
        } else if (Double.parseDouble(editTextZ0.getText().toString()) == 0) {
            textInputLayoutZ0.setError(getText(R.string.unreasonable_value));
            checkResult = false;
        }

        if (editTextFreq.length() == 0) {
            textInputLayoutF.setError(getText(R.string.Error_Freq_empty));
            checkResult = false;
        } else if (Double.parseDouble(editTextFreq.getText().toString()) == 0) {
            textInputLayoutF.setError(getText(R.string.error_zero_frequency));
            checkResult = false;
        }

        if (editTextEr.length() == 0) {
            textInputLayoutEr.setError(getText(R.string.error_er_empty));
            checkResult = false;
        } else if (Double.parseDouble(editTextEr.getText().toString()) < 1) {
            textInputLayoutEr.setError(getText(R.string.unreasonable_value));
            checkResult = false;
        }

        if (!withGround) {
            if (editTextT.length() == 0) {
                textInputLayoutT.setError(getText(R.string.Error_T_empty));
                checkResult = false;
            }
        }

        if (target == Constants.Synthesize_Width) {
            if (editTextG.length() == 0) {
                textInputLayoutG.setError(getText(R.string.Error_S_empty));
                checkResult = false;
            } else if (Constants.value2meter(Double.parseDouble(editTextG.getText().toString()),
                    spinnerG.getText().toString()) < Constants.MINI_LIMIT) {
                textInputLayoutG.setError(getText(R.string.unreasonable_value));
                checkResult = false;
            }

            if (editTextH.length() == 0) {
                textInputLayoutH.setError(getText(R.string.Error_H_empty));
                checkResult = false;
            } else if (Constants.value2meter(Double.parseDouble(editTextH.getText().toString()),
                    spinnerH.getText().toString()) < Constants.MINI_LIMIT) {
                textInputLayoutH.setError(getText(R.string.unreasonable_value));
                checkResult = false;
            }

            if (!checkResult) {
                editTextW.setText("");
            }
        } else if (target == Constants.Synthesize_Gap) {
            if (editTextW.length() == 0) {
                textInputLayoutW.setError(getText(R.string.Error_W_empty));
                checkResult = false;
            } else if (Constants.value2meter(Double.parseDouble(editTextW.getText().toString()),
                    spinnerW.getText().toString()) < Constants.MINI_LIMIT) {
                textInputLayoutW.setError(getText(R.string.unreasonable_value));
                checkResult = false;
            }

            if (editTextH.length() == 0) {
                textInputLayoutH.setError(getText(R.string.Error_H_empty));
                checkResult = false;
            } else if (Constants.value2meter(Double.parseDouble(editTextH.getText().toString()),
                    spinnerH.getText().toString()) < Constants.MINI_LIMIT) {
                textInputLayoutH.setError(getText(R.string.unreasonable_value));
                checkResult = false;
            }

            if (!checkResult) {
                editTextG.setText("");
            }
        } else if (target == Constants.Synthesize_Height) {
            if (editTextW.length() == 0) {
                textInputLayoutW.setError(getText(R.string.Error_W_empty));
                checkResult = false;
            } else if (Constants.value2meter(Double.parseDouble(editTextW.getText().toString()),
                    spinnerW.getText().toString()) < Constants.MINI_LIMIT) {
                textInputLayoutW.setError(getText(R.string.unreasonable_value));
                checkResult = false;
            }

            if (editTextG.length() == 0) {
                textInputLayoutG.setError(getText(R.string.Error_S_empty));
                checkResult = false;
            } else if (Constants.value2meter(Double.parseDouble(editTextG.getText().toString()),
                    spinnerG.getText().toString()) < Constants.MINI_LIMIT) {
                textInputLayoutG.setError(getText(R.string.unreasonable_value));
                checkResult = false;
            }

            if (!checkResult) {
                editTextH.setText("");
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
        textInputLayoutG.setError(null);
        textInputLayoutG.setErrorEnabled(false);
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

    public void resetValues() {
        if (withGround) {
            editTextW.setText("1");
            spinnerW.setSelection(Constants.LengthUnit_mm);
            editTextG.setText("0.15");
            spinnerG.setSelection(Constants.LengthUnit_mm);
            editTextL.setText("44.38");
            spinnerL.setSelection(Constants.LengthUnit_mm);
            editTextZ0.setText("50.00");
            spinnerZ0.setSelection(Constants.ImpedanceUnit_Ohm);
            editTextPhs.setText("90.00");
            spinnerPhs.setSelection(Constants.PhaseUnit_Degree);
            editTextFreq.setText("1.00");
            spinnerFreq.setSelection(Constants.FreqUnit_GHz);
            editTextEr.setText("4.6");
            editTextH.setText("1.6");
            spinnerH.setSelection(Constants.LengthUnit_mm);
            target = Constants.Synthesize_Width;
        } else {
            editTextW.setText("1");
            spinnerW.setSelection(Constants.LengthUnit_mm);
            editTextG.setText("0.244");
            spinnerG.setSelection(Constants.LengthUnit_mm);
            editTextL.setText("44.464");
            spinnerL.setSelection(Constants.LengthUnit_mm);
            editTextZ0.setText("50");
            spinnerZ0.setSelection(Constants.ImpedanceUnit_Ohm);
            editTextPhs.setText("90");
            spinnerPhs.setSelection(Constants.PhaseUnit_Degree);
            editTextFreq.setText("1.00");
            spinnerFreq.setSelection(Constants.FreqUnit_GHz);
            editTextEr.setText("4.6");
            editTextH.setText("1.6");
            spinnerH.setSelection(Constants.LengthUnit_mm);
            editTextT.setText("0.035");
            spinnerT.setSelection(Constants.LengthUnit_mm);
            target = Constants.Synthesize_Width;
        }

        radioButtonW.setChecked(true);
        editTextW.setBackgroundTintList(getResources().getColorStateList(R.color.background_tint_synthesize));
        editTextW.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
        radioButtonG.setChecked(false);
        editTextG.setBackgroundTintList(
                getResources().getColorStateList(R.color.background_tint_default_synthesize));
        editTextG.setTextColor(defaultEditTextColor);
        radioButtonH.setChecked(false);
        editTextH.setBackgroundTintList(
                getResources().getColorStateList(R.color.background_tint_default_synthesize));
        editTextH.setTextColor(defaultEditTextColor);
    }
}
