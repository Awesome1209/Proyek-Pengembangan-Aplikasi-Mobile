package com.example.hujjah.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hujjah.data.local.datastore.UserPreferences
import com.example.hujjah.domain.model.islamic.TilawahStreakSummary
import com.example.hujjah.domain.repository.hujjah.TilawahRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val userPreferences: UserPreferences,
    private val tilawahRepository: TilawahRepository
) : ViewModel() {

    private var timerJob: Job? = null
    val isTimerRunning = MutableStateFlow(false)

    val tilawahStreakSummary: StateFlow<TilawahStreakSummary> = tilawahRepository.getStreakSummary()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TilawahStreakSummary(0, 0, 0, false, 0)
        )

    val dailyTargetMinutes: StateFlow<Int> = userPreferences.dailyTargetMinutes
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 15
        )

    val readingDurationSeconds: StateFlow<Int> = userPreferences.readingDurationSeconds
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val itemsReadToday: StateFlow<Int> = userPreferences.itemsReadToday
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val versesReadToday: StateFlow<Int> = userPreferences.versesReadToday
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val hadithsReadToday: StateFlow<Int> = userPreferences.hadithsReadToday
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val currentStreakDays: StateFlow<Int> = userPreferences.currentStreakDays
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val lastReadLocation: StateFlow<String> = userPreferences.lastReadQuranLocation
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ""
        )

    val userName: StateFlow<String> = userPreferences.userName
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "Awi"
        )

    val profileImageBase64: StateFlow<String> = userPreferences.profileImageBase64
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ""
        )

    private val quotesList = listOf(
        QuoteData(
            arabic = "أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ",
            translation = "Ingatlah, hanya dengan mengingati Allah-lah hati menjadi tenteram.",
            reference = "QS. Ar-Ra'd: 28"
        ),
        QuoteData(
            arabic = "خَيْرُكُمْ مَنْ تَعَلَّمَ الْقُرْآنَ وَعَلَّمَهُ",
            translation = "Sebaik-baik kalian adalah orang yang mempelajari Al-Qur'an dan mengajarkannya.",
            reference = "HR. Bukhari No. 5027"
        ),
        QuoteData(
            arabic = "اقْرَءُوا الْقُرْآنَ فَإِنَّهُ يَأْتِي يَوْمَ الْقِيَامَةِ شَفِيعًا لِأَصْحَابِهِ",
            translation = "Bacalah Al-Qur'an, karena sesungguhnya ia akan datang pada hari kiamat sebagai pemberi syafaat bagi pembacanya.",
            reference = "HR. Muslim No. 804"
        ),
        QuoteData(
            arabic = "وَنُنَزِّلُ مِنَ الْقُرْآنِ مَا هُوَ شِفَاءٌ وَرَحْمَةٌ لِلْمُؤْمِنِينَ",
            translation = "Dan Kami turunkan dari Al-Qur'an suatu yang menjadi penawar dan rahmat bagi orang-orang yang beriman.",
            reference = "QS. Al-Isra': 82"
        ),
        QuoteData(
            arabic = "إِنَّ هٰذَا الْقُرْآنَ يَهْدِي لِلَّتِي هِيَ أَقْوَمُ",
            translation = "Sungguh, Al-Qur'an ini memberi petunjuk ke jalan yang paling lurus.",
            reference = "QS. Al-Isra': 9"
        ),
        QuoteData(
            arabic = "مَنْ قَرَأَ حَرْفًا مِنْ كِتَابِ اللَّهِ فَلَهُ بِهِ حَسَنَةٌ وَالْحَسَنَةُ بِعَشْرِ أَمْثَالِهَا",
            translation = "Siapa yang membaca satu huruf dari Kitabullah maka baginya satu kebaikan, dan satu kebaikan dilipatgandakan sepuluh kali.",
            reference = "HR. Tirmidzi No. 2910"
        )
    )

    private val _quoteIndex = MutableStateFlow(0)
    val quoteOfTheDay: StateFlow<QuoteData> = _quoteIndex.map { quotesList[it % quotesList.size] }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = quotesList[0]
        )

    init {
        // Rotasi Kutipan Otomatis Setiap 1 Menit (60 Detik)
        viewModelScope.launch {
            while (true) {
                delay(60_000L)
                _quoteIndex.value = (_quoteIndex.value + 1) % quotesList.size
            }
        }
    }

    fun updateDailyTargetMinutes(minutes: Int) {
        viewModelScope.launch {
            userPreferences.setDailyTargetMinutes(minutes)
        }
    }

    fun toggleTimer() {
        if (isTimerRunning.value) {
            stopTimer()
        } else {
            startTimer()
        }
    }

    private fun startTimer() {
        if (timerJob?.isActive == true) return
        isTimerRunning.value = true
        timerJob = viewModelScope.launch {
            userPreferences.updateStreak()
            while (true) {
                delay(1000)
                userPreferences.addReadingDuration(1)
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        isTimerRunning.value = false
    }

    fun resetReadingTime() {
        stopTimer()
        viewModelScope.launch {
            userPreferences.resetReadingDuration()
        }
    }
}

data class QuoteData(
    val arabic: String,
    val translation: String,
    val reference: String
)
