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
import com.rookiedev.microwavetools.libs.Constants;
import com.rookiedev.microwavetools.libs.CslinCalculator;
import com.rookiedev.microwavetools.libs.CslinModel;

import java.math.BigDecimal;

public class CslinFragment extends Fragment {
    private Context mContext;
    private View viewRoot;
    private CardView cardViewParameters, cardViewDimensions;
    private TextView textZ0, textK, textZ0o, textZ0e;
    private EditText edittextW, edittextS, edittextL, edittextZ0, edittextK, edittextZ0o, edittextZ0e, edittextPhs,
            edittextFreq, edittextT, edittextH, edittextEr;
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
                if (!analysisInputCheck()) {
                    edittextZ0.setText(""); // clear the Z0 and Eeff outputs
                    edittextPhs.setText("");
                    edittextZ0o.setText(""); //
                    edittextZ0e.setText(""); //
                    edittextK.setText("");
                } else {
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

                        CslinCalculator cslin = new CslinCalculator();
                        line = cslin.getAnaResult(line);

                        BigDecimal Eeff_temp = new BigDecimal(line.getElectricalLength()); // cut the decimal of the Eeff
                        double Eeff = Eeff_temp.setScale(Constants.DecimalLength, BigDecimal.ROUND_HALF_UP).doubleValue();
                        edittextPhs.setText(String.valueOf(Eeff));

                    } else {
                        CslinCalculator cslin = new CslinCalculator();
                        line = cslin.getAnaResult(line);
                        edittextPhs.setText(""); // if the L input is empty, clear the Eeff
                    }

