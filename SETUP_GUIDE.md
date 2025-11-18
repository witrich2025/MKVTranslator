# MKV Subtitle Extractor - Guía Completa de Desarrollo e Implementación

## Descripción General

**MKV Subtitle Extractor** es una aplicación Android nativa que permite:
1. Seleccionar archivos MKV desde el almacenamiento del dispositivo
2. Extraer subtítulos incrustados (SRT, ASS, SSA) usando FFmpeg
3. Mostrar los subtítulos en un formato legible
4. Traducir los subtítulos a 24+ idiomas usando LibreTranslate API
5. Guardar los subtítulos traducidos o originales en el dispositivo

## Arquitectura de la Aplicación

### Componentes Principales

```
├── MainActivity.kt
│   ├── File selection (File Picker)
│   ├── Permission handling
│   ├── Python initialization
│   └── Subtitle extraction orchestration
│
├── SubtitleDisplayActivity.kt
│   ├── Subtitle display
│   ├── Language selection
│   ├── Translation handling
│   └── File saving
│
├── Services/
│   ├── SubtitleExtractionService.kt (Python bridge)
│   └── TranslationService.kt (LibreTranslate API)
│
├── Utils/
│   └── FileUtils.kt (File operations & permissions)
│
├── Models/
│   └── Models.kt (Data classes)
│
└── Python/
    ├── extract_subtitles.py (FFmpeg wrapper)
    └── translate_subtitles.py (Translation handler)
```

## Requisitos Previos

### Software
- Android Studio 4.2 o superior
- Android SDK 21 (API level 21 - Lollipop) o superior
- Java 11 o superior
- Python 3.9 (incluido automáticamente por Chaquopy)

### Hardware
- Dispositivo Android o emulador con mínimo 2GB RAM
- 500MB de espacio de almacenamiento disponible

### Herramientas Externas
- FFmpeg (se debe instalar en el dispositivo o acceso a binarios compilados)
- Conexión a internet para traducción

## Configuración del Entorno de Desarrollo

### 1. Clonar/Configurar el Proyecto

```bash
cd /path/to/MKVSubtitleExtractor
```

### 2. Configurar Build.gradle

Los siguientes componentes ya están configurados:

```gradle
// Chaquopy para Python
id 'com.chaquo.python'

// Python packages
python {
    buildPython "python3.9"
    pip {
        install "ffmpeg-python==0.2.1"
        install "requests==2.31.0"
        install "pymkv==1.0.10"
        install "pysrt==1.1.2"
    }
}

// Dependencies
- androidx.core:core-ktx:1.12.0
- androidx.appcompat:appcompat:1.6.1
- com.google.android.material:material:1.11.0
- org.jetbrains.kotlin:kotlin-stdlib:1.9.20
- com.squareup.retrofit2:retrofit:2.9.0
- com.google.code.gson:gson:2.10.1
```

### 3. Compilar el Proyecto

```bash
./gradlew clean build
```

Si hay errores de Chaquopy, verificar:
- Android SDK tools están actualizados
- Gradle Wrapper es compatible con la versión de Android Studio

### 4. Ejecutar la Aplicación

```bash
./gradlew installDebug
./gradlew run
```

## Flujo de Uso de la Aplicación

### Paso 1: Seleccionar Archivo MKV
1. Abre la aplicación
2. Haz clic en "Browse Files"
3. Selecciona un archivo .mkv de tu almacenamiento
4. La aplicación mostrará el nombre y tamaño del archivo

### Paso 2: Extraer Subtítulos
1. Después de seleccionar un archivo, la app analizará los subtítulos disponibles
2. Automáticamente se inicia la extracción
3. Los subtítulos se extraen a formato SRT

### Paso 3: Ver Subtítulos
- Los subtítulos aparecen en la pantalla de visualización
- Se muestran con timestamps y textos legibles
- Se puede hacer scroll para ver más contenido

### Paso 4: Traducir (Opcional)
1. Selecciona un idioma del dropdown
2. Haz clic en "Translate"
3. La aplicación envía el contenido a LibreTranslate
4. Los subtítulos traducidos aparecen en pantalla

### Paso 5: Guardar
1. Haz clic en "Save" para guardar los subtítulos
2. Se guarda con timestamp en el almacenamiento caché
3. Puedes compartir o usar la ruta guardada

## Estructura de Archivos

