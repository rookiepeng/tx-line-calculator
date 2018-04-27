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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.rookiedev.microwavetools.R;
import com.rookiedev.microwavetools.libs.CPWG;
import com.rookiedev.microwavetools.libs.Constant;
import com.rookiedev.microwavetools.libs.LineCPW_G;

import java.math.BigDecimal;

public class CpwFragment extends Fragment {
    public final static String CPW_W = "CPW_W";
    public final static String CPW_W_UNIT = "CPW_W_UNIT";
    public final static String CPW_S = "CPW_S";
    public final static String CPW_S_UNIT = "CPW_S_UNIT";
    public final static String CPW_L = "CPW_L";
    public final static String CPW_L_UNIT = "CPW_L_UNIT";
    public final static String CPW_Z0 = "CPW_Z0";
    public final static String CPW_Z0_UNIT = "CPW_Z0_UNIT";
    public final static String CPW_Eeff = "CPW_Eeff";
    public final static String CPW_Eeff_UNIT = "CPW_Eeff_UNIT";
    public final static String CPW_Freq = "CPW_Freq";
    public final static String CPW_Freq_UNIT = "CPW_Freq_UNIT";
    public final static String CPW_er = "CPW_er";
    public final static String CPW_H = "CPW_H";
    public final static String CPW_H_UNIT = "CPW_H_UNIT";
    public final static String CPW_T = "CPW_T";
    public final static String CPW_T_UNIT = "CPW_T_UNIT";
    public final static String CPW_Flag = "CPW_Flag";
    public final static String CPW_WITH_GROUND = "CPW_WITH_GROUND";
    private View rootView, width_input, space_input, height_input, er_input;
    private CardView electricalCard, physicalCard;
    private int DecimalLength; // the length of the Decimal, accurate of the result
    private SpannableString error_er, error_Z0;
    private TextView text_er, text_Z0, text_Eeff; // strings which include the subscript
    private EditText edittext_W, // the width
            edittext_S, //
            edittext_L, // the length
            edittext_Z0, // the impedance
            edittext_Eeff, // the electrical length
            edittext_Freq, // the frequency
            edittext_T, // the thickness of the metal
            edittext_H, // the thickness of the dielectric
            edittext_er; // the relative dielectric constant
    private Button button_syn,// button synthesize
            button_ana;// button analyze
    private Spinner spinner_W, spinner_S, spinner_L, spinner_T, spinner_H,
            spinner_Z0, spinner_Eeff, spinner_Freq;// the units of each parameter
    private int flag;
    private RadioButton radioBtn_W, radioBtn_S, radioBtn_H, radioBtn_er;
    private CheckBox withGround;
    private boolean hasGround;
    private ImageView CPW_G;
    private LineCPW_G line;

    public CpwFragment() {
        // Empty constructor required for fragment subclasses
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.cpw, container, false);
        initUI(); // initial the UI
        readSharedPref(); // read shared preferences
        setRadioBtn();
        setImage();

