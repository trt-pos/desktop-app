package org.lebastudios.theroundtable.setup;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import org.lebastudios.theroundtable.accounts.LocalPasswordValidator;
import org.lebastudios.theroundtable.apparience.UIEffects;
import org.lebastudios.theroundtable.database.Database;
import org.lebastudios.theroundtable.entities.Account;
import org.lebastudios.theroundtable.locale.Translator;
import org.lebastudios.theroundtable.ui.TitleBuilder;

import java.net.URL;

public class AccountSetupPaneController extends SetupPaneController
{
    @FXML public TextField usernameField;
    @FXML public Label errorLabel;
    @FXML public TextField passwordField;
    @FXML public TextField confirmPasswordField;

    @FXML
    @Override
    protected void initialize()
    {
        ((BorderPane) getRoot()).setTop(
                new TitleBuilder(
                        Translator.getInstance().t("setup.title.adminconfig"),
                        "admin-user.png"
                ).build()
        );
        errorLabel.setText("");
    }

    @Override
    public URL getFXML()
    {
        return AccountSetupPaneController.class.getResource("accountSetupPane.fxml");
    }

    @Override
    public void apply()
    {
        Account account = new Account(usernameField.getText(),
                LocalPasswordValidator.hashPassword(passwordField.getText()),
                Account.AccountType.ROOT);

        Database.getInstance().connectTransaction(session -> session.persist(account));
    }

    @Override
    public boolean validate()
    {
        if (usernameField.getText().isBlank() || usernameField.getText().length() < 3)
        {
            errorLabel.setText(Translator.getInstance().t("setup.error.invalidname"));
            UIEffects.shakeNode(usernameField);
            return false;
        }

        if (!LocalPasswordValidator.isValidFormat(passwordField.getText()))
        {
            errorLabel.setText(Translator.getInstance().t("setup.error.invalidpassword"));
            UIEffects.shakeNode(passwordField);
            return false;
        }

        if (!passwordField.getText().equals(confirmPasswordField.getText()))
        {
            errorLabel.setText(Translator.getInstance().t("setup.error.passwordmatch"));
            UIEffects.shakeNode(confirmPasswordField);
            return false;
        }

        errorLabel.setText("");
        return true;
    }
}
