# Testing Guide - MKV Subtitle Extractor

## 📋 Estrategia de Testing

La aplicación requiere testing en múltiples niveles:
1. **Unit Tests** - Lógica individual de componentes
2. **Integration Tests** - Interacción entre componentes
3. **UI Tests** - Verificación de interfaz de usuario
4. **Manual Tests** - Validación de flujos reales

## 🧪 Unit Tests

### Test de FileUtils

```kotlin
@RunWith(AndroidTestRunner::class)
class FileUtilsTest {
    
    @Test
    fun testIsValidMKVFile() {
        assertTrue(FileUtils.isValidMKVFile("video.mkv"))
        assertTrue(FileUtils.isValidMKVFile("movie.MKV"))
        assertFalse(FileUtils.isValidMKVFile("video.mp4"))
        assertFalse(FileUtils.isValidMKVFile("subtitle.srt"))
    }
    
    @Test
    fun testGetFileSizeInMB() {
        val size = FileUtils.getFileSizeInMB("/tmp/test.mkv")
        assertTrue(size >= 0)
    }
    
    @Test
    fun testCreateSubtitleFile() {
        val content = "1\n00:00:00,000 --> 00:00:05,000\nHello World"
        val file = FileUtils.createSubtitleFile(context, content)
        assertTrue(file.exists())
        assertTrue(file.readText().contains("Hello World"))
        file.delete()
    }
    
    @Test
    fun testReadSubtitleFile() {
        // Crear archivo de prueba
        val testContent = "Test subtitle content"
        val file = File.createTempFile("subtitle", ".srt")
        file.writeText(testContent)
        
        val content = FileUtils.readSubtitleFile(file.absolutePath)
        assertEquals(testContent, content)
        
        file.delete()
    }
}
```

### Test de TranslationService

```kotlin
class TranslationServiceTest {
    
    private lateinit var service: TranslationService
    
    @Before
    fun setUp() {
        service = TranslationService(context)
    }
    
    @Test
    fun testGetSupportedLanguages() {
        val languages = service.getSupportedLanguages()
        assertNotNull(languages)
        assertTrue(languages.isNotEmpty())
        assertTrue(languages.contains("Spanish"))
        assertTrue(languages.contains("French"))
        assertTrue(languages.size >= 20)
    }
    
    @Test
    fun testMapLanguageToCode() {
        // Usar reflexión para acceder a método privado
        val method = TranslationService::class.java.getDeclaredMethod(
            "mapLanguageToCode", 
            String::class.java
        )
        method.isAccessible = true
        
        assertEquals("es", method.invoke(service, "Spanish"))
        assertEquals("fr", method.invoke(service, "French"))
        assertEquals("de", method.invoke(service, "German"))
    }
}
```

## 🔄 Integration Tests

### Test de Extracción + Visualización

```kotlin
@RunWith(AndroidTestRunner::class)
class ExtractionIntegrationTest {
    
    private lateinit var extractionService: SubtitleExtractionService
    private lateinit var testMkvFile: File
    
    @Before
    fun setUp() {
        extractionService = SubtitleExtractionService(context)
        // Crear archivo MKV de prueba o usar existente
        testMkvFile = File("/sdcard/test.mkv")
    }
    
    @Test
    fun testExtractSubtitles() {
        // Arrange
        val outputDir = context.cacheDir.absolutePath
        
        // Act
        val response = runBlocking {
            extractionService.extractSubtitles(
                testMkvFile.absolutePath,
                outputDir
            )
        }
        
        // Assert
        assertTrue(response.success)
        assertTrue(response.subtitles.isNotEmpty())
        response.subtitles.forEach { track ->
            assertTrue(File(track.filePath).exists())
            assertTrue(track.size > 0)
        }
    }
}
```

### Test de Traducción

```kotlin
@RunWith(AndroidTestRunner::class)
class TranslationIntegrationTest {
    
    private lateinit var translationService: TranslationService
    
    @Before
    fun setUp() {
        translationService = TranslationService(context)
    }
    
    @Test
    fun testTranslateSubtitleContent() {
        // Arrange
        val srtContent = """
            1
            00:00:00,000 --> 00:00:05,000
            Hello World
            
            2
            00:00:05,000 --> 00:00:10,000
            This is a test
        """.trimIndent()
        
        // Act
        val response = runBlocking {
            translationService.translateSubtitleContent(srtContent, "Spanish")
        }
        
        // Assert
        assertTrue(response.success)
        assertTrue(response.translated_content.isNotEmpty())
        assertTrue(response.translated_content.contains("Hola")) // Expected Spanish
    }
}
```

