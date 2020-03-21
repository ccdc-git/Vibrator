package com.ccdc.vibrator

data class OneShot (
    var codeName : String,
    var duration : Long,
    var isStaccato : Boolean
    )
/*
    1. "PIANO"       -> soft          -> red
    2. "FORTE"       -> loud          -> blue
    3. "CRESCENDO"   -> growing       -> green
    4. "DECRESCENDO" -> decreasing    -> black
    ''sc'' -> staccato  ex) scPIANO -> soft staccato
    1 Shot divided for 25
 */