### Layout XML
- `activity_main.xml` - Pantalla principal con selector de archivo
- `activity_subtitle_display.xml` - Pantalla de visualización y traducción

### Valores
- `strings.xml` - Todas las cadenas de texto localizables
- `colors.xml` - Paleta de colores
- `themes.xml` - Estilos de la aplicación

### Python
- `extract_subtitles.py` - Integración con FFmpeg para extracción
- `translate_subtitles.py` - Interfaz con LibreTranslate

## Manejo de Permisos

### Permisos Requeridos

```xml
<!-- Lectura de archivos -->
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.MANAGE_EXTERNAL_STORAGE" />

<!-- Escritura de archivos (para caché) -->
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />

<!-- Internet (para traducción) -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

### Manejo en Runtime

**Android 6.0+ (API 23)**: La aplicación solicita permisos en runtime
**Android 10+ (API 29)**: Utiliza Scoped Storage - los archivos se copian a caché
**Android 11+**: Requiere permisos explícitos de gestión de almacenamiento

## Configuración de API de Traducción

### Usando LibreTranslate (Recomendado - Gratuito)

LibreTranslate es de código abierto y **no requiere API key**:

```kotlin
// URL pública (con limitaciones de rate limit)
val LIBRETRANSLATE_URL = "https://libretranslate.com/translate"

// Payload (en TranslationService.kt)
val payload = mapOf(
    "q" to text,
    "source" to "auto",
    "target" to targetLanguage,
    "format" to "text"
)
```

**Limitaciones**:
- Rate limit: ~1 solicitud por segundo
- Algunas direcciones IP pueden estar bloqueadas
- Mejor calidad con 100-500 caracteres por solicitud

### Alternativa: Instancia Local de LibreTranslate

Para mejor rendimiento y sin límites:

1. Instalar LibreTranslate localmente:
```bash
pip install libretranslate
libretranslate --host 0.0.0.0 --port 5000
```

2. Modificar la URL en TranslationService.kt:
```kotlin
val LIBRETRANSLATE_URL = "http://192.168.1.100:5000/translate"
```

3. Asegurar que el dispositivo Android puede acceder a la red local

### Idiomas Soportados

```
Spanish (es)          |  French (fr)          |  German (de)
Chinese Simplified    |  Chinese Traditional  |  Arabic (ar)
Japanese (ja)         |  Portuguese (pt)      |  Russian (ru)
Korean (ko)           |  Italian (it)         |  Dutch (nl)
Polish (pl)           |  Turkish (tr)         |  Greek (el)
Hungarian (hu)        |  Swedish (sv)         |  Finnish (fi)
Norwegian (no)        |  Danish (da)          |  Czech (cs)
Romanian (ro)         |  Vietnamese (vi)      |  Thai (th)
Indonesian (id)
```

## Instalación de FFmpeg

FFmpeg es crítico para la extracción. La aplicación lo requiere.

### En el Dispositivo Android

La mayoría de dispositivos Android no tienen FFmpeg preinstalado. Hay dos opciones:

**Opción 1: Compilar como Biblioteca (Recomendado)**
```gradle
// Agregar a build.gradle
implementation 'com.brainsoftware:ffmpeg:4.4.2-2'
```

**Opción 2: Descargar Binarios Precompilados**
```python
# En extract_subtitles.py
import subprocess
ffmpeg_path = "/data/data/com.example.mkvsubtitle/lib/ffmpeg"
subprocess.run([ffmpeg_path, "-i", input_file, ...])
```

### Verificar FFmpeg en Runtime

```kotlin
fun checkFFmpegAvailable(): Boolean {
    return try {
        val process = Runtime.getRuntime().exec("which ffmpeg")
        process.waitFor() == 0
    } catch (e: Exception) {
        false
    }
}
```

## Solución de Problemas

### Problema: "Python no está disponible"
**Causa**: Chaquopy no se inicializó correctamente
**Solución**:
```kotlin
if (!Python.isStarted()) {
    Python.start(AndroidPlatform(this))
}
```

### Problema: FFmpeg no encontrado
**Causa**: FFmpeg no instalado o no en PATH
**Solución**:
1. Compilar FFmpeg como biblioteca Android
2. O usar "FFmpeg-Android" desde Github
3. Reemplazar rutas en extract_subtitles.py

### Problema: Timeout en traducción
**Causa**: Conexión de red lenta o LibreTranslate no responde
**Solución**:
- Usar instancia local de LibreTranslate
- Aumentar timeout en TranslationService:
```kotlin
.readTimeout(60, TimeUnit.SECONDS)
```

### Problema: Error de permisos en Android 11+
**Causa**: Scoped Storage requiere permisos especiales
**Solución**:
- La app copia archivos a caché automáticamente
- FileUtils.kt maneja esto en getRealPathFromUri()

### Problema: Subtítulos no se muestran correctamente
**Causa**: Encoding incorrecto o formato no soportado
**Solución**:
- Convertir a UTF-8:
```python
content.encode('utf-8').decode('utf-8')
```
- Soportar múltiples formatos (SRT, ASS, SSA)

## Tratamiento de Casos Extremos

### 1. MKV sin Subtítulos
```
Resultado esperado: Mensaje "No se encontraron subtítulos"
Código:
if not streams:
    result["message"] = "No se encontraron pistas de subtítulos"
    result["success"] = True
