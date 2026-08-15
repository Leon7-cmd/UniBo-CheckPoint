package com.example.checkpoint.ui.sections.search.components.filter

data class FilterTag(
    val label: String,      // Text shown to the user
    val igdbValue: String   // Exact match value in the IGDB API
)

object IgdbFilterTags {

    // CONSOLE & PLATFORMS + RETRO
    val CONSOLES = listOf(
        FilterTag("PC", "PC (Microsoft Windows)"),
        FilterTag("PlayStation 5", "PlayStation 5"),
        FilterTag("PlayStation 4", "PlayStation 4"),
        FilterTag("Nintendo Switch", "Nintendo Switch"),
        FilterTag("Xbox Series X|S", "Xbox Series X|S"),
        FilterTag("Xbox One", "Xbox One"),
        FilterTag("PlayStation 3", "PlayStation 3"),
        FilterTag("Xbox 360", "Xbox 360"),
        FilterTag("PlayStation 2", "PlayStation 2"),
        FilterTag("PlayStation", "PlayStation"),
        FilterTag("Wii U", "Wii U"),
        FilterTag("Wii", "Wii"),
        FilterTag("GameCube", "Nintendo GameCube"),
        FilterTag("Nintendo 64", "Nintendo 64"),
        FilterTag("SNES", "Super Nintendo Entertainment System (SNES)"),
        FilterTag("NES", "Nintendo Entertainment System (NES)"),
        FilterTag("Nintendo 3DS", "Nintendo 3DS"),
        FilterTag("Nintendo DS", "Nintendo DS"),
        FilterTag("Game Boy Advance", "Game Boy Advance"),
        FilterTag("Game Boy Color", "Game Boy Color"),
        FilterTag("Game Boy", "Game Boy"),
        FilterTag("PSP", "PlayStation Portable"),
        FilterTag("PS Vita", "PlayStation Vita"),
        FilterTag("Sega Mega Drive", "Sega Mega Drive/Genesis"),
        FilterTag("Sega Dreamcast", "Sega Dreamcast"),
        FilterTag("Sega Saturn", "Sega Saturn"),
        FilterTag("Arcade", "Arcade")
    )

    // GENRE
    val GENRES = listOf(
        FilterTag("Azione", "Action"),
        FilterTag("Avventura", "Adventure"),
        FilterTag("GDR / RPG", "Role-playing (RPG)"),
        FilterTag("Platform", "Platform"),
        FilterTag("Sparatutto", "Shooter"),
        FilterTag("Horror", "Horror"),
        FilterTag("Stealth", "Stealth"),
        FilterTag("Strategia", "Strategy"),
        FilterTag("Simulazione", "Simulator"),
        FilterTag("Puzzle", "Puzzle"),
        FilterTag("Corse", "Racing"),
        FilterTag("Sport", "Sport"),
        FilterTag("Picchiaduro", "Fighting"),
        FilterTag("Indie", "Indie"),
        FilterTag("Arcade", "Arcade"),
        FilterTag("Visual Novel", "Visual Novel"),
        FilterTag("Card & Board Game", "Card & Board Game"),
        FilterTag("Tattico", "Tactical"),
        FilterTag("A Turni (TBS)", "Turn-based strategy (TBS)"),
        FilterTag("Strategia in T.R. (RTS)", "Real Time Strategy (RTS)"),
        FilterTag("MOBA", "MOBA"),
        FilterTag("Musicale / Rhythm", "Music"),
        FilterTag("Pinball", "Pinball"),
        FilterTag("Point-and-click", "Point-and-click"),
        FilterTag("Quiz / Trivia", "Quiz/Trivia")
    )

    // GAMEPLAY & THEMATIC
    val GAMEPLAY_AND_THEMES = listOf(
        FilterTag("Singleplayer", "Single player"),
        FilterTag("Multiplayer", "Multiplayer"),
        FilterTag("Co-op", "Co-operative"),
        FilterTag("Open World", "Open world"),
        FilterTag("Fantasy", "Fantasy"),
        FilterTag("Fantascienza", "Science fiction"),
        FilterTag("Sopravvivenza", "Survival"),
        FilterTag("Mistero / Thriller", "Thriller"),
        FilterTag("Storico", "Historical"),
        FilterTag("Post-Apocalittico", "Post-apocalyptic"),
        FilterTag("Cyberpunk", "Cyberpunk"),
        FilterTag("Guerra", "Warfare"),
        FilterTag("Sandbox", "Sandbox"),
        FilterTag("Commedia", "Comedy"),
        FilterTag("Drammatico", "Drama"),
        FilterTag("Split Screen", "Split screen"),
        FilterTag("MMO", "Massively Multiplayer Online (MMO)"),
        FilterTag("Battle Royale", "Battle Royale")
    )
}