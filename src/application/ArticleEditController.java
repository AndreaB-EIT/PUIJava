/**
 * 
 */
package application;

import java.io.FileWriter;
import java.io.IOException;
import javax.json.JsonObject;

import application.news.Article;
import application.news.Categories;
import application.news.User;
import application.utils.JsonArticle;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.web.HTMLEditor;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import serverConection.ConnectionManager;
import serverConection.exceptions.ServerCommunicationError;

/**
 * @author Ã�ngelLucas  & AndreaB-EIT
 *
 */
public class ArticleEditController {
	
    private ConnectionManager connection;
	private ArticleEditModel editingArticle;
	private User usr;
	
	@FXML
	private AnchorPane anchorPane;
	
	@FXML
	private TextField titleBox;
	
	@FXML
	private TextField subtitleBox;
	
	@FXML
	private ComboBox<Categories> categoriesList;
	
	@FXML
	private ImageView imageView;
	
	@FXML
	private HTMLEditor content_html;
	
	@FXML
	private TextArea content_text;
	
	@FXML
	private Button btn_saveToFile;
	
	@FXML
	private Button btn_back;
	
	@FXML
	private Button btn_saveAndBack;
	
	@FXML
	private Button btn_TextHtml;
	public boolean isHTML = true; // This states if the user is using the HTML editor (true) or the classic text editor (false)
	
	@FXML
	private Button btn_AbstractBody;
	public boolean editingAbstract = true; // This states whether the user is currently editing the abstract (true) or the body (false)
	
	@FXML
	private Label showingBA;
	
	private Alert alert;
	
