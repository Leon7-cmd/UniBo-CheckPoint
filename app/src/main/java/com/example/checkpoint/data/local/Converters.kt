package com.example.checkpoint.data.local

import androidx.room.TypeConverter

/**
 * Room type converters for serializing and deserializing string collections into delimited storage strings.
 */
object Converters {
    private const val SEPARATOR = "|||"

    @TypeConverter
    @JvmStatic
    fun fromStringList(value: List<String>?): String {
        return value?.mapNotNull { it.trim().ifEmpty { null } }?.joinToString(SEPARATOR).orEmpty()
    }

    @TypeConverter
    @JvmStatic
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrBlank()) return emptyList()
        return value.split(SEPARATOR).mapNotNull { it.trim().ifEmpty { null } }
    }
}