package com.tickets.store_cost

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform