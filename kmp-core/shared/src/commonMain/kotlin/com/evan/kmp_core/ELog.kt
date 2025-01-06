package com.evan.kmp_core

object ELog {

    fun v(message: String) {
        v("KMPCore", message)
    }

    fun v(tag: String, message: String) {
        platformLog(tag, message)
    }
}