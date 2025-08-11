package com.eyedock.app.utils

import android.util.Log

object Logger {
    
    fun d(tag: String, message: String) {
        Log.d(tag, message)
    }
    
    fun i(tag: String, message: String) {
        Log.i(tag, message)
    }
    
    fun w(tag: String, message: String) {
        Log.w(tag, message)
    }
    
    fun e(tag: String, message: String) {
        Log.e(tag, message)
    }
    
    fun withTag(tag: String): LoggerWithTag {
        return LoggerWithTag(tag)
    }
    
    class LoggerWithTag(private val tag: String) {
        fun d(message: String) = Logger.d(tag, message)
        fun i(message: String) = Logger.i(tag, message)
        fun w(message: String) = Logger.w(tag, message)
        fun e(message: String) = Logger.e(tag, message)
    }
}
