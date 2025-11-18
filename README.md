# MKV Subtitle Extractor

Una aplicación Android nativa completa para extraer, visualizar y traducir subtítulos de archivos MKV.

## 🎯 Características Principales

- **Extracción de Subtítulos**: Extrae automáticamente subtítulos de archivos MKV usando FFmpeg
- **Visualización Inteligente**: Muestra subtítulos en formato SRT con timestamps claramente organizados
- **Traducción Multiidioma**: Traduce subtítulos a 24+ idiomas usando LibreTranslate (sin API key)
- **Almacenamiento Seguro**: Gestiona permisos de almacenamiento en Android 6+ a 14+
- **Interfaz Intuitiva**: UI moderna con Material Design
- **Python Integration**: Chaquopy para scripts Python complejos

## 🏗️ Arquitectura

### Componentes Core

```
┌─────────────────────────────────────────────────────┐
│         Android UI Layer (Kotlin)                    │
│  ┌──────────────────┬─────────────────────────────┐ │
│  │  MainActivity    │  SubtitleDisplayActivity    │ │
│  │  - File Picker   │  - Display Subtitles       │ │
│  │  - Permissions   │  - Translate               │ │
│  └──────────────────┴─────────────────────────────┘ │
└─────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────┐
│         Services Layer (Kotlin)                      │
│  ┌──────────────────┬─────────────────────────────┐ │
│  │ Extraction       │ Translation                 │ │
│  │ Service          │ Service                     │ │
│  │ (Python Bridge)  │ (REST Client)              │ │
│  └──────────────────┴─────────────────────────────┘ │
└─────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────┐
│         External Services                            │
│  ┌──────────────────┬─────────────────────────────┐ │
│  │  FFmpeg          │  LibreTranslate API         │ │
│  │  (Extraction)    │  (Translation)              │ │
│  └──────────────────┴─────────────────────────────┘ │
└─────────────────────────────────────────────────────┘
```

## 📱 Pantallas

### Pantalla Principal (MainActivity)
- Selector de archivos MKV
- Visualización de archivo seleccionado
- Barra de progreso de extracción
- Estado actual del proceso
- Información y tutorial

### Pantalla de Visualización (SubtitleDisplayActivity)
- Visualización completa de subtítulos
- Selector de idioma para traducción
- Botón de traducción
- Botón para guardar
- Botón para volver atrás

## 🚀 Inicio Rápido

### Requisitos
- Android Studio 4.2+
- Android SDK 21+
- Java 11+
- Conexión a Internet (para traducción)

### Instalación

1. **Clonar el repositorio**
```bash
git clone https://github.com/tuusuario/MKVSubtitleExtractor.git
cd MKVSubtitleExtractor
```

2. **Abrir en Android Studio**
```bash
Android Studio → Open Project → Seleccionar carpeta
```

3. **Compilar y ejecutar**
```bash
./gradlew clean build
./gradlew installDebug
```

4. **Ejecutar en dispositivo**
```bash
./gradlew run
```

## 📋 Uso de la Aplicación

### Paso 1: Seleccionar Archivo
1. Abre la aplicación
2. Toca "Browse Files"
3. Selecciona un archivo .mkv
4. Verifica que el nombre y tamaño se muestren

### Paso 2: Extraer Subtítulos
1. Los subtítulos se extraen automáticamente
2. Espera a que se complete el proceso
3. Se navegará automáticamente a la pantalla de visualización

### Paso 3: Ver y Traducir
1. Los subtítulos se muestran en la pantalla
2. Selecciona un idioma del dropdown
3. Toca "Translate"
4. Los subtítulos traducidos aparecerán

### Paso 4: Guardar
1. Toca el botón "Save"
2. Los subtítulos se guardan en el caché de la aplicación
3. Recibirás la ruta donde se guardó

## 🔧 Configuración Avanzada

### Cambiar Servidor de Traducción

Edita `TranslationService.kt`:

```kotlin
// Cambiar de:
private const val LIBRETRANSLATE_URL = "https://libretranslate.com/translate"

// A tu servidor local:
private const val LIBRETRANSLATE_URL = "http://192.168.1.100:5000/translate"
```

### Agregar Idiomas

En `TranslationService.kt`, método `mapLanguageToCode()`:

```kotlin
"Idioma" to "codigo_ISO"
```

### Ajustar Timeouts

En `TranslationService.kt`:

```kotlin
.connectTimeout(60, TimeUnit.SECONDS)  // Aumentar a 60s
.readTimeout(60, TimeUnit.SECONDS)
```

## 🐍 Python Scripts

### extract_subtitles.py

Extrae subtítulos usando FFmpeg:

```python
extract_subtitles_with_ffmpeg(
    mkv_file: str,      # Ruta al archivo MKV
    output_dir: str     # Directorio de salida
) → Dict
```

Retorna JSON con información de subtítulos extraídos.

### translate_subtitles.py

Traduce contenido SRT:

