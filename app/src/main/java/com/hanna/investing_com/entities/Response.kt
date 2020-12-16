package com.hanna.investing_com.entities

data class Response(
    var results: List<Result> = emptyList()
) : ResponseStatus {
    override var status: String = ""
}

interface ResponseStatus {
    var status: String
}