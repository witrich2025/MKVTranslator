Instrucciones para compilar `MKVSubtitleExtractor` en Ubuntu

Resumen rápido
- Requisitos: OpenJDK 11+ (recomendado 17), Android SDK (cmdline-tools), Git, y acceso a Internet.
- Uso: configurar `local.properties` con `sdk.dir` apuntando al Android SDK, luego ejecutar `./gradlew assembleDebug`.

1) Instalar paquetes en Ubuntu (Debian/Ubuntu)
Abrir terminal y ejecutar:

```bash
sudo apt update; 
sudo apt install -y openjdk-17-jdk unzip wget git
```

2) Instalar Android SDK (command-line tools)

```bash
# Crea directorio para SDK
mkdir -p $HOME/Android/Sdk; 
cd $HOME/Android/Sdk
# Descarga commandlinetools (reemplaza URL si es necesario)
wget https://dl.google.com/android/repository/commandlinetools-linux-9477386_latest.zip -O cmdline-tools.zip; 
unzip cmdline-tools.zip; 
mkdir -p cmdline-tools/latest; 
mv cmdline-tools/* cmdline-tools/latest/; 
rm cmdline-tools.zip

# Acepta licencias e instala plataformas y build-tools (ajusta versiones si se requiere)
export ANDROID_SDK_ROOT=$HOME/Android/Sdk
$ANDROID_SDK_ROOT/cmdline-tools/latest/bin/sdkmanager --sdk_root=$ANDROID_SDK_ROOT "platform-tools" "platforms;android-34" "build-tools;34.0.0"
$ANDROID_SDK_ROOT/cmdline-tools/latest/bin/sdkmanager --sdk_root=$ANDROID_SDK_ROOT --licenses
```

3) Ajustar `local.properties`
En la raíz del proyecto (donde está `settings.gradle`) crear `local.properties` con el contenido:

```
sdk.dir=/home/<tu-usuario>/Android/Sdk
```

4) Usar Gradle wrapper para compilar

```bash
cd /ruta/al/proyecto/MKVSubtitleExtractor
./gradlew clean assembleDebug
# APK generado en app/build/outputs/apk/debug/
```

5) Firmado (opcional - release)
Generar keystore y configurar `signingConfigs` en `app/build.gradle` según tus necesidades.

Notas
- El proyecto usa `compileSdk 34` y dependencias como `ffmpeg-kit`. Asegúrate de que `platforms;android-34` y `build-tools;34.0.0` estén instalados.
- Si fallan descargas de dependencias en CI/servidor, revisa acceso a repositorios Maven (jitpack y ffmpegkit) y versiones.

Si quieres, puedo adaptar las instrucciones para tu versión específica de Ubuntu o automatizar la creación de `local.properties` aquí mismo.