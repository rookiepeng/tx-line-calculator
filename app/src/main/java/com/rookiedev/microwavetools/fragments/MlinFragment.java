
package com.rookiedev.microwavetools.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
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
import com.rookiedev.microwavetools.libs.MlinModel;
import com.rookiedev.microwavetools.libs.MlinCalculator;

import java.math.BigDecimal;

public class MlinFragment extends Fragment {
    private Context mContext;
    private View viewRoot;
    private CardView cardViewParameters, cardViewDimensions;
    private int DecimalLength; // the length of the Decimal, accurate of the result
    private RadioButton radioButtonW, radioButtonH;
    private TextView textW, textH;
    private EditText editTextW, editTextL, editTextZ0, editTextPhs, editTextFreq, editTextT, editTextH, editTextEr;
    private Button buttonSynthesize, buttonAnalyze;
    private Spinner spinnerW, spinnerL, spinnerT, spinnerH, spinnerZ0, spinnerPhs, spinnerFreq;
    private MlinModel line;
    private int target;
    private ColorStateList defaultTextColor, defaultEdittextColor;
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
                Preference_SharedPref();
                if (!analysisInputCheck()) {
                    editTextZ0.setText(""); // clear the Z0 and Eeff outputs
                    editTextPhs.setText("");
                } else {
                    line.setMetalWidth(Double.parseDouble(editTextW.getText().toString()),
                            spinnerW.getSelectedItemPosition()); // get the header_parameters
                    line.setFrequency(Double.parseDouble(editTextFreq.getText().toString()),
                            spinnerFreq.getSelectedItemPosition());
                    line.setSubEpsilon(Double.parseDouble(editTextEr.getText().toString()));
                    line.setSubHeight(Double.parseDouble(editTextH.getText().toString()),
                            spinnerH.getSelectedItemPosition());
                    line.setMetalThick(Double.parseDouble(editTextT.getText().toString()),
                            spinnerT.getSelectedItemPosition());

                    if (editTextL.length() != 0) { // check the L input
                        line.setMetalLength(Double.parseDouble(editTextL.getText().toString()),
                                spinnerL.getSelectedItemPosition());
                        MlinCalculator mlin = new MlinCalculator();
                        line = mlin.getAnaResult(line);

                        BigDecimal Z0_temp = new BigDecimal(line.getImpedance());
                        double Z0 = Z0_temp.setScale(DecimalLength, BigDecimal.ROUND_HALF_UP).doubleValue();
                        editTextZ0.setText(String.valueOf(Z0)); // cut the decimal

                        BigDecimal Eeff_temp = new BigDecimal(line.getElectricalLength()); // cut the decimal of the Eeff
                        double Eeff = Eeff_temp.setScale(DecimalLength, BigDecimal.ROUND_HALF_UP).doubleValue();
                        editTextPhs.setText(String.valueOf(Eeff));
                    } else {
                        MlinCalculator mlin = new MlinCalculator();
                        line = mlin.getAnaResult(line);

                        BigDecimal Z0_temp = new BigDecimal(line.getImpedance());
                        double Z0 = Z0_temp.setScale(DecimalLength, BigDecimal.ROUND_HALF_UP).doubleValue();
                        editTextZ0.setText(String.valueOf(Z0)); // cut the decimal

                        editTextPhs.setText(""); // if the L input is empty, clear the Eeff
                    }
                }
                forceRippleAnimation(cardViewParameters);
            }
        });

        buttonSynthesize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Preference_SharedPref();
                if (!synthesizeInputCheck()) {
                    if (target == Constants.Synthesize_Width) {
                        editTextW.setText("");
                    } else if (target == Constants.Synthesize_Height) {
                        editTextH.setText("");
                    }
                } else {
                    line.setImpedance(Double.parseDouble(editTextZ0.getText().toString()));
                    line.setFrequency(Double.parseDouble(editTextFreq.getText().toString()),
                            spinnerFreq.getSelectedItemPosition());
                    line.setSubEpsilon(Double.parseDouble(editTextEr.getText().toString()));
                    line.setSubHeight(Double.parseDouble(editTextH.getText().toString()),
                            spinnerH.getSelectedItemPosition());
                    line.setMetalThick(Double.parseDouble(editTextT.getText().toString()),
                            spinnerT.getSelectedItemPosition());

                    if (target == Constants.Synthesize_Width) {
                        line.setSubHeight(Double.parseDouble(editTextH.getText().toString()),
                                spinnerH.getSelectedItemPosition());
                        line.setSubEpsilon(Double.parseDouble(editTextEr.getText().toString()));
                        line.setMetalWidth(0, Constants.LengthUnit_m);
                    } else if (target == Constants.Synthesize_Height) {
                        line.setMetalWidth(Double.parseDouble(editTextW.getText().toString()),
                                spinnerW.getSelectedItemPosition());
                        line.setSubEpsilon(Double.parseDouble(editTextEr.getText().toString()));
                        line.setSubHeight(0, Constants.LengthUnit_m);
                    }

                    if (editTextPhs.length() != 0) {
                        line.setElectricalLength(Double.parseDouble(editTextPhs.getText().toString()));
                        MlinCalculator mlin = new MlinCalculator();
                        line = mlin.getSynResult(line, target);
                        BigDecimal L_temp = new BigDecimal(
                                Constants.meter2others(line.getMetalLength(), spinnerL.getSelectedItemPosition())); // cut the decimal of L
                        double L = L_temp.setScale(DecimalLength, BigDecimal.ROUND_HALF_UP).doubleValue();
                        editTextL.setText(String.valueOf(L));

                        BigDecimal W_temp = new BigDecimal(
                                Constants.meter2others(line.getMetalWidth(), spinnerW.getSelectedItemPosition())); // cut the decimal of W
                        double W = W_temp.setScale(DecimalLength, BigDecimal.ROUND_HALF_UP).doubleValue();
                        editTextW.setText(String.valueOf(W));
                    } else {
                        MlinCalculator mlin = new MlinCalculator();
                        line = mlin.getSynResult(line, Constants.Synthesize_Width);
                        editTextL.setText(""); // clear the L if the Eeff input is empty

                        BigDecimal W_temp = new BigDecimal(
                                Constants.meter2others(line.getMetalWidth(), spinnerW.getSelectedItemPosition())); // cut the decimal of W
                        double W = W_temp.setScale(DecimalLength, BigDecimal.ROUND_HALF_UP).doubleValue();
                        editTextW.setText(String.valueOf(W));
                    }
                    if (target == Constants.Synthesize_Width) {

                        BigDecimal W_temp = new BigDecimal(
                                Constants.meter2others(line.getMetalWidth(), spinnerW.getSelectedItemPosition())); // cut the decimal of W
                        double W = W_temp.setScale(DecimalLength, BigDecimal.ROUND_HALF_UP).doubleValue();
                        editTextW.setText(String.valueOf(W));
                    } else if (target == Constants.Synthesize_Height) {
                        BigDecimal H_temp = new BigDecimal(
                                Constants.meter2others(line.getSubHeight(), spinnerH.getSelectedItemPosition()));
                        double H = H_temp.setScale(DecimalLength, BigDecimal.ROUND_HALF_UP).doubleValue();
                        editTextH.setText(String.valueOf(H));
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

    /*
     * initialize the UI
     */
    private void initUI() {
        line = new MlinModel();

        cardViewDimensions = viewRoot.findViewById(R.id.card_dimensions);
        cardViewParameters = viewRoot.findViewById(R.id.card_parameters);

        radioButtonW = viewRoot.findViewById(R.id.radioBtn_W);
        radioButtonW.setVisibility(View.VISIBLE);
        // radioButtonW.setChecked(true);

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

        textW = viewRoot.findViewById(R.id.text_W);
        textH = viewRoot.findViewById(R.id.text_H);

        defaultTextColor = textW.getTextColors();

        //textW.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
        //textH.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));

        TextView textL = viewRoot.findViewById(R.id.text_L);
        textL.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));

        TextView textPhs = viewRoot.findViewById(R.id.text_Phs);
        textPhs.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor));

        TextView textZ0 = viewRoot.findViewById(R.id.text_Z0);
        textZ0.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor));
        textZ0.append(Constants.stringZ0(mContext));

        TextView textEr = viewRoot.findViewById(R.id.text_er);
        textEr.append(Constants.stringEr(mContext));

        // edittext elements
        editTextW = viewRoot.findViewById(R.id.editText_W);
        defaultEdittextColor = editTextW.getTextColors();
        editTextW.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
        editTextL = viewRoot.findViewById(R.id.editText_L);
        editTextL.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
        editTextZ0 = viewRoot.findViewById(R.id.editText_Z0);
        editTextZ0.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor));
        editTextPhs = viewRoot.findViewById(R.id.editText_Phs);
        editTextPhs.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor));
        editTextFreq = viewRoot.findViewById(R.id.editText_Freq);
        editTextT = viewRoot.findViewById(R.id.editText_T);
        editTextH = viewRoot.findViewById(R.id.editText_H);
        editTextEr = viewRoot.findViewById(R.id.editText_er);

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

        // configure the dimension units
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

    private void setRadioBtn() {
        if (target == Constants.Synthesize_Width) {
            radioButtonW.setChecked(true);
            textW.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
            editTextW.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
            radioButtonH.setChecked(false);
            textH.setTextColor(defaultTextColor);
            editTextH.setTextColor(defaultEdittextColor);
        } else {
            target = Constants.Synthesize_Height;
            radioButtonW.setChecked(false);
            textW.setTextColor(defaultTextColor);
            editTextW.setTextColor(defaultEdittextColor);
            radioButtonH.setChecked(true);
            textH.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
            editTextH.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
        }
        radioButtonW.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                radioButtonW.setChecked(true);
                textW.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
                editTextW.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
                radioButtonH.setChecked(false);
                textH.setTextColor(defaultTextColor);
                editTextH.setTextColor(defaultEdittextColor);
                target = Constants.Synthesize_Width;
            }
        });
        radioButtonH.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                radioButtonW.setChecked(false);
                textW.setTextColor(defaultTextColor);
                editTextW.setTextColor(defaultEdittextColor);
                radioButtonH.setChecked(true);
                textH.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
                editTextH.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
                target = Constants.Synthesize_Height;
            }
        });
    }

    private void readSharedPref() {
        SharedPreferences prefs = mContext.getSharedPreferences(Constants.SHARED_PREFS_NAME,
                AppCompatActivity.MODE_PRIVATE);// get the header_parameters from the Shared Preferences in the device

        // read values from the shared preferences

        // fragment_mlin header_parameters
        editTextW.setText(prefs.getString(Constants.MLIN_W, "19.23"));
        spinnerW.setSelection(Integer.parseInt(prefs.getString(Constants.MLIN_W_UNIT, "0")));

        editTextL.setText(prefs.getString(Constants.MLIN_L, "1000.00"));
        spinnerL.setSelection(Integer.parseInt(prefs.getString(Constants.MLIN_L_UNIT, "0")));

        editTextZ0.setText(prefs.getString(Constants.MLIN_Z0, "50.0"));
        spinnerZ0.setSelection(Integer.parseInt(prefs.getString(Constants.MLIN_Z0_UNIT, "0")));

        editTextPhs.setText(prefs.getString(Constants.MLIN_PHS, "52.58"));
        spinnerPhs.setSelection(Integer.parseInt(prefs.getString(Constants.MLIN_PHS_UNIT, "0")));

        editTextFreq.setText(prefs.getString(Constants.MLIN_FREQ, "1.00"));
        spinnerFreq.setSelection(Integer.parseInt(prefs.getString(Constants.MLIN_FREQ_UNIT, "1")));

        editTextEr.setText(prefs.getString(Constants.MLIN_ER, "4.00"));

        editTextH.setText(prefs.getString(Constants.MLIN_H, "10.00"));
        spinnerH.setSelection(Integer.parseInt(prefs.getString(Constants.MLIN_H_UNIT, "0")));

        editTextT.setText(prefs.getString(Constants.MLIN_T, "1.40"));
        spinnerT.setSelection(Integer.parseInt(prefs.getString(Constants.MLIN_T_UNIT, "0")));

        target = Integer.parseInt(prefs.getString(Constants.MLIN_TARGET, "0"));
    }

    private void Preference_SharedPref() {
        SharedPreferences prefs = mContext.getSharedPreferences(Constants.SHARED_PREFS_NAME,
                AppCompatActivity.MODE_PRIVATE);// get the header_parameters from the Shared
        // Preferences in the device
        // universal header_parameters
        DecimalLength = Integer.parseInt(prefs.getString("DecimalLength", "2"));
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

    private boolean analysisInputCheck() {
        boolean checkResult = true;
        if (editTextW.length() == 0) {
            editTextW.setError(getText(R.string.Error_W_empty));
            checkResult = false;
        }
        if (editTextFreq.length() == 0) {
            editTextFreq.setError(getText(R.string.Error_Freq_empty));
            checkResult = false;
        }
        if (editTextH.length() == 0) {
            editTextH.setError(getText(R.string.Error_H_empty));
            checkResult = false;
        }
        if (editTextT.length() == 0) {
            editTextT.setError(getText(R.string.Error_T_empty));
            checkResult = false;
        }
        if (editTextEr.length() == 0) {
            editTextEr.setError(Constants.errorErEmpty(mContext));
            checkResult = false;
        }
        return checkResult;
    }

    private boolean synthesizeInputCheck() {
        boolean checkResult = true;
        if (editTextZ0.length() == 0) {
            editTextZ0.setError(Constants.errorZ0Empty(mContext));
            checkResult = false;
        }
        if (editTextFreq.length() == 0) {
            editTextFreq.setError(getText(R.string.Error_Freq_empty));
            checkResult = false;
        }
        if (editTextT.length() == 0) {
            editTextT.setError(getText(R.string.Error_T_empty));
            checkResult = false;
        }
        if (editTextEr.length() == 0) {
            editTextEr.setError(Constants.errorErEmpty(mContext));
            checkResult = false;
        }
        if (target == Constants.Synthesize_Width) {
            if (editTextH.length() == 0) {
                editTextH.setError(getText(R.string.Error_H_empty));
                checkResult = false;
            }
        } else if (target == Constants.Synthesize_Height) {
            if (editTextW.length() == 0) {
                editTextW.setError(getText(R.string.Error_W_empty));
                checkResult = false;
            }
        }
        return checkResult;
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
            fragmentManager.beginTransaction().replace(R.id.ad_frame, adFragment).commit();
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
