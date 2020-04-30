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
import com.rookiedev.microwavetools.libs.CoaxCalculator;
import com.rookiedev.microwavetools.libs.CoaxModel;
import com.rookiedev.microwavetools.libs.Constants;

import java.math.BigDecimal;

public class CoaxFragment extends Fragment {
    private Context mContext;
    private View viewRoot;
    private TextInputEditText editTextA, editTextB, editTextC, editTextL, editTextZ0, editTextPhs, editTextFreq,
            editTextEr;
    private TextInputLayout textInputLayoutA, textInputLayoutB, textInputLayoutC, textInputLayoutZ0, textInputLayoutEr,
            textInputLayoutF;
    private Button buttonSynthesize, buttonAnalyze;
    private AutoCompleteTextView spinnerA, spinnerB, spinnerC, spinnerL, spinnerZ0, spinnerPhs, spinnerFreq;
    private int target;
    private RadioButton radioButtonA, radioButtonB, radioButtonC;
    private CoaxModel line;
    private ColorStateList defaultEditTextColor;
    private AdFragment adFragment = null;
    private FragmentManager fragmentManager = null;

    private boolean isAdFree;

    public CoaxFragment() {
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
        viewRoot = inflater.inflate(R.layout.fragment_coax, container, false);
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
                    line.setCoreRadius(Double.parseDouble(editTextA.getText().toString()),
                            spinnerA.getText().toString());
                    line.setSubRadius(Double.parseDouble(editTextB.getText().toString()),
                            spinnerB.getText().toString());
                    line.setCoreOffset(Double.parseDouble(editTextC.getText().toString()),
                            spinnerC.getText().toString());
                    line.setFrequency(Double.parseDouble(editTextFreq.getText().toString()),
                            spinnerFreq.getText().toString());
                    line.setSubEpsilon(Double.parseDouble(editTextEr.getText().toString()));

                    if (!editTextL.getText().toString().equals("")) {
                        line.setMetalLength(Double.parseDouble(editTextL.getText().toString()),
                                spinnerL.getText().toString());
                    } else {
                        line.setMetalLength(0, spinnerL.getText().toString());
                    }

                    CoaxCalculator coax = new CoaxCalculator();
                    line = coax.getAnaResult(line);

                    if (line.getErrorCode() == Constants.ERROR.NO_ERROR) {
                        if (!editTextL.getText().toString().equals("")) {
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
                    } else if (line.getErrorCode() == Constants.ERROR.SUBSTRATE_TOO_LARGE) {
                        editTextZ0.setText("");
                        editTextPhs.setText("");
                        textInputLayoutA.setError(getString(R.string.substrate_too_large));
                        editTextA.requestFocus();
                        textInputLayoutB.setError(getString(R.string.substrate_too_large));
                    } else if (line.getErrorCode() == Constants.ERROR.OFFSET_TOO_LARGE) {
                        editTextZ0.setText("");
                        editTextPhs.setText("");
                        textInputLayoutC.setError(getString(R.string.offset_too_large));
                        editTextC.requestFocus();
                    }
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
                    line.setSubEpsilon(Double.parseDouble(editTextEr.getText().toString()));

                    if (target == Constants.Synthesize_CoreRadius) { // a = 0;
                        line.setSubRadius(Double.parseDouble(editTextB.getText().toString()),
                                spinnerB.getText().toString());
                        line.setCoreOffset(Double.parseDouble(editTextC.getText().toString()),
                                spinnerC.getText().toString());
                    } else if (target == Constants.Synthesize_SubRadius) { // b = 0;
                        line.setCoreRadius(Double.parseDouble(editTextA.getText().toString()),
                                spinnerA.getText().toString());
                        line.setCoreOffset(Double.parseDouble(editTextC.getText().toString()),
                                spinnerC.getText().toString());
                    } else if (target == Constants.Synthesize_CoreOffset) { // c = 0;
                        line.setCoreRadius(Double.parseDouble(editTextA.getText().toString()),
                                spinnerA.getText().toString());
                        line.setSubRadius(Double.parseDouble(editTextB.getText().toString()),
                                spinnerB.getText().toString());
                    }

                    if (editTextPhs.length() != 0) {
                        line.setPhase(Double.parseDouble(editTextPhs.getText().toString()));
                    } else {
                        line.setPhase(0);
                    }

                    CoaxCalculator coax = new CoaxCalculator();
                    line = coax.getSynResult(line, target);

                    if (line.getErrorCode() == Constants.ERROR.NO_ERROR) {
                        if (editTextPhs.length() != 0) {
                            BigDecimal L_temp = new BigDecimal(
                                    Constants.meter2others(line.getMetalLength(), spinnerL.getText().toString()));
                            double L = L_temp.setScale(Constants.DecimalLength, BigDecimal.ROUND_HALF_UP).doubleValue();
                            editTextL.setText(String.valueOf(L));
                        } else {
                            editTextL.setText("");
                        }

                        if (target == Constants.Synthesize_CoreRadius) {
                            if ((Double.isNaN(line.getCoreRadius()) || Double.isInfinite(line.getCoreRadius()))) {
                                editTextA.setText("");
                                textInputLayoutA.setError(getString(R.string.synthesize_failed));
                                editTextA.requestFocus();
                            } else {
                                BigDecimal a_temp = new BigDecimal(Constants.meter2others(line.getCoreRadius(),
                                        spinnerA.getText().toString()));
                                double a = a_temp.setScale(Constants.DecimalLength, BigDecimal.ROUND_HALF_UP)
                                        .doubleValue();
                                editTextA.setText(String.valueOf(a));
                            }
                        } else if (target == Constants.Synthesize_SubRadius) {
                            if ((Double.isNaN(line.getSubRadius()) || Double.isInfinite(line.getSubRadius()))) {
                                editTextB.setText("");
                                textInputLayoutB.setError(getString(R.string.synthesize_failed));
                                editTextB.requestFocus();
                            } else {
                                BigDecimal b_temp = new BigDecimal(Constants.meter2others(line.getSubRadius(),
                                        spinnerB.getText().toString()));
                                double height = b_temp.setScale(Constants.DecimalLength, BigDecimal.ROUND_HALF_UP)
                                        .doubleValue();
                                editTextB.setText(String.valueOf(height));
                            }
                        } else if (target == Constants.Synthesize_CoreOffset) {
                            if ((Double.isNaN(line.getCoreOffset()) || Double.isInfinite(line.getCoreOffset()))) {
                                editTextC.setText("");
                                textInputLayoutC.setError(getString(R.string.synthesize_failed));
                                editTextC.requestFocus();
                            } else {
                                BigDecimal c_temp = new BigDecimal(Constants.meter2others(line.getCoreOffset(),
                                        spinnerC.getText().toString()));
                                double b = c_temp.setScale(Constants.DecimalLength, BigDecimal.ROUND_HALF_UP)
                                        .doubleValue();
                                editTextC.setText(String.valueOf(b));
                            }
                        }
                    } else {
                        if (target == Constants.Synthesize_CoreRadius) {
                            editTextA.setText("");
                            textInputLayoutA.setError(getString(R.string.synthesize_failed));
                            editTextA.requestFocus();
                        } else if (target == Constants.Synthesize_SubRadius) {
                            editTextB.setText("");
                            textInputLayoutB.setError(getString(R.string.synthesize_failed));
                            editTextB.requestFocus();
                        } else if (target == Constants.Synthesize_CoreOffset) {
                            editTextC.setText("");
                            textInputLayoutC.setError(getString(R.string.synthesize_failed));
                            editTextC.requestFocus();
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
        line = new CoaxModel();

        RadioButton radioButtonL = viewRoot.findViewById(R.id.radioBtn_L);
        radioButtonL.setVisibility(View.VISIBLE);
        radioButtonL.setChecked(true);

        RadioButton radioButtonPhs = viewRoot.findViewById(R.id.radioBtn_Phs);
        radioButtonPhs.setVisibility(View.VISIBLE);
        radioButtonPhs.setChecked(true);

        RadioButton radioButtonZ0 = viewRoot.findViewById(R.id.radioBtn_Z0);
        radioButtonZ0.setVisibility(View.VISIBLE);
        radioButtonZ0.setChecked(true);

        radioButtonA = viewRoot.findViewById(R.id.radioBtn_a);
        radioButtonA.setVisibility(View.VISIBLE);
        radioButtonC = viewRoot.findViewById(R.id.radioBtn_c);
        radioButtonC.setVisibility(View.VISIBLE);
        radioButtonB = viewRoot.findViewById(R.id.radio_button_b);
        radioButtonB.setVisibility(View.VISIBLE);

        textInputLayoutA = viewRoot.findViewById(R.id.text_input_layout_a);
        editTextA = viewRoot.findViewById(R.id.editText_a);
        defaultEditTextColor = editTextA.getTextColors();
        editTextA.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                textInputLayoutA.setError("");
                textInputLayoutA.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        textInputLayoutC = viewRoot.findViewById(R.id.text_input_layout_c);
        editTextC = viewRoot.findViewById(R.id.editText_c);
        editTextC.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                textInputLayoutC.setError("");
                textInputLayoutC.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        textInputLayoutB = viewRoot.findViewById(R.id.text_input_layout_b);
        editTextB = viewRoot.findViewById(R.id.editText_b);
        editTextB.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                textInputLayoutB.setError("");
                textInputLayoutB.setErrorEnabled(false);
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
        spinnerA = viewRoot.findViewById(R.id.menu_a);
        spinnerC = viewRoot.findViewById(R.id.menu_c);
        spinnerB = viewRoot.findViewById(R.id.menu_b);
        spinnerL = viewRoot.findViewById(R.id.menu_L);
        spinnerZ0 = viewRoot.findViewById(R.id.menu_Z0);
        spinnerPhs = viewRoot.findViewById(R.id.menu_Phs);
        spinnerFreq = viewRoot.findViewById(R.id.menu_Freq);

        // configure the length units
        spinnerA.setAdapter(Constants.adapterDimensionUnits(mContext));
        spinnerC.setAdapter(Constants.adapterDimensionUnits(mContext));
        spinnerB.setAdapter(Constants.adapterDimensionUnits(mContext));
        spinnerL.setAdapter(Constants.adapterDimensionUnits(mContext));

        // configure the impedance units
        spinnerZ0.setAdapter(Constants.adapterImpedanceUnits(mContext));

        // configure the electrical length units
        spinnerPhs.setAdapter(Constants.adapterPhaseUnits(mContext));

        // configure the frequency units
        spinnerFreq.setAdapter(Constants.adapterFrequencyUnits(mContext));
    }

    private void readSharedPref() {
        SharedPreferences prefs = mContext.getSharedPreferences(Constants.SHARED_PREFS_NAME,
                AppCompatActivity.MODE_PRIVATE);// get the header_parameters from the Shared

        editTextA.setText(prefs.getString(Constants.COAX_A, "0.167"));
        spinnerA.setText(Constants.validateUnit(Constants.adapterDimensionUnits(mContext), prefs.getString(Constants.COAX_A_UNIT, Constants.LengthUnit_mm)), false);

        editTextB.setText(prefs.getString(Constants.COAX_B, "1.00"));
        spinnerB.setText(Constants.validateUnit(Constants.adapterDimensionUnits(mContext), prefs.getString(Constants.COAX_B_UNIT, Constants.LengthUnit_mm)), false);

        editTextC.setText(prefs.getString(Constants.COAX_C, "0.00"));
        spinnerC.setText(Constants.validateUnit(Constants.adapterDimensionUnits(mContext), prefs.getString(Constants.COAX_C_UNIT, Constants.LengthUnit_mm)), false);

        editTextL.setText(prefs.getString(Constants.COAX_L, "34.945"));
        spinnerL.setText(Constants.validateUnit(Constants.adapterDimensionUnits(mContext), prefs.getString(Constants.COAX_L_UNIT, Constants.LengthUnit_mm)), false);

        editTextZ0.setText(prefs.getString(Constants.COAX_Z0, "50"));
        spinnerZ0.setText(Constants.validateUnit(Constants.adapterImpedanceUnits(mContext), prefs.getString(Constants.COAX_Z0_UNIT, Constants.ImpedanceUnit_Ohm)), false);

        editTextPhs.setText(prefs.getString(Constants.COAX_PHS, "90"));
        spinnerPhs.setText(Constants.validateUnit(Constants.adapterPhaseUnits(mContext), prefs.getString(Constants.COAX_PHS_UNIT, Constants.PhaseUnit_Degree)), false);

        editTextFreq.setText(prefs.getString(Constants.COAX_FREQ, "1.00"));
        spinnerFreq.setText(Constants.validateUnit(Constants.adapterFrequencyUnits(mContext), prefs.getString(Constants.COAX_FREQ_UNIT, Constants.FreqUnit_GHz)), false);

        editTextEr.setText(prefs.getString(Constants.COAX_ER, "4.6"));

        target = Integer
                .parseInt(prefs.getString(Constants.COAX_TARGET, Integer.toString(Constants.Synthesize_CoreRadius)));
    }

    private void setRadioBtn() {
        if (target == Constants.Synthesize_CoreRadius) {
            radioButtonA.setChecked(true);
            editTextA.setBackgroundTintList(getResources().getColorStateList(R.color.background_tint_synthesize));
            editTextA.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
            radioButtonB.setChecked(false);
            editTextB.setBackgroundTintList(
                    getResources().getColorStateList(R.color.background_tint_default_synthesize));
            editTextB.setTextColor(defaultEditTextColor);
            radioButtonC.setChecked(false);
            editTextC.setBackgroundTintList(
                    getResources().getColorStateList(R.color.background_tint_default_synthesize));
            editTextC.setTextColor(defaultEditTextColor);
        } else if (target == Constants.Synthesize_SubRadius) {
            radioButtonA.setChecked(false);
            editTextA.setBackgroundTintList(
                    getResources().getColorStateList(R.color.background_tint_default_synthesize));
            editTextA.setTextColor(defaultEditTextColor);
            radioButtonB.setChecked(true);
            editTextB.setBackgroundTintList(getResources().getColorStateList(R.color.background_tint_synthesize));
            editTextB.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
            radioButtonC.setChecked(false);
            editTextC.setBackgroundTintList(
                    getResources().getColorStateList(R.color.background_tint_default_synthesize));
            editTextC.setTextColor(defaultEditTextColor);
        } else if (target == Constants.Synthesize_CoreOffset) {
            radioButtonA.setChecked(false);
            editTextA.setBackgroundTintList(
                    getResources().getColorStateList(R.color.background_tint_default_synthesize));
            editTextA.setTextColor(defaultEditTextColor);
            radioButtonB.setChecked(false);
            editTextB.setBackgroundTintList(
                    getResources().getColorStateList(R.color.background_tint_default_synthesize));
            editTextB.setTextColor(defaultEditTextColor);
            radioButtonC.setChecked(true);
            editTextC.setBackgroundTintList(getResources().getColorStateList(R.color.background_tint_synthesize));
            editTextC.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
        } else {
            target = Constants.Synthesize_CoreRadius;
            radioButtonA.setChecked(true);
            editTextA.setBackgroundTintList(getResources().getColorStateList(R.color.background_tint_synthesize));
            editTextA.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
            radioButtonB.setChecked(false);
            editTextB.setBackgroundTintList(
                    getResources().getColorStateList(R.color.background_tint_default_synthesize));
            editTextB.setTextColor(defaultEditTextColor);
            radioButtonC.setChecked(false);
            editTextC.setBackgroundTintList(
                    getResources().getColorStateList(R.color.background_tint_default_synthesize));
            editTextC.setTextColor(defaultEditTextColor);
        }
        radioButtonA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                radioButtonA.setChecked(true);
                editTextA.setBackgroundTintList(getResources().getColorStateList(R.color.background_tint_synthesize));
                editTextA.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
                radioButtonB.setChecked(false);
                editTextB.setBackgroundTintList(
                        getResources().getColorStateList(R.color.background_tint_default_synthesize));
                editTextB.setTextColor(defaultEditTextColor);
                radioButtonC.setChecked(false);
                editTextC.setBackgroundTintList(
                        getResources().getColorStateList(R.color.background_tint_default_synthesize));
                editTextC.setTextColor(defaultEditTextColor);
                target = Constants.Synthesize_CoreRadius;
            }
        });
        radioButtonB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                radioButtonA.setChecked(false);
                editTextA.setBackgroundTintList(
                        getResources().getColorStateList(R.color.background_tint_default_synthesize));
                editTextA.setTextColor(defaultEditTextColor);
                radioButtonB.setChecked(true);
                editTextB.setBackgroundTintList(getResources().getColorStateList(R.color.background_tint_synthesize));
                editTextB.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
                radioButtonC.setChecked(false);
                editTextC.setBackgroundTintList(
                        getResources().getColorStateList(R.color.background_tint_default_synthesize));
                editTextC.setTextColor(defaultEditTextColor);
                target = Constants.Synthesize_Height;
            }
        });
        radioButtonC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                radioButtonA.setChecked(false);
                editTextA.setBackgroundTintList(
                        getResources().getColorStateList(R.color.background_tint_default_synthesize));
                editTextA.setTextColor(defaultEditTextColor);
                radioButtonB.setChecked(false);
                editTextB.setBackgroundTintList(
                        getResources().getColorStateList(R.color.background_tint_default_synthesize));
                editTextB.setTextColor(defaultEditTextColor);
                radioButtonC.setChecked(true);
                editTextC.setBackgroundTintList(getResources().getColorStateList(R.color.background_tint_synthesize));
                editTextC.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
                target = Constants.Synthesize_CoreOffset;
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        SharedPreferences prefs = mContext.getSharedPreferences(Constants.SHARED_PREFS_NAME,
                AppCompatActivity.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString(Constants.COAX_A, editTextA.getText().toString());
        editor.putString(Constants.COAX_A_UNIT, spinnerA.getText().toString());
        editor.putString(Constants.COAX_B, editTextB.getText().toString());
        editor.putString(Constants.COAX_B_UNIT, spinnerB.getText().toString());
        editor.putString(Constants.COAX_C, editTextC.getText().toString());
        editor.putString(Constants.COAX_C_UNIT, spinnerC.getText().toString());
        editor.putString(Constants.COAX_ER, editTextEr.getText().toString());
        editor.putString(Constants.COAX_L, editTextL.getText().toString());
        editor.putString(Constants.COAX_L_UNIT, spinnerL.getText().toString());
        editor.putString(Constants.COAX_Z0, editTextZ0.getText().toString());
        editor.putString(Constants.COAX_Z0_UNIT, spinnerZ0.getText().toString());
        editor.putString(Constants.COAX_PHS, editTextPhs.getText().toString());
        editor.putString(Constants.COAX_PHS_UNIT, spinnerPhs.getText().toString());
        editor.putString(Constants.COAX_FREQ, editTextFreq.getText().toString());
        editor.putString(Constants.COAX_FREQ_UNIT, spinnerFreq.getText().toString());
        editor.putString(Constants.COAX_TARGET, Integer.toString(target));

        editor.apply();
    }

    private boolean analysisInputCheck() {
        boolean checkResult = true;
        if (editTextA.length() == 0) {
            textInputLayoutA.setError(getText(R.string.Error_a_empty));
            checkResult = false;
        } else if (Constants.value2meter(Double.parseDouble(editTextA.getText().toString()),
                spinnerA.getText().toString()) < Constants.MINI_LIMIT) {
            textInputLayoutA.setError(getText(R.string.unreasonable_value));
            checkResult = false;
        }

        if (editTextFreq.length() == 0) {
            textInputLayoutF.setError(getText(R.string.Error_Freq_empty));
            checkResult = false;
        } else if (Double.parseDouble(editTextFreq.getText().toString()) == 0) {
            textInputLayoutF.setError(getText(R.string.error_zero_frequency));
            checkResult = false;
        }

        if (editTextB.length() == 0) {
            textInputLayoutB.setError(getText(R.string.Error_b_empty));
            checkResult = false;
        } else if (Constants.value2meter(Double.parseDouble(editTextB.getText().toString()),
                spinnerB.getText().toString()) < Constants.MINI_LIMIT) {
            textInputLayoutB.setError(getText(R.string.unreasonable_value));
            checkResult = false;
        }

        if (editTextC.length() == 0) {
            textInputLayoutC.setError(getText(R.string.Error_c_empty));
            checkResult = false;
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

        if (target == Constants.Synthesize_CoreRadius) {
            if (editTextB.length() == 0) {
                textInputLayoutB.setError(getText(R.string.Error_b_empty));
                checkResult = false;
            }
            if (editTextC.length() == 0) {
                textInputLayoutC.setError(getText(R.string.Error_c_empty));
                checkResult = false;
            }

            if (!checkResult) {
                editTextA.setText("");
            }
        } else if (target == Constants.Synthesize_SubRadius) {
            if (editTextA.length() == 0) {
                textInputLayoutA.setError(getText(R.string.Error_a_empty));
                checkResult = false;
            }
            if (editTextC.length() == 0) {
                textInputLayoutC.setError(getText(R.string.Error_c_empty));
                checkResult = false;
            }

            if (!checkResult) {
                editTextB.setText("");
            }
        } else if (target == Constants.Synthesize_CoreOffset) {
            if (editTextA.length() == 0) {
                textInputLayoutA.setError(getText(R.string.Error_a_empty));
                checkResult = false;
            }
            if (editTextB.length() == 0) {
                textInputLayoutB.setError(getText(R.string.Error_b_empty));
                checkResult = false;
            }

            if (!checkResult) {
                editTextC.setText("");
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
        textInputLayoutA.setError(null);
        textInputLayoutA.setErrorEnabled(false);
        textInputLayoutB.setError(null);
        textInputLayoutB.setErrorEnabled(false);
        textInputLayoutC.setError(null);
        textInputLayoutC.setErrorEnabled(false);
        textInputLayoutZ0.setError(null);
        textInputLayoutZ0.setErrorEnabled(false);
        textInputLayoutEr.setError(null);
        textInputLayoutEr.setErrorEnabled(false);
        textInputLayoutF.setError(null);
        textInputLayoutF.setErrorEnabled(false);
    }

    public void resetValues() {
        editTextA.setText("0.167");
        spinnerA.setText(Constants.LengthUnit_mm);
        editTextB.setText("1.00");
        spinnerB.setText(Constants.LengthUnit_mm);
        editTextC.setText("0.00");
        spinnerC.setText(Constants.LengthUnit_mm);
        editTextL.setText("34.945");
        spinnerL.setText(Constants.LengthUnit_mm);
        editTextZ0.setText("50");
        spinnerZ0.setText(Constants.ImpedanceUnit_Ohm);
        editTextPhs.setText("90");
        spinnerPhs.setText(Constants.PhaseUnit_Degree);
        editTextFreq.setText("1.00");
        spinnerFreq.setText(Constants.FreqUnit_GHz);
        editTextEr.setText("4.6");
        target = Constants.Synthesize_CoreRadius;

        radioButtonA.setChecked(true);
        editTextA.setBackgroundTintList(getResources().getColorStateList(R.color.background_tint_synthesize));
        editTextA.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
        radioButtonB.setChecked(false);
        editTextB.setBackgroundTintList(
                getResources().getColorStateList(R.color.background_tint_default_synthesize));
        editTextB.setTextColor(defaultEditTextColor);
        radioButtonC.setChecked(false);
        editTextC.setBackgroundTintList(
                getResources().getColorStateList(R.color.background_tint_default_synthesize));
        editTextC.setTextColor(defaultEditTextColor);
    }
}
