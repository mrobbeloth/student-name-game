// Free Dictionary API – GTK3 GUI Application
// Looks up words via https://api.dictionaryapi.dev/ and displays
// pronunciation, parts of speech, definitions, examples and synonyms.

#include <gtk/gtk.h>
#include <nlohmann/json.hpp>
#include <string>
#include <sstream>
#include <iomanip>
#include <thread>
#include <mutex>
#include <atomic>

#define CPPHTTPLIB_OPENSSL_SUPPORT
#include "httplib.h"

using json = nlohmann::json;

// ── Widgets shared between callbacks ──────────────────────────────────────────
static GtkWidget *g_entry_word   = nullptr;
static GtkWidget *g_combo_lang   = nullptr;
static GtkWidget *g_text_view    = nullptr;
static GtkWidget *g_spinner      = nullptr;
static GtkWidget *g_btn_lookup   = nullptr;

// Language options supported by the Free Dictionary API
static const struct { const char *label; const char *code; } LANGUAGES[] = {
    { "English (US)",      "en"    },
    { "English (UK)",      "en_GB" },
    { "Hindi",             "hi"    },
    { "Spanish",           "es"    },
    { "French",            "fr"    },
    { "Japanese",          "ja"    },
    { "Russian",           "ru"    },
    { "German",            "de"    },
    { "Italian",           "it"    },
    { "Korean",            "ko"    },
    { "Brazilian Portuguese", "pt-BR" },
    { "Arabic",            "ar"    },
    { "Turkish",           "tr"    },
};
static constexpr int LANGUAGE_COUNT = sizeof(LANGUAGES) / sizeof(LANGUAGES[0]);

// Tracks whether the application is shutting down so worker threads do not
// call g_idle_add after the GLib main context has been destroyed.
static std::atomic<bool> g_app_quitting{false};

// ── Helpers ───────────────────────────────────────────────────────────────────

// Percent-encode a UTF-8 string for use in a URL path segment.
static std::string url_encode(const std::string &s)
{
    std::ostringstream out;
    out << std::hex << std::uppercase;
    for (unsigned char c : s) {
        if (std::isalnum(c) || c == '-' || c == '_' || c == '.' || c == '~') {
            out << static_cast<char>(c);
        } else {
            out << '%' << std::setw(2) << std::setfill('0') << static_cast<int>(c);
        }
    }
    return out.str();
}

// Append text to the GtkTextBuffer with an optional Pango markup tag.
static void append_text(GtkTextBuffer *buf, const std::string &text,
                        const char *tag = nullptr)
{
    GtkTextIter end;
    gtk_text_buffer_get_end_iter(buf, &end);
    if (tag)
        gtk_text_buffer_insert_with_tags_by_name(buf, &end, text.c_str(), -1, tag, nullptr);
    else
        gtk_text_buffer_insert(buf, &end, text.c_str(), -1);
}

// Build a human-readable result string from the JSON response.
static std::string format_result(const json &data)
{
    std::ostringstream out;

    for (const auto &entry : data) {
        // Word + phonetic
        std::string word = entry.value("word", "");
        out << "══════════════════════════════════════\n";
        out << "  WORD:  " << word << "\n";

        // Phonetics
        if (entry.contains("phonetics") && !entry["phonetics"].empty()) {
            for (const auto &ph : entry["phonetics"]) {
                std::string text = ph.value("text", "");
                if (!text.empty())
                    out << "  Pronunciation: " << text << "\n";
            }
        } else if (entry.contains("phonetic")) {
            std::string ph = entry.value("phonetic", "");
            if (!ph.empty())
                out << "  Pronunciation: " << ph << "\n";
        }
        out << "\n";

        // Meanings
        for (const auto &meaning : entry.value("meanings", json::array())) {
            std::string pos = meaning.value("partOfSpeech", "unknown");
            out << "  ▸ Part of speech: " << pos << "\n";
            out << "  ─────────────────────────────────\n";

            // Definitions
            int defIdx = 0;
            for (const auto &def : meaning.value("definitions", json::array())) {
                ++defIdx;
                std::string defText = def.value("definition", "");
                out << "    " << defIdx << ". " << defText << "\n";

                if (def.contains("example") && !def["example"].get<std::string>().empty())
                    out << "       Example: \"" << def.value("example", "") << "\"\n";
            }

            // Synonyms
            auto syns = meaning.value("synonyms", json::array());
            if (!syns.empty()) {
                out << "       Synonyms: ";
                for (size_t i = 0; i < syns.size(); ++i) {
                    if (i) out << ", ";
                    out << syns[i].get<std::string>();
                }
                out << "\n";
            }

            // Antonyms
            auto ants = meaning.value("antonyms", json::array());
            if (!ants.empty()) {
                out << "       Antonyms: ";
                for (size_t i = 0; i < ants.size(); ++i) {
                    if (i) out << ", ";
                    out << ants[i].get<std::string>();
                }
                out << "\n";
            }
            out << "\n";
        }
    }
    return out.str();
}

// ── Data passed to/from the worker thread ────────────────────────────────────
struct LookupData {
    std::string word;
    std::string lang_code;
    std::string result;   // filled by worker
    bool        success;
};

// Worker thread: perform the HTTP request (blocking).
static void lookup_worker(LookupData *ld)
{
    try {
        httplib::SSLClient cli("api.dictionaryapi.dev");
        cli.enable_server_certificate_verification(true);
        cli.set_connection_timeout(10);
        cli.set_read_timeout(15);

        std::string path = "/api/v2/entries/" + ld->lang_code + "/" + url_encode(ld->word);

        auto res = cli.Get(path.c_str());
        if (!res) {
            ld->result  = "Error: Could not connect to the dictionary API.\n"
                          "Please check your internet connection.";
            ld->success = false;
            return;
        }

        if (res->status == 404) {
            ld->result  = "No definitions found for \"" + ld->word + "\".\n"
                          "The word may not exist in this language or may be misspelled.";
            ld->success = false;
            return;
        }

        if (res->status != 200) {
            ld->result  = "Error: API returned HTTP " + std::to_string(res->status);
            ld->success = false;
            return;
        }

        auto j = json::parse(res->body);
        ld->result  = format_result(j);
        ld->success = true;
    } catch (const json::parse_error &pe) {
        ld->result  = std::string("Error: Failed to parse API response – ") + pe.what();
        ld->success = false;
    } catch (const std::exception &ex) {
        ld->result  = std::string("Error: ") + ex.what();
        ld->success = false;
    }
}

// ── GTK idle callback: update UI once the worker thread finishes ──────────────
static gboolean update_ui(gpointer user_data)
{
    auto *ld = static_cast<LookupData *>(user_data);

    gtk_spinner_stop(GTK_SPINNER(g_spinner));
    gtk_widget_set_sensitive(g_btn_lookup, TRUE);
    gtk_widget_set_sensitive(g_entry_word, TRUE);
    gtk_widget_set_sensitive(g_combo_lang, TRUE);

    GtkTextBuffer *buf = gtk_text_view_get_buffer(GTK_TEXT_VIEW(g_text_view));
    gtk_text_buffer_set_text(buf, "", -1);
    append_text(buf, ld->result, ld->success ? nullptr : "error");

    delete ld;
    return G_SOURCE_REMOVE; // run once only
}

// ── Button "Look Up" callback ─────────────────────────────────────────────────
static void on_lookup_clicked(GtkButton * /*btn*/, gpointer /*data*/)
{
    const gchar *word = gtk_entry_get_text(GTK_ENTRY(g_entry_word));
    if (!word || word[0] == '\0') {
        GtkTextBuffer *buf = gtk_text_view_get_buffer(GTK_TEXT_VIEW(g_text_view));
        gtk_text_buffer_set_text(buf, "Please enter a word to look up.", -1);
        return;
    }

    // Determine selected language code
    gint active = gtk_combo_box_get_active(GTK_COMBO_BOX(g_combo_lang));
    const char *lang_code = (active >= 0 && active < LANGUAGE_COUNT)
                            ? LANGUAGES[active].code : "en";

    // Disable UI while searching
    gtk_spinner_start(GTK_SPINNER(g_spinner));
    gtk_widget_set_sensitive(g_btn_lookup, FALSE);
    gtk_widget_set_sensitive(g_entry_word, FALSE);
    gtk_widget_set_sensitive(g_combo_lang, FALSE);

    GtkTextBuffer *buf = gtk_text_view_get_buffer(GTK_TEXT_VIEW(g_text_view));
    gtk_text_buffer_set_text(buf, "Looking up…", -1);

    // Launch worker thread so the GUI stays responsive
    auto *ld = new LookupData{ std::string(word), std::string(lang_code), "", false };
    std::thread([ld]() {
        lookup_worker(ld);
        // Only schedule the UI update if the application is still running.
        if (!g_app_quitting.load()) {
            g_idle_add(update_ui, ld);
        } else {
            delete ld;
        }
    }).detach();
}

// Allow pressing Enter in the word entry to trigger lookup
static void on_entry_activate(GtkEntry * /*entry*/, gpointer /*data*/)
{
    on_lookup_clicked(nullptr, nullptr);
}

// ── Window destroy callback – set quit flag before GTK tears down ─────────────
static void on_window_destroy(GtkWidget * /*widget*/, gpointer /*data*/)
{
    g_app_quitting.store(true);
}

