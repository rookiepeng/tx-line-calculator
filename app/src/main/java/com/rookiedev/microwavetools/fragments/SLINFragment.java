package com.rookiedev.microwavetools.fragments;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBarActivity;
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

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.rookiedev.microwavetools.MainActivity;
import com.rookiedev.microwavetools.R;
import com.rookiedev.microwavetools.libs.SLIN;

import java.math.BigDecimal;

public class SLINFragment extends Fragment {
    public final static String SLIN_W = "SLIN_W";
    public final static String SLIN_W_UNIT = "SLIN_W_UNIT";
    public final static String SLIN_L = "SLIN_L";
    public final static String SLIN_L_UNIT = "SLIN_L_UNIT";
    public final static String SLIN_Z0 = "SLIN_Z0";
    public final static String SLIN_Z0_UNIT = "SLIN_Z0_UNIT";
    public final static String SLIN_Eeff = "SLIN_Eeff";
    public final static String SLIN_Eeff_UNIT = "SLIN_Eeff_UNIT";
    public final static String SLIN_Freq = "SLIN_Freq";
    public final static String SLIN_Freq_UNIT = "SLIN_Freq_UNIT";
    public final static String SLIN_er = "SLIN_er";
    public final static String SLIN_H = "SLIN_H";
    public final static String SLIN_H_UNIT = "SLIN_H_UNIT";
    public final static String SLIN_T = "SLIN_T";
    public final static String SLIN_T_UNIT = "SLIN_T_UNIT";
    public final static String SLIN_FLAG = "SLIN_FLAG";
    private View rootView, width_input, height_input, er_input;
    private CardView electricalCard, physicalCard;
    private int DecimalLength; // the length of the Decimal, accurate of the result
    private SpannableString error_er, error_Z0;
    private TextView text_er, text_Z0, text_Eeff; // strings which include the subscript
    private EditText edittext_W, // the width
            edittext_L, // the length
            edittext_Z0, // the impedance
            edittext_Eeff, // the electrical length
            edittext_Freq, // the frequency
            edittext_T, // the thickness of the metal
            edittext_H, // the thickness of the dielectric
            edittext_er; // the relative dielectric constant
    private double W, L, Z0, Eeff, Freq, T, H, er;
    private Button button_syn,// button synthesize
            button_ana;// button analyze
    private Spinner spinner_W, spinner_L, spinner_T, spinner_H, spinner_Z0,
            spinner_Eeff, spinner_Freq;// the units of each parameter
    private int flag;
    private RadioButton radioBtn_W, radioBtn_H, radioBtn_er;

    public SLINFragment() {
        // Empty constructor required for fragment subclasses
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.slin, container, false);
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
                    W = Double.parseDouble(edittext_W.getText().toString()); // get the parameters
                    Freq = Double.parseDouble(edittext_Freq.getText().toString());
                    er = Double.parseDouble(edittext_er.getText().toString());
                    H = Double.parseDouble(edittext_H.getText().toString());
                    T = Double.parseDouble(edittext_T.getText().toString());

                    temp = spinner_W.getSelectedItemPosition(); // convert unit to meter
                    if (temp == 0) {
                        W = W / 39370.0787402;
                    } else if (temp == 1) {
                        W = W / 1000;
                    } else if (temp == 2) {
                        W = W / 100;
                    }

                    temp = spinner_Freq.getSelectedItemPosition(); // convert unit to Hz
                    if (temp == 0) {
                        Freq = Freq * 1e6;
                    } else if (temp == 1) {
                        Freq = Freq * 1e9;
                    }

                    temp = spinner_H.getSelectedItemPosition(); // convert unit to meter
                    if (temp == 0) {
                        H = H / 39370.0787402;
                    } else if (temp == 1) {
                        H = H / 1000;
                    } else if (temp == 2) {
                        H = H / 100;
                    }

                    temp = spinner_T.getSelectedItemPosition(); // convert unit to meter
                    if (temp == 0) {
                        T = T / 39370.0787402;
                    } else if (temp == 1) {
                        T = T / 1000;
                    } else if (temp == 2) {
                        T = T / 100;
                    }

