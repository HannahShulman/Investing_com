package com.hanna.investing_com.extensions

fun Float.distanceToMetersFormat(): String{
    return String.format("%.2f", this)+ "m"
}