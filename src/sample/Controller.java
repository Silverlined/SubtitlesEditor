package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.text.NumberFormat;
import java.util.regex.Pattern;


public class Controller {
    @FXML
    MenuButton encodingBtn;
    @FXML
    TextArea textEditor;
    @FXML
    Button timeIntervalButton;
    @FXML
    Button loadFileButton, openFileWithButton, exitButton, saveChangesButton;
    @FXML
    RadioButton speedUpButton, slowDownButton;
    @FXML
    CheckBox removeTagsBox;
    @FXML
    TextField fileLocation, milliSecondsField;
    @FXML
    Text nameOfLoadedFile;

    private File subtitlesFile;
    private String encoding = "Windows-1251";

    public void loadFile() {
        FileChooser fileChooser = new FileChooser();
        configureFileChooser(fileChooser, "Open Subtitle File");
        subtitlesFile = fileChooser.showOpenDialog(new Stage());
        if (subtitlesFile != null) {
            setTextFields();
            saveChangesButton.setDisable(false);
            openFileWithButton.setDisable(false);
            putSubtitlesInTheEditor();
            encodingBtn.setText("Windows-1251");
        }
    }

    private void putSubtitlesInTheEditor() {
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
                    new FileInputStream(subtitlesFile), encoding));
            String line;
            StringBuilder stringBuilder = new StringBuilder(8192);
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
            textEditor.setText(stringBuilder.toString());
            bufferedReader.close();
        } catch (UnsupportedEncodingException e) {
            MessageBox.display("Your system does not support the specified encoding");
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            MessageBox.display("File Not Found");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setTextFields() {
        nameOfLoadedFile.setText(subtitlesFile.getName());
        fileLocation.setText(subtitlesFile.getAbsolutePath());
    }

    private void configureFileChooser(final FileChooser fileChooser, String message) {
        fileChooser.setTitle(message);
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Subtitles", "*.srt", "*.sub"),
                new FileChooser.ExtensionFilter("Sub", "*.sub"),
                new FileChooser.ExtensionFilter("Srt", "*.srt")
        );
    }

    public void openWith() {
        try {
            Desktop desktop = Desktop.getDesktop();
            desktop.open(subtitlesFile);
        } catch (NullPointerException | FileNotFoundException e) {
            MessageBox.display("Open Subtitles File First");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void exit() {
        System.exit(0);
    }

    public void saveChanges() {
        BufferedWriter bufferedWriter = null;
        try {
            File chosenLocation = new File(fileLocation.getText());
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(chosenLocation), encoding));
            String subtitles = textEditor.getText();
            bufferedWriter.write(subtitles);
        } catch (UnsupportedEncodingException e) {
            MessageBox.display("Your system does not support the specified encoding");
            e.printStackTrace();
        } catch (NullPointerException | FileNotFoundException e) {
            MessageBox.display("File Not Found");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufferedWriter != null) {
                try {
                    bufferedWriter.flush();
                    bufferedWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void removeTags() {
        String subtitles = textEditor.getText();
        Pattern tagRegex = Pattern.compile("<[^>]*>");
        subtitles = subtitles.replaceAll(tagRegex.pattern(), "");
        textEditor.setText(subtitles);
    }

    public void changeTimeIntervals() {
        if (speedUpButton.selectedProperty().getValue() || slowDownButton.selectedProperty().getValue()) {
            File temporaryEditable = new File("sample.tmpSubtitles.srt");
            createTemporarySubtitles(temporaryEditable);
            BufferedReader bufferedReader = null;
            try {
                bufferedReader = new BufferedReader(new InputStreamReader(
                        new FileInputStream(temporaryEditable), encoding
                ));
                int timeInterval = Integer.parseInt(milliSecondsField.getText());
                String line;
                StringBuilder stringBuilder = new StringBuilder(8192);
                while ((line = bufferedReader.readLine()) != null) {
                    if (line.contains("-->")) {
                        long startingTimeMilliSec = 0;
                        long endingTimeMilliSec = 0;
                        String[] timeIntervals = line.split(" --> ");
                        String[] startingTimes = timeIntervals[0].split(":");
                        String[] endingTimes = timeIntervals[1].split(":");
                        startingTimes[2] = removeTheComma(startingTimes[2]);
                        endingTimes[2] = removeTheComma(endingTimes[2]);
                        for (int i = 0; i < 3; i++) {
                            switch (i) {
                                case 0:
                                    startingTimeMilliSec += Integer.parseInt(startingTimes[i]) * 3600000;
                                    endingTimeMilliSec += Integer.parseInt(endingTimes[i]) * 3600000;
                                    break;
                                case 1:
                                    startingTimeMilliSec += Integer.parseInt(startingTimes[i]) * 60000;
                                    endingTimeMilliSec += Integer.parseInt(endingTimes[i]) * 60000;
                                    break;
                                case 2:
                                    startingTimeMilliSec += Integer.parseInt(startingTimes[i]);
                                    endingTimeMilliSec += Integer.parseInt(endingTimes[i]);
                                    break;
                            }
                        }
                        long startingTimeEdited = 0;
                        long endingTimeEdited = 0;
                        if (speedUpButton.selectedProperty().getValue()) {
                            startingTimeEdited = startingTimeMilliSec - timeInterval;
                            endingTimeEdited = endingTimeMilliSec - timeInterval;
                            if (endingTimeEdited < 0) {
                                startingTimeEdited = 0;
                                endingTimeEdited = 0;
                            } else if (startingTimeEdited < 0) {
                                startingTimeEdited = 0;
                            }
                        } else if (slowDownButton.selectedProperty().getValue()) {
                            startingTimeEdited = startingTimeMilliSec + timeInterval;
                            endingTimeEdited = endingTimeMilliSec + timeInterval;
                        }
                        line = convertToHours(startingTimeEdited, endingTimeEdited);
                    }
                    stringBuilder.append(line).append("\n");
                }
                textEditor.setText(stringBuilder.toString());
            } catch (NumberFormatException e) {
                MessageBox.display("Change the milliseconds");
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                MessageBox.display("Your system does not support the specified encoding");
                e.printStackTrace();
            } catch (NullPointerException | FileNotFoundException e) {
                MessageBox.display("File Not Found");
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (bufferedReader != null) {
                        bufferedReader.close();
                    }
                    Files.delete(temporaryEditable.toPath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String convertToHours(long startingTimeEdited, long endingTimeEdited) {
        String line;
        int startingHours = (int) (startingTimeEdited / 3600000);
        int endingHours = (int) (endingTimeEdited / 3600000);
        startingTimeEdited = startingTimeEdited % 3600000;
        endingTimeEdited = endingTimeEdited % 3600000;
        int startingMinutes = (int) (startingTimeEdited / 60000);
        int endingMinutes = (int) (endingTimeEdited / 60000);
        startingTimeEdited = startingTimeEdited % 60000;
        endingTimeEdited = endingTimeEdited % 60000;
        int startingSeconds = (int) (startingTimeEdited / 1000);
        int endingSeconds = (int) (endingTimeEdited / 1000);
        int startingMilli = (int) startingTimeEdited % 1000;
        int endingMilli = (int) endingTimeEdited % 1000;

        line = String.format("%02d:%02d:%02d,%03d --> %02d:%02d:%02d,%03d",
                startingHours, startingMinutes, startingSeconds, startingMilli,
                endingHours, endingMinutes, endingSeconds, endingMilli);
        return line;
    }

    private void createTemporarySubtitles(File temporaryEditable) {
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(temporaryEditable), encoding
            ));
            bufferedWriter.write(textEditor.getText());
            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (UnsupportedEncodingException e) {
            MessageBox.display("Your system does not support the specified encoding");
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String removeTheComma(String str) {
        return str.replace(",", "");
    }

    public void activateSpeedUp() {
        slowDownButton.selectedProperty().setValue(false);
    }

    public void activateSlowDown() {
        speedUpButton.selectedProperty().setValue(false);
    }

    public void setTo1251() {
        encoding = "Windows-1251";
        reloadFile();
        encodingBtn.setText("Windows-1251");
    }

    private void reloadFile() {
        if (subtitlesFile != null) {
            BufferedReader bufferedReader = null;
            try {
                bufferedReader = new BufferedReader(new InputStreamReader(
                        new FileInputStream(subtitlesFile), encoding
                ));
                String line;
                StringBuilder stringBuilder = new StringBuilder(8192);
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                textEditor.setText(stringBuilder.toString());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void setTo1253() {
        encoding = "Windows-1253";
        reloadFile();
        encodingBtn.setText("UTF-8");
    }

    public void setTo1250() {
        encoding = "Windows-1250";
        reloadFile();
        encodingBtn.setText("Windows-1250");
    }
}
