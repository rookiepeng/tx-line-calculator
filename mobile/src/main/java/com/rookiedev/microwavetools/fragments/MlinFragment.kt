package com.rookiedev.microwavetools.fragments

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageView
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.rookiedev.microwavetools.R
import com.rookiedev.microwavetools.libs.Constants
import com.rookiedev.microwavetools.libs.MlinCalculator
import com.rookiedev.microwavetools.libs.MlinModel
import java.lang.Double
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Objects
import kotlin.Boolean
import kotlin.CharSequence
import kotlin.Int

class MlinFragment : Fragment() {
    private var mContext: Context? = null
    private var viewRoot: View? = null
    private var radioButtonW: RadioButton? = null
    private var radioButtonH: RadioButton? = null
    private var editTextW: TextInputEditText? = null
    private var editTextL: TextInputEditText? = null
    private var editTextZ0: TextInputEditText? = null
    private var editTextPhs: TextInputEditText? = null
    private var editTextFreq: TextInputEditText? = null
    private var editTextT: TextInputEditText? = null
    private var editTextH: TextInputEditText? = null
    private var editTextEr: TextInputEditText? = null
    private var textInputLayoutT: TextInputLayout? = null
    private var textInputLayoutH: TextInputLayout? = null
    private var textInputLayoutW: TextInputLayout? = null
    private var textInputLayoutZ0: TextInputLayout? = null
    private var textInputLayoutEr: TextInputLayout? = null
    private var textInputLayoutF: TextInputLayout? = null
    private var buttonSynthesize: Button? = null
    private var buttonAnalyze: Button? = null
    private var spinnerW: AutoCompleteTextView? = null
    private var spinnerL: AutoCompleteTextView? = null
    private var spinnerT: AutoCompleteTextView? = null
    private var spinnerH: AutoCompleteTextView? = null
    private var spinnerZ0: AutoCompleteTextView? = null
    private var spinnerPhs: AutoCompleteTextView? = null
    private var spinnerFreq: AutoCompleteTextView? = null
    private var line: MlinModel? = null
    private var target = 0
    private var defaultEditTextColor: ColorStateList? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    /**
     * Called to have the fragment instantiate its user interface view.
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
     * @return Return the View for the fragment's UI, or null.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewRoot = inflater.inflate(R.layout.fragment_mlin, container, false)
        mContext = this.getContext()

        initUI()
        readSharedPref() // read shared preferences
        setRadioBtn()

        buttonAnalyze!!.setOnClickListener(View.OnClickListener { view: View? ->
            Constants.refreshAnimation(
                mContext!!,
                viewRoot!!.findViewById<ImageView>(R.id.analyze_reveal)!!,
                Constants.ANALYZE
            )
            clearEditTextErrors()
            if (analysisInputEmptyCheck()) {
                line!!.setMetalWidth(
                    Objects.requireNonNull<Editable?>(editTextW!!.getText()).toString().toDouble(),
                    spinnerW!!.getText().toString()
                )
                line!!.setFrequency(
                    Objects.requireNonNull<Editable?>(editTextFreq!!.getText()).toString()
                        .toDouble(), spinnerFreq!!.getText().toString()
                )
                line!!.subEpsilon =
                    Objects.requireNonNull<Editable?>(editTextEr!!.getText()).toString().toDouble()
                line!!.setSubHeight(
                    Objects.requireNonNull<Editable?>(editTextH!!.getText()).toString().toDouble(),
                    spinnerH!!.getText().toString()
                )
                line!!.setMetalThick(
                    Objects.requireNonNull<Editable?>(editTextT!!.getText()).toString().toDouble(),
                    spinnerT!!.getText().toString()
                )

                if (editTextL!!.length() != 0) {
                    line!!.setMetalLength(
                        Objects.requireNonNull<Editable?>(editTextL!!.getText()).toString()
                            .toDouble(), spinnerL!!.getText().toString()
                    )
                } else {
                    line!!.setMetalLength(0.0, spinnerL!!.getText().toString())
                }
                val mlin = MlinCalculator()
                line = mlin.getAnaResult(line!!)

                val Z0_temp = BigDecimal.valueOf(line!!.impedance)
                val Z0 = Z0_temp.setScale(Constants.DecimalLength, RoundingMode.HALF_UP).toDouble()
                editTextZ0!!.setText(Z0.toString())

                if (editTextL!!.length() != 0) {
                    val Eeff_temp = BigDecimal.valueOf(line!!.phase)
                    val Eeff =
                        Eeff_temp.setScale(Constants.DecimalLength, RoundingMode.HALF_UP).toDouble()
                    editTextPhs!!.setText(Eeff.toString())
                } else {
                    editTextPhs!!.setText("")
                }
            }
        })

        buttonSynthesize!!.setOnClickListener(View.OnClickListener { view: View? ->
            Constants.refreshAnimation(
                mContext!!,
                viewRoot!!.findViewById<ImageView>(R.id.synthesize_reveal)!!,
                Constants.SYNTHESIZE
            )
            clearEditTextErrors()
            if (synthesizeInputEmptyCheck()) {
                line!!.impedance =
                    Objects.requireNonNull<Editable?>(editTextZ0!!.getText()).toString().toDouble()
                line!!.setFrequency(
                    Objects.requireNonNull<Editable?>(editTextFreq!!.getText()).toString()
                        .toDouble(), spinnerFreq!!.getText().toString()
                )
                line!!.subEpsilon =
                    Objects.requireNonNull<Editable?>(editTextEr!!.getText()).toString().toDouble()
                line!!.setMetalThick(
                    Objects.requireNonNull<Editable?>(editTextT!!.getText()).toString().toDouble(),
                    spinnerT!!.getText().toString()
                )

                if (target == Constants.Synthesize_Width) {
                    line!!.setSubHeight(
                        Objects.requireNonNull<Editable?>(editTextH!!.getText()).toString()
                            .toDouble(), spinnerH!!.getText().toString()
                    )
                    line!!.setMetalWidth(0.0, "m")
                } else if (target == Constants.Synthesize_Height) {
                    line!!.setMetalWidth(
                        Objects.requireNonNull<Editable?>(editTextW!!.getText()).toString()
                            .toDouble(), spinnerW!!.getText().toString()
                    )
                    line!!.setSubHeight(0.0, "m")
                }

                if (editTextPhs!!.length() != 0) {
                    line!!.phase = Objects.requireNonNull<Editable?>(editTextPhs!!.getText()).toString()
                        .toDouble()
                } else {
                    line!!.phase = 0.0
                }
                val mlin = MlinCalculator()
                line = mlin.getSynResult(line!!, target)

                if (line!!.errorCode == Constants.ERROR.NO_ERROR) {
                    if (editTextPhs!!.length() != 0) {
                        val L_temp = BigDecimal.valueOf(
                            Constants.meter2others(
                                line!!.metalLength,
                                spinnerL!!.getText().toString()
                            )
                        )
                        val L = L_temp.setScale(Constants.DecimalLength, RoundingMode.HALF_UP)
                            .toDouble()
                        editTextL!!.setText(L.toString())
                    } else {
                        editTextL!!.setText("")
                    }

                    if (target == Constants.Synthesize_Width) {
                        if ((Double.isNaN(line!!.metalWidth) || Double.isInfinite(line!!.metalWidth))) {
                            editTextW!!.setText("")
                            textInputLayoutW!!.setError(getString(R.string.synthesize_failed))
                            editTextW!!.requestFocus()
                        } else {
                            val W_temp = BigDecimal.valueOf(
                                Constants.meter2others(
                                    line!!.metalWidth,
                                    spinnerW!!.getText().toString()
                                )
                            )
                            val W = W_temp.setScale(Constants.DecimalLength, RoundingMode.HALF_UP)
                                .toDouble()
                            editTextW!!.setText(W.toString())
                        }
                    } else if (target == Constants.Synthesize_Height) {
                        if ((Double.isNaN(line!!.subHeight) || Double.isInfinite(line!!.subHeight))) {
                            editTextH!!.setText("")
                            textInputLayoutH!!.setError(getString(R.string.synthesize_failed))
                            editTextH!!.requestFocus()
                        } else {
                            val H_temp = BigDecimal.valueOf(
                                Constants.meter2others(
                                    line!!.subHeight,
                                    spinnerH!!.getText().toString()
                                )
                            )
                            val H = H_temp.setScale(Constants.DecimalLength, RoundingMode.HALF_UP)
                                .toDouble()
                            editTextH!!.setText(H.toString())
                        }
                    }
                } else {
                    if (target == Constants.Synthesize_Width) {
                        editTextW!!.setText("")
                        textInputLayoutW!!.setError(getString(R.string.synthesize_failed))
                        editTextW!!.requestFocus()
                    } else if (target == Constants.Synthesize_Height) {
                        editTextH!!.setText("")
                        textInputLayoutH!!.setError(getString(R.string.synthesize_failed))
                        editTextH!!.requestFocus()
                    }
                }
            }
        })

        return viewRoot!!
    }

    /**
     * Initialize the user interface components.
     */
    private fun initUI() {
        line = MlinModel()

        radioButtonW = viewRoot!!.findViewById<RadioButton>(R.id.radioBtn_W)
        radioButtonW!!.setVisibility(View.VISIBLE)

        radioButtonH = viewRoot!!.findViewById<RadioButton>(R.id.radioBtn_H)
        radioButtonH!!.setVisibility(View.VISIBLE)

        val radioButtonL = viewRoot!!.findViewById<RadioButton>(R.id.radioBtn_L)
        radioButtonL.setVisibility(View.VISIBLE)
        radioButtonL.setChecked(true)

        val radioButtonZ0 = viewRoot!!.findViewById<RadioButton>(R.id.radioBtn_Z0)
        radioButtonZ0.setVisibility(View.VISIBLE)
        radioButtonZ0.setChecked(true)

        val radioButtonPhs = viewRoot!!.findViewById<RadioButton>(R.id.radioBtn_Phs)
        radioButtonPhs.setVisibility(View.VISIBLE)
        radioButtonPhs.setChecked(true)

        textInputLayoutW = viewRoot!!.findViewById<TextInputLayout>(R.id.text_input_layout_W)
        editTextW = viewRoot!!.findViewById<TextInputEditText>(R.id.editText_W)
        defaultEditTextColor = editTextW!!.getTextColors()

        editTextW!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                textInputLayoutW!!.setError("")
                textInputLayoutW!!.setErrorEnabled(false)
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        editTextL = viewRoot!!.findViewById<TextInputEditText>(R.id.editText_L)
        editTextL!!.setTextColor(ContextCompat.getColor(mContext!!, R.color.synthesizeColor))
        editTextL!!.setBackgroundTintList(
            AppCompatResources.getColorStateList(
                mContext!!,
                R.color.background_tint_synthesize
            )
        )

        textInputLayoutZ0 = viewRoot!!.findViewById<TextInputLayout>(R.id.text_input_layout_Z0)
        editTextZ0 = viewRoot!!.findViewById<TextInputEditText>(R.id.editText_Z0)
        editTextZ0!!.setTextColor(ContextCompat.getColor(mContext!!, R.color.analyzeColor))
        editTextZ0!!.setBackgroundTintList(
            AppCompatResources.getColorStateList(
                mContext!!,
                R.color.background_tint_analyze
            )
        )
        editTextZ0!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                textInputLayoutZ0!!.setError("")
                textInputLayoutZ0!!.setErrorEnabled(false)
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        editTextPhs = viewRoot!!.findViewById<TextInputEditText>(R.id.editText_Phs)
        editTextPhs!!.setTextColor(ContextCompat.getColor(mContext!!, R.color.analyzeColor))
        editTextPhs!!.setBackgroundTintList(
            AppCompatResources.getColorStateList(
                mContext!!,
                R.color.background_tint_analyze
            )
        )

        textInputLayoutF = viewRoot!!.findViewById<TextInputLayout>(R.id.text_input_layout_Freq)
        editTextFreq = viewRoot!!.findViewById<TextInputEditText>(R.id.editText_Freq)
        editTextFreq!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                textInputLayoutF!!.setError("")
                textInputLayoutF!!.setErrorEnabled(false)
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        textInputLayoutT = viewRoot!!.findViewById<TextInputLayout>(R.id.text_input_layout_T)
        editTextT = viewRoot!!.findViewById<TextInputEditText>(R.id.editText_T)
        editTextT!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                textInputLayoutT!!.setError("")
                textInputLayoutT!!.setErrorEnabled(false)
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        textInputLayoutH = viewRoot!!.findViewById<TextInputLayout>(R.id.text_input_layout_H)
        editTextH = viewRoot!!.findViewById<TextInputEditText>(R.id.editText_H)
        editTextH!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                textInputLayoutH!!.setError("")
                textInputLayoutH!!.setErrorEnabled(false)
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        textInputLayoutEr = viewRoot!!.findViewById<TextInputLayout>(R.id.text_input_layout_er)
        editTextEr = viewRoot!!.findViewById<TextInputEditText>(R.id.editText_er)
        editTextEr!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                textInputLayoutEr!!.setError("")
                textInputLayoutEr!!.setErrorEnabled(false)
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        buttonAnalyze = viewRoot!!.findViewById<Button>(R.id.button_ana)
        buttonSynthesize = viewRoot!!.findViewById<Button>(R.id.button_syn)

        spinnerW = viewRoot!!.findViewById<AutoCompleteTextView>(R.id.menu_W)
        spinnerL = viewRoot!!.findViewById<AutoCompleteTextView>(R.id.menu_L)
        spinnerZ0 = viewRoot!!.findViewById<AutoCompleteTextView>(R.id.menu_Z0)
        spinnerPhs = viewRoot!!.findViewById<AutoCompleteTextView>(R.id.menu_Phs)
        spinnerFreq = viewRoot!!.findViewById<AutoCompleteTextView>(R.id.menu_Freq)
        spinnerT = viewRoot!!.findViewById<AutoCompleteTextView>(R.id.menu_T)
        spinnerH = viewRoot!!.findViewById<AutoCompleteTextView>(R.id.menu_H)

        spinnerW!!.setAdapter<ArrayAdapter<CharSequence?>?>(Constants.adapterDimensionUnits(mContext!!))
        spinnerL!!.setAdapter<ArrayAdapter<CharSequence?>?>(Constants.adapterDimensionUnits(mContext!!))
        spinnerT!!.setAdapter<ArrayAdapter<CharSequence?>?>(Constants.adapterDimensionUnits(mContext!!))
        spinnerH!!.setAdapter<ArrayAdapter<CharSequence?>?>(Constants.adapterDimensionUnits(mContext!!))

        spinnerZ0!!.setAdapter<ArrayAdapter<CharSequence?>?>(
            Constants.adapterImpedanceUnits(
                mContext!!
            )
        )

        spinnerPhs!!.setAdapter<ArrayAdapter<CharSequence?>?>(Constants.adapterPhaseUnits(mContext!!))

        spinnerFreq!!.setAdapter<ArrayAdapter<CharSequence?>?>(
            Constants.adapterFrequencyUnits(
                mContext!!
            )
        )
    }

