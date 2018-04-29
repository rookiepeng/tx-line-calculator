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
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.SubscriptSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.rookiedev.microwavetools.R;
import com.rookiedev.microwavetools.libs.Constants;
import com.rookiedev.microwavetools.libs.CpwCalculator;
import com.rookiedev.microwavetools.libs.CpwModel;

import java.math.BigDecimal;

public class CpwFragment extends Fragment {
    private Context mContext;
    private View rootView, width_input, space_input, height_input, er_input;
    private CardView electricalCard, physicalCard;
    private int DecimalLength; // the length of the Decimal, accurate of the result
    private SpannableString error_er, error_Z0;
    private TextView text_H, text_W, text_S, text_er, text_Z0, text_Eeff; // strings which include the subscript
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
    private RadioButton radioBtn_W, radioBtn_S, radioBtn_H;
    private boolean withGround;
    private CpwModel line;
    private ColorStateList defaultTextColor, defaultEdittextColor;
    public static final String CPW_TYPE_PARAM = "TYPE";
    private AdFragment adFragment=null;

    private boolean isAdFree;

    public CpwFragment() {
        // Empty constructor required for fragment subclasses
    }

    public static CpwFragment newInstance(String param, boolean param2) {
        CpwFragment fragment = new CpwFragment();
        Bundle args = new Bundle();
        args.putString(CPW_TYPE_PARAM, param);
        args.putBoolean(Constants.IS_AD_FREE, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            withGround = getArguments().getString(CPW_TYPE_PARAM) != "CPW";
            isAdFree=getArguments().getBoolean(Constants.IS_AD_FREE,true);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_cpw, container, false);
        mContext=this.getContext();
        initUI(); // initial the UI
        readSharedPref(); // read shared preferences
        setRadioBtn();
        //setImage();

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

                        CpwCalculator cpwg = new CpwCalculator();
                        line = cpwg.getAnaResult(line, withGround);

                        BigDecimal Eeff_temp = new BigDecimal(line.getElectricalLength()); // cut the decimal of the Eeff
                        double Eeff = Eeff_temp.setScale(DecimalLength,
                                BigDecimal.ROUND_HALF_UP).doubleValue();
                        edittext_Eeff.setText(String.valueOf(Eeff));
                    } else {
                        CpwCalculator cpwg = new CpwCalculator();
                        line = cpwg.getAnaResult(line, withGround);
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
                    if (flag == Constants.Synthesize_Width) {
                        edittext_W.setText("");
                    } else if (flag == Constants.Synthesize_Gap) {
                        edittext_S.setText("");
                    } else if (flag == Constants.Synthesize_Height) {
                        edittext_H.setText("");
                    } else if (flag == Constants.Synthesize_Er) {
                        edittext_er.setText("");
                    }
                } else {
                    int temp;
                    line.setImpedance(Double.parseDouble(edittext_Z0.getText().toString()));
                    line.setFrequency(Double.parseDouble(edittext_Freq.getText().toString()), spinner_Freq.getSelectedItemPosition());
                    line.setMetalThick(Double.parseDouble(edittext_T.getText().toString()), spinner_T.getSelectedItemPosition());
                    //Z0 = Double.parseDouble(edittext_Z0.getText().toString()); // get the header_parameters

                    if (flag == Constants.Synthesize_Width) {
                        line.setSubHeight(Double.parseDouble(edittext_H.getText().toString()), spinner_H.getSelectedItemPosition());
                        line.setMetalSpace(Double.parseDouble(edittext_S.getText().toString()), spinner_S.getSelectedItemPosition());
                        line.setSubEpsilon(Double.parseDouble(edittext_er.getText().toString()));

                        //W = 0;
                    } else if (flag == Constants.Synthesize_Gap) {
                        line.setSubHeight(Double.parseDouble(edittext_H.getText().toString()), spinner_H.getSelectedItemPosition());
                        line.setMetalWidth(Double.parseDouble(edittext_W.getText().toString()), spinner_W.getSelectedItemPosition());
                        line.setSubEpsilon(Double.parseDouble(edittext_er.getText().toString()));

                        //S = 0;
                    } else if (flag == Constants.Synthesize_Height) {
                        line.setMetalSpace(Double.parseDouble(edittext_S.getText().toString()), spinner_S.getSelectedItemPosition());
                        line.setMetalWidth(Double.parseDouble(edittext_W.getText().toString()), spinner_W.getSelectedItemPosition());
                        line.setSubEpsilon(Double.parseDouble(edittext_er.getText().toString()));

                        //H = 0;
                    } else if (flag == Constants.Synthesize_Er) {
                        line.setMetalSpace(Double.parseDouble(edittext_S.getText().toString()), spinner_S.getSelectedItemPosition());
                        line.setMetalWidth(Double.parseDouble(edittext_W.getText().toString()), spinner_W.getSelectedItemPosition());
                        line.setSubHeight(Double.parseDouble(edittext_H.getText().toString()), spinner_H.getSelectedItemPosition());

                        //er = 0;
                    }

                    if (edittext_Eeff.length() != 0) { // check if the Eeff is empty
                        line.setElectricalLength(Double.parseDouble(edittext_Eeff.getText().toString()));
                        CpwCalculator cpwg = new CpwCalculator();
                        line = cpwg.getSynResult(line, flag, withGround);
                        BigDecimal L_temp = new BigDecimal(Constants.meter2others(line.getMetalLength(), spinner_L.getSelectedItemPosition())); // cut the
                        // decimal of L
                        double L = L_temp.setScale(DecimalLength, BigDecimal.ROUND_HALF_UP)
                                .doubleValue();
                        edittext_L.setText(String.valueOf(L));
                    } else {
                        CpwCalculator cpwg = new CpwCalculator();
                        line = cpwg.getSynResult(line, flag, withGround);
                        edittext_L.setText(""); // clear the L if the Eeff input is empty
                    }
                    if (flag == Constants.Synthesize_Width) {
                        BigDecimal W_temp = new BigDecimal(Constants.meter2others(line.getMetalWidth(), spinner_W.getSelectedItemPosition())); // cut the decimal of W
                        double W = W_temp.setScale(DecimalLength, BigDecimal.ROUND_HALF_UP)
                                .doubleValue();
                        edittext_W.setText(String.valueOf(W));
                    } else if (flag == Constants.Synthesize_Gap) {
                        BigDecimal S_temp = new BigDecimal(Constants.meter2others(line.getMetalSpace(), spinner_S.getSelectedItemPosition())); // cut the decimal of S
                        double S = S_temp.setScale(DecimalLength, BigDecimal.ROUND_HALF_UP)
                                .doubleValue();
                        edittext_S.setText(String.valueOf(S));
                    } else if (flag == Constants.Synthesize_Height) {
                        BigDecimal H_temp = new BigDecimal(Constants.meter2others(line.getSubHeight(), spinner_H.getSelectedItemPosition()));
                        double H = H_temp.setScale(DecimalLength, BigDecimal.ROUND_HALF_UP)
                                .doubleValue();
                        edittext_H.setText(String.valueOf(H));
                    } else if (flag == Constants.Synthesize_Er) {
                        BigDecimal er_temp = new BigDecimal(line.getSubEpsilon());
                        double er = er_temp.setScale(DecimalLength, BigDecimal.ROUND_HALF_UP)
                                .doubleValue();
                        edittext_er.setText(String.valueOf(er));
                    }
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
        return rootView;
    }

