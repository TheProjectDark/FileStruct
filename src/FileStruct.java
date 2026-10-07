import javafx.application.Application;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class FileStruct extends Application {
    private static final String ACCENT = "#5365D8";
    private static final String TEXT = "#202638";
    private static final String MUTED = "#788196";

    private static final Map<String, Set<String>> EXTENSIONS = Map.of(
            "Images", Set.of("png", "jpg", "jpeg", "gif", "webp", "svg", "bmp", "heic"),
            "Documents", Set.of("pdf", "doc", "docx", "txt", "rtf", "odt", "xls", "xlsx", "ppt", "pptx", "csv"),
            "Videos", Set.of("mp4", "mov", "mkv", "avi", "webm"),
            "Audio", Set.of("mp3", "wav", "flac", "aac", "m4a", "ogg"),
            "Archives", Set.of("zip", "rar", "7z", "tar", "gz", "bz2"),
            "Code", Set.of("java", "py", "js", "html", "css", "c", "cpp", "cs", "rb", "php", "ts"),
            "Other", Set.of()
    );
    private static final DateTimeFormatter MODIFIED_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());



    private final ObservableList<FileItem> files = FXCollections.observableArrayList();
    private final TableView<FileItem> fileTable = new TableView<>(files);
    private final TextField folderField = new TextField();
    private final Label statusLabel = new Label("Choose a folder to get started.");
    private final Button organizeButton = new Button("Organize Files");
    private Path selectedDirectory;

    @Override
    public void start(Stage stage) {
        stage.setTitle("FileStruct");
        stage.setMinWidth(720);
        stage.setMinHeight(480);

        Label mark = new Label("F");
        mark.setAlignment(Pos.CENTER);
        mark.setMinSize(34, 34);
        mark.setStyle("-fx-background-color: " + ACCENT + "; -fx-background-radius: 10;"
                + "-fx-text-fill: white; -fx-font-size: 17px; -fx-font-weight: bold;");

        Label brand = new Label("FileStruct");
        brand.setStyle("-fx-text-fill: " + TEXT + "; -fx-font-size: 16px; -fx-font-weight: bold;");
        HBox brandRow = new HBox(10, mark, brand);
        brandRow.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Your files");
        title.setStyle("-fx-text-fill: " + TEXT + "; -fx-font-size: 25px; -fx-font-weight: bold;");
        Label subtitle = new Label("Choose a folder to review and organize its files.");
        subtitle.setStyle("-fx-text-fill: " + MUTED + "; -fx-font-size: 12px;");
        VBox heading = new VBox(5, title, subtitle);

        folderField.setPromptText("Select a folder...");
        folderField.setEditable(false);
        folderField.setStyle("-fx-background-color: #F7F8FC; -fx-background-radius: 8;"
                + "-fx-border-color: #E5E8F0; -fx-border-radius: 8; -fx-padding: 10 12;"
                + "-fx-font-size: 12px;");
        HBox.setHgrow(folderField, Priority.ALWAYS);

        Button chooseButton = new Button("Choose Folder");
        chooseButton.setStyle(buttonStyle(false));
        chooseButton.setOnAction(event -> chooseDirectory(stage));

        Button scanButton = new Button("Scan");
        scanButton.setStyle(buttonStyle(true));
        scanButton.setOnAction(event -> scanDirectory());

        HBox folderRow = new HBox(8, folderField, chooseButton, scanButton);
        folderRow.setAlignment(Pos.CENTER_LEFT);
        folderRow.setPadding(new Insets(14));
        folderRow.setStyle(cardStyle());

        configureTable();

        organizeButton.setDisable(true);
        organizeButton.setStyle(buttonStyle(true));
        organizeButton.setOnAction(event -> organizeFiles(stage));

        HBox footer = new HBox(statusLabel, organizeButton);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setPadding(new Insets(2, 2, 0, 2));
        HBox.setHgrow(statusLabel, Priority.ALWAYS);
        statusLabel.setStyle("-fx-text-fill: " + MUTED + "; -fx-font-size: 12px;");

        VBox tableCard = new VBox(12, fileTable);
        tableCard.setPadding(new Insets(12));
        tableCard.setStyle(cardStyle());

        VBox content = new VBox(18, brandRow, heading, folderRow, tableCard, footer);
        content.setPadding(new Insets(26, 30, 24, 30));
        VBox.setVgrow(fileTable, Priority.ALWAYS);
        VBox.setVgrow(tableCard, Priority.ALWAYS);

        BorderPane root = new BorderPane(content);
        root.setStyle("-fx-background-color: #F5F6FA; -fx-font-family: 'Arial';");
        Scene scene = new Scene(root, 920, 620);
        scene.getStylesheets().add(FileStruct.class.getResource("/filestruct.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }

    private void configureTable() {
        TableColumn<FileItem, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().name()));
        nameColumn.setPrefWidth(380);

        TableColumn<FileItem, String> typeColumn = new TableColumn<>("Type");
        typeColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().category()));
        typeColumn.setPrefWidth(150);

        TableColumn<FileItem, String> sizeColumn = new TableColumn<>("Size");
        sizeColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().size()));
        sizeColumn.setPrefWidth(100);

        TableColumn<FileItem, String> modifiedColumn = new TableColumn<>("Modified");
        modifiedColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().modified()));
        modifiedColumn.setPrefWidth(160);

        fileTable.getColumns().setAll(nameColumn, typeColumn, sizeColumn, modifiedColumn);
        fileTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        fileTable.setPlaceholder(new Label("No files to display. Choose a folder and click Scan."));
        fileTable.getStyleClass().add("file-table");
    }

    private void chooseDirectory(Stage stage) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Choose a folder");
        if (selectedDirectory != null && Files.isDirectory(selectedDirectory)) {
            chooser.setInitialDirectory(selectedDirectory.toFile());
        }

        java.io.File directory = chooser.showDialog(stage);
        if (directory != null) {
            selectedDirectory = directory.toPath();
            folderField.setText(selectedDirectory.toString());
            files.clear();
            organizeButton.setDisable(true);
            statusLabel.setText("Folder selected. Click Scan to list its files.");
        }
    }

    private void scanDirectory() {
        if (selectedDirectory == null || !Files.isDirectory(selectedDirectory)) {
            showAlert(Alert.AlertType.INFORMATION, "No folder selected",
                    "Choose a folder before scanning.");
            return;
        }

        try (var paths = Files.list(selectedDirectory)) {
            List<FileItem> scanned = new ArrayList<>();
            for (Path path : paths.filter(Files::isRegularFile).toList()) {
                scanned.add(toFileItem(path));
            }
            scanned.sort(Comparator.comparing(FileItem::name, String.CASE_INSENSITIVE_ORDER));
            files.setAll(scanned);
            organizeButton.setDisable(files.isEmpty());
            statusLabel.setText(files.size() + (files.size() == 1 ? " file found." : " files found."));
        } catch (IOException exception) {
            showAlert(Alert.AlertType.ERROR, "Could not scan folder", exception.getMessage());
        }
    }

    private FileItem toFileItem(Path path) throws IOException {
        return new FileItem(path, path.getFileName().toString(), categoryFor(path),
                formatSize(Files.size(path)),
                MODIFIED_FORMAT.format(Files.getLastModifiedTime(path).toInstant()));
    }

    private void organizeFiles(Stage stage) {
        if (selectedDirectory == null || files.isEmpty()) {
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.initOwner(stage);
        confirmation.setTitle("Organize files");
        confirmation.setHeaderText("Move files into category folders?");
        confirmation.setContentText("Files will be moved into subfolders inside the selected folder.");
        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        int moved = 0;
        List<String> failures = new ArrayList<>();
        for (FileItem file : files) {
            Path targetDirectory = selectedDirectory.resolve(file.category());
            try {
                Files.createDirectories(targetDirectory);
                Files.move(file.path(), uniqueTarget(targetDirectory.resolve(file.name())));
                moved++;
            } catch (IOException exception) {
                failures.add(file.name() + ": " + exception.getMessage());
            }
        }

        String message = "Moved " + moved + " file(s).";
        if (!failures.isEmpty()) {
            message += "\nCould not move " + failures.size() + " file(s):\n"
                    + failures.stream().limit(5).collect(Collectors.joining("\n"));
        }
        showAlert(failures.isEmpty() ? Alert.AlertType.INFORMATION : Alert.AlertType.WARNING,
                "Organization complete", message);
        scanDirectory();
    }

    private Path uniqueTarget(Path target) {
        if (!Files.exists(target)) {
            return target;
        }

        String filename = target.getFileName().toString();
        int dot = filename.lastIndexOf('.');
        String stem = dot > 0 ? filename.substring(0, dot) : filename;
        String extension = dot > 0 ? filename.substring(dot) : "";
        for (int index = 1; ; index++) {
            Path candidate = target.resolveSibling(stem + " (" + index + ")" + extension);
            if (!Files.exists(candidate)) {
                return candidate;
            }
        }
    }

    private String categoryFor(Path path) {
        String filename = path.getFileName().toString();
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) {
            return "Other";
        }

        String extension = filename.substring(dot + 1).toLowerCase(Locale.ROOT);
        return EXTENSIONS.entrySet().stream()
                .filter(entry -> entry.getValue().contains(extension))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse("Other");
    }

    private String formatSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        if (bytes < 1024 * 1024) {
            return String.format(Locale.ROOT, "%.1f KB", bytes / 1024.0);
        }
        return String.format(Locale.ROOT, "%.1f MB", bytes / (1024.0 * 1024));
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String cardStyle() {
        return "-fx-background-color: white; -fx-background-radius: 12;"
                + "-fx-border-color: #E8EAF0; -fx-border-radius: 12;";
    }

    private String buttonStyle(boolean primary) {
        if (primary) {
            return "-fx-background-color: " + ACCENT + "; -fx-background-radius: 8;"
                    + "-fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 15;"
                    + "-fx-font-size: 12px; -fx-cursor: hand;";
        }
        return "-fx-background-color: white; -fx-background-radius: 8;"
                + "-fx-border-color: #E2E5ED; -fx-border-radius: 8; -fx-text-fill: " + TEXT
                + "; -fx-padding: 9 13; -fx-font-size: 12px; -fx-cursor: hand;";
    }

    public static void main(String[] args) {
        launch(args);
    }

    private record FileItem(Path path, String name, String category, String size, String modified) {
    }
}
