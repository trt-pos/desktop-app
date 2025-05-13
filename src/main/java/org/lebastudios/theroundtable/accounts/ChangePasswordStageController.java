package org.lebastudios.theroundtable.accounts;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.stage.Modality;
import org.lebastudios.theroundtable.apparience.UIEffects;
import org.lebastudios.theroundtable.controllers.StageController;
import org.lebastudios.theroundtable.database.Database;
import org.lebastudios.theroundtable.entities.Account;
import org.lebastudios.theroundtable.ui.StageBuilder;

import java.net.URL;

public class ChangePasswordStageController extends StageController<ChangePasswordStageController>
{
    @FXML public PasswordField password;
    @FXML public PasswordField confirmPassword;

    private final Account account;
    private Runnable onAccept;

    public ChangePasswordStageController(Account account)
    {
        this.account = account;
    }

    public ChangePasswordStageController setOnAccept(Runnable runnable)
    {
        this.onAccept = runnable;
        return this;
    }

    @Override
    protected void customizeStageBuilder(StageBuilder stageBuilder)
    {
        stageBuilder.setModality(Modality.WINDOW_MODAL);
    }

    @Override
    public String getTitle()
    {
        return "Change password";
    }

    @FXML
    public void changePassword(ActionEvent actionEvent)
    {
        if (!LocalPasswordValidator.isValidFormat(password.getText()))
        {
            UIEffects.shakeNode(password);
            return;
        }

        if (!password.getText().equals(confirmPassword.getText()))
        {
            UIEffects.shakeNode(password);
            return;
        }

        Database.getInstance().connectTransaction(session ->
        {
            Account account = session.get(Account.class, this.account.getId());

            account.setPassword(LocalPasswordValidator.hashPassword(password.getText()));
            account.setChangePasswordOnNextLogin(false);

            session.merge(account);
        });

        if (onAccept != null) onAccept.run();
        close();
    }

    @FXML
    public void cancel(ActionEvent actionEvent)
    {
        close();
    }

    @Override
    public URL getFXML()
    {
        return ChangePasswordStageController.class.getResource("changePasswordStage.fxml");
    }
}
