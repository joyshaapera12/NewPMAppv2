package com.example.core.network

import android.content.Context
import android.content.SharedPreferences
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import java.util.concurrent.ConcurrentHashMap

class PersistentCookieJar(context: Context) : CookieJar {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("pitchmetrics_cookies", Context.MODE_PRIVATE)
    private val cookieStore = ConcurrentHashMap<String, MutableMap<String, Cookie>>()

    init {
        loadCookiesFromPrefs()
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val host = url.host
        val hostCookies = cookieStore.getOrPut(host) { ConcurrentHashMap() }
        for (cookie in cookies) {
            hostCookies[cookie.name] = cookie
        }
        persistCookiesToPrefs()
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val host = url.host
        val hostCookies = cookieStore[host] ?: return emptyList()
        val now = System.currentTimeMillis()
        val validCookies = mutableListOf<Cookie>()
        val it = hostCookies.entries.iterator()
        while (it.hasNext()) {
            val entry = it.next()
            val cookie = entry.value
            if (cookie.expiresAt < now) {
                it.remove()
            } else {
                validCookies.add(cookie)
            }
        }
        return validCookies
    }

    fun clearAllCookies() {
        cookieStore.clear()
        prefs.edit().clear().apply()
    }

    private fun persistCookiesToPrefs() {
        val editor = prefs.edit()
        editor.clear()
        for ((host, cookies) in cookieStore) {
            for ((name, cookie) in cookies) {
                val key = "$host|$name"
                val value = "${cookie.value}|${cookie.expiresAt}|${cookie.path}|${cookie.secure}|${cookie.httpOnly}"
                editor.putString(key, value)
            }
        }
        editor.apply()
    }

    private fun loadCookiesFromPrefs() {
        val all = prefs.all
        val now = System.currentTimeMillis()
        for ((key, value) in all) {
            if (value is String) {
                val parts = key.split("|")
                if (parts.size == 2) {
                    val host = parts[0]
                    val name = parts[1]
                    val cookieParts = value.split("|")
                    if (cookieParts.size >= 5) {
                        val cookieVal = cookieParts[0]
                        val expiresAt = cookieParts[1].toLongOrNull() ?: (now + 86400000)
                        val path = cookieParts[2]
                        val secure = cookieParts[3].toBoolean()
                        val httpOnly = cookieParts[4].toBoolean()

                        if (expiresAt > now) {
                            val builder = Cookie.Builder()
                                .name(name)
                                .value(cookieVal)
                                .domain(host)
                                .path(path)
                                .expiresAt(expiresAt)
                            if (secure) builder.secure()
                            if (httpOnly) builder.httpOnly()
                            val cookie = builder.build()
                            val hostCookies = cookieStore.getOrPut(host) { ConcurrentHashMap() }
                            hostCookies[name] = cookie
                        }
                    }
                }
            }
        }
    }
}
