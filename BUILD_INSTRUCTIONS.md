# Build Instructions - MKV Subtitle Extractor

## 📋 Tabla de Contenidos

1. [Requisitos Previos](#requisitos-previos)
2. [Configuración Inicial](#configuración-inicial)
3. [Build Debug](#build-debug)
4. [Build Release](#build-release)
5. [Troubleshooting](#troubleshooting)
6. [Optimizaciones](#optimizaciones)

## Requisitos Previos

### Software Requerido
- **Android Studio**: 4.2 o superior (descarga desde developer.android.com)
- **Java Development Kit (JDK)**: 11 o superior
- **Android SDK**: 
  - API 21 (Android 5.0 Lollipop) - Mínimo
  - API 34 (Android 14) - Recomendado
- **Android NDK**: Recomendado para compilación optimizada
- **Python**: 3.9 (se descarga automáticamente via Chaquopy)

### Hardware Requerido
- **Laptop/Desktop**: Mínimo 4GB RAM, 8GB recomendado
- **SSD**: Mínimo 5GB espacio libre para Android Studio + SDK
- **Dispositivo Android**: API 21+ con 2GB RAM disponible

### Herramientas de Desarrollo
```bash
# Verificar versión de Java
java -version

# Debe retornar Java 11+
# Ejemplo: openjdk version "11.0.15" 2022-04-19
```

## Configuración Inicial

### Paso 1: Clonar el Repositorio

```bash
# Opción 1: HTTPS
git clone https://github.com/usuario/MKVSubtitleExtractor.git
cd MKVSubtitleExtractor

# Opción 2: SSH (si tienes SSH key)
git clone git@github.com:usuario/MKVSubtitleExtractor.git
cd MKVSubtitleExtractor
```

### Paso 2: Configurar Android Studio

```bash
# Abrir Android Studio
# File → New → Open Project → Seleccionar carpeta MKVSubtitleExtractor

# O desde línea de comando
studio .
```

### Paso 3: Descargar Componentes

Android Studio descargará automáticamente:
- Gradle Wrapper (7.0+)
- Android SDK Build Tools (34.0.0+)
- Chaquopy Plugin

**Tiempo esperado**: 5-15 minutos (depende de conexión)

### Paso 4: Sincronizar Gradle

```bash
# Android Studio → Gradle → Sync Now
# O desde terminal
./gradlew clean
```

## Build Debug

### Compilación Local (Desarrollo)

```bash
# Build debug APK
./gradlew clean build

# O con más verbosidad
./gradlew clean build --stacktrace

# Resultado: app/build/outputs/apk/debug/app-debug.apk
```

### Instalar en Dispositivo/Emulador

**Conectar dispositivo o iniciar emulador primero**

```bash
# Instalar
./gradlew installDebug

# O directamente en Android Studio:
# Build → Build Bundle(s) / APK(s) → Build APK(s)
# Luego Run → Run 'app'
```

### Ejecutar en Emulador

```bash
# Desde terminal (necesita Android Emulator en PATH)
emulator -avd Pixel_5_API_31 &

# O crear emulador desde Android Studio:
# Tools → Device Manager → Create Virtual Device
```

### Ver Logs en Tiempo Real

```bash
# Terminal 1: Ejecutar app
./gradlew run

# Terminal 2: Ver logs
adb logcat | grep "MKVSubtitle"

# O desde Android Studio:
# View → Tool Windows → Logcat
```

## Build Release

### Preparar Compilación Release

```bash
# 1. Incrementar versión en build.gradle
versionCode = 2  # Incrementar
versionName = "1.1.0"  # Nuevo version

# 2. Limpiar artefactos previos
./gradlew clean

# 3. Build release APK
./gradlew assembleRelease

# 4. O crear bundle para Play Store
./gradlew bundleRelease

# Resultado:
# APK: app/build/outputs/apk/release/app-release.apk
# Bundle: app/build/outputs/bundle/release/app-release.aab
```

### Firmar APK

El APK release necesita firma digital. Hay dos opciones:

**Opción 1: Usando Android Studio**

```
Build → Generate Signed Bundle/APK
```

Pasos:
1. Seleccionar "APK"
2. Crear nuevo keystore o usar existente
3. Ingresar contraseña y alias
4. Seleccionar release
5. Click "Finish"

**Opción 2: Desde Línea de Comando**

```bash
# 1. Crear keystore (solo primera vez)
keytool -genkey -v -keystore release.keystore \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias mkvsubtitle

# 2. Firmar APK
jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 \
  -keystore release.keystore \
  app/build/outputs/apk/release/app-release-unsigned.apk \
  mkvsubtitle

# 3. Optimizar con zipalign
zipalign -v 4 \
  app/build/outputs/apk/release/app-release-unsigned.apk \
  app/build/outputs/apk/release/app-release.apk
```

### Verificar Firma

```bash
jarsigner -verify -verbose app-release.apk
```

Resultado esperado:
```
jar verified.
```

## Troubleshooting

### Error 1: "Gradle build failed"

**Solución**:
```bash
# Limpiar caché
./gradlew clean

# Invalidar caché de Gradle
rm -rf ~/.gradle

# Intentar nuevamente
./gradlew build --stacktrace
```

### Error 2: "Chaquopy not found"

**Solución**:
1. Verificar build.gradle tiene: `id 'com.chaquo.python'`
2. Sincronizar Gradle: `./gradlew sync`
3. Usar versión correcta de Chaquopy: `14.0.2+`

### Error 3: "SDK not installed"

**Solución**:
```bash
# Aceptar licencias
yes | /path/to/Android/sdk/tools/bin/sdkmanager --licenses

# Descargar SDK específico
/path/to/Android/sdk/tools/bin/sdkmanager "platforms;android-34"
```

### Error 4: "FFmpeg not found"

**Solución**:
Esta es una limitación conocida. Opciones:
1. Compilar FFmpeg como librería Android
2. Usar `ffmpeg-android-java` from GitHub
3. Distribuir binarios con APK

### Error 5: "Out of memory"

**Solución**:
```bash
# Aumentar heap size para Gradle
export GRADLE_OPTS="-Xmx4096m"
./gradlew build
```

Alternativa permanente en `gradle.properties`:
```properties
org.gradle.jvmargs=-Xmx4096m
```

### Error 6: "Python build failed"

**Solución**:
```bash
# Verificar Python 3.9 está disponible
python3 --version

# Limpiar caché de Python de Chaquopy
rm -rf ~/.chaquo

# Rebuild
./gradlew clean build
```

## Optimizaciones

### 1. Acelerar Build

```bash
# Usar Gradle daemon
echo "org.gradle.daemon=true" >> gradle.properties

# Habilitar compilación en paralelo
echo "org.gradle.parallel=true" >> gradle.properties

# Aumentar workers
echo "org.gradle.workers.max=8" >> gradle.properties
```

### 2. Reducir Tamaño APK

```gradle
// En build.gradle
buildTypes {
    release {
        minifyEnabled true  // ProGuard/R8
        shrinkResources true  // Remover recursos no usados
        proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
    }
}
```

### 3. Habilitar BuildCache

```bash
echo "org.gradle.build.cache=true" >> gradle.properties
```

### 4. Usar Compilación Offline

```bash
./gradlew build --offline
```

### 5. Análisis de Build

```bash
# Ver duración de cada tarea
./gradlew build --profile

# Resultado en: build/reports/profile/
```

## Configuración de CI/CD

### GitHub Actions Example

```yaml
name: Build APK

on: [push, pull_request]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      
      - name: Set up JDK
        uses: actions/setup-java@v2
        with:
          java-version: '11'
      
      - name: Build APK
        run: ./gradlew build
      
      - name: Upload APK
        uses: actions/upload-artifact@v2
        with:
          name: app-debug.apk
          path: app/build/outputs/apk/debug/app-debug.apk
```

## Testing Before Release

```bash
# 1. Ejecutar tests unitarios
./gradlew test

# 2. Ejecutar tests instrumentados
./gradlew connectedAndroidTest

# 3. Análisis de código estático
./gradlew lint
```

## Distribución

### Play Store

1. Crear cuenta developer (USD $25 una vez)
2. Generar release APK (ver [Build Release](#build-release))
3. Subir a Play Store Console
4. Completar información de la app
5. Enviar para revisión (típicamente 24-48 horas)

### GitHub Releases

```bash
# Crear tag
git tag v1.0.0

# Push a GitHub
git push origin v1.0.0

# Luego agregar en GitHub:
# Releases → Create Release
# Adjuntar app-release.apk
```

### Distribución Manual

```bash
# Compartir APK directamente
# Opción 1: Email o Mensajería
# Opción 2: Servidor web
# Opción 3: QR code con enlace
```

## Información Técnica de Build

### Gradle Wrapper

El proyecto usa Gradle Wrapper para consistencia:

```bash
# Verificar versión
./gradlew --version

# Actualizar wrapper
./gradlew wrapper --gradle-version 8.2
```

### Versiones de Componentes

```
Android Gradle Plugin: 8.2.2
Gradle: 8.2+
Kotlin: 1.9.20
Chaquopy: 14.0.2
```

### Tamaño de Artefactos

| Tipo | Tamaño |
|------|--------|
| app-debug.apk | ~150-180 MB |
| app-release.apk | ~80-120 MB |
| app-release.aab | ~60-90 MB |

## Limpieza Después de Build

```bash
# Eliminar archivos compilados
./gradlew clean

# Eliminar todo incluyendo caché
rm -rf build/
rm -rf .gradle/
```

## Referencia Rápida

```bash
# Build
./gradlew build

# Build Debug
./gradlew assembleDebug

# Build Release
./gradlew assembleRelease

# Instalar y ejecutar
./gradlew run

# Tests
./gradlew test

# Lint (análisis de código)
./gradlew lint

# Limpieza
./gradlew clean
```

---

**Última actualización**: Noviembre 2024  
**Gradle Version**: 8.2+  
**AGP Version**: 8.2.2
