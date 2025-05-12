package org.lebastudios.theroundtable.ui;

import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import org.lebastudios.theroundtable.apparience.LabelAutoSize;

public class LabeledIconButton extends VBox
{
    private final Label label;
    private final IconView icon;
    private final EventHandler<MouseEvent> handler;
    
    public LabeledIconButton(String text, IconView icon, EventHandler<MouseEvent> handler)
    {
        super();

        this.label = new Label(text);
        this.icon = icon;
        this.handler = handler;

        LabelAutoSize.apply(label);
        
        label.setWrapText(true);
        
        icon.setIconSize(80);
        
        this.setOnMouseClicked(handler);
        this.setSpacing(10);
        this.getStyleClass().add("button");
        this.setAlignment(Pos.CENTER);
        this.setPrefSize(125, 125);
        
        this.getChildren().addAll(this.icon, this.label);
        
    }
}
