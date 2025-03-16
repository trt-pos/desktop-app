package org.lebastudios.theroundtable.accounts;

import at.favre.lib.crypto.bcrypt.BCrypt;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;
import org.lebastudios.theroundtable.Launcher;
import org.lebastudios.theroundtable.apparience.UIEffects;
import org.lebastudios.theroundtable.controllers.StageController;
import org.lebastudios.theroundtable.database.Database;
import org.lebastudios.theroundtable.database.entities.Account;
import org.lebastudios.theroundtable.ui.IconView;
import org.lebastudios.theroundtable.ui.StageBuilder;

import java.util.function.Consumer;

public class PrivilegeScalationStageController extends StageController<PrivilegeScalationStageController>
{
    @FXML private IconView iconView;
    @FXML private TextField accountNameField;
    @FXML private PasswordField accountPasswordField;

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
        iconView.setIconName(accountType.getIconName());
    }

    @FXML
    public void cancel()
    {
        close();
        callback.accept(false);
    }

    @FXML
    public void accept()
    {
        Account account = Database.getInstance().connectQuery(session ->
        {
            Account foundAcc = session.createQuery("from Account a where a.name = :name", Account.class)
                    .setParameter("name", accountNameField.getText())
                    .getSingleResultOrNull();

            if (foundAcc == null)
            {
                return null;
            }
            
            if (!LocalPasswordValidator.validatePassword(accountPasswordField.getText(), foundAcc.getPassword()))
            {
                return null;
            }

            return foundAcc;
        });

        if (account == null)
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
        
        UIEffects.shakeNode(accountNameField);
        UIEffects.shakeNode(accountPasswordField);
    }

    @Override
    protected void customizeStageBuilder(StageBuilder stageBuilder)
    {
        stageBuilder.setModality(Modality.APPLICATION_MODAL)
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
    public Class<?> getBundleClass()
    {
        return Launcher.class;
    }

    @Override
    public void instantiate(Consumer<PrivilegeScalationStageController> acceptController, boolean shouldWait)
    {
        if (AccountManager.getInstance().getCurrentLogged().getType().hasEnoughAccessLevelAs(accountType))
        {
            callback.accept(true);
            return;
        }
        
        super.instantiate(acceptController, shouldWait);
    }
}
