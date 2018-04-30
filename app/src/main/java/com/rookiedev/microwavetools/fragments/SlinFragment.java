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
import com.rookiedev.microwavetools.libs.Constants;
import com.rookiedev.microwavetools.libs.SlinCalculator;
import com.rookiedev.microwavetools.libs.SlinModel;

import java.math.BigDecimal;

public class SlinFragment extends Fragment {
    private Context mContext;
    private View rootView, width_input, height_input, er_input;
    private CardView electricalCard, physicalCard;
    private int DecimalLength; // the length of the Decimal, accurate of the result
    private SpannableString error_er, error_Z0;
    private TextView text_er, text_Z0, text_Eeff, text_W, text_H; // strings which include the subscript
    private EditText edittext_W, // the width
            edittext_L, // the length
            edittext_Z0, // the impedance
            edittext_Eeff, // the electrical length
            edittext_Freq, // the frequency
            edittext_T, // the thickness of the metal
            edittext_H, // the thickness of the dielectric
            edittext_er; // the relative dielectric constant
    private Button button_syn, // button synthesize
            button_ana;// button analyze
    private Spinner spinner_W, spinner_L, spinner_T, spinner_H, spinner_Z0, spinner_Eeff, spinner_Freq;// the units of each parameter
    private int flag;
    private RadioButton radioBtn_W, radioBtn_H, radioBtn_L;
    private SlinModel line;
    private ColorStateList defaultTextColor, defaultEdittextColor;
    private AdFragment adFragment=null;

    private boolean isAdFree;

    public SlinFragment() {
        // Empty constructor required for fragment subclasses
    }

