package com.proximyst.banvictus

import litebans.api.Entry

interface EntryHandler<P> {
    fun handle(entry: Entry, plugin: P): Boolean
}