```python
translate_subtitles(
    srt_content: str,   # Contenido SRT completo
    target_lang: str    # Idioma objetivo
) → Dict
```

## 📦 Dependencias

### Android
- AndroidX Core KTX
- AndroidX AppCompat
- Material Design
- Constraint Layout
- Lifecycle/Coroutines

### Networking
- Retrofit 2
- OkHttp3
- Gson

### Python (vía Chaquopy)
- ffmpeg-python
- requests
- pymkv
- pysrt

## 🔐 Permisos

```xml
<!-- Lectura de archivos multimedia -->
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_MEDIA_VIDEO" /> <!-- Android 13+ -->

<!-- Escritura (para caché) -->
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.MANAGE_EXTERNAL_STORAGE" />

<!-- Red -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

**Nota**: La aplicación solicita permisos en runtime (Android 6+).

## 🐛 Solución de Problemas

| Problema | Solución |
|----------|----------|
| "Python no disponible" | Verificar Chaquopy en build.gradle |
| FFmpeg no encontrado | Compilar FFmpeg como librería Android |
| Timeout en traducción | Aumentar timeout en TranslationService |
| Error de almacenamiento | Verificar permisos en configuración |
| MKV sin subtítulos | La app muestra mensaje apropiado |
| Caracteres especiales | Los scripts convierten a UTF-8 automáticamente |

## 📊 Formatos Soportados

### Entrada
- **MKV Container**: Matroska Video
- **Subtitle Streams**: SRT, ASS, SSA, VOB, DVD

### Salida
- **SRT (SubRip)**: Formato estándar de subtítulos
- **Con timestamp**: Preserva timings originales

## 🌐 Idiomas Soportados para Traducción

Español, Francés, Alemán, Chino (Simplificado/Tradicional), Árabe, Japonés, Portugués, Ruso, Coreano, Italiano, Holandés, Polaco, Turco, Griego, Húngaro, Sueco, Finlandés, Noruego, Danés, Checo, Rumano, Vietnamita, Tailandés, Indonesio

## 📈 Performance

- **Extracción**: Depends on file size (típicamente 5-30s)
- **Traducción**: ~1-2s por subtítulo (online)
- **Visualización**: Instant
- **Tamaño APK**: ~80-150MB (incluye Python + FFmpeg)

## 🔄 Ciclo de Vida

1. **Inicialización**: Python se inicia al abrir la app
2. **Selección**: Usuario elige archivo MKV
3. **Análisis**: Se valida formato con ffprobe
4. **Extracción**: FFmpeg extrae subtítulos
5. **Visualización**: Se muestran en pantalla
6. **Traducción** (opcional): LibreTranslate traduce
7. **Guardado**: Se guarda en caché con timestamp

## 🎨 Customización UI

### Colores (colors.xml)
```xml
<color name="primary_blue">#2196F3</color>
<color name="accent_green">#4CAF50</color>
<color name="error_red">#F44336</color>
```

### Strings (strings.xml)
Todos los textos son externalizados y localizables.

### Temas (themes.xml)
Basado en Material Design 3.

## 📚 Documentación

- **SETUP_GUIDE.md**: Guía completa de configuración
- **Inline Comments**: Código bien documentado
- **Docstrings Python**: Funciones descritas

## ⚠️ Limitaciones Conocidas

1. **FFmpeg**: Debe estar disponible en el dispositivo
2. **Rate Limiting**: LibreTranslate tiene limitaciones de solicitudes
3. **Tamaño Archivo**: Archivos MKV > 500MB pueden causar lentitud
4. **Red**: Traducción requiere conexión a Internet
5. **Almacenamiento**: Android 11+ usa Scoped Storage

## 🤝 Contribuir

Las contribuciones son bienvenidas:

1. Fork el proyecto
2. Crea una rama feature (`git checkout -b feature/AmazingFeature`)
3. Commit cambios (`git commit -m 'Add AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

## 📝 Licencia

Este proyecto está bajo la Licencia MIT - ver LICENSE.md para detalles.

## 🙏 Agradecimientos

- **FFmpeg**: Extracción de subtítulos
- **LibreTranslate**: Traducción de subtítulos
- **Chaquopy**: Integración Python en Android
- **Android Jetpack**: Componentes modernos

## 📧 Contacto

Para preguntas o reportar bugs:
- Abre un issue en GitHub
- Envía un email: support@example.com

## 🗺️ Roadmap

- [ ] Soporte para WebVTT
- [ ] Editor de subtítulos con sincronización manual
- [ ] Caché de traducciones previas
- [ ] Dark mode
- [ ] Múltiples idiomas simultáneamente
- [ ] Exportación a JSON/XML
- [ ] OCR para subtítulos quemados

## 📊 Estadísticas

- **Líneas de Kotlin**: ~1000+
- **Líneas de Python**: ~200+
- **Layouts XML**: 3
- **Servicios**: 2
- **Idiomas Soportados**: 24+

---

**Versión**: 1.0.0  
**Última Actualización**: Noviembre 2024  
**Status**: ✅ Completo y Funcional
