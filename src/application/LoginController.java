package application;

import application.news.User;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import serverConection.ConnectionManager;

public class LoginController {
	
	private LoginModel loginModel = new LoginModel();
	
	@FXML
	private TextField usernameBox;
	private String username;
	
	@FXML
	private TextField clearPasswordBox; // This shows the unmasked password
	private String password;
	
	@FXML
	private PasswordField passwordBox; // This shows the masked password
	
	@FXML
	private AnchorPane anchorPane;
	
	private User loggedUsr = null;

	public LoginController (){
		loginModel.setDummyData(false);
		
	}
	
	User getLoggedUsr() {
		return loggedUsr;
		
	}
		
	void setConnectionManager (ConnectionManager connection) {
		this.loginModel.setConnectionManager(connection);
	}
	
	// This handles the showing or masking the password's characters
	@FXML
	void toggleMask() {
		if (this.passwordBox.isDisabled()) {
			this.passwordBox.setText(this.clearPasswordBox.getText());
			this.passwordBox.setDisable(false);
			this.passwordBox.setVisible(true);
			this.clearPasswordBox.setDisable(true);
			this.clearPasswordBox.setVisible(false);
		} else {
			this.clearPasswordBox.setText(this.passwordBox.getText());
			this.clearPasswordBox.setDisable(false);
			this.clearPasswordBox.setVisible(true);
			this.passwordBox.setDisable(true);
			this.passwordBox.setVisible(false);
		}
	}
	
	// This function starts the login process
	@FXML
	void startLogin() {
		this.username = this.usernameBox.getText();
		this.password = (this.passwordBox.isDisabled() ? this.clearPasswordBox.getText() : this.passwordBox.getText());
		Alert alert;
		
		this.loggedUsr = loginModel.validateUser(this.username, this.password);
		if (this.loggedUsr != null) {
			this.closeWindow();
		} else {
			alert = new Alert(AlertType.ERROR, "Wrong username and/or password, sorry", ButtonType.OK);
			alert.setTitle("Bad login");
			alert.showAndWait();

		}
	}
	
	@FXML
	void initialize() {
		assert usernameBox != null : "fx:id=\"usernameBox\" was not injected: check your FXML file 'Login.fxml'.";
		assert passwordBox != null : "fx:id=\"passwordBox\" was not injected: check your FXML file 'Login.fxml'.";
        assert clearPasswordBox != null : "fx:id=\"clearPasswordBox\" was not injected: check your FXML file 'Login.fxml'.";
        assert anchorPane != null : "fx:id=\"anchorPane\" was not injected: check your FXML file 'Login.fxml'.";
	}
	
	// This function closes the window
	@FXML
	void closeWindow() {
		Stage stage = (Stage) anchorPane.getScene().getWindow();
	    stage.close();
	}
}