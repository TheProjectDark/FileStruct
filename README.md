# FileStruct

![FileStruct](assets/FileStruct.png)

FileStruct is a lightweight JavaFX desktop app for scanning a folder, reviewing the files it contains, and organizing them into category-based subfolders. It helps keep messy directories tidy by grouping files into common types such as Images, Documents, Videos, Audio, Archives, Code, and Other.

## Features

- Choose any folder from your filesystem
- Scan the folder and list all regular files
- Detect file categories automatically
- View file name, type, size, and modification time in a table
- Move files into category folders inside the selected directory
- Avoid overwrites by creating unique names automatically when collisions occur
- Built with Java 21 and Maven

## How it works

1. Select a folder using the built-in folder chooser.
2. Click Scan to list the files in that directory.
3. Review the detected categories and file metadata.
4. Click Organize Files to move each file into a subfolder such as `Images/`, `Documents/`, `Code/`, and so on.

The app preserves the original filename unless a file with the same name already exists in the destination folder, in which case it appends a numbered suffix such as `name (1).png`.

## Requirements

- Java 21 or newer
- Maven 3.9 or newer

## Quick start

```bash
git clone https://github.com/TheProjectDark/FileStruct.git
cd FileStruct
mvn clean package
java -jar target/FileStruct.jar
```

You can also run the app directly through Maven:

```bash
mvn javafx:run
```

## Project structure

- `src/FileStruct.java` — main JavaFX application logic and UI
- `src/Launcher.java` — launcher entry point
- `src/main/resources/filestruct.css` — UI styling
- `assets/` — application branding assets
- `pom.xml` — Maven configuration and JavaFX dependencies

## License

This project is licensed under the GNU General Public License v3.0. See the `LICENSE` file for details.

## Contributing

Contributions are welcome. If you want to improve the application, open a pull request with a clear description of the change and validation steps.
