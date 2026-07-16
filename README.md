# Shopping List — Android

A native Android port of my Vue shopping-list web app, built with **Kotlin** and **Jetpack Compose**. The app is a second client of the same REST API the web frontend uses — the backend is unchanged.

## Tech stack

- **Language:** Kotlin (coroutines for all async work)
- **UI:** Jetpack Compose (Material 3), Navigation Compose
- **State:** ViewModel + `StateFlow` with explicit Loading / Success / Error UI states
- **Networking:** Retrofit + OkHttp (logging interceptor) + kotlinx-serialization
- **Build:** Gradle with version catalog (`gradle/libs.versions.toml`)

## Project structure

```
app/src/main/java/com/example/shoppinglist/
├── MainActivity.kt              # NavHost: "lists" and "detail/{listId}" routes
├── data/
│   ├── api/
│   │   ├── ApiService.kt        # Retrofit interface (all 9 endpoints)
│   │   └── RetrofitClient.kt    # Retrofit/OkHttp singleton, base URL
│   └── models/Models.kt         # @Serializable data classes + request bodies
├── lists/
│   ├── ListsScreen.kt           # Home: all lists + create/rename/delete dialogs
│   ├── ListsViewModel.kt        # load / create / rename / delete lists
│   ├── ListDetailScreen.kt      # One list: items, checkboxes, TopAppBar w/ back
│   ├── ListDetailViewModel.kt   # load / add / toggle / edit / delete items
│   └── ItemFormDialog.kt        # Shared form for add AND edit (≈ ItemForm.vue)
└── ui/
    ├── ListsUiState.kt          # Sealed Loading / Success / Error state
    ├── DetailUiState.kt         # Same pattern for the detail screen
    └── theme/                   # Material 3 theme
```

## Web → Android concept map

The port is a frontend rewrite; every Vue concept has a Compose equivalent:

| Vue (web app) | Compose (this app) |
|---|---|
| `.vue` component | `@Composable` function |
| Route + `vue-router` | Screen composable + `NavHost` route |
| `$route.params.id` | Nav argument via `SavedStateHandle["listId"]` |
| `axios` service module (`api.js`) | Retrofit interface (`ApiService.kt`) |
| `ref()` reactive state | `StateFlow` (ViewModel) / `remember { mutableStateOf() }` (local) |
| Fetch in `onMounted` | `viewModelScope.launch` in ViewModel `init` |
| `v-for` | `LazyColumn` + `items()` |
| `v-if` for a modal | `if (showDialog) { AlertDialog(...) }` |
| Shared `ItemForm.vue` for create/edit | Shared `ItemFormDialog` with nullable `initial: Item?` |

## Key code pieces

### 1. One sealed UI state per screen

Every screen renders from a single state object — no scattered loading flags:

```kotlin
sealed interface ListsUiState {
    data object Loading : ListsUiState
    data class Success(val lists: List<ShoppingList>) : ListsUiState
    data class Error(val message: String) : ListsUiState
}
```

The screen just uses `when` expressions to render the state over it; the compiler forces all three branches to exist.

### 2. ViewModel owns the data, and the screen observes it

```kotlin
private val _uiState = MutableStateFlow<ListsUiState>(ListsUiState.Loading)
val uiState: StateFlow<ListsUiState> = _uiState.asStateFlow()

init { loadLists() }   // ≈ fetch in onMounted
```

In the composable: `val state by viewModel.uiState.collectAsState()` — any new value recomposes the screen automatically.

### 3. Route params without a factory

Navigation Compose puts route arguments into `SavedStateHandle` for free:

```kotlin
class ListDetailViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {
    private val listId: String = checkNotNull(savedStateHandle["listId"])
}
```

### 4. State is replaced but never mutated

Compose only recomposes when the `StateFlow` gets a *new* value. So updates
build new lists instead of editing in place:

