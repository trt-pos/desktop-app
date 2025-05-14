package org.lebastudios.theroundtable.components;

import javafx.animation.FadeTransition;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class SwitcheableNodePane extends StackPane
{
    public void switchContent(Node form)
    {
        if (this.getChildren().contains(form)) return;

        this.getChildren().setAll(form);

        FadeTransition ft = new FadeTransition(Duration.millis(200), form);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    } 
}
