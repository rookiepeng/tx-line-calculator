package com.rookiedev.microwavetools.fragments;

import android.content.SharedPreferences;
import android.graphics.Color;
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
import com.rookiedev.microwavetools.libs.COAX;
import com.rookiedev.microwavetools.libs.Constant;
import com.rookiedev.microwavetools.libs.LineCOAX;

import java.math.BigDecimal;

public class COAXFragment extends Fragment {
    public final static String COAX_A = "COAX_A";
    public final static String COAX_A_UNIT = "COAX_A_UNIT";
    public final static String COAX_H = "COAX_H";
    public final static String COAX_H_UNIT = "COAX_H_UNIT";
    public final static String COAX_B = "COAX_B";
    public final static String COAX_B_UNIT = "COAX_B_UNIT";
    public final static String COAX_er = "COAX_er";
    public final static String COAX_L = "COAX_L";
    public final static String COAX_L_UNIT = "COAX_L_UNIT";
    public final static String COAX_Z0 = "COAX_Z0";
    public final static String COAX_Z0_UNIT = "COAX_Z0_UNIT";
    public final static String COAX_Eeff = "COAX_Eeff";
    public final static String COAX_Eeff_UNIT = "COAX_Eeff_UNIT";
    public final static String COAX_Freq = "COAX_Freq";
    public final static String COAX_Freq_UNIT = "COAX_Freq_UNIT";
    public final static String COAX_T = "COAX_T";
    public final static String COAX_T_UNIT = "COAX_T_UNIT";
    public final static String COAX_Flag = "COAX_Flag";
    private View rootView, height_input, er_input, a_input, b_input;
    private CardView electricalCard, physicalCard;
    private int DecimalLength; // the length of the Decimal,
    private SpannableString error_er, error_Z0;
    private TextView text_er, text_Z0, text_Eeff; // strings which include the subscript
    private EditText edittext_a, //
            edittext_H, //
            edittext_b, //
            edittext_L, // the length
            edittext_Z0, // the impedance
            edittext_Eeff, // the electrical length
            edittext_Freq, // the frequency
            edittext_T, // the thickness of the metal
            edittext_er; // the relative dielectric constant
    private Button button_syn,// button synthesize
            button_ana;// button analyze
    private Spinner spinner_a, spinner_h, spinner_b, spinner_L, spinner_T,
            spinner_Z0, spinner_Eeff, spinner_Freq;// the units of each
    // parameter
    private int flag;
    private RadioButton radioBtn_a, radioBtn_H, radioBtn_b, radioBtn_er;
    private LineCOAX line;

    public COAXFragment() {
        // Empty constructor required for fragment subclasses
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.coax, container, false);

        initUI(); // initial the UI
        readSharedPref(); // read shared preferences
        setRadioBtn();

