package com.example.hujjah.domain.model.islamic

enum class SourceType {
    QURAN,
    HADITH;

    fun label(): String = when (this) {
        QURAN -> "Al-Qur'an"
        HADITH -> "Hadis"
    }
}
