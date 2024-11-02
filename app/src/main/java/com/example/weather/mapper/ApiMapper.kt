package com.example.weather.mapper

interface ApiMapper<R, M> {
    fun mapToDomain(apiResponse: R): M
}