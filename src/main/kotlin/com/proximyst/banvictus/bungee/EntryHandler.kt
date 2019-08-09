package com.proximyst.banvictus.bungee

import litebans.api.Entry

interface EntryHandler {
    fun handle(entry: Entry)
}