# MySQL → Android Import Plan

## Overview

Import a phpMyAdmin `.sql` dump exported from the `hospital` web-app database into the
Android app's Room database. The user selects the file from device storage; the app
navigates to a dedicated progress screen that visualises each phase of the import in
real-time, then transitions to a success summary or a detailed error view.

---

## 1. Source vs. Target Schema

### 1.1 `patient_data`

| MySQL column | MySQL type | Room field | Room type | Conversion notes |
|---|---|---|---|---|
| `id` | `int` | `id` | `Int` | Direct |
| `firstName` | `varchar(20)` | `firstName` | `String` | `trim()` |
| `middleName` | `varchar(20)` | `middleName` | `String?` | empty string → `null` |
| `lastName` | `varchar(20)` | `lastName` | `String` | `trim()` |
| `age` | `int` | `age` | `Int` | Direct |
| `sex` | `varchar(10)` | `sex` | `String?` | Direct |
| `occupation` | `varchar(20)` | `occupation` | `String?` | Direct |
| `address` | `varchar(50)` | `address` | `String?` | Direct |
| `phone` | `varchar(10)` | `phone` | `String?` | `"2147483647"` → `null` (INT_MAX placeholder) |
| `regno` | `varchar(5)` | `regno` | `String?` | Direct |
| `height` | `int` | `height` | `Int?` | `0` → `null` |
| `weight` | `int` | `weight` | `Int?` | `0` → `null` |
| `diagnosis` | `tinytext` | `diagnosis` (**new**) | `String?` | Requires Room v2→v3 migration |
| `cc1`–`cc3` | `varchar(150)` | `cc1`–`cc3` | `String?` | Direct |
| `appetite`→`family_history` | various | same names | `String?` | Direct |
| `treatment` | `varchar(200)` | `treatment` | `String?` | Direct |
| `paid` | `varchar(5)` | `paid` | `String?` | Direct |
| `balance` | `varchar(5)` | `balance` | `String?` | Direct |
| `followUp1`–`followUp4` | `varchar(400)` | *(not in Room)* | — | **Discard** — legacy text blobs, superseded by `follow_up_data` |
| `dateJoined` | `datetime` | `dateJoined` | `Long?` | Parse `"yyyy-MM-dd HH:mm:ss"` → epoch millis |
| *(absent)* | — | `urlToImage` | `String?` | Set `null` |

### 1.2 `follow_up_data`

| MySQL column | MySQL type | Room field | Room type | Conversion notes |
|---|---|---|---|---|
| `followUpId` | `int` | `followUpId` | `Int` | Direct |
| `id` | `int` (FK) | `id` | `Int` | Must match an imported patient |
| `date` | `datetime` | `date` | `Long` | Parse `"yyyy-MM-dd HH:mm:ss"` → epoch millis |
| `regno` | `varchar(5)` | `regno` | `String` | Direct |
| `follow_up_num` | `varchar(4)` | `follow_up_num` | `Int` | `parseInt` or `0` |
| `weight` | `varchar(4)` e.g. `"63 K"` | `weight` | `Int` | Extract leading digits, ignore `"K"` suffix; `""` → `-1` |
| `treatment_output` | `varchar(10)` | `treatment_output` | `String` | Direct |
| `other_complains` | `varchar(300)` | `other_complains` | `String` | Direct |
| `treatment` | `varchar(300)` | `treatment` | `String` | Direct |
| `medicine_duration` | `varchar(20)` | `medicine_duration` | `String` | Direct |
| `paid` | `varchar(5)` | `paid` | `String` | Direct |
| `balance` | `varchar(5)` e.g. `" 00"`, `"450"` | `balance` | `Int` | `trim()` → `parseInt` or `-1` |

### 1.3 `admin_users`

**Skip entirely.** The Android app uses Firebase Authentication; no local admin table.

---

## 2. Known Data Quirks

| Quirk | Example | Fix |
|---|---|---|
| Phone = `2147483647` | Most patients | Map to `null` (INT_MAX was used as PHP placeholder) |
| Follow-up weight with suffix | `"63 K"`, `"62.5"`, `"80 k"` | Regex `\d+(\.\d+)?`, `toFloatOrNull()?.toInt()` |
| Balance with leading space | `" 00"`, `" 450"` | `trim()` before parse |
| `\r\n` inside string values | Treatment notes | Already embedded in SQL literal; preserve as-is |
| Empty string vs NULL | Many optional fields | Treat `""` and `"''"` as `null` for nullable Room fields |
| Legacy `followUp1–4` blobs | Some old patients | Discard; structured `follow_up_data` rows exist |
| MySQL auto-increment IDs | May collide with existing Room rows | `OnConflictStrategy.REPLACE` |

