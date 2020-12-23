package application;

import java.io.File;

import java.nio.file.FileSystems;
import java.nio.file.Path;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.FileChooser.ExtensionFilter;

public class FilePickerController {

    @FXML
    private Label path;

    @FXML
    void onAccept(ActionEvent event) {
    	Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();    		
		stage.close();
    }
    
    // This function closes the picker without saving any path
    @FXML
    void onCancel(ActionEvent event) {
    	path.setText("");
    	Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
		stage.close();
    }
    
    // This function shows the file chooser and filters for only .news elements
    @FXML
    void onFileDialog(ActionEvent event) {
    	FileChooser fileChooser = new FileChooser();
    	 fileChooser.setTitle("Open Resource File");
    	 fileChooser.getExtensionFilters().addAll(
    	         new ExtensionFilter("News Files", "*.news"));
    	 Window parentStage = ((Node) event.getSource()).getScene().getWindow();
    	 File selectedFile = fileChooser.showOpenDialog(parentStage);
    	 
    	 //Getting the URI for the local file
    	 if (selectedFile != null) {
    		 Path filepath = FileSystems.getDefault().getPath(
    				 selectedFile.getAbsolutePath());
    		 this.path.setText(filepath.toUri().toString());
    	 }
    }

    void initialize() {
        assert path != null : "fx:id=\"path\" was not injected: check your FXML file 'ImagePicker.fxml'.";

    }
    
    // This is called from the NewsReaderController to get the selected file's path
    String getFilePath(){
    	return path.getText();
    }
}