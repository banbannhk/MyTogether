import json
import os

path = os.path.expanduser("~/Library/Application Support/Cursor/User/keybindings.json")
print(f"Reading from {path}")

try:
    with open(path, 'r') as f:
        data = json.load(f)
    
    initial_count = len(data)
    print(f"Initial count: {initial_count}")

    # Remove the bad item
    # Bad item: key="cmd+alt+g", command="editor.action.revealDefinition"
    new_data = [item for item in data if not (item.get("key") == "cmd+alt+g" and item.get("command") == "editor.action.revealDefinition")]
    
    final_count = len(new_data)
    print(f"Final count: {final_count}")
    
    if initial_count != final_count:
        with open(path, 'w') as f:
            json.dump(new_data, f, indent=4)
        print("Successfully updated keybindings.json")
    else:
        print("No changes needed.")

except Exception as e:
    print(f"Error: {e}")