## 🎨 UI Tests

### Test de MainActivity

```kotlin
@RunWith(AndroidTestRunner::class)
class MainActivityTest {
    
    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)
    
    @Test
    fun testSelectFileButtonExists() {
        onView(withId(R.id.selectFileButton))
            .check(matches(isDisplayed()))
    }
    
    @Test
    fun testSelectFileButtonClick() {
        onView(withId(R.id.selectFileButton))
            .perform(click())
        
        // Verificar que se abre file picker
        intended(hasAction(Intent.ACTION_GET_CONTENT))
    }
    
    @Test
    fun testStatusTextViewDisplay() {
        val statusText = getString(R.string.waiting_for_file)
        onView(withId(R.id.statusTextView))
            .check(matches(withText(statusText)))
    }
}
```

### Test de SubtitleDisplayActivity

```kotlin
@RunWith(AndroidTestRunner::class)
class SubtitleDisplayActivityTest {
    
    @get:Rule
    val activityRule = ActivityScenarioRule(SubtitleDisplayActivity::class.java)
    
    @Test
    fun testLanguageSpinnerPopulated() {
        onView(withId(R.id.languageSpinner))
            .check(matches(isDisplayed()))
        
        // Verificar que el spinner tiene opciones
        onView(withId(R.id.languageSpinner))
            .perform(click())
        onView(withText("Spanish"))
            .check(matches(isDisplayed()))
    }
    
    @Test
    fun testTranslateButtonClick() {
        onView(withId(R.id.translateButton))
            .perform(click())
        
        // Verificar que se muestra progress bar
        onView(withId(R.id.translationProgressBar))
            .check(matches(isDisplayed()))
    }
    
    @Test
    fun testSaveButtonFunctionality() {
        onView(withId(R.id.saveButton))
            .perform(click())
        
        // Verificar toast de confirmación
        onView(withText(containsString("Guardado")))
            .check(matches(isDisplayed()))
    }
    
    @Test
    fun testBackButtonNavigation() {
        onView(withId(R.id.backButton))
            .perform(click())
        
        // Verificar que regresa a MainActivity
        intended(hasComponent(MainActivity::class.java.name))
    }
}
```

## 📋 Casos de Prueba Manual

### Test Case 1: Selección de Archivo

**Objetivo**: Verificar que se puede seleccionar un archivo MKV

| Paso | Acción | Resultado Esperado |
|------|--------|-------------------|
| 1 | Abrir la aplicación | Pantalla principal visible |
| 2 | Tocar "Browse Files" | Se abre file picker |
| 3 | Seleccionar archivo MKV válido | Nombre y tamaño mostrados |
| 4 | Verificar validación | Solo archivos .mkv permitidos |

### Test Case 2: Extracción de Subtítulos

**Objetivo**: Extraer subtítulos correctamente

| Paso | Acción | Resultado Esperado |
|------|--------|-------------------|
| 1 | Archivo MKV seleccionado | Progreso visible |
| 2 | Se inicia extracción | Progress bar animado |
| 3 | Extracción completa | Pantalla de visualización abierta |
| 4 | Subtítulos mostrados | Texto legible con timestamps |

### Test Case 3: Traducción

**Objetivo**: Traducir subtítulos a otro idioma

| Paso | Acción | Resultado Esperado |
|------|--------|-------------------|
| 1 | Seleccionar idioma | Dropdown muestra idiomas |
| 2 | Tocar "Translate" | Progress bar visible |
| 3 | Traducción completa | Subtítulos en nuevo idioma |
| 4 | Verificar calidad | Traducción coherente |

### Test Case 4: Guardado de Subtítulos

**Objetivo**: Guardar subtítulos correctamente

| Paso | Acción | Resultado Esperado |
|------|--------|-------------------|
| 1 | Tocar "Save" | Toast de confirmación |
| 2 | Verificar ubicación | Archivo en caché de app |
| 3 | Verificar formato | Archivo .srt válido |
| 4 | Verificar contenido | Todos los subtítulos presentes |

### Test Case 5: Manejo de Errores

**Objetivo**: Manejo graceful de errores

| Caso de Error | Acción | Resultado Esperado |
|---------------|--------|-------------------|
| MKV sin subtítulos | Seleccionar archivo | Mensaje informativo |
| MKV corrupto | Seleccionar archivo | Error description |
| Sin conexión a internet | Intentar traducir | Error con fallback |
| Permisos denegados | Usar app sin permisos | Solicitud runtime |
| Almacenamiento lleno | Intentar guardar | Error informativo |

## 🔍 Casos de Prueba - Casos Extremos