---

## 3. Room Database Migration (v2 → v3)

**`Patient.kt`** — add one field:
```kotlin
val diagnosis: String? = null
```

**`PatientDatabase.kt`** — bump version and add migration:
```kotlin
@Database(entities = [Patient::class, PatientFollowUp::class], version = 3)

private val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE patient_data ADD COLUMN diagnosis TEXT")
    }
}
// .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
```

---

## 4. New Files to Create

```
app/src/main/java/com/example/patientrecords/
├── data/
│   └── import/
│       ├── SqlImportParser.kt        # Pure Kotlin – no Android deps
│       ├── SqlImportMapper.kt        # Maps parsed rows → Room entities
│       ├── ImportProgress.kt         # Sealed class for progress events
│       └── ImportRepository.kt      # Emits Flow<ImportProgress>; runs transaction
└── ui/
    └── importsql/
        ├── ImportProgressActivity.kt # Progress + result screen
        ├── ImportViewModel.kt        # Collects Flow, exposes StateFlow<ImportUiState>
        └── activity_import_progress.xml

app/src/main/res/
└── layout/
    └── activity_import_progress.xml
```

**AndroidManifest.xml** — register `ImportProgressActivity` with `android:exported="false"`.

---

## 5. Data Model: Progress Events and UI State

### 5.1 `ImportProgress.kt` — emitted by the repository

```kotlin
sealed class ImportProgress {
    data class ReadingFile(val fileName: String)              : ImportProgress()
    object Parsing                                            : ImportProgress()
    data class InsertingPatients(val done: Int, val total: Int) : ImportProgress()
    data class InsertingFollowUps(val done: Int, val total: Int): ImportProgress()
    data class Completed(val result: ImportResult)            : ImportProgress()
    data class Failed(val reason: String, val partial: ImportResult?) : ImportProgress()
}
```

### 5.2 `ImportResult` — expanded to capture skipped rows

```kotlin
data class ImportResult(
    val patientsInserted: Int  = 0,
    val patientsSkipped: Int   = 0,
    val followUpsInserted: Int = 0,
    val followUpsSkipped: Int  = 0,
    val rowErrors: List<RowError> = emptyList(),
    val durationMs: Long       = 0L
)

data class RowError(
    val table: String,   // "patient_data" | "follow_up_data"
    val rowIndex: Int,
    val reason: String
)
```

### 5.3 `ImportUiState` — consumed by the Activity

```kotlin
sealed class ImportUiState {
    object Idle                                                  : ImportUiState()
    data class ReadingFile(val fileName: String)                 : ImportUiState()
    object Parsing                                               : ImportUiState()
    data class InsertingPatients(val done: Int, val total: Int)  : ImportUiState()
    data class InsertingFollowUps(val done: Int, val total: Int) : ImportUiState()
    data class Success(val result: ImportResult)                 : ImportUiState()
    data class PartialSuccess(val result: ImportResult)          : ImportUiState() // inserted some, skipped some
    data class Failure(val reason: String)                       : ImportUiState() // nothing was inserted
}
```

---

## 6. `ImportRepository.kt` — Flow-based progress

The repository replaces the one-shot `suspend` function with a `Flow` so the Activity can
react to each individual insert.