```

### 2. Archivos MKV Corruptos
```
Resultado esperado: Error con descripción clara
Validación:
- ffprobe verifica integridad
- FFmpeg detecta formatos inválidos
- Se muestra error al usuario
```

### 3. Archivos MUY GRANDES (> 500MB)
```
Limitación: Depende de RAM del dispositivo
Optimización:
- Extraer solo pistas de subtítulos (no video)
- Usar streaming si es posible
- Mostrar barra de progreso
```

### 4. Red No Disponible Durante Traducción
```
Fallback: Mostrar subtítulos originales
Manejo:
try {
    translatedText = translateText(...)
} catch (Exception e) {
    return originalText  // Fallback
}
```

### 5. Almacenamiento Insuficiente
```
Validación:
val availableSpace = File("/").freeSpace
if (availableSpace < requiredSpace) {
    showError("Espacio insuficiente")
}
```

## Características Implementadas

✅ **Extracción de Subtítulos**
- Múltiples pistas de subtítulos
- Formatos SRT, ASS, SSA
- Detección automática de idioma

✅ **Visualización**
- Scroll en tiempo real
- Formateo legible
- Timestamps preservados

✅ **Traducción**
- 24+ idiomas soportados
- Reintentos automáticos
- Rate limit handling

✅ **Almacenamiento**
- Caché segura
- Scoped Storage compliant
- Guardado con timestamp

✅ **Manejo de Errores**
- Excepciones capturadas
- Mensajes claros al usuario
- Logs para debugging

## Testing

### Test Manual

1. **Test de Extracción**:
   - Usar archivo MKV de prueba
   - Verificar que se extraiga SRT
   - Validar contenido en la UI

2. **Test de Traducción**:
   - Traducir a Spanish, French, German
   - Verificar calidad de traducción
   - Probar offline (debe fallar gracefully)

3. **Test de Permisos**:
   - Denegar permisos en Android 6+
   - Verificar solicitud runtime
   - Probar en Scoped Storage (Android 10+)

4. **Test de Casos Extremos**:
   - Archivo MKV sin subtítulos
   - Archivo MKV corrupto
   - Archivo muy grande
   - Red lenta/cortada

## Optimizaciones Futuras

1. **Caché de Traducción**: Guardar traducciones frecuentes
2. **OCR de Subtítulos**: Para subtítulos quemados en video
3. **Editor de Subtítulos**: Sincronización manual de timestamps
4. **Múltiples Idiomas**: Traducir a varios idiomas simultáneamente
5. **Exportación**: WebVTT, JSON, XML además de SRT
6. **UI Mejorada**: Tema oscuro, fragmentos, animaciones

## Distribución

### Compilar APK de Producción

```bash
./gradlew clean build

# Generar APK signado
./gradlew bundleRelease
```

El APK estará en: `app/build/outputs/apk/release/app-release.apk`

### Requisitos de Play Store

- targetSdk = 34 (actualizado para octubre 2024)
- Política de privacidad requerida
- Cumplimiento con Play Store policies

## Licencia

Proyecto educativo - Usar libremente para desarrollo y aprendizaje.

## Contacto y Soporte

Para problemas o preguntas:
1. Revisar esta documentación
2. Consultar logs en Android Studio
3. Verificar permisos del dispositivo
4. Probar en dispositivo físico si es posible

---

**Última actualización**: Noviembre 2024
**Versión**: 1.0.0
