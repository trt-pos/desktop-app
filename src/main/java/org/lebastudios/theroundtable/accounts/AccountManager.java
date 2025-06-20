package org.lebastudios.theroundtable.accounts;

import lombok.Getter;
import org.lebastudios.theroundtable.entities.Account;

@Getter
public class AccountManager
{
    private static AccountManager instance;
    private Account currentLogged;

    private AccountManager() {}

    public static AccountManager getInstance()
    {
        if (instance == null) instance = new AccountManager();

        return instance;
    }

    public boolean isAccountAdmin()
    {
        if (currentLogged == null) return false;

        return currentLogged.getType() == Account.AccountType.ROOT
                || currentLogged.getType() == Account.AccountType.ADMIN;
    }

    public String getCurrentLoggedAccountName()
    {
        return currentLogged == null ? "default" : currentLogged.getName();
    }

    public void setCurrentLogged(Account currentLogged)
    {
        this.currentLogged = currentLogged;

        if (currentLogged == null) return;
        
        AccountEvents.onAccountLogIn.invoke(currentLogged);
    }

    public void logOut()
    {
        Account account = this.currentLogged;
        AccountEvents.onAccountLogOutBefore.invoke(account);
        this.currentLogged = null;
        AccountEvents.onAccountLogOutAfter.invoke(account);
    }
}
