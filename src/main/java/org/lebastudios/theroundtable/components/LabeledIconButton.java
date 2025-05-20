package org.lebastudios.theroundtable.components;

import javafx.beans.property.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import lombok.Getter;
import lombok.Setter;
import org.lebastudios.theroundtable.apparience.LabelAutoSize;

public class LabeledIconButton extends VBox
{
    private final ObjectProperty<EventHandler<ActionEvent>> onAction = new SimpleObjectProperty<>();
    private final StringProperty text = new SimpleStringProperty("");
    private final StringProperty iconName = new SimpleStringProperty("");
    private final DoubleProperty iconSize = new SimpleDoubleProperty();
    
    public LabeledIconButton(String text, String iconName, EventHandler<ActionEvent> onAction)
    {
        super();

        Label label = new Label();
        label.textProperty().bindBidirectional(this.text);
        
        IconView icon = new IconView();
        icon.iconNameProperty().bindBidirectional(this.iconName);
        icon.iconSizeProperty().bindBidirectional(this.iconSize);
        
        this.onAction.addListener((_, _, newValue) ->
        {
            this.setOnMouseClicked(event -> newValue.handle(new ActionEvent(event.getSource(), event.getTarget())));
        });

        LabelAutoSize.apply(label);
        label.setWrapText(true);
        
        this.setSpacing(10);
        this.getStyleClass().add("button");
        this.setAlignment(Pos.CENTER);
        this.setPrefSize(125, 125);
        
        this.getChildren().addAll(icon, label);
        
        VBox.setVgrow(icon, Priority.ALWAYS);
        
        this.text.set(text);
        this.iconName.set(iconName);
        this.iconSize.set(75);
        this.onAction.setValue(onAction);
    }
    
    public LabeledIconButton()
    {
        this("", "", null);
    }
    
    public void setOnAction(EventHandler<ActionEvent> onAction)
    {
        this.onAction.set(onAction);
    }
    
    public EventHandler<ActionEvent> getOnAction()
    {
        return onAction.get();
    }
    
    public String getText()
    {
        return text.get();
    }
    
    public void setText(String text)
    {
        this.text.set(text);
    }
    
    public String getIconName()
    {
        return iconName.get();
    }
    
    public void setIconName(String iconName)
    {
        this.iconName.set(iconName);
    }
    
    public double getIconSize()
    {
        return iconSize.get();
    }
    
    public void setIconSize(double iconSize)
    {
        this.iconSize.set(iconSize);
    }
}
