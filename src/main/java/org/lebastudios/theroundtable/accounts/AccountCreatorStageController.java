package org.lebastudios.theroundtable.accounts;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import org.lebastudios.theroundtable.apparience.UIEffects;
import org.lebastudios.theroundtable.controllers.StageController;
import org.lebastudios.theroundtable.entities.Account;
import org.lebastudios.theroundtable.ui.StageBuilder;

import java.net.URL;
import java.util.function.Consumer;

public class AccountCreatorStageController extends StageController<AccountCreatorStageController>
{
    @FXML public TextField usernameField;
    @FXML public PasswordField passwordField;
    @FXML public PasswordField confirmPasswordField;
    @FXML public ChoiceBox<Account.AccountType> accountTypeChoiceBox;

    private final Consumer<Account> accountConsumer;

    public AccountCreatorStageController(Consumer<Account> accountConsumer)
    {
        this.accountConsumer = accountConsumer;
    }
    
    @FXML @Override protected void initialize()
    {
        accountTypeChoiceBox.getItems().addAll(Account.AccountType.values());
        accountTypeChoiceBox.getItems().removeFirst();

        accountTypeChoiceBox.getSelectionModel().select(0);
    }

    @FXML
    public void createAccount(ActionEvent actionEvent)
    {
        if (passwordField.getText().isBlank() || passwordField.getText().length() < 8)
        {
            UIEffects.shakeNode(passwordField);
            return;
        }

        if (!passwordField.getText().equals(confirmPasswordField.getText()))
        {
            UIEffects.shakeNode(confirmPasswordField);
            return;
        }

        Account account = new Account(
                usernameField.getText(),
                LocalPasswordValidator.hashPassword(passwordField.getText()),
                Account.AccountType.values()[accountTypeChoiceBox.getSelectionModel().getSelectedIndex() + 1]
        );

        close();
        
        if (accountConsumer != null) accountConsumer.accept(account);
    }

    @FXML
    public void cancel(ActionEvent actionEvent)
    {
        close();
    }

    @Override
    protected void customizeStageBuilder(StageBuilder stageBuilder)
    {
        stageBuilder.setModality(Modality.WINDOW_MODAL);
    }

    @Override
    public String getTitle()
    {
        return "Create Account";
    }

    @Override
    public URL getFXML()
    {
        return AccountCreatorStageController.class.getResource("accountCreator.fxml");
    }
}