```kotlin
class ImportRepository(private val db: PatientDatabase) {

    fun importFromSql(sqlContent: String, fileName: String): Flow<ImportProgress> = flow {

        emit(ImportProgress.ReadingFile(fileName))
        val startMs = System.currentTimeMillis()

        emit(ImportProgress.Parsing)
        val patientRows  = SqlImportParser.parseTable(sqlContent, "patient_data")
        val followUpRows = SqlImportParser.parseTable(sqlContent, "follow_up_data")

        if (patientRows.isEmpty()) {
            emit(ImportProgress.Failed("No patient_data found in file.", null))
            return@flow
        }

        val errors = mutableListOf<RowError>()
        var patientsOk = 0; var patientsFail = 0
        var followUpsOk = 0; var followUpsFail = 0

        db.withTransaction {
            // --- Phase 3: insert patients ---
            patientRows.forEachIndexed { i, row ->
                emit(ImportProgress.InsertingPatients(i + 1, patientRows.size))
                runCatching { SqlImportMapper.toPatient(row) }
                    .onSuccess { db.patientDao().insert(it); patientsOk++ }
                    .onFailure { errors += RowError("patient_data", i, it.message ?: "unknown"); patientsFail++ }
            }

            // --- Phase 4: insert follow-ups ---
            followUpRows.forEachIndexed { i, row ->
                emit(ImportProgress.InsertingFollowUps(i + 1, followUpRows.size))
                runCatching { SqlImportMapper.toFollowUp(row) }
                    .onSuccess { db.patientFollowUpDao().insertFollowUp(it); followUpsOk++ }
                    .onFailure { errors += RowError("follow_up_data", i, it.message ?: "unknown"); followUpsFail++ }
            }
        }

        val result = ImportResult(
            patientsInserted  = patientsOk,
            patientsSkipped   = patientsFail,
            followUpsInserted = followUpsOk,
            followUpsSkipped  = followUpsFail,
            rowErrors         = errors,
            durationMs        = System.currentTimeMillis() - startMs
        )
        emit(ImportProgress.Completed(result))

    }.flowOn(Dispatchers.IO)
}
```

---

## 7. `ImportViewModel.kt`

```kotlin
class ImportViewModel(private val repo: ImportRepository) : ViewModel() {

    private val _state = MutableStateFlow<ImportUiState>(ImportUiState.Idle)
    val state: StateFlow<ImportUiState> = _state.asStateFlow()

    fun startImport(sqlContent: String, fileName: String) {
        viewModelScope.launch {
            repo.importFromSql(sqlContent, fileName).collect { progress ->
                _state.value = when (progress) {
                    is ImportProgress.ReadingFile       -> ImportUiState.ReadingFile(progress.fileName)
                    is ImportProgress.Parsing           -> ImportUiState.Parsing
                    is ImportProgress.InsertingPatients -> ImportUiState.InsertingPatients(progress.done, progress.total)
                    is ImportProgress.InsertingFollowUps-> ImportUiState.InsertingFollowUps(progress.done, progress.total)
                    is ImportProgress.Completed         -> {
                        if (progress.result.rowErrors.isEmpty())
                            ImportUiState.Success(progress.result)
                        else
                            ImportUiState.PartialSuccess(progress.result)
                    }
                    is ImportProgress.Failed            -> ImportUiState.Failure(progress.reason)
                }
            }
        }
    }
}
```

---

## 8. Import Progress Screen

### 8.1 Navigation

When the user picks a file in `SettingsActivity`:

```kotlin
pickSqlFile = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
    uri ?: return@registerForActivityResult
    val intent = Intent(this, ImportProgressActivity::class.java).apply {
        putExtra("uri", uri.toString())
        putExtra("fileName", uri.lastPathSegment ?: "backup.sql")
    }
    startActivity(intent)
}
```

`ImportProgressActivity.onCreate` reads the URI, opens the stream on `Dispatchers.IO`, and
calls `viewModel.startImport(content, fileName)`.

### 8.2 Layout — `activity_import_progress.xml`

The screen is divided into three layers that cross-fade between states:

```
┌────────────────────────────────────────┐
│  ← Import from Web App                │  ← toolbar (no drawer)
├────────────────────────────────────────┤
│                                        │
│  ①   ②   ③   ④                       │  ← step chips (StepIndicator)
│ Read Parse Patients Follow-ups         │
│                                        │
│  ┌──────────────────────────────────┐  │
│  │  🌐  →→→→→→→→→→→→→  📱        │  │  ← animated pipeline view
│  │  Web DB   flowing dots   Local   │  │
│  └──────────────────────────────────┘  │
│                                        │
│  Importing patients…                   │  ← phase label
│  ████████████░░░░░░░░   72 / 108      │  ← LinearProgressIndicator
│                                        │
│  ┌─────────────┐  ┌─────────────┐   │
│  │  Patients   │  │ Follow-ups  │   │  ← live counter chips
│  │  72 done ✓  │  │  pending…   │   │
│  └─────────────┘  └─────────────┘   │
│                                        │
└────────────────────────────────────────┘
```

**Key views and their IDs:**