    /**
     * Read shared preferences and set the values to the corresponding UI components.
     */
    private fun readSharedPref() {
        val prefs = mContext!!.getSharedPreferences(
            Constants.SHARED_PREFS_NAME,
            AppCompatActivity.MODE_PRIVATE
        )

        editTextW!!.setText(prefs.getString(Constants.MLIN_W, "2.9"))
        spinnerW!!.setText(
            Constants.validateUnit(
                Constants.adapterDimensionUnits(mContext!!), prefs.getString(
                    Constants.MLIN_W_UNIT, Constants.LengthUnit_mm
                )!!
            ), false
        )

        editTextL!!.setText(prefs.getString(Constants.MLIN_L, "40"))
        spinnerL!!.setText(
            Constants.validateUnit(
                Constants.adapterDimensionUnits(mContext!!), prefs.getString(
                    Constants.MLIN_L_UNIT, Constants.LengthUnit_mm
                )!!
            ), false
        )

        editTextZ0!!.setText(prefs.getString(Constants.MLIN_Z0, "50.0"))
        spinnerZ0!!.setText(
            Constants.validateUnit(
                Constants.adapterImpedanceUnits(mContext!!), prefs.getString(
                    Constants.MLIN_Z0_UNIT, Constants.ImpedanceUnit_Ohm
                )!!
            ), false
        )

        editTextPhs!!.setText(prefs.getString(Constants.MLIN_PHS, "90"))
        spinnerPhs!!.setText(
            Constants.validateUnit(
                Constants.adapterPhaseUnits(mContext!!), prefs.getString(
                    Constants.MLIN_PHS_UNIT, Constants.PhaseUnit_Degree
                )!!
            ), false
        )

        editTextFreq!!.setText(prefs.getString(Constants.MLIN_FREQ, "1.00"))
        spinnerFreq!!.setText(
            Constants.validateUnit(
                Constants.adapterFrequencyUnits(mContext!!), prefs.getString(
                    Constants.MLIN_FREQ_UNIT, Constants.FreqUnit_GHz
                )!!
            ), false
        )

        editTextEr!!.setText(prefs.getString(Constants.MLIN_ER, "4.6"))

        editTextH!!.setText(prefs.getString(Constants.MLIN_H, "1.6"))
        spinnerH!!.setText(
            Constants.validateUnit(
                Constants.adapterDimensionUnits(mContext!!), prefs.getString(
                    Constants.MLIN_H_UNIT, Constants.LengthUnit_mm
                )!!
            ), false
        )

        editTextT!!.setText(prefs.getString(Constants.MLIN_T, "0.035"))
        spinnerT!!.setText(
            Constants.validateUnit(
                Constants.adapterDimensionUnits(mContext!!), prefs.getString(
                    Constants.MLIN_T_UNIT, Constants.LengthUnit_mm
                )!!
            ), false
        )

        target = prefs.getString(
            com.rookiedev.microwavetools.libs.Constants.MLIN_TARGET,
            com.rookiedev.microwavetools.libs.Constants.Synthesize_Width.toString()
        )!!.toInt()
    }

