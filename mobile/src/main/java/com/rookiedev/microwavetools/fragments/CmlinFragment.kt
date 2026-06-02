package com.rookiedev.microwavetools.fragments

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.rookiedev.microwavetools.R
import com.rookiedev.microwavetools.libs.CmlinCalculator
import com.rookiedev.microwavetools.libs.CmlinModel
import com.rookiedev.microwavetools.libs.Constants
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.Boolean
import kotlin.CharSequence
import kotlin.Int

class CmlinFragment : Fragment() {
    private val mContext: Context get() = requireContext()
    private var viewRoot: View? = null
    private var editTextW: TextInputEditText? = null
    private var editTextG: TextInputEditText? = null
    private var editTextL: TextInputEditText? = null
    private var editTextZ0: TextInputEditText? = null
    private var editTextK: TextInputEditText? = null
    private var editTextZ0o: TextInputEditText? = null
    private var editTextZ0e: TextInputEditText? = null
    private var editTextPhs: TextInputEditText? = null
    private var editTextFreq: TextInputEditText? = null
    private var editTextH: TextInputEditText? = null
    private var editTextEr: TextInputEditText? = null
    private var textInputLayoutH: TextInputLayout? = null
    private var textInputLayoutW: TextInputLayout? = null
    private var textInputLayoutG: TextInputLayout? = null
    private var textInputLayoutZ0: TextInputLayout? = null
    private var textInputLayoutK: TextInputLayout? = null
    private var textInputLayoutZ0o: TextInputLayout? = null
    private var textInputLayoutZ0e: TextInputLayout? = null
    private var textInputLayoutEr: TextInputLayout? = null
    private var textInputLayoutF: TextInputLayout? = null
    private lateinit var line: CmlinModel
    private var buttonSynthesize: Button? = null
    private var buttonAnalyze: Button? = null
    private var spinnerW: AutoCompleteTextView? = null
    private var spinnerG: AutoCompleteTextView? = null
    private var spinnerL: AutoCompleteTextView? = null
    private var spinnerT: AutoCompleteTextView? = null
    private var spinnerH: AutoCompleteTextView? = null
    private var spinnerZ0: AutoCompleteTextView? = null
    private var spinnerZ0o: AutoCompleteTextView? = null
    private var spinnerZ0e: AutoCompleteTextView? = null
    private var spinnerPhs: AutoCompleteTextView? = null
    private var spinnerFreq: AutoCompleteTextView? = null
    private var radioButtonZ0: RadioButton? = null
    private var radioButtonK: RadioButton? = null
    private var radioButtonZ0o: RadioButton? = null
    private var radioButtonZ0e: RadioButton? = null
    private var useZ0k = false // calculate with Z0, k, or Z0e, Z0o

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
        viewRoot = inflater.inflate(R.layout.fragment_cmlin, container, false)

        initUI()
        readSharedPref()
        setRadioBtn()

        buttonAnalyze!!.setOnClickListener(View.OnClickListener { view: View? ->
            Constants.refreshAnimation(
                mContext,
                viewRoot!!.findViewById<ImageView>(R.id.analyze_reveal)!!,
                Constants.ANALYZE
            )
            clearEditTextErrors()
            if (analysisInputCheck()) {
                line.setMetalWidth(
                    editTextW!!.text!!.toString().toDouble(),
                    spinnerW!!.text.toString()
                )
                line.setMetalSpace(
                    editTextG!!.text!!.toString().toDouble(),
                    spinnerG!!.text.toString()
                )
                line.setFrequency(
                    editTextFreq!!.text!!.toString().toDouble(), 
                    spinnerFreq!!.text.toString()
                )
                line.subEpsilon = editTextEr!!.text!!.toString().toDouble()
                line.setSubHeight(
                    editTextH!!.text!!.toString().toDouble(),
                    spinnerH!!.text.toString()
                )
                line.setMetalThick(0.0, spinnerT!!.text.toString())

                if (editTextL!!.length() != 0) {
                    line.setMetalLength(
                        editTextL!!.text!!.toString().toDouble(), 
                        spinnerL!!.text.toString()
                    )
                } else {
                    line.setMetalLength(0.0, spinnerL!!.text.toString())
                }

                val cmlin = CmlinCalculator()
                line = cmlin.getAnaResult(line)

                if (editTextL!!.length() != 0) {
                    val eeff_temp = BigDecimal.valueOf(line.phase)
                    val eeff =
                        eeff_temp.setScale(Constants.DecimalLength, RoundingMode.HALF_UP).toDouble()
                    editTextPhs!!.setText(eeff.toString())
                } else {
                    editTextPhs!!.setText("")
                }

                val z0_temp = BigDecimal.valueOf(line.impedance)
                val z0 = z0_temp.setScale(Constants.DecimalLength, RoundingMode.HALF_UP).toDouble()
                editTextZ0!!.setText(z0.toString())

                val k_temp = BigDecimal.valueOf(line.couplingFactor)
                val k = k_temp.setScale(Constants.DecimalLength, RoundingMode.HALF_UP).toDouble()
                editTextK!!.setText(k.toString())

                val z0o_temp = BigDecimal.valueOf(line.impedanceOdd)
                val z0o =
                    z0o_temp.setScale(Constants.DecimalLength, RoundingMode.HALF_UP).toDouble()
                editTextZ0o!!.setText(z0o.toString())

                val z0e_temp = BigDecimal.valueOf(line.impedanceEven)
                val z0e =
                    z0e_temp.setScale(Constants.DecimalLength, RoundingMode.HALF_UP).toDouble()
                editTextZ0e!!.setText(z0e.toString())
            }
        })

