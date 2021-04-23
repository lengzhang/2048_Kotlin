package com.lengzhang.android.lz2048

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.TextView
import androidx.cardview.widget.CardView

class Block @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CardView(context, attrs, defStyleAttr) {


    private var labelTextView: TextView
    private var valueTextView: TextView

    var label: String = ""
        set(value) {
            field = value
            labelTextView.text = value
        }

    var value: Int = 0
        set(value) {
            field = value
            valueTextView.text = value.toString()
        }

    init {
        LayoutInflater.from(context).inflate(R.layout.block, this)
        labelTextView = this.findViewById(R.id.label)
        valueTextView = this.findViewById(R.id.value)

        applyAttributes(attrs)
    }

    private fun applyAttributes(attrs: AttributeSet? = null) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.Block)
        label = typedArray.getString(R.styleable.Block_label) ?: ""
        value = typedArray.getInt(R.styleable.Block_value, 0)
        typedArray.recycle()
    }
}