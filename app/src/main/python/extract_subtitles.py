#!/usr/bin/env python3
"""
Script para extraer subtítulos de archivos MKV
Soporta múltiples formatos: SRT, ASS, SSA
Utiliza FFmpeg para extracción
"""

import os
import sys
import json
import subprocess
from pathlib import Path
from typing import Dict, List, Any

# Configuración
FFMPEG_TIMEOUT = 300  # 5 minutos para operaciones de FFmpeg


def extract_subtitles_with_ffmpeg(mkv_file: str, output_dir: str) -> Dict[str, Any]:
    """
    Extrae subtítulos de un archivo MKV usando FFmpeg
    
    Args:
        mkv_file: Ruta al archivo MKV
        output_dir: Directorio de salida para los subtítulos
    
    Returns:
        dict: Información sobre los subtítulos extraídos
    """
    
    result = {
        "success": False,
        "message": "",
        "subtitles": [],
        "error": None
    }
    
    try:
        # Crear directorio si no existe
        os.makedirs(output_dir, exist_ok=True)
        
        # Validar que el archivo MKV existe
        if not os.path.exists(mkv_file):
            result["error"] = f"File not found: {mkv_file}"
            result["message"] = "El archivo MKV no existe"
            return result
        
        # Usar ffprobe para obtener información de los subtítulos
        probe_cmd = [
            "ffprobe",
            "-v", "error",
            "-select_streams", "s",
            "-show_entries", "stream=index,codec_name,tags=title,tags=language",
            "-of", "json",
            mkv_file
        ]
        
        probe_result = subprocess.run(probe_cmd, capture_output=True, text=True, timeout=FFMPEG_TIMEOUT)
        
        if probe_result.returncode != 0:
            result["error"] = probe_result.stderr
            result["message"] = "Error al examinar el archivo MKV"
            return result
        
        try:
            probe_data = json.loads(probe_result.stdout)
            streams = probe_data.get("streams", [])
        except json.JSONDecodeError:
            result["error"] = "Invalid JSON from ffprobe"
            result["message"] = "Error al parsear datos de ffprobe"
            return result
        
        if not streams:
            result["message"] = "No se encontraron pistas de subtítulos"
            result["success"] = True
            return result
        
        # Extraer cada pista de subtítulos
        for stream in streams:
            stream_index = stream.get("index")
            codec_name = stream.get("codec_name", "unknown")
            tags = stream.get("tags", {})
            title = tags.get("title", f"Subtitle {stream_index}")
            language = tags.get("language", "unknown")
            
            # Determinar extensión basada en codec
            ext = "srt"  # SRT es el formato por defecto
            if codec_name in ["ass", "ssa"]:
                ext = "ass"
            elif codec_name == "dvd_subtitle":
                ext = "srt"  # Convertir DVD subtitles a SRT
            
            output_file = os.path.join(output_dir, f"subtitle_{stream_index}_{language}.{ext}")
            
            # Comando para extraer
            extract_cmd = [
                "ffmpeg",
                "-i", mkv_file,
                "-map", f"0:s:{stream_index}",
                "-c", "copy",
                "-y",
                output_file
            ]
            
            try:
                extract_result = subprocess.run(
                    extract_cmd,
                    capture_output=True,
                    text=True,
                    timeout=FFMPEG_TIMEOUT
                )
                
                if extract_result.returncode == 0 and os.path.exists(output_file):
                    result["subtitles"].append({
                        "file": output_file,
                        "index": stream_index,
                        "codec": codec_name,
                        "language": language,
                        "title": title,
                        "format": ext,
                        "size": os.path.getsize(output_file)
                    })
                else:
                    # Intenta como SRT de todos modos
                    fallback_file = os.path.join(output_dir, f"subtitle_{stream_index}.srt")
                    fallback_cmd = [
                        "ffmpeg",
                        "-i", mkv_file,
                        "-map", f"0:s:{stream_index}",
                        "-c:s", "srt",
                        "-y",
                        fallback_file
                    ]
                    fallback_result = subprocess.run(fallback_cmd, capture_output=True, text=True, timeout=FFMPEG_TIMEOUT)
                    
                    if fallback_result.returncode == 0 and os.path.exists(fallback_file):
                        result["subtitles"].append({
                            "file": fallback_file,
                            "index": stream_index,
                            "codec": codec_name,
                            "language": language,
                            "title": title,
                            "format": "srt",
                            "size": os.path.getsize(fallback_file)
                        })
            
            except subprocess.TimeoutExpired:
                result["error"] = f"Timeout extracting subtitle {stream_index}"
                continue
            except Exception as e:
                result["error"] = str(e)
                continue
        
        result["success"] = True
        result["message"] = f"Successfully extracted {len(result['subtitles'])} subtitle(s)"
        
    except Exception as e:
        result["error"] = str(e)
        result["message"] = f"Error during extraction: {str(e)}"
    
    return result


if __name__ == "__main__":
    if len(sys.argv) < 3:
        print(json.dumps({
            "success": False,
            "error": "Usage: extract_subtitles.py <mkv_file> <output_dir>",
            "message": "Argumentos insuficientes"
        }))
        sys.exit(1)
    
    mkv_file = sys.argv[1]
    output_dir = sys.argv[2]
    
    result = extract_subtitles_with_ffmpeg(mkv_file, output_dir)
    print(json.dumps(result, ensure_ascii=False, indent=2))
