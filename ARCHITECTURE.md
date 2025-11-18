# Technical Architecture - MKV Subtitle Extractor

## 🏗️ Arquitectura General del Sistema

```
┌────────────────────────────────────────────────────────────────────────┐
│                         ANDROID APPLICATION LAYER                      │
├────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │                    UI COMPONENTS (Activities)                    │  │
│  │  ┌──────────────────────┐         ┌──────────────────────────┐  │  │
│  │  │   MainActivity       │         │ SubtitleDisplayActivity  │  │  │
│  │  │                      │   -->   │                          │  │  │
│  │  │ • File Selection     │         │ • Display Subtitles      │  │  │
│  │  │ • Permissions        │         │ • Language Selection     │  │  │
│  │  │ • Progress UI        │         │ • Translation Control    │  │  │
│  │  │ • Status Messages    │         │ • Save Functionality     │  │  │
│  │  └──────────────────────┘         └──────────────────────────┘  │  │
│  │                                                                  │  │
│  └──────────────────────────────────────────────────────────────────┘  │
│                                    ↓                                    │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │                    BUSINESS LOGIC LAYER (Services)               │  │
│  │  ┌────────────────────────────┐   ┌──────────────────────────┐  │  │
│  │  │ SubtitleExtractionService  │   │ TranslationService       │  │  │
│  │  │                            │   │                          │  │  │
│  │  │ • Python Bridge            │   │ • OkHttp Client          │  │  │
│  │  │ • Process Management       │   │ • JSON Parsing (Gson)    │  │  │
│  │  │ • Result Parsing           │   │ • Retry Logic            │  │  │
│  │  │ • Error Handling           │   │ • Rate Limiting          │  │  │
│  │  │ • File Validation          │   │ • Language Mapping       │  │  │
│  │  └────────────────────────────┘   └──────────────────────────┘  │  │
│  │                                                                  │  │
│  └──────────────────────────────────────────────────────────────────┘  │
│                           ↓                    ↓                       │
│  ┌────────────────────────────────────────────────────────────────┐   │
│  │                    UTILITY LAYER (Utils)                       │   │
│  │  ┌──────────────────────────────────────────────────────────┐  │   │
│  │  │ FileUtils: URI handling, Scoped Storage, Validation      │  │   │
│  │  └──────────────────────────────────────────────────────────┘  │   │
│  │                                                                 │   │
│  └────────────────────────────────────────────────────────────────┘   │
│                                                                         │
└────────────────────────────────────────────────────────────────────────┘
                           ↓ (IPC)               ↓ (HTTP)
          
┌─────────────────────────────────────────────────────────────┐
│              EXTERNAL SERVICES / FRAMEWORKS                  │
│                                                              │
│  ┌──────────────────────┐        ┌─────────────────────┐   │
│  │ Chaquopy             │        │ LibreTranslate API  │   │
│  │ (Python Integration) │        │ (Translation SaaS)  │   │
│  │                      │        │                     │   │
│  │ • Python 3.9         │        │ • Endpoint: https://│   │
│  │ • FFmpeg Libraries   │        │   libretranslate.com│   │
│  │ • Process Execution  │        │ • 24+ Languages     │   │
│  │                      │        │ • REST API          │   │
│  └──────────────────────┘        └─────────────────────┘   │
│           ↓                                ↓                │
│  ┌──────────────────────┐        ┌─────────────────────┐   │
│  │ Python Scripts       │        │ HTTP Requests       │   │
│  │                      │        │ (JSON Format)       │   │
│  │ • extract_subtitles. │        │                     │   │
│  │   py                 │        │ POST /translate     │   │
│  │ • translate_subtitles│        │ {q, source, target} │   │
│  │   .py                │        │                     │   │
│  │                      │        │ → {translatedText}  │   │
│  │                      │        │                     │   │
│  └──────────────────────┘        └─────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

---

## 📦 Componentes Detallados

### 1. User Interface Layer

**MainActivity.kt**
```
Responsabilidades:
├── Inicializar Python (Chaquopy)
├── Gestionar File Picker
├── Solicitar permisos runtime
├── Coordinar extracción de subtítulos
├── Navegar a SubtitleDisplayActivity
└── Manejo de excepciones

Dependencias:
├── Android Framework
├── Chaquopy
├── Context/Fragment APIs
└── Coroutines
```

**SubtitleDisplayActivity.kt**
```
Responsabilidades:
├── Cargar archivos SRT
├── Mostrar subtítulos
├── Gestionar spinner de idiomas
├── Coordinar traducción
├── Guardar archivos
└── Manejo de navegación

Dependencias:
├── TranslationService
├── FileUtils
├── Coroutines
└── Context APIs
```

### 2. Service Layer

**SubtitleExtractionService.kt**
```
Responsabilidades:
├── Bridge con Python
├── Llamar extract_subtitles_with_ffmpeg()
├── Parsear respuesta JSON
├── Validar resultado
├── Manejo de errores
└── Conversión de tipos

