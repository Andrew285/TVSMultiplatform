package org.tutvsisvoyi.testkmpapp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform