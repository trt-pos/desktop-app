package org.lebastudios.theroundtable.config;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.lebastudios.theroundtable.apparience.UIEffects;
import org.lebastudios.theroundtable.locale.Currency;
import org.lebastudios.theroundtable.locale.Translator;
import org.lebastudios.theroundtable.locale.Language;

import java.time.format.DateTimeFormatter;

public class GlobalPreferencesConfigPaneController extends ConfigPaneController<GlobalPreferencesConfigData>
{
    @FXML public ChoiceBox<Language> languageChoiceBox;
    @FXML public ChoiceBox<Currency> currencyChoiceBox;
    @FXML public TextField dateTimeFormatterTextField;

    @FXML public CheckBox proxyEnabledCheckBox;
    @FXML public TextField proxyHostTextField;
    @FXML public TextField proxyPortTextField;
    @FXML public CheckBox proxyAuthenticationCheckBox;
    @FXML public VBox proxyAuthenticationContainer;
    @FXML public TextField proxyUsernameTextField;
    @FXML public TextField proxyPasswordTextField;

    @FXML public CheckBox intraProxyEnabledCheckBox;
    @FXML public CheckBox intraProxyAuthenticationCheckBox;
    @FXML public TextField intraProxyHostTextField;
    @FXML public TextField intraProxyPortTextField;
    @FXML public VBox intraProxyAuthenticationContainer;
    @FXML public TextField intraProxyUsernameTextField;
    @FXML public TextField intraProxyPasswordTextField;

    public GlobalPreferencesConfigPaneController()
    {
        super(new GlobalPreferencesConfigData(), Translator.getInstance().t("core.config.globalpref"), "preferences.png");
    }

    @Override
    protected void initialize()
    {
        super.initialize();

        languageChoiceBox.getItems().setAll(Language.availableLanguages());
        languageChoiceBox.setConverter(Language.CONVERTER);

        currencyChoiceBox.getItems().setAll(Currency.availableCurrencies());
        currencyChoiceBox.setConverter(Currency.CONVERTER);

        proxyAuthenticationContainer.disableProperty().bind(proxyAuthenticationCheckBox.selectedProperty().not());
        intraProxyAuthenticationContainer.disableProperty()
                .bind(intraProxyAuthenticationCheckBox.selectedProperty().not());
    }

    @Override
    public void updateConfigData(GlobalPreferencesConfigData configData)
    {
        configData.language = languageChoiceBox.getValue();
        configData.currency = currencyChoiceBox.getValue();
        configData.dateTimeFormatter = dateTimeFormatterTextField.getText();

        configData.proxyData.enabled = proxyEnabledCheckBox.isSelected();
        configData.proxyData.proxyAddress = proxyHostTextField.getText();
        configData.proxyData.proxyPort = Integer.parseInt(proxyPortTextField.getText());
        configData.proxyData.proxyUsername =
                proxyAuthenticationCheckBox.isSelected() ? proxyUsernameTextField.getText() : null;
        configData.proxyData.proxyPassword =
                proxyAuthenticationCheckBox.isSelected() ? proxyPasswordTextField.getText() : null;

        configData.intraNetProxyData.enabled = intraProxyEnabledCheckBox.isSelected();
        configData.intraNetProxyData.proxyAddress = intraProxyHostTextField.getText();
        configData.intraNetProxyData.proxyPort = Integer.parseInt(intraProxyPortTextField.getText());
        configData.intraNetProxyData.proxyUsername =
                intraProxyAuthenticationCheckBox.isSelected() ? intraProxyUsernameTextField.getText() : null;
        configData.intraNetProxyData.proxyPassword =
                intraProxyAuthenticationCheckBox.isSelected() ? intraProxyPasswordTextField.getText() : null;
    }

    @Override
    public void updateUI(GlobalPreferencesConfigData configData)
    {
        languageChoiceBox.setValue(configData.language);
        currencyChoiceBox.setValue(configData.currency);
        dateTimeFormatterTextField.setText(configData.dateTimeFormatter);

        proxyEnabledCheckBox.setSelected(configData.proxyData.enabled);
        proxyHostTextField.setText(configData.proxyData.proxyAddress);
        proxyPortTextField.setText(String.valueOf(configData.proxyData.proxyPort));
        proxyAuthenticationCheckBox.setSelected(configData.proxyData.proxyUsername != null);
        proxyUsernameTextField.setText(configData.proxyData.proxyUsername);
        proxyPasswordTextField.setText(configData.proxyData.proxyPassword);

        intraProxyEnabledCheckBox.setSelected(configData.intraNetProxyData.enabled);
        intraProxyHostTextField.setText(configData.intraNetProxyData.proxyAddress);
        intraProxyPortTextField.setText(String.valueOf(configData.intraNetProxyData.proxyPort));
        intraProxyAuthenticationCheckBox.setSelected(configData.intraNetProxyData.proxyUsername != null);
        intraProxyUsernameTextField.setText(configData.intraNetProxyData.proxyUsername);
        intraProxyPasswordTextField.setText(configData.intraNetProxyData.proxyPassword);
    }

    @Override
    public ValidationResult validate()
    {
        if (!validateProxyData(proxyEnabledCheckBox, proxyHostTextField, proxyPortTextField,
                proxyAuthenticationCheckBox, proxyUsernameTextField, proxyPasswordTextField))
        {
            return ValidationResult.invalid();
        }

        if (!validateProxyData(intraProxyEnabledCheckBox, intraProxyHostTextField, intraProxyPortTextField,
                intraProxyAuthenticationCheckBox, intraProxyUsernameTextField, intraProxyPasswordTextField))
        {
            return ValidationResult.invalid();
        }

        if (dateTimeFormatterTextField.getText().isBlank())
        {
            UIEffects.shakeNode(dateTimeFormatterTextField);
            return ValidationResult.invalid();
        }
        
        try
        {
            DateTimeFormatter.ofPattern(dateTimeFormatterTextField.getText());
        }
        catch (IllegalArgumentException e)
        {
            UIEffects.shakeNode(dateTimeFormatterTextField);
            return ValidationResult.invalid();
        }
        
        return ValidationResult.valid();
    }

    private boolean validateProxyData(CheckBox proxyEnabledCheckBox, TextField proxyHostTextField,
            TextField proxyPortTextField, CheckBox proxyAuthenticationCheckBox, TextField proxyUsernameTextField,
            TextField proxyPasswordTextField)
    {
        if (proxyEnabledCheckBox.isSelected())
        {
            if (proxyHostTextField.getText().isBlank())
            {
                UIEffects.shakeNode(proxyHostTextField);
                return false;
            }

            if (proxyPortTextField.getText().isBlank())
            {
                UIEffects.shakeNode(proxyPortTextField);
                return false;
            }

            try
            {
                Integer.parseInt(proxyPortTextField.getText());
            }
            catch (NumberFormatException e)
            {
                UIEffects.shakeNode(proxyPortTextField);
                return false;
            }

            if (proxyAuthenticationCheckBox.isSelected())
            {
                if (proxyUsernameTextField.getText().isEmpty())
                {
                    UIEffects.shakeNode(proxyUsernameTextField);
                    return false;
                }

                if (proxyPasswordTextField.getText().isEmpty())
                {
                    UIEffects.shakeNode(proxyPasswordTextField);
                    return false;
                }
            }
        }
        return true;
    }
}
