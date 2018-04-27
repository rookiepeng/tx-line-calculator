/**
 * Created by rookie on 8/11/13.
 */

package com.rookiedev.microwavetools.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.SubscriptSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.rookiedev.microwavetools.R;
import com.rookiedev.microwavetools.libs.Constant;
import com.rookiedev.microwavetools.libs.MlinModel;
import com.rookiedev.microwavetools.libs.MlinCalculator;

import java.math.BigDecimal;

public class MlinFragment extends Fragment {
    private final static String MLIN_W = "MLIN_W";
    private final static String MLIN_W_UNIT = "MLIN_W_UNIT";
    private final static String MLIN_L = "MLIN_L";
    private final static String MLIN_L_UNIT = "MLIN_L_UNIT";
    private final static String MLIN_Z0 = "MLIN_Z0";
    private final static String MLIN_Z0_UNIT = "MLIN_Z0_UNIT";
    private final static String MLIN_Eeff = "MLIN_Eeff";
    private final static String MLIN_Eeff_UNIT = "MLIN_Eeff_UNIT";
    private final static String MLIN_Freq = "MLIN_Freq";
    private final static String MLIN_Freq_UNIT = "MLIN_Freq_UNIT";
    private final static String MLIN_er = "MLIN_er";
    private final static String MLIN_H = "MLIN_H";
    private final static String MLIN_H_UNIT = "MLIN_H_UNIT";
    private final static String MLIN_T = "MLIN_T";
    private final static String MLIN_T_UNIT = "MLIN_T_UNIT";
    private Context mContext;
    private View rootView;
    private CardView electricalCard, physicalCard;
    private SpannableString error_er, error_Z0;
    private int DecimalLength; // the length of the Decimal, accurate of the result
    private TextView text_epsilon; // strings which include the subscript
    private EditText edittext_W, // the width
            edittext_L, // the length
            edittext_Z0, // the impedance
            edittext_Phs, // the electrical length
            edittext_Freq, // the frequency
            edittext_T, // the thickness of the metal
            edittext_H, // the thickness of the dielectric
            edittext_er; // the relative dielectric constant
    //private double W, L, Z0, Eeff, Freq, T, H, er;
    private Button mlinSynthesize,// button synthesize
            mlinAnalyze;// button analyze
    private Spinner spinner_W, spinner_L, spinner_T, spinner_H, spinner_Z0,
            spinner_Eeff, spinner_Freq;// the units of each parameter
    private MlinModel line;

    public MlinFragment() {
        // Empty constructor required for fragment subclasses
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.mlin_fragment, container, false);
        mContext=this.getContext();
        initUI();
        readSharedPref(); // read shared preferences

