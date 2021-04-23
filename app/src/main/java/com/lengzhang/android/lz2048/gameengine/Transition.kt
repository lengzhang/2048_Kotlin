package com.lengzhang.android.lz2048.gameengine

enum class TransitionTypes { NEW, MOVE, MERGE, REMOVE, NONE }

data class Transition(
    var type: TransitionTypes,
    var value: Int,
    var pos: Int,
    var posA: Int? = null,
    var posB: Int? = null
)
