/**
 * 
 */
package application;


import application.news.Article;
import application.news.User;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

/**
 * @authors Ã�ngelLucas & AndreaB-EIT
 *
 */
public class ArticleDetailsController {
	
	@FXML
	private Label title;
	
	@FXML
	private Label subtitle;
	
	@FXML
	private Label category;
	
	@FXML
	private ImageView image;
	
	@FXML
	private WebView text;
	
	@FXML
	private Button btn_AbstractBody;
	private boolean isAbstract = false; // This states if the WebView is showing the contents of the abstract (true) or the body (false)
	// It starts as false because the requirements say to show the body first
	
	@FXML
	private Label showingBA;
	
	@FXML
	private Label userID;
	
    private User usr;
    private Article article;
    // These variables are technically not needed, but I wanted to keep them to make it more "MVC style"

	/**
	 * @param usr the usr to set
	 */
	void setUsr(User usr) {
		this.usr = usr;
		if (this.usr == null) {
			this.userID.setText("");
		} else {
			this.userID.setText("Logged in with user ID: " + this.usr.getIdUser());
		}
	}

	/**
	 * @param article the article to set
	 */
	void setArticle(Article article) {
		this.article = article;
		this.title.setText(this.article.getTitle());
		this.subtitle.setText(this.article.getSubtitle());
		this.category .setText(this.article.getCategory());
		if (article.getImageData() != null) {
			this.image.setImage(article.getImageData());
		} else {
			this.image.setImage(new Image("images/noImage.jpg"));
		}
		WebEngine engine = this.text.getEngine();
		engine.loadContent(this.isAbstract ? this.article.getAbstractText() : this.article.getBodyText());
	}
	
	// This function changes the contents shown in the WebView when the related button is pressed, body <-> abstract
	@FXML
	void onAbstractBody() {
		this.isAbstract = !this.isAbstract;
		this.showingBA.setText(this.isAbstract ? "Showing: Abstract" : "Showing: Body");
		WebEngine engine = this.text.getEngine();
		engine.loadContent(this.isAbstract ? this.article.getAbstractText() : this.article.getBodyText());
	}
	
	// This function closes the details window
	@FXML
    public void onBack(Event event) {
    	Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
    	stage.close();
    }
}
