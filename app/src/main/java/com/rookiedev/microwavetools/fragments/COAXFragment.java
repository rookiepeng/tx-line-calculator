package com.rookiedev.microwavetools.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
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
import com.rookiedev.microwavetools.libs.COAX;

import java.math.BigDecimal;

/**
 * Created by rookie on 8/11/13.
 */
public class COAXFragment extends Fragment {
    public final static String COAX_A = "COAX_A";
    public final static String COAX_A_UNIT = "COAX_A_UNIT";
    public final static String COAX_B = "COAX_B";
    public final static String COAX_B_UNIT = "COAX_B_UNIT";
    public final static String COAX_C = "COAX_C";
    public final static String COAX_C_UNIT = "COAX_C_UNIT";
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
    private View rootView;
    private int DecimalLength; // the length of the Decimal,
    private SpannableString error_er, error_Z0;
    private TextView text_er, text_Z0, text_Eeff; // strings which include the subscript
    private EditText edittext_a, //
            edittext_b, //
            edittext_c, //
            edittext_L, // the length
            edittext_Z0, // the impedance
            edittext_Eeff, // the electrical length
            edittext_Freq, // the frequency
            edittext_T, // the thickness of the metal
            edittext_er; // the relative dielectric constant
    private double a, b, c, L, Z0, Eeff, Freq, T, er;
    private Button coax_syn,// button synthesize
            coax_ana;// button analyze
    private Spinner spinner_a, spinner_b, spinner_c, spinner_L, spinner_T,
            spinner_Z0, spinner_Eeff, spinner_Freq;// the units of each
    // parameter
    private int flag;
    private RadioButton radioBtn1, radioBtn2, radioBtn3, radioBtn4;


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