    /**
     * Set the radio buttons for selecting the target parameter for synthesis.
     */
    private fun setRadioBtn() {
        if (target == Constants.Synthesize_Width) {
            radioButtonW!!.setChecked(true)
            editTextW!!.setBackgroundTintList(
                AppCompatResources.getColorStateList(
                    mContext!!,
                    R.color.background_tint_synthesize
                )
            )
            editTextW!!.setTextColor(ContextCompat.getColor(mContext!!, R.color.synthesizeColor))
            radioButtonH!!.setChecked(false)
            editTextH!!.setBackgroundTintList(
                AppCompatResources.getColorStateList(
                    mContext!!,
                    R.color.background_tint_default_synthesize
                )
            )
            editTextH!!.setTextColor(defaultEditTextColor)
        } else {
            target = Constants.Synthesize_Height
            radioButtonW!!.setChecked(false)
            editTextW!!.setBackgroundTintList(
                AppCompatResources.getColorStateList(
                    mContext!!,
                    R.color.background_tint_default_synthesize
                )
            )
            editTextW!!.setTextColor(defaultEditTextColor)
            radioButtonH!!.setChecked(true)
            editTextH!!.setBackgroundTintList(
                AppCompatResources.getColorStateList(
                    mContext!!,
                    R.color.background_tint_synthesize
                )
            )
            editTextH!!.setTextColor(ContextCompat.getColor(mContext!!, R.color.synthesizeColor))
        }

        radioButtonW!!.setOnClickListener(View.OnClickListener { arg0: View? ->
            radioButtonW!!.setChecked(true)
            editTextW!!.setBackgroundTintList(
                AppCompatResources.getColorStateList(
                    mContext!!,
                    R.color.background_tint_synthesize
                )
            )
            editTextW!!.setTextColor(ContextCompat.getColor(mContext!!, R.color.synthesizeColor))
            radioButtonH!!.setChecked(false)
            editTextH!!.setBackgroundTintList(
                AppCompatResources.getColorStateList(
                    mContext!!,
                    R.color.background_tint_default_synthesize
                )
            )
            editTextH!!.setTextColor(defaultEditTextColor)
            target = Constants.Synthesize_Width
        })
        radioButtonH!!.setOnClickListener(View.OnClickListener { arg0: View? ->
            radioButtonW!!.setChecked(false)
            editTextW!!.setBackgroundTintList(
                AppCompatResources.getColorStateList(
                    mContext!!,
                    R.color.background_tint_default_synthesize
                )
            )
            editTextW!!.setTextColor(defaultEditTextColor)
            radioButtonH!!.setChecked(true)
            editTextH!!.setBackgroundTintList(
                AppCompatResources.getColorStateList(
                    mContext!!,
                    R.color.background_tint_synthesize
                )
            )
            editTextH!!.setTextColor(ContextCompat.getColor(mContext!!, R.color.synthesizeColor))
            target = Constants.Synthesize_Height
        })
    }

