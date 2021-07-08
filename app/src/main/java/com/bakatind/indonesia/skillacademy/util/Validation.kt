package com.bakatind.indonesia.skillacademy.util

import android.text.TextUtils
import android.util.Patterns

class Validation {

    companion object {
        fun validateFields(input: String): Boolean {
            return TextUtils.isEmpty(input)
        }

        fun validateEmail(string: String): Boolean {
            return (TextUtils.isEmpty(string) || !Patterns.EMAIL_ADDRESS.matcher(string).matches())
        }
    }
}