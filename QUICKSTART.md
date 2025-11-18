# Quick Start - MKV Subtitle Extractor

## ⚡ Inicio Rápido (5 minutos)

### 1. Preparar Proyecto

```bash
cd MKVSubtitleExtractor
./gradlew clean build
```

### 2. Conectar Dispositivo

```bash
# Habilitar USB Debugging en dispositivo
# Settings → Developer Options → USB Debugging → ON

# Verificar conexión
adb devices
```

### 3. Instalar y Ejecutar

```bash
./gradlew run
# O desde Android Studio: Run → Run 'app'
```

---

## 📱 Primer Uso en la App

### Paso 1: Seleccionar Archivo
1. Toca **"Browse Files"**
2. Navega a un archivo `.mkv`
3. Selecciona el archivo

### Paso 2: Extraer Subtítulos
- La extracción comienza automáticamente
- Espera a que se complete (barra de progreso)
- Serás redirigido a la pantalla de visualización

### Paso 3: Ver Subtítulos
- Los subtítulos se muestran automáticamente
- Scroll para ver más contenido
- Verifica que el contenido sea legible

### Paso 4: Traducir (Opcional)
1. Selecciona idioma del dropdown (ej: "Spanish")
2. Toca **"Translate"**
3. Espera (requiere conexión a internet)
4. Los subtítulos traducidos aparecerán

### Paso 5: Guardar
1. Toca **"Save"**
2. Recibirás notificación de ubicación guardada
3. Archivo listo para usar

---

## 🔧 Troubleshooting Rápido

| Problema | Solución |
|----------|----------|
| App no compila | `./gradlew clean build --stacktrace` |
| Permisos rechazados | Ir a Settings → App → Permisos → Habilitar almacenamiento |
| No ve archivos MKV | Usar file manager para verificar ubicación del archivo |
| Traducción no funciona | Verificar conexión a internet |
| App se congela | Usar archivo MKV más pequeño para testing |

---

## 📁 Archivos Importantes

```
├── app/build.gradle ............... Dependencias + configuración Python
├── app/src/main/
│   ├── AndroidManifest.xml ......... Permisos y actividades
│   ├── java/com/example/mkvsubtitle/
│   │   ├── MainActivity.kt ......... Pantalla principal
│   │   ├── SubtitleDisplayActivity.kt . Pantalla de subtítulos
│   │   ├── services/ .............. Lógica de negocio
│   │   └── utils/ ................. Funciones auxiliares
│   └── res/layout/ ................ Interfaces UI
├── README.md ...................... Documentación general
├── SETUP_GUIDE.md ................. Guía detallada
└── BUILD_INSTRUCTIONS.md .......... Cómo compilar
```

---

## 🎯 Flujo de la Aplicación

```
MainActivity (Pantalla 1)
    ↓
Seleccionar archivo MKV
    ↓
Validar que sea .mkv
    ↓
Extraer subtítulos (Python/FFmpeg)
    ↓
SubtitleDisplayActivity (Pantalla 2)
    ↓
Ver subtítulos
    ↓
[Opcional] Traducir a otro idioma
    ↓
Guardar archivo
    ↓
Finalizar
```

---

## 💡 Tips

1. **Para Testing**: Usa archivo MKV pequeño (< 100MB)
2. **Sin Internet**: La traducción fallará pero los subtítulos se mostrarán
3. **Dispositivos Antiguos**: API 21+ soportado (Android 5.0+)
4. **Storage**: Los archivos se guardan en caché de la app
5. **Permisos**: Se solicitan automáticamente al usar la app

---

## 📚 Documentación Completa

| Documento | Contenido |
|-----------|----------|
| README.md | Resumen general del proyecto |
| SETUP_GUIDE.md | Arquitectura y configuración detallada |
| BUILD_INSTRUCTIONS.md | Cómo compilar y optimizar |
| TESTING_GUIDE.md | Casos de prueba y testing |
| IMPLEMENTATION_SUMMARY.md | Lista de todo implementado |

---

## 🚀 Próximas Acciones

- [ ] Compilar proyecto (`./gradlew build`)
- [ ] Conectar dispositivo con `adb`
- [ ] Instalar app (`./gradlew run`)
- [ ] Probar con archivo MKV real
- [ ] Traducir a un idioma
- [ ] Guardar resultado
- [ ] Leer documentación completa

---

## ❓ ¿Necesitas Ayuda?

1. **Problemas de compilación**: Ver `BUILD_INSTRUCTIONS.md`
2. **Cómo funciona la app**: Ver `SETUP_GUIDE.md`
3. **Cómo testear**: Ver `TESTING_GUIDE.md`
4. **Qué se implementó**: Ver `IMPLEMENTATION_SUMMARY.md`

---

**¡Listo para empezar!**

```bash
cd MKVSubtitleExtractor && ./gradlew run
```
