/**
 * 
 */
package application;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.function.Predicate;

import javax.json.JsonObject;

import application.news.Article;
import application.news.Categories;
import application.news.User;
import application.utils.JsonArticle;
import application.utils.exceptions.ErrorMalFormedArticle;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.Callback;
import serverConection.ConnectionManager;
import serverConection.exceptions.AuthenticationError;

/**
 * @author Ã�ngelLucas & AndreaB-EIT
 *
 */
public class NewsReaderController {
	
	@FXML
	private ListView<Article> articlesList;
	
	@FXML
	private WebView webView;
	
	@FXML
	private ImageView imageView;
	
	@FXML
	private AnchorPane anchorPane;
	
	@FXML
	private MenuItem choiceNew;
	
	@FXML
	private MenuItem choiceEdit;
	
	@FXML
	private MenuItem choiceDelete;
	
	@FXML
	private MenuItem choiceLogin;
	
	@FXML
	private ComboBox<Categories> categoriesList;
	
	@FXML
	private Button btn_moreDetails;
	
	private FilteredList<Article> filteredItems;
	
	private NewsReaderModel newsReaderModel = new NewsReaderModel();
	private User usr;
	
	private Article selected;

	public NewsReaderController() {
		 newsReaderModel.setDummyData(false);
		
	}
	