    override fun onStop() {
        super.onStop()

        val prefs = mContext!!.getSharedPreferences(
            Constants.SHARED_PREFS_NAME,
            AppCompatActivity.MODE_PRIVATE
        )
        val editor = prefs.edit()

        editor.putString(
            Constants.MLIN_W,
            Objects.requireNonNull<Editable?>(editTextW!!.getText()).toString()
        )
        editor.putString(Constants.MLIN_W_UNIT, spinnerW!!.getText().toString())
        editor.putString(
            Constants.MLIN_L,
            Objects.requireNonNull<Editable?>(editTextL!!.getText()).toString()
        )
        editor.putString(Constants.MLIN_L_UNIT, spinnerL!!.getText().toString())
        editor.putString(
            Constants.MLIN_Z0,
            Objects.requireNonNull<Editable?>(editTextZ0!!.getText()).toString()
        )
        editor.putString(Constants.MLIN_Z0_UNIT, spinnerZ0!!.getText().toString())
        editor.putString(
            Constants.MLIN_PHS,
            Objects.requireNonNull<Editable?>(editTextPhs!!.getText()).toString()
        )
        editor.putString(Constants.MLIN_PHS_UNIT, spinnerPhs!!.getText().toString())
        editor.putString(
            Constants.MLIN_FREQ,
            Objects.requireNonNull<Editable?>(editTextFreq!!.getText()).toString()
        )
        editor.putString(Constants.MLIN_FREQ_UNIT, spinnerFreq!!.getText().toString())
        editor.putString(
            Constants.MLIN_ER,
            Objects.requireNonNull<Editable?>(editTextEr!!.getText()).toString()
        )
        editor.putString(
            Constants.MLIN_H,
            Objects.requireNonNull<Editable?>(editTextH!!.getText()).toString()
        )
        editor.putString(Constants.MLIN_H_UNIT, spinnerH!!.getText().toString())
        editor.putString(
            Constants.MLIN_T,
            Objects.requireNonNull<Editable?>(editTextT!!.getText()).toString()
        )
        editor.putString(Constants.MLIN_T_UNIT, spinnerT!!.getText().toString())
        editor.putString(Constants.MLIN_TARGET, target.toString())
        editor.apply()
    }

