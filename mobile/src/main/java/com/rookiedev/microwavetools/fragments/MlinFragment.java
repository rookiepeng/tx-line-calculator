
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
import com.rookiedev.microwavetools.libs.MlinCalculator;
import com.rookiedev.microwavetools.libs.MlinModel;

import java.math.BigDecimal;

public class MlinFragment extends Fragment {
    private Context mContext;
    private View viewRoot;
    private RadioButton radioButtonW, radioButtonH;
    private TextInputEditText editTextW, editTextL, editTextZ0, editTextPhs, editTextFreq, editTextT, editTextH,
            editTextEr;
    private TextInputLayout textInputLayoutT, textInputLayoutH, textInputLayoutW, textInputLayoutZ0, textInputLayoutEr,
            textInputLayoutF;
    private Button buttonSynthesize, buttonAnalyze;
    private Spinner spinnerW, spinnerL, spinnerT, spinnerH, spinnerZ0, spinnerPhs, spinnerFreq;
    private MlinModel line;
    private int target;
    private ColorStateList defaultEditTextColor;
    private AdFragment adFragment = null;
    private boolean isAdFree;
    private FragmentManager fragmentManager = null;

    public MlinFragment() {
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
        viewRoot = inflater.inflate(R.layout.fragment_mlin, container, false);
        mContext = this.getContext();
        fragmentManager = getFragmentManager();

        initUI();
        readSharedPref(); // read shared preferences
        setRadioBtn();

        buttonAnalyze.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Constants.refreshAnimation(mContext, (ImageView) viewRoot.findViewById(R.id.analyze_reveal),
                        Constants.ANALYZE);
                clearEditTextErrors();
                if (analysisInputEmptyCheck()) {
                    line.setMetalWidth(Double.parseDouble(editTextW.getText().toString()),
                            spinnerW.getSelectedItemPosition());
                    line.setFrequency(Double.parseDouble(editTextFreq.getText().toString()),
                            spinnerFreq.getSelectedItemPosition());
                    line.setSubEpsilon(Double.parseDouble(editTextEr.getText().toString()));
                    line.setSubHeight(Double.parseDouble(editTextH.getText().toString()),
                            spinnerH.getSelectedItemPosition());
                    line.setMetalThick(Double.parseDouble(editTextT.getText().toString()),
                            spinnerT.getSelectedItemPosition());

                    if (editTextL.length() != 0) {
                        line.setMetalLength(Double.parseDouble(editTextL.getText().toString()),
                                spinnerL.getSelectedItemPosition());
                    } else {
                        line.setMetalLength(0, spinnerL.getSelectedItemPosition());
                    }
                    MlinCalculator mlin = new MlinCalculator();
                    line = mlin.getAnaResult(line);

                    BigDecimal Z0_temp = new BigDecimal(line.getImpedance());
                    double Z0 = Z0_temp.setScale(Constants.DecimalLength, BigDecimal.ROUND_HALF_UP).doubleValue();
                    editTextZ0.setText(String.valueOf(Z0));

                    if (editTextL.length() != 0) {
                        BigDecimal Eeff_temp = new BigDecimal(line.getPhase());
                        double Eeff = Eeff_temp.setScale(Constants.DecimalLength, BigDecimal.ROUND_HALF_UP)
                                .doubleValue();
                        editTextPhs.setText(String.valueOf(Eeff));
                    } else {
                        editTextPhs.setText("");
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
                if (synthesizeInputEmptyCheck()) {
                    line.setImpedance(Double.parseDouble(editTextZ0.getText().toString()));
                    line.setFrequency(Double.parseDouble(editTextFreq.getText().toString()),
                            spinnerFreq.getSelectedItemPosition());
                    line.setSubEpsilon(Double.parseDouble(editTextEr.getText().toString()));
                    line.setMetalThick(Double.parseDouble(editTextT.getText().toString()),
                            spinnerT.getSelectedItemPosition());

                    if (target == Constants.Synthesize_Width) {
                        line.setSubHeight(Double.parseDouble(editTextH.getText().toString()),
                                spinnerH.getSelectedItemPosition());
                        line.setMetalWidth(0, Constants.LengthUnit_m);
                    } else if (target == Constants.Synthesize_Height) {
                        line.setMetalWidth(Double.parseDouble(editTextW.getText().toString()),
                                spinnerW.getSelectedItemPosition());
                        line.setSubHeight(0, Constants.LengthUnit_m);
                    }

                    if (editTextPhs.length() != 0) {
                        line.setPhase(Double.parseDouble(editTextPhs.getText().toString()));
                    } else {
                        line.setPhase(0);
                    }
                    MlinCalculator mlin = new MlinCalculator();
                    line = mlin.getSynResult(line, target);

                    if (line.getErrorCode() == Constants.ERROR.NO_ERROR) {
                        if (editTextPhs.length() != 0) {
                            BigDecimal L_temp = new BigDecimal(
                                    Constants.meter2others(line.getMetalLength(), spinnerL.getSelectedItemPosition()));
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
                                        spinnerW.getSelectedItemPosition()));
                                double W = W_temp.setScale(Constants.DecimalLength, BigDecimal.ROUND_HALF_UP)
                                        .doubleValue();
                                editTextW.setText(String.valueOf(W));
                            }
                        } else if (target == Constants.Synthesize_Height) {
                            if ((Double.isNaN(line.getSubHeight()) || Double.isInfinite(line.getSubHeight()))) {
                                editTextH.setText("");
                                textInputLayoutH.setError(getString(R.string.synthesize_failed));
                                editTextH.requestFocus();
                            } else {
                                BigDecimal H_temp = new BigDecimal(Constants.meter2others(line.getSubHeight(),
                                        spinnerH.getSelectedItemPosition()));
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
        line = new MlinModel();

        radioButtonW = viewRoot.findViewById(R.id.radioBtn_W);
        radioButtonW.setVisibility(View.VISIBLE);

        radioButtonH = viewRoot.findViewById(R.id.radioBtn_H);
        radioButtonH.setVisibility(View.VISIBLE);

        RadioButton radioButtonL = viewRoot.findViewById(R.id.radioBtn_L);
        radioButtonL.setVisibility(View.VISIBLE);
        radioButtonL.setChecked(true);

        RadioButton radioButtonZ0 = viewRoot.findViewById(R.id.radioBtn_Z0);
        radioButtonZ0.setVisibility(View.VISIBLE);
        radioButtonZ0.setChecked(true);

        RadioButton radioButtonPhs = viewRoot.findViewById(R.id.radioBtn_Phs);
        radioButtonPhs.setVisibility(View.VISIBLE);
        radioButtonPhs.setChecked(true);

        textInputLayoutW = viewRoot.findViewById(R.id.text_input_layout_W);
        editTextW = viewRoot.findViewById(R.id.editText_W);
        defaultEditTextColor = editTextW.getTextColors();
        // editTextW.setTextColor(ContextCompat.getColor(mContext,
        // R.color.synthesizeColor));
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

        buttonAnalyze = viewRoot.findViewById(R.id.button_ana);
        buttonSynthesize = viewRoot.findViewById(R.id.button_syn);

        spinnerW = viewRoot.findViewById(R.id.spinner_W);
        spinnerL = viewRoot.findViewById(R.id.spinner_L);
        spinnerZ0 = viewRoot.findViewById(R.id.spinner_Z0);
        spinnerPhs = viewRoot.findViewById(R.id.spinner_Phs);
        spinnerFreq = viewRoot.findViewById(R.id.spinner_Freq);
        spinnerT = viewRoot.findViewById(R.id.spinner_T);
        spinnerH = viewRoot.findViewById(R.id.spinner_H);

        spinnerW.setAdapter(Constants.adapterDimensionUnits(mContext));
        spinnerL.setAdapter(Constants.adapterDimensionUnits(mContext));
        spinnerT.setAdapter(Constants.adapterDimensionUnits(mContext));
        spinnerH.setAdapter(Constants.adapterDimensionUnits(mContext));

        spinnerZ0.setAdapter(Constants.adapterImpedanceUnits(mContext));

        spinnerPhs.setAdapter(Constants.adapterPhaseUnits(mContext));

        spinnerFreq.setAdapter(Constants.adapterFrequencyUnits(mContext));
    }

    private void readSharedPref() {
        SharedPreferences prefs = mContext.getSharedPreferences(Constants.SHARED_PREFS_NAME,
                AppCompatActivity.MODE_PRIVATE);

        editTextW.setText(prefs.getString(Constants.MLIN_W, "2.9"));
        spinnerW.setSelection(
                Integer.parseInt(prefs.getString(Constants.MLIN_W_UNIT, Integer.toString(Constants.LengthUnit_mm))));

        editTextL.setText(prefs.getString(Constants.MLIN_L, "40"));
        spinnerL.setSelection(
                Integer.parseInt(prefs.getString(Constants.MLIN_L_UNIT, Integer.toString(Constants.LengthUnit_mm))));

        editTextZ0.setText(prefs.getString(Constants.MLIN_Z0, "50.0"));
        spinnerZ0.setSelection(Integer
                .parseInt(prefs.getString(Constants.MLIN_Z0_UNIT, Integer.toString(Constants.ImpedanceUnit_Ohm))));

        editTextPhs.setText(prefs.getString(Constants.MLIN_PHS, "90"));
        spinnerPhs.setSelection(Integer
                .parseInt(prefs.getString(Constants.MLIN_PHS_UNIT, Integer.toString(Constants.PhaseUnit_Degree))));

        editTextFreq.setText(prefs.getString(Constants.MLIN_FREQ, "1.00"));
        spinnerFreq.setSelection(
                Integer.parseInt(prefs.getString(Constants.MLIN_FREQ_UNIT, Integer.toString(Constants.FreqUnit_GHz))));

        editTextEr.setText(prefs.getString(Constants.MLIN_ER, "4.6"));

        editTextH.setText(prefs.getString(Constants.MLIN_H, "1.6"));
        spinnerH.setSelection(
                Integer.parseInt(prefs.getString(Constants.MLIN_H_UNIT, Integer.toString(Constants.LengthUnit_mm))));

        editTextT.setText(prefs.getString(Constants.MLIN_T, "0.035"));
        spinnerT.setSelection(
                Integer.parseInt(prefs.getString(Constants.MLIN_T_UNIT, Integer.toString(Constants.LengthUnit_mm))));

        target = Integer.parseInt(prefs.getString(Constants.MLIN_TARGET, Integer.toString(Constants.Synthesize_Width)));
    }

    private void setRadioBtn() {
        if (target == Constants.Synthesize_Width) {
            radioButtonW.setChecked(true);
            editTextW.setBackgroundTintList(getResources().getColorStateList(R.color.background_tint_synthesize));
            editTextW.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
            radioButtonH.setChecked(false);
            editTextH.setBackgroundTintList(
                    getResources().getColorStateList(R.color.background_tint_default_synthesize));
            editTextH.setTextColor(defaultEditTextColor);
        } else {
            target = Constants.Synthesize_Height;
            radioButtonW.setChecked(false);
            editTextW.setBackgroundTintList(
                    getResources().getColorStateList(R.color.background_tint_default_synthesize));
            editTextW.setTextColor(defaultEditTextColor);
            radioButtonH.setChecked(true);
            editTextH.setBackgroundTintList(getResources().getColorStateList(R.color.background_tint_synthesize));
            editTextH.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
        }

        radioButtonW.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                radioButtonW.setChecked(true);
                editTextW.setBackgroundTintList(getResources().getColorStateList(R.color.background_tint_synthesize));
                editTextW.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
                radioButtonH.setChecked(false);
                editTextH.setBackgroundTintList(
                        getResources().getColorStateList(R.color.background_tint_default_synthesize));
                editTextH.setTextColor(defaultEditTextColor);
                target = Constants.Synthesize_Width;
            }
        });
        radioButtonH.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                radioButtonW.setChecked(false);
                editTextW.setBackgroundTintList(
                        getResources().getColorStateList(R.color.background_tint_default_synthesize));
                editTextW.setTextColor(defaultEditTextColor);
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

        editor.putString(Constants.MLIN_W, editTextW.getText().toString());
        editor.putString(Constants.MLIN_W_UNIT, Integer.toString(spinnerW.getSelectedItemPosition()));
        editor.putString(Constants.MLIN_L, editTextL.getText().toString());
        editor.putString(Constants.MLIN_L_UNIT, Integer.toString(spinnerL.getSelectedItemPosition()));
        editor.putString(Constants.MLIN_Z0, editTextZ0.getText().toString());
        editor.putString(Constants.MLIN_Z0_UNIT, Integer.toString(spinnerZ0.getSelectedItemPosition()));
        editor.putString(Constants.MLIN_PHS, editTextPhs.getText().toString());
        editor.putString(Constants.MLIN_PHS_UNIT, Integer.toString(spinnerPhs.getSelectedItemPosition()));
        editor.putString(Constants.MLIN_FREQ, editTextFreq.getText().toString());
        editor.putString(Constants.MLIN_FREQ_UNIT, Integer.toString(spinnerFreq.getSelectedItemPosition()));
        editor.putString(Constants.MLIN_ER, editTextEr.getText().toString());
        editor.putString(Constants.MLIN_H, editTextH.getText().toString());
        editor.putString(Constants.MLIN_H_UNIT, Integer.toString(spinnerH.getSelectedItemPosition()));
        editor.putString(Constants.MLIN_T, editTextT.getText().toString());
        editor.putString(Constants.MLIN_T_UNIT, Integer.toString(spinnerT.getSelectedItemPosition()));
        editor.putString(Constants.MLIN_TARGET, Integer.toString(target));
        editor.apply();
    }

