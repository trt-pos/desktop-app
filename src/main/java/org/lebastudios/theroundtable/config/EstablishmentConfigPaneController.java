package org.lebastudios.theroundtable.config;

import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import lombok.SneakyThrows;
import org.lebastudios.theroundtable.Launcher;
import org.lebastudios.theroundtable.apparience.ImageLoader;

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
        super(new EstablishmentConfigData());
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
            imageFile = ImageLoader.saveImageInSpecialFolder(imageFile);
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
        establishmentLogo.setOnTouchPressed(_ -> selectImage());
        if (!imageFile.exists() || !imageFile.isFile())
        {
            establishmentLogo.setImage(ImageLoader.getIcon("no-product-image.png"));
        }
        else
        {
            establishmentLogo.setImage(ImageLoader.getSavedImage(configData.logoImgPath));
        }
    }

    @Override
    public boolean validate()
    {
        return true;
    }

    @SneakyThrows
    private void selectImage()
    {
        var result = ImageLoader.showImageChooser(this.getStage().getOwner());
        
        if (result == null) return;
        
        establishmentLogo.setImage(result.image());
        this.imageFile = result.imageFile();
    }

    @Override
    public Class<?> getBundleClass()
    {
        return Launcher.class;
    }

}
