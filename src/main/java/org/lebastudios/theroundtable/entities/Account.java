package org.lebastudios.theroundtable.entities;

import jakarta.persistence.*;
import javafx.util.StringConverter;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.lebastudios.theroundtable.database.PluginTable;
import org.lebastudios.theroundtable.locale.Translator;

import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@Entity
@PluginTable(name = "account")
public class Account
{
    public static final StringConverter<Account> STRING_CONVERTER = new StringConverter<>()
    {
        @Override
        public String toString(Account account)
        {
            return account.name;
        }

        @Override
        public Account fromString(String string)
        {
            return null;
        }
    };
    
    public Account(String name, String password, AccountType type)
    {
        this.name = name;
        this.password = password;
        this.type = type;
    }

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private AccountType type = AccountType.CASHIER;

    @Column(name = "changue_password_on_next_login", nullable = false)
    private boolean changePasswordOnNextLogin = false;

    @OneToOne(mappedBy = "lastAccount")
    private AppInstallation appInstallation;
    
    public boolean hasAuthorityOver(Account account)
    {
        if (Objects.equals(this.id, account.id)) return true;

        return this.type.accessLevel() < account.type.accessLevel();
    }

    public String getIconName()
    {
        return type.getIconName();
    }

    public enum AccountType
    {
        ROOT,
        ADMIN,
        MANAGER,
        CASHIER,
        ACCOUNTANT;
        
        public int accessLevel()
        {
            return switch (this)
            {
                case ROOT -> 0;
                case ADMIN -> 1;
                case MANAGER -> 2;
                default -> 99;
            };
        }
        
        public boolean hasEnoughAccessLevelAs(AccountType type)
        {
            if (type.accessLevel() == CASHIER.accessLevel()) 
            {
                return type == this;
            }
            
            return accessLevel() <= type.accessLevel();
        }
        
        public String getIconName()
        {
            return switch (this)
            {
                case ROOT, ADMIN -> "core:admin-user.png";
                default -> "core:user.png";
            };
        }

        @Override
        public String toString()
        {
            return Translator.getInstance().t("core:enum.accounttype." + this.name().toLowerCase());
        }
    }

    @Override
    public final boolean equals(Object o)
    {
        if (!(o instanceof Account account)) return false;

        return Objects.equals(id, account.id) && Objects.equals(name, account.name) &&
                Objects.equals(password, account.password) && type == account.type;
    }

    @Override
    public int hashCode()
    {
        int result = Objects.hashCode(id);
        result = 31 * result + Objects.hashCode(name);
        result = 31 * result + Objects.hashCode(password);
        result = 31 * result + Objects.hashCode(type);
        return result;
    }
}
