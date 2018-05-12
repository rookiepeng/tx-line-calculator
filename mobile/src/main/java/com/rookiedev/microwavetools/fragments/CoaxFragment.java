package com.rookiedev.microwavetools.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
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
import com.rookiedev.microwavetools.libs.CoaxCalculator;
import com.rookiedev.microwavetools.libs.CoaxModel;
import com.rookiedev.microwavetools.libs.Constants;

import java.math.BigDecimal;

public class CoaxFragment extends Fragment {
    private Context mContext;
    private View viewRoot;
    private TextView textA, textB, textC;
    private EditText edittextA, edittextB, edittextC, edittextL, edittextZ0, edittextPhs, edittextFreq, edittextEr;
    private Button buttonSynthesize, buttonAnalyze;
    private Spinner spinnerA, spinnerB, spinnerC, spinnerL, spinnerZ0, spinnerPhs, spinnerFreq;
    private int target;
    private RadioButton radioButtonA, radioButtonB, radioButtonC;
    private CoaxModel line;
    private ColorStateList defaultTextColor, defaultEdittextColor;
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
                    line.setCoreRadius(Double.parseDouble(edittextA.getText().toString()),
                            spinnerA.getSelectedItemPosition());
                    line.setSubRadius(Double.parseDouble(edittextB.getText().toString()),
                            spinnerB.getSelectedItemPosition());
                    line.setCoreOffset(Double.parseDouble(edittextC.getText().toString()),
                            spinnerC.getSelectedItemPosition());
                    line.setFrequency(Double.parseDouble(edittextFreq.getText().toString()),
                            spinnerFreq.getSelectedItemPosition());
                    line.setSubEpsilon(Double.parseDouble(edittextEr.getText().toString()));

                    if (!edittextL.getText().toString().equals("")) {
                        line.setMetalLength(Double.parseDouble(edittextL.getText().toString()),
                                spinnerL.getSelectedItemPosition());
                    } else {
                        line.setMetalLength(0, spinnerL.getSelectedItemPosition());
                    }

                    CoaxCalculator coax = new CoaxCalculator();
                    line = coax.getAnaResult(line);

