package com.rookiedev.microwavetools.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import com.rookiedev.microwavetools.libs.CoaxCalculator;
import com.rookiedev.microwavetools.libs.Constants;
import com.rookiedev.microwavetools.libs.CoaxModel;

import java.math.BigDecimal;

public class CoaxFragment extends Fragment {
    private Context mContext;
    private View viewRoot;
    private CardView cardViewParameters, cardViewDimensions;
    private int DecimalLength; // the length of the Decimal,
    private SpannableString error_er, error_Z0;
    private TextView text_a, text_b, text_c, text_er, text_Z0; // strings which include the subscript
    private EditText edittextA, edittextB, edittextC, edittextL, edittextZ0, edittextPhs, edittextFreq, edittextEr;
    private Button buttonSynthesize, buttonAnalyze;
    private Spinner spinnerA, spinnerB, spinnerC, spinnerL, spinnerZ0, spinnerPhs, spinnerFreq;
    private int target;
    private RadioButton radioBtnA, radioBtnB, radioBtnC;
    private CoaxModel line;
    private ColorStateList defaultTextColor, defaultEdittextColor;
    private AdFragment adFragment=null;

    private boolean isAdFree;

    public CoaxFragment() {
        // Empty constructor required for fragment subclasses
    }

    public static CoaxFragment newInstance(boolean param) {
        CoaxFragment fragment = new CoaxFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        viewRoot = inflater.inflate(R.layout.fragment_coax, container, false);
        mContext=this.getContext();
        initUI(); // initial the UI
        readSharedPref(); // read shared preferences
        setRadioBtn();

        buttonAnalyze.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int temp;
                Preference_SharedPref();
                if (!analysisInputCheck()) {
                    edittextZ0.setText(""); // clear the Z0 and Eeff outputs
                    edittextPhs.setText("");
                } else {
                    line.setCoreRadius(Double.parseDouble(edittextA.getText().toString()),
                            spinnerA.getSelectedItemPosition());
                    line.setSubRadius(Double.parseDouble(edittextB.getText().toString()),
                            spinnerB.getSelectedItemPosition());
                    line.setCoreOffset(Double.parseDouble(edittextC.getText().toString()),
                            spinnerC.getSelectedItemPosition());
                    line.setFrequency(Double.parseDouble(edittextFreq.getText().toString()),
                            spinnerFreq.getSelectedItemPosition());
                    line.setSubEpsilon(Double.parseDouble(edittextEr.getText().toString()));

                    if (!edittextL.getText().toString().equals("")) { // check the L input
                        line.setMetalLength(Double.parseDouble(edittextL.getText().toString()),
                                spinnerL.getSelectedItemPosition());

                        CoaxCalculator coax = new CoaxCalculator();
                        line = coax.getAnaResult(line);

                        BigDecimal Eeff_temp = new BigDecimal(line.getElectricalLength()); // cut the decimal of the Eeff
                        double Eeff = Eeff_temp.setScale(DecimalLength, BigDecimal.ROUND_HALF_UP).doubleValue();
                        edittextPhs.setText(String.valueOf(Eeff));
                    } else {
                        CoaxCalculator coax = new CoaxCalculator();
                        line = coax.getAnaResult(line);
                        //Z0 = fragment_coax.getZ0(); // calculate the Z0
                        edittextPhs.setText(""); // if the L input is empty, clear the Eeff
                    }
                    BigDecimal Z0_temp = new BigDecimal(line.getImpedance());
                    double Z0 = Z0_temp.setScale(DecimalLength, BigDecimal.ROUND_HALF_UP).doubleValue();
                    edittextZ0.setText(String.valueOf(Z0)); // cut the decimal of the Z0
                }
                forceRippleAnimation(cardViewParameters);
            }
        });

        buttonSynthesize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Preference_SharedPref();
                if (!synthesizeInputCheck()) {
                    if (target == 0) {
                        edittextA.setText("");
                    } else if (target == 1) {
                        edittextC.setText("");
                    } else if (target == 2) {
                        edittextB.setText("");
                    } else if (target == 3) {
                        edittextEr.setText("");
                    }
                } else {
                    line.setImpedance(Double.parseDouble(edittextZ0.getText().toString()));
                    line.setFrequency(Double.parseDouble(edittextFreq.getText().toString()),
                            spinnerFreq.getSelectedItemPosition());

                    if (target == Constants.Synthesize_CoreRadius) { //a = 0;
                        line.setSubRadius(Double.parseDouble(edittextB.getText().toString()),
                                spinnerB.getSelectedItemPosition());
                        line.setCoreOffset(Double.parseDouble(edittextC.getText().toString()),
                                spinnerC.getSelectedItemPosition());
                        line.setSubEpsilon(Double.parseDouble(edittextEr.getText().toString()));
                    } else if (target == Constants.Synthesize_SubRadius) { //b = 0;
                        line.setCoreRadius(Double.parseDouble(edittextA.getText().toString()),
                                spinnerA.getSelectedItemPosition());
                        line.setCoreOffset(Double.parseDouble(edittextC.getText().toString()),
                                spinnerC.getSelectedItemPosition());
                        line.setSubEpsilon(Double.parseDouble(edittextEr.getText().toString()));
                    } else if (target == Constants.Synthesize_CoreOffset) { //c = 0;
                        line.setCoreRadius(Double.parseDouble(edittextA.getText().toString()),
                                spinnerA.getSelectedItemPosition());
                        line.setSubRadius(Double.parseDouble(edittextB.getText().toString()),
                                spinnerB.getSelectedItemPosition());
                        line.setSubEpsilon(Double.parseDouble(edittextEr.getText().toString()));
                    } else if (target == Constants.Synthesize_Er) { //er = 0;
                        line.setCoreRadius(Double.parseDouble(edittextA.getText().toString()),
                                spinnerA.getSelectedItemPosition());
                        line.setSubRadius(Double.parseDouble(edittextB.getText().toString()),
                                spinnerB.getSelectedItemPosition());
                        line.setCoreOffset(Double.parseDouble(edittextC.getText().toString()),
                                spinnerC.getSelectedItemPosition());
                    }

                    if (edittextPhs.length() != 0) { // check if the Eeff is empty
                        line.setElectricalLength(Double.parseDouble(edittextPhs.getText().toString()));
                        CoaxCalculator coax = new CoaxCalculator();
                        line = coax.getSynResult(line, target);

                        BigDecimal L_temp = new BigDecimal(
                                Constants.meter2others(line.getMetalLength(), spinnerL.getSelectedItemPosition())); // cut the decimal of L
                        double L = L_temp.setScale(DecimalLength, BigDecimal.ROUND_HALF_UP).doubleValue();
                        edittextL.setText(String.valueOf(L));
                    } else {
                        CoaxCalculator coax = new CoaxCalculator();
                        line = coax.getSynResult(line, target);
                        edittextL.setText(""); // clear the L if the Eeff input is empty
                    }
                    if (target == Constants.Synthesize_CoreRadius) {

                        BigDecimal a_temp = new BigDecimal(
                                Constants.meter2others(line.getCoreRadius(), spinnerA.getSelectedItemPosition())); // cut the decimal of W
                        double a = a_temp.setScale(DecimalLength, BigDecimal.ROUND_HALF_UP).doubleValue();
                        edittextA.setText(String.valueOf(a));
                    } else if (target == Constants.Synthesize_SubRadius) {

                        BigDecimal b_temp = new BigDecimal(
                                Constants.meter2others(line.getSubRadius(), spinnerB.getSelectedItemPosition())); // cut the decimal of S
                        double height = b_temp.setScale(DecimalLength, BigDecimal.ROUND_HALF_UP).doubleValue();
                        edittextB.setText(String.valueOf(height));
                    } else if (target == Constants.Synthesize_CoreOffset) {

                        BigDecimal c_temp = new BigDecimal(
                                Constants.meter2others(line.getCoreOffset(), spinnerC.getSelectedItemPosition()));
                        double b = c_temp.setScale(DecimalLength, BigDecimal.ROUND_HALF_UP).doubleValue();
                        edittextC.setText(String.valueOf(b));
                    } else if (target == Constants.Synthesize_Er) {
                        BigDecimal er_temp = new BigDecimal(line.getSubEpsilon());
                        double er = er_temp.setScale(DecimalLength, BigDecimal.ROUND_HALF_UP).doubleValue();
                        edittextEr.setText(String.valueOf(er));
                    }
                }
                forceRippleAnimation(cardViewDimensions);
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

    private void initUI() {
        line = new CoaxModel();
        RadioButton radioButtonL = (RadioButton) viewRoot.findViewById(R.id.radioBtn_L);
        radioButtonL.setVisibility(View.VISIBLE);
        radioButtonL.setChecked(true);

        RadioButton radioButtonPhs = (RadioButton) viewRoot.findViewById(R.id.radioBtn_Phs);
        radioButtonPhs.setVisibility(View.VISIBLE);
        radioButtonPhs.setChecked(true);

        RadioButton radioButtonZ0 = (RadioButton) viewRoot.findViewById(R.id.radioBtn_Z0);
        radioButtonZ0.setVisibility(View.VISIBLE);
        radioButtonZ0.setChecked(true);

        cardViewDimensions = (CardView) viewRoot.findViewById(R.id.card_dimensions);
        cardViewParameters = (CardView) viewRoot.findViewById(R.id.card_parameters);

        error_er = new SpannableString(this.getString(R.string.Error_er_empty));
        error_Z0 = new SpannableString(this.getString(R.string.Error_Z0_empty));

        // Subscript strings
        text_er = (TextView) viewRoot.findViewById(R.id.text_er);
        defaultTextColor = text_er.getTextColors();

        text_a = (TextView) viewRoot.findViewById(R.id.text_a);
        text_b = (TextView) viewRoot.findViewById(R.id.text_b);
        text_c = (TextView) viewRoot.findViewById(R.id.text_c);

        text_Z0 = (TextView) viewRoot.findViewById(R.id.text_Z0);
        text_Z0.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor));
        TextView text_Eeff = (TextView) viewRoot.findViewById(R.id.text_Phs);
        text_Eeff.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor));
        TextView text_L = (TextView) viewRoot.findViewById(R.id.text_L);
        text_L.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));

        SpannableString spanEr = new SpannableString(this.getString(R.string.text_er));
        spanEr.setSpan(new SubscriptSpan(), 1, 2, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        text_er.append(spanEr);

        SpannableString spanZ0 = new SpannableString(this.getString(R.string.text_Z0));
        spanZ0.setSpan(new SubscriptSpan(), 1, 2, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        text_Z0.append(spanZ0);

        // edittext elements
        edittextA = (EditText) viewRoot.findViewById(R.id.editText_a);
        defaultEdittextColor = edittextA.getTextColors();
        edittextC = (EditText) viewRoot.findViewById(R.id.editText_c);
        edittextB = (EditText) viewRoot.findViewById(R.id.editText_b);
        edittextL = (EditText) viewRoot.findViewById(R.id.editText_L);
        edittextL.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
        edittextZ0 = (EditText) viewRoot.findViewById(R.id.editText_Z0);
        edittextZ0.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor));
        edittextPhs = (EditText) viewRoot.findViewById(R.id.editText_Phs);
        edittextPhs.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor));
        edittextFreq = (EditText) viewRoot.findViewById(R.id.editText_Freq);
        //edittext_T = (EditText) viewRoot.findViewById(R.id.editText_T);
        edittextEr = (EditText) viewRoot.findViewById(R.id.editText_er);

        // button elements
        buttonAnalyze = (Button) viewRoot.findViewById(R.id.button_ana);
        buttonSynthesize = (Button) viewRoot.findViewById(R.id.button_syn);

        // spinner elements
        spinnerA = (Spinner) viewRoot.findViewById(R.id.spinner_a);
        spinnerC = (Spinner) viewRoot.findViewById(R.id.spinner_c);
        spinnerB = (Spinner) viewRoot.findViewById(R.id.spinner_b);
        spinnerL = (Spinner) viewRoot.findViewById(R.id.spinner_L);
        spinnerZ0 = (Spinner) viewRoot.findViewById(R.id.spinner_Z0);
        spinnerPhs = (Spinner) viewRoot.findViewById(R.id.spinner_Phs);
        spinnerFreq = (Spinner) viewRoot.findViewById(R.id.spinner_Freq);
        //spinner_T = (Spinner) viewRoot.findViewById(R.id.spinner_T);

        // configure the length units
        ArrayAdapter<CharSequence> adapterLength = ArrayAdapter.createFromResource(this.getActivity(),
                R.array.list_units_Length, android.R.layout.simple_spinner_item);
        adapterLength.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerA.setAdapter(adapterLength);
        spinnerC.setAdapter(adapterLength);
        spinnerB.setAdapter(adapterLength);
        spinnerL.setAdapter(adapterLength);

        // configure the impedance units
        ArrayAdapter<CharSequence> adapterImpedance = ArrayAdapter.createFromResource(this.getActivity(),
                R.array.list_units_Impedance, android.R.layout.simple_spinner_item);
        adapterImpedance.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerZ0.setAdapter(adapterImpedance);

        // configure the electrical length units
        ArrayAdapter<CharSequence> adapterEleLength = ArrayAdapter.createFromResource(this.getActivity(),
                R.array.list_units_Ele_length, android.R.layout.simple_spinner_item);
        adapterEleLength.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPhs.setAdapter(adapterEleLength);

        // configure the frequency units
        ArrayAdapter<CharSequence> adapterFreq = ArrayAdapter.createFromResource(this.getActivity(),
                R.array.list_units_Frequency, android.R.layout.simple_spinner_item);
        adapterFreq.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFreq.setAdapter(adapterFreq);
        radioBtnA = (RadioButton) viewRoot.findViewById(R.id.radioBtn_a);
        radioBtnA.setVisibility(View.VISIBLE);
        radioBtnC = (RadioButton) viewRoot.findViewById(R.id.radioBtn_c);
        radioBtnC.setVisibility(View.VISIBLE);
        radioBtnB = (RadioButton) viewRoot.findViewById(R.id.radioBtn_b);
        radioBtnB.setVisibility(View.VISIBLE);
    }

    private void readSharedPref() {
        SharedPreferences prefs = getActivity().getSharedPreferences(Constants.SHARED_PREFS_NAME,
                AppCompatActivity.MODE_PRIVATE);// get the header_parameters from the Shared

        // fragment_coax header_parameters
        edittextA.setText(prefs.getString(Constants.COAX_A, "0.30"));
        spinnerA.setSelection(Integer.parseInt(prefs.getString(Constants.COAX_A_UNIT, "2")));

        edittextB.setText(prefs.getString(Constants.COAX_B, "1.00"));
        spinnerB.setSelection(Integer.parseInt(prefs.getString(Constants.COAX_B_UNIT, "2")));

        edittextC.setText(prefs.getString(Constants.COAX_C, "0.00"));
        spinnerC.setSelection(Integer.parseInt(prefs.getString(Constants.COAX_C_UNIT, "2")));

        edittextL.setText(prefs.getString(Constants.COAX_L, "1000"));
        spinnerL.setSelection(Integer.parseInt(prefs.getString(Constants.COAX_L_UNIT, "0")));

        edittextZ0.setText(prefs.getString(Constants.COAX_Z0, "72.19"));
        spinnerZ0.setSelection(Integer.parseInt(prefs.getString(Constants.COAX_Z0_UNIT, "0")));

        edittextPhs.setText(prefs.getString(Constants.COAX_PHS, "30.50"));
        spinnerPhs.setSelection(Integer.parseInt(prefs.getString(Constants.COAX_PHS_UNIT, "0")));

        edittextFreq.setText(prefs.getString(Constants.COAX_FREQ, "1.00"));
        spinnerFreq.setSelection(Integer.parseInt(prefs.getString(Constants.COAX_FREQ_UNIT, "1")));

        edittextEr.setText(prefs.getString(Constants.COAX_ER, "1.00"));

        target = Integer.parseInt(prefs.getString(Constants.COAX_TARGET, Integer.toString(Constants.Synthesize_CoreRadius)));
    }

    private void Preference_SharedPref() {
        SharedPreferences prefs = getActivity().getSharedPreferences(Constants.SHARED_PREFS_NAME,
                AppCompatActivity.MODE_PRIVATE);// get the header_parameters from the Shared
        // Preferences in the device
        // universal header_parameters
        DecimalLength = Integer.parseInt(prefs.getString("DecimalLength", "2"));
    }

    private void setRadioBtn() {
        if (target == Constants.Synthesize_CoreRadius) {
            radioBtnA.setChecked(true);
            text_a.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
            edittextA.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
            radioBtnB.setChecked(false);
            text_b.setTextColor(defaultTextColor);
            edittextB.setTextColor(defaultEdittextColor);
            radioBtnC.setChecked(false);
            text_c.setTextColor(defaultTextColor);
            edittextC.setTextColor(defaultEdittextColor);
            //a_input.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.green_shadow));
            //c_input.setBackgroundColor(Color.WHITE);
            //b_input.setBackgroundColor(Color.WHITE);
            //er_input.setBackgroundColor(Color.WHITE);
        } else if (target == Constants.Synthesize_SubRadius) {
            radioBtnA.setChecked(false);
            text_a.setTextColor(defaultTextColor);
            edittextA.setTextColor(defaultEdittextColor);
            radioBtnB.setChecked(true);
            text_b.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
            edittextB.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
            radioBtnC.setChecked(false);
            text_c.setTextColor(defaultTextColor);
            edittextC.setTextColor(defaultEdittextColor);
            //a_input.setBackgroundColor(Color.WHITE);
            //c_input.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.green_shadow));
            //b_input.setBackgroundColor(Color.WHITE);
            //er_input.setBackgroundColor(Color.WHITE);
        } else if (target == Constants.Synthesize_CoreOffset) {
            radioBtnA.setChecked(false);
            text_a.setTextColor(defaultTextColor);
            edittextA.setTextColor(defaultEdittextColor);
            radioBtnB.setChecked(false);
            text_b.setTextColor(defaultTextColor);
            edittextB.setTextColor(defaultEdittextColor);
            radioBtnC.setChecked(true);
            text_c.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
            edittextC.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
            //a_input.setBackgroundColor(Color.WHITE);
            //c_input.setBackgroundColor(Color.WHITE);
            //b_input.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.green_shadow));
            //er_input.setBackgroundColor(Color.WHITE);
        }
        radioBtnA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                radioBtnA.setChecked(true);
                text_a.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
                edittextA.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
                radioBtnB.setChecked(false);
                text_b.setTextColor(defaultTextColor);
                edittextB.setTextColor(defaultEdittextColor);
                radioBtnC.setChecked(false);
                text_c.setTextColor(defaultTextColor);
                edittextC.setTextColor(defaultEdittextColor);
                //a_input.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.green_shadow));
                //c_input.setBackgroundColor(Color.WHITE);
                //b_input.setBackgroundColor(Color.WHITE);
                //er_input.setBackgroundColor(Color.WHITE);
                target = Constants.Synthesize_CoreRadius;
            }
        });
        radioBtnB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                radioBtnA.setChecked(false);
                text_a.setTextColor(defaultTextColor);
                edittextA.setTextColor(defaultEdittextColor);
                radioBtnB.setChecked(true);
                text_b.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
                edittextB.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
                radioBtnC.setChecked(false);
                text_c.setTextColor(defaultTextColor);
                edittextC.setTextColor(defaultEdittextColor);
                //a_input.setBackgroundColor(Color.WHITE);
                //c_input.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.green_shadow));
                //b_input.setBackgroundColor(Color.WHITE);
                //er_input.setBackgroundColor(Color.WHITE);
                target = Constants.Synthesize_Height;
            }
        });
        radioBtnC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                radioBtnA.setChecked(false);
                text_a.setTextColor(defaultTextColor);
                edittextA.setTextColor(defaultEdittextColor);
                radioBtnB.setChecked(false);
                text_b.setTextColor(defaultTextColor);
                edittextB.setTextColor(defaultEdittextColor);
                radioBtnC.setChecked(true);
                text_c.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
                edittextC.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
                target = Constants.Synthesize_CoreOffset;
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        String coax_a, coax_H, coax_b, coax_er, coax_L, coax_Z0, coax_Eeff, coax_Freq, coax_T;
        String coax_a_unit, coax_H_unit, coax_b_unit, coax_L_unit, coax_Z0_unit, coax_Eeff_unit, coax_Freq_unit,
                coax_T_unit;
        String coax_flag;

        SharedPreferences prefs = getActivity().getSharedPreferences(Constants.SHARED_PREFS_NAME,
                AppCompatActivity.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        coax_a = edittextA.getText().toString();
        coax_a_unit = Integer.toString(spinnerA.getSelectedItemPosition());
        coax_H = edittextC.getText().toString();
        coax_H_unit = Integer.toString(spinnerC.getSelectedItemPosition());
        coax_b = edittextB.getText().toString();
        coax_b_unit = Integer.toString(spinnerB.getSelectedItemPosition());
        coax_er = edittextEr.getText().toString();
        coax_L = edittextL.getText().toString();
        coax_L_unit = Integer.toString(spinnerL.getSelectedItemPosition());
        coax_Z0 = edittextZ0.getText().toString();
        coax_Z0_unit = Integer.toString(spinnerZ0.getSelectedItemPosition());
        coax_Eeff = edittextPhs.getText().toString();
        coax_Eeff_unit = Integer.toString(spinnerPhs.getSelectedItemPosition());
        coax_Freq = edittextFreq.getText().toString();
        coax_Freq_unit = Integer.toString(spinnerFreq.getSelectedItemPosition());
        //coax_T = edittext_T.getText().toString();
        //coax_T_unit = Integer.toString(spinner_T.getSelectedItemPosition());
        coax_flag = Integer.toString(target);

        editor.putString(Constants.COAX_A, coax_a);
        editor.putString(Constants.COAX_A_UNIT, coax_a_unit);
        editor.putString(Constants.COAX_B, coax_H);
        editor.putString(Constants.COAX_B_UNIT, coax_H_unit);
        editor.putString(Constants.COAX_C, coax_b);
        editor.putString(Constants.COAX_C_UNIT, coax_b_unit);
        editor.putString(Constants.COAX_ER, coax_er);
        editor.putString(Constants.COAX_L, coax_L);
        editor.putString(Constants.COAX_L_UNIT, coax_L_unit);
        editor.putString(Constants.COAX_Z0, coax_Z0);
        editor.putString(Constants.COAX_Z0_UNIT, coax_Z0_unit);
        editor.putString(Constants.COAX_PHS, coax_Eeff);
        editor.putString(Constants.COAX_PHS_UNIT, coax_Eeff_unit);
        editor.putString(Constants.COAX_FREQ, coax_Freq);
        editor.putString(Constants.COAX_FREQ_UNIT, coax_Freq_unit);
        editor.putString(Constants.COAX_TARGET, coax_flag);

        editor.apply();
    }

    private boolean analysisInputCheck() {
        boolean checkResult = true;
        if (edittextA.length() == 0) {
            edittextA.setError(getText(R.string.Error_a_empty));
            checkResult = false;
        }
        if (edittextFreq.length() == 0) {
            edittextFreq.setError(getText(R.string.Error_Freq_empty));
            checkResult = false;
        }
        if (edittextB.length() == 0) {
            edittextB.setError(getText(R.string.Error_b_empty));
            checkResult = false;
        }
        if (edittextC.length() == 0) {
            edittextC.setError(getText(R.string.Error_c_empty));
            checkResult = false;
        }
        if (edittextEr.length() == 0) {
            error_er.setSpan(new SubscriptSpan(), 13, 14, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            edittextEr.setError(error_er);
            checkResult = false;
        }
        return checkResult;
    }

    private boolean synthesizeInputCheck() {
        boolean checkResult = true;
        if (edittextZ0.length() == 0) {
            error_Z0.setSpan(new SubscriptSpan(), 13, 14, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            edittextZ0.setError(error_Z0);
            checkResult = false;
        }
        if (edittextFreq.length() == 0) {
            edittextFreq.setError(getText(R.string.Error_Freq_empty));
            checkResult = false;
        }
        //if (edittext_T.length() == 0) {
        //    edittext_T.setError(getText(R.string.Error_T_empty));
        //    checkResult = false;
        //}
        if (target == 0) {
            if (edittextB.length() == 0) {
                edittextB.setError(getText(R.string.Error_b_empty));
                checkResult = false;
            }
            if (edittextC.length() == 0) {
                edittextC.setError(getText(R.string.Error_c_empty));
                checkResult = false;
            }
            if (edittextEr.length() == 0) {
                error_er.setSpan(new SubscriptSpan(), 13, 14, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                edittextEr.setError(error_er);
                checkResult = false;
            }
        } else if (target == 1) {
            if (edittextA.length() == 0) {
                edittextA.setError(getText(R.string.Error_a_empty));
                checkResult = false;
            }
            if (edittextC.length() == 0) {
                edittextC.setError(getText(R.string.Error_c_empty));
                checkResult = false;
            }
            if (edittextEr.length() == 0) {
                error_er.setSpan(new SubscriptSpan(), 13, 14, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                edittextEr.setError(error_er);
                checkResult = false;
            }
        } else if (target == 2) {
            if (edittextA.length() == 0) {
                edittextA.setError(getText(R.string.Error_a_empty));
                checkResult = false;
            }
            if (edittextB.length() == 0) {
                edittextB.setError(getText(R.string.Error_b_empty));
                checkResult = false;
            }
            if (edittextEr.length() == 0) {
                error_er.setSpan(new SubscriptSpan(), 13, 14, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                edittextEr.setError(error_er);
                checkResult = false;
            }
        } else if (target == 3) {
            if (edittextA.length() == 0) {
                edittextA.setError(getText(R.string.Error_a_empty));
                checkResult = false;
            }
            if (edittextB.length() == 0) {
                edittextB.setError(getText(R.string.Error_b_empty));
                checkResult = false;
            }
            if (edittextC.length() == 0) {
                edittextC.setError(getText(R.string.Error_c_empty));
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

    public void addAdFragment(){
        adFragment = new AdFragment();
        FragmentManager fragmentManager = getFragmentManager();
        if(fragmentManager !=null&& viewRoot != null) {
            fragmentManager.beginTransaction().replace(R.id.ad_frame, adFragment).commit();
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
