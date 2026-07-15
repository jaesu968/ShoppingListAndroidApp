# Shopping List — Android

A native Android port of my Vue shopping-list web app, built with **Kotlin** and **Jetpack Compose**. The app is a second client of the same REST API the web frontend uses — the backend is unchanged.

## Tech stack

- **UI:** Jetpack Compose (Material 3), Navigation Compose
- **State:** ViewModel + `StateFlow` with explicit Loading / Success / Error UI states
- **Networking:** Retrofit + OkHttp (logging interceptor) + kotlinx-serialization
- **Build:** Gradle with version catalog (`gradle/libs.versions.toml`)

## Project structure

```
app/src/main/java/com/example/shoppinglist/
├── MainActivity.kt              # NavHost + navigation scaffold
├── data/
│   ├── api/
│   │   ├── ApiService.kt        # Retrofit interface (all endpoints)
│   │   └── RetrofitClient.kt    # Retrofit/OkHttp singleton, base URL
│   └── models/Models.kt         # @Serializable data classes + request bodies
├── lists/
│   ├── ListsScreen.kt           # Home screen: all shopping lists
│   └── ListsViewModel.kt        # Fetches lists, exposes StateFlow<ListsUiState>
└── ui/
    ├── ListsUiState.kt          # Sealed Loading / Success / Error state
    └── theme/                   # Material 3 theme
```

## API

The app talks to the shopping-list backend at `/api`:

| Method | Path | Purpose |
|---|---|---|
| GET | `/lists` | All shopping lists |
| POST | `/lists` | Create a list |
| GET / PUT / DELETE | `/lists/{id}` | Read / rename / delete a list |
| GET / POST | `/lists/{listId}/items` | Items in a list / add item |
| PUT / DELETE | `/lists/{listId}/items/{itemId}` | Update / delete an item |

All responses are wrapped in `{ success, data, error, message }` (see `ApiResponse` in `Models.kt`).

## Running it

1. **Start the backend** on your computer (it must be listening on port `8000`).
2. **Open the project in Android Studio** and let Gradle sync.
3. **Run on an emulator.** The base URL in `RetrofitClient.kt` is `http://10.0.2.2:8000/api/` — `10.0.2.2` is how the emulator reaches your computer's `localhost`.
   - On a **physical phone**, change the base URL to your computer's LAN IP (both devices on the same Wi-Fi), e.g. `http://192.168.1.x:8000/api/`.
4. Plain HTTP is allowed for development via `android:usesCleartextTraffic="true"` in the manifest; switch to HTTPS before any real release.

## Status

- [x] Data models, Retrofit API layer, networking setup
- [x] Lists screen (fetch + display all shopping lists) with Loading / Error / Retry
- [x] Navigation skeleton (list → detail route with `listId` argument)
- [x] List detail screen (items, checkboxes)
- [ ] Create / rename / delete lists (in progress)
- [ ] Add / update / delete items
