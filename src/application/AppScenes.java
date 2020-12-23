/**
 * 
 */
package application;

/**
 * Contain all app scenes
 * @author Ã�ngelLucas & AndreaB-EIT
 *
 */
public enum AppScenes {
 LOGIN("Login.fxml"), READER("NewsReader.fxml"), 
 NEWS_DETAILS ("ArticleDetails.fxml"),
 EDITOR("ArticleEdit.fxml"), ADMIN("AdminNews.fxml"), FILE_PICKER("FilePicker.fxml") // Added my own file picker window layout
		 ,IMAGE_PICKER("ImagePicker.fxml")
		 /*,IMAGE_PICKER("ImagePickerMaterailDesign.fxml")*/; 
 private String fxmlFile;
 
 private AppScenes (String file){
	 this.fxmlFile = file;
 }
 
 public String getFxmlFile()
 {
	 return this.fxmlFile;
 }
 
}