	private double getLongestTitleSize(ObservableList<Article> list) {
		double longest = 0;
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getTitle().length() > longest) {
				longest = list.get(i).getTitle().length();
			}
		}
		//return Math.min(longest*8, 512);
		return longest*8;
	}

	private void getData() {
		//TODO retrieve data and update UI
		ObservableList<Article> list = this.newsReaderModel.getArticles();
		
		if (this.usr != null) this.newsReaderModel.retrieveData(); // CHANGE THIS AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAH
		//this.newsReaderModel.retrieveData();
		
		this.articlesList.setMinWidth(this.getLongestTitleSize(list));
		this.articlesList.setItems(list);
		
		// Manage logged in or not
		if (!this.newsReaderModel.getConnectionManager().isLoggedOK()) {
			this.choiceDelete.setVisible(false);
			this.choiceEdit.setVisible(false);
			this.choiceNew.setText("New (local files only)");
			this.webView.getEngine().loadContent("");
			this.imageView.setVisible(false);
			this.btn_moreDetails.setDisable(true);
			this.choiceDelete.setDisable(true);
			this.choiceEdit.setDisable(true);
			this.selected = null;
		} else {
			this.selected = null;
			this.btn_moreDetails.setDisable(true);
			this.choiceDelete.setDisable(true);
			this.choiceEdit.setDisable(true);
			this.imageView.setVisible(false);
			this.choiceDelete.setVisible(true);
			this.choiceEdit.setVisible(true);
			this.choiceEdit.setText("Edit selected");
			this.choiceNew.setText("New article");
			this.choiceLogin.setText("Logged in!");
			this.choiceLogin.setDisable(true);
			Alert alert = new Alert(AlertType.INFORMATION, "You are logged in", ButtonType.OK);
			alert.setTitle("Logged in");
			alert.show();
		}
	}

	/**
	 * @return the usr
	 */
	User getUsr() {
		return usr;
	}

	void setConnectionManager (ConnectionManager connection) {
		this.newsReaderModel.setConnectionManager(connection);
		this.getData();
	}
	
	/**
	 * @param usr the usr to set
	 */
	void setUsr(User usr) {
		
		this.usr = usr;
		//Reload articles
		this.getData();
	}
	
	void manageAction(String action) {
		Scene parentScene = ((Stage) this.anchorPane.getScene().getWindow()).getScene();
    	
    	FXMLLoader loader = new FXMLLoader(getClass().getResource(
				AppScenes.EDITOR.getFxmlFile()));
    	
    	Alert alert = new Alert(AlertType.ERROR, "Generic error");
		alert.setTitle("Error");
    	
    	try {
			Pane root = loader.load();
			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			
			// Create the second stage
			Window parentWindow = parentScene.getWindow();
			Stage stage = new Stage();
			stage.initOwner(parentWindow);
			stage.initModality(Modality.APPLICATION_MODAL);
			stage.setScene(scene);

			// // Get the selected model
			ArticleEditController controller = loader.<ArticleEditController>getController();
			controller.setConnectionManager(this.newsReaderModel.getConnectionManager());
			controller.sendCategories(this.newsReaderModel.getCategories());
			
			// To clear and undo changes when the user closes the window
			stage.setOnCloseRequest(ev -> controller.exitForm());
			
			switch (action) {
			case "new":
				controller.setArticle(null);
				break;
			case "newlogged":
				// Pass the data
				controller.setUsr(this.getUsr());
				controller.setArticle(null);
				alert = new Alert(AlertType.INFORMATION, "Article created successfully!");
				alert.setTitle("Article created");
				alert.showAndWait();
				break;
			case "edit":
				controller.setArticle(this.selected);
				break;
			case "editlogged":
				controller.setUsr(this.getUsr());
				// ID checker?
				controller.setArticle(this.selected);
				break;
			}
			
			// Open the new stage and wait for user response
			stage.setX(parentScene.getX());
			stage.setY(parentScene.getY());
			stage.setTitle("Article editor");
			stage.showAndWait();
			
			if (controller.isChanged() && action.equals("editlogged")) {
				Article edited = controller.getArticle();
				int index = this.articlesList.getItems().indexOf(this.selected);
				this.articlesList.getItems().add(index, edited);
				this.articlesList.getItems().remove(index+1);
				this.articlesList.refresh();
				this.selected = edited;
				this.webView.getEngine().loadContent(edited.getAbstractText());
				if (edited.getImageData() != null) {
					NewsReaderController.this.imageView.setImage(edited.getImageData());
				} else {
					NewsReaderController.this.imageView.setImage(new Image("images/noImage.jpg"));
				}
    		}
			
			if (!(this.newsReaderModel.getCategories().get(0).toString().equals("All"))) {
				this.newsReaderModel.getCategories().add(0, Categories.ALL);
				this.categoriesList.getSelectionModel().select(Categories.ALL);
			}
		} catch (IOException e) {
			alert = new Alert(AlertType.ERROR, "There was an error with the article, sorry");
			alert.setTitle("Error with the article");
			alert.show();
			e.printStackTrace();
		}
	}
	
	@FXML
	void onMoreDetails(ActionEvent event) {
		if(this.selected == null) {
			Alert alert = new Alert(AlertType.ERROR, "No selected article");
			alert.setTitle("No article");
			alert.show();
			return;
		}
		Scene parentScene = ((Stage) this.anchorPane.getScene().getWindow()).getScene();
    	
    	FXMLLoader loader = new FXMLLoader(getClass().getResource(
				AppScenes.NEWS_DETAILS.getFxmlFile()));
    	
    	try {
			Pane root = loader.load();
			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			
			// Create the second stage
			Window parentWindow = parentScene.getWindow();
			Stage stage = new Stage();
			stage.initOwner(parentWindow);
			stage.initModality(Modality.NONE);
			stage.setScene(scene);

			// // Get the selected model
			ArticleDetailsController controller = loader.<ArticleDetailsController>getController();

			controller.setUsr(this.newsReaderModel.getConnectionManager().isLoggedOK() ? this.getUsr() : null);
			
			controller.setArticle(this.selected);

			stage.setX(parentScene.getX());
			stage.setY(parentScene.getY());
			stage.setTitle("Details - " + this.selected.getTitle());
			stage.show();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@FXML
	void onNew() {
		if (!this.newsReaderModel.getConnectionManager().isLoggedOK()) {
			this.manageAction("new");
		} else {
			this.manageAction("newlogged");
		}
		
	}
	
	@FXML
	void onEdit() {
		if (!this.newsReaderModel.getConnectionManager().isLoggedOK()) {
			this.manageAction("edit");
		} else {
			this.manageAction("editlogged");
		}
	}
	
	@FXML
	void onDelete() {
		Alert followAlert;
		Alert alert = new Alert(AlertType.CONFIRMATION, "Are you sure you want to delete the selected article?", ButtonType.YES, ButtonType.NO);
		alert.setTitle("Delete \"" + this.selected.getTitle() + "\"?");
		if (alert.showAndWait().get() == ButtonType.YES) {
			
			Article toDelete = this.selected;
			this.newsReaderModel.getArticles().remove(this.newsReaderModel.getArticles().indexOf(this.selected));
			this.newsReaderModel.deleteArticle(toDelete);
			
			this.articlesList.setItems(this.newsReaderModel.getArticles());
			
			followAlert = new Alert(AlertType.INFORMATION, "Article deleted successfully!");
			followAlert.setTitle("Deleted article");
			followAlert.show();
		}
		
	}
	
	@FXML
	void onLocalFile(ActionEvent event) {
		
		Scene parentScene = this.anchorPane.getScene();
		FXMLLoader loader = null;
		try {
			loader = new FXMLLoader(getClass().getResource(AppScenes.FILE_PICKER.getFxmlFile()));
			Pane root = loader.load();
			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			Window parentStage = parentScene.getWindow();
			Stage stage = new Stage();
			FilePickerController controller = loader.<FilePickerController>getController();
			stage.initOwner(parentStage);
			stage.setScene(scene);
			stage.initModality(Modality.APPLICATION_MODAL);
			stage.showAndWait();
			
			String raw = controller.getFilePath();
			
			// Get just the filename
			String filename = raw.substring(raw.lastIndexOf("/") + 1, raw.length());
			String path ="saveNews//"+ filename;
			
			if (path != null && !(path.equals("")) && !(path.equals("saveNews//"))) {
				
				this.selected = JsonArticle.jsonToArticle(JsonArticle.readFile(path));
				this.onEdit();
			} else {
				Alert alert = new Alert(AlertType.WARNING, "No file selected");
				alert.setTitle("No file");
				alert.show();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	@FXML
    void onLogin() {
		
		if (!this.newsReaderModel.getConnectionManager().isLoggedOK()) {
	    	Scene parentScene = ((Stage) this.anchorPane.getScene().getWindow()).getScene();
	    	
	    	FXMLLoader loader = new FXMLLoader(getClass().getResource(
					AppScenes.LOGIN.getFxmlFile()));
	    	
	    	try {
				Pane root = loader.load();
				Scene scene = new Scene(root);
				scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
				
				// Create the second stage
				Window parentWindow = parentScene.getWindow();
				Stage stage = new Stage();
				stage.initOwner(parentWindow);
				stage.initModality(Modality.APPLICATION_MODAL);
				stage.setScene(scene);
				
				// // Get the selected model
				LoginController controller = loader.<LoginController>getController();
				controller.setConnectionManager(this.newsReaderModel.getConnectionManager());
				
				// Open the new stage and wait for user response
				stage.setX(parentScene.getX());
				stage.setY(parentScene.getY());
				stage.setTitle("Login");
				stage.showAndWait();
				
				this.setUsr(controller.getLoggedUsr());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@FXML
	void initialize() {
    	assert articlesList != null : "fx:id=\"articlesList\" was not injected: check your FXML file 'NewsReader.fxml'.";
    	assert webView != null : "fx:id=\"webView\" was not injected: check your FXML file 'NewsReader.fxml'.";
    	assert categoriesList != null : "fx:id=\"categoriesList\" was not injected: check your FXML file 'NewsReader.fxml'.";
    	assert anchorPane != null : "fx:id=\"anchorPane\" was not injected: check your FXML file 'NewsReader.fxml'.";
    	assert imageView != null : "fx:id=\"imageView\" was not injected: check your FXML file 'NewsReader.fxml'.";
    	assert choiceNew != null : "fx:id=\"choiceNew\" was not injected: check your FXML file 'NewsReader.fxml'.";
    	assert choiceEdit != null : "fx:id=\"choiceEdit\" was not injected: check your FXML file 'NewsReader.fxml'.";
    	assert choiceDelete != null : "fx:id=\"choiceDelete\" was not injected: check your FXML file 'NewsReader.fxml'.";
    	assert choiceLogin != null : "fx:id=\"choiceLogin\" was not injected: check your FXML file 'NewsReader.fxml'.";
    	assert btn_moreDetails != null : "fx:id=\"moreDetails\" was not injected: check your FXML file 'NewsReader.fxml'.";
    	this.btn_moreDetails.setDisable(true);
		this.choiceDelete.setDisable(true);
		this.choiceEdit.setDisable(true);
    	WebEngine webEngine = this.webView.getEngine();
    	
    	this.articlesList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Article>() {
    		@Override
    		/**
    		 * When the selected element is changed this event handler is called
    		 */
    		public void changed(ObservableValue<? extends Article> observable, Article oldValue, Article newValue) {
    			if (newValue != null){
    				NewsReaderController.this.selected = newValue;
    				NewsReaderController.this.btn_moreDetails.setDisable(false);
    				NewsReaderController.this.choiceDelete.setDisable(false);
    				NewsReaderController.this.choiceEdit.setDisable(false);
    				NewsReaderController.this.imageView.setVisible(true);
    				webEngine.loadContent(newValue.getAbstractText());
    				if (newValue.getImageData() != null) {
    					NewsReaderController.this.imageView.setImage(newValue.getImageData());
    				} else {
    					NewsReaderController.this.imageView.setImage(new Image("images/noImage.jpg"));
    				}
    			}
    			else {
    				webEngine.loadContent("");
    			}
    		}
        });
    	
    	filteredItems = new FilteredList<>(this.newsReaderModel.getArticles(), article -> true);
    	this.categoriesList.setItems(this.newsReaderModel.getCategories());
		this.categoriesList.getSelectionModel().select(Categories.ALL);
		this.categoriesList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Categories>() {
			
			@Override
			public void changed(ObservableValue<? extends Categories> observable, Categories oldValue, Categories newValue) {
				
	            if (newValue.equals(null) || newValue == Categories.ALL) {
	                filteredItems.setPredicate(article -> true);
	            } else {
	            	filteredItems.setPredicate(article -> article.getCategory().equals(newValue.toString()));
	            }
	            NewsReaderController.this.articlesList.setItems(filteredItems);
	            NewsReaderController.this.selected = null;
	            NewsReaderController.this.imageView.setImage(null);
	            NewsReaderController.this.imageView.setVisible(false);
	            NewsReaderController.this.btn_moreDetails.setDisable(true);
	            NewsReaderController.this.choiceDelete.setDisable(true);
	            NewsReaderController.this.choiceEdit.setDisable(true);
	            NewsReaderController.this.webView.getEngine().loadContent("");
			};
			
		});
	}
	
	@FXML
	void bye() {
		Stage stage = (Stage) anchorPane.getScene().getWindow();
	    stage.close();
	}

}