| View | ID | Notes |
|---|---|---|
| `MaterialToolbar` | `toolbar` | Back button only (no drawer) |
| `LinearLayout` (step row) | `stepRow` | 4 `Chip` widgets in a row |
| Chip 1–4 | `chipRead`, `chipParse`, `chipPatients`, `chipFollowUps` | Inactive → active → done styling |
| `ConstraintLayout` (pipeline) | `pipelineView` | Contains two `ImageView` icons + `View` for dots |
| `MaterialTextView` | `tvPhaseLabel` | `"Reading file…"` / `"Parsing…"` / etc. |
| `LinearProgressIndicator` | `progressBar` | `indeterminate=true` for read/parse, determinate for insert |
| `MaterialTextView` | `tvProgressCount` | `"72 / 108"` or blank |
| `MaterialCardView` | `cardPatients` | Live patient counter |
| `MaterialTextView` | `tvPatientsCount` | `"72 done"` |
| `MaterialCardView` | `cardFollowUps` | Live follow-up counter |
| `MaterialTextView` | `tvFollowUpsCount` | `"pending…"` → `"1886 done"` |

### 8.3 Animated Pipeline

The pipeline `ConstraintLayout` contains:
- `ImageView` left — `@drawable/ic_web_database` (cloud/web icon), colorPrimary tint
- `ImageView` right — `@drawable/ic_phone_database` (storage icon), colorPrimary tint
- Three small `View` dots between them, animated with `ObjectAnimator`:

```kotlin
// Repeating translate animation on each dot with staggered startDelay
fun animateDot(dot: View, delayMs: Long) {
    ObjectAnimator.ofFloat(dot, "translationX", 0f, pipelineWidth).apply {
        duration       = 1200
        startDelay     = delayMs
        repeatCount    = ObjectAnimator.INFINITE
        repeatMode     = ObjectAnimator.RESTART
        interpolator   = LinearInterpolator()
    }.start()
}
// Call: animateDot(dot1, 0), animateDot(dot2, 400), animateDot(dot3, 800)
```

The animation runs as long as import is in progress and is cancelled on Done/Error.

### 8.4 Step Chip Styling

Each chip cycles through three visual states:

| State | Background | Text colour | Icon |
|---|---|---|---|
| Pending | `colorSurfaceVariant` | `colorOnSurfaceVariant` | none |
| Active | `colorPrimaryContainer` | `colorOnPrimaryContainer` | animated spinner |
| Done | `colorSecondaryContainer` | `colorOnSecondaryContainer` | `✓` |

```kotlin
fun Chip.setStepState(state: StepState) {
    when (state) {
        PENDING -> { chipBackgroundColor = colorSurfaceVariant; chipIcon = null }
        ACTIVE  -> { chipBackgroundColor = colorPrimaryContainer; chipIcon = spinnerDrawable }
        DONE    -> { chipBackgroundColor = colorSecondaryContainer; chipIcon = checkDrawable }
    }
}
```

### 8.5 State → UI Mapping (in `ImportProgressActivity`)

```kotlin
lifecycleScope.launch {
    repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.state.collect { state ->
            when (state) {
                is ImportUiState.ReadingFile -> {
                    setStepActive(chipRead)
                    tvPhaseLabel.text = "Reading ${state.fileName}…"
                    progressBar.isIndeterminate = true
                }
                is ImportUiState.Parsing -> {
                    setStepDone(chipRead); setStepActive(chipParse)
                    tvPhaseLabel.text = "Parsing SQL structure…"
                    progressBar.isIndeterminate = true
                }
                is ImportUiState.InsertingPatients -> {
                    setStepDone(chipParse); setStepActive(chipPatients)
                    tvPhaseLabel.text = "Importing patients…"
                    progressBar.isIndeterminate = false
                    progressBar.max      = state.total
                    progressBar.progress = state.done
                    tvProgressCount.text = "${state.done} / ${state.total}"
                    tvPatientsCount.text = "${state.done} imported"
                }
                is ImportUiState.InsertingFollowUps -> {
                    setStepDone(chipPatients); setStepActive(chipFollowUps)
                    tvPhaseLabel.text = "Importing follow-ups…"
                    progressBar.max      = state.total
                    progressBar.progress = state.done
                    tvProgressCount.text = "${state.done} / ${state.total}"
                    tvFollowUpsCount.text = "${state.done} imported"
                }
                is ImportUiState.Success        -> showSuccessView(state.result)
                is ImportUiState.PartialSuccess -> showSuccessView(state.result, hasErrors = true)
                is ImportUiState.Failure        -> showErrorView(state.reason)
                else -> { /* Idle: no-op */ }
            }
        }
    }
}
```

