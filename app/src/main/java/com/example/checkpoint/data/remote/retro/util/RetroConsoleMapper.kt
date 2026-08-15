package com.example.checkpoint.data.remote.retro.util

object RetroConsoleMapper {

    // Private utility that maps IGDB console names to RetroAchievements console IDs.
    private fun getRetroConsoleId(igdbPlatformName: String?): Int? {
        if (igdbPlatformName.isNullOrBlank()) return null

        val name = igdbPlatformName.lowercase()
        return when {
            // Nintendo Home Consoles
            name.contains("super nintendo") || name.contains("snes") || name.contains("super famicom") -> 3
            name.contains("nintendo 64") || name.contains("n64") -> 2
            name.contains("nintendo entertainment system") || name.contains("nes") || name.contains("famicom disk") -> 7
            name.contains("gamecube") -> 16
            name.contains("wii") && !name.contains("wii u") -> 19

            // Nintendo Handhelds
            name.contains("game boy advance") || name.contains("gba") -> 5
            name.contains("game boy color") || name.contains("gbc") -> 6
            name.contains("game boy") -> 4
            name.contains("nintendo ds") || name.equals("ds", ignoreCase = true) -> 18
            name.contains("nintendo dsi") -> 78
            name.contains("virtual boy") -> 28
            name.contains("pokemon mini") -> 24

            // SEGA
            name.contains("sega genesis") || name.contains("mega drive") -> 1
            name.contains("master system") -> 11
            name.contains("sega cd") || name.contains("mega cd") -> 9
            name.contains("32x") -> 10
            name.contains("game gear") -> 15
            name.contains("saturn") -> 39
            name.contains("dreamcast") -> 40
            name.contains("sg-1000") -> 33

            // Sony
            name.contains("playstation 1") || name.contains("playstation") || name.contains("ps1") || name.contains("psx") -> 12
            name.contains("playstation 2") || name.contains("ps2") -> 21
            name.contains("playstation portable") || name.contains("psp") -> 41

            // Atari
            name.contains("atari 2600") -> 25
            name.contains("atari 7800") -> 51
            name.contains("atari lynx") -> 13
            name.contains("atari jaguar") -> 17

            // PC Engine / TurboGrafx
            name.contains("pc engine cd") || name.contains("turbografx-cd") -> 76
            name.contains("pc engine") || name.contains("turbografx") -> 8

            // Arcade / Altri
            name.contains("arcade") -> 27
            name.contains("neogeo pocket") || name.contains("neo geo pocket") -> 14
            name.contains("neogeo cd") || name.contains("neo geo cd") -> 56
            name.contains("wonderswan") -> 53
            name.contains("colecovision") -> 44
            name.contains("vectrex") -> 46

            else -> null
        }
    }

    // Public function that maps a list of IGDB console names to a list of RetroAchievements console IDs.
    fun getRetroConsoleIds(igdbPlatformNames: List<String>?): List<Int> {
        if (igdbPlatformNames.isNullOrEmpty()) return emptyList()
        return igdbPlatformNames
            .mapNotNull { getRetroConsoleId(it) }
            .distinct()
    }
}