        button_ana.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int temp;
                Preference_SharedPref();
                if (!analysisInputCheck()) {
                    edittext_Z0.setText(""); // clear the Z0 and Eeff outputs
                    edittext_Eeff.setText("");
                } else {
                    line.setCoreRadius(Double.parseDouble(edittext_a.getText().toString()), spinner_a.getSelectedItemPosition());
                    line.setSubRadius(Double.parseDouble(edittext_H.getText().toString()), spinner_h.getSelectedItemPosition());
                    line.setCoreOffset(Double.parseDouble(edittext_b.getText().toString()), spinner_b.getSelectedItemPosition());
                    line.setFrequency(Double.parseDouble(edittext_Freq.getText().toString()), spinner_Freq.getSelectedItemPosition());
                    line.setSubEpsilon(Double.parseDouble(edittext_er.getText().toString()));

                    //T = Double.parseDouble(edittext_T.getText().toString());

                    if (!edittext_L.getText().toString().equals("")) { // check the L input
                        line.setMetalLength(Double.parseDouble(edittext_L.getText().toString()), spinner_L.getSelectedItemPosition());

                        COAX coax = new COAX();
                        line = coax.getAnaResult(line);

                        BigDecimal Eeff_temp = new BigDecimal(line.getElectricalLength()); // cut the decimal of the Eeff
                        double Eeff = Eeff_temp.setScale(DecimalLength,
                                BigDecimal.ROUND_HALF_UP).doubleValue();
                        edittext_Eeff.setText(String.valueOf(Eeff));
                    } else {
                        COAX coax = new COAX();
                        line = coax.getAnaResult(line);
                        //Z0 = coax.getZ0(); // calculate the Z0
                        edittext_Eeff.setText(""); // if the L input is empty, clear the Eeff
                    }
                    BigDecimal Z0_temp = new BigDecimal(line.getImpedance());
                    double Z0 = Z0_temp.setScale(DecimalLength,
                            BigDecimal.ROUND_HALF_UP).doubleValue();
                    edittext_Z0.setText(String.valueOf(Z0)); // cut the decimal of the Z0
                }
                forceRippleAnimation(electricalCard);
            }
        });

        button_syn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Preference_SharedPref();
                if (!synthesizeInputCheck()) {
                    if (flag == 0) {
                        edittext_a.setText("");
                    } else if (flag == 1) {
                        edittext_H.setText("");
                    } else if (flag == 2) {
                        edittext_b.setText("");
                    } else if (flag == 3) {
                        edittext_er.setText("");
                    }
                } else {
                    int temp;
                    line.setImpedance(Double.parseDouble(edittext_Z0.getText().toString()));
                    line.setFrequency(Double.parseDouble(edittext_Freq.getText().toString()), spinner_Freq.getSelectedItemPosition());

                    if (flag == Constant.Synthesize_CoreRadius) {
                        line.setSubRadius(Double.parseDouble(edittext_H.getText().toString()), spinner_h.getSelectedItemPosition());
                        line.setCoreOffset(Double.parseDouble(edittext_b.getText().toString()), spinner_b.getSelectedItemPosition());
                        line.setSubEpsilon(Double.parseDouble(edittext_er.getText().toString()));

                        //a = 0;
                    } else if (flag == Constant.Synthesize_Height) {
                        line.setCoreRadius(Double.parseDouble(edittext_a.getText().toString()), spinner_a.getSelectedItemPosition());
                        line.setCoreOffset(Double.parseDouble(edittext_b.getText().toString()), spinner_b.getSelectedItemPosition());
                        line.setSubEpsilon(Double.parseDouble(edittext_er.getText().toString()));

                        //height = 0;
                    } else if (flag == Constant.Synthesize_CoreOffset) {
                        line.setCoreRadius(Double.parseDouble(edittext_a.getText().toString()), spinner_a.getSelectedItemPosition());
                        line.setSubRadius(Double.parseDouble(edittext_H.getText().toString()), spinner_h.getSelectedItemPosition());
                        line.setSubEpsilon(Double.parseDouble(edittext_er.getText().toString()));

                        //b = 0;
                    } else if (flag == Constant.Synthesize_Er) {
                        line.setCoreRadius(Double.parseDouble(edittext_a.getText().toString()), spinner_a.getSelectedItemPosition());
                        line.setSubRadius(Double.parseDouble(edittext_H.getText().toString()), spinner_h.getSelectedItemPosition());
                        line.setCoreOffset(Double.parseDouble(edittext_b.getText().toString()), spinner_b.getSelectedItemPosition());

                        //er = 0;
                    }

                    if (edittext_Eeff.length() != 0) { // check if the Eeff is empty
                        line.setElectricalLength(Double.parseDouble(edittext_Eeff.getText().toString()));
                        COAX coax = new COAX();
                        line = coax.getSynResult(line, flag);
                        //coax.coax_syn();
                        //L = coax.getL();
                        //a = coax.geta();
                        //height = coax.getb();
                        //b = coax.getc();
                        //er = coax.geter();

                        BigDecimal L_temp = new BigDecimal(Constant.meter2others(line.getMetalLength(), spinner_L.getSelectedItemPosition())); // cut the decimal of L
                        double L = L_temp.setScale(DecimalLength, BigDecimal.ROUND_HALF_UP)
                                .doubleValue();
                        edittext_L.setText(String.valueOf(L));
                    } else {
                        COAX coax = new COAX();
                        line = coax.getSynResult(line, flag);
                        edittext_L.setText(""); // clear the L if the Eeff input is empty
                    }
                    if (flag == Constant.Synthesize_CoreRadius) {

                        BigDecimal a_temp = new BigDecimal(Constant.meter2others(line.getCoreRadius(), spinner_a.getSelectedItemPosition())); // cut the decimal of W
                        double a = a_temp.setScale(DecimalLength, BigDecimal.ROUND_HALF_UP)
                                .doubleValue();
                        edittext_a.setText(String.valueOf(a));
                    } else if (flag == Constant.Synthesize_Height) {

                        BigDecimal b_temp = new BigDecimal(Constant.meter2others(line.getSubRadius(), spinner_h.getSelectedItemPosition())); // cut the decimal of S
                        double height = b_temp.setScale(DecimalLength, BigDecimal.ROUND_HALF_UP)
                                .doubleValue();
                        edittext_H.setText(String.valueOf(height));
                    } else if (flag == Constant.Synthesize_CoreOffset) {

                        BigDecimal c_temp = new BigDecimal(Constant.meter2others(line.getCoreOffset(), spinner_b.getSelectedItemPosition()));
                        double b = c_temp.setScale(DecimalLength, BigDecimal.ROUND_HALF_UP)
                                .doubleValue();
                        edittext_b.setText(String.valueOf(b));
                    } else if (flag == Constant.Synthesize_Er) {
                        BigDecimal er_temp = new BigDecimal(line.getSubEpsilon());
                        double er = er_temp.setScale(DecimalLength, BigDecimal.ROUND_HALF_UP)
                                .doubleValue();
                        edittext_er.setText(String.valueOf(er));
                    }
                }
                forceRippleAnimation(physicalCard);
            }
        });

        return rootView;
    }

    private void initUI() {
        line = new LineCOAX();
        height_input = rootView.findViewById(R.id.height_input_radio);
        er_input = rootView.findViewById(R.id.epsilon_input_radio);
        a_input = rootView.findViewById(R.id.a_input_radio);
        b_input = rootView.findViewById(R.id.b_input_radio);

        View length_input = rootView.findViewById(R.id.length_input_radio);
        length_input.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.green_shadow));

        View z0_input = rootView.findViewById(R.id.z0_input);
        z0_input.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.blue_shadow));

        View eeff_input = rootView.findViewById(R.id.eeff_input);
        eeff_input.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.blue_shadow));

        physicalCard = (CardView) rootView.findViewById(R.id.physicalParaCard);
        electricalCard = (CardView) rootView.findViewById(R.id.electricalParaCard);

        error_er = new SpannableString(this.getString(R.string.Error_er_empty));
        error_Z0 = new SpannableString(this.getString(R.string.Error_Z0_empty));

        // Subscript strings
        text_er = (TextView) rootView.findViewById(R.id.text_er_radio);
        text_Z0 = (TextView) rootView.findViewById(R.id.text_Z0);
        text_Eeff = (TextView) rootView.findViewById(R.id.text_Eeff);

        SpannableString spanEr = new SpannableString(
                this.getString(R.string.text_er));
        spanEr.setSpan(new SubscriptSpan(), 1, 2,
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        text_er.append(spanEr);

        SpannableString spanZ0 = new SpannableString(
                this.getString(R.string.text_Z0));
        spanZ0.setSpan(new SubscriptSpan(), 1, 2,
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        text_Z0.append(spanZ0);

        SpannableString spanEeff = new SpannableString(
                this.getString(R.string.text_Eeff));
        spanEeff.setSpan(new SubscriptSpan(), 1, 4,
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        text_Eeff.append(spanEeff);

        // edittext elements
        edittext_a = (EditText) rootView.findViewById(R.id.editText_a_radio);
        edittext_H = (EditText) rootView.findViewById(R.id.editText_H_radio);
        edittext_b = (EditText) rootView.findViewById(R.id.editText_b_radio);
        edittext_L = (EditText) rootView.findViewById(R.id.editText_L_radio);
        edittext_Z0 = (EditText) rootView.findViewById(R.id.editText_Z0);
        edittext_Eeff = (EditText) rootView.findViewById(R.id.editText_Eeff);
        edittext_Freq = (EditText) rootView.findViewById(R.id.editText_Freq);
        edittext_T = (EditText) rootView.findViewById(R.id.editText_T_radio);
        edittext_er = (EditText) rootView.findViewById(R.id.editText_er_radio);

        // button elements
        button_ana = (Button) rootView.findViewById(R.id.button_ana);
        button_syn = (Button) rootView.findViewById(R.id.button_syn);

        // spinner elements
        spinner_a = (Spinner) rootView.findViewById(R.id.spinner_a_radio);
        spinner_h = (Spinner) rootView.findViewById(R.id.spinner_H_radio);
        spinner_b = (Spinner) rootView.findViewById(R.id.spinner_b_radio);
        spinner_L = (Spinner) rootView.findViewById(R.id.spinner_L_radio);
        spinner_Z0 = (Spinner) rootView.findViewById(R.id.spinner_Z0);
        spinner_Eeff = (Spinner) rootView.findViewById(R.id.spinner_Eeff);
        spinner_Freq = (Spinner) rootView.findViewById(R.id.spinner_Freq);
        spinner_T = (Spinner) rootView.findViewById(R.id.spinner_T_radio);

        // configure the length units
        ArrayAdapter<CharSequence> adapterLength = ArrayAdapter
                .createFromResource(this.getActivity(), R.array.list_units_Length,
                        android.R.layout.simple_spinner_item);
        adapterLength
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_a.setAdapter(adapterLength);
        spinner_h.setAdapter(adapterLength);
        spinner_b.setAdapter(adapterLength);
        spinner_L.setAdapter(adapterLength);
        spinner_T.setAdapter(adapterLength);

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
        radioBtn_a = (RadioButton) rootView.findViewById(R.id.radioBtn_a);
        radioBtn_H = (RadioButton) rootView.findViewById(R.id.radioBtn_H);
        radioBtn_b = (RadioButton) rootView.findViewById(R.id.radioBtn_b);
        radioBtn_er = (RadioButton) rootView.findViewById(R.id.radioBtn_er);
    }

    private void readSharedPref() {
        SharedPreferences prefs = getActivity().getSharedPreferences(Constant.SHARED_PREFS_NAME,
                AppCompatActivity.MODE_PRIVATE);// get the parameters from the Shared
        // Preferences in the device

        // read values from the shared preferences

        // coax parameters
        edittext_a.setText(prefs.getString(COAX_A, "0.30"));
        spinner_a.setSelection(Integer.parseInt(prefs.getString(COAX_A_UNIT,
                "2")));

        edittext_H.setText(prefs.getString(COAX_H, "1.00"));
        spinner_h.setSelection(Integer.parseInt(prefs.getString(COAX_H_UNIT,
                "2")));

        edittext_b.setText(prefs.getString(COAX_B, "0.00"));
        spinner_b.setSelection(Integer.parseInt(prefs.getString(COAX_B_UNIT,
                "2")));

        edittext_L.setText(prefs.getString(COAX_L, "1000"));
        spinner_L.setSelection(Integer.parseInt(prefs.getString(COAX_L_UNIT,
                "0")));

        edittext_Z0.setText(prefs.getString(COAX_Z0, "72.19"));
        spinner_Z0.setSelection(Integer.parseInt(prefs.getString(
                COAX_Z0_UNIT, "0")));

        edittext_Eeff.setText(prefs.getString(COAX_Eeff, "30.50"));
        spinner_Eeff.setSelection(Integer.parseInt(prefs.getString(
                COAX_Eeff_UNIT, "0")));

        edittext_Freq.setText(prefs.getString(COAX_Freq, "1.00"));
        spinner_Freq.setSelection(Integer.parseInt(prefs.getString(
                COAX_Freq_UNIT, "1")));

        edittext_er.setText(prefs.getString(COAX_er, "1.00"));

        edittext_T.setText(prefs.getString(COAX_T, "1.40"));
        spinner_T.setSelection(Integer.parseInt(prefs.getString(COAX_T_UNIT,
                "0")));
        flag = Integer.parseInt(prefs.getString(COAX_Flag, Integer.toString(Constant.Synthesize_CoreRadius)));
    }

    private void Preference_SharedPref() {
        SharedPreferences prefs = getActivity().getSharedPreferences(Constant.SHARED_PREFS_NAME,
                AppCompatActivity.MODE_PRIVATE);// get the parameters from the Shared
        // Preferences in the device
        // universal parameters
        DecimalLength = Integer.parseInt(prefs.getString("DecimalLength", "2"));
    }

    private void setRadioBtn() {
        if (flag == Constant.Synthesize_CoreRadius) {
            radioBtn_a.setChecked(true);
            radioBtn_H.setChecked(false);
            radioBtn_b.setChecked(false);
            radioBtn_er.setChecked(false);
            a_input.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.green_shadow));
            height_input.setBackgroundColor(Color.WHITE);
            b_input.setBackgroundColor(Color.WHITE);
            er_input.setBackgroundColor(Color.WHITE);
        } else if (flag == Constant.Synthesize_Height) {
            radioBtn_a.setChecked(false);
            radioBtn_H.setChecked(true);
            radioBtn_b.setChecked(false);
            radioBtn_er.setChecked(false);
            a_input.setBackgroundColor(Color.WHITE);
            height_input.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.green_shadow));
            b_input.setBackgroundColor(Color.WHITE);
            er_input.setBackgroundColor(Color.WHITE);
        } else if (flag == Constant.Synthesize_CoreOffset) {
            radioBtn_a.setChecked(false);
            radioBtn_H.setChecked(false);
            radioBtn_b.setChecked(true);
            radioBtn_er.setChecked(false);
            a_input.setBackgroundColor(Color.WHITE);
            height_input.setBackgroundColor(Color.WHITE);
            b_input.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.green_shadow));
            er_input.setBackgroundColor(Color.WHITE);
        } else if (flag == Constant.Synthesize_Er) {
            radioBtn_a.setChecked(false);
            radioBtn_H.setChecked(false);
            radioBtn_b.setChecked(false);
            radioBtn_er.setChecked(true);
            a_input.setBackgroundColor(Color.WHITE);
            height_input.setBackgroundColor(Color.WHITE);
            b_input.setBackgroundColor(Color.WHITE);
            er_input.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.green_shadow));
        }
        radioBtn_a.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                radioBtn_a.setChecked(true);
                radioBtn_H.setChecked(false);
                radioBtn_b.setChecked(false);
                radioBtn_er.setChecked(false);
                a_input.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.green_shadow));
                height_input.setBackgroundColor(Color.WHITE);
                b_input.setBackgroundColor(Color.WHITE);
                er_input.setBackgroundColor(Color.WHITE);
                flag = Constant.Synthesize_CoreRadius;
            }
        });
        radioBtn_H.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                radioBtn_a.setChecked(false);
                radioBtn_H.setChecked(true);
                radioBtn_b.setChecked(false);
                radioBtn_er.setChecked(false);
                a_input.setBackgroundColor(Color.WHITE);
                height_input.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.green_shadow));
                b_input.setBackgroundColor(Color.WHITE);
                er_input.setBackgroundColor(Color.WHITE);
                flag = Constant.Synthesize_Height;
            }
        });
        radioBtn_b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                radioBtn_a.setChecked(false);
                radioBtn_H.setChecked(false);
                radioBtn_b.setChecked(true);
                radioBtn_er.setChecked(false);
                a_input.setBackgroundColor(Color.WHITE);
                height_input.setBackgroundColor(Color.WHITE);
                b_input.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.green_shadow));
                er_input.setBackgroundColor(Color.WHITE);
                flag = Constant.Synthesize_CoreOffset;
            }
        });
        radioBtn_er.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                radioBtn_a.setChecked(false);
                radioBtn_H.setChecked(false);
                radioBtn_b.setChecked(false);
                radioBtn_er.setChecked(true);
                a_input.setBackgroundColor(Color.WHITE);
                height_input.setBackgroundColor(Color.WHITE);
                b_input.setBackgroundColor(Color.WHITE);
                er_input.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.green_shadow));
                flag = Constant.Synthesize_Er;
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        String coax_a, coax_H, coax_b, coax_er, coax_L, coax_Z0, coax_Eeff, coax_Freq, coax_T;
        String coax_a_unit, coax_H_unit, coax_b_unit, coax_L_unit, coax_Z0_unit, coax_Eeff_unit, coax_Freq_unit, coax_T_unit;
        String coax_flag;

        SharedPreferences prefs = getActivity().getSharedPreferences(Constant.SHARED_PREFS_NAME,
                AppCompatActivity.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        coax_a = edittext_a.getText().toString();
        coax_a_unit = Integer.toString(spinner_a.getSelectedItemPosition());
        coax_H = edittext_H.getText().toString();
        coax_H_unit = Integer.toString(spinner_h.getSelectedItemPosition());
        coax_b = edittext_b.getText().toString();
        coax_b_unit = Integer.toString(spinner_b.getSelectedItemPosition());
        coax_er = edittext_er.getText().toString();
        coax_L = edittext_L.getText().toString();
        coax_L_unit = Integer.toString(spinner_L.getSelectedItemPosition());
        coax_Z0 = edittext_Z0.getText().toString();
        coax_Z0_unit = Integer.toString(spinner_Z0.getSelectedItemPosition());
        coax_Eeff = edittext_Eeff.getText().toString();
        coax_Eeff_unit = Integer.toString(spinner_Eeff
                .getSelectedItemPosition());
        coax_Freq = edittext_Freq.getText().toString();
        coax_Freq_unit = Integer.toString(spinner_Freq
                .getSelectedItemPosition());
        coax_T = edittext_T.getText().toString();
        coax_T_unit = Integer.toString(spinner_T.getSelectedItemPosition());
        coax_flag = Integer.toString(flag);

        editor.putString(COAX_A, coax_a);
        editor.putString(COAX_A_UNIT, coax_a_unit);
        editor.putString(COAX_H, coax_H);
        editor.putString(COAX_H_UNIT, coax_H_unit);
        editor.putString(COAX_B, coax_b);
        editor.putString(COAX_B_UNIT, coax_b_unit);
        editor.putString(COAX_er, coax_er);
        editor.putString(COAX_L, coax_L);
        editor.putString(COAX_L_UNIT, coax_L_unit);
        editor.putString(COAX_Z0, coax_Z0);
        editor.putString(COAX_Z0_UNIT, coax_Z0_unit);
        editor.putString(COAX_Eeff, coax_Eeff);
        editor.putString(COAX_Eeff_UNIT, coax_Eeff_unit);
        editor.putString(COAX_Freq, coax_Freq);
        editor.putString(COAX_Freq_UNIT, coax_Freq_unit);
        editor.putString(COAX_T, coax_T);
        editor.putString(COAX_T_UNIT, coax_T_unit);
        editor.putString(COAX_Flag, coax_flag);

        editor.apply();
    }

    private boolean analysisInputCheck() {
        boolean checkResult = true;
        if (edittext_a.length() == 0) {
            edittext_a.setError(getText(R.string.Error_a_empty));
            checkResult = false;
        }
        if (edittext_Freq.length() == 0) {
            edittext_Freq.setError(getText(R.string.Error_Freq_empty));
            checkResult = false;
        }
        if (edittext_H.length() == 0) {
            edittext_H.setError(getText(R.string.Error_b_empty));
            checkResult = false;
        }
        if (edittext_b.length() == 0) {
            edittext_b.setError(getText(R.string.Error_c_empty));
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
        if (edittext_T.length() == 0) {
            edittext_T.setError(getText(R.string.Error_T_empty));
            checkResult = false;
        }
        if (flag == 0) {
            if (edittext_H.length() == 0) {
                edittext_H.setError(getText(R.string.Error_b_empty));
                checkResult = false;
            }
            if (edittext_b.length() == 0) {
                edittext_b.setError(getText(R.string.Error_c_empty));
                checkResult = false;
            }
            if (edittext_er.length() == 0) {
                error_er.setSpan(new SubscriptSpan(), 13, 14,
                        Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                edittext_er.setError(error_er);
                checkResult = false;
            }
        } else if (flag == 1) {
            if (edittext_a.length() == 0) {
                edittext_a.setError(getText(R.string.Error_a_empty));
                checkResult = false;
            }
            if (edittext_b.length() == 0) {
                edittext_b.setError(getText(R.string.Error_c_empty));
                checkResult = false;
            }
            if (edittext_er.length() == 0) {
                error_er.setSpan(new SubscriptSpan(), 13, 14,
                        Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                edittext_er.setError(error_er);
                checkResult = false;
            }
        } else if (flag == 2) {
            if (edittext_a.length() == 0) {
                edittext_a.setError(getText(R.string.Error_a_empty));
                checkResult = false;
            }
            if (edittext_H.length() == 0) {
                edittext_H.setError(getText(R.string.Error_b_empty));
                checkResult = false;
            }
            if (edittext_er.length() == 0) {
                error_er.setSpan(new SubscriptSpan(), 13, 14,
                        Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                edittext_er.setError(error_er);
                checkResult = false;
            }
        } else if (flag == 3) {
            if (edittext_a.length() == 0) {
                edittext_a.setError(getText(R.string.Error_a_empty));
                checkResult = false;
            }
            if (edittext_H.length() == 0) {
                edittext_H.setError(getText(R.string.Error_b_empty));
                checkResult = false;
            }
            if (edittext_b.length() == 0) {
                edittext_b.setError(getText(R.string.Error_c_empty));
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

            rippleDrawable.setState(new int[]{android.R.attr.state_pressed, android.R.attr.state_enabled});

            view.setClickable(false);
            rippleDrawable.setState(new int[]{});
        }
    }
}