```kotlin
// toggle: swap one item for the server's updated copy
items = current.items.map { if (it.id == item.id) response.data else it }

// delete: filter it out
items = current.items.filter { it.id != item.id }
```

### 5. Dialog state patterns

Two idioms cover every dialog in the app:

```kotlin
var showDialog by remember { mutableStateOf(false) }            // simple open/close
var listToDelete by remember { mutableStateOf<ShoppingList?>(null) }  // open AND carries the target
```

The nullable version renders with `listToDelete?.let { list -> AlertDialog(...) }` —
`null` means closed, non-null means open *and* knows which list it's about.

### 6. One form, two modes

`ItemFormDialog` serves both add and edit, exactly like `ItemForm.vue` on the web.
`initial = null` means create; a real item prefills the fields:

```kotlin
var name by remember(initial) { mutableStateOf(initial?.name ?: "") }
```

`remember(initial)` uses `initial` as a **key** — the state resets when you open
the dialog for a different item, instead of showing stale text.

## Gotchas worth remembering

- **`modifier` vs `Modifier`:** the lowercase parameter carries what the caller passed
  (like Scaffold padding) — apply it once at the screen's root; capital `Modifier` starts
  fresh and is for children.
- **Kotlin JSON parsing is strict:** every model field must have a default or nullable type
  unless *every* endpoint returns it. A missing default on `listId` made item-creation
  *look* failed even though the POST succeeded — although the 'write' worked, only response parsing broke.
- **`localhost` on the emulator is the emulator.** The host machine is `10.0.2.2`, and
  Android blocks plain HTTP unless the manifest allows cleartext (dev only).
- **Watch auto-imports:** Android Studio will happily import `java.util.Collections.list`
  or `android.R` and turn a typo into a confusing type error.
- **Material slots take composables, not strings:** `label = { Text("Name") }`, never `label = "Name"`.
- **Confirm-before-delete means rerouting the trigger:** the destructive call moves *into*
  the dialog's 'confirm' button; the icon only nominates the target.

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

### The backend

The REST API is the **same server the Vue web app uses** — it lives in that project
(`shopping-list/src/server1`) and is deliberately *not* duplicated here: one backend,
two clients. For convenience this repo uses a small launcher script, `run-backend.sh`
(gitignored, since it hardcodes a machine-specific path):

```bash
#!/bin/bash
SERVER_DIR="$HOME/path/to/shopping-list/src/server1"   # adjust to your machine

if ! nc -z 127.0.0.1 27017 2>/dev/null; then
  echo "MongoDB isn't running — start it first (e.g. 'brew services start mongodb-community')"
  exit 1
fi

exec npm --prefix "$SERVER_DIR" start
```

It checks MongoDB is up, then starts the server from its real home. Day-to-day
workflow: `./run-backend.sh` in Android Studio's terminal tab, then Run the app.

### The app

1. **Start the backend** (see above — it must be listening on port `8000`).
2. **Open the project in Android Studio** and let Gradle sync.
3. **Run on an emulator.** The base URL in `RetrofitClient.kt` is `http://10.0.2.2:8000/api/` — `10.0.2.2` is how the emulator reaches your computer's `localhost`.
   - On a **physical phone**, change the base URL to your computer's LAN IP (both devices on the same Wi-Fi), e.g. `http://192.168.1.x:8000/api/`.
4. Plain HTTP is allowed for development via `android:usesCleartextTraffic="true"` in the manifest; switch to HTTPS before any real release.

## Status

- [x] Data models, Retrofit API layer, networking setup
- [x] Lists screen with Loading / Error / Retry
- [x] Navigation: list → detail with `listId` argument, TopAppBar with back arrow
- [x] List detail screen: items, checkbox toggle
- [x] Create / rename / delete lists (delete with confirmation)
- [x] Add / edit / delete items via the shared form dialog

**Full feature parity with the Vue web app.** Possible future work: snackbar-with-undo
for deletes, pull-to-refresh, favorites, offline caching with Room.