        buttonSynthesize!!.setOnClickListener(View.OnClickListener { view: View? ->
            Constants.refreshAnimation(
                mContext,
                viewRoot!!.findViewById<ImageView>(R.id.synthesize_reveal)!!,
                Constants.SYNTHESIZE
            )
            clearEditTextErrors()
            if (synthesizeInputCheck()) {
                synthesizeButton()
            }
        })

        return viewRoot!!
    }

    /**
     * Initialize the UI components and set up listeners.
     */
    private fun initUI() {
        line = CmlinModel()

        val radioButtonW = viewRoot!!.findViewById<RadioButton>(R.id.radioBtn_W)
        radioButtonW.setVisibility(View.VISIBLE)
        radioButtonW.setChecked(true)

        val radioButtonS = viewRoot!!.findViewById<RadioButton>(R.id.radioBtn_g)
        radioButtonS.setVisibility(View.VISIBLE)
        radioButtonS.setChecked(true)

        val radioButtonL = viewRoot!!.findViewById<RadioButton>(R.id.radioBtn_L)
        radioButtonL.setVisibility(View.VISIBLE)
        radioButtonL.setChecked(true)

        val radioButtonPhs = viewRoot!!.findViewById<RadioButton>(R.id.radioBtn_Phs)
        radioButtonPhs.setVisibility(View.VISIBLE)
        radioButtonPhs.setChecked(true)

        radioButtonZ0 = viewRoot!!.findViewById<RadioButton>(R.id.radioBtn_Z0)
        radioButtonZ0!!.setVisibility(View.VISIBLE)
        radioButtonK = viewRoot!!.findViewById<RadioButton>(R.id.radioBtn_k)
        radioButtonK!!.setVisibility(View.VISIBLE)
        radioButtonZ0o = viewRoot!!.findViewById<RadioButton>(R.id.radioBtn_Z0o)
        radioButtonZ0o!!.setVisibility(View.VISIBLE)
        radioButtonZ0e = viewRoot!!.findViewById<RadioButton>(R.id.radioBtn_Z0e)
        radioButtonZ0e!!.setVisibility(View.VISIBLE)

        textInputLayoutW = viewRoot!!.findViewById<TextInputLayout>(R.id.text_input_layout_W)
        editTextW = viewRoot!!.findViewById<TextInputEditText>(R.id.editText_W)
        editTextW!!.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor))
        editTextW!!.backgroundTintList =
            AppCompatResources.getColorStateList(
                mContext,
                R.color.background_tint_synthesize
            )
        editTextW!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                textInputLayoutW!!.error = ""
                textInputLayoutW!!.isErrorEnabled = false
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        textInputLayoutG = viewRoot!!.findViewById<TextInputLayout>(R.id.text_input_layout_g)
        editTextG = viewRoot!!.findViewById<TextInputEditText>(R.id.editText_g)
        editTextG!!.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor))
        editTextG!!.backgroundTintList =
            AppCompatResources.getColorStateList(
                mContext,
                R.color.background_tint_synthesize
            )
        editTextG!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                textInputLayoutG!!.error = ""
                textInputLayoutG!!.isErrorEnabled = false
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        editTextL = viewRoot!!.findViewById<TextInputEditText>(R.id.editText_L)
        editTextL!!.setTextColor(ContextCompat.getColor(mContext, R.color.synthesizeColor))
        editTextL!!.backgroundTintList =
            AppCompatResources.getColorStateList(
                mContext,
                R.color.background_tint_synthesize
            )

        textInputLayoutZ0 = viewRoot!!.findViewById<TextInputLayout>(R.id.text_input_layout_Z0)
        editTextZ0 = viewRoot!!.findViewById<TextInputEditText>(R.id.editText_Z0)
        editTextZ0!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                textInputLayoutZ0!!.error = ""
                textInputLayoutZ0!!.isErrorEnabled = false
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        textInputLayoutK = viewRoot!!.findViewById<TextInputLayout>(R.id.text_input_layout_k)
        editTextK = viewRoot!!.findViewById<TextInputEditText>(R.id.editText_k)
        editTextK!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                textInputLayoutK!!.error = ""
                textInputLayoutK!!.isErrorEnabled = false
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        textInputLayoutZ0o = viewRoot!!.findViewById<TextInputLayout>(R.id.text_input_layout_Z0o)
        editTextZ0o = viewRoot!!.findViewById<TextInputEditText>(R.id.editText_Z0o)
        editTextZ0o!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                textInputLayoutZ0o!!.error = ""
                textInputLayoutZ0o!!.isErrorEnabled = false
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        textInputLayoutZ0e = viewRoot!!.findViewById<TextInputLayout>(R.id.text_input_layout_Z0e)
        editTextZ0e = viewRoot!!.findViewById<TextInputEditText>(R.id.editText_Z0e)
        editTextZ0e!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                textInputLayoutZ0e!!.error = ""
                textInputLayoutZ0e!!.isErrorEnabled = false
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        editTextPhs = viewRoot!!.findViewById<TextInputEditText>(R.id.editText_Phs)
        editTextPhs!!.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor))
        editTextPhs!!.backgroundTintList =
            AppCompatResources.getColorStateList(
                mContext,
                R.color.background_tint_analyze
            )

        textInputLayoutF = viewRoot!!.findViewById<TextInputLayout>(R.id.text_input_layout_Freq)
        editTextFreq = viewRoot!!.findViewById<TextInputEditText>(R.id.editText_Freq)
        editTextFreq!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                textInputLayoutF!!.error = ""
                textInputLayoutF!!.isErrorEnabled = false
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        val editTextT = viewRoot!!.findViewById<EditText>(R.id.editText_T)
        editTextT.setText("0")
        editTextT.isEnabled = false

        textInputLayoutH = viewRoot!!.findViewById<TextInputLayout>(R.id.text_input_layout_H)
        editTextH = viewRoot!!.findViewById<TextInputEditText>(R.id.editText_H)
        editTextH!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                textInputLayoutH!!.error = ""
                textInputLayoutH!!.isErrorEnabled = false
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
                textInputLayoutEr!!.error = ""
                textInputLayoutEr!!.isErrorEnabled = false
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        // button elements
        buttonAnalyze = viewRoot!!.findViewById<Button>(R.id.button_ana)
        buttonSynthesize = viewRoot!!.findViewById<Button>(R.id.button_syn)

        // spinner elements
        spinnerW = viewRoot!!.findViewById<AutoCompleteTextView>(R.id.menu_W)
        spinnerG = viewRoot!!.findViewById<AutoCompleteTextView>(R.id.menu_g)
        spinnerL = viewRoot!!.findViewById<AutoCompleteTextView>(R.id.menu_L)
        spinnerZ0 = viewRoot!!.findViewById<AutoCompleteTextView>(R.id.menu_Z0)
        spinnerZ0o = viewRoot!!.findViewById<AutoCompleteTextView>(R.id.menu_Z0o)
        spinnerZ0e = viewRoot!!.findViewById<AutoCompleteTextView>(R.id.menu_Z0e)
        spinnerPhs = viewRoot!!.findViewById<AutoCompleteTextView>(R.id.menu_Phs)
        spinnerFreq = viewRoot!!.findViewById<AutoCompleteTextView>(R.id.menu_Freq)
        spinnerT = viewRoot!!.findViewById<AutoCompleteTextView>(R.id.menu_T)
        spinnerH = viewRoot!!.findViewById<AutoCompleteTextView>(R.id.menu_H)

        // configure the length units
        spinnerW!!.setAdapter<ArrayAdapter<CharSequence?>?>(Constants.adapterDimensionUnits(mContext))
        spinnerG!!.setAdapter<ArrayAdapter<CharSequence?>?>(Constants.adapterDimensionUnits(mContext))
        spinnerL!!.setAdapter<ArrayAdapter<CharSequence?>?>(Constants.adapterDimensionUnits(mContext))
        spinnerT!!.setAdapter<ArrayAdapter<CharSequence?>?>(Constants.adapterDimensionUnits(mContext))
        spinnerH!!.setAdapter<ArrayAdapter<CharSequence?>?>(Constants.adapterDimensionUnits(mContext))

        // configure the impedance units
        spinnerZ0!!.setAdapter<ArrayAdapter<CharSequence?>?>(
            Constants.adapterImpedanceUnits(
                mContext
            )
        )
        spinnerZ0o!!.setAdapter<ArrayAdapter<CharSequence?>?>(
            Constants.adapterImpedanceUnits(
                mContext
            )
        )
        spinnerZ0e!!.setAdapter<ArrayAdapter<CharSequence?>?>(
            Constants.adapterImpedanceUnits(
                mContext
            )
        )

        // configure the electrical length units
        spinnerPhs!!.setAdapter<ArrayAdapter<CharSequence?>?>(Constants.adapterPhaseUnits(mContext))

        // configure the frequency units
        spinnerFreq!!.setAdapter<ArrayAdapter<CharSequence?>?>(
            Constants.adapterFrequencyUnits(
                mContext
            )
        )
    }

    /**
     * Set the radio button behavior for selecting calculation mode.
     */
    private fun setRadioBtn() {
        radioButtonZ0!!.setOnClickListener(View.OnClickListener { arg0: View? ->
            radioButtonZ0!!.isChecked = true
            radioButtonK!!.isChecked = true
            radioButtonZ0o!!.isChecked = false
            radioButtonZ0e!!.isChecked = false
            useZ0k = true
            editTextZ0!!.isEnabled = true
            editTextZ0!!.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor))
            editTextK!!.isEnabled = true
            editTextK!!.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor))
            editTextZ0o!!.isEnabled = false
            editTextZ0o!!.setTextColor(
                ContextCompat.getColor(
                    mContext,
                    R.color.analyzeColorLight
                )
            )
            editTextZ0e!!.isEnabled = false
            editTextZ0e!!.setTextColor(
                ContextCompat.getColor(
                    mContext,
                    R.color.analyzeColorLight
                )
            )
            editTextZ0!!.backgroundTintList =
                AppCompatResources.getColorStateList(
                    mContext,
                    R.color.background_tint_analyze
                )
            editTextK!!.backgroundTintList =
                AppCompatResources.getColorStateList(
                    mContext,
                    R.color.background_tint_analyze
                )
            editTextZ0e!!.backgroundTintList =
                AppCompatResources.getColorStateList(
                    mContext,
                    R.color.background_tint_analyze_light
                )
            editTextZ0o!!.backgroundTintList =
                AppCompatResources.getColorStateList(
                    mContext,
                    R.color.background_tint_analyze_light
                )
        })
        radioButtonK!!.setOnClickListener(View.OnClickListener { arg0: View? ->
            radioButtonZ0!!.isChecked = true
            radioButtonK!!.isChecked = true
            radioButtonZ0o!!.isChecked = false
            radioButtonZ0e!!.isChecked = false
            useZ0k = true
            editTextZ0!!.isEnabled = true
            editTextZ0!!.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor))
            editTextK!!.isEnabled = true
            editTextK!!.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor))
            editTextZ0o!!.isEnabled = false
            editTextZ0o!!.setTextColor(
                ContextCompat.getColor(
                    mContext,
                    R.color.analyzeColorLight
                )
            )
            editTextZ0e!!.isEnabled = false
            editTextZ0e!!.setTextColor(
                ContextCompat.getColor(
                    mContext,
                    R.color.analyzeColorLight
                )
            )
            editTextZ0!!.backgroundTintList =
                AppCompatResources.getColorStateList(
                    mContext,
                    R.color.background_tint_analyze
                )
            editTextK!!.backgroundTintList =
                AppCompatResources.getColorStateList(
                    mContext,
                    R.color.background_tint_analyze
                )
            editTextZ0e!!.backgroundTintList =
                AppCompatResources.getColorStateList(
                    mContext,
                    R.color.background_tint_analyze_light
                )
            editTextZ0o!!.backgroundTintList =
                AppCompatResources.getColorStateList(
                    mContext,
                    R.color.background_tint_analyze_light
                )
        })
        radioButtonZ0o!!.setOnClickListener(View.OnClickListener { arg0: View? ->
            radioButtonZ0o!!.isChecked = true
            radioButtonZ0e!!.isChecked = true
            radioButtonZ0!!.isChecked = false
            radioButtonK!!.isChecked = false
            useZ0k = false
            editTextZ0!!.isEnabled = false
            editTextZ0!!.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColorLight))
            editTextK!!.isEnabled = false
            editTextK!!.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColorLight))
            editTextZ0o!!.isEnabled = true
            editTextZ0o!!.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor))
            editTextZ0e!!.isEnabled = true
            editTextZ0e!!.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor))
            editTextZ0!!.backgroundTintList =
                AppCompatResources.getColorStateList(
                    mContext,
                    R.color.background_tint_analyze_light
                )
            editTextK!!.backgroundTintList =
                AppCompatResources.getColorStateList(
                    mContext,
                    R.color.background_tint_analyze_light
                )
            editTextZ0e!!.backgroundTintList =
                AppCompatResources.getColorStateList(
                    mContext,
                    R.color.background_tint_analyze
                )
            editTextZ0o!!.backgroundTintList =
                AppCompatResources.getColorStateList(
                    mContext,
                    R.color.background_tint_analyze
                )
        })
        radioButtonZ0e!!.setOnClickListener(View.OnClickListener { arg0: View? ->
            radioButtonZ0o!!.isChecked = true
            radioButtonZ0e!!.isChecked = true
            radioButtonZ0!!.isChecked = false
            radioButtonK!!.isChecked = false
            useZ0k = false
            editTextZ0!!.isEnabled = false
            editTextZ0!!.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColorLight))
            editTextK!!.isEnabled = false
            editTextK!!.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColorLight))
            editTextZ0o!!.isEnabled = true
            editTextZ0o!!.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor))
            editTextZ0e!!.isEnabled = true
            editTextZ0e!!.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor))
            editTextZ0!!.backgroundTintList =
                AppCompatResources.getColorStateList(
                    mContext,
                    R.color.background_tint_analyze_light
                )
            editTextK!!.backgroundTintList =
                AppCompatResources.getColorStateList(
                    mContext,
                    R.color.background_tint_analyze_light
                )
            editTextZ0e!!.backgroundTintList =
                AppCompatResources.getColorStateList(
                    mContext,
                    R.color.background_tint_analyze
                )
            editTextZ0o!!.backgroundTintList =
                AppCompatResources.getColorStateList(
                    mContext,
                    R.color.background_tint_analyze
                )
        })
        if (useZ0k) {
            radioButtonZ0!!.isChecked = true
            radioButtonK!!.isChecked = true
            radioButtonZ0o!!.isChecked = false
            radioButtonZ0e!!.isChecked = false
            editTextZ0!!.isEnabled = true
            editTextZ0!!.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor))
            editTextK!!.isEnabled = true
            editTextK!!.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor))
            editTextZ0o!!.isEnabled = false
            editTextZ0o!!.setTextColor(
                ContextCompat.getColor(
                    mContext,
                    R.color.analyzeColorLight
                )
            )
            editTextZ0e!!.isEnabled = false
            editTextZ0e!!.setTextColor(
                ContextCompat.getColor(
                    mContext,
                    R.color.analyzeColorLight
                )
            )
            editTextZ0!!.backgroundTintList =
                AppCompatResources.getColorStateList(
                    mContext,
                    R.color.background_tint_analyze
                )
            editTextK!!.backgroundTintList =
                AppCompatResources.getColorStateList(
                    mContext,
                    R.color.background_tint_analyze
                )
            editTextZ0e!!.backgroundTintList =
                AppCompatResources.getColorStateList(
                    mContext,
                    R.color.background_tint_analyze_light
                )
            editTextZ0o!!.backgroundTintList =
                AppCompatResources.getColorStateList(
                    mContext,
                    R.color.background_tint_analyze_light
                )
        } else {
            radioButtonZ0!!.isChecked = false
            radioButtonK!!.isChecked = false
            radioButtonZ0o!!.isChecked = true
            radioButtonZ0e!!.isChecked = true
            editTextZ0!!.isEnabled = false
            editTextZ0!!.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColorLight))
            editTextK!!.isEnabled = false
            editTextK!!.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColorLight))
            editTextZ0o!!.isEnabled = true
            editTextZ0o!!.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor))
            editTextZ0e!!.isEnabled = true
            editTextZ0e!!.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor))
            editTextZ0!!.backgroundTintList =
                AppCompatResources.getColorStateList(
                    mContext,
                    R.color.background_tint_analyze_light
                )
            editTextK!!.backgroundTintList =
                AppCompatResources.getColorStateList(
                    mContext,
                    R.color.background_tint_analyze_light
                )
            editTextZ0e!!.backgroundTintList =
                AppCompatResources.getColorStateList(
                    mContext,
                    R.color.background_tint_analyze
                )
            editTextZ0o!!.backgroundTintList =
                AppCompatResources.getColorStateList(
                    mContext,
                    R.color.background_tint_analyze
                )
        }
    }

    /**
     * Read shared preferences and set the UI components accordingly.
     */
    private fun readSharedPref() {
        val prefs = mContext.getSharedPreferences(
            Constants.SHARED_PREFS_NAME,
            AppCompatActivity.MODE_PRIVATE
        )

        editTextW!!.setText(prefs.getString(Constants.CMLIN_W, "2.7"))
        spinnerW!!.setText(
            Constants.validateUnit(
                Constants.adapterDimensionUnits(mContext), prefs.getString(
                    Constants.CMLIN_W_UNIT, Constants.LengthUnit_mm
                )!!
            ), false
        )

        editTextG!!.setText(prefs.getString(Constants.CMLIN_S, "0.8"))
        spinnerG!!.setText(
            Constants.validateUnit(
                Constants.adapterDimensionUnits(mContext), prefs.getString(
                    Constants.CMLIN_S_UNIT, Constants.LengthUnit_mm
                )!!
            ), false
        )

        editTextL!!.setText(prefs.getString(Constants.CMLIN_L, "40.9"))
        spinnerL!!.setText(
            Constants.validateUnit(
                Constants.adapterDimensionUnits(mContext), prefs.getString(
                    Constants.CMLIN_L_UNIT, Constants.LengthUnit_mm
                )!!
            ), false
        )

        editTextZ0!!.setText(prefs.getString(Constants.CMLIN_Z0, "50"))
        spinnerZ0!!.setText(
            Constants.validateUnit(
                Constants.adapterImpedanceUnits(mContext), prefs.getString(
                    Constants.CMLIN_Z0_UNIT, Constants.ImpedanceUnit_Ohm
                )!!
            ), false
        )

        editTextK!!.setText(prefs.getString(Constants.CMLIN_K, "0.2"))

        editTextZ0o!!.setText(prefs.getString(Constants.CMLIN_Z0O, "41.044"))
        spinnerZ0o!!.setText(
            Constants.validateUnit(
                Constants.adapterImpedanceUnits(mContext), prefs.getString(
                    Constants.CMLIN_Z0O_UNIT, Constants.ImpedanceUnit_Ohm
                )!!
            ), false
        )

        editTextZ0e!!.setText(prefs.getString(Constants.CMLIN_Z0E, "62.341"))
        spinnerZ0e!!.setText(
            Constants.validateUnit(
                Constants.adapterImpedanceUnits(mContext), prefs.getString(
                    Constants.CMLIN_Z0E_UNIT, Constants.ImpedanceUnit_Ohm
                )!!
            ), false
        )

        editTextPhs!!.setText(prefs.getString(Constants.CMLIN_PHS, "90"))
        spinnerPhs!!.setText(
            Constants.validateUnit(
                Constants.adapterPhaseUnits(mContext), prefs.getString(
                    Constants.CMLIN_PHS_UNIT, Constants.PhaseUnit_Degree
                )!!
            ), false
        )

        editTextFreq!!.setText(prefs.getString(Constants.CMLIN_FREQ, "1.00"))
        spinnerFreq!!.setText(
            Constants.validateUnit(
                Constants.adapterFrequencyUnits(mContext), prefs.getString(
                    Constants.CMLIN_FREQ_UNIT, Constants.FreqUnit_GHz
                )!!
            ), false
        )

        editTextEr!!.setText(prefs.getString(Constants.CMLIN_ER, "4.60"))

        editTextH!!.setText(prefs.getString(Constants.CMLIN_H, "1.6"))
        spinnerH!!.setText(
            Constants.validateUnit(
                Constants.adapterDimensionUnits(mContext), prefs.getString(
                    Constants.CMLIN_H_UNIT, Constants.LengthUnit_mm
                )!!
            ), false
        )

        useZ0k = prefs.getString(Constants.CMLIN_USEZ0K, "true") == "true"
    }

    override fun onStop() {
        super.onStop()
        val prefs = mContext.getSharedPreferences(
            Constants.SHARED_PREFS_NAME,
            AppCompatActivity.MODE_PRIVATE
        )
        val editor = prefs.edit()

        editor.putString(
            Constants.CMLIN_W,
            editTextW!!.text!!.toString()
        )
        editor.putString(Constants.CMLIN_W_UNIT, spinnerW!!.text.toString())
        editor.putString(
            Constants.CMLIN_S,
            editTextG!!.text!!.toString()
        )
        editor.putString(Constants.CMLIN_S_UNIT, spinnerG!!.text.toString())
        editor.putString(
            Constants.CMLIN_L,
            editTextL!!.text!!.toString()
        )
        editor.putString(Constants.CMLIN_L_UNIT, spinnerL!!.text.toString())
        editor.putString(
            Constants.CMLIN_Z0,
            editTextZ0!!.text!!.toString()
        )
        editor.putString(Constants.CMLIN_Z0_UNIT, spinnerZ0!!.text.toString())
        editor.putString(
            Constants.CMLIN_K,
            editTextK!!.text!!.toString()
        )
        editor.putString(
            Constants.CMLIN_Z0E,
            editTextZ0e!!.text!!.toString()
        )
        editor.putString(Constants.CMLIN_Z0E_UNIT, spinnerZ0e!!.text.toString())
        editor.putString(Constants.CMLIN_Z0O_UNIT, spinnerZ0o!!.text.toString())
        editor.putString(
            Constants.CMLIN_PHS,
            editTextPhs!!.text!!.toString()
        )
        editor.putString(Constants.CMLIN_PHS_UNIT, spinnerPhs!!.text.toString())
        editor.putString(
            Constants.CMLIN_FREQ,
            editTextFreq!!.text!!.toString()
        )
        editor.putString(Constants.CMLIN_FREQ_UNIT, spinnerFreq!!.text.toString())
        editor.putString(
            Constants.CMLIN_ER,
            editTextEr!!.text!!.toString()
        )
        editor.putString(
            Constants.CMLIN_H,
            editTextH!!.text!!.toString()
        )
        editor.putString(Constants.CMLIN_H_UNIT, spinnerH!!.text.toString())

        if (useZ0k) {
            editor.putString(Constants.CMLIN_USEZ0K, "true")
        } else {
            editor.putString(Constants.CMLIN_USEZ0K, "false")
        }
        editor.apply()
    }

    /**
     * Check the input fields for the analysis operation.
     * @return true if all inputs are valid, false otherwise.
     */
    private fun analysisInputCheck(): Boolean {
        var checkResult = true
        if (editTextW!!.length() == 0) {
            textInputLayoutW!!.setError(getText(R.string.Error_W_empty))
            checkResult = false
        } else if (Constants.value2meter(
                editTextW!!.text!!.toString().toDouble(),
                spinnerW!!.text.toString()
            ) < Constants.MINI_LIMIT
        ) {
            textInputLayoutW!!.setError(getText(R.string.unreasonable_value))
            checkResult = false
        }

        if (editTextG!!.length() == 0) {
            textInputLayoutG!!.setError(getText(R.string.Error_S_empty))
            checkResult = false
        } else if (Constants.value2meter(
                editTextG!!.text!!.toString().toDouble(),
                spinnerG!!.text.toString()
            ) < Constants.MINI_LIMIT
        ) {
            textInputLayoutG!!.setError(getText(R.string.unreasonable_value))
            checkResult = false
        }

        if (editTextFreq!!.length() == 0) {
            textInputLayoutF!!.setError(getText(R.string.Error_Freq_empty))
            checkResult = false
        } else if (editTextFreq!!.text!!.toString()
                .toDouble() == 0.0
        ) {
            textInputLayoutF!!.setError(getText(R.string.error_zero_frequency))
            checkResult = false
        }

        if (editTextH!!.length() == 0) {
            textInputLayoutH!!.setError(getText(R.string.Error_H_empty))
            checkResult = false
        } else if (Constants.value2meter(
                editTextH!!.text!!.toString().toDouble(),
                spinnerH!!.text.toString()
            ) < Constants.MINI_LIMIT
        ) {
            textInputLayoutH!!.setError(getText(R.string.unreasonable_value))
            checkResult = false
        }

        if (editTextEr!!.length() == 0) {
            textInputLayoutEr!!.setError(getText(R.string.error_er_empty))
            checkResult = false
        } else if (editTextEr!!.text!!.toString()
                .toDouble() < 1
        ) {
            textInputLayoutEr!!.setError(getText(R.string.unreasonable_value))
            checkResult = false
        }

        if (!checkResult) {
            editTextZ0!!.setText("")
            editTextPhs!!.setText("")
            editTextZ0o!!.setText("")
            editTextZ0e!!.setText("")
            editTextK!!.setText("")
        }
        return checkResult
    }

    /**
     * Check the input fields for the synthesis operation.
     * @return true if all inputs are valid, false otherwise.
     */
    private fun synthesizeInputCheck(): Boolean {
        var checkResult = true
        if (editTextFreq!!.length() == 0) {
            textInputLayoutF!!.setError(getText(R.string.Error_Freq_empty))
            checkResult = false
        } else if (editTextFreq!!.text!!.toString()
                .toDouble() == 0.0
        ) {
            textInputLayoutF!!.setError(getText(R.string.error_zero_frequency))
            checkResult = false
        }

        if (editTextH!!.length() == 0) {
            textInputLayoutH!!.setError(getText(R.string.Error_H_empty))
            checkResult = false
        } else if (Constants.value2meter(
                editTextH!!.text!!.toString().toDouble(),
                spinnerH!!.text.toString()
            ) < Constants.MINI_LIMIT
        ) {
            textInputLayoutH!!.setError(getText(R.string.unreasonable_value))
            checkResult = false
        }

        if (editTextEr!!.length() == 0) {
            textInputLayoutEr!!.setError(getText(R.string.error_er_empty))
            checkResult = false
        } else if (editTextEr!!.text!!.toString()
                .toDouble() < 1
        ) {
            textInputLayoutEr!!.setError(getText(R.string.unreasonable_value))
            checkResult = false
        }

        if (useZ0k) {
            if (editTextZ0!!.length() == 0) {
                textInputLayoutZ0!!.setError(getText(R.string.error_Z0_empty))
                checkResult = false
            } else if (editTextZ0!!.text!!.toString()
                    .toDouble() == 0.0
            ) {
                textInputLayoutZ0!!.setError(getText(R.string.unreasonable_value))
                checkResult = false
            }

            if (editTextK!!.length() == 0) {
                textInputLayoutK!!.setError(getText(R.string.Error_k_empty))
                checkResult = false
            } else if (editTextK!!.text!!.toString()
                    .toDouble() == 0.0
            ) {
                textInputLayoutK!!.setError(getText(R.string.unreasonable_value))
                checkResult = false
            }
        } else {
            if (editTextZ0e!!.length() == 0) {
                textInputLayoutZ0e!!.setError(getText(R.string.error_Z0e_empty))
                checkResult = false
            } else if (editTextZ0e!!.text!!.toString()
                    .toDouble() == 0.0
            ) {
                textInputLayoutZ0e!!.setError(getText(R.string.unreasonable_value))
                checkResult = false
            }

            if (editTextZ0o!!.length() == 0) {
                textInputLayoutZ0o!!.setError(getText(R.string.error_Z0o_empty))
                checkResult = false
            } else if (editTextZ0o!!.text!!.toString()
                    .toDouble() == 0.0
            ) {
                textInputLayoutZ0o!!.setError(getText(R.string.unreasonable_value))
                checkResult = false
            }
        }

        if (!checkResult) {
            editTextL!!.setText("")
            editTextG!!.setText("")
            editTextW!!.setText("")
        }
        return checkResult
    }

    /**
     * Perform the synthesis operation based on the input values.
     */
    private fun synthesizeButton() {
        if (useZ0k) {
            line.impedance = editTextZ0!!.text!!.toString().toDouble()
            line.couplingFactor =
                editTextK!!.text!!.toString().toDouble()
            line.impedanceOdd = 0.0
            line.impedanceEven = 0.0
        } else {
            line.impedanceEven =
                editTextZ0e!!.text!!.toString().toDouble()
            line.impedanceOdd =
                editTextZ0o!!.text!!.toString().toDouble()
            line.impedance = 0.0
            line.couplingFactor = 0.0
        }

        line.setFrequency(
            editTextFreq!!.text!!.toString().toDouble(),
            spinnerFreq!!.text.toString()
        )
        line.subEpsilon = editTextEr!!.text!!.toString().toDouble()
        line.setSubHeight(
            editTextH!!.text!!.toString().toDouble(),
            spinnerH!!.text.toString()
        )
        line.setMetalThick(0.0, spinnerT!!.text.toString())

        var w: Double
        var s: Double
        var l: Double
        if (editTextPhs!!.length() != 0) {
            line.phase = editTextPhs!!.text!!.toString().toDouble()
        } else {
            line.phase = 0.0
        }
        val cmlin = CmlinCalculator()
        line = cmlin.getSynResult(line, useZ0k)

        if (line.errorCode == Constants.ERROR.NO_ERROR) {
            if (editTextPhs!!.length() != 0) {
                val l_temp = BigDecimal.valueOf(
                    Constants.meter2others(
                        line.metalLength,
                        spinnerL!!.text.toString()
                    )
                )
                l = l_temp.setScale(Constants.DecimalLength, RoundingMode.HALF_UP).toDouble()
                editTextL!!.setText(l.toString())
            } else {
                editTextL!!.setText("")
            }

            if ((line.metalWidth.isNaN() || line.metalWidth.isInfinite()) || (line.metalSpace.isNaN() || line.metalSpace.isInfinite())
            ) {
                editTextW!!.setText("")
                textInputLayoutW!!.setError(getString(R.string.synthesize_failed))
                editTextW!!.requestFocus()
                editTextG!!.setText("")
                textInputLayoutG!!.setError(getString(R.string.synthesize_failed))
                editTextL!!.setText("")
            } else {
                val w_temp = BigDecimal.valueOf(
                    Constants.meter2others(
                        line.metalWidth,
                        spinnerW!!.text.toString()
                    )
                )
                w = w_temp.setScale(Constants.DecimalLength, RoundingMode.HALF_UP).toDouble()
                editTextW!!.setText(w.toString())

                val s_temp = BigDecimal.valueOf(
                    Constants.meter2others(
                        line.metalSpace,
                        spinnerG!!.text.toString()
                    )
                )
                s = s_temp.setScale(Constants.DecimalLength, RoundingMode.HALF_UP).toDouble()
                editTextG!!.setText(s.toString())
            }

            if (useZ0k) {
                val z0e: Double
                val z0o: Double

                val z0o_temp = BigDecimal.valueOf(line.impedanceOdd)
                z0o = z0o_temp.setScale(Constants.DecimalLength, RoundingMode.HALF_UP).toDouble()
                editTextZ0o!!.setText(z0o.toString())

                val z0e_temp = BigDecimal.valueOf(line.impedanceEven)
                z0e = z0e_temp.setScale(Constants.DecimalLength, RoundingMode.HALF_UP).toDouble()
                editTextZ0e!!.setText(z0e.toString())
            } else {
                val z0: Double
                val k: Double

                val z0_temp = BigDecimal.valueOf(line.impedance)
                z0 = z0_temp.setScale(Constants.DecimalLength, RoundingMode.HALF_UP).toDouble()
                editTextZ0!!.setText(z0.toString()) // cut the decimal of the Z0
                val k_temp = BigDecimal.valueOf(line.couplingFactor)
                k = k_temp.setScale(Constants.DecimalLength, RoundingMode.HALF_UP).toDouble()
                editTextK!!.setText(k.toString())
            }
        } else if (line.errorCode == Constants.ERROR.K_OUT_OF_RANGE) {
            editTextW!!.setText("")
            editTextG!!.setText("")
            editTextL!!.setText("")
            textInputLayoutK!!.setError(getString(R.string.k_out_of_range))
            editTextK!!.requestFocus()
        } else if (line.errorCode == Constants.ERROR.Z0E_Z0O_MISTAKE) {
            editTextW!!.setText("")
            editTextG!!.setText("")
            editTextL!!.setText("")
            textInputLayoutZ0o!!.setError(getString(R.string.Z0e_larger_Z0o))
            editTextZ0o!!.requestFocus()
            textInputLayoutZ0e!!.setError(getString(R.string.Z0e_larger_Z0o))
        } else {
            editTextW!!.setText("")
            textInputLayoutW!!.setError(getString(R.string.synthesize_failed))
            editTextW!!.requestFocus()
            editTextG!!.setText("")
            textInputLayoutG!!.setError(getString(R.string.synthesize_failed))
        }
    }

    /**
     * Clear all error messages from the input fields.
     */
    private fun clearEditTextErrors() {
        textInputLayoutW!!.setError(null)
        textInputLayoutW!!.setErrorEnabled(false)
        textInputLayoutG!!.setError(null)
        textInputLayoutG!!.setErrorEnabled(false)
        textInputLayoutH!!.setError(null)
        textInputLayoutH!!.setErrorEnabled(false)
        textInputLayoutZ0!!.setError(null)
        textInputLayoutZ0!!.setErrorEnabled(false)
        textInputLayoutK!!.setError(null)
        textInputLayoutK!!.setErrorEnabled(false)
        textInputLayoutZ0e!!.setError(null)
        textInputLayoutZ0e!!.setErrorEnabled(false)
        textInputLayoutZ0o!!.setError(null)
        textInputLayoutZ0o!!.setErrorEnabled(false)
        textInputLayoutEr!!.setError(null)
        textInputLayoutEr!!.setErrorEnabled(false)
        textInputLayoutF!!.setError(null)
        textInputLayoutF!!.setErrorEnabled(false)
    }

    /**
     * Reset all input fields to their default values.
     */
    fun resetValues() {
        editTextW!!.setText("2.7")
        spinnerW!!.setText(Constants.LengthUnit_mm)
        editTextG!!.setText("0.8")
        spinnerG!!.setText(Constants.LengthUnit_mm)
        editTextL!!.setText("40.9")
        spinnerL!!.setText(Constants.LengthUnit_mm)
        editTextZ0!!.setText("50")
        spinnerZ0!!.setText(Constants.ImpedanceUnit_Ohm)
        editTextK!!.setText("0.2")
        editTextZ0o!!.setText("41.044")
        spinnerZ0o!!.setText(Constants.ImpedanceUnit_Ohm)
        editTextZ0e!!.setText("62.341")
        spinnerZ0e!!.setText(Constants.ImpedanceUnit_Ohm)
        editTextPhs!!.setText("90")
        spinnerPhs!!.setText(Constants.PhaseUnit_Degree)
        editTextFreq!!.setText("1.00")
        spinnerFreq!!.setText(Constants.FreqUnit_GHz)
        editTextEr!!.setText("4.60")
        editTextH!!.setText("1.6")
        spinnerH!!.setText(Constants.LengthUnit_mm)
        useZ0k = true

        radioButtonZ0!!.isChecked = true
        radioButtonK!!.isChecked = true
        radioButtonZ0o!!.isChecked = false
        radioButtonZ0e!!.isChecked = false
        editTextZ0!!.isEnabled = true
        editTextZ0!!.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor))
        editTextK!!.isEnabled = true
        editTextK!!.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColor))
        editTextZ0o!!.isEnabled = false
        editTextZ0o!!.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColorLight))
        editTextZ0e!!.isEnabled = false
        editTextZ0e!!.setTextColor(ContextCompat.getColor(mContext, R.color.analyzeColorLight))
        editTextZ0!!.backgroundTintList =
            AppCompatResources.getColorStateList(
                mContext,
                R.color.background_tint_analyze
            )
        editTextK!!.backgroundTintList =
            AppCompatResources.getColorStateList(
                mContext,
                R.color.background_tint_analyze
            )
        editTextZ0e!!.backgroundTintList =
            AppCompatResources.getColorStateList(
                mContext,
                R.color.background_tint_analyze_light
            )
        editTextZ0o!!.backgroundTintList =
            AppCompatResources.getColorStateList(
                mContext,
                R.color.background_tint_analyze_light
            )
    }
}