        mlinAnalyze.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Preference_SharedPref();
                if (!analysisInputCheck()) {
                    edittext_Z0.setText(""); // clear the Z0 and Eeff outputs
                    edittext_Phs.setText("");
                } else {
                    line.setMetalWidth(Double.parseDouble(edittext_W.getText().toString()),
                            spinner_W.getSelectedItemPosition()); // get the parameters
                    line.setFrequency(Double.parseDouble(edittext_Freq.getText().toString()),
                            spinner_Freq.getSelectedItemPosition());
                    line.setSubEpsilon(Double.parseDouble(edittext_er.getText().toString()));
                    line.setSubHeight(Double.parseDouble(edittext_H.getText().toString()),
                            spinner_H.getSelectedItemPosition());
                    line.setMetalThick(Double.parseDouble(edittext_T.getText().toString()),
                            spinner_T.getSelectedItemPosition());

                    if (edittext_L.length() != 0) { // check the L input
                        line.setMetalLength(Double.parseDouble(edittext_L.getText().toString()),
                                spinner_L.getSelectedItemPosition());
                        MlinCalculator mlin = new MlinCalculator();
                        line = mlin.getAnaResult(line);

                        BigDecimal Z0_temp = new BigDecimal(line.getImpedance());
                        double Z0 = Z0_temp.setScale(DecimalLength,
                                BigDecimal.ROUND_HALF_UP).doubleValue();
                        edittext_Z0.setText(String.valueOf(Z0)); // cut the decimal

                        BigDecimal Eeff_temp = new BigDecimal(line.getElectricalLength()); // cut the decimal of the Eeff
                        double Eeff = Eeff_temp.setScale(DecimalLength,
                                BigDecimal.ROUND_HALF_UP).doubleValue();
                        edittext_Phs.setText(String.valueOf(Eeff));
                    } else {
                        MlinCalculator mlin = new MlinCalculator();
                        line = mlin.getAnaResult(line);

                        BigDecimal Z0_temp = new BigDecimal(line.getImpedance());
                        double Z0 = Z0_temp.setScale(DecimalLength,
                                BigDecimal.ROUND_HALF_UP).doubleValue();
                        edittext_Z0.setText(String.valueOf(Z0)); // cut the decimal

                        edittext_Phs.setText(""); // if the L input is empty, clear the Eeff
                    }
                }
                forceRippleAnimation(electricalCard);
            }
        });

        mlinSynthesize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int temp;
                Preference_SharedPref();
                if (!synthesizeInputCheck()) {
                    edittext_L.setText(""); // clear the L and W outputs
                    edittext_W.setText("");
                } else {
                    line.setImpedance(Double.parseDouble(edittext_Z0.getText().toString())); // get the parameters
                    line.setFrequency(Double.parseDouble(edittext_Freq.getText().toString()),
                            spinner_Freq.getSelectedItemPosition());
                    line.setSubEpsilon(Double.parseDouble(edittext_er.getText().toString()));
                    line.setSubHeight(Double.parseDouble(edittext_H.getText().toString()),
                            spinner_H.getSelectedItemPosition());
                    line.setMetalThick(Double.parseDouble(edittext_T.getText().toString()),
                            spinner_T.getSelectedItemPosition());

                    double W, L;
                    if (edittext_Phs.length() != 0) {
                        line.setElectricalLength(Double.parseDouble(edittext_Phs.getText().toString()));
                        MlinCalculator mlin = new MlinCalculator();
                        line = mlin.getSynResult(line, Constant.Synthesize_Width);
                        W = line.getMetalWidth();
                        //mlin_fragment.setW(W);
                        L = line.getMetalLength();
                        temp = spinner_L.getSelectedItemPosition();
                        if (temp == 0) {
                            L = L * 1000 * 39.37007874;
                        } else if (temp == 1) {
                            L = L * 1000;
                        } else if (temp == 2) {
                            L = L * 100;
                        }
                        BigDecimal L_temp = new BigDecimal(L); // cut the decimal of L
                        L = L_temp.setScale(DecimalLength,
                                BigDecimal.ROUND_HALF_UP).doubleValue();
                        edittext_L.setText(String.valueOf(L));
                    } else {
                        MlinCalculator mlin = new MlinCalculator();
                        line = mlin.getSynResult(line, Constant.Synthesize_Width);
                        W = line.getMetalWidth();
                        edittext_L.setText(""); // clear the L if the Eeff input is empty
                    }
                    temp = spinner_W.getSelectedItemPosition();
                    if (temp == 0) {
                        W = W * 39370.0787402;
                    } else if (temp == 1) {
                        W = W * 1000;
                    } else if (temp == 2) {
                        W = W * 100;
                    }

                    BigDecimal W_temp = new BigDecimal(W); // cut the decimal of W
                    W = W_temp
                            .setScale(DecimalLength, BigDecimal.ROUND_HALF_UP)
                            .doubleValue();
                    edittext_W.setText(String.valueOf(W));
                }
                forceRippleAnimation(physicalCard);
            }
        });

        return rootView;
    }

    /*
     * initialize the UI
	 */
    private void initUI() {
        RadioButton radioButtonW = (RadioButton) rootView.findViewById(R.id.radioBtn_W);
        radioButtonW.setVisibility(View.VISIBLE);
        radioButtonW.setChecked(true);

        RadioButton radioButtonL = (RadioButton) rootView.findViewById(R.id.radioBtn_L);
        radioButtonL.setVisibility(View.VISIBLE);
        radioButtonL.setChecked(true);

        RadioButton radioButtonZ0 = (RadioButton) rootView.findViewById(R.id.radioBtn_Z0);
        radioButtonZ0.setVisibility(View.VISIBLE);
        radioButtonZ0.setChecked(true);

        RadioButton radioButtonPhs = (RadioButton) rootView.findViewById(R.id.radioBtn_Phs);
        radioButtonPhs.setVisibility(View.VISIBLE);
        radioButtonPhs.setChecked(true);

        TextView textW = (TextView) rootView.findViewById(R.id.text_W);
        textW.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));

        TextView textL = (TextView) rootView.findViewById(R.id.text_L);
        textL.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));

        TextView textZ0 = (TextView) rootView.findViewById(R.id.text_Z0);
        textZ0.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor));

        TextView textPhs = (TextView) rootView.findViewById(R.id.text_Phs);
        textPhs.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor));

        // Subscript strings
        text_epsilon = (TextView) rootView.findViewById(R.id.text_er);
        //text_eeff = (TextView) rootView.findViewById(R.id.text_Eeff);

        SpannableString spanEpsilon = new SpannableString(
                this.getString(R.string.text_er));
        spanEpsilon.setSpan(new SubscriptSpan(), 1, 2,
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        text_epsilon.append(spanEpsilon);

        SpannableString spanZ0 = new SpannableString(
                this.getString(R.string.text_Z0));
        spanZ0.setSpan(new SubscriptSpan(), 1, 2,
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        textZ0.append(spanZ0);

        physicalCard = (CardView) rootView.findViewById(R.id.physicalParaCard);
        electricalCard = (CardView) rootView.findViewById(R.id.electricalParaCard);

        line = new MlinModel();
        error_er = new SpannableString(
                this.getString(R.string.Error_er_empty));
        error_Z0 = new SpannableString(this.getString(R.string.Error_Z0_empty));

        // edittext elements
        edittext_W = (EditText) rootView.findViewById(R.id.editText_W);
        //edittext_W.getBackground().setColorFilter(ContextCompat.getColor(getContext(), R.color.green), PorterDuff.Mode.SRC_ATOP);
        edittext_W.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
        edittext_L = (EditText) rootView.findViewById(R.id.editText_L);
        edittext_L.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
        edittext_Z0 = (EditText) rootView.findViewById(R.id.editText_Z0);
        edittext_Z0.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor));
        edittext_Phs = (EditText) rootView.findViewById(R.id.editText_Phs);
        edittext_Phs.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor));
        edittext_Freq = (EditText) rootView.findViewById(R.id.editText_Freq);
        edittext_T = (EditText) rootView.findViewById(R.id.editText_T);
        edittext_H = (EditText) rootView.findViewById(R.id.editText_H);
        edittext_er = (EditText) rootView.findViewById(R.id.editText_er);

        // button elements
        mlinAnalyze = (Button) rootView.findViewById(R.id.button_ana);
        mlinSynthesize = (Button) rootView.findViewById(R.id.button_syn);

        // spinner elements
        spinner_W = (Spinner) rootView.findViewById(R.id.spinner_W);
        spinner_L = (Spinner) rootView.findViewById(R.id.spinner_L);
        spinner_Z0 = (Spinner) rootView.findViewById(R.id.spinner_Z0);
        spinner_Eeff = (Spinner) rootView.findViewById(R.id.spinner_Phs);
        spinner_Freq = (Spinner) rootView.findViewById(R.id.spinner_Freq);
        spinner_T = (Spinner) rootView.findViewById(R.id.spinner_T);
        spinner_H = (Spinner) rootView.findViewById(R.id.spinner_H);

        // configure the length units
        ArrayAdapter<CharSequence> adapterLength = ArrayAdapter
                .createFromResource(this.getActivity(), R.array.list_units_Length,
                        android.R.layout.simple_spinner_item);
        adapterLength
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_W.setAdapter(adapterLength);
        spinner_L.setAdapter(adapterLength);
        spinner_T.setAdapter(adapterLength);
        spinner_H.setAdapter(adapterLength);

        // configure the impedance units
        ArrayAdapter<CharSequence> adapterImpedance = ArrayAdapter
                .createFromResource(this.getActivity(), R.array.list_units_Impedance,
                        android.R.layout.simple_spinner_item);
        adapterImpedance
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_Z0.setAdapter(adapterImpedance);

        // configure the electrical length units
        ArrayAdapter<CharSequence> adapterEleLength = ArrayAdapter
                .createFromResource(this.getActivity(), R.array.list_units_Ele_length,
                        android.R.layout.simple_spinner_item);
        adapterEleLength
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_Eeff.setAdapter(adapterEleLength);

        // configure the frequency units
        ArrayAdapter<CharSequence> adapterFreq = ArrayAdapter
                .createFromResource(this.getActivity(), R.array.list_units_Frequency,
                        android.R.layout.simple_spinner_item);
        adapterFreq
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_Freq.setAdapter(adapterFreq);
    }

    private void readSharedPref() {
        SharedPreferences prefs = getActivity().getSharedPreferences(Constant.SHARED_PREFS_NAME,
                AppCompatActivity.MODE_PRIVATE);// get the parameters from the Shared
        // Preferences in the device

        // read values from the shared preferences

        // mlin_fragment parameters
        edittext_W.setText(prefs.getString(MLIN_W, "19.23"));
        spinner_W.setSelection(Integer.parseInt(prefs.getString(MLIN_W_UNIT,
                "0")));

        edittext_L.setText(prefs.getString(MLIN_L, "1000.00"));
        spinner_L.setSelection(Integer.parseInt(prefs.getString(MLIN_L_UNIT,
                "0")));

        edittext_Z0.setText(prefs.getString(MLIN_Z0, "50.0"));
        spinner_Z0.setSelection(Integer.parseInt(prefs.getString(
                MLIN_Z0_UNIT, "0")));

        edittext_Phs.setText(prefs.getString(MLIN_Eeff, "52.58"));
        spinner_Eeff.setSelection(Integer.parseInt(prefs.getString(
                MLIN_Eeff_UNIT, "0")));

        edittext_Freq.setText(prefs.getString(MLIN_Freq, "1.00"));
        spinner_Freq.setSelection(Integer.parseInt(prefs.getString(
                MLIN_Freq_UNIT, "1")));

        edittext_er.setText(prefs.getString(MLIN_er, "4.00"));

        edittext_H.setText(prefs.getString(MLIN_H, "10.00"));
        spinner_H.setSelection(Integer.parseInt(prefs.getString(MLIN_H_UNIT,
                "0")));

        edittext_T.setText(prefs.getString(MLIN_T, "1.40"));
        spinner_T.setSelection(Integer.parseInt(prefs.getString(MLIN_T_UNIT,
                "0")));
    }

    private void Preference_SharedPref() {
        SharedPreferences prefs = getActivity().getSharedPreferences(Constant.SHARED_PREFS_NAME,
                AppCompatActivity.MODE_PRIVATE);// get the parameters from the Shared
        // Preferences in the device
        // universal parameters
        DecimalLength = Integer.parseInt(prefs.getString("DecimalLength", "2"));
    }

    @Override
    public void onStop() {
        super.onStop();

        String mlin_W, mlin_T, mlin_H, mlin_epsilon, mlin_L, mlin_Z0, mlin_Eeff, mlin_Freq;
        String mlin_W_unit, mlin_L_unit, mlin_T_unit, mlin_H_unit, mlin_Z0_unit, mlin_Eeff_unit, mlin_Freq_unit;

        SharedPreferences prefs = getActivity().getSharedPreferences(Constant.SHARED_PREFS_NAME,
                AppCompatActivity.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        mlin_W = edittext_W.getText().toString();
        mlin_W_unit = Integer.toString(spinner_W.getSelectedItemPosition());
        mlin_L = edittext_L.getText().toString();
        mlin_L_unit = Integer.toString(spinner_L.getSelectedItemPosition());
        mlin_Z0 = edittext_Z0.getText().toString();
        mlin_Z0_unit = Integer.toString(spinner_Z0.getSelectedItemPosition());
        mlin_Eeff = edittext_Phs.getText().toString();
        mlin_Eeff_unit = Integer.toString(spinner_Eeff
                .getSelectedItemPosition());
        mlin_Freq = edittext_Freq.getText().toString();
        mlin_Freq_unit = Integer.toString(spinner_Freq
                .getSelectedItemPosition());
        mlin_epsilon = edittext_er.getText().toString();
        mlin_H = edittext_H.getText().toString();
        mlin_H_unit = Integer.toString(spinner_H.getSelectedItemPosition());
        mlin_T = edittext_T.getText().toString();
        mlin_T_unit = Integer.toString(spinner_T.getSelectedItemPosition());

        editor.putString(MLIN_W, mlin_W);
        editor.putString(MLIN_W_UNIT, mlin_W_unit);
        editor.putString(MLIN_L, mlin_L);
        editor.putString(MLIN_L_UNIT, mlin_L_unit);
        editor.putString(MLIN_Z0, mlin_Z0);
        editor.putString(MLIN_Z0_UNIT, mlin_Z0_unit);
        editor.putString(MLIN_Eeff, mlin_Eeff);
        editor.putString(MLIN_Eeff_UNIT, mlin_Eeff_unit);
        editor.putString(MLIN_Freq, mlin_Freq);
        editor.putString(MLIN_Freq_UNIT, mlin_Freq_unit);
        editor.putString(MLIN_er, mlin_epsilon);
        editor.putString(MLIN_H, mlin_H);
        editor.putString(MLIN_H_UNIT, mlin_H_unit);
        editor.putString(MLIN_T, mlin_T);
        editor.putString(MLIN_T_UNIT, mlin_T_unit);

        editor.apply();
    }

    private boolean analysisInputCheck() {
        boolean checkResult = true;
        if (edittext_W.length() == 0) {
            edittext_W.setError(getText(R.string.Error_W_empty));
            checkResult = false;
        }
        if (edittext_Freq.length() == 0) {
            edittext_Freq.setError(getText(R.string.Error_Freq_empty));
            checkResult = false;
        }
        if (edittext_H.length() == 0) {
            edittext_H.setError(getText(R.string.Error_H_empty));
            checkResult = false;
        }
        if (edittext_T.length() == 0) {
            edittext_T.setError(getText(R.string.Error_T_empty));
            checkResult = false;
        }
        if (edittext_er.length() == 0) {
            error_er.setSpan(new SubscriptSpan(), 13, 14,
                    Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            edittext_er.setError(error_er);
            checkResult = false;
        }
        return checkResult;
    }

    private boolean synthesizeInputCheck() {
        boolean checkResult = true;
        if (edittext_Z0.length() == 0) {
            error_Z0.setSpan(new SubscriptSpan(), 13, 14,
                    Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            edittext_Z0.setError(error_Z0);
            checkResult = false;
        }
        if (edittext_Freq.length() == 0) {
            edittext_Freq.setError(getText(R.string.Error_Freq_empty));
            checkResult = false;
        }
        if (edittext_H.length() == 0) {
            edittext_H.setError(getText(R.string.Error_H_empty));
            checkResult = false;
        }
        if (edittext_T.length() == 0) {
            edittext_T.setError(getText(R.string.Error_T_empty));
            checkResult = false;
        }
        if (edittext_er.length() == 0) {
            error_er.setSpan(new SubscriptSpan(), 13, 14,
                    Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            edittext_er.setError(error_er);
            checkResult = false;
        }
        return checkResult;
    }

    protected void forceRippleAnimation(View view) {
        if (Build.VERSION.SDK_INT >= 23) {
            view.setClickable(true);
            Drawable background = view.getForeground();
            final RippleDrawable rippleDrawable = (RippleDrawable) background;

            rippleDrawable.setState(new int[]{android.R.attr.state_pressed, android.R.attr.state_enabled});

            view.setClickable(false);
            rippleDrawable.setState(new int[]{});
        }
    }
}
