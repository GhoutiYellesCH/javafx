javac --module-path "D:\code\java\javafx-sdk-25.0.1\lib" --add-modules javafx.controls,javafx.fxml -d out "tp\src\main\java\com\example\*.java"
java --module-path "D:\code\java\javafx-sdk-25.0.1\lib" --add-modules javafx.controls,javafx.fxml -cp "out;tp\src\main\resources" com.example.App

D:\code\java\JavaFX\tp
 ├─ src
 │   └─ main
 │       └─ java
 │           └─ com
 │               └─ example
 │                   ├─ App.java
 │                   └─ PrimaryController.java
 └─ src
     └─ main
         └─ resources
             └─ com
                 └─ example
                     └─ primary.fxml


javac --module-path "D:\code\java\javafx-sdk-25.0.1\lib" --add-modules javafx.controls,javafx.fxml,javafx.media -d out "tp\src\main\java\com\example\*.java"

java --module-path "D:\code\java\javafx-sdk-25.0.1\lib" --add-modules javafx.controls,javafx.fxml -cp "out;tp\src\main\resources;tp\resources" com.example.App