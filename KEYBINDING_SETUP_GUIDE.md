# IntelliJ IDEA Keybindings Setup Guide for Cursor

This guide will help you configure the IntelliJ IDEA keybindings in Cursor.

## Method 1: Copy to Cursor Settings (Recommended - Automatic)

1. **Open Cursor Settings:**
   - Press `Cmd+Shift+P` to open the Command Palette
   - Type "Preferences: Open Keyboard Shortcuts (JSON)"
   - Press Enter

2. **Copy the Keybindings:**
   - Open the file `cursor-intellij-keybindings.json` from your project
   - Select all (`Cmd+A`) and copy (`Cmd+C`)
   - Paste into the keybindings JSON file that opened
   - Save the file (`Cmd+S`)

3. **Restart Cursor** (optional but recommended)

## Method 2: Import via Settings UI

1. **Open Cursor Settings:**
   - Press `Cmd+,` to open Settings
   - Click on "Keyboard Shortcuts" in the left sidebar

2. **Import Keybindings:**
   - Click the small file icon in the top-right corner (looks like a document)
   - Select "Open Keyboard Shortcuts (JSON)"
   - Copy the contents of `cursor-intellij-keybindings.json` and paste it there
   - Save

## Quick Verification

After setup, test these common shortcuts to verify it's working:

- `Cmd+Shift+A` - Should open Command Palette
- `Cmd+O` - Should open "Go to Symbol in Editor"
- `Cmd+E` - Should open recent files
- `Alt+Enter` - Should show quick fixes (in a Java file)
- `Cmd+Shift+O` - Should show all symbols in workspace

## Troubleshooting

### Keybindings Not Working?

1. **Check for Conflicts:**
   - Open Keyboard Shortcuts (`Cmd+K Cmd+S`)
   - Search for the shortcut you're trying to use
   - Look for conflicting bindings

2. **Verify Java Extension:**
   - Make sure you have the Java extension pack installed
   - Some shortcuts require the Java extension to work

3. **Check "when" Conditions:**
   - Some shortcuts only work in specific contexts (e.g., `editorTextFocus`, `editorLangId == 'java'`)
   - Make sure you're in the correct context when testing

### Some Java-Specific Features Missing?

Install required extensions:
- **Extension Pack for Java** (by Microsoft)
- **Language Support for Java(TM) by Red Hat**

## What's Included

The keybindings include 100+ shortcuts organized into:

✅ **Navigation** - Go to class, symbol, recent files, etc.
✅ **Editing** - Duplicate, delete, move lines, comment, etc.
✅ **Code Generation** - Generate code, auto-complete, quick fixes
✅ **Refactoring** - Rename, extract method/variable, inline, etc.
✅ **Running & Debugging** - Run, debug, breakpoints, stepping
✅ **Search & Replace** - Find, replace, find in files
✅ **VCS/Git** - Commit, history, revert
✅ **Code Folding** - Fold/unfold regions
✅ **Tool Windows** - Explorer, search, debug views
✅ **Build & Run** - Build tasks

## Most Useful Shortcuts

| Shortcut | Action |
|----------|--------|
| `Cmd+Shift+A` | Command Palette (Find Action) |
| `Cmd+O` | Go to Symbol in File |
| `Cmd+Shift+O` | Go to Symbol in Workspace |
| `Cmd+E` | Recent Files |
| `Cmd+B` | Go to Definition |
| `Cmd+Alt+B` | Go to Implementation |
| `Alt+Enter` | Quick Fix / Show Suggestions |
| `Shift+F6` | Rename |
| `Cmd+Alt+L` | Format Code |
| `Cmd+Alt+O` | Organize Imports |
| `Cmd+D` | Duplicate Line/Selection |
| `Cmd+Y` | Delete Line |
| `Cmd+/` | Toggle Line Comment |
| `Cmd+Shift+R` | Run |
| `Cmd+Shift+T` | Navigate to Test |
| `Alt+F7` | Find Usages |
| `F8` | Step Over (Debug) |
| `F7` | Step Into (Debug) |
| `Cmd+F8` | Toggle Breakpoint |

## Additional Notes

- All shortcuts use `Cmd` instead of `Ctrl` for macOS
- Some refactoring shortcuts work best with Java files open
- Debug shortcuts (`F7`, `F8`, etc.) only work when debugging
- Some advanced features may require additional VS Code/Cursor extensions

---

**Need help?** If a specific shortcut isn't working, check:
1. The file type (some shortcuts are Java-specific)
2. The cursor position (some require text focus)
3. Extension requirements (install Java extension pack)
