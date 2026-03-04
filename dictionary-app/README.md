# Free Dictionary Lookup – C++ GTK3 Application

A cross-platform desktop application that queries the
[Free Dictionary API](https://api.dictionaryapi.dev/) and displays
well-formatted word definitions, pronunciations, parts of speech, usage
examples, synonyms, and antonyms.

## Features

| Feature | Details |
|---|---|
| Word lookup | Type any word and press **Enter** or click **Look Up** |
| Language selector | 13 languages supported (see list below) |
| Pronunciation | Phonetic text displayed for each entry |
| Parts of speech | Noun, verb, adjective, etc. |
| Definitions | Numbered list of definitions |
| Usage examples | Inline example sentences |
| Synonyms / Antonyms | Listed per meaning |
| Async requests | UI stays responsive during API calls |

### Supported languages

English (US/UK), Hindi, Spanish, French, Japanese, Russian, German,
Italian, Korean, Brazilian Portuguese, Arabic, Turkish.

## Requirements

| Dependency | Version |
|---|---|
| CMake | ≥ 3.16 |
| GCC / Clang | C++17 |
| GTK+ 3 dev | ≥ 3.20 |
| OpenSSL dev | ≥ 1.1 |
| nlohmann/json dev | ≥ 3.2 |

### Ubuntu / Debian

```bash
sudo apt-get install cmake g++ libgtk-3-dev libssl-dev nlohmann-json3-dev
```

### Fedora / RHEL

```bash
sudo dnf install cmake gcc-c++ gtk3-devel openssl-devel json-devel
```

### macOS (Homebrew)

```bash
brew install cmake gtk+3 openssl nlohmann-json
```

## Build

```bash
# From the dictionary-app directory:
cmake -B build -DCMAKE_BUILD_TYPE=Release
cmake --build build -j$(nproc)
```

The binary is placed at `build/dictionary-app`.

## Run

```bash
./build/dictionary-app
```

> **Note:** An active internet connection is required to reach
> `api.dictionaryapi.dev`.

## API

The app uses the public [Free Dictionary API](https://dictionaryapi.dev/):

```
GET https://api.dictionaryapi.dev/api/v2/entries/{language}/{word}
```

No API key is needed.

## Screenshot

![Dictionary App screenshot](screenshot.png)
*(screenshot taken on Ubuntu 24.04)*
