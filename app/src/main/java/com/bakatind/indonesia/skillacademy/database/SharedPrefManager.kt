package com.bakatind.indonesia.skillacademy.database

import android.content.Context

class SharedPrefManager private constructor(context: Context) {

    init {
        mContext = context
    }

    companion object {

        private const val PROFILE = "profile"

        private const val USER_STATUS = "userStatus"
        private const val USER_EMAIL = "userEmail"

        private lateinit var mContext: Context
        private var mInstance: SharedPrefManager? = null

        @Synchronized
        fun getInstance(context: Context?): SharedPrefManager? {
            if (mInstance == null)
                mInstance = context?.let { SharedPrefManager(it) }
            return mInstance
        }
    }

    val getIsTeacher: Boolean
        get() {
            val preferences = mContext.getSharedPreferences(PROFILE, Context.MODE_PRIVATE)
            return preferences.getBoolean(USER_STATUS, false)
        }

    fun isTeacher(status: Boolean): Boolean {
        val preferences = mContext.getSharedPreferences(PROFILE, Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putBoolean(USER_STATUS, status)
        editor.apply()
        return true
    }

    val getEmailUser: String?
        get() {
            val preferences = mContext.getSharedPreferences(PROFILE, Context.MODE_PRIVATE)
            return preferences.getString(USER_EMAIL, "")
        }

    fun setEmailUser(email: String) {
        val preferences = mContext.getSharedPreferences(PROFILE, Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putString(USER_EMAIL, email)
        editor.apply()
    }

    fun restoreUser(): Boolean {
        val preferences = mContext.getSharedPreferences(PROFILE, Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.clear()
        return editor.commit()
    }
}