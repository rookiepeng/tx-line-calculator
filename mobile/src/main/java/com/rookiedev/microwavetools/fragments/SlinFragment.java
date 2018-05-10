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
import com.rookiedev.microwavetools.libs.Constants;
import com.rookiedev.microwavetools.libs.SlinCalculator;
import com.rookiedev.microwavetools.libs.SlinModel;

import java.math.BigDecimal;

public class SlinFragment extends Fragment {
    private Context mContext;
    private View viewRoot;
    private TextView textW, textH;
    private EditText edittextW, edittextL, edittextZ0, edittextPhs, edittextFreq, edittextT, edittextH, edittextEr;
    private Button buttonSynthesize, buttonAnalyze;
    private Spinner spinnerW, spinnerL, spinnerT, spinnerH, spinnerZ0, spinnerPhs, spinnerFreq;
    private int target;
    private RadioButton radioButtonW, radioButtonH;
    private SlinModel line;
    private ColorStateList defaultTextColor, defaultEdittextColor;
    private AdFragment adFragment = null;
    private FragmentManager fragmentManager = null;

    private boolean isAdFree;

    public SlinFragment() {
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
        viewRoot = inflater.inflate(R.layout.fragment_slin, container, false);
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
                if (!analysisInputCheck()) {
                    edittextZ0.setText(""); // clear the Z0 and Eeff outputs
                    edittextPhs.setText("");
                } else {
                    line.setMetalWidth(Double.parseDouble(edittextW.getText().toString()),
                            spinnerW.getSelectedItemPosition());
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

                        SlinCalculator slin = new SlinCalculator();
                        line = slin.getAnaResult(line);
                        //Eeff = fragment_slin.getEeff();

                        BigDecimal Eeff_temp = new BigDecimal(line.getElectricalLength()); // cut the decimal of the Eeff
                        double Eeff = Eeff_temp.setScale(Constants.DecimalLength, BigDecimal.ROUND_HALF_UP).doubleValue();
                        edittextPhs.setText(String.valueOf(Eeff));

                    } else {
                        line.setMetalLength(0, spinnerL.getSelectedItemPosition());
                        SlinCalculator slin = new SlinCalculator();
                        line = slin.getAnaResult(line);
                        edittextPhs.setText(""); // if the L input is empty, clear the Eeff
                    }
                    BigDecimal Z0_temp = new BigDecimal(line.getImpedance());
                    double Z0 = Z0_temp.setScale(Constants.DecimalLength, BigDecimal.ROUND_HALF_UP).doubleValue();
                    edittextZ0.setText(String.valueOf(Z0)); // cut the decimal of the Z0
                }
            }
        });

        buttonSynthesize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Constants.refreshAnimation(mContext, (ImageView) viewRoot.findViewById(R.id.synthesize_reveal),
                        Constants.SYNTHESIZE);
                if (!synthesizeInputCheck()) {
                    if (target == Constants.Synthesize_Width) {
                        edittextW.setText("");
                    } else if (target == Constants.Synthesize_Height) {
                        edittextH.setText("");
                    } else if (target == Constants.Synthesize_Er) {
                        edittextEr.setText("");
                    }
                } else {
                    line.setImpedance(Double.parseDouble(edittextZ0.getText().toString()));
                    line.setFrequency(Double.parseDouble(edittextFreq.getText().toString()),
                            spinnerFreq.getSelectedItemPosition());
                    line.setMetalThick(Double.parseDouble(edittextT.getText().toString()),
                            spinnerT.getSelectedItemPosition());

                    if (target == Constants.Synthesize_Width) {
                        line.setSubHeight(Double.parseDouble(edittextH.getText().toString()),
                                spinnerH.getSelectedItemPosition());
                        line.setSubEpsilon(Double.parseDouble(edittextEr.getText().toString()));
                        line.setMetalWidth(0, Constants.LengthUnit_m);
                    } else if (target == Constants.Synthesize_Height) {
                        line.setMetalWidth(Double.parseDouble(edittextW.getText().toString()),
                                spinnerW.getSelectedItemPosition());
                        line.setSubEpsilon(Double.parseDouble(edittextEr.getText().toString()));
                        line.setSubHeight(0, Constants.LengthUnit_m);
                    } else if (target == Constants.Synthesize_Er) {
                        line.setMetalWidth(Double.parseDouble(edittextW.getText().toString()),
                                spinnerW.getSelectedItemPosition());
                        line.setSubHeight(Double.parseDouble(edittextH.getText().toString()),
                                spinnerH.getSelectedItemPosition());
                        line.setSubEpsilon(0);
                    }

                    if (edittextPhs.length() != 0) { // check if the Eeff is empty
                        line.setElectricalLength(Double.parseDouble(edittextPhs.getText().toString()));
                        SlinCalculator slin = new SlinCalculator();
                        line = slin.getSynResult(line, target);

                        BigDecimal L_temp = new BigDecimal(
                                Constants.meter2others(line.getMetalLength(), spinnerL.getSelectedItemPosition())); // cut the decimal of L
                        double L = L_temp.setScale(Constants.DecimalLength, BigDecimal.ROUND_HALF_UP).doubleValue();
                        edittextL.setText(String.valueOf(L));
                    } else {
                        SlinCalculator slin = new SlinCalculator();
                        line = slin.getSynResult(line, target);
                        edittextL.setText(""); // clear the L if the Eeff input is empty
                    }
                    if (target == Constants.Synthesize_Width) {

                        BigDecimal W_temp = new BigDecimal(
                                Constants.meter2others(line.getMetalWidth(), spinnerW.getSelectedItemPosition())); // cut the decimal of W
                        double W = W_temp.setScale(Constants.DecimalLength, BigDecimal.ROUND_HALF_UP).doubleValue();
                        edittextW.setText(String.valueOf(W));
                    } else if (target == Constants.Synthesize_Height) {
                        BigDecimal H_temp = new BigDecimal(
                                Constants.meter2others(line.getSubHeight(), spinnerH.getSelectedItemPosition()));
                        double H = H_temp.setScale(Constants.DecimalLength, BigDecimal.ROUND_HALF_UP).doubleValue();
                        edittextH.setText(String.valueOf(H));
                    } else if (target == Constants.Synthesize_Er) {
                        BigDecimal er_temp = new BigDecimal(line.getSubEpsilon());
                        double er = er_temp.setScale(Constants.DecimalLength, BigDecimal.ROUND_HALF_UP).doubleValue();
                        edittextEr.setText(String.valueOf(er));
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
        line = new SlinModel();

        RadioButton radioButtonPhs = viewRoot.findViewById(R.id.radioBtn_Phs);
        radioButtonPhs.setVisibility(View.VISIBLE);
        radioButtonPhs.setChecked(true);

        RadioButton radioButtonZ0 = viewRoot.findViewById(R.id.radioBtn_Z0);
        radioButtonZ0.setVisibility(View.VISIBLE);
        radioButtonZ0.setChecked(true);

        RadioButton radioButtonL = viewRoot.findViewById(R.id.radioBtn_L);
        radioButtonL.setVisibility(View.VISIBLE);
        radioButtonL.setChecked(true);

        radioButtonW = viewRoot.findViewById(R.id.radioBtn_W);
        radioButtonW.setVisibility(View.VISIBLE);

        radioButtonH = viewRoot.findViewById(R.id.radioBtn_H);
        radioButtonH.setVisibility(View.VISIBLE);

        TextView textViewL = viewRoot.findViewById(R.id.text_L);
        textViewL.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));

        TextView textEr = viewRoot.findViewById(R.id.text_er);
        textEr.append(Constants.stringEr(mContext));

        TextView textZ0 = viewRoot.findViewById(R.id.text_Z0);
        textZ0.append(Constants.stringZ0o(mContext));
        textZ0.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor));

        TextView textPhs = viewRoot.findViewById(R.id.text_Phs);
        textPhs.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor));

        textW = viewRoot.findViewById(R.id.text_W);
        textH = viewRoot.findViewById(R.id.text_H);

        defaultTextColor = textW.getTextColors();

        // edittext elements
        edittextW = viewRoot.findViewById(R.id.editText_W);
        defaultEdittextColor = edittextW.getTextColors();

        edittextL = viewRoot.findViewById(R.id.editText_L);
        edittextL.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
        edittextZ0 = viewRoot.findViewById(R.id.editText_Z0);
        edittextZ0.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor));
        edittextPhs = viewRoot.findViewById(R.id.editText_Phs);
        edittextPhs.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor));
        edittextFreq = viewRoot.findViewById(R.id.editText_Freq);
        edittextT = viewRoot.findViewById(R.id.editText_T);
        edittextH = viewRoot.findViewById(R.id.editText_H);
        edittextEr = viewRoot.findViewById(R.id.editText_er);

        // button elements
        buttonAnalyze = viewRoot.findViewById(R.id.button_ana);
        buttonSynthesize = viewRoot.findViewById(R.id.button_syn);

        // spinner elements
        spinnerW = viewRoot.findViewById(R.id.spinner_W);
        spinnerL = viewRoot.findViewById(R.id.spinner_L);
        spinnerZ0 = viewRoot.findViewById(R.id.spinner_Z0);
        spinnerPhs = viewRoot.findViewById(R.id.spinner_Phs);
        spinnerFreq = viewRoot.findViewById(R.id.spinner_Freq);
        spinnerT = viewRoot.findViewById(R.id.spinner_T);
        spinnerH = viewRoot.findViewById(R.id.spinner_H);

        // configure the length units
        spinnerW.setAdapter(Constants.adapterDimensionUnits(mContext));
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
                AppCompatActivity.MODE_PRIVATE);// get the header_parameters from the Shared

        edittextW.setText(prefs.getString(Constants.SLIN_W, "10.00"));
        spinnerW.setSelection(Integer.parseInt(prefs.getString(Constants.SLIN_W_UNIT, "0")));

        edittextL.setText(prefs.getString(Constants.SLIN_L, "1000.00"));
        spinnerL.setSelection(Integer.parseInt(prefs.getString(Constants.SLIN_L_UNIT, "0")));

        edittextZ0.setText(prefs.getString(Constants.SLIN_Z0, "49.76"));
        spinnerZ0.setSelection(Integer.parseInt(prefs.getString(Constants.SLIN_Z0_UNIT, "0")));

        edittextPhs.setText(prefs.getString(Constants.SLIN_PHS, "61.00"));
        spinnerPhs.setSelection(Integer.parseInt(prefs.getString(Constants.SLIN_PHS_UNIT, "0")));

        edittextFreq.setText(prefs.getString(Constants.SLIN_FREQ, "1.00"));
        spinnerFreq.setSelection(Integer.parseInt(prefs.getString(Constants.SLIN_FREQ_UNIT, "1")));

        edittextEr.setText(prefs.getString(Constants.SLIN_ER, "4.00"));

        edittextH.setText(prefs.getString(Constants.SLIN_H, "25.00"));
        spinnerH.setSelection(Integer.parseInt(prefs.getString(Constants.SLIN_H_UNIT, "0")));

        edittextT.setText(prefs.getString(Constants.SLIN_T, "1.40"));
        spinnerT.setSelection(Integer.parseInt(prefs.getString(Constants.SLIN_T_UNIT, "0")));
        target = Integer.parseInt(prefs.getString(Constants.SLIN_TARGET, "0"));
    }

    private void setRadioBtn() {
        if (target == Constants.Synthesize_Width) {
            radioButtonW.setChecked(true);
            textW.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
            edittextW.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
            radioButtonH.setChecked(false);
            textH.setTextColor(defaultTextColor);
            edittextH.setTextColor(defaultEdittextColor);
        } else {
            target = Constants.Synthesize_Height;
            radioButtonW.setChecked(false);
            textW.setTextColor(defaultTextColor);
            edittextW.setTextColor(defaultEdittextColor);
            radioButtonH.setChecked(true);
            textH.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
            edittextH.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
        }
        radioButtonW.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                radioButtonW.setChecked(true);
                textW.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
                edittextW.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
                radioButtonH.setChecked(false);
                textH.setTextColor(defaultTextColor);
                edittextH.setTextColor(defaultEdittextColor);
                target = Constants.Synthesize_Width;
            }
        });
        radioButtonH.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                radioButtonW.setChecked(false);
                textW.setTextColor(defaultTextColor);
                edittextW.setTextColor(defaultEdittextColor);
                radioButtonH.setChecked(true);
                textH.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
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

        editor.putString(Constants.SLIN_W, edittextW.getText().toString());
        editor.putString(Constants.SLIN_W_UNIT, Integer.toString(spinnerW.getSelectedItemPosition()));
        editor.putString(Constants.SLIN_H, edittextH.getText().toString());
        editor.putString(Constants.SLIN_H_UNIT, Integer.toString(spinnerH.getSelectedItemPosition()));
        editor.putString(Constants.SLIN_ER, edittextEr.getText().toString());
        editor.putString(Constants.SLIN_L, edittextL.getText().toString());
        editor.putString(Constants.SLIN_L_UNIT, Integer.toString(spinnerL.getSelectedItemPosition()));
        editor.putString(Constants.SLIN_Z0, edittextZ0.getText().toString());
        editor.putString(Constants.SLIN_Z0_UNIT, Integer.toString(spinnerZ0.getSelectedItemPosition()));
        editor.putString(Constants.SLIN_PHS, edittextPhs.getText().toString());
        editor.putString(Constants.SLIN_PHS_UNIT, Integer.toString(spinnerPhs.getSelectedItemPosition()));
        editor.putString(Constants.SLIN_FREQ, edittextFreq.getText().toString());
        editor.putString(Constants.SLIN_FREQ_UNIT, Integer.toString(spinnerFreq.getSelectedItemPosition()));
        editor.putString(Constants.SLIN_T, edittextT.getText().toString());
        editor.putString(Constants.SLIN_T_UNIT, Integer.toString(spinnerT.getSelectedItemPosition()));
        editor.putString(Constants.SLIN_TARGET, Integer.toString(target));
        editor.apply();
    }

    private boolean analysisInputCheck() {
        boolean checkResult = true;
        if (edittextW.length() == 0) {
            edittextW.setError(getText(R.string.Error_W_empty));
            checkResult = false;
        }
        if (edittextFreq.length() == 0) {
            edittextFreq.setError(getText(R.string.Error_Freq_empty));
            checkResult = false;
        }
        if (edittextH.length() == 0) {
            edittextH.setError(getText(R.string.Error_H_empty));
            checkResult = false;
        }
        if (edittextT.length() == 0) {
            edittextT.setError(getText(R.string.Error_T_empty));
            checkResult = false;
        }
        if (edittextEr.length() == 0) {
            edittextEr.setError(Constants.errorErEmpty(mContext));
            checkResult = false;
        }
        return checkResult;
    }

    private boolean synthesizeInputCheck() {
        boolean checkResult = true;
        if (edittextZ0.length() == 0) {
            edittextZ0.setError(Constants.errorZ0Empty(mContext));
            checkResult = false;
        }
        if (edittextFreq.length() == 0) {
            edittextFreq.setError(getText(R.string.Error_Freq_empty));
            checkResult = false;
        }
        if (edittextT.length() == 0) {
            edittextT.setError(getText(R.string.Error_T_empty));
            checkResult = false;
        }
        if (edittextEr.length() == 0) {
            edittextEr.setError(Constants.errorErEmpty(mContext));
            checkResult = false;
        }
        if (target == Constants.Synthesize_Width) {
            if (edittextH.length() == 0) {
                edittextH.setError(getText(R.string.Error_H_empty));
                checkResult = false;
            }
        } else if (target == Constants.Synthesize_Height) {
            if (edittextW.length() == 0) {
                edittextW.setError(getText(R.string.Error_W_empty));
                checkResult = false;
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
}
