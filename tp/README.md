# TP - Super Mario JavaFX

This project is a JavaFX mini-game built for the Interface Homme Machine course.

Prerequisites
- JDK 21 installed (or use Gradle toolchain to download)
- Gradle (optional: `gradlew` will be generated if you run `gradle wrapper`)

Build & Run

Using Gradle:

```powershell
# From project root d:\code\java\JavaFX\tp
gradle run
```

Or compile with local JDK and JavaFX SDK (example):

```powershell
$env:JAVA_HOME = 'C:\Program Files\Eclipse Adoptium\jdk-21'
$env:Path = "$env:JAVA_HOME\bin;" + $env:Path
javac --release 21 --module-path "D:\code\java\javafx-sdk-25.0.1\lib" --add-modules javafx.controls,javafx.fxml -d out "tp\src\main\java\com\example\*.java"
java --module-path "D:\code\java\javafx-sdk-25.0.1\lib" --add-modules javafx.controls,javafx.fxml -cp "out;tp\src\main\resources" com.example.App
```