	@FXML
	void onImageClicked(MouseEvent event) {
			Scene parentScene = ((Node) event.getSource()).getScene();
			FXMLLoader loader = null;
			try {
				loader = new FXMLLoader(getClass().getResource(AppScenes.IMAGE_PICKER.getFxmlFile()));
				Pane root = loader.load();
				Scene scene = new Scene(root, 570, 420);
				//Scene scene = new Scene(root);
				scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
				Window parentStage = parentScene.getWindow();
				Stage stage = new Stage();
				stage.initOwner(parentStage);
				stage.setScene(scene);
				stage.initStyle(StageStyle.UNDECORATED);
				stage.initModality(Modality.WINDOW_MODAL);
				stage.showAndWait();
				ImagePickerController controller = loader.<ImagePickerController>getController();
				Image image = controller.getImage();
				if (image != null) {
					editingArticle.setImage(image);
					this.imageView.setImage(image);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
	}
	
	// This function manages updating the article with changes. Every component that could change uses this
	@FXML
    void onNewChar(Event event) {
		Node n = (Node)event.getSource();
		String id = n.getId();
		Scene scene = n.getScene();
		
		switch(id) {
		case "titleBox":
			this.editingArticle.titleProperty().set(((TextField) scene.lookup("#" + id)).getText());
			break;
		case "subtitleBox":
			this.editingArticle.subtitleProperty().set(((TextField) scene.lookup("#" + id)).getText());
			break;
		case "categoriesList":
			this.editingArticle.setCategory(this.categoriesList.getSelectionModel().getSelectedItem());
			break;
		case "content_html":
			if (this.editingAbstract) {
				String newAbstract = ((HTMLEditor) scene.lookup("#" + id)).getHtmlText();
				this.editingArticle.abstractTextProperty().set(newAbstract);
			} else {
				String newBody = ((HTMLEditor) scene.lookup("#" + id)).getHtmlText();
				this.editingArticle.bodyTextProperty().set(newBody);
			}
			break;
		case "content_text":
			if (this.editingAbstract) {
				String newAbstract = ((TextArea) scene.lookup("#" + id)).getText();
				this.editingArticle.abstractTextProperty().set(newAbstract);
			} else {
				String newBody = ((TextArea) scene.lookup("#" + id)).getText();
				this.editingArticle.bodyTextProperty().set(newBody);
			}
			break;
		default:
			break;
		}
    }
	
	/**
	 * Send and article to server,
	 * Title and category must be defined and category must be different to ALL
	 * @return true if the article has been saved
	 */
	private boolean send() {
		String titleText = this.editingArticle.getTitle();
		Categories category = this.editingArticle.getCategory();
		if (titleText == null || category == null || 
				titleText.equals("") || category == Categories.ALL) {
			Alert alert = new Alert(AlertType.ERROR, "Impossible to send the article! Title and category are mandatory", ButtonType.OK);
			alert.showAndWait();
			return false;
		} else {
			try {
				this.editingArticle.commit(); // Saving changes to the article
				this.editingArticle.original.setIdArticle(this.connection.saveArticle(this.getArticle())); // Sending the updated article to the server
				return true;
			} catch (ServerCommunicationError e) {
				e.printStackTrace();
				return false;
			}
		}
	}
	
	/**
	 * This method is used to set the connection manager which is
	 * needed to save a news 
	 * @param connection connection manager
	 */
	void setConnectionManager(ConnectionManager connection) {
		this.connection = connection;
		
		// If the user is logged, they can send the updated article to the server with the saveAndBack button
		if (this.connection.isLoggedOK()) {
			this.btn_saveAndBack.setDisable(false);
		} else {
			this.btn_saveAndBack.setDisable(true);
		}
	}

	/**
	 * 
	 * @param usr the usr to set
	 */
	void setUsr(User usr) {
		this.usr = usr;
		if (this.usr == null) {
			this.btn_saveAndBack.setDisable(true);
		} else {
			this.btn_saveAndBack.setDisable(false);
		}
	}
	
	// This is used in the NewsReaderController to "send" the categories to this ArticleEditController
	public void sendCategories(ObservableList<Categories> categories) {		
		// The "All" category can't be picked as a suitable category in the editor, so we filter it out
		FilteredList<Categories> tmp = new FilteredList<Categories>(categories);
		tmp.setPredicate(category -> category != Categories.ALL);

		this.categoriesList.setItems(tmp);
	}
	
	// This function finds the currently selected category to show in the ComboBox
	private Categories findCategory() {
		for (Categories theOne : Categories.values()) {
    		if (this.getArticle().getCategory().equals(theOne.toString())) {
    			return theOne;
    		}
    	}
		return null;
	}

	Article getArticle() {
		Article result = null;
		if (this.editingArticle != null) {
			result = this.editingArticle.getArticleOriginal();
		}
		return result;
	}

	/**
	 * PRE: User must be set
	 * 
	 * @param article
	 *            the article to set
	 */
	void setArticle(Article article) {
		// null = the article is new, not null = the article is being edited
		this.editingArticle = (article != null) ? new ArticleEditModel(article) : new ArticleEditModel(usr);
		
		if (article != null) {
			alert = new Alert(AlertType.INFORMATION, "Article edited successfully!");
			alert.setTitle("Article edited");
			
			this.titleBox.setText(this.getArticle().getTitle());
			if (this.getArticle().getTitle() == null) {
				// Can't save an article without the title
				this.btn_saveToFile.setDisable(true);
			} else {
				this.btn_saveToFile.setDisable(false);
			}
	    	if (this.usr != null) {
	    		// Can't change titles when editing existing logged article
				this.titleBox.setDisable(true); 
	    	}
	    	
	    	this.subtitleBox.setText(this.getArticle().getSubtitle());
	    	this.categoriesList.getSelectionModel().select(findCategory());
	    	if (article.getImageData() != null) {
				this.imageView.setImage(article.getImageData());
			} else {
				this.imageView.setImage(new Image("images/noImage.jpg"));
			}
	    	
	    	// Managing which content editor is being used and seen
	    	if (this.isHTML) {
	    		this.content_html.setVisible(true);
	    		this.content_html.setDisable(false);
	    		this.content_text.setVisible(false);
	    		this.content_text.setDisable(true);
	    		// Managing if the user is editing the abstract or the body
	    		if (this.editingAbstract) {
	    			this.content_html.setHtmlText(article.getAbstractText());
	    		} else {
	    			this.content_html.setHtmlText(article.getBodyText());
	    		}
	    	} else {
	    		this.content_html.setVisible(false);
	    		this.content_html.setDisable(true);
	    		this.content_text.setVisible(true);
	    		this.content_text.setDisable(false);
	    		// Managing if the user is editing the abstract or the body
	    		if (this.editingAbstract) {
	    			this.content_html.setHtmlText(article.getAbstractText());
	    		} else {
	    			this.content_html.setHtmlText(article.getBodyText());
	    		}
	    	}
	    	// Starting with the abstract and HTML active
	    	this.editingAbstract = true;
	    	this.isHTML = true;
		} else {
			alert = new Alert(AlertType.INFORMATION, "Article created successfully!");
			alert.setTitle("Article created");
			
			// Default category value
			this.categoriesList.getSelectionModel().select(Categories.ECONOMY);
			this.editingArticle.setCategory(Categories.ECONOMY);
			
			this.imageView.setImage(new Image("images/noImage.jpg"));
			this.btn_saveToFile.setDisable(true);
			this.btn_saveAndBack.setDisable(true); // The title is null at first
			this.editingArticle.titleProperty().addListener(
		   			 (observable, oldvalue, newValue) -> {
		   				if (newValue != null && !(newValue.equals(""))) {
		   					 // If the title exists, it's possible to save the article to a file
		   					this.btn_saveToFile.setDisable(false);
		   					if (this.usr != null) {
	   							this.btn_saveAndBack.setDisable(false);
		   					}
		   				} else {
		   					this.btn_saveToFile.setDisable(true);
	   					 	this.btn_saveAndBack.setDisable(true);
		   				}
	   				});
		}
	}
	
	/**
	 * Save an article to a file in a json format
	 * Article must have a title
	 */
	private void write(ActionEvent event) {
		this.editingArticle.commit();
		
		//Removes special characters not allowed for filenames
		String name = this.getArticle().getTitle().replaceAll("\\||/|\\\\|:|\\?","");
		String fileName ="saveNews//"+name+".news";
		JsonObject data = JsonArticle.articleToJson(this.getArticle());
		try (FileWriter file = new FileWriter(fileName)) {
	        file.write(data.toString());
	        file.flush();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
	
	// I use this in the NewsReaderController
	public boolean isChanged()
    {
    	return this.editingArticle.bModified;
    }
	
	// This function exits the form without saving changes
	@FXML
    public void exitForm() {
    	this.editingArticle.discardChanges();
    	
    	Stage stage = (Stage) this.anchorPane.getScene().getWindow();
    	stage.close();
    }
	
	// This function saves the article and exits the form
	@FXML
	void onSaveAndBack() throws ServerCommunicationError, IOException {
		if (this.isHTML) {
			this.editingArticle.abstractTextProperty().set(this.editingArticle.getAbstractText());
			this.editingArticle.bodyTextProperty().set(this.editingArticle.getBodyText());
		}

		/*boolean alreadyExists = false;
		for (int i = 0; i < this.connection.getArticles().size(); i++) {
			if (this.connection.getArticles().get(i).getTitle().equals(this.editingArticle.getTitle())) {
				alreadyExists = true;
				i = this.connection.getArticles().size();
			}
		}
		if (alreadyExists) {
			Alert alert = new Alert(AlertType.CONFIRMATION, "An existing article with the same title already exists. Do you want to overwrite it?", ButtonType.YES, ButtonType.NO);
			alert.setTitle("Overwrite existing article \"" + this.editingArticle.getTitle() + "\"?");
			if (alert.showAndWait().get() == ButtonType.YES) {
				if(!(this.send())) {
					alert = new Alert(AlertType.ERROR, "We couldn't send the article to the server, sorry! If you see it in the list, it's just a local version due to an error");
					alert.setTitle("Error");
				}
				alert.show();
				
				Stage stage = (Stage) this.anchorPane.getScene().getWindow();
				stage.close();
				
			} else {
				Alert followAlert = new Alert(AlertType.WARNING, "Please change your title");
				followAlert.setTitle("Change the title");
				followAlert.show();
			}
		} else {*/
			if(!(this.send())) {
				alert = new Alert(AlertType.ERROR, "We couldn't send the article to the server, sorry! If you see it in the list, it's just a local version due to an error");
				alert.setTitle("Error");
			}
			alert.show();
			
			Stage stage = (Stage) this.anchorPane.getScene().getWindow();
			stage.close();
		// }
	}
	
	// This function saves the article to a file
	@FXML
	void onSaveToFile(ActionEvent event) {
		if (this.editingArticle.getTitle() != null) {
			this.write(event);
		}
		
		Alert alert = new Alert(AlertType.INFORMATION, "Article saved successfully on your machine!");
		alert.setTitle("Article saved locally");
		alert.show();
	}
	
	// This function switches between editing HTML and plain text
	@FXML
	void onTextHtml() {
		
		if (this.editingAbstract && this.isHTML) {
			this.content_html.setVisible(false);
    		this.content_html.setDisable(true);
    		this.content_text.setVisible(true);
    		this.content_text.setDisable(false);
			this.content_text.setText(this.editingArticle.getAbstractText());
		} else if (!this.editingAbstract && this.isHTML) {
			this.content_html.setVisible(false);
    		this.content_html.setDisable(true);
    		this.content_text.setVisible(true);
    		this.content_text.setDisable(false);
			this.content_text.setText(this.editingArticle.getBodyText());
		} else if (this.editingAbstract && !this.isHTML) {
			this.content_html.setVisible(true);
    		this.content_html.setDisable(false);
    		this.content_text.setVisible(false);
    		this.content_text.setDisable(true);
			this.content_html.setHtmlText(this.editingArticle.getAbstractText());
		} else {
			this.content_html.setVisible(true);
    		this.content_html.setDisable(false);
    		this.content_text.setVisible(false);
    		this.content_text.setDisable(true);
			this.content_html.setHtmlText(this.editingArticle.getBodyText());
		}
		this.isHTML = !this.isHTML;
	}
	
	// This function switches between editing the abstract and the body
	@FXML
	void onAbstractBody() {
		
		if (this.editingAbstract && this.isHTML) {
			this.content_html.setHtmlText(this.editingArticle.getBodyText());
		} else if (!this.editingAbstract && this.isHTML) {
			this.content_html.setHtmlText(this.editingArticle.getAbstractText());
		} else if (this.editingAbstract && !this.isHTML) {
			this.content_text.setText(this.editingArticle.getBodyText());
		} else {
			this.content_text.setText(this.editingArticle.getAbstractText());
		}
		this.editingAbstract = !this.editingAbstract;
		this.showingBA.setText(this.editingAbstract ? "Editing: Abstract" : "Editing: Body");
	}
	
	@FXML
	void initialize() {
		assert titleBox != null : "fx:id=\"titleBox\" was not injected: check your FXML file 'NewsReader.fxml'.";
    	assert subtitleBox != null : "fx:id=\"subtitleBox\" was not injected: check your FXML file 'NewsReader.fxml'.";
    	assert categoriesList != null : "fx:id=\"categoriesList\" was not injected: check your FXML file 'NewsReader.fxml'.";
    	assert imageView != null : "fx:id=\"imageView\" was not injected: check your FXML file 'NewsReader.fxml'.";
    	assert btn_back != null : "fx:id=\"btn_back\" was not injected: check your FXML file 'NewsReader.fxml'.";
    	assert btn_saveAndBack != null : "fx:id=\"btn_saveAndBack\" was not injected: check your FXML file 'NewsReader.fxml'.";
    	assert btn_TextHtml != null : "fx:id=\"btn_TextHtml\" was not injected: check your FXML file 'NewsReader.fxml'.";
    	assert btn_AbstractBody != null : "fx:id=\"btn_AbstractBody\" was not injected: check your FXML file 'NewsReader.fxml'.";
    	assert btn_saveToFile != null : "fx:id=\"btn_saveToFile\" was not injected: check your FXML file 'NewsReader.fxml'.";
    	assert content_html != null : "fx:id=\"content_html\" was not injected: check your FXML file 'NewsReader.fxml'.";
    	assert content_text != null : "fx:id=\"content_text\" was not injected: check your FXML file 'NewsReader.fxml'.";
    	assert anchorPane != null : "fx:id=\"anchorPane\" was not injected: check your FXML file 'NewsReader.fxml'.";
    	
    	this.showingBA.setText(this.editingAbstract ? "Editing: Abstract" : "Editing: Body");
	}
}
