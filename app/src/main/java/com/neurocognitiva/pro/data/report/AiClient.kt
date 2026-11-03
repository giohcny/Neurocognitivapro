package com.neurocognitiva.pro.report

import com.neurocognitiva.pro.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Cliente mínimo de Gemini (sin dependencias extra). La clave se toma de local.properties
 * (BuildConfig). ADVERTENCIA: una clave dentro del APK puede extraerse; para producción
 * usa un backend propio que haga de intermediario y guarde la clave.
 */
object AiClient {
    fun isConfigured(): Boolean = BuildConfig.GEMINI_API_KEY.isNotBlank()

    suspend fun generate(systemPrompt: String, userPrompt: String): Result<String> = withContext(Dispatchers.IO) {
        var lastError: Throwable = IllegalStateException("Sin respuesta")
        val delays = longArrayOf(1000, 2000, 4000)
        for (attempt in 0..2) {
            try {
                return@withContext Result.success(call(systemPrompt, userPrompt))
            } catch (e: Exception) {
                lastError = e
                if (attempt < 2) delay(delays[attempt])
            }
        }
        Result.failure(lastError)
    }

    private fun call(systemPrompt: String, userPrompt: String): String {
        val url = URL("https://generativelanguage.googleapis.com/v1beta/models/${BuildConfig.GEMINI_MODEL}:generateContent")
        val body = JSONObject()
            .put("systemInstruction", JSONObject().put("parts", JSONArray().put(JSONObject().put("text", systemPrompt))))
            .put(
                "contents",
                JSONArray().put(
                    JSONObject().put("role", "user").put("parts", JSONArray().put(JSONObject().put("text", userPrompt)))
                )
            )
        val conn = url.openConnection() as HttpURLConnection
        try {
            conn.requestMethod = "POST"
            conn.connectTimeout = 15_000
            conn.readTimeout = 60_000
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            conn.setRequestProperty("x-goog-api-key", BuildConfig.GEMINI_API_KEY)
            conn.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val text = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            if (code !in 200..299) error("HTTP $code")
            return JSONObject(text)
                .getJSONArray("candidates").getJSONObject(0)
                .getJSONObject("content").getJSONArray("parts")
                .getJSONObject(0).getString("text")
        } finally {
            conn.disconnect()
        }
    }
}