        coax_ana.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int temp;
                Preference_SharedPref();
                if (!analysisInputCheck()) {
                    edittext_Z0.setText(""); // clear the Z0 and Eeff outputs
                    edittext_Eeff.setText("");
                } else {
                    a = Double.parseDouble(edittext_a.getText().toString()); // get the parameters
                    b = Double.parseDouble(edittext_b.getText().toString());
                    c = Double.parseDouble(edittext_c.getText().toString());

                    Freq = Double.parseDouble(edittext_Freq.getText().toString());
                    er = Double.parseDouble(edittext_er.getText().toString());
                    T = Double.parseDouble(edittext_T.getText().toString());

                    temp = spinner_a.getSelectedItemPosition(); // convert unit to meter
                    if (temp == 0) {
                        a = a / 39.37007874 / 1000;
                    } else if (temp == 1) {
                        a = a / 1000;
                    } else if (temp == 2) {
                        a = a * 100;
                    }

                    temp = spinner_b.getSelectedItemPosition(); // convert unit to meter
                    if (temp == 0) {
                        b = b / 39.37007874 / 1000;
                    } else if (temp == 1) {
                        b = b / 1000;
                    } else if (temp == 2) {
                        b = b * 100;
                    }

                    temp = spinner_c.getSelectedItemPosition(); // convert unit to meter
                    if (temp == 0) {
                        c = c / 39.37007874 / 1000;
                    } else if (temp == 1) {
                        c = c / 1000;
                    } else if (temp == 2) {
                        c = c * 100;
                    }

                    temp = spinner_Freq.getSelectedItemPosition(); // convert unit to Hz
                    if (temp == 0) {
                        Freq = Freq * 1e6;
                    } else if (temp == 1) {
                        Freq = Freq * 1e9;
                    }

                    temp = spinner_T.getSelectedItemPosition(); // convert unit to meter
                    if (temp == 0) {
                        T = T / 39.37007874 / 1000;
                    } else if (temp == 1) {
                        T = T / 1000;
                    } else if (temp == 2) {
                        T = T * 100;
                    }

                    if (!edittext_L.getText().toString().equals("")) { // check the L input
                        L = Double.parseDouble(edittext_L.getText().toString());
                        temp = spinner_L.getSelectedItemPosition(); // convert unit to meter
                        if (temp == 0) {
                            L = L / 39.37007874 / 1000;
                        } else if (temp == 1) {
                            L = L / 1000;
                        } else if (temp == 2) {
                            L = L * 100;
                        }
                        COAX coax = new COAX(a, b, c, er, L, 0, 0, Freq, T, flag);
                        Z0 = coax.getZ0(); // calculate the Z0
                        Eeff = coax.getEeff(); // calculate the Eeff

                        BigDecimal Eeff_temp = new BigDecimal(Eeff); // cut the decimal of the Eeff
                        Eeff = Eeff_temp.setScale(DecimalLength,
                                BigDecimal.ROUND_HALF_UP).doubleValue();
                        edittext_Eeff.setText(String.valueOf(Eeff));
                    } else {
                        COAX coax = new COAX(a, b, c, er, 0, 0, 0, Freq, T, flag);
                        Z0 = coax.getZ0(); // calculate the Z0
                        edittext_Eeff.setText(""); // if the L input is empty, clear the Eeff
                    }
                    BigDecimal Z0_temp = new BigDecimal(Z0);
                    Z0 = Z0_temp.setScale(DecimalLength,
                            BigDecimal.ROUND_HALF_UP).doubleValue();
                    edittext_Z0.setText(String.valueOf(Z0)); // cut the decimal of the Z0
                }
            }
        });

        coax_syn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Preference_SharedPref();
                if (!synthesizeInputCheck()) {
                    if (flag == 0) {
                        edittext_a.setText("");
                    } else if (flag == 1) {
                        edittext_b.setText("");
                    } else if (flag == 2) {
                        edittext_c.setText("");
                    } else if (flag == 3) {
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
                        b = Double.parseDouble(edittext_b.getText().toString());
                        temp = spinner_b.getSelectedItemPosition(); // convert the unit to metre
                        if (temp == 0) {
                            b = b / 39370.0787402;
                        } else if (temp == 1) {
                            b = b / 1000;
                        } else if (temp == 2) {
                            b = b / 100;
                        }
                        c = Double.parseDouble(edittext_c.getText().toString());
                        temp = spinner_c.getSelectedItemPosition();
                        if (temp == 0) {
                            c = c / 39370.0787402;
                        } else if (temp == 1) {
                            c = c / 1000;
                        } else if (temp == 2) {
                            c = c / 100;
                        }
                        er = Double.parseDouble(edittext_er.getText().toString());
                        a = 0;
                    } else if (flag == 1) {
                        a = Double.parseDouble(edittext_a.getText().toString());
                        temp = spinner_a.getSelectedItemPosition(); // convert the unit to metre
                        if (temp == 0) {
                            a = a / 39370.0787402;
                        } else if (temp == 1) {
                            a = a / 1000;
                        } else if (temp == 2) {
                            a = a / 100;
                        }
                        c = Double.parseDouble(edittext_c.getText().toString());
                        temp = spinner_c.getSelectedItemPosition();
                        if (temp == 0) {
                            c = c / 39370.0787402;
                        } else if (temp == 1) {
                            c = c / 1000;
                        } else if (temp == 2) {
                            c = c / 100;
                        }
                        er = Double.parseDouble(edittext_er.getText().toString());
                        b = 0;
                    } else if (flag == 2) {
                        a = Double.parseDouble(edittext_a.getText().toString());
                        temp = spinner_a.getSelectedItemPosition(); // convert the unit to metre
                        if (temp == 0) {
                            a = a / 39370.0787402;
                        } else if (temp == 1) {
                            a = a / 1000;
                        } else if (temp == 2) {
                            a = a / 100;
                        }
                        b = Double.parseDouble(edittext_b.getText().toString());
                        temp = spinner_b.getSelectedItemPosition();
                        if (temp == 0) {
                            b = b / 39370.0787402;
                        } else if (temp == 1) {
                            b = b / 1000;
                        } else if (temp == 2) {
                            b = b / 100;
                        }
                        er = Double.parseDouble(edittext_er.getText().toString());
                        c = 0;
                    } else if (flag == 3) {
                        a = Double.parseDouble(edittext_a.getText().toString());
                        temp = spinner_a.getSelectedItemPosition(); // convert the unit to metre
                        if (temp == 0) {
                            a = a / 39370.0787402;
                        } else if (temp == 1) {
                            a = a / 1000;
                        } else if (temp == 2) {
                            a = a / 100;
                        }
                        b = Double.parseDouble(edittext_b.getText().toString());
                        temp = spinner_b.getSelectedItemPosition();
                        if (temp == 0) {
                            b = b / 39370.0787402;
                        } else if (temp == 1) {
                            b = b / 1000;
                        } else if (temp == 2) {
                            b = b / 100;
                        }
                        c = Double.parseDouble(edittext_c.getText().toString());
                        temp = spinner_c.getSelectedItemPosition(); // convert the unit to metre
                        if (temp == 0) {
                            c = c / 39370.0787402;
                        } else if (temp == 1) {
                            c = c / 1000;
                        } else if (temp == 2) {
                            c = c / 100;
                        }
                        er = 0;
                    }

                    if (edittext_Eeff.length() != 0) { // check if the Eeff is empty
                        Eeff = Double.parseDouble(edittext_Eeff.getText().toString());
                        COAX coax = new COAX(a, b, c, er, 0, Z0, Eeff, Freq, T, flag);
                        coax.coax_syn();
                        L = coax.getL();
                        a = coax.geta();
                        b = coax.getb();
                        c = coax.getc();
                        er = coax.geter();

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
                        COAX coax = new COAX(a, b, c, er, 0, Z0, 0, Freq, T, flag);
                        coax.coax_syn();
                        a = coax.geta();
                        b = coax.getb();
                        c = coax.getc();
                        er = coax.geter();
                        edittext_L.setText(""); // clear the L if the Eeff input is empty
                    }
                    if (flag == 0) {
                        temp = spinner_a.getSelectedItemPosition(); // W (m)
                        if (temp == 0) {
                            a = a * 1000 * 39.37007874;
                        } else if (temp == 1) {
                            a = a * 1000;
                        } else if (temp == 2) {
                            a = a * 100;
                        }

                        BigDecimal a_temp = new BigDecimal(a); // cut the decimal of W
                        a = a_temp.setScale(DecimalLength, BigDecimal.ROUND_HALF_UP)
                                .doubleValue();
                        edittext_a.setText(String.valueOf(a));
                    } else if (flag == 1) {
                        temp = spinner_b.getSelectedItemPosition();
                        if (temp == 0) {
                            b = b * 1000 * 39.37007874;
                        } else if (temp == 1) {
                            b = b * 1000;
                        } else if (temp == 2) {
                            b = b * 100;
                        }

                        BigDecimal b_temp = new BigDecimal(b); // cut the decimal of S
                        b = b_temp.setScale(DecimalLength, BigDecimal.ROUND_HALF_UP)
                                .doubleValue();
                        edittext_b.setText(String.valueOf(b));
                    } else if (flag == 2) {
                        temp = spinner_c.getSelectedItemPosition();
                        if (temp == 0) {
                            c = c * 1000 * 39.37007874;
                        } else if (temp == 1) {
                            c = c * 1000;
                        } else if (temp == 2) {
                            c = c * 100;
                        }

                        BigDecimal c_temp = new BigDecimal(c);
                        c = c_temp.setScale(DecimalLength, BigDecimal.ROUND_HALF_UP)
                                .doubleValue();
                        edittext_c.setText(String.valueOf(c));
                    } else if (flag == 3) {
                        BigDecimal er_temp = new BigDecimal(er);
                        er = er_temp.setScale(DecimalLength, BigDecimal.ROUND_HALF_UP)
                                .doubleValue();
                        edittext_er.setText(String.valueOf(er));
                    }
                }
            }
        });

        /** Look up the AdView as a resource and load a request. */
        AdView adView = (AdView) rootView.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR) // All emulators
                .addTestDevice("015d172c791c0215") // my test device
                .addTestDevice("04afa117002e7ebc") // my test device
                .build();
        adView.loadAd(adRequest);

        return rootView;
    }

    private void initUI() {
        error_er = new SpannableString(this.getString(R.string.Error_er_empty));
        error_Z0 = new SpannableString(this.getString(R.string.Error_Z0_empty));

        /** find the elements */

        // Subscript strings
        text_er = (TextView) rootView.findViewById(R.id.coax_text_er);
        text_Z0 = (TextView) rootView.findViewById(R.id.coax_text_Z0);
        text_Eeff = (TextView) rootView.findViewById(R.id.coax_text_Eeff);

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
        edittext_a = (EditText) rootView.findViewById(R.id.coax_editText_a);
        edittext_b = (EditText) rootView.findViewById(R.id.coax_editText_b);
        edittext_c = (EditText) rootView.findViewById(R.id.coax_editText_c);
        edittext_L = (EditText) rootView.findViewById(R.id.coax_editText_L);
        edittext_Z0 = (EditText) rootView.findViewById(R.id.coax_editText_Z0);
        edittext_Eeff = (EditText) rootView.findViewById(R.id.coax_editText_Eeff);
        edittext_Freq = (EditText) rootView.findViewById(R.id.coax_editText_Freq);
        edittext_T = (EditText) rootView.findViewById(R.id.coax_editText_T);
        edittext_er = (EditText) rootView.findViewById(R.id.coax_editText_er);

        // button elements
        coax_ana = (Button) rootView.findViewById(R.id.coax_ana);
        coax_syn = (Button) rootView.findViewById(R.id.coax_syn);

        // spinner elements
        spinner_a = (Spinner) rootView.findViewById(R.id.coax_spinner_a);
        spinner_b = (Spinner) rootView.findViewById(R.id.coax_spinner_b);
        spinner_c = (Spinner) rootView.findViewById(R.id.coax_spinner_c);
        spinner_L = (Spinner) rootView.findViewById(R.id.coax_spinner_L);
        spinner_Z0 = (Spinner) rootView.findViewById(R.id.coax_spinner_Z0);
        spinner_Eeff = (Spinner) rootView.findViewById(R.id.coax_spinner_Eeff);
        spinner_Freq = (Spinner) rootView.findViewById(R.id.coax_spinner_Freq);
        spinner_T = (Spinner) rootView.findViewById(R.id.coax_spinner_T);

        // configure the length units
        ArrayAdapter<CharSequence> adapterLength = ArrayAdapter
                .createFromResource(this.getActivity(), R.array.list_units_Length,
                        android.R.layout.simple_spinner_item);
        adapterLength
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_a.setAdapter(adapterLength);
        spinner_b.setAdapter(adapterLength);
        spinner_c.setAdapter(adapterLength);
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
        radioBtn1 = (RadioButton) rootView.findViewById(R.id.radioBtn1);
        radioBtn2 = (RadioButton) rootView.findViewById(R.id.radioBtn2);
        radioBtn3 = (RadioButton) rootView.findViewById(R.id.radioBtn3);
        radioBtn4 = (RadioButton) rootView.findViewById(R.id.radioBtn4);
    }

    private void readSharedPref() {
        SharedPreferences prefs = getActivity().getSharedPreferences(MainActivity.SHARED_PREFS_NAME,
                ActionBarActivity.MODE_PRIVATE);// get the parameters from the Shared
        // Preferences in the device

        // read values from the shared preferences

        // coax parameters
        edittext_a.setText(prefs.getString(COAX_A, "0.30"));
        spinner_a.setSelection(Integer.parseInt(prefs.getString(COAX_A_UNIT,
                "2")));

        edittext_b.setText(prefs.getString(COAX_B, "1.00"));
        spinner_b.setSelection(Integer.parseInt(prefs.getString(COAX_B_UNIT,
                "2")));

        edittext_c.setText(prefs.getString(COAX_C, "0.00"));
        spinner_c.setSelection(Integer.parseInt(prefs.getString(COAX_C_UNIT,
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
        flag = Integer.parseInt(prefs.getString(COAX_Flag, "0"));
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
            radioBtn1.setChecked(true);
            radioBtn2.setChecked(false);
            radioBtn3.setChecked(false);
            radioBtn4.setChecked(false);
        } else if (flag == 1) {
            radioBtn1.setChecked(false);
            radioBtn2.setChecked(true);
            radioBtn3.setChecked(false);
            radioBtn4.setChecked(false);
        } else if (flag == 2) {
            radioBtn1.setChecked(false);
            radioBtn2.setChecked(false);
            radioBtn3.setChecked(true);
            radioBtn4.setChecked(false);
        } else if (flag == 3) {
            radioBtn1.setChecked(false);
            radioBtn2.setChecked(false);
            radioBtn3.setChecked(false);
            radioBtn4.setChecked(true);
        }
        radioBtn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                radioBtn1.setChecked(true);
                radioBtn2.setChecked(false);
                radioBtn3.setChecked(false);
                radioBtn4.setChecked(false);
                flag = 0;
            }
        });
        radioBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                radioBtn1.setChecked(false);
                radioBtn2.setChecked(true);
                radioBtn3.setChecked(false);
                radioBtn4.setChecked(false);
                flag = 1;
            }
        });
        radioBtn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                radioBtn1.setChecked(false);
                radioBtn2.setChecked(false);
                radioBtn3.setChecked(true);
                radioBtn4.setChecked(false);
                flag = 2;
            }
        });
        radioBtn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                radioBtn1.setChecked(false);
                radioBtn2.setChecked(false);
                radioBtn3.setChecked(false);
                radioBtn4.setChecked(true);
                flag = 3;
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        String coax_a, coax_b, coax_c, coax_er, coax_L, coax_Z0, coax_Eeff, coax_Freq, coax_T;
        String coax_a_unit, coax_b_unit, coax_c_unit, coax_L_unit, coax_Z0_unit, coax_Eeff_unit, coax_Freq_unit, coax_T_unit;
        String coax_flag;

        SharedPreferences prefs = getActivity().getSharedPreferences(MainActivity.SHARED_PREFS_NAME,
                ActionBarActivity.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        coax_a = edittext_a.getText().toString();
        coax_a_unit = Integer.toString(spinner_a.getSelectedItemPosition());
        coax_b = edittext_b.getText().toString();
        coax_b_unit = Integer.toString(spinner_b.getSelectedItemPosition());
        coax_c = edittext_c.getText().toString();
        coax_c_unit = Integer.toString(spinner_c.getSelectedItemPosition());
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
        editor.putString(COAX_B, coax_b);
        editor.putString(COAX_B_UNIT, coax_b_unit);
        editor.putString(COAX_C, coax_c);
        editor.putString(COAX_C_UNIT, coax_c_unit);
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

        editor.commit();
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
        if (edittext_b.length() == 0) {
            edittext_b.setError(getText(R.string.Error_b_empty));
            checkResult = false;
        }
        if (edittext_c.length() == 0) {
            edittext_c.setError(getText(R.string.Error_c_empty));
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
            if (edittext_b.length() == 0) {
                edittext_b.setError(getText(R.string.Error_b_empty));
                checkResult = false;
            }
            if (edittext_c.length() == 0) {
                edittext_c.setError(getText(R.string.Error_c_empty));
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
            if (edittext_c.length() == 0) {
                edittext_c.setError(getText(R.string.Error_c_empty));
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
            if (edittext_b.length() == 0) {
                edittext_b.setError(getText(R.string.Error_b_empty));
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
            if (edittext_b.length() == 0) {
                edittext_b.setError(getText(R.string.Error_b_empty));
                checkResult = false;
            }
            if (edittext_c.length() == 0) {
                edittext_c.setError(getText(R.string.Error_c_empty));
                checkResult = false;
            }
        }
        return checkResult;
    }
}
