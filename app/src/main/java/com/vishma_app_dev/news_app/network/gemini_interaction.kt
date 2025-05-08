//package com.vishma_app_dev.news_app.models
//
//import com.google.ai.client.generativeai.GenerativeModel
//import com.google.ai.client.generativeai.type.Content
//import com.google.ai.client.generativeai.type.GenerateContentResponse
//import com.google.ai.client.generativeai.type.GenerationConfig
//import com.google.ai.client.generativeai.type.Part
//import com.google.ai.client.generativeai.type.SafetySetting
//
//data class GeminiChatRequest(
//    val contents: List<GeminiContent>
//)
//
//data class GeminiContent(
//    val role: String? = null,
//    val parts: List<GeminiPart>
//)
//
//data class GeminiPart(
//    val text: String? = null
//)
//
//suspend fun chatWithGemini(
//    apiKey: String,
//    modelName: String = "gemini-1.5-flash", // Or try "gemini-pro"
//    messages: List<Content>,
//    generationConfig: GenerationConfig? = null,
//    safetySettings: List<SafetySetting>? = null
//): GenerateContentResponse {
//    val generativeModel = GenerativeModel(
//        modelName = modelName,
//        apiKey = apiKey,
//        generationConfig = generationConfig,
//        safetySettings = safetySettings
//    )
//    val chat = generativeModel.startChat(
//        history = messages
//    )
//
//    return chat.sendMessage(messages.last().parts.first())
//}