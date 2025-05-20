package org.lebastudios.theroundtable.components;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.image.ImageView;
import org.lebastudios.theroundtable.apparience.ImageLoader;

public class IconView extends ImageView
{
    private static final int ICON_SIZE = 32;

    private final StringProperty iconName = new SimpleStringProperty("");
    private final DoubleProperty iconSize = new SimpleDoubleProperty();

    public IconView(String iconName)
    {
        this.iconName.addListener((_, _, newValue) ->
        {
            this.setImage(ImageLoader.getIcon(newValue));
        });

        iconSize.addListener((_, _, newValue) ->
        {
            double size = newValue.doubleValue();
            
            this.setFitWidth(size);
            this.setFitHeight(size);

            this.minWidth(size);
            this.minHeight(size);

            this.maxWidth(size);
            this.maxHeight(size);
            
            this.prefHeight(size);
            this.prefWidth(size);
        });

        this.setPreserveRatio(true);
        this.iconName.set(iconName);
        this.iconSize.set(ICON_SIZE);
    }

    public IconView()
    {
        this("");
    }


    public void setIconName(String iconName)
    {
        this.iconName.set(iconName);
    }

    public String getIconName()
    {
        return iconName.get();
    }

    public StringProperty iconNameProperty()
    {
        return iconName;
    }

    public void setIconSize(double iconSize)
    {
        this.iconSize.set(iconSize);
    }

    public double getIconSize()
    {
        return iconSize.get();
    }

    public DoubleProperty iconSizeProperty()
    {
        return iconSize;
    }
}
