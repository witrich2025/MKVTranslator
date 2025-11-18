#!/usr/bin/env python3
"""
Script para traducir subtítulos usando LibreTranslate o Google Translate
"""

import sys
import json
import time
import requests
from typing import Dict, Any

# Configuración
TRANSLATION_TIMEOUT = 30
RETRY_ATTEMPTS = 3
RETRY_DELAY = 2
LIBRETRANSLATE_URL = "https://libretranslate.com/translate"


def translate_subtitles(srt_content: str, target_lang: str) -> Dict[str, Any]:
    """
    Traduce los subtítulos del contenido SRT.
    
    Args:
        srt_content: Contenido del archivo SRT
        target_lang: Código de idioma ISO 639-1 o nombre
        
    Returns:
        dict: Con el contenido traducido o error
    """
    
    result = {
        "success": False,
        "message": "",
        "translated_content": "",
        "error": None
    }
    
    try:
        # Mapeo de idiomas a códigos ISO
        lang_map = {
            "Spanish": "es", "Español": "es",
            "French": "fr", "Francés": "fr",
            "German": "de", "Alemán": "de",
            "Chinese (Simplified)": "zh", "Chino (Simplificado)": "zh",
            "Chinese (Traditional)": "zh-TW", "Chino (Tradicional)": "zh-TW",
            "Arabic": "ar", "Árabe": "ar",
            "Japanese": "ja", "Japonés": "ja",
            "Portuguese": "pt", "Portugués": "pt",
            "Russian": "ru", "Ruso": "ru",
            "Korean": "ko", "Coreano": "ko",
            "Italian": "it", "Italiano": "it",
            "Dutch": "nl", "Holandés": "nl",
            "Polish": "pl", "Polaco": "pl",
            "Turkish": "tr", "Turco": "tr",
            "Greek": "el", "Griego": "el",
            "Hungarian": "hu", "Húngaro": "hu",
            "Swedish": "sv", "Sueco": "sv",
            "Finnish": "fi", "Finlandés": "fi",
            "Norwegian": "no", "Noruego": "no",
            "Danish": "da", "Danés": "da",
            "Czech": "cs", "Checo": "cs",
            "Romanian": "ro", "Rumano": "ro",
            "Vietnamese": "vi", "Vietnamita": "vi",
            "Thai": "th", "Tailandés": "th",
            "Indonesian": "id", "Indonesio": "id",
            "Filipino": "fil", "Filipino": "fil",
            "Malay": "ms", "Malayo": "ms",
        }
        
        # Obtener código de idioma
        lang_code = lang_map.get(target_lang, target_lang.lower())
        
        # Procesar líneas del SRT
        lines = srt_content.split('\n')
        translated_lines = []
        
        for line in lines:
            stripped = line.strip()
            
            # Saltar líneas vacías, números de subtitle y timestamps
            if not stripped or stripped[0].isdigit() or '-->' in line:
                translated_lines.append(line)
            else:
                # Traducir línea de texto
                try:
                    translated = translate_text(stripped, lang_code)
                    translated_lines.append(translated)
                except Exception as e:
                    # Fallback a texto original si traducción falla
                    translated_lines.append(line)
                    print(f"Warning: Could not translate line: {e}", file=sys.stderr)
                
                # Pequeño delay para evitar rate limiting
                time.sleep(0.05)
        
        result["translated_content"] = '\n'.join(translated_lines)
        result["success"] = True
        result["message"] = "Subtítulos traducidos exitosamente"
        
    except Exception as e:
        result["error"] = str(e)
        result["message"] = f"Error during translation: {str(e)}"
    
    return result


def translate_text(text: str, target_lang: str, retries: int = RETRY_ATTEMPTS) -> str:
    """
    Traduce texto usando LibreTranslate API.
    
    Args:
        text: Texto a traducir
        target_lang: Código de idioma ISO 639-1
        retries: Número de reintentos
        
    Returns:
        Texto traducido o texto original si falla
    """
    if not text.strip():
        return text
    
    payload = {
        "q": text,
        "source": "auto",
        "target": target_lang,
        "format": "text"
    }
    
    for attempt in range(retries):
        try:
            response = requests.post(
                LIBRETRANSLATE_URL,
                json=payload,
                timeout=TRANSLATION_TIMEOUT
            )
            
            if response.status_code == 200:
                result_json = response.json()
                if 'translatedText' in result_json:
                    return result_json['translatedText']
            elif response.status_code == 429:  # Rate limit
                if attempt < retries - 1:
                    time.sleep(RETRY_DELAY * (attempt + 1))
                    continue
                else:
                    return text
            else:
                if attempt < retries - 1:
                    time.sleep(RETRY_DELAY)
                    continue
        
        except requests.exceptions.Timeout:
            if attempt < retries - 1:
                time.sleep(RETRY_DELAY)
                continue
        except requests.exceptions.ConnectionError:
            if attempt < retries - 1:
                time.sleep(RETRY_DELAY)
                continue
        except Exception as e:
            if attempt < retries - 1:
                time.sleep(RETRY_DELAY)
                continue
    
    # Fallback: retornar texto original
    return text


if __name__ == "__main__":
    if len(sys.argv) < 3:
        print(json.dumps({
            "success": False,
            "error": "Usage: translate_subtitles.py <srt_content> <target_language>",
            "message": "Argumentos insuficientes"
        }))
        sys.exit(1)
    
    srt_content = sys.argv[1]
    target_lang = sys.argv[2]
    
    result = translate_subtitles(srt_content, target_lang)
    print(json.dumps(result, ensure_ascii=False, indent=2))
