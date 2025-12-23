#!/bin/bash

# Activate virtual environment and run the face recognition service
cd "$(dirname "$0")"

# Check if virtual environment exists
if [ ! -d ".venv" ]; then
    echo "Creating virtual environment..."
    python3 -m venv .venv
fi

# Activate virtual environment
source .venv/bin/activate

# Install dependencies if needed
echo "Checking dependencies..."
pip install -q -r requirements.txt

# Run the application
echo "Starting Face Recognition Service..."
python app.py
