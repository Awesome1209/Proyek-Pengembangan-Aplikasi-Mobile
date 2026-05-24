package com.example.hujjah.presentation.screens.lens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hujjah.domain.model.islamic.BookmarkReference
import com.example.hujjah.domain.model.islamic.IslamicReference
import com.example.hujjah.domain.model.islamic.SourceType
import com.example.hujjah.domain.repository.AIRepository
import com.example.hujjah.domain.repository.hujjah.BookmarkRepository
import com.example.hujjah.domain.repository.hujjah.HujjahRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

data class ChatMessage(
    val id: String,
    val sender: Sender,
    val text: String,
    val timestamp: Long,
    val references: List<IslamicReference> = emptyList(),
    val solutions: List<String> = emptyList()
)

enum class Sender {
    USER, AI
}

data class HujjahLensUiState(
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@Serializable
private data class GeminiIslamicResponse(
    val counselorResponse: String,
    val solutions: List<String> = emptyList(),
    val references: List<GeminiReferenceItem> = emptyList()
)

@Serializable
private data class GeminiReferenceItem(
    val sourceType: String, // QURAN or HADITH
    val title: String,
    val sourceName: String,
    val arabicText: String,
    val translation: String,
    val explanation: String
)

class HujjahLensViewModel(
    private val hujjahRepository: HujjahRepository,
    private val aiRepository: AIRepository,
    private val bookmarkRepository: BookmarkRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HujjahLensUiState())
    val uiState: StateFlow<HujjahLensUiState> = _uiState.asStateFlow()

    private val jsonParser = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    init {
        // Initial welcoming message from AI Counselor
        _uiState.value = HujjahLensUiState(
            messages = listOf(
                ChatMessage(
                    id = "welcome",
                    sender = Sender.AI,
                    text = "Assalamualaikum, saya adalah Hujjah Lens. Ceritakan apa yang sedang mengganjal di hatimu saat ini, atau gunakan filter emosi di bawah. Saya akan mencarikan dalil yang menenangkan jiwamu.",
                    timestamp = Clock.System.now().toEpochMilliseconds()
                )
            )
        )
    }

    fun onInputTextChanged(value: String) {
        _uiState.value = _uiState.value.copy(inputText = value)
    }

    fun sendUserMessage(text: String) {
        if (text.isBlank()) return

        val userMessage = ChatMessage(
            id = "msg-${Clock.System.now().toEpochMilliseconds()}",
            sender = Sender.USER,
            text = text,
            timestamp = Clock.System.now().toEpochMilliseconds()
        )

        val updatedMessages = _uiState.value.messages + userMessage
        _uiState.value = _uiState.value.copy(
            messages = updatedMessages,
            inputText = "",
            isLoading = true,
            errorMessage = null
        )

        viewModelScope.launch {
            val systemPrompt = """
                Kamu adalah Hujjah Lens, Konselor Spiritual Islam berbasis AI yang berempati, bijaksana, dan menenangkan. 
                Tugasmu adalah menganalisis keluhan emosional pengguna (seperti kecemasan, kesedihan, kemarahan, keraguan, rasa bersyukur, dll.), memberikan respon konseling yang menenangkan dan relevan, lalu menyusun "Solusi Berdalil" berdasarkan kutipan ayat Al-Qur'an dan Hadis yang sahih.

                FORMAT JAWABAN:
                Kamu wajib menjawab dalam bentuk JSON murni dengan format persis seperti di bawah ini, tanpa tambahan teks pembuka markdown atau tanda kutip di luar JSON:
                {
                  "counselorResponse": "Tanggapan konseling yang hangat, berempati, dan menenangkan pengguna...",
                  "solutions": [
                    "Saran praktis pertama yang berlandaskan dalil...",
                    "Saran praktis kedua yang berlandaskan dalil..."
                  ],
                  "references": [
                    {
                      "sourceType": "QURAN", 
                      "title": "Judul Konteks Ayat",
                      "sourceName": "QS. NamaSurah: NomorAyat",
                      "arabicText": "Teks Arab Ayat",
                      "translation": "Terjemahan Bahasa Indonesia",
                      "explanation": "Penjelasan singkat relevansi ayat ini dengan kondisi pengguna."
                    },
                    {
                      "sourceType": "HADITH",
                      "title": "Judul Konteks Hadis",
                      "sourceName": "HR. NamaPerawi (Bukhari/Muslim/dll)",
                      "arabicText": "Teks Arab Hadis",
                      "translation": "Terjemahan Bahasa Indonesia",
                      "explanation": "Penjelasan singkat relevansi hadis ini dengan kondisi pengguna."
                    }
                  ]
                }
                
                Aturan penting:
                1. Jika keluhan pengguna bernada positif atau rasa bersyukur, respon dengan apresiasi spiritual (Tashakur) dan dalil tentang bersyukur.
                2. Teks Arab wajib ditulis dengan harakat lengkap dan rapi.
                3. Pastikan format JSON valid agar aplikasi tidak crash saat memparsing.
            """.trimIndent()

            val result = aiRepository.chat(
                message = "$systemPrompt\n\nKondisi/Keluhan Pengguna saat ini: \"$text\""
            )

            result.fold(
                onSuccess = { responseText ->
                    try {
                        // Extract JSON if AI wrapped it in markdown codeblocks
                        val cleanJson = responseText
                            .trim()
                            .removePrefix("```json")
                            .removeSuffix("```")
                            .trim()

                        val parsed = jsonParser.decodeFromString<GeminiIslamicResponse>(cleanJson)

                        val mappedReferences = parsed.references.map { item ->
                            IslamicReference(
                                id = "ref-${Clock.System.now().toEpochMilliseconds()}-${item.sourceName.hashCode()}",
                                sourceType = if (item.sourceType.uppercase() == "QURAN") SourceType.QURAN else SourceType.HADITH,
                                title = item.title,
                                sourceName = item.sourceName,
                                arabicText = item.arabicText,
                                translation = item.translation,
                                explanation = item.explanation,
                                topicId = "ai-lens",
                                topicTitle = "Hujjah Lens"
                            )
                        }

                        val aiMessage = ChatMessage(
                            id = "msg-${Clock.System.now().toEpochMilliseconds()}-ai",
                            sender = Sender.AI,
                            text = parsed.counselorResponse,
                            timestamp = Clock.System.now().toEpochMilliseconds(),
                            references = mappedReferences,
                            solutions = parsed.solutions
                        )

                        _uiState.value = _uiState.value.copy(
                            messages = _uiState.value.messages + aiMessage,
                            isLoading = false
                        )
                    } catch (e: Exception) {
                        // Fallback in case JSON parsing fails
                        val aiFallbackMessage = ChatMessage(
                            id = "msg-${Clock.System.now().toEpochMilliseconds()}-ai",
                            sender = Sender.AI,
                            text = responseText,
                            timestamp = Clock.System.now().toEpochMilliseconds()
                        )
                        _uiState.value = _uiState.value.copy(
                            messages = _uiState.value.messages + aiFallbackMessage,
                            isLoading = false
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Gagal mendapatkan tanggapan AI"
                    )
                }
            )
        }
    }

    fun saveBookmark(reference: IslamicReference) {
        viewModelScope.launch {
            bookmarkRepository.saveBookmark(reference)
        }
    }

    fun deleteMessage(messageId: String) {
        val updated = _uiState.value.messages.filter { it.id != messageId }
        _uiState.value = _uiState.value.copy(messages = updated)
    }
}
