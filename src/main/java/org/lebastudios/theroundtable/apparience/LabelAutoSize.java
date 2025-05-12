package org.lebastudios.theroundtable.apparience;

import javafx.beans.value.ChangeListener;
import javafx.scene.control.Label;

public class LabelAutoSize
{
    public static void apply(Label label)
    {
        ChangeListener<Number> resizeListener = (_, _, _) -> {
            double width = label.getWidth();
            if (width == 0) return;

            int length = label.getText().length();
            double fontSize = Math.max(8, Math.min(13, width / (length * 0.6)));
            label.setStyle("-fx-font-size: " + fontSize + ";");
        };

        label.widthProperty().addListener(resizeListener);
    }
}