                    if (edittext_L.length() != 0) {
                        L = Double.parseDouble(edittext_L.getText().toString());
                        temp = spinner_L.getSelectedItemPosition(); // convert unit to meter
                        if (temp == 0) {
                            L = L / 39370.0787402;
                        } else if (temp == 1) {
                            L = L / 1000;
                        } else if (temp == 2) {
                            L = L / 100;
                        }

                        SLIN slin = new SLIN(W, H, er, L, 0, 0, Freq, T, 0);
                        Z0 = slin.getZ0();

                        Eeff = slin.getEeff();

                        BigDecimal Eeff_temp = new BigDecimal(Eeff); // cut the decimal of the Eeff
                        Eeff = Eeff_temp.setScale(DecimalLength,
                                BigDecimal.ROUND_HALF_UP).doubleValue();
                        edittext_Eeff.setText(String.valueOf(Eeff));

                    } else {
                        SLIN slin = new SLIN(W, H, er, 0, 0, 0, Freq, T, 0);
                        Z0 = slin.getZ0();
                        edittext_Eeff.setText(""); // if the L input is empty, clear the Eeff
                    }
                    BigDecimal Z0_temp = new BigDecimal(Z0);
                    Z0 = Z0_temp.setScale(DecimalLength,
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
                        edittext_W.setText("");
                    } else if (flag == 1) {
                        edittext_H.setText("");
                    } else if (flag == 2) {
                        edittext_er.setText("");
                    }
                } else {
                    int temp;
                    Z0 = Double.parseDouble(edittext_Z0.getText().toString()); // get the parameters

                    Freq = Double.parseDouble(edittext_Freq.getText().toString());
                    temp = spinner_Freq.getSelectedItemPosition(); // convert the unit to Hz
                    if (temp == 0) {
                        Freq = Freq * 1000000;
                    } else if (temp == 1) {
                        Freq = Freq * 1000000000;
                    }
                    T = Double.parseDouble(edittext_T.getText().toString());
                    temp = spinner_T.getSelectedItemPosition(); // convert the unit to metre
                    if (temp == 0) {
                        T = T / 39370.0787402;
                    } else if (temp == 1) {
                        T = T / 1000;
                    } else if (temp == 2) {
                        T = T / 100;
                    }

                    if (flag == 0) {
                        H = Double.parseDouble(edittext_H.getText().toString());
                        temp = spinner_H.getSelectedItemPosition(); // convert the unit to metre
                        if (temp == 0) {
                            H = H / 39370.0787402;
                        } else if (temp == 1) {
                            H = H / 1000;
                        } else if (temp == 2) {
                            H = H / 100;
                        }
                        er = Double.parseDouble(edittext_er.getText().toString());
                        W = 0;
                    } else if (flag == 1) {
                        W = Double.parseDouble(edittext_W.getText().toString());
                        temp = spinner_W.getSelectedItemPosition();
                        if (temp == 0) {
                            W = W / 39370.0787402;
                        } else if (temp == 1) {
                            W = W / 1000;
                        } else if (temp == 2) {
                            W = W / 100;
                        }
                        er = Double.parseDouble(edittext_er.getText().toString());
                        H = 0;
                    } else if (flag == 2) {
                        W = Double.parseDouble(edittext_W.getText().toString());
                        temp = spinner_W.getSelectedItemPosition();
                        if (temp == 0) {
                            W = W / 39370.0787402;
                        } else if (temp == 1) {
                            W = W / 1000;
                        } else if (temp == 2) {
                            W = W / 100;
                        }
                        H = Double.parseDouble(edittext_H.getText().toString());
                        temp = spinner_H.getSelectedItemPosition(); // convert the unit to metre
                        if (temp == 0) {
                            H = H / 39370.0787402;
                        } else if (temp == 1) {
                            H = H / 1000;
                        } else if (temp == 2) {
                            H = H / 100;
                        }
                        er = 0;
                    }

                    if (edittext_Eeff.length() != 0) { // check if the Eeff is empty
                        Eeff = Double.parseDouble(edittext_Eeff.getText().toString());
                        SLIN slin = new SLIN(W, H, er, L, Z0, Eeff, Freq, T, flag);
                        slin.stripline_syn();
                        W = slin.getW();
                        H = slin.getH();
                        er = slin.geter();
                        L = slin.getL();
                        //stripline_syn(Z0, Eeff, Freq, flag);
                        temp = spinner_L.getSelectedItemPosition();
                        if (temp == 0) {
                            L = L * 1000 * 39.37007874;
                        } else if (temp == 1) {
                            L = L * 1000;
                        } else if (temp == 2) {
                            L = L * 100;
                        }
                        BigDecimal L_temp = new BigDecimal(L); // cut the decimal of L
                        L = L_temp.setScale(DecimalLength, BigDecimal.ROUND_HALF_UP)
                                .doubleValue();
                        edittext_L.setText(String.valueOf(L));
                    } else {
                        SLIN slin = new SLIN(W, H, er, L, Z0, 0, Freq, T, flag);
                        slin.stripline_syn();
                        W = slin.getW();
                        H = slin.getH();
                        er = slin.geter();
                        edittext_L.setText(""); // clear the L if the Eeff input is empty
                    }
                    if (flag == 0) {
                        temp = spinner_W.getSelectedItemPosition(); // W (m)
                        if (temp == 0) {
                            W = W * 1000 * 39.37007874;
                        } else if (temp == 1) {
                            W = W * 1000;
                        } else if (temp == 2) {
                            W = W * 100;
                        }

                        BigDecimal W_temp = new BigDecimal(W); // cut the decimal of W
                        W = W_temp.setScale(DecimalLength, BigDecimal.ROUND_HALF_UP)
                                .doubleValue();
                        edittext_W.setText(String.valueOf(W));
                    } else if (flag == 1) {
                        temp = spinner_H.getSelectedItemPosition();
                        if (temp == 0) {
                            H = H * 1000 * 39.37007874;
                        } else if (temp == 1) {
                            H = H * 1000;
                        } else if (temp == 2) {
                            H = H * 100;
                        }

                        BigDecimal H_temp = new BigDecimal(H);
                        H = H_temp.setScale(DecimalLength, BigDecimal.ROUND_HALF_UP)
                                .doubleValue();
                        edittext_H.setText(String.valueOf(H));
                    } else if (flag == 2) {
                        BigDecimal er_temp = new BigDecimal(er);
                        er = er_temp.setScale(DecimalLength, BigDecimal.ROUND_HALF_UP)
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
        width_input = rootView.findViewById(R.id.width_input_radio);
        height_input = rootView.findViewById(R.id.height_input_radio);
        er_input = rootView.findViewById(R.id.epsilon_input_radio);
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
        edittext_W = (EditText) rootView.findViewById(R.id.editText_W_radio);
        edittext_L = (EditText) rootView.findViewById(R.id.editText_L_radio);
        edittext_Z0 = (EditText) rootView.findViewById(R.id.editText_Z0);
        edittext_Eeff = (EditText) rootView.findViewById(R.id.editText_Eeff);
        edittext_Freq = (EditText) rootView.findViewById(R.id.editText_Freq);
        edittext_T = (EditText) rootView.findViewById(R.id.editText_T_radio);
        edittext_H = (EditText) rootView.findViewById(R.id.editText_H_radio);
        edittext_er = (EditText) rootView.findViewById(R.id.editText_er_radio);

        // button elements
        button_ana = (Button) rootView.findViewById(R.id.button_ana);
        button_syn = (Button) rootView.findViewById(R.id.button_syn);

        // spinner elements
        spinner_W = (Spinner) rootView.findViewById(R.id.spinner_W_radio);
        spinner_L = (Spinner) rootView.findViewById(R.id.spinner_L_radio);
        spinner_Z0 = (Spinner) rootView.findViewById(R.id.spinner_Z0);
        spinner_Eeff = (Spinner) rootView.findViewById(R.id.spinner_Eeff);
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
        radioBtn_H = (RadioButton) rootView.findViewById(R.id.radioBtn_H);
        radioBtn_er = (RadioButton) rootView.findViewById(R.id.radioBtn_er);
    }

    private void readSharedPref() {
        SharedPreferences prefs = getActivity().getSharedPreferences(MainActivity.SHARED_PREFS_NAME,
                ActionBarActivity.MODE_PRIVATE);// get the parameters from the Shared
        // Preferences in the device

        // read values from the shared preferences

        // SLIN parameters
        edittext_W.setText(prefs.getString(SLIN_W, "10.00"));
        spinner_W.setSelection(Integer.parseInt(prefs.getString(SLIN_W_UNIT,
                "0")));

        edittext_L.setText(prefs.getString(SLIN_L, "1000.00"));
        spinner_L.setSelection(Integer.parseInt(prefs.getString(SLIN_L_UNIT,
                "0")));

        edittext_Z0.setText(prefs.getString(SLIN_Z0, "49.76"));
        spinner_Z0.setSelection(Integer.parseInt(prefs.getString(
                SLIN_Z0_UNIT, "0")));

        edittext_Eeff.setText(prefs.getString(SLIN_Eeff, "61.00"));
        spinner_Eeff.setSelection(Integer.parseInt(prefs.getString(
                SLIN_Eeff_UNIT, "0")));

        edittext_Freq.setText(prefs.getString(SLIN_Freq, "1.00"));
        spinner_Freq.setSelection(Integer.parseInt(prefs.getString(
                SLIN_Freq_UNIT, "1")));

        edittext_er.setText(prefs.getString(SLIN_er, "4.00"));

        edittext_H.setText(prefs.getString(SLIN_H, "25.00"));
        spinner_H.setSelection(Integer.parseInt(prefs.getString(SLIN_H_UNIT,
                "0")));

        edittext_T.setText(prefs.getString(SLIN_T, "1.40"));
        spinner_T.setSelection(Integer.parseInt(prefs.getString(SLIN_T_UNIT,
                "0")));
        flag = Integer.parseInt(prefs.getString(SLIN_FLAG, "0"));
    }

    private void Preference_SharedPref() {
        SharedPreferences prefs = getActivity().getSharedPreferences(MainActivity.SHARED_PREFS_NAME,
                ActionBarActivity.MODE_PRIVATE);// get the parameters from the Shared
        // Preferences in the device
        // universal parameters
        DecimalLength = Integer.parseInt(prefs.getString("DecimalLength", "2"));
    }

    private void setRadioBtn() {
        if (flag == 0) {
            radioBtn_W.setChecked(true);
            radioBtn_H.setChecked(false);
            radioBtn_er.setChecked(false);
            width_input.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.green_shadow));
            height_input.setBackgroundColor(Color.WHITE);
            er_input.setBackgroundColor(Color.WHITE);
        } else if (flag == 1) {
            radioBtn_W.setChecked(false);
            radioBtn_H.setChecked(true);
            radioBtn_er.setChecked(false);
            width_input.setBackgroundColor(Color.WHITE);
            height_input.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.green_shadow));
            er_input.setBackgroundColor(Color.WHITE);
        } else if (flag == 2) {
            radioBtn_W.setChecked(false);
            radioBtn_H.setChecked(false);
            radioBtn_er.setChecked(true);
            width_input.setBackgroundColor(Color.WHITE);
            height_input.setBackgroundColor(Color.WHITE);
            er_input.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.green_shadow));
        }
        radioBtn_W.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                radioBtn_W.setChecked(true);
                radioBtn_H.setChecked(false);
                radioBtn_er.setChecked(false);
                width_input.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.green_shadow));
                height_input.setBackgroundColor(Color.WHITE);
                er_input.setBackgroundColor(Color.WHITE);
                flag = 0;
            }
        });
        radioBtn_H.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                radioBtn_W.setChecked(false);
                radioBtn_H.setChecked(true);
                radioBtn_er.setChecked(false);
                width_input.setBackgroundColor(Color.WHITE);
                height_input.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.green_shadow));
                er_input.setBackgroundColor(Color.WHITE);
                flag = 1;
            }
        });
        radioBtn_er.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                radioBtn_W.setChecked(false);
                radioBtn_H.setChecked(false);
                radioBtn_er.setChecked(true);
                width_input.setBackgroundColor(Color.WHITE);
                height_input.setBackgroundColor(Color.WHITE);
                er_input.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.green_shadow));
                flag = 2;
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        String slin_W, slin_H, slin_er, slin_L, slin_Z0, slin_Eeff, slin_Freq, slin_T;
        String slin_W_unit, slin_H_unit, slin_L_unit, slin_Z0_unit, slin_Eeff_unit, slin_Freq_unit, slin_T_unit;
        String slin_flag;

        SharedPreferences prefs = getActivity().getSharedPreferences(MainActivity.SHARED_PREFS_NAME,
                ActionBarActivity.MODE_PRIVATE);
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
        slin_Eeff_unit = Integer.toString(spinner_Eeff
                .getSelectedItemPosition());
        slin_Freq = edittext_Freq.getText().toString();
        slin_Freq_unit = Integer.toString(spinner_Freq
                .getSelectedItemPosition());
        slin_T = edittext_T.getText().toString();
        slin_T_unit = Integer.toString(spinner_T.getSelectedItemPosition());
        slin_flag = Integer.toString(flag);

        editor.putString(SLIN_W, slin_W);
        editor.putString(SLIN_W_UNIT, slin_W_unit);
        editor.putString(SLIN_H, slin_H);
        editor.putString(SLIN_H_UNIT, slin_H_unit);
        editor.putString(SLIN_er, slin_er);
        editor.putString(SLIN_L, slin_L);
        editor.putString(SLIN_L_UNIT, slin_L_unit);
        editor.putString(SLIN_Z0, slin_Z0);
        editor.putString(SLIN_Z0_UNIT, slin_Z0_unit);
        editor.putString(SLIN_Eeff, slin_Eeff);
        editor.putString(SLIN_Eeff_UNIT, slin_Eeff_unit);
        editor.putString(SLIN_Freq, slin_Freq);
        editor.putString(SLIN_Freq_UNIT, slin_Freq_unit);
        editor.putString(SLIN_T, slin_T);
        editor.putString(SLIN_T_UNIT, slin_T_unit);
        editor.putString(SLIN_FLAG, slin_flag);
        editor.commit();
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
