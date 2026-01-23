# Sound Files

This directory should contain sound effect files for the game:

- `correct.wav` - Played when the user answers correctly
- `incorrect.wav` - Played when the user answers incorrectly  
- `streak.wav` - Played when the user achieves a streak bonus
- `complete.wav` - Played when a game session is completed

## Obtaining Sound Files

You can obtain free sound effects from:

1. **Freesound.org** - https://freesound.org/
   - Search for "correct", "success", "error", "achievement"
   - Many sounds are CC0 (public domain)

2. **Mixkit** - https://mixkit.co/free-sound-effects/
   - Free sound effects for personal and commercial use

3. **OpenGameArt** - https://opengameart.org/
   - Game-specific sounds, many CC0 licensed

## Recommended Sounds

For the best user experience, choose short sounds (< 1 second) that are:
- **correct.wav**: A pleasant chime or ding
- **incorrect.wav**: A soft buzz or thud (not harsh)
- **streak.wav**: A rewarding fanfare or combo sound
- **complete.wav**: A triumphant finish sound

## Audio Format

Supported formats:
- WAV (recommended for best compatibility)
- MP3
- AIFF

Sample rate: 44100 Hz is standard
Channels: Mono or Stereo

## File Naming

The application expects these exact filenames:
- `correct.wav`
- `incorrect.wav`
- `streak.wav`  
- `complete.wav`

If files are not present, the game will run silently without sound effects.
