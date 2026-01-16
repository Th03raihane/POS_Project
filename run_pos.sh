#!/bin/bash

# Move to the project directory
cd ~/POS_Project

# 1. Start MySQL service if it's not running
echo "Checking Database..."
sudo systemctl start mysql

# 2. Clean and Prepare folders
echo "Preparing folders..."
rm -rf bin/*
mkdir -p bin

# 3. Compile the project
echo "Compiling Java code..."
# Note: On utilise "lib/*" pour inclure iText et FlatLaf
javac -d bin -cp "lib/*:src" src/com/pos/main/MainApp.java src/com/pos/view/*.java src/com/pos/connection/*.java

# 4. Check if compilation was successful
if [ $? -eq 0 ]; then
    echo "Success! Launching POS System..."
    # 5. Run the application
    # Note: On inclut le dossier bin et toutes les libs
    java -cp "bin:lib/*" com.pos.main.MainApp
else
    echo "Error: Compilation failed. Please check your code."
fi
