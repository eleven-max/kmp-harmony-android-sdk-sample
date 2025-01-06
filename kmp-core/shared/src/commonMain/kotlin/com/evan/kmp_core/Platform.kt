package com.evan.kmp_core

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform