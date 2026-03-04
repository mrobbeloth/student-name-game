cmake_minimum_required(VERSION 3.16)
project(DictionaryApp VERSION 1.0.0 LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# ── Dependencies ──────────────────────────────────────────────────────────────
find_package(PkgConfig REQUIRED)
pkg_check_modules(GTK3 REQUIRED gtk+-3.0)
find_package(OpenSSL REQUIRED)
find_package(nlohmann_json 3.2.0 REQUIRED)

# ── Executable ────────────────────────────────────────────────────────────────
add_executable(dictionary-app src/main.cpp)

target_include_directories(dictionary-app
    PRIVATE
        ${GTK3_INCLUDE_DIRS}
        ${CMAKE_CURRENT_SOURCE_DIR}/include   # httplib.h
)

target_compile_options(dictionary-app PRIVATE ${GTK3_CFLAGS_OTHER})

target_link_libraries(dictionary-app
    PRIVATE
        ${GTK3_LIBRARIES}
        OpenSSL::SSL
        OpenSSL::Crypto
        nlohmann_json::nlohmann_json
        pthread
)

install(TARGETS dictionary-app DESTINATION bin)