Flujo:
┌─────────┐
│ Service │
└────┬────┘
     │
     ├─→ Validar ruta MKV
     ├─→ Llamar Python
     ├─→ Recibir JSON
     ├─→ Parsear a ExtractionResponse
     └─→ Retornar resultado
```

**TranslationService.kt**
```
Responsabilidades:
├── Cliente HTTP OkHttp3
├── Llamar LibreTranslate API
├── Reintentos automáticos
├── Rate limit handling
├── Mapeo de idiomas
└── Procesamiento SRT

Flujo:
┌─────────┐
│ Service │
└────┬────┘
     │
     ├─→ Validar contenido
     ├─→ Mapear idioma a código ISO
     ├─→ Parsear SRT línea por línea
     ├─→ Traducir texto (skip metadata)
     ├─→ Manejar reintentos
     └─→ Retornar contenido traducido
```

### 3. Utility Layer

**FileUtils.kt**
```
Métodos Principales:

1. getPath(context, uri)
   ├─→ Detectar Android version
   ├─→ Android 10+: Copiar a caché
   ├─→ Android 6-9: Obtener ruta directa
   └─→ Retornar ruta absoluta

2. getFileName(context, uri)
   ├─→ Parsear URI según scheme
   └─→ Retornar nombre de archivo

3. createSubtitleFile(context, content, fileName)
   ├─→ Crear directorio de salida
   ├─→ Escribir contenido UTF-8
   └─→ Retornar objeto File

4. isValidMKVFile(filePath)
   ├─→ Verificar extensión
   └─→ Retornar boolean

5. getOutputDirectory(context)
   ├─→ Crear MKVSubtitles/ en caché
   └─→ Retornar directorio
```

### 4. Data Models

**Models.kt**
```
Subtitle
├── sequence: Int
├── startTime: String
├── endTime: String
└── text: String

SubtitleTrack
├── index: Int
├── language: String
├── codec: String
├── title: String
├── format: String
├── filePath: String
└── size: Long

TranslationResponse
├── success: Boolean
├── message: String
├── translated_content: String
└── error: String?

ExtractionResponse
├── success: Boolean
├── message: String
├── subtitles: List<SubtitleTrack>
└── error: String?

FileSelection
├── filePath: String
├── fileName: String
└── size: Long
```

---

## 🔄 Flujos de Datos Principales

### Flujo 1: Extracción de Subtítulos

```
Usuario selecciona MKV
         ↓
MainActivity valida archivo
         ↓
SubtitleExtractionService.extractSubtitles()
         ↓
Chaquopy llama Python
         ↓
extract_subtitles_with_ffmpeg()
         ├→ FFprobe: obtiene streams
         ├→ FFmpeg: extrae cada stream
         └→ Retorna JSON
         ↓
Parsear JSON → ExtractionResponse
         ↓
MainActivity obtiene List<SubtitleTrack>
         ↓
Navegar a SubtitleDisplayActivity
         ↓
Mostrar primer subtítulo en TextVie
```

### Flujo 2: Traducción de Subtítulos

```
Usuario selecciona idioma
         ↓
Usuario toca "Translate"
         ↓
TranslationService.translateSubtitleContent()
         ↓
Leer contenido SRT original
         ↓
Para cada línea:
    ├→ Si es timestamp/número: skip
    ├→ Si es texto: 
    │   ├→ Llamar translateText()
    │   ├→ OkHttp POST a LibreTranslate
    │   ├→ Parsear JSON resultado
    │   ├→ Manejar reintentos si falla
    │   └→ Agregar delay (50ms)
    └→ Agregar a lista
         ↓
Retornar contenido traducido
         ↓
Mostrar en TextVie con traducción
```

### Flujo 3: Guardado de Archivos

```
Usuario toca "Save"
         ↓
Obtener contenido de TextVie
         ↓
Generar nombre: subtitles_TIMESTAMP.srt
         ↓
FileUtils.createSubtitleFile()
         ├→ Crear MKVSubtitles/ en caché
         ├→ Escribir UTF-8
         └→ Retornar File
         ↓
Mostrar Toast con ruta
         ↓
Archivo disponible en caché
```

---

## 🔐 Manejo de Permisos

```
Android Versión         Estrategia
────────────────────────────────────────────────────

5.0 - 5.1 (21-22)      Permisos instalación
                        Solo en manifest

6.0 (23)               Runtime permissions
                        Request → Grant

7.0 (24)               FileProvider requerido

8.0 (26)               Background restrictions

10.0 (29)              Scoped Storage
                        Copiar a caché

11.0 (30)              MANAGE_EXTERNAL_STORAGE
                        MediaStore query

13.0 (33)              READ_MEDIA_VIDEO
                        READ_MEDIA_AUDIO

14.0 (34)              Más restricciones
                        Scoped Storage obligatorio
```

---

## 🌐 Integración con APIs Externas

### LibreTranslate API

```
Endpoint: https://libretranslate.com/translate

Request:
POST /translate HTTP/1.1
Content-Type: application/json

{
  "q": "Hello World",
  "source": "auto",
  "target": "es",
  "format": "text"
}