---

## 9. Success Screen

### 9.1 Visual Design

When `ImportUiState.Success` or `ImportUiState.PartialSuccess` is emitted, the progress
layer fades out and the result layer fades in (using `ViewPropertyAnimator.alpha()`):

```
┌────────────────────────────────────────┐
│  ← Import from Web App                │
├────────────────────────────────────────┤
│                                        │
│            ✅  (64dp icon)             │
│        Import Complete!                │
│    All records imported successfully.  │
│                                        │
│  ┌──────────┐  ┌──────────┐          │
│  │ Patients │  │Follow-ups│          │  ← summary cards
│  │   108    │  │  1886    │          │
│  │ imported │  │ imported │          │
│  └──────────┘  └──────────┘          │
│                                        │
│  ┌──────────┐  ┌──────────┐          │
│  │ Skipped  │  │  Time    │          │
│  │   0 rows │  │  3.2 s   │          │
│  └──────────┘  └──────────┘          │
│                                        │
│  ─────── Skipped rows ──────────      │  ← only shown if rowErrors > 0
│  • patient row 23: missing lastName   │
│  • followup row 145: orphaned FK      │
│                                        │
│  ┌──────────────┐  ┌──────────────┐  │
│  │ View Patients│  │     Done     │  │
│  └──────────────┘  └──────────────┘  │
└────────────────────────────────────────┘
```

### 9.2 Key Views in the Result Layer

| View | ID | Content |
|---|---|---|
| `ImageView` | `ivResultIcon` | `@drawable/baseline_check_circle_24` (green tint) |
| `MaterialTextView` | `tvResultTitle` | `"Import Complete!"` |
| `MaterialTextView` | `tvResultSubtitle` | `"All records imported."` / `"Some rows were skipped."` |
| `MaterialCardView` | `cardPatientsResult` | `patientsInserted` |
| `MaterialCardView` | `cardFollowUpsResult` | `followUpsInserted` |
| `MaterialCardView` | `cardSkipped` | `patientsSkipped + followUpsSkipped` — hidden if 0 |
| `MaterialCardView` | `cardDuration` | `"X.X s"` |
| `RecyclerView` | `rvErrors` | Error list — `GONE` if empty |
| `MaterialButton` (outlined) | `btnViewPatients` | Navigate to `ViewAllPatientsActivity` |
| `MaterialButton` (filled) | `btnDone` | `finish()` back to Settings |

### 9.3 `showSuccessView` implementation

```kotlin
private fun showSuccessView(result: ImportResult, hasErrors: Boolean = false) {
    stopPipelineAnimation()
    progressLayer.animate().alpha(0f).withEndAction {
        progressLayer.visibility = View.GONE
        resultLayer.visibility   = View.VISIBLE
        resultLayer.animate().alpha(1f)
    }

    ivResultIcon.setImageResource(
        if (hasErrors) R.drawable.baseline_warning_24
        else           R.drawable.baseline_check_circle_24
    )
    ivResultIcon.imageTintList = ColorStateList.valueOf(
        if (hasErrors) getColor(com.google.android.material.R.color.design_default_color_error)
        else           getColor(R.color.md_theme_light_primary)
    )

    tvResultTitle.text    = if (hasErrors) "Import Complete with Warnings" else "Import Complete!"
    tvResultSubtitle.text = if (hasErrors)
        "${result.rowErrors.size} rows could not be imported."
    else
        "All records imported successfully."

    tvPatientsResultCount.text   = "${result.patientsInserted}"
    tvFollowUpsResultCount.text  = "${result.followUpsInserted}"
    tvSkippedCount.text          = "${result.patientsSkipped + result.followUpsSkipped}"
    tvDuration.text              = "%.1f s".format(result.durationMs / 1000.0)

    cardSkipped.isVisible = (result.patientsSkipped + result.followUpsSkipped) > 0
    rvErrors.isVisible    = result.rowErrors.isNotEmpty()
    if (result.rowErrors.isNotEmpty()) {
        rvErrors.adapter = ErrorListAdapter(result.rowErrors)
    }

    btnViewPatients.setOnClickListener {
        startActivity(Intent(this, ViewAllPatientsActivity::class.java))
        finish()
    }
    btnDone.setOnClickListener { finish() }
}
```