    /**
     * Check if the input fields for analysis are empty or contain invalid values.
     * @return true if all inputs are valid, false otherwise.
     */
    private fun analysisInputEmptyCheck(): Boolean {
        var checkResult = true
        if (editTextT!!.length() == 0) {
            textInputLayoutT!!.setError(getText(R.string.Error_T_empty))
            checkResult = false
        }
        if (editTextH!!.length() == 0) {
            textInputLayoutH!!.setError(getText(R.string.Error_H_empty))
            checkResult = false
        } else if (Constants.value2meter(
                Objects.requireNonNull<Editable?>(editTextH!!.getText()).toString().toDouble(),
                spinnerH!!.getText().toString()
            ) < Constants.MINI_LIMIT
        ) {
            textInputLayoutH!!.setError(getText(R.string.unreasonable_value))
            checkResult = false
        }
        if (editTextW!!.length() == 0) {
            textInputLayoutW!!.setError(getText(R.string.Error_W_empty))
            checkResult = false
        } else if (Constants.value2meter(
                Objects.requireNonNull<Editable?>(editTextW!!.getText()).toString().toDouble(),
                spinnerW!!.getText().toString()
            ) < Constants.MINI_LIMIT
        ) {
            textInputLayoutW!!.setError(getText(R.string.unreasonable_value))
            checkResult = false
        }
        if (editTextEr!!.length() == 0) {
            textInputLayoutEr!!.setError(getText(R.string.error_er_empty))
            checkResult = false
        } else if (Objects.requireNonNull<Editable?>(editTextEr!!.getText()).toString()
                .toDouble() < 1
        ) {
            textInputLayoutEr!!.setError(getText(R.string.unreasonable_value))
            checkResult = false
        }
        if (editTextFreq!!.length() == 0) {
            textInputLayoutF!!.setError(getText(R.string.Error_Freq_empty))
            checkResult = false
        } else if (Objects.requireNonNull<Editable?>(editTextFreq!!.getText()).toString()
                .toDouble() == 0.0
        ) {
            textInputLayoutF!!.setError(getText(R.string.error_zero_frequency))
            checkResult = false
        }

        if (!checkResult) {
            editTextZ0!!.setText("")
            editTextPhs!!.setText("")
        }

        return checkResult
    }