                    if (line.getErrorCode() == Constants.ERROR.NO_ERROR) {
                        if (!edittextL.getText().toString().equals("")) {
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
                    } else if (line.getErrorCode() == Constants.ERROR.SUBSTRATE_TOO_LARGE) {
                        edittextZ0.setText("");
                        edittextPhs.setText("");
                        edittextA.setError(getString(R.string.substrate_too_large));
                        edittextA.requestFocus();
                        edittextB.setError(getString(R.string.substrate_too_large));
                    } else if (line.getErrorCode() == Constants.ERROR.OFFSET_TOO_LARGE) {
                        edittextZ0.setText("");
                        edittextPhs.setText("");
                        edittextC.setError(getString(R.string.offset_too_large));
                        edittextC.requestFocus();
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
                    line.setImpedance(Double.parseDouble(edittextZ0.getText().toString()));
                    line.setFrequency(Double.parseDouble(edittextFreq.getText().toString()),
                            spinnerFreq.getSelectedItemPosition());
                    line.setSubEpsilon(Double.parseDouble(edittextEr.getText().toString()));

                    if (target == Constants.Synthesize_CoreRadius) { // a = 0;
                        line.setSubRadius(Double.parseDouble(edittextB.getText().toString()),
                                spinnerB.getSelectedItemPosition());
                        line.setCoreOffset(Double.parseDouble(edittextC.getText().toString()),
                                spinnerC.getSelectedItemPosition());
                    } else if (target == Constants.Synthesize_SubRadius) { // b = 0;
                        line.setCoreRadius(Double.parseDouble(edittextA.getText().toString()),
                                spinnerA.getSelectedItemPosition());
                        line.setCoreOffset(Double.parseDouble(edittextC.getText().toString()),
                                spinnerC.getSelectedItemPosition());
                    } else if (target == Constants.Synthesize_CoreOffset) { // c = 0;
                        line.setCoreRadius(Double.parseDouble(edittextA.getText().toString()),
                                spinnerA.getSelectedItemPosition());
                        line.setSubRadius(Double.parseDouble(edittextB.getText().toString()),
                                spinnerB.getSelectedItemPosition());
                    }

                    if (edittextPhs.length() != 0) {
                        line.setPhase(Double.parseDouble(edittextPhs.getText().toString()));
                    } else {
                        line.setPhase(0);
                    }

                    CoaxCalculator coax = new CoaxCalculator();
                    line = coax.getSynResult(line, target);

                    if (line.getErrorCode() == Constants.ERROR.NO_ERROR) {
                        if (edittextPhs.length() != 0) {
                            BigDecimal L_temp = new BigDecimal(
                                    Constants.meter2others(line.getMetalLength(), spinnerL.getSelectedItemPosition()));
                            double L = L_temp.setScale(Constants.DecimalLength, BigDecimal.ROUND_HALF_UP).doubleValue();
                            edittextL.setText(String.valueOf(L));
                        } else {
                            edittextL.setText("");
                        }

                        if (target == Constants.Synthesize_CoreRadius) {
                            if ((Double.isNaN(line.getCoreRadius()) || Double.isInfinite(line.getCoreRadius()))) {
                                edittextA.setText("");
                                edittextA.setError(getString(R.string.synthesize_failed));
                                edittextA.requestFocus();
                            } else {
                                BigDecimal a_temp = new BigDecimal(Constants.meter2others(line.getCoreRadius(),
                                        spinnerA.getSelectedItemPosition()));
                                double a = a_temp.setScale(Constants.DecimalLength, BigDecimal.ROUND_HALF_UP)
                                        .doubleValue();
                                edittextA.setText(String.valueOf(a));
                            }
                        } else if (target == Constants.Synthesize_SubRadius) {
                            if ((Double.isNaN(line.getSubRadius()) || Double.isInfinite(line.getSubRadius()))) {
                                edittextB.setText("");
                                edittextB.setError(getString(R.string.synthesize_failed));
                                edittextB.requestFocus();
                            } else {
                                BigDecimal b_temp = new BigDecimal(Constants.meter2others(line.getSubRadius(),
                                        spinnerB.getSelectedItemPosition()));
                                double height = b_temp.setScale(Constants.DecimalLength, BigDecimal.ROUND_HALF_UP)
                                        .doubleValue();
                                edittextB.setText(String.valueOf(height));
                            }
                        } else if (target == Constants.Synthesize_CoreOffset) {
                            if ((Double.isNaN(line.getCoreOffset()) || Double.isInfinite(line.getCoreOffset()))) {
                                edittextC.setText("");
                                edittextC.setError(getString(R.string.synthesize_failed));
                                edittextC.requestFocus();
                            } else {
                                BigDecimal c_temp = new BigDecimal(Constants.meter2others(line.getCoreOffset(),
                                        spinnerC.getSelectedItemPosition()));
                                double b = c_temp.setScale(Constants.DecimalLength, BigDecimal.ROUND_HALF_UP)
                                        .doubleValue();
                                edittextC.setText(String.valueOf(b));
                            }
                        }
                    } else {
                        if (target == Constants.Synthesize_CoreRadius) {
                            edittextA.setText("");
                            edittextA.setError(getString(R.string.synthesize_failed));
                            edittextA.requestFocus();
                        } else if (target == Constants.Synthesize_SubRadius) {
                            edittextB.setText("");
                            edittextB.setError(getString(R.string.synthesize_failed));
                            edittextB.requestFocus();
                        } else if (target == Constants.Synthesize_CoreOffset) {
                            edittextC.setText("");
                            edittextC.setError(getString(R.string.synthesize_failed));
                            edittextC.requestFocus();
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
        radioButtonB = viewRoot.findViewById(R.id.radioBtn_b);
        radioButtonB.setVisibility(View.VISIBLE);

        // Subscript strings
        TextView textEr = viewRoot.findViewById(R.id.text_er);
        defaultTextColor = textEr.getTextColors();
        textEr.append(Constants.stringEr(mContext));

        TextView textZ0 = viewRoot.findViewById(R.id.text_Z0);
        textZ0.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor));
        textZ0.append(Constants.stringZ0(mContext));

        TextView textPhs = viewRoot.findViewById(R.id.text_Phs);
        textPhs.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor));

