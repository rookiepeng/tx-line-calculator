
package com.rookiedev.microwavetools.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
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
import com.rookiedev.microwavetools.libs.Constants;
import com.rookiedev.microwavetools.libs.MlinModel;
import com.rookiedev.microwavetools.libs.MlinCalculator;

import java.math.BigDecimal;

public class MlinFragment extends Fragment {
    private Context mContext;
    private View viewRoot;
    private CardView electricalCard, physicalCard;
    private SpannableString error_er, error_Z0;
    private int DecimalLength; // the length of the Decimal, accurate of the result
    private TextView text_epsilon; // strings which include the subscript
    private EditText editTextW, editTextL, editTextZ0, editTextPhs, editTextFreq, editTextT, editTextH, editTextEr;
    private Button mlinSynthesize,// button synthesize
            mlinAnalyze;// button analyze
    private Spinner spinner_W, spinner_L, spinner_T, spinner_H, spinner_Z0,
            spinner_Eeff, spinner_Freq;// the units of each parameter
    private MlinModel line;
    private AdFragment adFragment=null;
    private boolean isAdFree;

    public MlinFragment() {
        // Empty constructor required for fragment subclasses
    }

    public static MlinFragment newInstance(boolean param) {
        MlinFragment fragment = new MlinFragment();
        Bundle args = new Bundle();
        args.putBoolean(Constants.IS_AD_FREE, param);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            isAdFree=getArguments().getBoolean(Constants.IS_AD_FREE,true);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewRoot = inflater.inflate(R.layout.fragment_mlin, container, false);
        mContext=this.getContext();

        initUI();
        readSharedPref(); // read shared preferences

        mlinAnalyze.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Preference_SharedPref();
                if (!analysisInputCheck()) {
                    editTextZ0.setText(""); // clear the Z0 and Eeff outputs
                    editTextPhs.setText("");
                } else {
                    line.setMetalWidth(Double.parseDouble(editTextW.getText().toString()),
                            spinner_W.getSelectedItemPosition()); // get the header_parameters
                    line.setFrequency(Double.parseDouble(editTextFreq.getText().toString()),
                            spinner_Freq.getSelectedItemPosition());
                    line.setSubEpsilon(Double.parseDouble(editTextEr.getText().toString()));
                    line.setSubHeight(Double.parseDouble(editTextH.getText().toString()),
                            spinner_H.getSelectedItemPosition());
                    line.setMetalThick(Double.parseDouble(editTextT.getText().toString()),
                            spinner_T.getSelectedItemPosition());

                    if (editTextL.length() != 0) { // check the L input
                        line.setMetalLength(Double.parseDouble(editTextL.getText().toString()),
                                spinner_L.getSelectedItemPosition());
                        MlinCalculator mlin = new MlinCalculator();
                        line = mlin.getAnaResult(line);

                        BigDecimal Z0_temp = new BigDecimal(line.getImpedance());
                        double Z0 = Z0_temp.setScale(DecimalLength,
                                BigDecimal.ROUND_HALF_UP).doubleValue();
                        editTextZ0.setText(String.valueOf(Z0)); // cut the decimal

                        BigDecimal Eeff_temp = new BigDecimal(line.getElectricalLength()); // cut the decimal of the Eeff
                        double Eeff = Eeff_temp.setScale(DecimalLength,
                                BigDecimal.ROUND_HALF_UP).doubleValue();
                        editTextPhs.setText(String.valueOf(Eeff));
                    } else {
                        MlinCalculator mlin = new MlinCalculator();
                        line = mlin.getAnaResult(line);

                        BigDecimal Z0_temp = new BigDecimal(line.getImpedance());
                        double Z0 = Z0_temp.setScale(DecimalLength,
                                BigDecimal.ROUND_HALF_UP).doubleValue();
                        editTextZ0.setText(String.valueOf(Z0)); // cut the decimal

                        editTextPhs.setText(""); // if the L input is empty, clear the Eeff
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
                    editTextL.setText(""); // clear the L and W outputs
                    editTextW.setText("");
                } else {
                    line.setImpedance(Double.parseDouble(editTextZ0.getText().toString())); // get the header_parameters
                    line.setFrequency(Double.parseDouble(editTextFreq.getText().toString()),
                            spinner_Freq.getSelectedItemPosition());
                    line.setSubEpsilon(Double.parseDouble(editTextEr.getText().toString()));
                    line.setSubHeight(Double.parseDouble(editTextH.getText().toString()),
                            spinner_H.getSelectedItemPosition());
                    line.setMetalThick(Double.parseDouble(editTextT.getText().toString()),
                            spinner_T.getSelectedItemPosition());

                    double W, L;
                    if (editTextPhs.length() != 0) {
                        line.setElectricalLength(Double.parseDouble(editTextPhs.getText().toString()));
                        MlinCalculator mlin = new MlinCalculator();
                        line = mlin.getSynResult(line, Constants.Synthesize_Width);
                        W = line.getMetalWidth();
                        //fragment_mlin.setW(W);
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
                        editTextL.setText(String.valueOf(L));
                    } else {
                        MlinCalculator mlin = new MlinCalculator();
                        line = mlin.getSynResult(line, Constants.Synthesize_Width);
                        W = line.getMetalWidth();
                        editTextL.setText(""); // clear the L if the Eeff input is empty
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
                    editTextW.setText(String.valueOf(W));
                }
                forceRippleAnimation(physicalCard);
            }
        });

