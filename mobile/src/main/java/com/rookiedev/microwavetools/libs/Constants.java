package com.rookiedev.microwavetools.libs;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.SubscriptSpan;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.rookiedev.microwavetools.R;

public class Constants {
    // Shared Preference values
    public static final String SHARED_PREFS_NAME = "com.rookiedev.microwavetools_preferences";
    public static final String PREFS_POSITION = "POSITION";
    public static final String PREFS_ISFIRSTRUN = "ISFIRSTRUN";

    public static final int DecimalLength = 3;

    // constant values
    public static final double LIGHTSPEED = 2.99792458e8; // free space speed of light, m/s
    public static final double FREESPACE_MU0 = 4.0 * Math.PI * 1.0e-7; // free space permitivitty, H/m
    public static final double FREESPACE_E0 = 1.0 / (LIGHTSPEED * LIGHTSPEED * FREESPACE_MU0); // free space
                                                                                               // permitivitty, F/m
    public static final double FREESPACEZ0 = FREESPACE_MU0 * LIGHTSPEED; // free space impedance, Ohms
    public static final double Pi = Math.PI;
    public static final double MINI_LIMIT = 1e-10; // 0.1 nm

    // flags
    public static final int Synthesize_Width = 0, Synthesize_Height = 1, Synthesize_Er = 2, Synthesize_Length = 3,
            Synthesize_Gap = 4, Synthesize_CoreRadius = 5, Synthesize_CoreOffset = 6;
    public static final int Synthesize_SubRadius = Synthesize_Height;
    public static final int LengthUnit_mil = 0, LengthUnit_mm = 1, LengthUnit_cm = 2, LengthUnit_m = 3;
    public static final int FreqUnit_MHz = 0, FreqUnit_GHz = 1, FreqUnit_Hz = 10;
    public static final int ImpedanceUnit_Ohm = 0;
    public static final int PhaseUnit_Degree = 0;

    public static final int PositionMlin = 0, PositionCmlin = 1, PositionSlin = 2, PositionCslin = 3, PositionCpw = 4,
            PositionGcpw = 5, PositionCoax = 6;

    public static final String IS_AD_FREE = "IS_AD_FREE";

    public static final String MLIN_W = "MLIN_W";
    public static final String MLIN_W_UNIT = "MLIN_W_UNIT";
    public static final String MLIN_L = "MLIN_L";
    public static final String MLIN_L_UNIT = "MLIN_L_UNIT";
    public static final String MLIN_Z0 = "MLIN_Z0";
    public static final String MLIN_Z0_UNIT = "MLIN_Z0_UNIT";
    public static final String MLIN_PHS = "MLIN_PHS";
    public static final String MLIN_PHS_UNIT = "MLIN_PHS_UNIT";
    public static final String MLIN_FREQ = "MLIN_FREQ";
    public static final String MLIN_FREQ_UNIT = "MLIN_FREQ_UNIT";
    public static final String MLIN_ER = "MLIN_ER";
    public static final String MLIN_H = "MLIN_H";
    public static final String MLIN_H_UNIT = "MLIN_H_UNIT";
    public static final String MLIN_T = "MLIN_T";
    public static final String MLIN_T_UNIT = "MLIN_T_UNIT";
    public static final String MLIN_TARGET = "MLIN_TARGET";

    public static final String CMLIN_W = "CMLIN_W";
    public static final String CMLIN_W_UNIT = "CMLIN_W_UNIT";
    public static final String CMLIN_S = "CMLIN_S";
    public static final String CMLIN_S_UNIT = "CMLIN_S_UNIT";
    public static final String CMLIN_L = "CMLIN_L";
    public static final String CMLIN_L_UNIT = "CMLIN_L_UNIT";
    public static final String CMLIN_Z0 = "CMLIN_Z0";
    public static final String CMLIN_Z0_UNIT = "CMLIN_Z0_UNIT";
    public static final String CMLIN_K = "CMLIN_K";
    public static final String CMLIN_Z0O = "CMLIN_Z0O";
    public static final String CMLIN_Z0O_UNIT = "CMLIN_Z0O_UNIT";
    public static final String CMLIN_Z0E = "CMLIN_Z0E";
    public static final String CMLIN_Z0E_UNIT = "CMLIN_Z0E_UNIT";
    public static final String CMLIN_PHS = "CMLIN_PHS";
    public static final String CMLIN_PHS_UNIT = "CMLIN_PHS_UNIT";
    public static final String CMLIN_FREQ = "CMLIN_FREQ";
    public static final String CMLIN_FREQ_UNIT = "CMLIN_FREQ_UNIT";
    public static final String CMLIN_ER = "CMLIN_ER";
    public static final String CMLIN_H = "CMLIN_H";
    public static final String CMLIN_H_UNIT = "CMLIN_H_UNIT";
    public static final String CMLIN_T = "CMLIN_T";
    public static final String CMLIN_T_UNIT = "CMLIN_T_UNIT";
    public static final String CMLIN_USEZ0K = "CMLIN_USEZ0K";