// ── Application activate callback ────────────────────────────────────────────
static void activate(GtkApplication *app, gpointer /*data*/)
{
    // Main window
    GtkWidget *window = gtk_application_window_new(app);
    gtk_window_set_title(GTK_WINDOW(window), "Free Dictionary Lookup");
    gtk_window_set_default_size(GTK_WINDOW(window), 720, 600);
    gtk_container_set_border_width(GTK_CONTAINER(window), 12);
    g_signal_connect(window, "destroy", G_CALLBACK(on_window_destroy), nullptr);

    // Outer vertical box
    GtkWidget *vbox = gtk_box_new(GTK_ORIENTATION_VERTICAL, 8);
    gtk_container_add(GTK_CONTAINER(window), vbox);

    // ── Header row ────────────────────────────────────────────────────────────
    GtkWidget *header = gtk_box_new(GTK_ORIENTATION_HORIZONTAL, 6);
    gtk_box_pack_start(GTK_BOX(vbox), header, FALSE, FALSE, 0);

    // Word entry
    g_entry_word = gtk_entry_new();
    gtk_entry_set_placeholder_text(GTK_ENTRY(g_entry_word), "Enter a word…");
    gtk_widget_set_hexpand(g_entry_word, TRUE);
    gtk_box_pack_start(GTK_BOX(header), g_entry_word, TRUE, TRUE, 0);
    g_signal_connect(g_entry_word, "activate", G_CALLBACK(on_entry_activate), nullptr);

    // Language selector
    g_combo_lang = gtk_combo_box_text_new();
    for (int i = 0; i < LANGUAGE_COUNT; ++i)
        gtk_combo_box_text_append_text(GTK_COMBO_BOX_TEXT(g_combo_lang), LANGUAGES[i].label);
    gtk_combo_box_set_active(GTK_COMBO_BOX(g_combo_lang), 0); // default: English (US)
    gtk_box_pack_start(GTK_BOX(header), g_combo_lang, FALSE, FALSE, 0);

    // Look-up button
    g_btn_lookup = gtk_button_new_with_label("Look Up");
    gtk_style_context_add_class(gtk_widget_get_style_context(g_btn_lookup), "suggested-action");
    gtk_box_pack_start(GTK_BOX(header), g_btn_lookup, FALSE, FALSE, 0);
    g_signal_connect(g_btn_lookup, "clicked", G_CALLBACK(on_lookup_clicked), nullptr);

    // Spinner (busy indicator)
    g_spinner = gtk_spinner_new();
    gtk_box_pack_start(GTK_BOX(header), g_spinner, FALSE, FALSE, 0);

    // ── Separator ─────────────────────────────────────────────────────────────
    gtk_box_pack_start(GTK_BOX(vbox), gtk_separator_new(GTK_ORIENTATION_HORIZONTAL), FALSE, FALSE, 0);

    // ── Scrolled results view ─────────────────────────────────────────────────
    GtkWidget *scroll = gtk_scrolled_window_new(nullptr, nullptr);
    gtk_scrolled_window_set_policy(GTK_SCROLLED_WINDOW(scroll),
                                   GTK_POLICY_AUTOMATIC, GTK_POLICY_AUTOMATIC);
    gtk_widget_set_vexpand(scroll, TRUE);
    gtk_box_pack_start(GTK_BOX(vbox), scroll, TRUE, TRUE, 0);

    g_text_view = gtk_text_view_new();
    gtk_text_view_set_editable(GTK_TEXT_VIEW(g_text_view), FALSE);
    gtk_text_view_set_cursor_visible(GTK_TEXT_VIEW(g_text_view), FALSE);
    gtk_text_view_set_wrap_mode(GTK_TEXT_VIEW(g_text_view), GTK_WRAP_WORD_CHAR);
    gtk_text_view_set_left_margin(GTK_TEXT_VIEW(g_text_view), 8);
    gtk_text_view_set_right_margin(GTK_TEXT_VIEW(g_text_view), 8);
    gtk_text_view_set_top_margin(GTK_TEXT_VIEW(g_text_view), 6);
    gtk_container_add(GTK_CONTAINER(scroll), g_text_view);

    // ── Text tags for styling ─────────────────────────────────────────────────
    GtkTextBuffer *buf = gtk_text_view_get_buffer(GTK_TEXT_VIEW(g_text_view));
    gtk_text_buffer_create_tag(buf, "error",
                               "foreground", "#cc0000",
                               nullptr);

    // Initial placeholder message
    gtk_text_buffer_set_text(buf,
        "Enter a word above and press \"Look Up\" (or hit Enter).\n\n"
        "The app queries the Free Dictionary API "
        "(https://api.dictionaryapi.dev/) and displays:\n"
        "  • Pronunciation / phonetics\n"
        "  • Parts of speech\n"
        "  • Definitions\n"
        "  • Usage examples\n"
        "  • Synonyms and antonyms\n", -1);

    // ── Status bar ────────────────────────────────────────────────────────────
    GtkWidget *statusbar = gtk_label_new("Powered by the Free Dictionary API  •  "
                                         "https://api.dictionaryapi.dev/");
    gtk_widget_set_halign(statusbar, GTK_ALIGN_END);
    gtk_style_context_add_class(gtk_widget_get_style_context(statusbar), "dim-label");
    gtk_box_pack_start(GTK_BOX(vbox), statusbar, FALSE, FALSE, 0);

    gtk_widget_show_all(window);
    gtk_widget_hide(g_spinner); // hide spinner until needed
}

// ── main ──────────────────────────────────────────────────────────────────────
int main(int argc, char **argv)
{
    GtkApplication *app = gtk_application_new("dev.dictionaryapi.lookup",
                                               G_APPLICATION_DEFAULT_FLAGS);
    g_signal_connect(app, "activate", G_CALLBACK(activate), nullptr);
    int status = g_application_run(G_APPLICATION(app), argc, argv);
    g_object_unref(app);
    return status;
}