    /**
     * Check if the input fields for synthesis are empty or contain invalid values.
     * @return true if all inputs are valid, false otherwise.
     */
    private fun synthesizeInputEmptyCheck(): Boolean {
        var checkResult = true
        if (editTextZ0!!.length() == 0) {
            textInputLayoutZ0!!.setError(getText(R.string.error_Z0_empty))
            checkResult = false
        } else if (Objects.requireNonNull<Editable?>(editTextZ0!!.getText()).toString()
                .toDouble() == 0.0
        ) {
            textInputLayoutZ0!!.setError(getText(R.string.unreasonable_value))
            checkResult = false
        }

        if (editTextFreq!!.length() == 0) {
            textInputLayoutF!!.setError(getText(R.string.Error_Freq_empty))
            checkResult = false
        } else if (Objects.requireNonNull<Editable?>(editTextFreq!!.getText()).toString()
                .toDouble() == 0.0
        ) {
            textInputLayoutF!!.setError(getText(R.string.error_zero_frequency))
            checkResult = false
        }

        if (editTextT!!.length() == 0) {
            textInputLayoutT!!.setError(getText(R.string.Error_T_empty))
            checkResult = false
        }

        if (editTextEr!!.length() == 0) {
            textInputLayoutEr!!.setError(getText(R.string.error_er_empty))
            checkResult = false
        } else if (Objects.requireNonNull<Editable?>(editTextEr!!.getText()).toString()
                .toDouble() < 1
        ) {
            textInputLayoutEr!!.setError(getText(R.string.unreasonable_value))
            checkResult = false
        }

        if (target == Constants.Synthesize_Width) {
            if (editTextH!!.length() == 0) {
                textInputLayoutH!!.setError(getText(R.string.Error_H_empty))
                checkResult = false
            }
            if (!checkResult) {
                editTextW!!.setText("")
            }
        } else if (target == Constants.Synthesize_Height) {
            if (editTextW!!.length() == 0) {
                textInputLayoutW!!.setError(getText(R.string.Error_W_empty))
                checkResult = false
            }
            if (!checkResult) {
                editTextH!!.setText("")
            }
        }

        return checkResult
    }