    public static final String SLIN_W = "SLIN_W";
    public static final String SLIN_W_UNIT = "SLIN_W_UNIT";
    public static final String SLIN_L = "SLIN_L";
    public static final String SLIN_L_UNIT = "SLIN_L_UNIT";
    public static final String SLIN_Z0 = "SLIN_Z0";
    public static final String SLIN_Z0_UNIT = "SLIN_Z0_UNIT";
    public static final String SLIN_PHS = "SLIN_PHS";
    public static final String SLIN_PHS_UNIT = "SLIN_PHS_UNIT";
    public static final String SLIN_FREQ = "SLIN_FREQ";
    public static final String SLIN_FREQ_UNIT = "SLIN_FREQ_UNIT";
    public static final String SLIN_ER = "SLIN_ER";
    public static final String SLIN_H = "SLIN_H";
    public static final String SLIN_H_UNIT = "SLIN_H_UNIT";
    public static final String SLIN_T = "SLIN_T";
    public static final String SLIN_T_UNIT = "SLIN_T_UNIT";
    public static final String SLIN_TARGET = "SLIN_TARGET";

    public static final String CSLIN_W = "CSLIN_W";
    public static final String CSLIN_W_UNIT = "CSLIN_W_UNIT";
    public static final String CSLIN_S = "CSLIN_S";
    public static final String CSLIN_S_UNIT = "CSLIN_S_UNIT";
    public static final String CSLIN_L = "CSLIN_L";
    public static final String CSLIN_L_UNIT = "CSLIN_L_UNIT";
    public static final String CSLIN_Z0 = "CSLIN_Z0";
    public static final String CSLIN_Z0_UNIT = "CSLIN_Z0_UNIT";
    public static final String CSLIN_K = "CSLIN_K";
    public static final String CSLIN_Z0O = "CSLIN_Z0O";
    public static final String CSLIN_Z0O_UNIT = "CSLIN_Z0O_UNIT";
    public static final String CSLIN_Z0E = "CSLIN_Z0E";
    public static final String CSLIN_Z0E_UNIT = "CSLIN_Z0E_UNIT";
    public static final String CSLIN_PHS = "CSLIN_PHS";
    public static final String CSLIN_PHS_UNIT = "CSLIN_PHS_UNIT";
    public static final String CSLIN_FREQ = "CSLIN_FREQ";
    public static final String CSLIN_FREQ_UNIT = "CSLIN_FREQ_UNIT";
    public static final String CSLIN_ER = "CSLIN_ER";
    public static final String CSLIN_H = "CSLIN_H";
    public static final String CSLIN_H_UNIT = "CSLIN_H_UNIT";
    public static final String CSLIN_T = "CSLIN_T";
    public static final String CSLIN_T_UNIT = "CSLIN_T_UNIT";
    public static final String CSLIN_USEZ0K = "CSLIN_USEZ0K";

    public static final String CPW_W = "CPW_W";
    public static final String CPW_W_UNIT = "CPW_W_UNIT";
    public static final String CPW_S = "CPW_S";
    public static final String CPW_S_UNIT = "CPW_S_UNIT";
    public static final String CPW_L = "CPW_L";
    public static final String CPW_L_UNIT = "CPW_L_UNIT";
    public static final String CPW_Z0 = "CPW_Z0";
    public static final String CPW_Z0_UNIT = "CPW_Z0_UNIT";
    public static final String CPW_PHS = "CPW_PHS";
    public static final String CPW_PHS_UNIT = "CPW_PHS_UNIT";
    public static final String CPW_FREQ = "CPW_FREQ";
    public static final String CPW_FREQ_UNIT = "CPW_FREQ_UNIT";
    public static final String CPW_ER = "CPW_ER";
    public static final String CPW_H = "CPW_H";
    public static final String CPW_H_UNIT = "CPW_H_UNIT";
    public static final String CPW_T = "CPW_T";
    public static final String CPW_T_UNIT = "CPW_T_UNIT";
    public static final String CPW_TARGET = "CPW_TARGET";

