package com.rookiedev.microwavetools.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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
    private int decimalLength; // the length of the Decimal, accurate of the result
    private View viewRoot;
    private CardView cardViewParameters, cardViewDimensions;
    private TextView textZ0, textK, textZ0o, textZ0e;
    private EditText edittextW, edittextS, edittextL, edittextZ0, edittextK, edittextZ0o, edittextZ0e, edittextPhs,
            edittextFreq, edittextT, edittextH, edittextEr;
    private CmlinModel line;
    private Button buttonSynthesize, buttonAnalyze;
    private Spinner spinnerW, spinnerS, spinnerL, spinnerT, spinnerH, spinnerZ0, spinnerZ0o, spinnerZ0e, spinnerPhs,
            spinnerFreq;
    private RadioButton radioButtonZ0, radioButtonK, radioButtonZ0o, radioButtonZ0e;
    private boolean useZ0k; // calculate with Z0, k, or Z0e, Z0o
    private AdFragment adFragment = null;

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

        initUI();
        readSharedPref();
        setRadioBtn();

        buttonAnalyze.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Preference_SharedPref();
                if (!analysisInputCheck()) {
                    edittextZ0.setText(""); // clear the Z0 and Eeff outputs
                    edittextPhs.setText("");
                    edittextZ0o.setText(""); //
                    edittextZ0e.setText(""); //
                    edittextK.setText("");
                } else {
                    line.setMetalWidth(Double.parseDouble(edittextW.getText().toString()),
                            spinnerW.getSelectedItemPosition()); // get the header_parameters
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

                        CmlinCalculator cmlin = new CmlinCalculator();
                        line = cmlin.getAnaResult(line);

                        BigDecimal Eeff_temp = new BigDecimal(line.getElectricalLength()); // cut the decimal of the Eeff
                        double Eeff = Eeff_temp.setScale(decimalLength, BigDecimal.ROUND_HALF_UP).doubleValue();
                        edittextPhs.setText(String.valueOf(Eeff));
                    } else {
                        CmlinCalculator cmlin = new CmlinCalculator();
                        line = cmlin.getAnaResult(line);
                        edittextPhs.setText(""); // if the L input is empty, clear the Eeff

                    }
                    BigDecimal Z0_temp = new BigDecimal(line.getImpedance());
                    double Z0 = Z0_temp.setScale(decimalLength, BigDecimal.ROUND_HALF_UP).doubleValue();
                    edittextZ0.setText(String.valueOf(Z0)); // cut the decimal
                    // of the Z0
                    BigDecimal k_temp = new BigDecimal(line.getCouplingFactor());
                    double k = k_temp.setScale(decimalLength, BigDecimal.ROUND_HALF_UP).doubleValue();
                    edittextK.setText(String.valueOf(k));

                    BigDecimal Z0o_temp = new BigDecimal(line.getImpedanceOdd());
                    double Z0o = Z0o_temp.setScale(decimalLength, BigDecimal.ROUND_HALF_UP).doubleValue();
                    edittextZ0o.setText(String.valueOf(Z0o));

                    BigDecimal Z0e_temp = new BigDecimal(line.getImpedanceEven());
                    double Z0e = Z0e_temp.setScale(decimalLength, BigDecimal.ROUND_HALF_UP).doubleValue();
                    edittextZ0e.setText(String.valueOf(Z0e));
                }
                forceRippleAnimation(cardViewParameters);
            }
        });

        buttonSynthesize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Preference_SharedPref();
                if (!synthesizeInputCheck()) {
                    edittextL.setText(""); // clear the L and W outputs
                    edittextS.setText("");
                    edittextW.setText("");
                } else {
                    synthesizeButton();
                    if (useZ0k) {
                        double Z0e, Z0o;

                        BigDecimal Z0o_temp = new BigDecimal(line.getImpedanceOdd());
                        Z0o = Z0o_temp.setScale(decimalLength, BigDecimal.ROUND_HALF_UP).doubleValue();
                        edittextZ0o.setText(String.valueOf(Z0o));

                        BigDecimal Z0e_temp = new BigDecimal(line.getImpedanceEven());
                        Z0e = Z0e_temp.setScale(decimalLength, BigDecimal.ROUND_HALF_UP).doubleValue();
                        edittextZ0e.setText(String.valueOf(Z0e));
                    } else {
                        double Z0, k;

                        BigDecimal Z0_temp = new BigDecimal(line.getImpedance());
                        Z0 = Z0_temp.setScale(decimalLength, BigDecimal.ROUND_HALF_UP).doubleValue();
                        edittextZ0.setText(String.valueOf(Z0)); // cut the decimal of the Z0
                        BigDecimal k_temp = new BigDecimal(line.getCouplingFactor());
                        k = k_temp.setScale(decimalLength, BigDecimal.ROUND_HALF_UP).doubleValue();
                        edittextK.setText(String.valueOf(k));
                    }
                }
                forceRippleAnimation(cardViewDimensions);
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

        cardViewDimensions = viewRoot.findViewById(R.id.card_dimensions);
        cardViewParameters = viewRoot.findViewById(R.id.card_parameters);

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

        TextView textW = viewRoot.findViewById(R.id.text_W);
        textW.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));

        TextView textS = viewRoot.findViewById(R.id.text_S);
        textS.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));

        TextView textL = viewRoot.findViewById(R.id.text_L);
        textL.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));

        TextView textPhs = viewRoot.findViewById(R.id.text_Phs);
        textPhs.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor));

        TextView textEr = viewRoot.findViewById(R.id.text_er);
        textEr.append(Constants.stringEr(mContext));

        textZ0 = viewRoot.findViewById(R.id.text_Z0);
        textZ0.append(Constants.stringZ0(mContext));
        textK = viewRoot.findViewById(R.id.text_k);

        textZ0o = viewRoot.findViewById(R.id.text_Z0o);
        textZ0o.append(Constants.stringZ0o(mContext));
        textZ0e = viewRoot.findViewById(R.id.text_Z0e);
        textZ0e.append(Constants.stringZ0e(mContext));

        // edittext elements
        edittextW = viewRoot.findViewById(R.id.editText_W);
        edittextW.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
        edittextS = viewRoot.findViewById(R.id.editText_S);
        edittextS.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
        edittextL = viewRoot.findViewById(R.id.editText_L);
        edittextL.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
        edittextZ0 = viewRoot.findViewById(R.id.editText_Z0);
        edittextK = viewRoot.findViewById(R.id.editText_k);
        edittextZ0o = viewRoot.findViewById(R.id.editText_Z0o);
        edittextZ0e = viewRoot.findViewById(R.id.editText_Z0e);
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
                textZ0.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor));
                textK.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor));
                textZ0e.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColorLight));
                textZ0o.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColorLight));
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
                textZ0.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor));
                textK.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor));
                textZ0e.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColorLight));
                textZ0o.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColorLight));
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
                textZ0.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColorLight));
                textK.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColorLight));
                textZ0e.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor));
                textZ0o.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor));
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
                textZ0.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColorLight));
                textK.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColorLight));
                textZ0e.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor));
                textZ0o.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor));
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
            textZ0.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor));
            textK.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor));
            textZ0e.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColorLight));
            textZ0o.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColorLight));
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
            textZ0.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColorLight));
            textK.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColorLight));
            textZ0e.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor));
            textZ0o.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor));
        }
    }

    private void readSharedPref() {
        SharedPreferences prefs = mContext.getSharedPreferences(Constants.SHARED_PREFS_NAME,
                AppCompatActivity.MODE_PRIVATE);// get the header_parameters from the Shared

        edittextW.setText(prefs.getString(Constants.CMLIN_W, "20.00"));
        spinnerW.setSelection(Integer.parseInt(prefs.getString(Constants.CMLIN_W_UNIT, "0")));

        edittextS.setText(prefs.getString(Constants.CMLIN_S, "20.00"));
        spinnerS.setSelection(Integer.parseInt(prefs.getString(Constants.CMLIN_S_UNIT, "0")));

        edittextL.setText(prefs.getString(Constants.CMLIN_L, "1000.00"));
        spinnerL.setSelection(Integer.parseInt(prefs.getString(Constants.CMLIN_L_UNIT, "0")));

        edittextZ0.setText(prefs.getString(Constants.CMLIN_Z0, "50.6"));
        spinnerZ0.setSelection(Integer.parseInt(prefs.getString(Constants.CMLIN_Z0_UNIT, "0")));

        edittextK.setText(prefs.getString(Constants.CMLIN_K, "0.06"));

        edittextZ0o.setText(prefs.getString(Constants.CMLIN_Z0O, "47.65"));
        spinnerZ0o.setSelection(Integer.parseInt(prefs.getString(Constants.CMLIN_Z0O_UNIT, "0")));

        edittextZ0e.setText(prefs.getString(Constants.CMLIN_Z0E, "53.73"));
        spinnerZ0e.setSelection(Integer.parseInt(prefs.getString(Constants.CMLIN_Z0E_UNIT, "0")));

        edittextPhs.setText(prefs.getString(Constants.CMLIN_PHS, "53.33"));
        spinnerPhs.setSelection(Integer.parseInt(prefs.getString(Constants.CMLIN_PHS_UNIT, "0")));

        edittextFreq.setText(prefs.getString(Constants.CMLIN_FREQ, "1.00"));
        spinnerFreq.setSelection(Integer.parseInt(prefs.getString(Constants.CMLIN_FREQ_UNIT, "1")));

        edittextEr.setText(prefs.getString(Constants.CMLIN_ER, "4.00"));

        edittextH.setText(prefs.getString(Constants.CMLIN_H, "10.00"));
        spinnerH.setSelection(Integer.parseInt(prefs.getString(Constants.CMLIN_H_UNIT, "0")));

        edittextT.setText(prefs.getString(Constants.CMLIN_T, "1.40"));
        spinnerT.setSelection(Integer.parseInt(prefs.getString(Constants.CMLIN_T_UNIT, "0")));

        useZ0k = prefs.getString(Constants.CMLIN_USEZ0K, "true").equals("true");
    }

    private void Preference_SharedPref() {
        SharedPreferences prefs = mContext.getSharedPreferences(Constants.SHARED_PREFS_NAME,
                AppCompatActivity.MODE_PRIVATE);// get the header_parameters from the Shared Preferences in the device universal header_parameters
        decimalLength = Integer.parseInt(prefs.getString("decimalLength", "2"));
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
        editor.putString(Constants.CMLIN_T, edittextT.getText().toString());
        editor.putString(Constants.CMLIN_T_UNIT, Integer.toString(spinnerT.getSelectedItemPosition()));
        if (useZ0k) {
            editor.putString(Constants.CMLIN_USEZ0K, "true");
        }else{
            editor.putString(Constants.CMLIN_USEZ0K, "false");
        }
        editor.apply();
    }

    private boolean analysisInputCheck() {
        boolean checkResult = true;
        if (edittextW.length() == 0) {
            edittextW.setError(getText(R.string.Error_W_empty));
            checkResult = false;
        }
        if (edittextS.length() == 0) {
            edittextS.setError(getText(R.string.Error_S_empty));
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
        if (useZ0k) {
            if (edittextZ0.length() == 0) {
                edittextZ0.setError(Constants.errorZ0Empty(mContext));
                checkResult = false;
            }
            if (edittextK.length() == 0) {
                edittextK.setError(getText(R.string.Error_k_empty));
                checkResult = false;
            }
        } else {
            if (edittextZ0e.length() == 0) {
                edittextZ0e.setError(Constants.errorZ0eEmpty(mContext));
                checkResult = false;
            }
            if (edittextZ0o.length() == 0) {
                edittextZ0o.setError(Constants.errorZ0oEmpty(mContext));
                checkResult = false;
            }
        }
        return checkResult;
    }

    private void synthesizeButton() {
        int temp;
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

        double W, S, L;
        if (edittextPhs.length() != 0) { // check if the Eeff is empty
            line.setElectricalLength(Double.parseDouble(edittextPhs.getText().toString()));
            CmlinCalculator cmlin = new CmlinCalculator();
            line = cmlin.getSynResult(line, useZ0k);

            BigDecimal L_temp = new BigDecimal(
                    Constants.meter2others(line.getMetalLength(), spinnerL.getSelectedItemPosition())); // cut the decimal of L
            L = L_temp.setScale(decimalLength, BigDecimal.ROUND_HALF_UP).doubleValue();
            edittextL.setText(String.valueOf(L));

            BigDecimal W_temp = new BigDecimal(
                    Constants.meter2others(line.getMetalWidth(), spinnerW.getSelectedItemPosition())); // cut the decimal of W
            W = W_temp.setScale(decimalLength, BigDecimal.ROUND_HALF_UP).doubleValue();
            edittextW.setText(String.valueOf(W));

            BigDecimal S_temp = new BigDecimal(
                    Constants.meter2others(line.getMetalSpace(), spinnerS.getSelectedItemPosition())); // cut the decimal of S
            S = S_temp.setScale(decimalLength, BigDecimal.ROUND_HALF_UP).doubleValue();
            edittextS.setText(String.valueOf(S));
        } else {
            CmlinCalculator cmlin = new CmlinCalculator();
            line = cmlin.getSynResult(line, useZ0k);
            edittextL.setText(""); // clear the L if the Eeff input is empty

            BigDecimal W_temp = new BigDecimal(
                    Constants.meter2others(line.getMetalWidth(), spinnerW.getSelectedItemPosition())); // cut the decimal of W
            W = W_temp.setScale(decimalLength, BigDecimal.ROUND_HALF_UP).doubleValue();
            edittextW.setText(String.valueOf(W));

            BigDecimal S_temp = new BigDecimal(
                    Constants.meter2others(line.getMetalSpace(), spinnerS.getSelectedItemPosition())); // cut the decimal of S
            S = S_temp.setScale(decimalLength, BigDecimal.ROUND_HALF_UP).doubleValue();
            edittextS.setText(String.valueOf(S));
        }
    }

    protected void forceRippleAnimation(View view) {
        if (Build.VERSION.SDK_INT >= 23) {
            view.setClickable(true);
            Drawable background = view.getForeground();
            final RippleDrawable rippleDrawable = (RippleDrawable) background;

            rippleDrawable.setState(new int[] { android.R.attr.state_pressed, android.R.attr.state_enabled });

            view.setClickable(false);
            rippleDrawable.setState(new int[] {});
        }
    }

    public void addAdFragment() {
        adFragment = new AdFragment();
        FragmentManager fragmentManager = getFragmentManager();
        if (fragmentManager != null && viewRoot != null) {
            fragmentManager.beginTransaction().replace(R.id.ad_frame, adFragment).commit();
        }
    }

    public void removeAdFragment() {
        if (adFragment != null) {
            FragmentManager fragmentManager = getFragmentManager();
            if (fragmentManager != null) {
                fragmentManager.beginTransaction().remove(adFragment).commit();
            }
        }
    }
}