    private boolean analysisInputEmptyCheck() {
        boolean checkResult = true;
        if (editTextT.length() == 0) {
            textInputLayoutT.setError(getText(R.string.Error_T_empty));
            checkResult = false;
        }
        if (editTextH.length() == 0) {
            textInputLayoutH.setError(getText(R.string.Error_H_empty));
            checkResult = false;
        } else if (Constants.value2meter(Double.parseDouble(editTextH.getText().toString()),
                spinnerH.getSelectedItemPosition()) < Constants.MINI_LIMIT) {
            textInputLayoutH.setError(getText(R.string.unreasonable_value));
            checkResult = false;
        }
        if (editTextW.length() == 0) {
            textInputLayoutW.setError(getText(R.string.Error_W_empty));
            checkResult = false;
        } else if (Constants.value2meter(Double.parseDouble(editTextW.getText().toString()),
                spinnerW.getSelectedItemPosition()) < Constants.MINI_LIMIT) {
            textInputLayoutW.setError(getText(R.string.unreasonable_value));
            checkResult = false;
        }
        if (editTextEr.length() == 0) {
            textInputLayoutEr.setError(getText(R.string.error_er_empty));
            checkResult = false;
        } else if (Double.parseDouble(editTextEr.getText().toString()) < 1) {
            textInputLayoutEr.setError(getText(R.string.unreasonable_value));
            checkResult = false;
        }
        if (editTextFreq.length() == 0) {
            textInputLayoutF.setError(getText(R.string.Error_Freq_empty));
            checkResult = false;
        } else if (Double.parseDouble(editTextFreq.getText().toString()) == 0) {
            textInputLayoutF.setError(getText(R.string.error_zero_frequency));
            checkResult = false;
        }

        if (!checkResult) {
            editTextZ0.setText("");
            editTextPhs.setText("");
        }

        return checkResult;
    }