    public static final String PARAMS_CPW = "PARAMS_CPW";
    public static final String GROUNDED_CPW = "GROUNDED_CPW";
    public static final String UNGROUNDED_CPW = "UNGROUNDED_CPW";

    public static final String GCPW_W = "GCPW_W";
    public static final String GCPW_W_UNIT = "GCPW_W_UNIT";
    public static final String GCPW_S = "GCPW_S";
    public static final String GCPW_S_UNIT = "GCPW_S_UNIT";
    public static final String GCPW_L = "GCPW_L";
    public static final String GCPW_L_UNIT = "GCPW_L_UNIT";
    public static final String GCPW_Z0 = "GCPW_Z0";
    public static final String GCPW_Z0_UNIT = "GCPW_Z0_UNIT";
    public static final String GCPW_PHS = "GCPW_PHS";
    public static final String GCPW_PHS_UNIT = "GCPW_PHS_UNIT";
    public static final String GCPW_FREQ = "GCPW_FREQ";
    public static final String GCPW_FREQ_UNIT = "GCPW_FREQ_UNIT";
    public static final String GCPW_ER = "GCPW_ER";
    public static final String GCPW_H = "GCPW_H";
    public static final String GCPW_H_UNIT = "GCPW_H_UNIT";
    public static final String GCPW_T = "GCPW_T";
    public static final String GCPW_T_UNIT = "GCPW_T_UNIT";
    public static final String GCPW_TARGET = "GCPW_TARGET";

    public static final String COAX_A = "COAX_A";
    public static final String COAX_A_UNIT = "COAX_A_UNIT";
    public static final String COAX_B = "COAX_B";
    public static final String COAX_B_UNIT = "COAX_B_UNIT";
    public static final String COAX_C = "COAX_C";
    public static final String COAX_C_UNIT = "COAX_C_UNIT";
    public static final String COAX_ER = "COAX_ER";
    public static final String COAX_L = "COAX_L";
    public static final String COAX_L_UNIT = "COAX_L_UNIT";
    public static final String COAX_Z0 = "COAX_Z0";
    public static final String COAX_Z0_UNIT = "COAX_Z0_UNIT";
    public static final String COAX_PHS = "COAX_PHS";
    public static final String COAX_PHS_UNIT = "COAX_PHS_UNIT";
    public static final String COAX_FREQ = "COAX_FREQ";
    public static final String COAX_FREQ_UNIT = "COAX_FREQ_UNIT";
    public static final String COAX_TARGET = "COAX_TARGET";

    /**
     * Array adapters for spinners
     * 
     * @param mContext context
     * @return adapters
     */
    public static ArrayAdapter<CharSequence> adapterDimensionUnits(Context mContext) {
        ArrayAdapter<CharSequence> adapterLength = ArrayAdapter.createFromResource(mContext, R.array.list_units_Length,
                android.R.layout.simple_spinner_item);
        adapterLength.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapterLength;
    }

    public static ArrayAdapter<CharSequence> adapterImpedanceUnits(Context mContext) {
        ArrayAdapter<CharSequence> adapterImpedance = ArrayAdapter.createFromResource(mContext,
                R.array.list_units_Impedance, android.R.layout.simple_spinner_item);
        adapterImpedance.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapterImpedance;
    }

    public static ArrayAdapter<CharSequence> adapterPhaseUnits(Context mContext) {
        ArrayAdapter<CharSequence> adapterPhase = ArrayAdapter.createFromResource(mContext, R.array.list_units_Phase,
                android.R.layout.simple_spinner_item);
        adapterPhase.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapterPhase;
    }

    public static ArrayAdapter<CharSequence> adapterFrequencyUnits(Context mContext) {
        ArrayAdapter<CharSequence> adapterFreq = ArrayAdapter.createFromResource(mContext, R.array.list_units_Frequency,
                android.R.layout.simple_spinner_item);
        adapterFreq.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapterFreq;
    }

