package com.deivarayan.kids.network

import android.util.Log
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

private const val TAG = "WikimediaImageRepo"
private const val CONNECT_TIMEOUT_MS = 5000
private const val READ_TIMEOUT_MS = 5000
private const val THUMBNAIL_WIDTH = 500

/**
 * Looks up a representative picture for a word on Wikimedia Commons (no API
 * key required). Falls back to null on any failure - offline, timeout, no
 * results - so callers can show the emoji instead.
 */
class WikimediaImageRepository {

    private val cache = ConcurrentHashMap<String, String?>()

    suspend fun fetchImageUrl(query: String): String? = withContext(Dispatchers.IO) {
        if (cache.containsKey(query)) return@withContext cache[query]
        val result = runCatching { fetch(query) }
            .onFailure { Log.w(TAG, "Image fetch failed for '$query'", it) }
            .onSuccess { Log.d(TAG, "Image fetch succeeded for '$query': $it") }
            .getOrNull()
        cache[query] = result
        result
    }

    private fun fetch(query: String): String? {
        val encoded = URLEncoder.encode(query, "UTF-8")
        val url = URL(
            "https://commons.wikimedia.org/w/api.php" +
                "?action=query&generator=search&gsrnamespace=6&gsrlimit=1" +
                "&gsrsearch=$encoded" +
                "&prop=imageinfo&iiprop=url&iiurlwidth=$THUMBNAIL_WIDTH&format=json",
        )
        val connection = url.openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = CONNECT_TIMEOUT_MS
            connection.readTimeout = READ_TIMEOUT_MS
            connection.setRequestProperty(
                "User-Agent",
                "KidsSpellingApp/1.0 (educational Android app; devrayan@gmail.com)",
            )
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw IOException("HTTP ${connection.responseCode}")
            }
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            val pages = JSONObject(body).getJSONObject("query").getJSONObject("pages")
            val firstKey = pages.keys().next()
            val imageInfo = pages.getJSONObject(firstKey).getJSONArray("imageinfo").getJSONObject(0)
            return if (imageInfo.has("thumburl")) imageInfo.getString("thumburl") else imageInfo.getString("url")
        } finally {
            connection.disconnect()
        }
    }
}