    private void initUI() {
        line = new CpwModel();
        RadioButton radioButtonL = (RadioButton) rootView.findViewById(R.id.radioBtn_L);
        radioButtonL.setVisibility(View.VISIBLE);
        radioButtonL.setChecked(true);

        RadioButton radioButtonPhs = (RadioButton) rootView.findViewById(R.id.radioBtn_Phs);
        radioButtonPhs.setVisibility(View.VISIBLE);
        radioButtonPhs.setChecked(true);

        RadioButton radioButtonZ0 = (RadioButton) rootView.findViewById(R.id.radioBtn_Z0);
        radioButtonZ0.setVisibility(View.VISIBLE);
        radioButtonZ0.setChecked(true);

        physicalCard=(CardView) rootView.findViewById(R.id.physicalParaCard);
        electricalCard = (CardView) rootView.findViewById(R.id.electricalParaCard);

        error_er = new SpannableString(this.getString(R.string.Error_er_empty));
        error_Z0 = new SpannableString(this.getString(R.string.Error_Z0_empty));
        /** find the elements */

        // Subscript strings
        text_er = (TextView) rootView.findViewById(R.id.text_er);
        defaultTextColor = text_er.getTextColors();
        text_Z0 = (TextView) rootView.findViewById(R.id.text_Z0);
        text_Z0.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor));
        text_Eeff = (TextView) rootView.findViewById(R.id.text_Phs);
        text_Eeff.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor));

        text_W = (TextView) rootView.findViewById(R.id.text_W);
        text_S = (TextView) rootView.findViewById(R.id.text_S);
        text_H = (TextView) rootView.findViewById(R.id.text_H);
        TextView text_L = (TextView) rootView.findViewById(R.id.text_L);
        text_L.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));

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

        //SpannableString spanEeff = new SpannableString(
        //        this.getString(R.string.text_Eeff));
        //spanEeff.setSpan(new SubscriptSpan(), 1, 4,
        //        Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        //text_Eeff.append(spanEeff);

        // edittext elements
        edittext_W = (EditText) rootView.findViewById(R.id.editText_W);
        defaultEdittextColor = edittext_W.getTextColors();
        edittext_S = (EditText) rootView.findViewById(R.id.editText_S);
        edittext_L = (EditText) rootView.findViewById(R.id.editText_L);
        edittext_L.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
        edittext_Z0 = (EditText) rootView.findViewById(R.id.editText_Z0);
        edittext_Z0.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor));
        edittext_Eeff = (EditText) rootView.findViewById(R.id.editText_Phs);
        edittext_Eeff.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor));
        edittext_Freq = (EditText) rootView.findViewById(R.id.editText_Freq);
        edittext_T = (EditText) rootView.findViewById(R.id.editText_T);
        edittext_H = (EditText) rootView.findViewById(R.id.editText_H);
        edittext_er = (EditText) rootView.findViewById(R.id.editText_er);

        // button elements
        button_ana = (Button) rootView.findViewById(R.id.button_ana);
        button_syn = (Button) rootView.findViewById(R.id.button_syn);

        // spinner elements
        spinner_W = (Spinner) rootView.findViewById(R.id.spinner_W);
        spinner_S = (Spinner) rootView.findViewById(R.id.spinner_S);
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
        radioBtn_W.setVisibility(View.VISIBLE);
        radioBtn_S = (RadioButton) rootView.findViewById(R.id.radioBtn_S);
        radioBtn_S.setVisibility(View.VISIBLE);
        radioBtn_H = (RadioButton) rootView.findViewById(R.id.radioBtn_H);
        radioBtn_H.setVisibility(View.VISIBLE);

        //withGround = (CheckBox) rootView.findViewById(R.id.checkBoxGround);
        //CPW_G = (ImageView) rootView.findViewById(R.id.cpw_G);
    }

    private void readSharedPref() {
        SharedPreferences prefs = getActivity().getSharedPreferences(Constants.SHARED_PREFS_NAME,
                AppCompatActivity.MODE_PRIVATE);// get the header_parameters from the Shared
        // Preferences in the device

        // read values from the shared preferences

        // fragment_cpw header_parameters
        edittext_W.setText(prefs.getString(Constants.CPW_W, "18.00"));
        spinner_W.setSelection(Integer.parseInt(prefs.getString(Constants.CPW_W_UNIT,
                "0")));

        edittext_S.setText(prefs.getString(Constants.CPW_S, "20.00"));
        spinner_S.setSelection(Integer.parseInt(prefs
                .getString(Constants.CPW_S_UNIT, "0")));

        edittext_L.setText(prefs.getString(Constants.CPW_L, "1000.0"));
        spinner_L.setSelection(Integer.parseInt(prefs.getString(Constants.CPW_L_UNIT,
                "0")));

        edittext_Z0.setText(prefs.getString(Constants.CPW_Z0, "100.00"));
        spinner_Z0.setSelection(Integer.parseInt(prefs.getString(Constants.CPW_Z0_UNIT,
                "0")));

        edittext_Eeff.setText(prefs.getString(Constants.CPW_PHS, "41.75"));
        spinner_Eeff.setSelection(Integer.parseInt(prefs.getString(
                Constants.CPW_PHS_UNIT, "0")));

        edittext_Freq.setText(prefs.getString(Constants.CPW_FREQ, "1.00"));
        spinner_Freq.setSelection(Integer.parseInt(prefs.getString(
                Constants.CPW_FREQ_UNIT, "1")));

        edittext_er.setText(prefs.getString(Constants.CPW_ER, "4.00"));

        edittext_H.setText(prefs.getString(Constants.CPW_H, "10.00"));
        spinner_H.setSelection(Integer.parseInt(prefs.getString(Constants.CPW_H_UNIT,
                "0")));

        edittext_T.setText(prefs.getString(Constants.CPW_T, "1.40"));
        spinner_T.setSelection(Integer.parseInt(prefs.getString(Constants.CPW_T_UNIT,
                "0")));
        flag = Integer.parseInt(prefs.getString(Constants.CPW_FLAG, "0"));

    }

    private void Preference_SharedPref() {
        SharedPreferences prefs = getActivity().getSharedPreferences(Constants.SHARED_PREFS_NAME,
                AppCompatActivity.MODE_PRIVATE);// get the header_parameters from the Shared
        // Preferences in the device
        // universal header_parameters
        DecimalLength = Integer.parseInt(prefs.getString("DecimalLength", "2"));
    }

    private void setRadioBtn() {
        if (flag == Constants.Synthesize_Width) {
            radioBtn_W.setChecked(true);
            text_W.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
            edittext_W.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
            radioBtn_S.setChecked(false);
            text_S.setTextColor(defaultTextColor);
            edittext_S.setTextColor(defaultEdittextColor);
            radioBtn_H.setChecked(false);
            text_H.setTextColor(defaultTextColor);
            edittext_H.setTextColor(defaultEdittextColor);
        } else if (flag == Constants.Synthesize_Gap) {
            radioBtn_W.setChecked(false);
            text_W.setTextColor(defaultTextColor);
            edittext_W.setTextColor(defaultEdittextColor);
            radioBtn_S.setChecked(true);
            text_S.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
            edittext_S.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
            radioBtn_H.setChecked(false);
            text_H.setTextColor(defaultTextColor);
            edittext_H.setTextColor(defaultEdittextColor);
            //radioBtn_er.setChecked(false);
            //parameter_width.setBackgroundColor(Color.WHITE);
            //parameter_space.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.green_shadow));
            //height_input.setBackgroundColor(Color.WHITE);
            //er_input.setBackgroundColor(Color.WHITE);
        } else if (flag == Constants.Synthesize_Height) {
            radioBtn_W.setChecked(false);
            text_W.setTextColor(defaultTextColor);
            edittext_W.setTextColor(defaultEdittextColor);
            radioBtn_S.setChecked(false);
            text_S.setTextColor(defaultTextColor);
            edittext_S.setTextColor(defaultEdittextColor);
            radioBtn_H.setChecked(true);
            text_H.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
            edittext_H.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
            //radioBtn_er.setChecked(false);
            //parameter_width.setBackgroundColor(Color.WHITE);
            //parameter_space.setBackgroundColor(Color.WHITE);
            //height_input.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.green_shadow));
            //er_input.setBackgroundColor(Color.WHITE);
        }
        radioBtn_W.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                radioBtn_W.setChecked(true);
                text_W.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
                edittext_W.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
                radioBtn_S.setChecked(false);
                text_S.setTextColor(defaultTextColor);
                edittext_S.setTextColor(defaultEdittextColor);
                radioBtn_H.setChecked(false);
                text_H.setTextColor(defaultTextColor);
                edittext_H.setTextColor(defaultEdittextColor);
                ///radioBtn_er.setChecked(false);
                //parameter_width.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.green_shadow));
                //parameter_space.setBackgroundColor(Color.WHITE);
                //height_input.setBackgroundColor(Color.WHITE);
                //er_input.setBackgroundColor(Color.WHITE);
                flag = Constants.Synthesize_Width;
            }
        });
        radioBtn_S.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                radioBtn_W.setChecked(false);
                text_W.setTextColor(defaultTextColor);
                edittext_W.setTextColor(defaultEdittextColor);
                radioBtn_S.setChecked(true);
                text_S.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
                edittext_S.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
                radioBtn_H.setChecked(false);
                text_H.setTextColor(defaultTextColor);
                edittext_H.setTextColor(defaultEdittextColor);
                //radioBtn_er.setChecked(false);
                //parameter_width.setBackgroundColor(Color.WHITE);
                //parameter_space.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.green_shadow));
                //height_input.setBackgroundColor(Color.WHITE);
                //er_input.setBackgroundColor(Color.WHITE);
                flag = Constants.Synthesize_Gap;
            }
        });
        radioBtn_H.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                radioBtn_W.setChecked(false);
                text_W.setTextColor(defaultTextColor);
                edittext_W.setTextColor(defaultEdittextColor);
                radioBtn_S.setChecked(false);
                text_S.setTextColor(defaultTextColor);
                edittext_S.setTextColor(defaultEdittextColor);
                radioBtn_H.setChecked(true);
                text_H.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
                edittext_H.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
                //radioBtn_er.setChecked(false);
                //parameter_width.setBackgroundColor(Color.WHITE);
                //parameter_space.setBackgroundColor(Color.WHITE);
                //height_input.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.green_shadow));
                //er_input.setBackgroundColor(Color.WHITE);
                flag = Constants.Synthesize_Height;
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

        SharedPreferences prefs = getActivity().getSharedPreferences(Constants.SHARED_PREFS_NAME,
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

        editor.putString(Constants.CPW_W, cpw_W);
        editor.putString(Constants.CPW_W_UNIT, cpw_W_unit);
        editor.putString(Constants.CPW_S, cpw_S);
        editor.putString(Constants.CPW_S_UNIT, cpw_S_unit);
        editor.putString(Constants.CPW_H, cpw_H);
        editor.putString(Constants.CPW_H_UNIT, cpw_H_unit);
        editor.putString(Constants.CPW_ER, cpw_er);
        editor.putString(Constants.CPW_L, cpw_L);
        editor.putString(Constants.CPW_L_UNIT, cpw_L_unit);
        editor.putString(Constants.CPW_Z0, cpw_Z0);
        editor.putString(Constants.CPW_Z0_UNIT, cpw_Z0_unit);
        editor.putString(Constants.CPW_PHS, cpw_Eeff);
        editor.putString(Constants.CPW_PHS_UNIT, cpw_Eeff_unit);
        editor.putString(Constants.CPW_FREQ, cpw_Freq);
        editor.putString(Constants.CPW_FREQ_UNIT, cpw_Freq_unit);
        editor.putString(Constants.CPW_T, cpw_T);
        editor.putString(Constants.CPW_T_UNIT, cpw_T_unit);
        editor.putString(Constants.CPW_FLAG, cpw_flag);

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

    public void addAdFragment(){
        AdFragment adFragment = new AdFragment();
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
