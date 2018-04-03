package sample;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

class ImageProcessingJob extends Task<Void> {
    SimpleStringProperty status;
    DoubleProperty progress;
    File source;
    File destination;

    ImageProcessingJob(File source) {
        this.source = source;
        updateMessage("waiting");
    }

    public File getFile() {
        return source;
    }


    @Override
    protected Void call() {
        convertToGrayscale(source, destination, progress);
        return null;
    }

    public void convertToGrayscale(
            File originalFile, //oryginalny plik graficzny
            File outputDir, //katalog docelowy
            DoubleProperty progressProp//własność określająca postęp operacji
    ) {
        updateProgress(0, 1);
        long start = System.currentTimeMillis(); //zwraca aktualny czas [ms]
        try {
            updateMessage("processing");
            //wczytanie oryginalnego pliku do pamięci
            BufferedImage original = ImageIO.read(originalFile);

            //przygotowanie bufora na grafikę w skali szarości
            BufferedImage grayscale = new BufferedImage(
                    original.getWidth(), original.getHeight(), original.getType());
            //przetwarzanie piksel po pikselu
            for (int i = 0; i < original.getWidth(); i++) {
                for (int j = 0; j < original.getHeight(); j++) {
                    //pobranie składowych RGB
                    int red = new Color(original.getRGB(i, j)).getRed();
                    int green = new Color(original.getRGB(i, j)).getGreen();
                    int blue = new Color(original.getRGB(i, j)).getBlue();
                    //obliczenie jasności piksela dla obrazu w skali szarości
                    int luminosity = (int) (0.21 * red + 0.71 * green + 0.07 * blue);
                    //przygotowanie wartości koloru w oparciu o obliczoną jaskość
                    int newPixel =
                            new Color(luminosity, luminosity, luminosity).getRGB();
                    //zapisanie nowego piksela w buforze
                    grayscale.setRGB(i, j, newPixel);
                }
                //obliczenie postępu przetwarzania jako liczby z przedziału [0, 1]
                double progress = (1.0 + i) / original.getWidth();
                //aktualizacja własności zbindowanej z paskiem postępu w tabeli
                // Platform.runLater(() -> progressProp.set(progress));
                Platform.runLater(() -> updateProgress(progress, 1));
            }
            //przygotowanie ścieżki wskazującej na plik wynikowy
            Path outputPath =
                    Paths.get(outputDir.getAbsolutePath(), "gray" + originalFile.getName());

            //zapisanie zawartości bufora do pliku na dysku
            ImageIO.write(grayscale, "jpg", outputPath.toFile());
        } catch (IOException ex) {
            //translacja wyjątku
            throw new RuntimeException(ex);
        }
        long end = System.currentTimeMillis(); //czas po zakończeniu operacji [ms]
        long duration = end - start; //czas przetwarzania [ms]
        updateMessage("completed in " + (float) duration / 1000f + "s");
    }
}
