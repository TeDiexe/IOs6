package com.album;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.Date;

public class Main extends Application implements Observer {

    private Album myAlbum;
    private ListView<String> photoListView;
    private Label statusLabel;

    @Override
    public void start(Stage primaryStage) {
        myAlbum = new Album("Moje Wakacje");

        myAlbum.attach(this);

        BorderPane root = new BorderPane();
        photoListView = new ListView<>();
        statusLabel = new Label("Stan: Gotowy (Album pusty)");
        statusLabel.setStyle("-fx-padding: 10; -fx-text-fill: grey;");

        Button btnAdd = new Button("Dodaj Zdjęcie");
        Button btnRemove = new Button("Usuń Ostatnie");

        btnAdd.setOnAction(e -> {
            String photoName = "IMG_" + System.currentTimeMillis() + ".jpg";
            RealPhoto newPhoto = new RealPhoto("id", photoName, "jpg", 1920, 1080, new Date());
            myAlbum.addElement(newPhoto);
        });

        btnRemove.setOnAction(e -> {
            if (!myAlbum.getElements().isEmpty()) {
                AlbumElement last = myAlbum.getElements().get(myAlbum.getElements().size() - 1);
                myAlbum.removeElement(last);
            }
        });

        Button btnExport = new Button("eksportuj");
        btnExport.setStyle("-fx-font-weight: bold; -fx-base: #b6e7c9;");

        btnExport.setOnAction(e -> {
            XmlExportVisitor visitor = new XmlExportVisitor();
            myAlbum.accept(visitor);
            String xmlResult = visitor.getXml();

            System.out.println("\nVISITOR");
            System.out.println(xmlResult);
            System.out.println("\n");

            statusLabel.setText("wyeksportowano");
        });

        VBox controls = new VBox(10,
                btnAdd,
                btnRemove,
                new Separator(),
                btnExport,
                statusLabel
        );
        controls.setStyle("-fx-padding: 10; -fx-background-color: #f0f0f0;");

        root.setCenter(photoListView);
        root.setRight(controls);

        Scene scene = new Scene(root, 500, 350);
        primaryStage.setTitle("Album - Testowanie Wzorców (Observer + Visitor)");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void update(String action, AlbumElement element) {
        Platform.runLater(() -> {
            refreshList();
            statusLabel.setText("Ostatnia akcja: " + action + " -> " + element.getName());
            System.out.println("Observer debrał powiadomienie: " + action + " " + element.getName());
        });
    }

    private void refreshList() {
        photoListView.getItems().clear();
        for (AlbumElement element : myAlbum.getElements()) {
            photoListView.getItems().add(element.getName());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}