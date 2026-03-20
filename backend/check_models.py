import requests
import json

api_key = "AIzaSyB1nf794lUj0_MkZXW0hJ7T0Hvm-ECIT_0"
url = f"https://generativelanguage.googleapis.com/v1beta/models?key={api_key}"

try:
    response = requests.get(url)
    if response.status_code == 200:
        models = response.json().get('models', [])
        print("Available Models:")
        for model in models:
            # Check for name and supported methods
            name = model.get('name', 'Unknown')
            methods = model.get('supportedGenerationMethods', [])
            print(f"- {name} (Supported: {methods})")
    else:
        print(f"Error {response.status_code}: {response.text}")
except Exception as e:
    print(f"Failed to connect: {e}")