                    BigDecimal Z0_temp = new BigDecimal(line.getImpedance());
                    double Z0 = Z0_temp.setScale(Constants.DecimalLength, BigDecimal.ROUND_HALF_UP).doubleValue();
                    edittextZ0.setText(String.valueOf(Z0)); // cut the decimal
                    // of the Z0
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
                forceRippleAnimation(cardViewParameters);
            }
        });

        buttonSynthesize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!synthesizeInputCheck()) {
                    edittextL.setText(""); // clear the L and W outputs
                    edittextS.setText("");
                    edittextW.setText("");
                } else {
                    synthesizeButton();
                    if (useZ0k) {
                        line.setImpedanceOdd(line.getImpedance()
                                * Math.sqrt((1.0 - line.getCouplingFactor()) / (1.0 + line.getCouplingFactor())));
                        line.setImpedanceEven(line.getImpedance()
                                * Math.sqrt((1.0 + line.getCouplingFactor()) / (1.0 - line.getCouplingFactor())));

                        BigDecimal Z0o_temp = new BigDecimal(line.getImpedanceOdd());
                        double Z0o = Z0o_temp.setScale(Constants.DecimalLength, BigDecimal.ROUND_HALF_UP).doubleValue();
                        edittextZ0o.setText(String.valueOf(Z0o));

                        BigDecimal Z0e_temp = new BigDecimal(line.getImpedanceEven());
                        double Z0e = Z0e_temp.setScale(Constants.DecimalLength, BigDecimal.ROUND_HALF_UP).doubleValue();
                        edittextZ0e.setText(String.valueOf(Z0e));
                    } else {
                        line.setImpedance(Math.sqrt(line.getImpedanceEven() * line.getImpedanceOdd()));
                        line.setCouplingFactor((line.getImpedanceEven() - line.getImpedanceOdd())
                                / (line.getImpedanceEven() + line.getImpedanceOdd()));

                        BigDecimal Z0_temp = new BigDecimal(line.getImpedance());
                        double Z0 = Z0_temp.setScale(Constants.DecimalLength, BigDecimal.ROUND_HALF_UP).doubleValue();
                        edittextZ0.setText(String.valueOf(Z0)); // cut the decimal of the Z0
                        BigDecimal k_temp = new BigDecimal(line.getCouplingFactor());
                        double k = k_temp.setScale(Constants.DecimalLength, BigDecimal.ROUND_HALF_UP).doubleValue();
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
        line = new CslinModel();

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

        edittextW.setText(prefs.getString(Constants.CSLIN_W, "50.00"));
        spinnerW.setSelection(Integer.parseInt(prefs.getString(Constants.CSLIN_W_UNIT, "0")));

        edittextS.setText(prefs.getString(Constants.CSLIN_S, "20.00"));
        spinnerS.setSelection(Integer.parseInt(prefs.getString(Constants.CSLIN_S_UNIT, "0")));

        edittextL.setText(prefs.getString(Constants.CSLIN_L, "1000.00"));
        spinnerL.setSelection(Integer.parseInt(prefs.getString(Constants.CSLIN_L_UNIT, "0")));

        edittextZ0.setText(prefs.getString(Constants.CSLIN_Z0, "33.42"));
        spinnerZ0.setSelection(Integer.parseInt(prefs.getString(Constants.CSLIN_Z0_UNIT, "0")));

        edittextK.setText(prefs.getString(Constants.CSLIN_K, "0.10"));

        edittextZ0o.setText(prefs.getString(Constants.CSLIN_Z0O, "30.35"));
        spinnerZ0o.setSelection(Integer.parseInt(prefs.getString(Constants.CSLIN_Z0O_UNIT, "0")));

        edittextZ0e.setText(prefs.getString(Constants.CSLIN_Z0E, "36.69"));
        spinnerZ0e.setSelection(Integer.parseInt(prefs.getString(Constants.CSLIN_Z0E_UNIT, "0")));

        edittextPhs.setText(prefs.getString(Constants.CSLIN_PHS, "61.00"));
        spinnerPhs.setSelection(Integer.parseInt(prefs.getString(Constants.CSLIN_PHS_UNIT, "0")));

        edittextFreq.setText(prefs.getString(Constants.CSLIN_FREQ, "1.00"));
        spinnerFreq.setSelection(Integer.parseInt(prefs.getString(Constants.CSLIN_FREQ_UNIT, "1")));

        edittextEr.setText(prefs.getString(Constants.CSLIN_ER, "4.00"));

        edittextH.setText(prefs.getString(Constants.CSLIN_H, "60.00"));
        spinnerH.setSelection(Integer.parseInt(prefs.getString(Constants.CSLIN_H_UNIT, "0")));

        edittextT.setText(prefs.getString(Constants.CSLIN_T, "2.80"));
        spinnerT.setSelection(Integer.parseInt(prefs.getString(Constants.CSLIN_T_UNIT, "0")));

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
        if (useZ0k) {
            line.setImpedance(Double.parseDouble(edittextZ0.getText().toString()));
            line.setCouplingFactor(Double.parseDouble(edittextK.getText().toString()));
        } else {
            line.setImpedanceEven(Double.parseDouble(edittextZ0e.getText().toString()));
            line.setImpedanceOdd(Double.parseDouble(edittextZ0o.getText().toString()));
        }

        line.setFrequency(Double.parseDouble(edittextFreq.getText().toString()), spinnerFreq.getSelectedItemPosition());
        line.setSubEpsilon(Double.parseDouble(edittextEr.getText().toString()));
        line.setSubHeight(Double.parseDouble(edittextH.getText().toString()), spinnerH.getSelectedItemPosition());
        line.setMetalThick(Double.parseDouble(edittextT.getText().toString()), spinnerT.getSelectedItemPosition());

        if (edittextPhs.length() != 0) { // check if the Eeff is empty
            line.setElectricalLength(Double.parseDouble(edittextPhs.getText().toString()));
            CslinCalculator cslin = new CslinCalculator();
            line = cslin.getSynResult(line, useZ0k);

            BigDecimal L_temp = new BigDecimal(
                    Constants.meter2others(line.getMetalLength(), spinnerL.getSelectedItemPosition())); // cut the decimal of L
            double L = L_temp.setScale(Constants.DecimalLength, BigDecimal.ROUND_HALF_UP).doubleValue();
            edittextL.setText(String.valueOf(L));
        } else {
            CslinCalculator cslin = new CslinCalculator();
            line = cslin.getSynResult(line, useZ0k);
            edittextL.setText(""); // clear the L if the Eeff input is empty
        }

        BigDecimal W_temp = new BigDecimal(
                Constants.meter2others(line.getMetalWidth(), spinnerW.getSelectedItemPosition())); // cut the decimal of W
        double W = W_temp.setScale(Constants.DecimalLength, BigDecimal.ROUND_HALF_UP).doubleValue();
        edittextW.setText(String.valueOf(W));

        BigDecimal S_temp = new BigDecimal(
                Constants.meter2others(line.getMetalSpace(), spinnerS.getSelectedItemPosition())); // cut the decimal of S
        double S = S_temp.setScale(Constants.DecimalLength, BigDecimal.ROUND_HALF_UP).doubleValue();
        edittextS.setText(String.valueOf(S));
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
