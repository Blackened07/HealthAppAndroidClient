package com.blackened.healthapp.presentation

enum class MetricType(val serverName: String, val label: String) {
    BLOOD_PRESSURE("BLOOD_PRESSURE", "Давление"),
    GLUCOSE("GLUCOSE", "Сахар"),
    TEMPERATURE("TEMPERATURE", "Температура"),
    WEIGHT("WEIGHT", "Вес"),
    CUSTOM("CUSTOM", "Другое")
}