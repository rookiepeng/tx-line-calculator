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
import com.rookiedev.microwavetools.libs.CoaxCalculator
import com.rookiedev.microwavetools.libs.CoaxModel
import com.rookiedev.microwavetools.libs.Constants
import java.lang.Double
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Objects
import kotlin.Boolean
import kotlin.CharSequence
import kotlin.Int
import kotlin.toString

class CoaxFragment : Fragment() {
    private var mContext: Context? = null
    private var viewRoot: View? = null
    private var editTextA: TextInputEditText? = null
    private var editTextB: TextInputEditText? = null
    private var editTextC: TextInputEditText? = null
    private var editTextL: TextInputEditText? = null
    private var editTextZ0: TextInputEditText? = null
    private var editTextPhs: TextInputEditText? = null
    private var editTextFreq: TextInputEditText? = null
    private var editTextEr: TextInputEditText? = null
    private var textInputLayoutA: TextInputLayout? = null
    private var textInputLayoutB: TextInputLayout? = null
    private var textInputLayoutC: TextInputLayout? = null
    private var textInputLayoutZ0: TextInputLayout? = null
    private var textInputLayoutEr: TextInputLayout? = null
    private var textInputLayoutF: TextInputLayout? = null
    private var buttonSynthesize: Button? = null
    private var buttonAnalyze: Button? = null
    private var spinnerA: AutoCompleteTextView? = null
    private var spinnerB: AutoCompleteTextView? = null
    private var spinnerC: AutoCompleteTextView? = null
    private var spinnerL: AutoCompleteTextView? = null
    private var spinnerZ0: AutoCompleteTextView? = null
    private var spinnerPhs: AutoCompleteTextView? = null
    private var spinnerFreq: AutoCompleteTextView? = null
    private var target = 0
    private var radioButtonA: RadioButton? = null
    private var radioButtonB: RadioButton? = null
    private var radioButtonC: RadioButton? = null
    private var line: CoaxModel? = null
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
        viewRoot = inflater.inflate(R.layout.fragment_coax, container, false)
        mContext = this.getContext()

        initUI()
        readSharedPref()
        setRadioBtn()

        buttonAnalyze!!.setOnClickListener(View.OnClickListener { view: View? ->
            Constants.refreshAnimation(
                mContext!!,
                viewRoot!!.findViewById(R.id.analyze_reveal),
                Constants.ANALYZE
            )
            clearEditTextErrors()
            if (analysisInputCheck()) {
                line!!.setCoreRadius(
                    Objects.requireNonNull<Editable?>(editTextA!!.getText()).toString().toDouble(),
                    spinnerA!!.getText().toString()
                )
                line!!.setSubRadius(
                    Objects.requireNonNull<Editable?>(editTextB!!.getText()).toString().toDouble(),
                    spinnerB!!.getText().toString()
                )
                line!!.setCoreOffset(
                    Objects.requireNonNull<Editable?>(editTextC!!.getText()).toString().toDouble(),
                    spinnerC!!.getText().toString()
                )
                line!!.setFrequency(
                    Objects.requireNonNull<Editable?>(editTextFreq!!.getText()).toString()
                        .toDouble(), spinnerFreq!!.getText().toString()
                )
                line!!.subEpsilon =
                    Objects.requireNonNull<Editable?>(editTextEr!!.getText()).toString().toDouble()

                if (Objects.requireNonNull<Editable?>(editTextL!!.getText()).toString() != "") {
                    line!!.setMetalLength(
                        editTextL!!.getText().toString().toDouble(),
                        spinnerL!!.getText().toString()
                    )
                } else {
                    line!!.setMetalLength(0.0, spinnerL!!.getText().toString())
                }

                val coax = CoaxCalculator()
                line = coax.getAnaResult(line!!)

                if (line!!.errorCode == Constants.ERROR.NO_ERROR) {
                    if (editTextL!!.getText().toString() != "") {
                        val Eeff_temp = BigDecimal.valueOf(line!!.phase)
                        val Eeff = Eeff_temp.setScale(Constants.DecimalLength, RoundingMode.HALF_UP)
                            .toDouble()
                        editTextPhs!!.setText(Eeff.toString())
                    } else {
                        editTextPhs!!.setText("")
                    }

                    val Z0_temp = BigDecimal.valueOf(line!!.impedance)
                    val Z0 =
                        Z0_temp.setScale(Constants.DecimalLength, RoundingMode.HALF_UP).toDouble()
                    editTextZ0!!.setText(Z0.toString())
                } else if (line!!.errorCode == Constants.ERROR.SUBSTRATE_TOO_LARGE) {
                    editTextZ0!!.setText("")
                    editTextPhs!!.setText("")
                    textInputLayoutA!!.setError(getString(R.string.substrate_too_large))
                    editTextA!!.requestFocus()
                    textInputLayoutB!!.setError(getString(R.string.substrate_too_large))
                } else if (line!!.errorCode == Constants.ERROR.OFFSET_TOO_LARGE) {
                    editTextZ0!!.setText("")
                    editTextPhs!!.setText("")
                    textInputLayoutC!!.setError(getString(R.string.offset_too_large))
                    editTextC!!.requestFocus()
                }
            }
        })

