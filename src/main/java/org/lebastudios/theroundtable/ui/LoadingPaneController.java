package org.lebastudios.theroundtable.ui;

import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.fxml.FXML;
import javafx.scene.CacheHint;
import javafx.util.Duration;
import org.lebastudios.theroundtable.CorePlugin;
import org.lebastudios.theroundtable.Launcher;
import org.lebastudios.theroundtable.controllers.PaneController;
import org.lebastudios.theroundtable.plugins.IPlugin;

public class LoadingPaneController extends PaneController<LoadingPaneController>
{
    @FXML private IconView loadingIcon;
    
    @Override
    protected void initialize()
    {
        loadingIcon.setIconName("loading.png");
        loadingIcon.setIconSize(35);

        new Thread(() ->
        {
            RotateTransition rotate = new RotateTransition(Duration.seconds(3), loadingIcon);
            rotate.setByAngle(360);
            rotate.setCycleCount(RotateTransition.INDEFINITE);
            rotate.setInterpolator(Interpolator.STEPS(60, Interpolator.StepPosition.START)); // Más suave
            loadingIcon.setCacheHint(CacheHint.SCALE_AND_ROTATE); // Priorizar velocidad
            rotate.play();
        }).start();
    }

    @Override
    public Class<? extends IPlugin> getBundleClass()
    {
        return CorePlugin.class;
    }

}