    private boolean synthesizeInputEmptyCheck() {
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

        if (editTextT.length() == 0) {
            textInputLayoutT.setError(getText(R.string.Error_T_empty));
            checkResult = false;
        }

        if (editTextEr.length() == 0) {
            textInputLayoutEr.setError(getText(R.string.error_er_empty));
            checkResult = false;
        } else if (Double.parseDouble(editTextEr.getText().toString()) < 1) {
            textInputLayoutEr.setError(getText(R.string.unreasonable_value));
            checkResult = false;
        }

        if (target == Constants.Synthesize_Width) {
            if (editTextH.length() == 0) {
                textInputLayoutH.setError(getText(R.string.Error_H_empty));
                checkResult = false;
            }
            if (!checkResult) {
                editTextW.setText("");
            }
        } else if (target == Constants.Synthesize_Height) {
            if (editTextW.length() == 0) {
                textInputLayoutW.setError(getText(R.string.Error_W_empty));
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
        textInputLayoutZ0.setError(null);
        textInputLayoutZ0.setErrorEnabled(false);
        textInputLayoutH.setError(null);
        textInputLayoutH.setErrorEnabled(false);
        textInputLayoutEr.setError(null);
        textInputLayoutEr.setErrorEnabled(false);
        textInputLayoutF.setError(null);
        textInputLayoutF.setErrorEnabled(false);
    }

    public void resetValues() {
        editTextW.setText("2.9");
        spinnerW.setSelection(Constants.LengthUnit_mm);
        editTextL.setText("40");
        spinnerL.setSelection(Constants.LengthUnit_mm);
        editTextZ0.setText("50.0");
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

        radioButtonW.setChecked(true);
        editTextW.setBackgroundTintList(getResources().getColorStateList(R.color.background_tint_synthesize));
        editTextW.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
        radioButtonH.setChecked(false);
        editTextH.setBackgroundTintList(
                getResources().getColorStateList(R.color.background_tint_default_synthesize));
        editTextH.setTextColor(defaultEditTextColor);
    }
}
