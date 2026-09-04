# Shopping List — Android

A native Android port of my Vue shopping-list web app, built with **Kotlin** and **Jetpack Compose**. The app is a second client of the same REST API the web frontend uses — the backend is unchanged.

## Tech stack

- **Language:** Kotlin (coroutines + `Flow` for all async work)
- **UI:** Jetpack Compose (Material 3), Navigation Compose
- **State:** ViewModel + `StateFlow` with explicit Loading / Success / Error UI states
- **Networking:** Retrofit + OkHttp (logging interceptor) + kotlinx-serialization
- **Offline cache:** Room (entities, DAOs, reactive `Flow` queries) as the on-device source of truth
- **Build:** Gradle with version catalog (`gradle/libs.versions.toml`), KSP for Room codegen

## Project structure

```
app/src/main/java/com/example/shoppinglist/
├── MainActivity.kt              # NavHost: "lists" and "detail/{listId}" routes
├── data/
│   ├── api/
│   │   ├── ApiService.kt        # Retrofit interface (all 9 endpoints)
│   │   └── RetrofitClient.kt    # Retrofit/OkHttp singleton, base URL
│   ├── local/                   # Room: the offline cache
│   │   ├── ListEntity.kt        # @Entity for the shopping_lists table
│   │   ├── ItemEntity.kt        # @Entity for the items table
│   │   ├── ListDao.kt           # @Dao: observeAll() Flow, upsert, clear
│   │   ├── ItemDao.kt           # @Dao: observeItemsForList() Flow, upsert, clear
│   │   ├── AppDatabase.kt       # @Database tying entities + DAOs together
│   │   ├── DatabaseBuilder.kt   # thread-safe singleton (Room.databaseBuilder)
│   │   └── Mappers.kt           # entity <-> model conversions (toEntity / toModel)
│   ├── ShoppingListRepository.kt # observe (Room) + refresh (network -> Room)
│   └── models/Models.kt         # @Serializable data classes + request bodies
├── lists/
│   ├── ListsScreen.kt           # Home: all lists + create/rename/delete dialogs
│   ├── ListsViewModel.kt        # observe/refresh lists + create / rename / delete
│   ├── ListDetailScreen.kt      # One list: items, checkboxes, TopAppBar w/ back
│   ├── ListDetailViewModel.kt   # observe/refresh items + add / toggle / edit / delete
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
| — (no offline layer on web) | Room cache + `ShoppingListRepository` (source of truth) |
| `ref()` reactive state | `StateFlow` (ViewModel) / `remember { mutableStateOf() }` (local) |
| Fetch in `onMounted` | Observe Room `Flow` + kick off a network `refresh()` in ViewModel `init` |
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

### 2. Offline-first: Room is the source of truth, the network only refreshes it

The ViewModel no longer *owns* the list data — it **observes** Room and, separately,
**refreshes** Room from the network. Reads never fail (they come from the cache); a
failed refresh leaves the cached screen intact instead of showing an error.

```kotlin
private val _uiState = MutableStateFlow<ListsUiState>(ListsUiState.Loading)
val uiState: StateFlow<ListsUiState> = _uiState.asStateFlow()

init {
    observeLists()   // subscribe to the Room Flow (works offline, never fails)
    refresh()        // pull from the network INTO Room (may fail; swallowed)
}

private fun observeLists() = viewModelScope.launch {
    repository.observeLists().collect { lists ->
        _uiState.value = ListsUiState.Success(lists)   // Room emits -> UI updates
    }
}
```

The repository keeps the two paths cleanly separated:

```kotlin
// read: Room only, reactive, offline-safe
fun observeLists(): Flow<List<ShoppingList>> =
    listDao.observeAll().map { entities -> entities.map { it.toModel() } }

