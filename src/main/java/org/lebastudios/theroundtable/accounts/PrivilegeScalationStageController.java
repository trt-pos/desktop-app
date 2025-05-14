package org.lebastudios.theroundtable.accounts;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.PasswordField;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;
import org.lebastudios.theroundtable.apparience.UIEffects;
import org.lebastudios.theroundtable.controllers.StageController;
import org.lebastudios.theroundtable.database.Database;
import org.lebastudios.theroundtable.entities.Account;
import org.lebastudios.theroundtable.components.IconView;
import org.lebastudios.theroundtable.components.StageBuilder;

import java.util.List;
import java.util.function.Consumer;

public class PrivilegeScalationStageController extends StageController<PrivilegeScalationStageController>
{
    @FXML public IconView iconView;
    @FXML public ChoiceBox<Account> accountChoiceBox;
    @FXML public PasswordField accountPasswordField;

    private final Consumer<Boolean> callback;
    private final Account.AccountType accountType;

    public PrivilegeScalationStageController(Account.AccountType accountType, Consumer<Boolean> callback)
    {
        this.callback = callback;
        this.accountType = accountType;
    }

    @Override
    @FXML
    protected void initialize()
    {
        List<Account> accounts = Database.getInstance().connectQuery(session ->
        {
            return session.createQuery("from Account", Account.class)
                    .getResultList()
                    .stream().filter(account -> account.getType().hasEnoughAccessLevelAs(accountType))
                    .toList();
        });
        
        accountChoiceBox.getItems().setAll(accounts);
        accountChoiceBox.getSelectionModel().selectFirst();
        
        accountChoiceBox.setConverter(Account.STRING_CONVERTER);
    }

    @FXML
    public void cancel(ActionEvent actionEvent)
    {
        close();
        callback.accept(false);
    }

    @FXML
    public void accept(ActionEvent actionEvent)
    {
        if (!LocalPasswordValidator.validatePassword(
                accountPasswordField.getText(),
                accountChoiceBox.getValue().getPassword()))
        {
            loginErrorAnimation();
            return;
        }
        
        callback.accept(true);
        close();
    }

    private void loginErrorAnimation()
    {
        accountPasswordField.setText("");
        
        UIEffects.shakeNode(accountPasswordField);
    }

    @Override
    protected void customizeStageBuilder(StageBuilder stageBuilder)
    {
        stageBuilder.setModality(Modality.WINDOW_MODAL)
                .setResizeable(false)
                .setStageConsumer(stage ->
                {
                    stage.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, _ -> callback.accept(false));
                });
    }

    @Override
    public String getTitle()
    {
        return "";
    }

    @Override
    public void instantiate(Consumer<PrivilegeScalationStageController> acceptController, boolean shouldWait)
    {
        Account currentLogged = AccountManager.getInstance().getCurrentLogged();
        
        if (currentLogged != null && currentLogged.getType().hasEnoughAccessLevelAs(accountType))
        {
            callback.accept(true);
            return;
        }
        
        super.instantiate(acceptController, shouldWait);
    }
}