---

## 10. Error Screen (Fatal Failure)

Shown when `ImportUiState.Failure` is received — no records were written (transaction rolled back).

```
┌────────────────────────────────────────┐
│  ← Import from Web App                │
├────────────────────────────────────────┤
│                                        │
│           ❌  (64dp icon)              │
│          Import Failed                 │
│                                        │
│  ┌──────────────────────────────────┐  │
│  │ Could not read the selected      │  │
│  │ file. Make sure it is a valid    │  │
│  │ phpMyAdmin MySQL export (.sql).  │  │
│  └──────────────────────────────────┘  │
│                                        │
│  Technical detail:                     │
│  No patient_data INSERT found in file  │  ← tvErrorDetail (monospace, small)
│                                        │
│  ┌──────────────┐  ┌──────────────┐  │
│  │  Try Again   │  │    Cancel    │  │
│  └──────────────┘  └──────────────┘  │
└────────────────────────────────────────┘
```

```kotlin
private fun showErrorView(reason: String) {
    stopPipelineAnimation()
    progressLayer.animate().alpha(0f).withEndAction {
        progressLayer.visibility = View.GONE
        errorLayer.visibility    = View.VISIBLE
        errorLayer.animate().alpha(1f)
    }
    tvErrorDetail.text = reason
    btnTryAgain.setOnClickListener {
        // Return to Settings so user can re-pick file
        finish()
    }
    btnCancelError.setOnClickListener { finish() }
}
```

---

## 11. Full Layout Structure — `activity_import_progress.xml`

```xml
<LinearLayout vertical, match_parent>

    <MaterialToolbar id="toolbar" />

    <!-- LAYER 1: progress (alpha=1 during import) -->
    <ScrollView id="progressLayer">
        <LinearLayout vertical>

            <!-- Step chips -->
            <HorizontalScrollView>
                <LinearLayout horizontal>
                    <Chip id="chipRead"      text="Read" />
                    <Chip id="chipParse"     text="Parse" />
                    <Chip id="chipPatients"  text="Patients" />
                    <Chip id="chipFollowUps" text="Follow-ups" />
                </LinearLayout>
            </HorizontalScrollView>

            <!-- Animated pipeline -->
            <ConstraintLayout id="pipelineView" height=80dp>
                <ImageView id="ivSourceDb"  src="@drawable/ic_web_database" />
                <View id="dot1"  4dp circle, colorPrimary background />
                <View id="dot2"  4dp circle />
                <View id="dot3"  4dp circle />
                <ImageView id="ivLocalDb"   src="@drawable/ic_phone_database" />
            </ConstraintLayout>

            <!-- Phase label -->
            <MaterialTextView id="tvPhaseLabel" textAppearanceTitleSmall />

            <!-- Progress bar -->
            <LinearProgressIndicator id="progressBar" indeterminate=true />
            <MaterialTextView id="tvProgressCount" textAppearanceBodySmall />

            <!-- Live counters -->
            <LinearLayout horizontal>
                <MaterialCardView>
                    <LinearLayout>
                        <MaterialTextView text="Patients" />
                        <MaterialTextView id="tvPatientsCount" text="pending…" />
                    </LinearLayout>
                </MaterialCardView>
                <MaterialCardView>
                    <LinearLayout>
                        <MaterialTextView text="Follow-ups" />
                        <MaterialTextView id="tvFollowUpsCount" text="pending…" />
                    </LinearLayout>
                </MaterialCardView>
            </LinearLayout>

        </LinearLayout>
    </ScrollView>

    <!-- LAYER 2: success/partial result (alpha=0, GONE during import) -->
    <ScrollView id="resultLayer" visibility=gone alpha=0>
        <LinearLayout vertical>
            <ImageView id="ivResultIcon" />
            <MaterialTextView id="tvResultTitle" />
            <MaterialTextView id="tvResultSubtitle" />
            <LinearLayout horizontal>
                <MaterialCardView id="cardPatientsResult">...</MaterialCardView>
                <MaterialCardView id="cardFollowUpsResult">...</MaterialCardView>
            </LinearLayout>
            <LinearLayout horizontal>
                <MaterialCardView id="cardSkipped">...</MaterialCardView>
                <MaterialCardView id="cardDuration">...</MaterialCardView>
            </LinearLayout>
            <MaterialTextView text="Skipped rows" visibility=gone />
            <RecyclerView id="rvErrors" visibility=gone />
            <LinearLayout horizontal>
                <MaterialButton id="btnViewPatients" style=outlined />
                <MaterialButton id="btnDone" />
            </LinearLayout>
        </LinearLayout>
    </ScrollView>

    <!-- LAYER 3: fatal error (alpha=0, GONE during import) -->
    <ScrollView id="errorLayer" visibility=gone alpha=0>
        <LinearLayout vertical>
            <ImageView id="ivErrorIcon" />
            <MaterialTextView id="tvErrorTitle" text="Import Failed" />
            <MaterialCardView>
                <MaterialTextView text="Could not read the selected file…" />
            </MaterialCardView>
            <MaterialTextView id="tvErrorDetail" textAppearanceBodySmall fontFamily=monospace />
            <LinearLayout horizontal>
                <MaterialButton id="btnTryAgain" style=outlined />
                <MaterialButton id="btnCancelError" />
            </LinearLayout>
        </LinearLayout>
    </ScrollView>

</LinearLayout>
```