// write: hit the network, then upsert into Room (which re-emits to observers)
suspend fun refreshLists() {
    val response = api.getAllLists()
    if (response.success && response.data != null)
        listDao.upsertAll(response.data.map { it.toEntity() })
}
```

Because writes flow *through* Room, every mutation (create/rename/delete) just calls the
API and then `refresh()` — the observe loop updates the UI; the ViewModel never edits
`_uiState` by hand for successful writes.

In the composable: `val state by viewModel.uiState.collectAsState()` — any new value recomposes the screen automatically.

### 3. Route params via a ViewModel factory

Both ViewModels take a `ShoppingListRepository`, which needs the Room DAOs, which need a
`Context`. The default `viewModel()` provider can't supply that, so each ViewModel exposes
a `viewModelFactory`. It reads the `Application` (a `Context`) from `CreationExtras` via
`APPLICATION_KEY`, builds the database + repository, and — for the detail screen — also
pulls the nav-arg-backed `SavedStateHandle` out with `createSavedStateHandle()`.

```kotlin
companion object {
    val Factory = viewModelFactory {
        initializer {
            val app = this[APPLICATION_KEY] as Application
            val db = DatabaseBuilder.getDatabase(app)
            val repository = ShoppingListRepository(
                api = RetrofitClient.api,
                listDao = db.listDao(),
                itemDao = db.itemDao(),
            )
            ListDetailViewModel(
                savedStateHandle = createSavedStateHandle(),   // carries listId
                repository = repository,
            )
        }
    }
}
```

Both screens build their ViewModel through the factory:

```kotlin
// lists
viewModel: ListsViewModel = viewModel(factory = ListsViewModel.Factory)
// detail (entry-scoped, so the handle carries listId)
ListDetailScreen(viewModel = viewModel(factory = ListDetailViewModel.Factory), onBack = ...)
```

`DatabaseBuilder.getDatabase()` is a double-checked singleton, so both factories share one
database instance for the app's lifetime.

### 4. Mutations reconcile through Room, not by editing UI state

Compose only recomposes when the `StateFlow` gets a *new* value. With Room as the source
of truth, a successful mutation doesn't hand-edit `_uiState` — it just re-`refresh()`es,
and the Room `Flow` emits a fresh list that replaces the state:

```kotlin
fun deleteItem(item: Item) = viewModelScope.launch {
    val response = api.deleteItem(listId, item.id)
    if (response.success) refresh()          // network -> Room -> observe loop -> UI
    else _uiState.value = DetailUiState.Error(...)
}
```

**Tradeoff:** this drops the earlier *optimistic* updates (instantly swapping/filtering the
item before the server replies) in exchange for a single, consistent code path. Toggles and
deletes now wait for the round-trip; optimistic-update-with-rollback is possible future work
if snappier feedback is wanted.

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

## Testing

32 tests across two source sets, split by what they need to run:

**Unit tests** (`app/src/test`, JVM — no device) cover the ViewModel logic. Both
ViewModels are tested against a hand-written `FakeApiService` that returns canned
`ApiResponse`s (and can be told to throw), so every branch — success, API-reported
failure, and thrown exception — is exercised without a network:

- `ListsViewModelTest` (9) — load / create / rename / delete, incl. that a failed
  mutation surfaces `Error` and does **not** reload.
- `ListDetailViewModelTest` (12) — load / add / toggle / edit / delete items, incl.
  empty-list and immutable-replacement paths.

A `MainDispatcherRule` swaps in a test dispatcher so `viewModelScope` coroutines run
synchronously, and mutable fields on the fake (e.g. `getListCallCount`) let tests assert
that a successful create actually triggers a reload.

**Instrumented tests** (`app/src/androidTest`, Compose UI — needs an emulator/device)
render composables in isolation:

- `ListDetailScreenTest` (6) — drives a stateless `DetailContent` with hand-built
  `DetailUiState`s and asserts each renders (Loading / Error / Success) and that
  checkbox, delete, and retry fire the right callbacks. (The `when(state)` block was
  extracted out of `ListDetailScreen` specifically so it could be tested without a VM.)
- `ItemFormDialogTest` (5) — Save enable/disable, quantity fallback, blank→null
  optionals, and edit-mode prefill.

**Libraries:** JUnit4, `kotlinx-coroutines-test`, and `androidx.compose.ui.test` (JUnit4).

```bash
./gradlew testDebugUnitTest            # unit tests (fast, no device)
./gradlew connectedDebugAndroidTest    # UI tests (emulator/device must be running)
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
- [x] Test suite: 32 unit + instrumented tests (ViewModels, Compose UI, dialog)
- [x] Offline caching with Room: reads served from an on-device cache, background refresh

**Full feature parity with the Vue web app, plus offline reads the web app doesn't have.**
Possible future work: snackbar-with-undo for deletes, pull-to-refresh, favorites,
optimistic mutations with rollback, and caching the list *name* on the detail screen
(currently only items are cached; the name falls back to empty until a refresh lands).
