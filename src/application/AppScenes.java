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
 LOGIN("Login1.fxml"), READER("NewsReader.fxml"), 
 NEWS_DETAILS ("ArticleDetails1.fxml"),
 EDITOR("ArticleEdit1.fxml"), ADMIN("AdminNews.fxml"), FILE_PICKER("FilePicker.fxml") // Added my own file picker window layout
		 ,IMAGE_PICKER("ImagePicker.fxml"); 
 private String fxmlFile;
 
 private AppScenes (String file){
	 this.fxmlFile = file;
 }
 
 public String getFxmlFile()
 {
	 return this.fxmlFile;
 }
 
}
