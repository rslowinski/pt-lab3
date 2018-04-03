package sample;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ProgressBarTableCell;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

public class Controller implements Initializable {
    @FXML
    Button selectFilesButton;
    @FXML
    Button destinationButton;
    @FXML
    TableColumn<ImageProcessingJob, String> imageColumn;
    @FXML
    TableColumn<ImageProcessingJob, Double> progressColumn;
    @FXML
    TableColumn<ImageProcessingJob, String> statusColumn;
    @FXML
    TableView workspaceTableView;
    @FXML
    Slider threadAmount;
    private ObservableList<ImageProcessingJob> processingJobs;
    private File destination;

    @FXML
    public void selectFilesHandler(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JPG images", "*.jpg"));
        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(null);

        if (selectedFiles == null) return;

        processingJobs.clear();
        selectedFiles.forEach(t -> processingJobs.add(new ImageProcessingJob(t)));
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        imageColumn.setCellValueFactory( //nazwa pliku
                p -> new SimpleStringProperty(p.getValue().getFile().getName()));
        statusColumn.setCellValueFactory( //status przetwarzania
                p -> p.getValue().messageProperty());
        progressColumn.setCellFactory( //wykorzystanie paska postępu
                ProgressBarTableCell.<ImageProcessingJob>forTableColumn());
        progressColumn.setCellValueFactory( //postęp przetwarzania
                p -> p.getValue().progressProperty().asObject());

        threadAmount.valueProperty().addListener((x, y, newValue) -> threadAmount.setValue(newValue.intValue()));

        processingJobs = FXCollections.observableArrayList(new ArrayList<>());
        workspaceTableView.setItems(processingJobs);
    }

    @FXML
    public void selectDestinationHandler( ) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        destination = directoryChooser.showDialog(null);
    }

    @FXML
    public void startProcessingImages( ) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        processingJobs.forEach(t -> {
            t.destination = destination;
            executorService.submit(t);
        });
    }

    @FXML
    public void startProcessingMultiThread() {
        ForkJoinPool pool = new ForkJoinPool((int)threadAmount.getValue());
        processingJobs.forEach(t -> {
            t.destination = destination;
            pool.submit(t);
        });
    }

    public void doTest(ActionEvent actionEvent) {
        long start = System.currentTimeMillis(); //zwraca aktualny czas [ms]
        startProcessingMultiThread();
        long end = System.currentTimeMillis(); //czas po zakończeniu operacji [ms]
        long durationOnMulti = end - start; //czas przetwarzania [ms]

        start = System.currentTimeMillis(); //zwraca aktualny czas [ms]
        startProcessingImages();
        end = System.currentTimeMillis(); //czas po zakończeniu operacji [ms]
        long durationOnSequential = end - start; //czas przetwarzania [ms]
        System.out.println("sequential processing: " + durationOnSequential + "ms");
        System.out.println("parallel processing with "+ (int)threadAmount.getValue()
                + ". threads, time: "
                + durationOnMulti + "ms");


    }
}