        button_ana.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int temp;
                Preference_SharedPref();
                if (!analysisInputCheck()) {
                    edittext_Z0.setText(""); // clear the Z0 and Eeff outputs
                    edittext_Eeff.setText("");
                } else {
                    line.setMetalWidth(Double.parseDouble(edittext_W.getText().toString()), spinner_W.getSelectedItemPosition());
                    line.setMetalSpace(Double.parseDouble(edittext_S.getText().toString()), spinner_S.getSelectedItemPosition());
                    line.setFrequency(Double.parseDouble(edittext_Freq.getText().toString()), spinner_Freq.getSelectedItemPosition());
                    line.setSubEpsilon(Double.parseDouble(edittext_er.getText().toString()));
                    line.setSubHeight(Double.parseDouble(edittext_H.getText().toString()), spinner_H.getSelectedItemPosition());
                    line.setMetalThick(Double.parseDouble(edittext_T.getText().toString()), spinner_T.getSelectedItemPosition());

                    if (edittext_L.length() != 0) { // check the L input
                        line.setMetalLength(Double.parseDouble(edittext_L.getText().toString()), spinner_L.getSelectedItemPosition());

                        CPWG cpwg = new CPWG();
                        line = cpwg.getAnaResult(line, withGround.isChecked());

                        /*if (withGround.isChecked()) {
                            CPWG cpwg = new CPWG(W, S, H, er, L, 0, 0, Freq, T, flag);
                            Z0 = cpwg.getZ0();
                            Eeff = cpwg.getEeff();
                        } else {
                            CPW cpw = new CPW(W, S, H, er, L, 0, 0, Freq, T, flag);
                            Z0 = cpw.getZ0();
                            Eeff = cpw.getEeff();
                        }*/
                        BigDecimal Eeff_temp = new BigDecimal(line.getElectricalLength()); // cut the decimal of the Eeff
                        double Eeff = Eeff_temp.setScale(DecimalLength,
                                BigDecimal.ROUND_HALF_UP).doubleValue();
                        edittext_Eeff.setText(String.valueOf(Eeff));
                    } else {
                        CPWG cpwg = new CPWG();
                        line = cpwg.getAnaResult(line, withGround.isChecked());
                        /*if (withGround.isChecked()) {
                            CPWG cpwg = new CPWG(W, S, H, er, 0, 0, 0, Freq, T, flag);
                            Z0 = cpwg.getZ0();
                        } else {
                            CPW cpw = new CPW(W, S, H, er, 0, 0, 0, Freq, T, flag);
                            Z0 = cpw.getZ0();
                        }*/
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
                    if (flag == Constant.Synthesize_Width) {
                        edittext_W.setText("");
                    } else if (flag == Constant.Synthesize_Gap) {
                        edittext_S.setText("");
                    } else if (flag == Constant.Synthesize_Height) {
                        edittext_H.setText("");
                    } else if (flag == Constant.Synthesize_Er) {
                        edittext_er.setText("");
                    }
                } else {
                    int temp;
                    line.setImpedance(Double.parseDouble(edittext_Z0.getText().toString()));
                    line.setFrequency(Double.parseDouble(edittext_Freq.getText().toString()), spinner_Freq.getSelectedItemPosition());
                    line.setMetalThick(Double.parseDouble(edittext_T.getText().toString()), spinner_T.getSelectedItemPosition());
                    //Z0 = Double.parseDouble(edittext_Z0.getText().toString()); // get the parameters

                    if (flag == Constant.Synthesize_Width) {
                        line.setSubHeight(Double.parseDouble(edittext_H.getText().toString()), spinner_H.getSelectedItemPosition());
                        line.setMetalSpace(Double.parseDouble(edittext_S.getText().toString()), spinner_S.getSelectedItemPosition());
                        line.setSubEpsilon(Double.parseDouble(edittext_er.getText().toString()));

                        //W = 0;
                    } else if (flag == Constant.Synthesize_Gap) {
                        line.setSubHeight(Double.parseDouble(edittext_H.getText().toString()), spinner_H.getSelectedItemPosition());
                        line.setMetalWidth(Double.parseDouble(edittext_W.getText().toString()), spinner_W.getSelectedItemPosition());
                        line.setSubEpsilon(Double.parseDouble(edittext_er.getText().toString()));

                        //S = 0;
                    } else if (flag == Constant.Synthesize_Height) {
                        line.setMetalSpace(Double.parseDouble(edittext_S.getText().toString()), spinner_S.getSelectedItemPosition());
                        line.setMetalWidth(Double.parseDouble(edittext_W.getText().toString()), spinner_W.getSelectedItemPosition());
                        line.setSubEpsilon(Double.parseDouble(edittext_er.getText().toString()));

                        //H = 0;
                    } else if (flag == Constant.Synthesize_Er) {
                        line.setMetalSpace(Double.parseDouble(edittext_S.getText().toString()), spinner_S.getSelectedItemPosition());
                        line.setMetalWidth(Double.parseDouble(edittext_W.getText().toString()), spinner_W.getSelectedItemPosition());
                        line.setSubHeight(Double.parseDouble(edittext_H.getText().toString()), spinner_H.getSelectedItemPosition());

                        //er = 0;
                    }

                    if (edittext_Eeff.length() != 0) { // check if the Eeff is empty
                        line.setElectricalLength(Double.parseDouble(edittext_Eeff.getText().toString()));
                        CPWG cpwg = new CPWG();
                        line = cpwg.getSynResult(line, flag, withGround.isChecked());

                        /*if (withGround.isChecked()) {
                            CPWG cpwg = new CPWG(W, S, H, er, 0, Z0, Eeff, Freq, T, flag);
                            cpwg.coplanar_syn();
                            W = cpwg.getW();
                            S = cpwg.getS();
                            H = cpwg.getH();
                            er = cpwg.geter();
                            L = cpwg.getL();
                        } else {
                            CPW cpw = new CPW(W, S, H, er, 0, Z0, Eeff, Freq, T, flag);
                            cpw.coplanar_syn();
                            W = cpw.getW();
                            S = cpw.getS();
                            H = cpw.getH();
                            er = cpw.geter();
                            L = cpw.getL();
                        }*/
                        BigDecimal L_temp = new BigDecimal(Constant.meter2others(line.getMetalLength(), spinner_L.getSelectedItemPosition())); // cut the
                        // decimal of L
                        double L = L_temp.setScale(DecimalLength, BigDecimal.ROUND_HALF_UP)
                                .doubleValue();
                        edittext_L.setText(String.valueOf(L));
                    } else {
                        CPWG cpwg = new CPWG();
                        line = cpwg.getSynResult(line, flag, withGround.isChecked());
                        /*if (withGround.isChecked()) {
                            CPWG cpwg = new CPWG(W, S, H, er, 0, Z0, 0, Freq, T, flag);
                            cpwg.coplanar_syn();
                            W = cpwg.getW();
                            S = cpwg.getS();
                            H = cpwg.getH();
                            er = cpwg.geter();
                            L = cpwg.getL();
                        } else {
                            CPW cpw = new CPW(W, S, H, er, 0, Z0, 0, Freq, T, flag);
                            cpw.coplanar_syn();
                            W = cpw.getW();
                            S = cpw.getS();
                            H = cpw.getH();
                            er = cpw.geter();
                            L = cpw.getL();
                        }*/
                        edittext_L.setText(""); // clear the L if the Eeff input is empty
                    }
                    if (flag == Constant.Synthesize_Width) {
                        BigDecimal W_temp = new BigDecimal(Constant.meter2others(line.getMetalWidth(), spinner_W.getSelectedItemPosition())); // cut the decimal of W
                        double W = W_temp.setScale(DecimalLength, BigDecimal.ROUND_HALF_UP)
                                .doubleValue();
                        edittext_W.setText(String.valueOf(W));
                    } else if (flag == Constant.Synthesize_Gap) {
                        BigDecimal S_temp = new BigDecimal(Constant.meter2others(line.getMetalSpace(), spinner_S.getSelectedItemPosition())); // cut the decimal of S
                        double S = S_temp.setScale(DecimalLength, BigDecimal.ROUND_HALF_UP)
                                .doubleValue();
                        edittext_S.setText(String.valueOf(S));
                    } else if (flag == Constant.Synthesize_Height) {
                        BigDecimal H_temp = new BigDecimal(Constant.meter2others(line.getSubHeight(), spinner_H.getSelectedItemPosition()));
                        double H = H_temp.setScale(DecimalLength, BigDecimal.ROUND_HALF_UP)
                                .doubleValue();
                        edittext_H.setText(String.valueOf(H));
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
        line = new LineCPW_G();
        width_input = rootView.findViewById(R.id.width_input_radio);
        height_input = rootView.findViewById(R.id.height_input_radio);
        er_input = rootView.findViewById(R.id.epsilon_input_radio);
        space_input = rootView.findViewById(R.id.space_input_radio);
        //width_input.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.green_shadow));

        View length_input = rootView.findViewById(R.id.length_input_radio);
        length_input.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.green_shadow));

        View z0_input = rootView.findViewById(R.id.z0_input);
        z0_input.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.blue_shadow));

        View eeff_input = rootView.findViewById(R.id.eeff_input);
        eeff_input.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.blue_shadow));

        physicalCard=(CardView) rootView.findViewById(R.id.physicalParaCard);
        electricalCard = (CardView) rootView.findViewById(R.id.electricalParaCard);

        error_er = new SpannableString(this.getString(R.string.Error_er_empty));
        error_Z0 = new SpannableString(this.getString(R.string.Error_Z0_empty));
        /** find the elements */

        // Subscript strings
        text_er = (TextView) rootView.findViewById(R.id.text_er_radio);
        text_Z0 = (TextView) rootView.findViewById(R.id.text_Z0);
        text_Eeff = (TextView) rootView.findViewById(R.id.text_Phs);

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
        edittext_W = (EditText) rootView.findViewById(R.id.editText_W_radio);
        edittext_S = (EditText) rootView.findViewById(R.id.editText_S_radio);
        edittext_L = (EditText) rootView.findViewById(R.id.editText_L_radio);
        edittext_Z0 = (EditText) rootView.findViewById(R.id.editText_Z0);
        edittext_Eeff = (EditText) rootView.findViewById(R.id.editText_Phs);
        edittext_Freq = (EditText) rootView.findViewById(R.id.editText_Freq);
        edittext_T = (EditText) rootView.findViewById(R.id.editText_T_radio);
        edittext_H = (EditText) rootView.findViewById(R.id.editText_H_radio);
        edittext_er = (EditText) rootView.findViewById(R.id.editText_er_radio);

        // button elements
        button_ana = (Button) rootView.findViewById(R.id.button_ana);
        button_syn = (Button) rootView.findViewById(R.id.button_syn);

        // spinner elements
        spinner_W = (Spinner) rootView.findViewById(R.id.spinner_W_radio);
        spinner_S = (Spinner) rootView.findViewById(R.id.spinner_S_radio);
        spinner_L = (Spinner) rootView.findViewById(R.id.spinner_L_radio);
        spinner_Z0 = (Spinner) rootView.findViewById(R.id.spinner_Z0);
        spinner_Eeff = (Spinner) rootView.findViewById(R.id.spinner_Phs);
        spinner_Freq = (Spinner) rootView.findViewById(R.id.spinner_Freq);
        spinner_T = (Spinner) rootView.findViewById(R.id.spinner_T_radio);
        spinner_H = (Spinner) rootView.findViewById(R.id.spinner_H_radio);

        // configure the length units
        ArrayAdapter<CharSequence> adapterLength = ArrayAdapter
                .createFromResource(this.getActivity(), R.array.list_units_Length,
                        android.R.layout.simple_spinner_item);
        adapterLength
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_W.setAdapter(adapterLength);
        spinner_S.setAdapter(adapterLength);
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

        radioBtn_W = (RadioButton) rootView.findViewById(R.id.radioBtn_W);
        radioBtn_S = (RadioButton) rootView.findViewById(R.id.radioBtn_S);
        radioBtn_H = (RadioButton) rootView.findViewById(R.id.radioBtn_H);
        radioBtn_er = (RadioButton) rootView.findViewById(R.id.radioBtn_er);

        withGround = (CheckBox) rootView.findViewById(R.id.checkBoxGround);
        CPW_G = (ImageView) rootView.findViewById(R.id.cpw_G);
    }

    private void readSharedPref() {
        SharedPreferences prefs = getActivity().getSharedPreferences(Constant.SHARED_PREFS_NAME,
                AppCompatActivity.MODE_PRIVATE);// get the parameters from the Shared
        // Preferences in the device

        // read values from the shared preferences

        // cpw parameters
        edittext_W.setText(prefs.getString(CPW_W, "18.00"));
        spinner_W.setSelection(Integer.parseInt(prefs.getString(CPW_W_UNIT,
                "0")));

        edittext_S.setText(prefs.getString(CPW_S, "20.00"));
        spinner_S.setSelection(Integer.parseInt(prefs
                .getString(CPW_S_UNIT, "0")));

        edittext_L.setText(prefs.getString(CPW_L, "1000.0"));
        spinner_L.setSelection(Integer.parseInt(prefs.getString(CPW_L_UNIT,
                "0")));

        edittext_Z0.setText(prefs.getString(CPW_Z0, "100.00"));
        spinner_Z0.setSelection(Integer.parseInt(prefs.getString(CPW_Z0_UNIT,
                "0")));

        edittext_Eeff.setText(prefs.getString(CPW_Eeff, "41.75"));
        spinner_Eeff.setSelection(Integer.parseInt(prefs.getString(
                CPW_Eeff_UNIT, "0")));

        edittext_Freq.setText(prefs.getString(CPW_Freq, "1.00"));
        spinner_Freq.setSelection(Integer.parseInt(prefs.getString(
                CPW_Freq_UNIT, "1")));

        edittext_er.setText(prefs.getString(CPW_er, "4.00"));

        edittext_H.setText(prefs.getString(CPW_H, "10.00"));
        spinner_H.setSelection(Integer.parseInt(prefs.getString(CPW_H_UNIT,
                "0")));

        edittext_T.setText(prefs.getString(CPW_T, "1.40"));
        spinner_T.setSelection(Integer.parseInt(prefs.getString(CPW_T_UNIT,
                "0")));
        flag = Integer.parseInt(prefs.getString(CPW_Flag, "0"));

        hasGround = prefs.getString(CPW_WITH_GROUND, "true").equals("true");
    }

    private void Preference_SharedPref() {
        SharedPreferences prefs = getActivity().getSharedPreferences(Constant.SHARED_PREFS_NAME,
                AppCompatActivity.MODE_PRIVATE);// get the parameters from the Shared
        // Preferences in the device
        // universal parameters
        DecimalLength = Integer.parseInt(prefs.getString("DecimalLength", "2"));
    }

    private void setRadioBtn() {
        if (flag == Constant.Synthesize_Width) {
            radioBtn_W.setChecked(true);
            radioBtn_S.setChecked(false);
            radioBtn_H.setChecked(false);
            radioBtn_er.setChecked(false);
            width_input.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.green_shadow));
            space_input.setBackgroundColor(Color.WHITE);
            height_input.setBackgroundColor(Color.WHITE);
            er_input.setBackgroundColor(Color.WHITE);
        } else if (flag == Constant.Synthesize_Gap) {
            radioBtn_W.setChecked(false);
            radioBtn_S.setChecked(true);
            radioBtn_H.setChecked(false);
            radioBtn_er.setChecked(false);
            width_input.setBackgroundColor(Color.WHITE);
            space_input.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.green_shadow));
            height_input.setBackgroundColor(Color.WHITE);
            er_input.setBackgroundColor(Color.WHITE);
        } else if (flag == Constant.Synthesize_Height) {
            radioBtn_W.setChecked(false);
            radioBtn_S.setChecked(false);
            radioBtn_H.setChecked(true);
            radioBtn_er.setChecked(false);
            width_input.setBackgroundColor(Color.WHITE);
            space_input.setBackgroundColor(Color.WHITE);
            height_input.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.green_shadow));
            er_input.setBackgroundColor(Color.WHITE);
        } else if (flag == Constant.Synthesize_Er) {
            radioBtn_W.setChecked(false);
            radioBtn_S.setChecked(false);
            radioBtn_H.setChecked(false);
            radioBtn_er.setChecked(true);
            width_input.setBackgroundColor(Color.WHITE);
            space_input.setBackgroundColor(Color.WHITE);
            height_input.setBackgroundColor(Color.WHITE);
            er_input.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.green_shadow));
        }
        radioBtn_W.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                radioBtn_W.setChecked(true);
                radioBtn_S.setChecked(false);
                radioBtn_H.setChecked(false);
                radioBtn_er.setChecked(false);
                width_input.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.green_shadow));
                space_input.setBackgroundColor(Color.WHITE);
                height_input.setBackgroundColor(Color.WHITE);
                er_input.setBackgroundColor(Color.WHITE);
                flag = Constant.Synthesize_Width;
            }
        });
        radioBtn_S.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                radioBtn_W.setChecked(false);
                radioBtn_S.setChecked(true);
                radioBtn_H.setChecked(false);
                radioBtn_er.setChecked(false);
                width_input.setBackgroundColor(Color.WHITE);
                space_input.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.green_shadow));
                height_input.setBackgroundColor(Color.WHITE);
                er_input.setBackgroundColor(Color.WHITE);
                flag = Constant.Synthesize_Gap;
            }
        });
        radioBtn_H.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                radioBtn_W.setChecked(false);
                radioBtn_S.setChecked(false);
                radioBtn_H.setChecked(true);
                radioBtn_er.setChecked(false);
                width_input.setBackgroundColor(Color.WHITE);
                space_input.setBackgroundColor(Color.WHITE);
                height_input.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.green_shadow));
                er_input.setBackgroundColor(Color.WHITE);
                flag = Constant.Synthesize_Height;
            }
        });
        radioBtn_er.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                radioBtn_W.setChecked(false);
                radioBtn_S.setChecked(false);
                radioBtn_H.setChecked(false);
                radioBtn_er.setChecked(true);
                width_input.setBackgroundColor(Color.WHITE);
                space_input.setBackgroundColor(Color.WHITE);
                height_input.setBackgroundColor(Color.WHITE);
                er_input.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.green_shadow));
                flag = Constant.Synthesize_Er;
            }
        });
    }

    private void setImage() {
        withGround.setChecked(hasGround);
        if (hasGround) {
            CPW_G.setImageResource(R.drawable.vt_cpwg);
        } else {
            CPW_G.setImageResource(R.drawable.vt_cpw);
        }
        withGround.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    CPW_G.setImageResource(R.drawable.vt_cpwg);
                } else {
                    CPW_G.setImageResource(R.drawable.vt_cpw);
                }
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();

        String cpw_W, cpw_S, cpw_H, cpw_er, cpw_L, cpw_Z0, cpw_Eeff, cpw_Freq, cpw_T;
        String cpw_W_unit, cpw_S_unit, cpw_H_unit, cpw_L_unit, cpw_Z0_unit, cpw_Eeff_unit, cpw_Freq_unit, cpw_T_unit;
        String cpw_flag;
        String cpw_with_ground;

        SharedPreferences prefs = getActivity().getSharedPreferences(Constant.SHARED_PREFS_NAME,
                AppCompatActivity.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        cpw_W = edittext_W.getText().toString();
        cpw_W_unit = Integer.toString(spinner_W.getSelectedItemPosition());
        cpw_S = edittext_S.getText().toString();
        cpw_S_unit = Integer.toString(spinner_S.getSelectedItemPosition());
        cpw_H = edittext_H.getText().toString();
        cpw_H_unit = Integer.toString(spinner_H.getSelectedItemPosition());
        cpw_er = edittext_er.getText().toString();
        cpw_L = edittext_L.getText().toString();
        cpw_L_unit = Integer.toString(spinner_L.getSelectedItemPosition());
        cpw_Z0 = edittext_Z0.getText().toString();
        cpw_Z0_unit = Integer.toString(spinner_Z0.getSelectedItemPosition());
        cpw_Eeff = edittext_Eeff.getText().toString();
        cpw_Eeff_unit = Integer
                .toString(spinner_Eeff.getSelectedItemPosition());
        cpw_Freq = edittext_Freq.getText().toString();
        cpw_Freq_unit = Integer
                .toString(spinner_Freq.getSelectedItemPosition());
        cpw_T = edittext_T.getText().toString();
        cpw_T_unit = Integer.toString(spinner_T.getSelectedItemPosition());
        cpw_flag = Integer.toString(flag);
        if (withGround.isChecked()) {
            cpw_with_ground = "true";
        } else {
            cpw_with_ground = "false";
        }

        editor.putString(CPW_W, cpw_W);
        editor.putString(CPW_W_UNIT, cpw_W_unit);
        editor.putString(CPW_S, cpw_S);
        editor.putString(CPW_S_UNIT, cpw_S_unit);
        editor.putString(CPW_H, cpw_H);
        editor.putString(CPW_H_UNIT, cpw_H_unit);
        editor.putString(CPW_er, cpw_er);
        editor.putString(CPW_L, cpw_L);
        editor.putString(CPW_L_UNIT, cpw_L_unit);
        editor.putString(CPW_Z0, cpw_Z0);
        editor.putString(CPW_Z0_UNIT, cpw_Z0_unit);
        editor.putString(CPW_Eeff, cpw_Eeff);
        editor.putString(CPW_Eeff_UNIT, cpw_Eeff_unit);
        editor.putString(CPW_Freq, cpw_Freq);
        editor.putString(CPW_Freq_UNIT, cpw_Freq_unit);
        editor.putString(CPW_T, cpw_T);
        editor.putString(CPW_T_UNIT, cpw_T_unit);
        editor.putString(CPW_Flag, cpw_flag);
        editor.putString(CPW_WITH_GROUND, cpw_with_ground);

        editor.apply();
    }

    private boolean analysisInputCheck() {
        boolean checkResult = true;
        if (edittext_W.length() == 0) {
            edittext_W.setError(getText(R.string.Error_W_empty));
            checkResult = false;
        }
        if (edittext_S.length() == 0) {
            edittext_S.setError(getText(R.string.Error_S_empty));
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
        if (edittext_T.length() == 0) {
            edittext_T.setError(getText(R.string.Error_T_empty));
            checkResult = false;
        }
        if (flag == 0) {
            if (edittext_S.length() == 0) {
                edittext_S.setError(getText(R.string.Error_S_empty));
                checkResult = false;
            }
            if (edittext_H.length() == 0) {
                edittext_H.setError(getText(R.string.Error_H_empty));
                checkResult = false;
            }
            if (edittext_er.length() == 0) {
                error_er.setSpan(new SubscriptSpan(), 13, 14,
                        Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                edittext_er.setError(error_er);
                checkResult = false;
            }
        } else if (flag == 1) {
            if (edittext_W.length() == 0) {
                edittext_W.setError(getText(R.string.Error_W_empty));
                checkResult = false;
            }
            if (edittext_H.length() == 0) {
                edittext_H.setError(getText(R.string.Error_H_empty));
                checkResult = false;
            }
            if (edittext_er.length() == 0) {
                error_er.setSpan(new SubscriptSpan(), 13, 14,
                        Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                edittext_er.setError(error_er);
                checkResult = false;
            }
        } else if (flag == 2) {
            if (edittext_W.length() == 0) {
                edittext_W.setError(getText(R.string.Error_W_empty));
                checkResult = false;
            }
            if (edittext_S.length() == 0) {
                edittext_S.setError(getText(R.string.Error_S_empty));
                checkResult = false;
            }
            if (edittext_er.length() == 0) {
                error_er.setSpan(new SubscriptSpan(), 13, 14,
                        Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                edittext_er.setError(error_er);
                checkResult = false;
            }
        } else if (flag == 3) {
            if (edittext_W.length() == 0) {
                edittext_W.setError(getText(R.string.Error_W_empty));
                checkResult = false;
            }
            if (edittext_S.length() == 0) {
                edittext_S.setError(getText(R.string.Error_S_empty));
                checkResult = false;
            }
            if (edittext_H.length() == 0) {
                edittext_H.setError(getText(R.string.Error_H_empty));
                checkResult = false;
            }
        }
        return checkResult;
    }

    protected void forceRippleAnimation(View view)
    {
        if(Build.VERSION.SDK_INT >= 23)
        {
            view.setClickable(true);
            Drawable background = view.getForeground();
            final RippleDrawable rippleDrawable = (RippleDrawable) background;

            rippleDrawable.setState(new int[]{android.R.attr.state_pressed, android.R.attr.state_enabled});

            view.setClickable(false);
            rippleDrawable.setState(new int[]{});
        }
    }
}
