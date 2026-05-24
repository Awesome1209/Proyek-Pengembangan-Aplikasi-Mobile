package com.example.hujjah.presentation.screens.quran

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hujjah.data.local.datastore.UserPreferences
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data class SurahItem(
    val number: Int,
    val name: String,
    val translation: String,
    val numberOfVerses: Int,
    val revelation: String,
    val asma: String
)

@Serializable
data class VerseItem(
    val number: Int,
    val arabic: String,
    val translation: String
)

@Serializable
private data class GadingQuranResponse(
    val code: Int,
    val status: String,
    val data: List<GadingSurahItem>
)

@Serializable
private data class GadingSurahItem(
    val number: Int,
    val name: GadingSurahName,
    val numberOfVerses: Int,
    val revelation: GadingRevelation
)

@Serializable
private data class GadingSurahName(
    val short: String,
    val translation: GadingTranslation
)

@Serializable
private data class GadingTranslation(
    val id: String
)

@Serializable
private data class GadingRevelation(
    val id: String
)

@Serializable
private data class GadingSurahDetailResponse(
    val code: Int,
    val status: String,
    val data: GadingSurahDetailData
)

@Serializable
private data class GadingSurahDetailData(
    val number: Int,
    val name: GadingSurahName,
    val verses: List<GadingVerseItem>
)

@Serializable
private data class GadingVerseItem(
    val number: GadingVerseNumber,
    val text: GadingVerseText,
    val translation: GadingTranslation
)

@Serializable
private data class GadingVerseNumber(
    val inSurah: Int
)

@Serializable
private data class GadingVerseText(
    val arabic: String
)

data class QuranUiState(
    val surahs: List<SurahItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class SurahDetailUiState(
    val number: Int = 0,
    val name: String = "",
    val verses: List<VerseItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class QuranViewModel(
    private val httpClient: HttpClient,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuranUiState())
    val uiState: StateFlow<QuranUiState> = _uiState.asStateFlow()

    private val _detailUiState = MutableStateFlow(SurahDetailUiState())
    val detailUiState: StateFlow<SurahDetailUiState> = _detailUiState.asStateFlow()

    val lastReadLocation: StateFlow<String> = userPreferences.lastReadQuranLocation
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ""
        )

    init {
        fetchSurahs()
    }

    fun fetchSurahs() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                val response: GadingQuranResponse = httpClient.get("https://api.gading.dev/quran/surah").body()
                val items = response.data.map {
                    SurahItem(
                        number = it.number,
                        name = it.name.short,
                        translation = it.name.translation.id,
                        numberOfVerses = it.numberOfVerses,
                        revelation = it.revelation.id,
                        asma = it.name.short
                    )
                }
                _uiState.value = QuranUiState(surahs = items, isLoading = false)
            } catch (e: Exception) {
                // Offline Fallback - Mock first 10 surahs
                val fallbackItems = listOf(
                    SurahItem(1, "Al-Fatihah", "Pembukaan", 7, "Mekah", "الفاتحة"),
                    SurahItem(2, "Al-Baqarah", "Sapi Betina", 286, "Madinah", "البقرة"),
                    SurahItem(3, "Ali 'Imran", "Keluarga 'Imran", 200, "Madinah", "آل عمران"),
                    SurahItem(4, "An-Nisa'", "Wanita", 176, "Madinah", "النساء"),
                    SurahItem(5, "Al-Ma'idah", "Hidangan", 120, "Madinah", "المائدة"),
                    SurahItem(6, "Al-An'am", "Binatang Ternak", 165, "Mekah", "الأنعام"),
                    SurahItem(7, "Al-A'raf", "Tempat yang Tinggi", 206, "Mekah", "الأعراف"),
                    SurahItem(8, "Al-Anfal", "Rampasan Perang", 75, "Madinah", "الأنفال"),
                    SurahItem(9, "At-Taubah", "Pengampunan", 129, "Madinah", "التوبة"),
                    SurahItem(10, "Yunus", "Yunus", 109, "Mekah", "يونس")
                )
                _uiState.value = QuranUiState(
                    surahs = fallbackItems,
                    isLoading = false,
                    error = "Koneksi lambat. Menampilkan data luring."
                )
            }
        }
    }

    fun fetchSurahDetail(surahNumber: Int, surahName: String) {
        _detailUiState.value = SurahDetailUiState(number = surahNumber, name = surahName, isLoading = true)
        viewModelScope.launch {
            try {
                val response: GadingSurahDetailResponse = httpClient.get("https://api.gading.dev/quran/surah/$surahNumber").body()
                val verses = response.data.verses.map {
                    VerseItem(
                        number = it.number.inSurah,
                        arabic = it.text.arabic,
                        translation = it.translation.id
                    )
                }
                _detailUiState.value = SurahDetailUiState(
                    number = surahNumber,
                    name = surahName,
                    verses = verses,
                    isLoading = false
                )
            } catch (e: Exception) {
                // Offline Fallback for Al-Fatihah or default mock
                val fallbackVerses = if (surahNumber == 1) {
                    listOf(
                        VerseItem(1, "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", "Dengan nama Allah Yang Maha Pengasih, Maha Penyayang."),
                        VerseItem(2, "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ", "Segala puji bagi Allah, Tuhan seluruh alam,"),
                        VerseItem(3, "الرَّحْمَٰنِ الرَّحِيمِ", "Yang Maha Pengasih, Maha Penyayang,"),
                        VerseItem(4, "مَالِكِ يَوْمِ الدِّينِ", "Pemilik hari pembalasan."),
                        VerseItem(5, "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ", "Hanya kepada Engkaulah kami menyembah dan hanya kepada Engkaulah kami mohon pertolongan."),
                        VerseItem(6, "اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ", "Tunjukilah kami jalan yang lurus,"),
                        VerseItem(7, "صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ", "(yaitu) jalan orang-orang yang telah Engkau beri nikmat kepadanya; bukan (jalan) mereka yang dimurkai, dan bukan (pula jalan) mereka yang sesat.")
                    )
                } else {
                    listOf(
                        VerseItem(1, "الۤمّۤ", "Alif Lam Mim"),
                        VerseItem(2, "ذٰلِكَ الْكِتٰبُ لَا رَيْبَ ۛ فِيْهِ ۛ هُدًى لِّلْمُتَّقِيْنَۙ", "Kitab (Al-Qur'an) ini tidak ada keraguan padanya; petunjuk bagi mereka yang bertakwa,"),
                        VerseItem(3, "الَّذِيْنَ يُؤْمِنُوْنَ بِالْغَيْبِ وَيُقِيْمُوْنَ الصَّلٰوةَ وَمِمَّا رَزَقْنٰهُمْ يُنْفِقُوْنَۙ", "(yaitu) mereka yang beriman kepada yang gaib, melaksanakan shalat, dan menginfakkan sebagian rezeki yang Kami berikan kepada mereka,")
                    )
                }
                _detailUiState.value = SurahDetailUiState(
                    number = surahNumber,
                    name = surahName,
                    verses = fallbackVerses,
                    isLoading = false,
                    error = "Koneksi lambat. Menampilkan data luring."
                )
            }
        }
    }

    fun saveLastRead(location: String) {
        viewModelScope.launch {
            userPreferences.setLastReadQuranLocation(location)
        }
    }

    fun addReadingTime(seconds: Int) {
        viewModelScope.launch {
            userPreferences.addReadingDuration(seconds)
            userPreferences.updateStreak()
        }
    }
}
