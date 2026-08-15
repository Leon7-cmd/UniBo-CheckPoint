package com.example.checkpoint.data.remote.retro.util

// Utility that takes a gameName from IGDB and cleans it to easy search it on RetroAchivemnts.
fun String.sanitizeForMatching(): String {
    return this.lowercase()
        // Removes everything in parentheses, brackets, braces
        .replace(Regex("""\([^)]*\)"""), "")
        .replace(Regex("""\[[^]]*\]"""), "")
        .replace(Regex("""\{[^}]*\}"""), "")
        // Substitute special characters with blank spaces
        .replace(Regex("""[^a-z0-9]"""), " ")
        // Collapse multiple spaces into one
        .replace(Regex("""\s+"""), " ")
        .trim()
}