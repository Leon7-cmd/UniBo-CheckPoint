# 🎮 Checkpoint

![Android](https://img.shields.io/badge/Android-3DDC84?style=flat-square&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=flat-square&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white)
![Firebase](https://img.shields.io/badge/Firebase-FFCA28?style=flat-square&logo=firebase&logoColor=black)
![SQLite](https://img.shields.io/badge/SQLite-003B57?style=flat-square&logo=sqlite&logoColor=white)

> *Un'applicazione Android nativa per la raccolta dei videogiochi e il tracciamento degli achievement.*

## 🌟 Punti chiave

- **Tracciamento & Libreria:** Organizzazione della propria collezione videoludica per stato di avanzamento (*Preferiti*, *Completati*, *In Corso*).
- **Gamification & Social:** Sistema di progressione a livelli con XP, badge sbloccabili, gestione amici tramite Friend Code univoco (`CKP-XXXX`) e controllo personalizzato della privacy.
- **Architettura MVVM Reattiva:** Interfaccia dichiarativa realizzata con Jetpack Compose e Material Design 3, sincronizzata tramite `StateFlow` e Kotlin Coroutines.
- **Strategia Offline-First:** Persistenza locale SQLite gestita con Room per caching istantaneo e sincronizzazione cloud su Firebase (Auth e Cloud Firestore).
- **Integrazioni Esterne:** Ricerca globale e metadati multimediali tramite IGDB (Twitch OAuth), Steam Web API e RetroAchievements, con caricamento asincrono delle immagini via Coil.

## ℹ️ Panoramica

**Checkpoint** è una piattaforma Android progettata per i videogiocatori che desiderano centralizzare la propria libreria di giochi, monitorare i propri progressi e confrontarsi con la community. 

## 🚀 Funzionalità Principali

L'applicazione è strutturata attorno a 5 sezioni di navigazione rapida accessibili tramite Bottom Bar:

1. **Profilo:** Monitoraggio del livello giocatore, barra di avanzamento XP, Codice Amico e bacheca dei badge ottenuti.
2. **Libreria:** Gestione a caroselli orizzontali dei titoli salvati, con filtri rapidi per stato e consultazione immediata anche in assenza di rete.
3. **Cerca:** Esplorazione del catalogo videoludico tramite le API di IGDB, anche attraverso l'utilizzo di filtri.
4. **Dettaglio Titolo:** Scheda multimediale con immagini, trama e tab dedicate per trofei/achievements e recensioni.
5. **Amici:** Ricerca di altri utenti via codice amico (`CKP-000000`), gestione asincrona delle richieste in arrivo, elenco amici e pagina amico visualizzabile con la loro libreria e statistiche.
6. **Impostazioni & Privacy:** Gestione del tema (tramite anche la selezione del colore secondario) e regolazione personalizzata della visibilità per statistiche, badge e libreria (*Pubblico*, *Solo Amici*, *Privato*).

## 🛠️ Tecnologie & Architettura

- **Linguaggio & Framework:** Kotlin, Jetpack Compose, Material Design 3.
- **Architettura:** Model-View-ViewModel (MVVM), StateFlow, Kotlin Coroutines, Single Source of Truth pattern.
- **Persistenza Locale:** Room Database (SQLite), DataStore Preferences.
- **Backend & Cloud:** Firebase Authentication, Cloud Firestore.
- **Media & Networking:** Retrofit, Coil (caricamento e caching asincrono delle immagini).
- **API Esterne:** IGDB API (Twitch OAuth 2.0), Steam Web API, RetroAchievements API.

## ⬇️ Download e Installazione

Per provare direttamente l'applicazione sul tuo dispositivo Android:

1. Vai alla sezione releases del repository.
2. Scarica il file **`checkpoint-app-release.apk`** relativo all'ultima versione disponibile.
3. Trasferisci o apri il file `.apk` direttamente sul tuo smartphone Android.
4. Se richiesto dal sistema operativo, autorizza l'installazione di app da origini sconosciute nelle impostazioni di sicurezza del dispositivo e procedi con l'installazione.
