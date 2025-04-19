package org.lebastudios.theroundtable.dialogs;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import lombok.NonNull;
import lombok.Setter;
import org.lebastudios.theroundtable.controllers.FormPaneController;
import org.lebastudios.theroundtable.controllers.StageController;
import org.lebastudios.theroundtable.ui.IconButton;
import org.lebastudios.theroundtable.ui.StageBuilder;

import java.util.function.Function;

public class FormDialogController<T> extends StageController<FormDialogController<T>>
{
    @FXML public StackPane formContainer;
    @FXML public IconButton deleteButton;

    protected final FormPaneController<T> formPaneController;
    protected final T object;

    @Setter private Function<T, Boolean> onDeleteAction = _ -> true;
    @Setter private Function<T, Boolean> onSaveAction = _ -> true;

    public FormDialogController(@NonNull FormPaneController<T> formPaneController, @NonNull T object)
    {
        this.formPaneController = formPaneController;
        this.object = object;
    }

    @Override
    protected void initialize()
    {
        formContainer.getChildren().addAll(formPaneController.getRoot());
        formPaneController.setObject(object);

        deleteButton.setVisible(onDeleteAction != null);
    }

    @FXML
    public void deleteButtonAction(ActionEvent actionEvent)
    {
        if (!formPaneController.onDeleteAction(object)) return;

        if (!onDeleteAction.apply(object)) return;
        
        this.close();
    }

    @FXML
    public void cancelButtonAction(ActionEvent actionEvent)
    {
        if (!formPaneController.onCancelAction()) return;
        
        this.close();
    }

    @FXML
    public void saveButtonAction(ActionEvent actionEvent)
    {
        if (!formPaneController.validate())
        {
            return;
        }

        T buildedObject = formPaneController.buildObject(object);

        if (!formPaneController.onSaveAction(buildedObject)) return;

        if (!onSaveAction.apply(buildedObject)) return;

        close();
    }

    @Override
    protected void customizeStageBuilder(StageBuilder stageBuilder)
    {
        stageBuilder.setModality(Modality.WINDOW_MODAL)
                .setResizeable(true);
    }

    @Override
    public String getTitle()
    {
        return "";
    }
}
