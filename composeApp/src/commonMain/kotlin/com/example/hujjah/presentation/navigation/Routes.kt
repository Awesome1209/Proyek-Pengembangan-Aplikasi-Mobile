package com.example.hujjah.presentation.navigation

import kotlinx.serialization.Serializable

sealed interface Route {

    // ==================== HUJJAH ROUTES ====================

    @Serializable
    data object Splash : Route // Splash screen

    @Serializable
    data object Home : Route // Beranda (Dashboard)

    @Serializable
    data object HujjahLens : Route // Lens (AI Counselor Chat)

    @Serializable
    data object Quran : Route // Qur'an list

    @Serializable
    data class QuranDetail(val surahNumber: Int, val surahName: String, val verseNumber: Int? = null) : Route // Qur'an detail reading

    @Serializable
    data object Hadith : Route // Hadits grid/list

    @Serializable
    data object Koleksi : Route // Koleksi Hub (Dalil Tersimpan, Khazanah, Catatan, Riwayat)

    @Serializable
    data object Profile : Route // Profil (Nested Destination from Home)

    @Serializable
    data class HujjahResult(val topicId: String) : Route

    @Serializable
    data class ReferenceDetail(val referenceId: String) : Route

    @Serializable
    data object Bookmark : Route // Bookmark Khazanah

    @Serializable
    data object Notes : Route // Daftar Catatan

    @Serializable
    data class AddNote(val noteId: Long? = null, val initialContent: String? = null) : Route // Tambah/Edit Catatan

    @Serializable
    data class NoteDetail(val noteId: Long) : Route // Detail Catatan

    @Serializable
    data object TilawahHistory : Route // Kalender & Jurnal Tilawah (Strava Ngaji)
}

interface NavigationActions {
    fun navigateToHome()
    fun navigateToHujjahLens()
    fun navigateToQuran()
    fun navigateToQuranDetail(surahNumber: Int, surahName: String, verseNumber: Int? = null)
    fun navigateToHadith()
    fun navigateToKoleksi()
    fun navigateToProfile()
    fun navigateToHujjahResult(topicId: String)
    fun navigateToReferenceDetail(referenceId: String)
    fun navigateToBookmarks()
    fun navigateToNotes()
    fun navigateToTilawahHistory()
    fun navigateToNoteDetail(noteId: Long)
    fun navigateToAddNote(noteId: Long? = null, initialContent: String? = null)
    fun navigateBack()
}
