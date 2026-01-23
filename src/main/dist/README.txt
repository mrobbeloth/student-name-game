================================================================================
                         STUDENT NAME GAME - QUICK START
================================================================================

Welcome to the Student Name Game! This application helps you learn student 
names and faces through interactive game modes.

--------------------------------------------------------------------------------
                              GETTING STARTED
--------------------------------------------------------------------------------

1. FIRST LAUNCH
   - Double-click run.bat (Windows) or run.sh (macOS/Linux)
   - A welcome dialog will appear asking you to select your data directory
   - Choose a folder containing:
     * roster.xls or roster.xlsx - Excel file with student names (requires a 
       "Name" column with names in "Last, First" format)
     * Student photos - Image files named like "SmithJohn_12345.jpg" where
       the name prefix matches student names from the roster

2. GAME MODES
   - MATCHING: Drag photos to matching names (requires 2+ students)
   - MULTIPLE CHOICE: Select the correct name from 4 options (requires 4+ students)
   - FILL IN THE BLANK: Type the student's name (requires 1+ student)

3. KEYBOARD SHORTCUTS
   - Press 1-4 in Multiple Choice mode to select answers quickly
   - Press Enter in Fill in the Blank mode to submit your answer
   - Press R to reload images when you add new photos

--------------------------------------------------------------------------------
                              FILE STRUCTURE
--------------------------------------------------------------------------------

Your data directory should look like this:

    MyClass/
    ├── roster.xlsx          <- Student roster (Excel format)
    └── photos/              <- Student photos (optional subfolder)
        ├── SmithJohn_12345.jpg
        ├── DoeJane_67890.png
        └── ...

Photo filename format: The app extracts the name part before the underscore
and matches it against squashed student names (spaces and commas removed).

--------------------------------------------------------------------------------
                              PORTABLE MODE
--------------------------------------------------------------------------------

This portable version stores all settings and data in the application folder:
- config.ini - Your settings (data directory location, etc.)
- mappings.json - Manual name assignments for unmatched photos
- statistics.json - Your game progress and scores

You can copy this entire folder to a USB drive and run it on any computer
with the same operating system.

--------------------------------------------------------------------------------
                              TROUBLESHOOTING
--------------------------------------------------------------------------------

Q: No students are appearing
A: Check that your roster file has a "Name" column and your photos follow
   the naming convention (LastFirst_ID.jpg)

Q: Photos aren't matching to names
A: Use Settings → Manage Unmatched Images to manually assign photos to names

Q: The app won't start
A: Ensure you have the correct version for your operating system:
   - Windows: student-name-game-windows.zip
   - macOS: student-name-game-macos.zip
   - Linux: student-name-game-linux.zip

Q: Multiple Choice mode is disabled
A: You need at least 4 matched students to play Multiple Choice mode

--------------------------------------------------------------------------------
                              BACKUP & RESTORE
--------------------------------------------------------------------------------

To backup your data:
  File → Export Data → Choose a location for the .zip file

To restore from backup:
  File → Import Data → Select your backup .zip file

--------------------------------------------------------------------------------

For more help, see the Help menu in the application.

Enjoy learning your students' names!
================================================================================
