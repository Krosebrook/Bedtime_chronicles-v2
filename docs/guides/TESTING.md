# Testing Guide — Bedtime Chronicles

Testing of **Infinity Heroes: Bedtime Chronicles** leverages modern local JVM execution ecosystems (Robolectric, JUnit, and Roborazzi) to achieve maximum reliability, low execution latency, and thorough visual coverage without requiring an active Android Emulator.

---

## 🛠️ 1. Testing Stack & Frameworks

We avoid slow, fragile emulator-based instrumented UI tests completely. The native testing suite runs strictly on the local JVM:

- **Robolectric**: Simulates the full Android OS environment on the JVM sandbox, allowing context queries, resource extraction, Shared Preferences access, and Room DB connections.
- **Roborazzi**: High-fidelity visual screenshot checking. Generates pixel-perfect mock screens of Jetpack Compose nodes under local JVM.
- **JUnit 4**: Execution harness for tests.
- **Kotlin Coroutines Test Library**: Provides structured virtual time engines (`runTest`) to test asynchronous StateFlow emission streams without real-world delay bottlenecks.

---

## 🧪 2. Test File Registry (`app/src/test/java/com/example/`)

1. **`ExampleUnitTest.kt`**: Basic logical tests covering standard syntax structures.
2. **`ExampleRobolectricTest.kt`**: Tests reading package string files or preferences metrics querying Android context simulations.
3. **`GreetingScreenshotTest.kt`**: Executes screenshot capture passes of standard composables using Roborazzi to confirm pixel consistency.
4. **`ReaderScreenFeaturesTest.kt`**: Verifies dynamic UI state parameters inside the narrative e-reader components (e.g. text size bounds, audio toggles, narration highlight offsets).
5. **`StoryLibraryTest.kt`**: Confirms local database insertion, query streams, and retrieval of generated bedtime chronicles via Room integration tests.

---

## 🚀 3. Execution & Validation Commands

All test procedures are launched using native Gradle execution pathways.

### Run Local Unit & Robolectric Tests
```bash
shell_exec(command="gradle :app:testDebugUnitTest")
```

### Roborazzi Screenshot Verification
```bash
shell_exec(command="gradle :app:verifyRoborazziDebug")
```

### Record Reference Screenshots (Overwriting outdated baselines)
```bash
shell_exec(command="gradle :app:recordRoborazziDebug")
```

---

## ✍️ 4. Code Patterns & Examples

### 4.1 Testing Room DB Entities (`StoryLibraryTest.kt`)
Always use an in-memory database configuration when running tests to guarantee isolation between runs:

```kotlin
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class StoryLibraryTest {
    private lateinit var db: AppDatabase
    private lateinit var dao: GeneratedStoryDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.generatedStoryDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testInsertAndRetrieveStory() = runTest {
        val story = GeneratedStory(
            title = "Sleepy Star Hunter",
            author = "Gemini",
            heroName = "Stardust Kid",
            mode = "sleep",
            paragraphsJson = "[\"Deep in space...\"]",
            dateCreated = System.currentTimeMillis()
        )
        dao.insertStory(story)
        val list = dao.getAllStories().first()
        assertEquals(1, list.size)
        assertEquals("Sleepy Star Hunter", list[0].title)
    }
}
```

### 4.2 Screenshot Verification with Roborazzi
```kotlin
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class GreetingScreenshotTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun verifyGreetingScreenCapture() {
        composeTestRule.setContent {
            AppTheme {
                HomeScreenContent(onAdventureLaunch = {})
            }
        }
        captureRoboImage("src/test/screenshots/home_screen.png")
    }
}
```