        buttonSynthesize!!.setOnClickListener(View.OnClickListener { view: View? ->
            Constants.refreshAnimation(
                mContext!!,
                viewRoot!!.findViewById(R.id.synthesize_reveal),
                Constants.SYNTHESIZE
            )
            clearEditTextErrors()
            if (synthesizeInputCheck()) {
                line!!.impedance =
                    Objects.requireNonNull<Editable?>(editTextZ0!!.getText()).toString().toDouble()
                line!!.setFrequency(
                    Objects.requireNonNull<Editable?>(editTextFreq!!.getText()).toString()
                        .toDouble(), spinnerFreq!!.getText().toString()
                )
                line!!.subEpsilon =
                    Objects.requireNonNull<Editable?>(editTextEr!!.getText()).toString().toDouble()

                if (target == Constants.Synthesize_CoreRadius) { // a = 0;
                    line!!.setSubRadius(
                        Objects.requireNonNull<Editable?>(editTextB!!.getText()).toString()
                            .toDouble(), spinnerB!!.getText().toString()
                    )
                    line!!.setCoreOffset(
                        Objects.requireNonNull<Editable?>(editTextC!!.getText()).toString()
                            .toDouble(), spinnerC!!.getText().toString()
                    )
                } else if (target == Constants.Synthesize_SubRadius) { // b = 0;
                    line!!.setCoreRadius(
                        Objects.requireNonNull<Editable?>(editTextA!!.getText()).toString()
                            .toDouble(), spinnerA!!.getText().toString()
                    )
                    line!!.setCoreOffset(
                        Objects.requireNonNull<Editable?>(editTextC!!.getText()).toString()
                            .toDouble(), spinnerC!!.getText().toString()
                    )
                } else if (target == Constants.Synthesize_CoreOffset) { // c = 0;
                    line!!.setCoreRadius(
                        Objects.requireNonNull<Editable?>(editTextA!!.getText()).toString()
                            .toDouble(), spinnerA!!.getText().toString()
                    )
                    line!!.setSubRadius(
                        Objects.requireNonNull<Editable?>(editTextB!!.getText()).toString()
                            .toDouble(), spinnerB!!.getText().toString()
                    )
                }

                if (editTextPhs!!.length() != 0) {
                    line!!.phase = Objects.requireNonNull<Editable?>(editTextPhs!!.getText()).toString()
                        .toDouble()
                } else {
                    line!!.phase = 0.0
                }

                val coax = CoaxCalculator()
                line = coax.getSynResult(line!!, target)

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

                    if (target == Constants.Synthesize_CoreRadius) {
                        if ((Double.isNaN(line!!.coreRadius) || Double.isInfinite(line!!.coreRadius))) {
                            editTextA!!.setText("")
                            textInputLayoutA!!.setError(getString(R.string.synthesize_failed))
                            editTextA!!.requestFocus()
                        } else {
                            val a_temp = BigDecimal.valueOf(
                                Constants.meter2others(
                                    line!!.coreRadius,
                                    spinnerA!!.getText().toString()
                                )
                            )
                            val a = a_temp.setScale(Constants.DecimalLength, RoundingMode.HALF_UP)
                                .toDouble()
                            editTextA!!.setText(a.toString())
                        }
                    } else if (target == Constants.Synthesize_SubRadius) {
                        if ((Double.isNaN(line!!.subRadius) || Double.isInfinite(line!!.subRadius))) {
                            editTextB!!.setText("")
                            textInputLayoutB!!.setError(getString(R.string.synthesize_failed))
                            editTextB!!.requestFocus()
                        } else {
                            val b_temp = BigDecimal.valueOf(
                                Constants.meter2others(
                                    line!!.subRadius,
                                    spinnerB!!.getText().toString()
                                )
                            )
                            val height =
                                b_temp.setScale(Constants.DecimalLength, RoundingMode.HALF_UP)
                                    .toDouble()
                            editTextB!!.setText(height.toString())
                        }
                    } else if (target == Constants.Synthesize_CoreOffset) {
                        if ((Double.isNaN(line!!.coreOffset) || Double.isInfinite(line!!.coreOffset))) {
                            editTextC!!.setText("")
                            textInputLayoutC!!.setError(getString(R.string.synthesize_failed))
                            editTextC!!.requestFocus()
                        } else {
                            val c_temp = BigDecimal.valueOf(
                                Constants.meter2others(
                                    line!!.coreOffset,
                                    spinnerC!!.getText().toString()
                                )
                            )
                            val b = c_temp.setScale(Constants.DecimalLength, RoundingMode.HALF_UP)
                                .toDouble()
                            editTextC!!.setText(b.toString())
                        }
                    }
                } else {
                    if (target == Constants.Synthesize_CoreRadius) {
                        editTextA!!.setText("")
                        textInputLayoutA!!.setError(getString(R.string.synthesize_failed))
                        editTextA!!.requestFocus()
                    } else if (target == Constants.Synthesize_SubRadius) {
                        editTextB!!.setText("")
                        textInputLayoutB!!.setError(getString(R.string.synthesize_failed))
                        editTextB!!.requestFocus()
                    } else if (target == Constants.Synthesize_CoreOffset) {
                        editTextC!!.setText("")
                        textInputLayoutC!!.setError(getString(R.string.synthesize_failed))
                        editTextC!!.requestFocus()
                    }
                }
            }
        })

        return viewRoot!!
    }

    /**
     * Initialize the UI components.
     */
    private fun initUI() {
        line = CoaxModel()

        val radioButtonL = viewRoot!!.findViewById<RadioButton>(R.id.radioBtn_L)
        radioButtonL.setVisibility(View.VISIBLE)
        radioButtonL.setChecked(true)

        val radioButtonPhs = viewRoot!!.findViewById<RadioButton>(R.id.radioBtn_Phs)
        radioButtonPhs.setVisibility(View.VISIBLE)
        radioButtonPhs.setChecked(true)

        val radioButtonZ0 = viewRoot!!.findViewById<RadioButton>(R.id.radioBtn_Z0)
        radioButtonZ0.setVisibility(View.VISIBLE)
        radioButtonZ0.setChecked(true)

        radioButtonA = viewRoot!!.findViewById<RadioButton>(R.id.radioBtn_a)
        radioButtonA!!.setVisibility(View.VISIBLE)
        radioButtonC = viewRoot!!.findViewById<RadioButton>(R.id.radioBtn_c)
        radioButtonC!!.setVisibility(View.VISIBLE)
        radioButtonB = viewRoot!!.findViewById<RadioButton>(R.id.radio_button_b)
        radioButtonB!!.setVisibility(View.VISIBLE)

        textInputLayoutA = viewRoot!!.findViewById<TextInputLayout>(R.id.text_input_layout_a)
        editTextA = viewRoot!!.findViewById<TextInputEditText>(R.id.editText_a)
        defaultEditTextColor = editTextA!!.getTextColors()
        editTextA!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                textInputLayoutA!!.setError("")
                textInputLayoutA!!.setErrorEnabled(false)
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        textInputLayoutC = viewRoot!!.findViewById<TextInputLayout>(R.id.text_input_layout_c)
        editTextC = viewRoot!!.findViewById<TextInputEditText>(R.id.editText_c)
        editTextC!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                textInputLayoutC!!.setError("")
                textInputLayoutC!!.setErrorEnabled(false)
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        textInputLayoutB = viewRoot!!.findViewById<TextInputLayout>(R.id.text_input_layout_b)
        editTextB = viewRoot!!.findViewById<TextInputEditText>(R.id.editText_b)
        editTextB!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                textInputLayoutB!!.setError("")
                textInputLayoutB!!.setErrorEnabled(false)
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

        // button elements
        buttonAnalyze = viewRoot!!.findViewById<Button>(R.id.button_ana)
        buttonSynthesize = viewRoot!!.findViewById<Button>(R.id.button_syn)

        // spinner elements
        spinnerA = viewRoot!!.findViewById<AutoCompleteTextView>(R.id.menu_a)
        spinnerC = viewRoot!!.findViewById<AutoCompleteTextView>(R.id.menu_c)
        spinnerB = viewRoot!!.findViewById<AutoCompleteTextView>(R.id.menu_b)
        spinnerL = viewRoot!!.findViewById<AutoCompleteTextView>(R.id.menu_L)
        spinnerZ0 = viewRoot!!.findViewById<AutoCompleteTextView>(R.id.menu_Z0)
        spinnerPhs = viewRoot!!.findViewById<AutoCompleteTextView>(R.id.menu_Phs)
        spinnerFreq = viewRoot!!.findViewById<AutoCompleteTextView>(R.id.menu_Freq)

        // configure the length units
        spinnerA!!.setAdapter<ArrayAdapter<CharSequence?>?>(Constants.adapterDimensionUnits(mContext!!))
        spinnerC!!.setAdapter<ArrayAdapter<CharSequence?>?>(Constants.adapterDimensionUnits(mContext!!))
        spinnerB!!.setAdapter<ArrayAdapter<CharSequence?>?>(Constants.adapterDimensionUnits(mContext!!))
        spinnerL!!.setAdapter<ArrayAdapter<CharSequence?>?>(Constants.adapterDimensionUnits(mContext!!))

        // configure the impedance units
        spinnerZ0!!.setAdapter<ArrayAdapter<CharSequence?>?>(
            Constants.adapterImpedanceUnits(
                mContext!!
            )
        )

        // configure the electrical length units
        spinnerPhs!!.setAdapter<ArrayAdapter<CharSequence?>?>(Constants.adapterPhaseUnits(mContext!!))

        // configure the frequency units
        spinnerFreq!!.setAdapter<ArrayAdapter<CharSequence?>?>(
            Constants.adapterFrequencyUnits(
                mContext!!
            )
        )
    }

    /**
     * Read shared preferences and set the initial values for the UI components.
     */
    private fun readSharedPref() {
        val prefs = mContext!!.getSharedPreferences(
            Constants.SHARED_PREFS_NAME,
            AppCompatActivity.MODE_PRIVATE
        ) // get the header_parameters from the Shared

        editTextA!!.setText(prefs.getString(Constants.COAX_A, "0.167"))
        spinnerA!!.setText(
            Constants.validateUnit(
                Constants.adapterDimensionUnits(mContext!!), prefs.getString(
                    Constants.COAX_A_UNIT, Constants.LengthUnit_mm
                )!!
            ), false
        )

        editTextB!!.setText(prefs.getString(Constants.COAX_B, "1.00"))
        spinnerB!!.setText(
            Constants.validateUnit(
                Constants.adapterDimensionUnits(mContext!!), prefs.getString(
                    Constants.COAX_B_UNIT, Constants.LengthUnit_mm
                )!!
            ), false
        )

        editTextC!!.setText(prefs.getString(Constants.COAX_C, "0.00"))
        spinnerC!!.setText(
            Constants.validateUnit(
                Constants.adapterDimensionUnits(mContext!!), prefs.getString(
                    Constants.COAX_C_UNIT, Constants.LengthUnit_mm
                )!!
            ), false
        )

        editTextL!!.setText(prefs.getString(Constants.COAX_L, "34.945"))
        spinnerL!!.setText(
            Constants.validateUnit(
                Constants.adapterDimensionUnits(mContext!!), prefs.getString(
                    Constants.COAX_L_UNIT, Constants.LengthUnit_mm
                )!!
            ), false
        )

        editTextZ0!!.setText(prefs.getString(Constants.COAX_Z0, "50"))
        spinnerZ0!!.setText(
            Constants.validateUnit(
                Constants.adapterImpedanceUnits(mContext!!), prefs.getString(
                    Constants.COAX_Z0_UNIT, Constants.ImpedanceUnit_Ohm
                )!!
            ), false
        )

        editTextPhs!!.setText(prefs.getString(Constants.COAX_PHS, "90"))
        spinnerPhs!!.setText(
            Constants.validateUnit(
                Constants.adapterPhaseUnits(mContext!!), prefs.getString(
                    Constants.COAX_PHS_UNIT, Constants.PhaseUnit_Degree
                )!!
            ), false
        )

        editTextFreq!!.setText(prefs.getString(Constants.COAX_FREQ, "1.00"))
        spinnerFreq!!.setText(
            Constants.validateUnit(
                Constants.adapterFrequencyUnits(mContext!!), prefs.getString(
                    Constants.COAX_FREQ_UNIT, Constants.FreqUnit_GHz
                )!!
            ), false
        )

        editTextEr!!.setText(prefs.getString(Constants.COAX_ER, "4.6"))

        target = prefs.getString(
            com.rookiedev.microwavetools.libs.Constants.COAX_TARGET,
            com.rookiedev.microwavetools.libs.Constants.Synthesize_CoreRadius.toString()
        )!!.toInt()
    }

    /**
     * Set the radio buttons based on the target value.
     */
    private fun setRadioBtn() {
        if (target == Constants.Synthesize_CoreRadius) {
            radioButtonA!!.setChecked(true)
            editTextA!!.setBackgroundTintList(
                AppCompatResources.getColorStateList(
                    mContext!!,
                    R.color.background_tint_synthesize
                )
            )
            editTextA!!.setTextColor(ContextCompat.getColor(mContext!!, R.color.synthesizeColor))
            radioButtonB!!.setChecked(false)
            editTextB!!.setBackgroundTintList(
                AppCompatResources.getColorStateList(
                    mContext!!,
                    R.color.background_tint_default_synthesize
                )
            )
            editTextB!!.setTextColor(defaultEditTextColor)
            radioButtonC!!.setChecked(false)
            editTextC!!.setBackgroundTintList(
                AppCompatResources.getColorStateList(
                    mContext!!,
                    R.color.background_tint_default_synthesize
                )
            )
            editTextC!!.setTextColor(defaultEditTextColor)
        } else if (target == Constants.Synthesize_SubRadius) {
            radioButtonA!!.setChecked(false)
            editTextA!!.setBackgroundTintList(
                AppCompatResources.getColorStateList(
                    mContext!!,
                    R.color.background_tint_default_synthesize
                )
            )
            editTextA!!.setTextColor(defaultEditTextColor)
            radioButtonB!!.setChecked(true)
            editTextB!!.setBackgroundTintList(
                AppCompatResources.getColorStateList(
                    mContext!!,
                    R.color.background_tint_synthesize
                )
            )
            editTextB!!.setTextColor(ContextCompat.getColor(mContext!!, R.color.synthesizeColor))
            radioButtonC!!.setChecked(false)
            editTextC!!.setBackgroundTintList(
                AppCompatResources.getColorStateList(
                    mContext!!,
                    R.color.background_tint_default_synthesize
                )
            )
            editTextC!!.setTextColor(defaultEditTextColor)
        } else if (target == Constants.Synthesize_CoreOffset) {
            radioButtonA!!.setChecked(false)
            editTextA!!.setBackgroundTintList(
                AppCompatResources.getColorStateList(
                    mContext!!,
                    R.color.background_tint_default_synthesize
                )
            )
            editTextA!!.setTextColor(defaultEditTextColor)
            radioButtonB!!.setChecked(false)
            editTextB!!.setBackgroundTintList(
                AppCompatResources.getColorStateList(
                    mContext!!,
                    R.color.background_tint_default_synthesize
                )
            )
            editTextB!!.setTextColor(defaultEditTextColor)
            radioButtonC!!.setChecked(true)
            editTextC!!.setBackgroundTintList(
                AppCompatResources.getColorStateList(
                    mContext!!,
                    R.color.background_tint_synthesize
                )
            )
            editTextC!!.setTextColor(ContextCompat.getColor(mContext!!, R.color.synthesizeColor))
        } else {
            target = Constants.Synthesize_CoreRadius
            radioButtonA!!.setChecked(true)
            editTextA!!.setBackgroundTintList(
                AppCompatResources.getColorStateList(
                    mContext!!,
                    R.color.background_tint_synthesize
                )
            )
            editTextA!!.setTextColor(ContextCompat.getColor(mContext!!, R.color.synthesizeColor))
            radioButtonB!!.setChecked(false)
            editTextB!!.setBackgroundTintList(
                AppCompatResources.getColorStateList(
                    mContext!!,
                    R.color.background_tint_default_synthesize
                )
            )
            editTextB!!.setTextColor(defaultEditTextColor)
            radioButtonC!!.setChecked(false)
            editTextC!!.setBackgroundTintList(
                AppCompatResources.getColorStateList(
                    mContext!!,
                    R.color.background_tint_default_synthesize
                )
            )
            editTextC!!.setTextColor(defaultEditTextColor)
        }
        radioButtonA!!.setOnClickListener(View.OnClickListener { arg0: View? ->
            radioButtonA!!.setChecked(true)
            editTextA!!.setBackgroundTintList(
                AppCompatResources.getColorStateList(
                    mContext!!,
                    R.color.background_tint_synthesize
                )
            )
            editTextA!!.setTextColor(ContextCompat.getColor(mContext!!, R.color.synthesizeColor))
            radioButtonB!!.setChecked(false)
            editTextB!!.setBackgroundTintList(
                AppCompatResources.getColorStateList(
                    mContext!!,
                    R.color.background_tint_default_synthesize
                )
            )
            editTextB!!.setTextColor(defaultEditTextColor)
            radioButtonC!!.setChecked(false)
            editTextC!!.setBackgroundTintList(
                AppCompatResources.getColorStateList(
                    mContext!!,
                    R.color.background_tint_default_synthesize
                )
            )
            editTextC!!.setTextColor(defaultEditTextColor)
            target = Constants.Synthesize_CoreRadius
        })
        radioButtonB!!.setOnClickListener(View.OnClickListener { arg0: View? ->
            radioButtonA!!.setChecked(false)
            editTextA!!.setBackgroundTintList(
                AppCompatResources.getColorStateList(
                    mContext!!,
                    R.color.background_tint_default_synthesize
                )
            )
            editTextA!!.setTextColor(defaultEditTextColor)
            radioButtonB!!.setChecked(true)
            editTextB!!.setBackgroundTintList(
                AppCompatResources.getColorStateList(
                    mContext!!,
                    R.color.background_tint_synthesize
                )
            )
            editTextB!!.setTextColor(ContextCompat.getColor(mContext!!, R.color.synthesizeColor))
            radioButtonC!!.setChecked(false)
            editTextC!!.setBackgroundTintList(
                AppCompatResources.getColorStateList(
                    mContext!!,
                    R.color.background_tint_default_synthesize
                )
            )
            editTextC!!.setTextColor(defaultEditTextColor)
            target = Constants.Synthesize_Height
        })
        radioButtonC!!.setOnClickListener(View.OnClickListener { arg0: View? ->
            radioButtonA!!.setChecked(false)
            editTextA!!.setBackgroundTintList(
                AppCompatResources.getColorStateList(
                    mContext!!,
                    R.color.background_tint_default_synthesize
                )
            )
            editTextA!!.setTextColor(defaultEditTextColor)
            radioButtonB!!.setChecked(false)
            editTextB!!.setBackgroundTintList(
                AppCompatResources.getColorStateList(
                    mContext!!,
                    R.color.background_tint_default_synthesize
                )
            )
            editTextB!!.setTextColor(defaultEditTextColor)
            radioButtonC!!.setChecked(true)
            editTextC!!.setBackgroundTintList(
                AppCompatResources.getColorStateList(
                    mContext!!,
                    R.color.background_tint_synthesize
                )
            )
            editTextC!!.setTextColor(ContextCompat.getColor(mContext!!, R.color.synthesizeColor))
            target = Constants.Synthesize_CoreOffset
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
            Constants.COAX_A,
            Objects.requireNonNull<Editable?>(editTextA!!.getText()).toString()
        )
        editor.putString(Constants.COAX_A_UNIT, spinnerA!!.getText().toString())
        editor.putString(
            Constants.COAX_B,
            Objects.requireNonNull<Editable?>(editTextB!!.getText()).toString()
        )
        editor.putString(Constants.COAX_B_UNIT, spinnerB!!.getText().toString())
        editor.putString(
            Constants.COAX_C,
            Objects.requireNonNull<Editable?>(editTextC!!.getText()).toString()
        )
        editor.putString(Constants.COAX_C_UNIT, spinnerC!!.getText().toString())
        editor.putString(
            Constants.COAX_ER,
            Objects.requireNonNull<Editable?>(editTextEr!!.getText()).toString()
        )
        editor.putString(
            Constants.COAX_L,
            Objects.requireNonNull<Editable?>(editTextL!!.getText()).toString()
        )
        editor.putString(Constants.COAX_L_UNIT, spinnerL!!.getText().toString())
        editor.putString(
            Constants.COAX_Z0,
            Objects.requireNonNull<Editable?>(editTextZ0!!.getText()).toString()
        )
        editor.putString(Constants.COAX_Z0_UNIT, spinnerZ0!!.getText().toString())
        editor.putString(
            Constants.COAX_PHS,
            Objects.requireNonNull<Editable?>(editTextPhs!!.getText()).toString()
        )
        editor.putString(Constants.COAX_PHS_UNIT, spinnerPhs!!.getText().toString())
        editor.putString(
            Constants.COAX_FREQ,
            Objects.requireNonNull<Editable?>(editTextFreq!!.getText()).toString()
        )
        editor.putString(Constants.COAX_FREQ_UNIT, spinnerFreq!!.getText().toString())
        editor.putString(Constants.COAX_TARGET, target.toString())

        editor.apply()
    }

    /**
     * Check the input values for the analysis operation.
     * @return true if all inputs are valid, false otherwise.
     */
    private fun analysisInputCheck(): Boolean {
        var checkResult = true
        if (editTextA!!.length() == 0) {
            textInputLayoutA!!.setError(getText(R.string.Error_a_empty))
            checkResult = false
        } else if (Constants.value2meter(
                Objects.requireNonNull<Editable?>(editTextA!!.getText()).toString().toDouble(),
                spinnerA!!.getText().toString()
            ) < Constants.MINI_LIMIT
        ) {
            textInputLayoutA!!.setError(getText(R.string.unreasonable_value))
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

        if (editTextB!!.length() == 0) {
            textInputLayoutB!!.setError(getText(R.string.Error_b_empty))
            checkResult = false
        } else if (Constants.value2meter(
                Objects.requireNonNull<Editable?>(editTextB!!.getText()).toString().toDouble(),
                spinnerB!!.getText().toString()
            ) < Constants.MINI_LIMIT
        ) {
            textInputLayoutB!!.setError(getText(R.string.unreasonable_value))
            checkResult = false
        }

        if (editTextC!!.length() == 0) {
            textInputLayoutC!!.setError(getText(R.string.Error_c_empty))
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

        if (!checkResult) {
            editTextZ0!!.setText("")
            editTextPhs!!.setText("")
        }

        return checkResult
    }

    /**
     * Check the input values for the synthesis operation.
     * @return true if all inputs are valid, false otherwise.
     */
    private fun synthesizeInputCheck(): Boolean {
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

        if (editTextEr!!.length() == 0) {
            textInputLayoutEr!!.setError(getText(R.string.error_er_empty))
            checkResult = false
        } else if (Objects.requireNonNull<Editable?>(editTextEr!!.getText()).toString()
                .toDouble() < 1
        ) {
            textInputLayoutEr!!.setError(getText(R.string.unreasonable_value))
            checkResult = false
        }

        if (target == Constants.Synthesize_CoreRadius) {
            if (editTextB!!.length() == 0) {
                textInputLayoutB!!.setError(getText(R.string.Error_b_empty))
                checkResult = false
            }
            if (editTextC!!.length() == 0) {
                textInputLayoutC!!.setError(getText(R.string.Error_c_empty))
                checkResult = false
            }

            if (!checkResult) {
                editTextA!!.setText("")
            }
        } else if (target == Constants.Synthesize_SubRadius) {
            if (editTextA!!.length() == 0) {
                textInputLayoutA!!.setError(getText(R.string.Error_a_empty))
                checkResult = false
            }
            if (editTextC!!.length() == 0) {
                textInputLayoutC!!.setError(getText(R.string.Error_c_empty))
                checkResult = false
            }

            if (!checkResult) {
                editTextB!!.setText("")
            }
        } else if (target == Constants.Synthesize_CoreOffset) {
            if (editTextA!!.length() == 0) {
                textInputLayoutA!!.setError(getText(R.string.Error_a_empty))
                checkResult = false
            }
            if (editTextB!!.length() == 0) {
                textInputLayoutB!!.setError(getText(R.string.Error_b_empty))
                checkResult = false
            }

            if (!checkResult) {
                editTextC!!.setText("")
            }
        }

        return checkResult
    }

    /**
     * Clear all error messages from the input fields.
     */
    private fun clearEditTextErrors() {
        textInputLayoutA!!.setError(null)
        textInputLayoutA!!.setErrorEnabled(false)
        textInputLayoutB!!.setError(null)
        textInputLayoutB!!.setErrorEnabled(false)
        textInputLayoutC!!.setError(null)
        textInputLayoutC!!.setErrorEnabled(false)
        textInputLayoutZ0!!.setError(null)
        textInputLayoutZ0!!.setErrorEnabled(false)
        textInputLayoutEr!!.setError(null)
        textInputLayoutEr!!.setErrorEnabled(false)
        textInputLayoutF!!.setError(null)
        textInputLayoutF!!.setErrorEnabled(false)
    }

    /**
     * Reset all input fields to their default values.
     */
    fun resetValues() {
        editTextA!!.setText("0.167")
        spinnerA!!.setText(Constants.LengthUnit_mm)
        editTextB!!.setText("1.00")
        spinnerB!!.setText(Constants.LengthUnit_mm)
        editTextC!!.setText("0.00")
        spinnerC!!.setText(Constants.LengthUnit_mm)
        editTextL!!.setText("34.945")
        spinnerL!!.setText(Constants.LengthUnit_mm)
        editTextZ0!!.setText("50")
        spinnerZ0!!.setText(Constants.ImpedanceUnit_Ohm)
        editTextPhs!!.setText("90")
        spinnerPhs!!.setText(Constants.PhaseUnit_Degree)
        editTextFreq!!.setText("1.00")
        spinnerFreq!!.setText(Constants.FreqUnit_GHz)
        editTextEr!!.setText("4.6")
        target = Constants.Synthesize_CoreRadius

        radioButtonA!!.setChecked(true)
        editTextA!!.setBackgroundTintList(
            AppCompatResources.getColorStateList(
                mContext!!,
                R.color.background_tint_synthesize
            )
        )
        editTextA!!.setTextColor(ContextCompat.getColor(mContext!!, R.color.synthesizeColor))
        radioButtonB!!.setChecked(false)
        editTextB!!.setBackgroundTintList(
            AppCompatResources.getColorStateList(
                mContext!!,
                R.color.background_tint_default_synthesize
            )
        )
        editTextB!!.setTextColor(defaultEditTextColor)
        radioButtonC!!.setChecked(false)
        editTextC!!.setBackgroundTintList(
            AppCompatResources.getColorStateList(
                mContext!!,
                R.color.background_tint_default_synthesize
            )
        )
        editTextC!!.setTextColor(defaultEditTextColor)
    }
}