        if(!isAdFree)
        {
            adFragment = new AdFragment();
            FragmentManager fragmentManager = getFragmentManager();
            if(fragmentManager !=null) {
                fragmentManager.beginTransaction().add(R.id.ad_frame, adFragment).commit();
            }
        }
        return viewRoot;
    }

    /*
     * initialize the UI
     */
    private void initUI() {
        RadioButton radioButtonW = (RadioButton) viewRoot.findViewById(R.id.radioBtn_W);
        radioButtonW.setVisibility(View.VISIBLE);
        radioButtonW.setChecked(true);

        RadioButton radioButtonL = (RadioButton) viewRoot.findViewById(R.id.radioBtn_L);
        radioButtonL.setVisibility(View.VISIBLE);
        radioButtonL.setChecked(true);

        RadioButton radioButtonZ0 = (RadioButton) viewRoot.findViewById(R.id.radioBtn_Z0);
        radioButtonZ0.setVisibility(View.VISIBLE);
        radioButtonZ0.setChecked(true);

        RadioButton radioButtonPhs = (RadioButton) viewRoot.findViewById(R.id.radioBtn_Phs);
        radioButtonPhs.setVisibility(View.VISIBLE);
        radioButtonPhs.setChecked(true);

        TextView textW = (TextView) viewRoot.findViewById(R.id.text_W);
        textW.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));

        TextView textL = (TextView) viewRoot.findViewById(R.id.text_L);
        textL.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));

        TextView textZ0 = (TextView) viewRoot.findViewById(R.id.text_Z0);
        textZ0.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor));

        TextView textPhs = (TextView) viewRoot.findViewById(R.id.text_Phs);
        textPhs.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor));

        // Subscript strings
        text_epsilon = (TextView) viewRoot.findViewById(R.id.text_er);
        //text_eeff = (TextView) viewRoot.findViewById(R.id.text_Eeff);

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

        physicalCard = (CardView) viewRoot.findViewById(R.id.physicalParaCard);
        electricalCard = (CardView) viewRoot.findViewById(R.id.electricalParaCard);

        line = new MlinModel();
        error_er = new SpannableString(
                this.getString(R.string.Error_er_empty));
        error_Z0 = new SpannableString(this.getString(R.string.Error_Z0_empty));

        // edittext elements
        editTextW = (EditText) viewRoot.findViewById(R.id.editText_W);
        //editTextW.getBackground().setColorFilter(ContextCompat.getColor(getContext(), R.color.green), PorterDuff.Mode.SRC_ATOP);
        editTextW.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
        editTextL = (EditText) viewRoot.findViewById(R.id.editText_L);
        editTextL.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
        editTextZ0 = (EditText) viewRoot.findViewById(R.id.editText_Z0);
        editTextZ0.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor));
        editTextPhs = (EditText) viewRoot.findViewById(R.id.editText_Phs);
        editTextPhs.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor));
        editTextFreq = (EditText) viewRoot.findViewById(R.id.editText_Freq);
        editTextT = (EditText) viewRoot.findViewById(R.id.editText_T);
        editTextH = (EditText) viewRoot.findViewById(R.id.editText_H);
        editTextEr = (EditText) viewRoot.findViewById(R.id.editText_er);

        // button elements
        mlinAnalyze = (Button) viewRoot.findViewById(R.id.button_ana);
        mlinSynthesize = (Button) viewRoot.findViewById(R.id.button_syn);

        // spinner elements
        spinner_W = (Spinner) viewRoot.findViewById(R.id.spinner_W);
        spinner_L = (Spinner) viewRoot.findViewById(R.id.spinner_L);
        spinner_Z0 = (Spinner) viewRoot.findViewById(R.id.spinner_Z0);
        spinner_Eeff = (Spinner) viewRoot.findViewById(R.id.spinner_Phs);
        spinner_Freq = (Spinner) viewRoot.findViewById(R.id.spinner_Freq);
        spinner_T = (Spinner) viewRoot.findViewById(R.id.spinner_T);
        spinner_H = (Spinner) viewRoot.findViewById(R.id.spinner_H);

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
        SharedPreferences prefs = getActivity().getSharedPreferences(Constants.SHARED_PREFS_NAME,
                AppCompatActivity.MODE_PRIVATE);// get the header_parameters from the Shared
        // Preferences in the device

        // read values from the shared preferences

        // fragment_mlin header_parameters
        editTextW.setText(prefs.getString(Constants.MLIN_W, "19.23"));
        spinner_W.setSelection(Integer.parseInt(prefs.getString(Constants.MLIN_W_UNIT,
                "0")));

        editTextL.setText(prefs.getString(Constants.MLIN_L, "1000.00"));
        spinner_L.setSelection(Integer.parseInt(prefs.getString(Constants.MLIN_L_UNIT,
                "0")));

        editTextZ0.setText(prefs.getString(Constants.MLIN_Z0, "50.0"));
        spinner_Z0.setSelection(Integer.parseInt(prefs.getString(
                Constants.MLIN_Z0_UNIT, "0")));

        editTextPhs.setText(prefs.getString(Constants.MLIN_PHS, "52.58"));
        spinner_Eeff.setSelection(Integer.parseInt(prefs.getString(
                Constants.MLIN_PHS_UNIT, "0")));

        editTextFreq.setText(prefs.getString(Constants.MLIN_FREQ, "1.00"));
        spinner_Freq.setSelection(Integer.parseInt(prefs.getString(
                Constants.MLIN_FREQ_UNIT, "1")));

        editTextEr.setText(prefs.getString(Constants.MLIN_ER, "4.00"));

        editTextH.setText(prefs.getString(Constants.MLIN_H, "10.00"));
        spinner_H.setSelection(Integer.parseInt(prefs.getString(Constants.MLIN_H_UNIT,
                "0")));

        editTextT.setText(prefs.getString(Constants.MLIN_T, "1.40"));
        spinner_T.setSelection(Integer.parseInt(prefs.getString(Constants.MLIN_T_UNIT,
                "0")));
    }

    private void Preference_SharedPref() {
        SharedPreferences prefs = getActivity().getSharedPreferences(Constants.SHARED_PREFS_NAME,
                AppCompatActivity.MODE_PRIVATE);// get the header_parameters from the Shared
        // Preferences in the device
        // universal header_parameters
        DecimalLength = Integer.parseInt(prefs.getString("DecimalLength", "2"));
    }

    @Override
    public void onStop() {
        super.onStop();

        String mlin_W, mlin_T, mlin_H, mlin_epsilon, mlin_L, mlin_Z0, mlin_Eeff, mlin_Freq;
        String mlin_W_unit, mlin_L_unit, mlin_T_unit, mlin_H_unit, mlin_Z0_unit, mlin_Eeff_unit, mlin_Freq_unit;

        SharedPreferences prefs = getActivity().getSharedPreferences(Constants.SHARED_PREFS_NAME,
                AppCompatActivity.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        mlin_W = editTextW.getText().toString();
        mlin_W_unit = Integer.toString(spinner_W.getSelectedItemPosition());
        mlin_L = editTextL.getText().toString();
        mlin_L_unit = Integer.toString(spinner_L.getSelectedItemPosition());
        mlin_Z0 = editTextZ0.getText().toString();
        mlin_Z0_unit = Integer.toString(spinner_Z0.getSelectedItemPosition());
        mlin_Eeff = editTextPhs.getText().toString();
        mlin_Eeff_unit = Integer.toString(spinner_Eeff
                .getSelectedItemPosition());
        mlin_Freq = editTextFreq.getText().toString();
        mlin_Freq_unit = Integer.toString(spinner_Freq
                .getSelectedItemPosition());
        mlin_epsilon = editTextEr.getText().toString();
        mlin_H = editTextH.getText().toString();
        mlin_H_unit = Integer.toString(spinner_H.getSelectedItemPosition());
        mlin_T = editTextT.getText().toString();
        mlin_T_unit = Integer.toString(spinner_T.getSelectedItemPosition());

        editor.putString(Constants.MLIN_W, mlin_W);
        editor.putString(Constants.MLIN_W_UNIT, mlin_W_unit);
        editor.putString(Constants.MLIN_L, mlin_L);
        editor.putString(Constants.MLIN_L_UNIT, mlin_L_unit);
        editor.putString(Constants.MLIN_Z0, mlin_Z0);
        editor.putString(Constants.MLIN_Z0_UNIT, mlin_Z0_unit);
        editor.putString(Constants.MLIN_PHS, mlin_Eeff);
        editor.putString(Constants.MLIN_PHS_UNIT, mlin_Eeff_unit);
        editor.putString(Constants.MLIN_FREQ, mlin_Freq);
        editor.putString(Constants.MLIN_FREQ_UNIT, mlin_Freq_unit);
        editor.putString(Constants.MLIN_ER, mlin_epsilon);
        editor.putString(Constants.MLIN_H, mlin_H);
        editor.putString(Constants.MLIN_H_UNIT, mlin_H_unit);
        editor.putString(Constants.MLIN_T, mlin_T);
        editor.putString(Constants.MLIN_T_UNIT, mlin_T_unit);

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
            error_er.setSpan(new SubscriptSpan(), 13, 14,
                    Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            editTextEr.setError(error_er);
            checkResult = false;
        }
        return checkResult;
    }

    private boolean synthesizeInputCheck() {
        boolean checkResult = true;
        if (editTextZ0.length() == 0) {
            error_Z0.setSpan(new SubscriptSpan(), 13, 14,
                    Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            editTextZ0.setError(error_Z0);
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
            error_er.setSpan(new SubscriptSpan(), 13, 14,
                    Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            editTextEr.setError(error_er);
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

    public void addAdFragment(){
        adFragment = new AdFragment();
        FragmentManager fragmentManager = getFragmentManager();
        if(fragmentManager !=null) {
            fragmentManager.beginTransaction().add(R.id.ad_frame, adFragment).commit();
        }
    }

    public void removeAdFragment(){
        if (adFragment != null) {
            FragmentManager fragmentManager = getFragmentManager();
            if(fragmentManager !=null) {
                fragmentManager.beginTransaction().remove(adFragment).commit();
            }
        }
    }
}
