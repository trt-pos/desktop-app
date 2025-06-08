package org.lebastudios.theroundtable.config;

import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import lombok.SneakyThrows;
import org.lebastudios.theroundtable.apparience.ImageManager;
import org.lebastudios.theroundtable.locale.Translator;

import java.io.File;

public class EstablishmentConfigPaneController extends ConfigPaneController<EstablishmentConfigData>
{
    public TextField establishmentName;
    public TextField establishmentId;
    public TextField establishmentAddress;
    public TextField establishmentPhone;
    public TextField establishmentZipCode;
    public TextField establishmentCity;
    public ImageView establishmentLogo;

    private File imageFile;

    public EstablishmentConfigPaneController()
    {
        super(new EstablishmentConfigData(), Translator.getInstance().t("core:word.establishment"), "core:establishment.png");
    }

    @Override
    public void updateConfigData(EstablishmentConfigData configData)
    {
        configData.name = establishmentName.getText();
        configData.id = establishmentId.getText();

        configData.phone = establishmentPhone.getText();
        configData.address = establishmentAddress.getText();

        configData.city = establishmentCity.getText();
        configData.zipCode = establishmentZipCode.getText();

        if (imageFile != null)
        {
            imageFile = ImageManager.getInstance().persistImageFile(imageFile);
            configData.logoImgPath = imageFile.getAbsolutePath();
        }
    }

    @Override
    public void updateUI(EstablishmentConfigData configData)
    {
        establishmentName.setText(configData.name);
        establishmentId.setText(configData.id);

        establishmentPhone.setText(configData.phone);

        establishmentAddress.setText(configData.address);
        establishmentCity.setText(configData.city);
        establishmentZipCode.setText(configData.zipCode);

        var imageFile = new File(configData.logoImgPath);
        establishmentLogo.setOnMouseClicked(_ -> selectImage());
        if (!imageFile.exists() || !imageFile.isFile())
        {
            establishmentLogo.setImage(ImageManager.getInstance().get("core:icon-not-found.png", ImageManager.ImageType.ICON));
        }
        else
        {
            establishmentLogo.setImage(ImageManager.getInstance().get(configData.logoImgPath, ImageManager.ImageType.PERSISTED));
        }
    }

    @Override
    public ValidationResult validate()
    {
        return ValidationResult.valid();
    }

    @SneakyThrows
    private void selectImage()
    {
        var result = ImageManager.showImageChooser(this.getStage().getOwner());
        
        if (result == null) return;
        
        establishmentLogo.setImage(result.image());
        this.imageFile = result.imageFile();
    }

}