    /**
     * Clear all error messages from the input fields.
     */
    private fun clearEditTextErrors() {
        textInputLayoutW!!.setError(null)
        textInputLayoutW!!.setErrorEnabled(false)
        textInputLayoutZ0!!.setError(null)
        textInputLayoutZ0!!.setErrorEnabled(false)
        textInputLayoutH!!.setError(null)
        textInputLayoutH!!.setErrorEnabled(false)
        textInputLayoutEr!!.setError(null)
        textInputLayoutEr!!.setErrorEnabled(false)
        textInputLayoutF!!.setError(null)
        textInputLayoutF!!.setErrorEnabled(false)
    }

    /**
     * Reset all input fields to their default values.
     */
    fun resetValues() {
        editTextW!!.setText("2.9")
        spinnerW!!.setText(Constants.LengthUnit_mm)
        editTextL!!.setText("40")
        spinnerL!!.setText(Constants.LengthUnit_mm)
        editTextZ0!!.setText("50.0")
        spinnerZ0!!.setText(Constants.ImpedanceUnit_Ohm)
        editTextPhs!!.setText("90")
        spinnerPhs!!.setText(Constants.PhaseUnit_Degree)
        editTextFreq!!.setText("1.00")
        spinnerFreq!!.setText(Constants.FreqUnit_GHz)
        editTextEr!!.setText("4.6")
        editTextH!!.setText("1.6")
        spinnerH!!.setText(Constants.LengthUnit_mm)
        editTextT!!.setText("0.035")
        spinnerT!!.setText(Constants.LengthUnit_mm)
        target = Constants.Synthesize_Width

        radioButtonW!!.setChecked(true)
        editTextW!!.setBackgroundTintList(
            AppCompatResources.getColorStateList(
                mContext!!,
                R.color.background_tint_synthesize
            )
        )
        editTextW!!.setTextColor(ContextCompat.getColor(mContext!!, R.color.synthesizeColor))
        radioButtonH!!.setChecked(false)
        editTextH!!.setBackgroundTintList(
            AppCompatResources.getColorStateList(
                mContext!!,
                R.color.background_tint_default_synthesize
            )
        )
        editTextH!!.setTextColor(defaultEditTextColor)
    }
}
