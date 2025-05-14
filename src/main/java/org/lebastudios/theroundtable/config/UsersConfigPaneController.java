package org.lebastudios.theroundtable.config;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.lebastudios.theroundtable.accounts.AccountCreatorStageController;
import org.lebastudios.theroundtable.accounts.AccountManager;
import org.lebastudios.theroundtable.accounts.ChangePasswordStageController;
import org.lebastudios.theroundtable.database.Database;
import org.lebastudios.theroundtable.dialogs.InformationTextDialogController;
import org.lebastudios.theroundtable.entities.Account;
import org.lebastudios.theroundtable.locale.Translator;
import org.lebastudios.theroundtable.components.IconButton;
import org.lebastudios.theroundtable.components.IconView;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class UsersConfigPaneController extends ConfigPaneController<NoConfigFile>
{
    @FXML public Label errorLabel;
    @FXML public IconButton deleteAccount;
    @FXML public ComboBox<Account.AccountType> accountType;
    @FXML public CheckBox changePasswordOnNextLogin;
    @FXML public PasswordField passwordField;
    @FXML public VBox usersContainer;
    @FXML public IconView userIcon;
    @FXML public Label userName;
    @FXML public StackPane userView;

    private Account selectedAccount;

    private final Set<Account> addedAccounts = new HashSet<>();
    private final Set<Account> removedAccounts = new HashSet<>();

    public UsersConfigPaneController()
    {
        super(new NoConfigFile(), Translator.getInstance().t("word.users"), "users.png");
    }

    @Override
    public void updateConfigData(NoConfigFile configData)
    {
        if (selectedAccount == null) return;

        Account.AccountType type = Account.AccountType.values()[accountType.getSelectionModel().getSelectedIndex() + 1];
        if (type == null) return;

        Database.getInstance().connectTransaction(session ->
        {
            addedAccounts.forEach(session::persist);
            removedAccounts.forEach(session::remove);
            
            Account account = session.get(Account.class, selectedAccount.getId());

            account.setType(type);
            account.setChangePasswordOnNextLogin(changePasswordOnNextLogin.isSelected());

            session.merge(account);
        });

        addedAccounts.clear();
        removedAccounts.clear();
        
        reloadUsersContainer();
    }

    @Override
    public void updateUI(NoConfigFile configData)
    {
        addedAccounts.clear();
        removedAccounts.clear();

        accountType.getItems().clear();
        accountType.getItems().addAll(Account.AccountType.values());
        accountType.getItems().removeFirst();

        reloadUsersContainer();

        passwordField.setEditable(false);
        passwordField.setOnMouseClicked(e ->
        {
            if (selectedAccount.getType() != Account.AccountType.ROOT)
            {
                new ChangePasswordStageController(selectedAccount)
                        .setOwner(this.getStage())
                        .instantiate();
            }
        });
    }

    @Override
    public ValidationResult validate()
    {
        return ValidationResult.valid();
    }

    private void reloadUsersContainer()
    {
        usersContainer.getChildren().clear();

        Database.getInstance().connectQuery(session ->
        {
            final var currentLogged = AccountManager.getInstance().getCurrentLogged();

            List<Account> accounts = session
                    .createQuery("from Account", Account.class)
                    .list();

            accounts.addAll(addedAccounts);

            for (Account account : accounts)
            {
                if (removedAccounts.contains(account)) continue;

                if (account.getType() != Account.AccountType.ROOT 
                        && Objects.equals(account.getId(), currentLogged.getId())) continue;
                if (!currentLogged.hasAuthorityOver(account)) continue;

                usersContainer.getChildren().add(createUserNode(account));
            }
        });

        showAccount(AccountManager.getInstance().getCurrentLogged());
    }

    public void addUser(ActionEvent actionEvent)
    {
        new AccountCreatorStageController(account ->
        {
            if (account != null)
            {
                usersContainer.getChildren().add(createUserNode(account));
                addedAccounts.add(account);
            }
        }).setOwner(this.getStage()).instantiate(false);
    }

    @FXML
    public void removeUser(ActionEvent actionEvent)
    {
        if (selectedAccount == null) return;

        if (selectedAccount.getType() == Account.AccountType.ROOT)
        {
            new InformationTextDialogController("The root account cannot be deleted.").instantiate();
            return;
        }

        if (selectedAccount.getType() == Account.AccountType.ADMIN
                && AccountManager.getInstance().getCurrentLogged().getType() != Account.AccountType.ROOT)
        {
            new InformationTextDialogController("Only the root account can delete an admin account.").instantiate();
            return;
        }

        if (!addedAccounts.remove(selectedAccount))
        {
            removedAccounts.add(selectedAccount);
        }

        showAccount(AccountManager.getInstance().getCurrentLogged());
        reloadUsersContainer();
    }

    private Node createUserNode(Account account)
    {
        HBox root = new HBox();
        root.setPrefWidth(250);
        root.getStyleClass().add("button");
        root.setSpacing(10);

        root.setOnMouseClicked(e -> showAccount(account));

        IconView icon = new IconView(account.getIconName());
        icon.setIconSize(35);
        root.getChildren().add(icon);

        VBox info = new VBox();
        root.getChildren().add(info);
        info.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(info, Priority.ALWAYS);
        info.setSpacing(5);

        info.getChildren().add(new Label(account.getName()));
        info.getChildren().add(new Label(account.getType().toString()));

        return root;
    }

    private void showAccount(Account account)
    {
        this.selectedAccount = account;

        deleteAccount.setDisable(
                account.getType() == Account.AccountType.ROOT
                        || account.getId() == AccountManager.getInstance().getCurrentLogged().getId()
        );

        userIcon.setIconName(account.getIconName());
        userName.setText(account.getName());

        accountType.setValue(account.getType());
        accountType.setDisable(account.getType() == Account.AccountType.ROOT);

        passwordField.setText("abc123.");

        changePasswordOnNextLogin.setSelected(account.isChangePasswordOnNextLogin());

        userView.setVisible(true);
    }
}