---

## 12. Settings Screen Change

Replace the single "Import" card sketch from the original plan with this flow:

```kotlin
// In SettingsActivity
binding.btnImportSql.setOnClickListener {
    pickSqlFile.launch("*/*")
}

private val pickSqlFile = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
    uri ?: return@registerForActivityResult
    startActivity(
        Intent(this, ImportProgressActivity::class.java).apply {
            putExtra("FILE_URI", uri.toString())
            putExtra("FILE_NAME", uri.lastPathSegment ?: "backup.sql")
        }
    )
}
```

Settings card XML (add below the Cloud Sync card):
```xml
<MaterialCardView outlined style>
    <LinearLayout vertical padding=spacing_md>
        <MaterialTextView text="Import from Web App" textAppearanceTitleMedium />
        <MaterialTextView
            text="Select a phpMyAdmin .sql backup file to import all patients and follow-ups."
            textAppearanceBodySmall colorOnSurfaceVariant />
        <MaterialButton
            id="@+id/btnImportSql"
            android:layout_marginTop="@dimen/spacing_md"
            android:text="Select .sql File"
            app:icon="@drawable/baseline_upload_file_24" />
    </LinearLayout>
</MaterialCardView>
```

---

## 13. Error Handling Strategy

| Scenario | Handling |
|---|---|
| File unreadable / permission denied | `ImportProgress.Failed` before parsing; show error layer |
| No `patient_data` INSERT in file | `ImportProgress.Failed("No patient_data found")`; show error layer |
| Individual row parse error | `runCatching` per row; row counted in `patientsSkipped`; import continues |
| Follow-up FK references missing patient | Caught by `runCatching`; counted in `followUpsSkipped` |
| Very large file (>5 MB) | All on `Dispatchers.IO`; progress bar keeps UI alive |
| App killed mid-transaction | Room `withTransaction` rolls back automatically; no partial data |
| User presses Back during import | `viewModelScope` is cancelled; transaction rolls back; navigate away |

---

## 14. Testing Checklist

- [ ] Import `backup.sql` (~2075 lines, ~108 patients, ~1886 follow-ups) — progress visible for each phase
- [ ] Pipeline animation plays during import, stops on result
- [ ] Step chips advance from Read → Parse → Patients → Follow-ups → done
- [ ] Patient counter increments live (`72 / 108`)
- [ ] `diagnosis` field populated on imported patients
- [ ] `weight = "63 K"` stored as `63`; `balance = " 00"` stored as `0`
- [ ] `phone = "2147483647"` stored as `null`
- [ ] `dateJoined = "2019-09-09 20:19:22"` converts to correct epoch millis
- [ ] Success screen shows correct counts + duration
- [ ] "View Patients" button navigates to patient list with imported data
- [ ] Import a corrupted/wrong file → error layer shown with technical message
- [ ] Re-import same file → counts stay identical (REPLACE, no duplicates)
- [ ] Back during import → no partial data remains in DB
- [ ] Skipped rows section appears only when `rowErrors` is non-empty

---

## 15. Out of Scope

- `admin_users` table (Firebase Auth handles authentication)
- `followUp1`–`followUp4` legacy blobs inside `patient_data` (discarded)
- Exporting back to MySQL format
- Server-side / network import (file-only, offline)