    public static SlinFragment newInstance(boolean param) {
        SlinFragment fragment = new SlinFragment();
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
        rootView = inflater.inflate(R.layout.fragment_slin, container, false);
        mContext = this.getContext();
        initUI();
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
                    line.setMetalWidth(Double.parseDouble(edittext_W.getText().toString()),
                            spinner_W.getSelectedItemPosition());
                    line.setFrequency(Double.parseDouble(edittext_Freq.getText().toString()),
                            spinner_Freq.getSelectedItemPosition());
                    line.setSubEpsilon(Double.parseDouble(edittext_er.getText().toString()));
                    line.setSubHeight(Double.parseDouble(edittext_H.getText().toString()),
                            spinner_H.getSelectedItemPosition());
                    line.setMetalThick(Double.parseDouble(edittext_T.getText().toString()),
                            spinner_T.getSelectedItemPosition());

                    if (edittext_L.length() != 0) {
                        line.setMetalLength(Double.parseDouble(edittext_L.getText().toString()),
                                spinner_L.getSelectedItemPosition());

                        SlinCalculator slin = new SlinCalculator();
                        line = slin.getAnaResult(line);
                        //Eeff = fragment_slin.getEeff();

                        BigDecimal Eeff_temp = new BigDecimal(line.getElectricalLength()); // cut the decimal of the Eeff
                        double Eeff = Eeff_temp.setScale(DecimalLength, BigDecimal.ROUND_HALF_UP).doubleValue();
                        edittext_Eeff.setText(String.valueOf(Eeff));

                    } else {
                        line.setMetalLength(0, spinner_L.getSelectedItemPosition());
                        SlinCalculator slin = new SlinCalculator();
                        line = slin.getAnaResult(line);
                        edittext_Eeff.setText(""); // if the L input is empty, clear the Eeff
                    }
                    BigDecimal Z0_temp = new BigDecimal(line.getImpedance());
                    double Z0 = Z0_temp.setScale(DecimalLength, BigDecimal.ROUND_HALF_UP).doubleValue();
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
                    } else if (flag == Constants.Synthesize_Height) {
                        edittext_H.setText("");
                    } else if (flag == Constants.Synthesize_Er) {
                        edittext_er.setText("");
                    }
                } else {
                    int temp;
                    line.setImpedance(Double.parseDouble(edittext_Z0.getText().toString()));
                    line.setFrequency(Double.parseDouble(edittext_Freq.getText().toString()),
                            spinner_Freq.getSelectedItemPosition());
                    line.setMetalThick(Double.parseDouble(edittext_T.getText().toString()),
                            spinner_T.getSelectedItemPosition());

                    if (flag == Constants.Synthesize_Width) {
                        line.setSubHeight(Double.parseDouble(edittext_H.getText().toString()),
                                spinner_H.getSelectedItemPosition());
                        line.setSubEpsilon(Double.parseDouble(edittext_er.getText().toString()));
                        line.setMetalWidth(0, Constants.LengthUnit_m);
                    } else if (flag == Constants.Synthesize_Height) {
                        line.setMetalWidth(Double.parseDouble(edittext_W.getText().toString()),
                                spinner_W.getSelectedItemPosition());
                        line.setSubEpsilon(Double.parseDouble(edittext_er.getText().toString()));
                        line.setSubHeight(0, Constants.LengthUnit_m);
                    } else if (flag == Constants.Synthesize_Er) {
                        line.setMetalWidth(Double.parseDouble(edittext_W.getText().toString()),
                                spinner_W.getSelectedItemPosition());
                        line.setSubHeight(Double.parseDouble(edittext_H.getText().toString()),
                                spinner_H.getSelectedItemPosition());
                        line.setSubEpsilon(0);
                    }

                    if (edittext_Eeff.length() != 0) { // check if the Eeff is empty
                        line.setElectricalLength(Double.parseDouble(edittext_Eeff.getText().toString()));
                        //Eeff = Double.parseDouble(edittext_Eeff.getText().toString());
                        //SlinCalculator fragment_slin = new SlinCalculator(W, H, er, L, Z0, Eeff, Freq, T, flag);
                        SlinCalculator slin = new SlinCalculator();
                        line = slin.getSynResult(line, flag);

                        BigDecimal L_temp = new BigDecimal(
                                Constants.meter2others(line.getMetalLength(), spinner_L.getSelectedItemPosition())); // cut the decimal of L
                        double L = L_temp.setScale(DecimalLength, BigDecimal.ROUND_HALF_UP).doubleValue();
                        edittext_L.setText(String.valueOf(L));
                    } else {
                        SlinCalculator slin = new SlinCalculator();
                        line = slin.getSynResult(line, flag);
                        edittext_L.setText(""); // clear the L if the Eeff input is empty
                    }
                    if (flag == Constants.Synthesize_Width) {

                        BigDecimal W_temp = new BigDecimal(
                                Constants.meter2others(line.getMetalWidth(), spinner_W.getSelectedItemPosition())); // cut the decimal of W
                        double W = W_temp.setScale(DecimalLength, BigDecimal.ROUND_HALF_UP).doubleValue();
                        edittext_W.setText(String.valueOf(W));
                    } else if (flag == Constants.Synthesize_Height) {
                        BigDecimal H_temp = new BigDecimal(
                                Constants.meter2others(line.getSubHeight(), spinner_H.getSelectedItemPosition()));
                        double H = H_temp.setScale(DecimalLength, BigDecimal.ROUND_HALF_UP).doubleValue();
                        edittext_H.setText(String.valueOf(H));
                    } else if (flag == Constants.Synthesize_Er) {
                        BigDecimal er_temp = new BigDecimal(line.getSubEpsilon());
                        double er = er_temp.setScale(DecimalLength, BigDecimal.ROUND_HALF_UP).doubleValue();
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
        line = new SlinModel();

        RadioButton radioButtonPhs = (RadioButton) rootView.findViewById(R.id.radioBtn_Phs);
        radioButtonPhs.setVisibility(View.VISIBLE);
        radioButtonPhs.setChecked(true);

        RadioButton radioButtonZ0 = (RadioButton) rootView.findViewById(R.id.radioBtn_Z0);
        radioButtonZ0.setVisibility(View.VISIBLE);
        radioButtonZ0.setChecked(true);

        TextView textViewL = (TextView) rootView.findViewById(R.id.text_L);
        textViewL.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));

        text_W = (TextView) rootView.findViewById(R.id.text_W);
        defaultTextColor = text_W.getTextColors();
        text_H = (TextView) rootView.findViewById(R.id.text_H);

        physicalCard = (CardView) rootView.findViewById(R.id.physicalParaCard);
        electricalCard = (CardView) rootView.findViewById(R.id.electricalParaCard);

        error_er = new SpannableString(this.getString(R.string.Error_er_empty));
        error_Z0 = new SpannableString(this.getString(R.string.Error_Z0_empty));

        // Subscript strings
        text_er = (TextView) rootView.findViewById(R.id.text_er);
        text_Z0 = (TextView) rootView.findViewById(R.id.text_Z0);
        text_Z0.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor));
        text_Eeff = (TextView) rootView.findViewById(R.id.text_Phs);
        text_Eeff.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor));

        SpannableString spanEr = new SpannableString(this.getString(R.string.text_er));
        spanEr.setSpan(new SubscriptSpan(), 1, 2, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        text_er.append(spanEr);

        SpannableString spanZ0 = new SpannableString(this.getString(R.string.text_Z0));
        spanZ0.setSpan(new SubscriptSpan(), 1, 2, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        text_Z0.append(spanZ0);

        // edittext elements
        edittext_W = (EditText) rootView.findViewById(R.id.editText_W);
        defaultEdittextColor = edittext_W.getTextColors();
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
        spinner_L = (Spinner) rootView.findViewById(R.id.spinner_L);
        spinner_Z0 = (Spinner) rootView.findViewById(R.id.spinner_Z0);
        spinner_Eeff = (Spinner) rootView.findViewById(R.id.spinner_Phs);
        spinner_Freq = (Spinner) rootView.findViewById(R.id.spinner_Freq);
        spinner_T = (Spinner) rootView.findViewById(R.id.spinner_T);
        spinner_H = (Spinner) rootView.findViewById(R.id.spinner_H);

        // configure the length units
        ArrayAdapter<CharSequence> adapterLength = ArrayAdapter.createFromResource(this.getActivity(),
                R.array.list_units_Length, android.R.layout.simple_spinner_item);
        adapterLength.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_W.setAdapter(adapterLength);
        spinner_L.setAdapter(adapterLength);
        spinner_T.setAdapter(adapterLength);
        spinner_H.setAdapter(adapterLength);

        // configure the impedance units
        ArrayAdapter<CharSequence> adapterImpedance = ArrayAdapter.createFromResource(this.getActivity(),
                R.array.list_units_Impedance, android.R.layout.simple_spinner_item);
        adapterImpedance.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_Z0.setAdapter(adapterImpedance);

        // configure the electrical length units
        ArrayAdapter<CharSequence> adapterEleLength = ArrayAdapter.createFromResource(this.getActivity(),
                R.array.list_units_Ele_length, android.R.layout.simple_spinner_item);
        adapterEleLength.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_Eeff.setAdapter(adapterEleLength);

        // configure the frequency units
        ArrayAdapter<CharSequence> adapterFreq = ArrayAdapter.createFromResource(this.getActivity(),
                R.array.list_units_Frequency, android.R.layout.simple_spinner_item);
        adapterFreq.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_Freq.setAdapter(adapterFreq);
        radioBtn_W = (RadioButton) rootView.findViewById(R.id.radioBtn_W);
        radioBtn_W.setVisibility(View.VISIBLE);
        radioBtn_H = (RadioButton) rootView.findViewById(R.id.radioBtn_H);
        radioBtn_H.setVisibility(View.VISIBLE);
        radioBtn_L = (RadioButton) rootView.findViewById(R.id.radioBtn_L);
        radioBtn_L.setVisibility(View.VISIBLE);
        radioBtn_L.setChecked(true);
    }

    private void readSharedPref() {
        SharedPreferences prefs = getActivity().getSharedPreferences(Constants.SHARED_PREFS_NAME,
                AppCompatActivity.MODE_PRIVATE);// get the header_parameters from the Shared
        // Preferences in the device

        // read values from the shared preferences

        // SlinCalculator header_parameters
        edittext_W.setText(prefs.getString(Constants.SLIN_W, "10.00"));
        spinner_W.setSelection(Integer.parseInt(prefs.getString(Constants.SLIN_W_UNIT, "0")));

        edittext_L.setText(prefs.getString(Constants.SLIN_L, "1000.00"));
        spinner_L.setSelection(Integer.parseInt(prefs.getString(Constants.SLIN_L_UNIT, "0")));

        edittext_Z0.setText(prefs.getString(Constants.SLIN_Z0, "49.76"));
        spinner_Z0.setSelection(Integer.parseInt(prefs.getString(Constants.SLIN_Z0_UNIT, "0")));

        edittext_Eeff.setText(prefs.getString(Constants.SLIN_PHS, "61.00"));
        spinner_Eeff.setSelection(Integer.parseInt(prefs.getString(Constants.SLIN_PHS_UNIT, "0")));

        edittext_Freq.setText(prefs.getString(Constants.SLIN_FREQ, "1.00"));
        spinner_Freq.setSelection(Integer.parseInt(prefs.getString(Constants.SLIN_FREQ_UNIT, "1")));

        edittext_er.setText(prefs.getString(Constants.SLIN_ER, "4.00"));

        edittext_H.setText(prefs.getString(Constants.SLIN_H, "25.00"));
        spinner_H.setSelection(Integer.parseInt(prefs.getString(Constants.SLIN_H_UNIT, "0")));

        edittext_T.setText(prefs.getString(Constants.SLIN_T, "1.40"));
        spinner_T.setSelection(Integer.parseInt(prefs.getString(Constants.SLIN_T_UNIT, "0")));
        flag = Integer.parseInt(prefs.getString(Constants.SLIN_TARGET, "0"));
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
            radioBtn_H.setChecked(false);
            text_H.setTextColor(defaultTextColor);
            edittext_H.setTextColor(defaultEdittextColor);
            //parameter_width.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.green_shadow));
            //height_input.setBackgroundColor(Color.WHITE);
            //er_input.setBackgroundColor(Color.WHITE);
        } else {
            flag = Constants.Synthesize_Height;
            radioBtn_W.setChecked(false);
            text_W.setTextColor(defaultTextColor);
            edittext_W.setTextColor(defaultEdittextColor);
            radioBtn_H.setChecked(true);
            text_H.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
            edittext_H.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
            //parameter_width.setBackgroundColor(Color.WHITE);
            //height_input.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.green_shadow));
            //er_input.setBackgroundColor(Color.WHITE);
        }
        radioBtn_W.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                radioBtn_W.setChecked(true);
                text_W.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
                edittext_W.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
                radioBtn_H.setChecked(false);
                text_H.setTextColor(defaultTextColor);
                edittext_H.setTextColor(defaultEdittextColor);
                //parameter_width.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.green_shadow));
                //height_input.setBackgroundColor(Color.WHITE);
                //er_input.setBackgroundColor(Color.WHITE);
                flag = Constants.Synthesize_Width;
            }
        });
        radioBtn_H.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                radioBtn_W.setChecked(false);
                text_W.setTextColor(defaultTextColor);
                edittext_W.setTextColor(defaultEdittextColor);
                radioBtn_H.setChecked(true);
                text_H.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
                edittext_H.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor));
                //parameter_width.setBackgroundColor(Color.WHITE);
                //height_input.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.green_shadow));
                //er_input.setBackgroundColor(Color.WHITE);
                flag = Constants.Synthesize_Height;
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        String slin_W, slin_H, slin_er, slin_L, slin_Z0, slin_Eeff, slin_Freq, slin_T;
        String slin_W_unit, slin_H_unit, slin_L_unit, slin_Z0_unit, slin_Eeff_unit, slin_Freq_unit, slin_T_unit;
        String slin_flag;

        SharedPreferences prefs = getActivity().getSharedPreferences(Constants.SHARED_PREFS_NAME,
                AppCompatActivity.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        slin_W = edittext_W.getText().toString();
        slin_W_unit = Integer.toString(spinner_W.getSelectedItemPosition());
        slin_H = edittext_H.getText().toString();
        slin_H_unit = Integer.toString(spinner_H.getSelectedItemPosition());
        slin_er = edittext_er.getText().toString();
        slin_L = edittext_L.getText().toString();
        slin_L_unit = Integer.toString(spinner_L.getSelectedItemPosition());
        slin_Z0 = edittext_Z0.getText().toString();
        slin_Z0_unit = Integer.toString(spinner_Z0.getSelectedItemPosition());
        slin_Eeff = edittext_Eeff.getText().toString();
        slin_Eeff_unit = Integer.toString(spinner_Eeff.getSelectedItemPosition());
        slin_Freq = edittext_Freq.getText().toString();
        slin_Freq_unit = Integer.toString(spinner_Freq.getSelectedItemPosition());
        slin_T = edittext_T.getText().toString();
        slin_T_unit = Integer.toString(spinner_T.getSelectedItemPosition());
        slin_flag = Integer.toString(flag);

        editor.putString(Constants.SLIN_W, slin_W);
        editor.putString(Constants.SLIN_W_UNIT, slin_W_unit);
        editor.putString(Constants.SLIN_H, slin_H);
        editor.putString(Constants.SLIN_H_UNIT, slin_H_unit);
        editor.putString(Constants.SLIN_ER, slin_er);
        editor.putString(Constants.SLIN_L, slin_L);
        editor.putString(Constants.SLIN_L_UNIT, slin_L_unit);
        editor.putString(Constants.SLIN_Z0, slin_Z0);
        editor.putString(Constants.SLIN_Z0_UNIT, slin_Z0_unit);
        editor.putString(Constants.SLIN_PHS, slin_Eeff);
        editor.putString(Constants.SLIN_PHS_UNIT, slin_Eeff_unit);
        editor.putString(Constants.SLIN_FREQ, slin_Freq);
        editor.putString(Constants.SLIN_FREQ_UNIT, slin_Freq_unit);
        editor.putString(Constants.SLIN_T, slin_T);
        editor.putString(Constants.SLIN_T_UNIT, slin_T_unit);
        editor.putString(Constants.SLIN_TARGET, slin_flag);
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
            error_er.setSpan(new SubscriptSpan(), 13, 14, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            edittext_er.setError(error_er);
            checkResult = false;
        }
        return checkResult;
    }

    private boolean synthesizeInputCheck() {
        boolean checkResult = true;
        if (edittext_Z0.length() == 0) {
            error_Z0.setSpan(new SubscriptSpan(), 13, 14, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
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
                edittext_H.setError(getText(R.string.Error_H_empty));
                checkResult = false;
            }
            if (edittext_er.length() == 0) {
                error_er.setSpan(new SubscriptSpan(), 13, 14, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                edittext_er.setError(error_er);
                checkResult = false;
            }
        } else if (flag == 1) {
            if (edittext_W.length() == 0) {
                edittext_W.setError(getText(R.string.Error_W_empty));
                checkResult = false;
            }
            if (edittext_er.length() == 0) {
                error_er.setSpan(new SubscriptSpan(), 13, 14, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                edittext_er.setError(error_er);
                checkResult = false;
            }
        } else if (flag == 2) {
            if (edittext_W.length() == 0) {
                edittext_W.setError(getText(R.string.Error_W_empty));
                checkResult = false;
            }
            if (edittext_H.length() == 0) {
                edittext_H.setError(getText(R.string.Error_H_empty));
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
        if(fragmentManager !=null&& rootView != null) {
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