### Test Case 6: Archivo MKV Muy Grande

**Archivo**: > 2GB
**Esperado**: Manejo de memoria correcto

```kotlin
@Test
fun testLargeMKVFile() {
    // Usar archivo de prueba > 500MB
    val largeMkvFile = "/sdcard/large_video.mkv"
    
    val response = runBlocking {
        extractionService.extractSubtitles(largeMkvFile, outputDir)
    }
    
    // Debe completarse sin crash
    assertTrue(response.success)
}
```

### Test Case 7: Múltiples Pistas de Subtítulos

**MKV con**: Inglés, Español, Francés
**Esperado**: Todas extraídas

```kotlin
@Test
fun testMultipleSubtitleTracks() {
    val response = runBlocking {
        extractionService.extractSubtitles(multiTrackMkv, outputDir)
    }
    
    assertTrue(response.subtitles.size >= 3)
    assertTrue(response.subtitles.any { it.language == "eng" })
    assertTrue(response.subtitles.any { it.language == "spa" })
}
```

### Test Case 8: Caracteres Especiales

**Contenido**: Caracteres acentuados, emojis
**Esperado**: Conservados correctamente

```kotlin
@Test
fun testSpecialCharactersPreservation() {
    val content = "Test café résumé ñ 你好 🎬"
    val file = FileUtils.createSubtitleFile(context, content)
    val readContent = FileUtils.readSubtitleFile(file.absolutePath)
    
    assertEquals(content, readContent)
}
```

### Test Case 9: Timeout en Red Lenta

**Conexión**: 2G/3G o lenta
**Esperado**: Reintentos y fallback

```kotlin
@Test
fun testSlowNetworkTranslation() {
    // Simular red lenta
    val response = runBlocking {
        withTimeout(5000L) {
            translationService.translateSubtitleContent(srtContent, "Spanish")
        }
    }
    
    // Debe fallar gracefully o retornar original
    assertNotNull(response)
}
```

## 📊 Cobertura de Código

### Target de Cobertura

- **FileUtils**: 100%
- **Services**: 85%+
- **Models**: 100%
- **Activities**: 70%+ (UI es difícil de testear)
- **Python**: Tested via integration tests

### Ejecutar Cobertura

```bash
# Generar reporte de cobertura
./gradlew testDebugUnitTestCoverage

# Ver reporte
open app/build/reports/coverage/index.html
```

## ✅ Checklist de Testing Pre-Release

- [ ] Unit tests: 90%+ passing
- [ ] Integration tests: 100% passing
- [ ] UI tests: 100% passing
- [ ] Manual testing en 3+ dispositivos diferentes
- [ ] Prueba offline (sin internet)
- [ ] Prueba con permisos denegados
- [ ] Prueba con almacenamiento bajo
- [ ] Prueba con archivos MKV reales
- [ ] Prueba de traducción a 5+ idiomas
- [ ] Prueba de guardado de archivos
- [ ] Memory profiling: No memory leaks
- [ ] Battery profiling: Uso razonable

## 🚀 Continuous Integration

### GitHub Actions

```yaml
name: Test

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    
    steps:
      - uses: actions/checkout@v2
      
      - name: Set up JDK
        uses: actions/setup-java@v2
        with:
          java-version: '11'
      
      - name: Run tests
        run: ./gradlew test
      
      - name: Run lint
        run: ./gradlew lint
      
      - name: Upload coverage
        uses: codecov/codecov-action@v2
```

## 📈 Métricas de Calidad

| Métrica | Target | Actual |
|---------|--------|--------|
| Code Coverage | 80%+ | TBD |
| Cyclomatic Complexity | < 10 | TBD |
| Tech Debt | < 5% | TBD |
| Critical Issues | 0 | TBD |
| Bugs Found in Testing | < 3 | TBD |

## 🔬 Herramientas de Testing

```gradle
dependencies {
    // Testing
    testImplementation "junit:junit:4.13.2"
    testImplementation "org.mockito:mockito-core:3.6.0"
    testImplementation "org.mockito.kotlin:mockito-kotlin:3.2.0"
    
    // Android Testing
    androidTestImplementation "androidx.test.ext:junit:1.1.5"
    androidTestImplementation "androidx.test.espresso:espresso-core:3.5.1"
    androidTestImplementation "androidx.test.espresso:espresso-intents:3.5.1"
    androidTestImplementation "androidx.test:runner:1.5.2"
    
    // Coroutines Testing
    testImplementation "org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3"
}
```

---

**Última actualización**: Noviembre 2024  
**Versión**: 1.0.0