        TextView text_L = viewRoot.findViewById(R.id.text_L);
        text_L.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));

        textA = viewRoot.findViewById(R.id.text_a);
        textB = viewRoot.findViewById(R.id.text_b);
        textC = viewRoot.findViewById(R.id.text_c);

        // edittext elements
        edittextA = viewRoot.findViewById(R.id.editText_a);
        defaultEdittextColor = edittextA.getTextColors();
        edittextC = viewRoot.findViewById(R.id.editText_c);
        edittextB = viewRoot.findViewById(R.id.editText_b);
        edittextL = viewRoot.findViewById(R.id.editText_L);
        edittextL.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
        edittextZ0 = viewRoot.findViewById(R.id.editText_Z0);
        edittextZ0.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor));
        edittextPhs = viewRoot.findViewById(R.id.editText_Phs);
        edittextPhs.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor));
        edittextFreq = viewRoot.findViewById(R.id.editText_Freq);
        edittextEr = viewRoot.findViewById(R.id.editText_er);

        // button elements
        buttonAnalyze = viewRoot.findViewById(R.id.button_ana);
        buttonSynthesize = viewRoot.findViewById(R.id.button_syn);

        // spinner elements
        spinnerA = viewRoot.findViewById(R.id.spinner_a);
        spinnerC = viewRoot.findViewById(R.id.spinner_c);
        spinnerB = viewRoot.findViewById(R.id.spinner_b);
        spinnerL = viewRoot.findViewById(R.id.spinner_L);
        spinnerZ0 = viewRoot.findViewById(R.id.spinner_Z0);
        spinnerPhs = viewRoot.findViewById(R.id.spinner_Phs);
        spinnerFreq = viewRoot.findViewById(R.id.spinner_Freq);

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

        edittextA.setText(prefs.getString(Constants.COAX_A, "0.167"));
        spinnerA.setSelection(
                Integer.parseInt(prefs.getString(Constants.COAX_A_UNIT, Integer.toString(Constants.LengthUnit_mm))));

        edittextB.setText(prefs.getString(Constants.COAX_B, "1.00"));
        spinnerB.setSelection(
                Integer.parseInt(prefs.getString(Constants.COAX_B_UNIT, Integer.toString(Constants.LengthUnit_mm))));

        edittextC.setText(prefs.getString(Constants.COAX_C, "0.00"));
        spinnerC.setSelection(
                Integer.parseInt(prefs.getString(Constants.COAX_C_UNIT, Integer.toString(Constants.LengthUnit_mm))));

        edittextL.setText(prefs.getString(Constants.COAX_L, "34.945"));
        spinnerL.setSelection(
                Integer.parseInt(prefs.getString(Constants.COAX_L_UNIT, Integer.toString(Constants.LengthUnit_mm))));

        edittextZ0.setText(prefs.getString(Constants.COAX_Z0, "50"));
        spinnerZ0.setSelection(Integer
                .parseInt(prefs.getString(Constants.COAX_Z0_UNIT, Integer.toString(Constants.ImpedanceUnit_Ohm))));

        edittextPhs.setText(prefs.getString(Constants.COAX_PHS, "90"));
        spinnerPhs.setSelection(Integer
                .parseInt(prefs.getString(Constants.COAX_PHS_UNIT, Integer.toString(Constants.PhaseUnit_Degree))));

        edittextFreq.setText(prefs.getString(Constants.COAX_FREQ, "1.00"));
        spinnerFreq.setSelection(
                Integer.parseInt(prefs.getString(Constants.COAX_FREQ_UNIT, Integer.toString(Constants.FreqUnit_GHz))));

        edittextEr.setText(prefs.getString(Constants.COAX_ER, "4.6"));

        target = Integer
                .parseInt(prefs.getString(Constants.COAX_TARGET, Integer.toString(Constants.Synthesize_CoreRadius)));
    }

    private void setRadioBtn() {
        if (target == Constants.Synthesize_CoreRadius) {
            radioButtonA.setChecked(true);
            textA.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
            edittextA.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
            radioButtonB.setChecked(false);
            textB.setTextColor(defaultTextColor);
            edittextB.setTextColor(defaultEdittextColor);
            radioButtonC.setChecked(false);
            textC.setTextColor(defaultTextColor);
            edittextC.setTextColor(defaultEdittextColor);
        } else if (target == Constants.Synthesize_SubRadius) {
            radioButtonA.setChecked(false);
            textA.setTextColor(defaultTextColor);
            edittextA.setTextColor(defaultEdittextColor);
            radioButtonB.setChecked(true);
            textB.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
            edittextB.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
            radioButtonC.setChecked(false);
            textC.setTextColor(defaultTextColor);
            edittextC.setTextColor(defaultEdittextColor);
        } else if (target == Constants.Synthesize_CoreOffset) {
            radioButtonA.setChecked(false);
            textA.setTextColor(defaultTextColor);
            edittextA.setTextColor(defaultEdittextColor);
            radioButtonB.setChecked(false);
            textB.setTextColor(defaultTextColor);
            edittextB.setTextColor(defaultEdittextColor);
            radioButtonC.setChecked(true);
            textC.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
            edittextC.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
        } else {
            target = Constants.Synthesize_CoreRadius;
            radioButtonA.setChecked(true);
            textA.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
            edittextA.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
            radioButtonB.setChecked(false);
            textB.setTextColor(defaultTextColor);
            edittextB.setTextColor(defaultEdittextColor);
            radioButtonC.setChecked(false);
            textC.setTextColor(defaultTextColor);
            edittextC.setTextColor(defaultEdittextColor);
        }
        radioButtonA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                radioButtonA.setChecked(true);
                textA.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
                edittextA.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
                radioButtonB.setChecked(false);
                textB.setTextColor(defaultTextColor);
                edittextB.setTextColor(defaultEdittextColor);
                radioButtonC.setChecked(false);
                textC.setTextColor(defaultTextColor);
                edittextC.setTextColor(defaultEdittextColor);
                target = Constants.Synthesize_CoreRadius;
            }
        });
        radioButtonB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                radioButtonA.setChecked(false);
                textA.setTextColor(defaultTextColor);
                edittextA.setTextColor(defaultEdittextColor);
                radioButtonB.setChecked(true);
                textB.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
                edittextB.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
                radioButtonC.setChecked(false);
                textC.setTextColor(defaultTextColor);
                edittextC.setTextColor(defaultEdittextColor);
                target = Constants.Synthesize_Height;
            }
        });
        radioButtonC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                radioButtonA.setChecked(false);
                textA.setTextColor(defaultTextColor);
                edittextA.setTextColor(defaultEdittextColor);
                radioButtonB.setChecked(false);
                textB.setTextColor(defaultTextColor);
                edittextB.setTextColor(defaultEdittextColor);
                radioButtonC.setChecked(true);
                textC.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
                edittextC.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
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

        editor.putString(Constants.COAX_A, edittextA.getText().toString());
        editor.putString(Constants.COAX_A_UNIT, Integer.toString(spinnerA.getSelectedItemPosition()));
        editor.putString(Constants.COAX_B, edittextB.getText().toString());
        editor.putString(Constants.COAX_B_UNIT, Integer.toString(spinnerB.getSelectedItemPosition()));
        editor.putString(Constants.COAX_C, edittextC.getText().toString());
        editor.putString(Constants.COAX_C_UNIT, Integer.toString(spinnerC.getSelectedItemPosition()));
        editor.putString(Constants.COAX_ER, edittextEr.getText().toString());
        editor.putString(Constants.COAX_L, edittextL.getText().toString());
        editor.putString(Constants.COAX_L_UNIT, Integer.toString(spinnerL.getSelectedItemPosition()));
        editor.putString(Constants.COAX_Z0, edittextZ0.getText().toString());
        editor.putString(Constants.COAX_Z0_UNIT, Integer.toString(spinnerZ0.getSelectedItemPosition()));
        editor.putString(Constants.COAX_PHS, edittextPhs.getText().toString());
        editor.putString(Constants.COAX_PHS_UNIT, Integer.toString(spinnerPhs.getSelectedItemPosition()));
        editor.putString(Constants.COAX_FREQ, edittextFreq.getText().toString());
        editor.putString(Constants.COAX_FREQ_UNIT, Integer.toString(spinnerFreq.getSelectedItemPosition()));
        editor.putString(Constants.COAX_TARGET, Integer.toString(target));

        editor.apply();
    }

    private boolean analysisInputCheck() {
        boolean checkResult = true;
        if (edittextA.length() == 0) {
            edittextA.setError(getText(R.string.Error_a_empty));
            checkResult = false;
        } else if (Constants.value2meter(Double.parseDouble(edittextA.getText().toString()),
                spinnerA.getSelectedItemPosition()) < Constants.MINI_LIMIT) {
            edittextA.setError(getText(R.string.unreasonable_value));
            checkResult = false;
        }

        if (edittextFreq.length() == 0) {
            edittextFreq.setError(getText(R.string.Error_Freq_empty));
            checkResult = false;
        } else if (Double.parseDouble(edittextFreq.getText().toString()) == 0) {
            edittextFreq.setError(getText(R.string.error_zero_frequency));
            checkResult = false;
        }

        if (edittextB.length() == 0) {
            edittextB.setError(getText(R.string.Error_b_empty));
            checkResult = false;
        } else if (Constants.value2meter(Double.parseDouble(edittextB.getText().toString()),
                spinnerB.getSelectedItemPosition()) < Constants.MINI_LIMIT) {
            edittextB.setError(getText(R.string.unreasonable_value));
            checkResult = false;
        }

        if (edittextC.length() == 0) {
            edittextC.setError(getText(R.string.Error_c_empty));
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
        }

        return checkResult;
    }

    private boolean synthesizeInputCheck() {
        boolean checkResult = true;
        if (edittextZ0.length() == 0) {
            edittextZ0.setError(Constants.errorZ0Empty(mContext));
            checkResult = false;
        } else if (Double.parseDouble(edittextZ0.getText().toString()) == 0) {
            edittextZ0.setError(getText(R.string.unreasonable_value));
            checkResult = false;
        }

        if (edittextFreq.length() == 0) {
            edittextFreq.setError(getText(R.string.Error_Freq_empty));
            checkResult = false;
        } else if (Double.parseDouble(edittextFreq.getText().toString()) == 0) {
            edittextFreq.setError(getText(R.string.error_zero_frequency));
            checkResult = false;
        }

        if (edittextEr.length() == 0) {
            edittextEr.setError(Constants.errorErEmpty(mContext));
            checkResult = false;
        } else if (Double.parseDouble(edittextEr.getText().toString()) < 1) {
            edittextEr.setError(getText(R.string.unreasonable_value));
            checkResult = false;
        }

        if (target == Constants.Synthesize_CoreRadius) {
            if (edittextB.length() == 0) {
                edittextB.setError(getText(R.string.Error_b_empty));
                checkResult = false;
            }
            if (edittextC.length() == 0) {
                edittextC.setError(getText(R.string.Error_c_empty));
                checkResult = false;
            }

            if (!checkResult) {
                edittextA.setText("");
            }
        } else if (target == Constants.Synthesize_SubRadius) {
            if (edittextA.length() == 0) {
                edittextA.setError(getText(R.string.Error_a_empty));
                checkResult = false;
            }
            if (edittextC.length() == 0) {
                edittextC.setError(getText(R.string.Error_c_empty));
                checkResult = false;
            }

            if (!checkResult) {
                edittextB.setText("");
            }
        } else if (target == Constants.Synthesize_CoreOffset) {
            if (edittextA.length() == 0) {
                edittextA.setError(getText(R.string.Error_a_empty));
                checkResult = false;
            }
            if (edittextB.length() == 0) {
                edittextB.setError(getText(R.string.Error_b_empty));
                checkResult = false;
            }

            if (!checkResult) {
                edittextC.setText("");
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
        edittextA.setError(null);
        edittextB.setError(null);
        edittextC.setError(null);
        edittextZ0.setError(null);
        edittextEr.setError(null);
        edittextFreq.setError(null);
    }
}
