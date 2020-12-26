package application;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Screen;

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
    void onFileDialog(ActionEvent event) throws IOException {
    	FileChooser fileChooser = new FileChooser();
    	fileChooser.setTitle("Open Resource File");
    	fileChooser.getExtensionFilters().addAll(
    				new ExtensionFilter("News Files", "*.news"));
    	Window parentStage = ((Node) event.getSource()).getScene().getWindow();
    	File selectedFile = fileChooser.showOpenDialog(parentStage);
    	
    	//Getting the URI for the local file
    	if (selectedFile != null) {
    		Path filepath = FileSystems.getDefault().getPath(
    			 selectedFile.getCanonicalPath());
    		
    		String path = filepath.toUri().toString();
    		
    		// This part of the code handles the width of the file picker, making it shorter or longer according to the title length
    		double length = path.length() * 10;
    		Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
    		FXMLLoader loader = new FXMLLoader(getClass().getResource(
    				AppScenes.FILE_PICKER.getFxmlFile()));
			Pane root = loader.load();
    		double originalWidth = root.getPrefWidth();
    		double originalX = (Screen.getPrimary().getBounds().getWidth()/2) - (originalWidth/2);
    		if(length > originalWidth) {
    			// Handles most of the paths
    			stage.setWidth(length);
    			stage.setX(originalX - (length-originalWidth)/2);
    		} else {
    			// Handles very small paths
    			stage.setWidth(originalWidth);
    			stage.setX(originalX);
    		}
    		
    		this.path.setText(path);
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