Response:
{
  "translatedText": "Hola Mundo"
}

Límites:
├─ Rate: 1 req/sec recomendado
├─ Timeout: 30 segundos
├─ Reintentos: 3 automáticos
└─ Delay: 50ms entre líneas
```

### Python Integration (Chaquopy)

```
Android App
    ↓ (IPC)
Chaquopy Runtime
    ↓
Python 3.9
    ├─ ffmpeg-python 0.2.1
    ├─ requests 2.31.0
    ├─ pymkv 1.0.10
    └─ pysrt 1.1.2
    ↓ (Shell execution)
FFmpeg/FFprobe binaries
    ↓
Resultado JSON
    ↓ (Parsing)
Android App
```

---

## 💾 Manejo de Almacenamiento

```
Ubicaciones:

1. Archivos originales
   └─ /storage/emulated/0/Downloads/*.mkv
      (Usuario selecciona vía file picker)

2. Archivos temporales
   └─ /data/data/com.example.mkvsubtitle/cache/MKVSubtitles/
      ├─ subtitle_0_eng.srt
      ├─ subtitle_1_spa.srt
      └─ subtitles_20241114_120000.srt

3. App cache
   └─ Context.cacheDir (limpieza automática)

4. Scoped Storage (Android 10+)
   └─ No acceso directo al almacenamiento externo
      └─ Copia a caché local

Ciclo de vida:
1. Usuario selecciona → Copy a caché
2. Procesar en caché
3. Guardar con timestamp
4. Usuario puede acceder vía file manager
```

---

## ⚡ Performance & Optimization

### Coroutines para Async Operations

```kotlin
lifecycleScope.launch {
    // En Main thread
    showProgressBar()
    
    try {
        // En Default dispatcher (CPU-bound)
        val response = extractionService.extractSubtitles(...)
        
        // De vuelta a Main thread
        displaySubtitles(response.subtitles)
    } catch (e: Exception) {
        showError(e.message)
    }
}
```

### Connection Pooling (OkHttp)

```kotlin
private val httpClient = OkHttpClient.Builder()
    .connectionPool(ConnectionPool(8, 30, TimeUnit.SECONDS))
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)
    .build()
```

### Caché de Respuestas

```
LibreTranslate API
    ├─ Sin caché en cliente (stateless)
    ├─ Reintentos con backoff exponencial
    └─ Rate limiting handling built-in
```

---

## 🐛 Error Handling Strategy

```
Layer 1: Input Validation
├─ FileUtils.isValidMKVFile()
├─ Validar tamaño de archivo
└─ Validar permisos

Layer 2: Process Execution
├─ Try-catch en Python calls
├─ Timeout handling
└─ Exit code checking

Layer 3: API Communication
├─ HTTP response codes
├─ Retry logic with backoff
└─ Fallback strategies

Layer 4: UI Presentation
├─ Toast messages
├─ Error dialogs
└─ User-friendly descriptions
```

---

## 📊 Architecture Patterns

### 1. **Separation of Concerns**
- UI Logic → Activities
- Business Logic → Services
- Data Access → FileUtils
- Data Models → Models.kt

### 2. **Dependency Injection**
```kotlin
class MainActivity : AppCompatActivity() {
    private lateinit var extractionService: SubtitleExtractionService
    
    override fun onCreate(savedInstanceState: Bundle?) {
        extractionService = SubtitleExtractionService(this)
    }
}
```

### 3. **Repository Pattern**
```
Services actúan como repositories
├─ SubtitleExtractionService
│  └─ Datos de Python/FFmpeg
└─ TranslationService
   └─ Datos de LibreTranslate API
```

### 4. **Observer Pattern**
```kotlin
// Coroutines + LiveData implícito
lifecycleScope.launch {
    val result = service.processData()
    // UI se actualiza cuando result cambia
}
```

---

## 🔗 Dependencias Entre Componentes

```
MainActivity
├─→ FileUtils (Static)
├─→ SubtitleExtractionService (Creado)
└─→ Intent → SubtitleDisplayActivity

SubtitleDisplayActivity
├─→ FileUtils (Static)
├─→ TranslationService (Creado)
└─→ Intent → MainActivity (back)

SubtitleExtractionService
├─→ Python (via Chaquopy)
└─→ Gson (Parsing)

TranslationService
├─→ OkHttp3 (HTTP Client)
├─→ Gson (JSON)
└─→ Kotlin Coroutines
```

---

## 📈 Escalabilidad Futura

```
Mejoras Posibles:

1. Agregar base de datos local
   └─ Room database para caché de traducciones

2. Implementar ViewModel
   └─ Estado compartido entre fragments

3. Inyección de dependencias
   └─ Hilt para DI automático

4. Testing mejorado
   └─ Mockito para unit tests
   └─ Espresso para UI tests

5. API customizable
   └─ Permitir diferentes backends de traducción
   └─ Plugins para otros formatos
```

---

**Arquitectura versión**: 1.0.0  
**Última actualización**: Noviembre 2024