    // unit conversion
    public static double value2meter(double value, int lengthUnit) {
        double l = 0;
        switch (lengthUnit) {
        case LengthUnit_mil:
            l = value / 39370.0787402;
            break;
        case LengthUnit_mm:
            l = value / 1000;
            break;
        case LengthUnit_cm:
            l = value / 100;
            break;
        case LengthUnit_m:
            l = value;
            break;
        }
        return l;
    }

    public static double value2Hz(double value, int freqUnit) {
        double f = 0;
        switch (freqUnit) {
        case FreqUnit_Hz:
            f = value;
            break;
        case FreqUnit_MHz:
            f = value * 1e6;
            break;
        case FreqUnit_GHz:
            f = value * 1e9;
            break;
        }
        return f;
    }

    public static double meter2others(double value, int lengthUnit) {
        double l = 0;
        switch (lengthUnit) {
        case LengthUnit_mil:
            l = value * 1000 * 39.37007874;
            break;
        case LengthUnit_mm:
            l = value * 1000;
            break;
        case LengthUnit_cm:
            l = value * 100;
            break;
        case LengthUnit_m:
            l = value;
            break;
        }
        return l;
    }

    public static final int ANALYZE = 0, SYNTHESIZE = 1;

    public static void refreshAnimation(Context mContext, final ImageView view, int flag) {
        int cx, cy;
        if (flag == ANALYZE) {
            cx = view.getRight();
            cy = view.getTop();
        } else {
            cx = view.getLeft();
            cy = view.getBottom();
        }
        int finalRadius = (int) Math.sqrt(Math.pow(view.getWidth(), 2) + Math.pow(view.getHeight(), 2));

        Animator animCircularReveal = ViewAnimationUtils.createCircularReveal(view, cx, cy, 0, finalRadius);
        view.setVisibility(View.VISIBLE);
        animCircularReveal.setDuration(mContext.getResources().getInteger(android.R.integer.config_mediumAnimTime));
        animCircularReveal.setInterpolator(new AccelerateDecelerateInterpolator());
        animCircularReveal.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                view.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        ObjectAnimator animFadeOut = ObjectAnimator.ofFloat(view, View.ALPHA, 1f, 0f);
        animFadeOut.setDuration(mContext.getResources().getInteger(android.R.integer.config_mediumAnimTime));
        animFadeOut.setInterpolator(new AccelerateInterpolator());

        /*
         * ValueAnimator animFadeOut= ValueAnimator.ofFloat(1f, 0.2f);
         * animFadeOut.setDuration(mContext.getResources().getInteger(android.R.integer.
         * config_mediumAnimTime)); animFadeOut.setInterpolator(new
         * DecelerateInterpolator()); animFadeOut.addUpdateListener(new
         * ValueAnimator.AnimatorUpdateListener() {
         * 
         * @Override public void onAnimationUpdate(ValueAnimator animation) {
         * view.setAlpha((Float) animation.getAnimatedValue()); } });
         */

        animCircularReveal.start();
        animFadeOut.start();
    }

    public enum ERROR {
        NO_ERROR(0), DIMENSION_MINIMAL_LIMIT(1), ER_MINIMAL_LIMIT(2), COULD_NOT_BRACKET_SOLUTION(3), MAX_ITERATIONS(4),
        WIDTH_MINIMAL_LIMIT(5), HEIGHT_MINIMAL_LIMIT(6), SPACE_MINIMAL_LIMIT(7), K_OUT_OF_RANGE(8), Z0E_Z0O_MISTAKE(9),
        SUBSTRATE_TOO_LARGE(10), OFFSET_TOO_LARGE(11);

        private int errorCode;

        ERROR(int errorCode) {
            this.errorCode = errorCode;
        }
    }

    public enum WARNING {
        NO_WARNING(0), WIDTH2HEIGHT_OUT_OF_RANGE(1), SPACE2HEIGHT_OUT_OF_RANGE(2), DIELECTRIC_OUT_OF_RANGE(3),
        FREQUENCY2HEIGHT_OUT_OF_RANGE(4);

        private int warningCode;

        WARNING(int warningCode) {
            this.warningCode = warningCode;
        }
    }
}
