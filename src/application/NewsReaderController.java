/**
 * 
 */
package application;

import java.io.IOException;
import application.news.Article;
import application.news.Categories;
import application.news.User;
import application.utils.JsonArticle;
import application.utils.exceptions.ErrorMalFormedArticle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import serverConection.ConnectionManager;

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
	
	// This function is used to manage the width of the articles' list container
	private double getLongestTitleSize(ObservableList<Article> list) {
		double longest = 0;
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getTitle().length() > longest) {
				longest = list.get(i).getTitle().length();
			}
		}
		//return Math.min(longest*8, 512); // This can be uncommented and used to limit the maximum size
		return longest*8;
	}

	// This function gets the data from the server and updates the UI
	private void getData() {
		ObservableList<Article> list = this.newsReaderModel.getArticles();
		
		this.newsReaderModel.retrieveData();
		
		this.articlesList.setMinWidth(this.getLongestTitleSize(list));
		this.articlesList.setItems(list);
		
		// Update the UI based on the user being logged in or not
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
		if (this.getUsr() != null) {
			this.getData();
		}
	}
	
	// This function manages opening the article editor in the right way
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
			stage.initStyle(StageStyle.UNDECORATED); // With this, no upper bar with the X and the other icons
			stage.setScene(scene);
			stage.setTitle("Article editor");

			// Get the selected model
			ArticleEditController controller = loader.<ArticleEditController>getController();
			controller.setConnectionManager(this.newsReaderModel.getConnectionManager());
			controller.sendCategories(this.newsReaderModel.getCategories());
			
			// To clear and undo changes when the user closes the window
			stage.setOnCloseRequest(ev -> controller.exitForm());
			
			// The managing itself happens here
			switch (action) {
			case "new": // In this case the user is not logged and they're creating a new article
				controller.setArticle(null);
				break;
			case "newlogged": // In this case the user is logged and they're creating a new article
				// Pass the data
				controller.setUsr(this.getUsr());
				controller.setArticle(null);
				alert = new Alert(AlertType.INFORMATION, "Article created successfully!");
				alert.setTitle("Article created");
				break;
			case "edit": // In this case the user is not logged and they're editing an existing article
				controller.setArticle(this.selected);
				break;
			case "editlogged": // In this case the user is logged and they're editing an existing article
				controller.setUsr(this.getUsr());
				controller.setArticle(this.selected);
				break;
			}
			
			// Open the new stage and wait for user response
			stage.showAndWait();
			
			// Since the requirements don't say anything about sending again an article from a file being considered an edit or a new file,
			// I decided to treat it like a new file, if it doesn't have a saved article ID already
			
			// Finding the edited article if an article was edited (returns -1 if the article was new)
			int index = -1;
			for (int i = 0; i < this.newsReaderModel.getArticles().size(); i++) {
				if (this.newsReaderModel.getArticles().get(i).getIdArticle() == controller.getArticle().getIdArticle()) {
					index = i;
					i = this.newsReaderModel.getArticles().size();
				}
			}
			
			// Decide whether to add the item to the list (new article) or to replace the old one with the new one (edit article)
			if (controller.isChanged() && index != -1) {
				Article edited = controller.getArticle();
				this.articlesList.getItems().add(index, edited);
				this.articlesList.getItems().remove(index+1);
				this.articlesList.refresh();
			} else if (controller.isChanged() && index == -1) {
    			Article newArticle = controller.getArticle();
    			this.articlesList.getItems().add(0, newArticle);
    			this.articlesList.refresh();
    		}
			
			// Deselecting the item
			this.selected = null;
			this.webView.getEngine().loadContent("");
			this.imageView.setVisible(false);
			this.articlesList.getSelectionModel().select(-1);
			
			// This fixes removing the "All" category for the editor
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
	
	// This function handles opening the article details page
	@FXML
	void onMoreDetails(ActionEvent event) {
		if(this.selected == null) {
			Alert alert = new Alert(AlertType.ERROR, "No selected article");
			alert.setTitle("No article");
			alert.show();
			return;
		}
		System.out.println(this.selected.getIdUser());
		
		Scene parentScene = ((Stage) this.anchorPane.getScene().getWindow()).getScene();
    	
    	FXMLLoader loader = new FXMLLoader(getClass().getResource(
				AppScenes.NEWS_DETAILS.getFxmlFile()));
    	
    	try {
			Pane root = loader.load();
			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			
			// Create the separate stage
			Window parentWindow = parentScene.getWindow();
			Stage stage = new Stage();
			stage.initOwner(parentWindow);
			stage.initModality(Modality.NONE);
			stage.setScene(scene);
			stage.setTitle("Details - " + this.selected.getTitle());
			stage.setMinHeight(root.minHeight(-1));
			stage.setMinWidth(root.minWidth(-1));

			ArticleDetailsController controller = loader.<ArticleDetailsController>getController();

			controller.setUsr(this.newsReaderModel.getConnectionManager().isLoggedOK() ? this.getUsr() : null);
			
			controller.setArticle(this.selected);

			stage.show();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// The two following functions are what "start" the main manageAction
	// This one handles new articles
	@FXML
	void onNew() {
		
		if (!this.newsReaderModel.getConnectionManager().isLoggedOK()) {
			this.manageAction("new");
		} else {
			this.manageAction("newlogged");
		}
		
	}
	
	// This one handles editing articles
	@FXML
	void onEdit() {
		if (!this.newsReaderModel.getConnectionManager().isLoggedOK()) {
			this.manageAction("edit");
		} else {
			this.manageAction("editlogged");
		}
	}
	
	// This functions handles articles deletion
	@FXML
	void onDelete() {
		Alert followAlert;
		// Asking for confirmation is very important when deleting an article
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
	
	// This functions loads a local .news file
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
			stage.initStyle(StageStyle.UNDECORATED);
			stage.showAndWait();
			
			String raw = controller.getFilePath();
			
			// Get just the filename
			String filename = raw.substring(raw.lastIndexOf("/") + 1, raw.length());
			
			String sane = filename.replaceAll("[%20]+", " ");
			
			String path ="saveNews//"+ sane;
			
			if (path != null && !(path.equals("")) && !(path.equals("saveNews//"))) {
				// Opening the file with editing purposes
				try {
					Article loaded = JsonArticle.jsonToArticle(JsonArticle.readFile(path));
					if ((loaded.getIdUser() == -1) || (this.getUsr() != null && this.getUsr().getIdUser() == loaded.getIdUser())) {
						// If the article doesn't belong to any logged user then anyone can edit it
						// If the article belongs to the logged user then the logger user can edit it
						// loaded.setIdUser(this.getUsr().getIdUser());
						this.selected = loaded;
						this.onEdit();
						
					} else if ((this.getUsr() != null && this.getUsr().getIdUser() != loaded.getIdUser()) || (this.getUsr() == null && loaded.getIdUser() != -1)) {
						// If the article has an owner then only the logged owner can access it
						Alert alert = new Alert(AlertType.ERROR, "You have no authorization to access this file as you're not the author, sorry");
						alert.setTitle("No authorization");
						alert.show();
					} else {
						// This is just for safety but this should never be reached
						Alert alert = new Alert(AlertType.ERROR, "There was an error with your file, sorry");
						alert.setTitle("Error");
						alert.show();
					}
				} catch (ErrorMalFormedArticle e) {
					Alert alert = new Alert(AlertType.ERROR, "The article seems to be malformed and cannot be loaded, sorry");
					alert.setTitle("Malformed article");
					alert.show();
				}
				
				
			} else {
				Alert alert = new Alert(AlertType.WARNING, "No file selected");
				alert.setTitle("No file");
				alert.show();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	// This function handles the login window, and the window (see LoginController and LoginModel) will handle the login process
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
				
				Window parentWindow = parentScene.getWindow();
				Stage stage = new Stage();
				stage.initOwner(parentWindow);
				stage.initModality(Modality.APPLICATION_MODAL);
				stage.initStyle(StageStyle.UNDECORATED);
				stage.setScene(scene);
				stage.setTitle("Login");
				
				LoginController controller = loader.<LoginController>getController();
				controller.setConnectionManager(this.newsReaderModel.getConnectionManager());
				
				// Open the new stage and wait for user response
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
    	
    	// Handling the "selected" item
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
    			} else {
    				webEngine.loadContent("");
    			}
    		}
        });
    	
    	// Filtering the list of articles by the selected category
    	filteredItems = new FilteredList<Article>(this.newsReaderModel.getArticles(), article -> true);
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
	
	// This function closes the form
	@FXML
	void bye() {
		Stage stage = (Stage) anchorPane.getScene().getWindow();
	    stage.close();
	}

}
