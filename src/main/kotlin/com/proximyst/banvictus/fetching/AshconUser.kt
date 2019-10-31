package com.proximyst.banvictus.fetching

data class AshconUser(
    val uuid: String,
    val username: String
) {
    override fun toString(): String {
        return "AshconUser(uuid='$uuid', username='$username')"
    }
}