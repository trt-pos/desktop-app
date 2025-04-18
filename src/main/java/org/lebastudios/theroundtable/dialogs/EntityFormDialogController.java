package org.lebastudios.theroundtable.dialogs;

import jakarta.persistence.Entity;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import lombok.NonNull;
import org.lebastudios.theroundtable.controllers.FormPaneController;
import org.lebastudios.theroundtable.controllers.StageController;
import org.lebastudios.theroundtable.database.Database;
import org.lebastudios.theroundtable.ui.IconButton;
import org.lebastudios.theroundtable.ui.StageBuilder;

public class EntityFormDialogController<T> extends StageController<EntityFormDialogController<T>>
{
    @FXML public StackPane formContainer;
    @FXML public IconButton deleteButton;

    private final FormPaneController<T> formPaneController;
    private final T object;

    public EntityFormDialogController(@NonNull FormPaneController<T> formPaneController, @NonNull T object)
    {
        this.formPaneController = formPaneController;

        if (!object.getClass().isAnnotationPresent(Entity.class)) 
        {
            throw new IllegalArgumentException("Object must be annotated with @Entity");
        }
        
        this.object = object;
    }
    
    @Override
    protected void initialize()
    {
        formContainer.getChildren().addAll(formPaneController.getRoot());
        formPaneController.setObject(object);
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

    public void deleteButtonAction(ActionEvent actionEvent) 
    {
        boolean result = Database.getInstance().connectTransactionWithBool(session ->
        {
            session.remove(object);
        });
        
        if (result) 
        {
            close();
        }
    }

    public void cancelButtonAction(ActionEvent actionEvent) 
    {
        this.close();
    }

    public void saveButtonAction(ActionEvent actionEvent) 
    {
        if (!formPaneController.validate()) 
        {
            return;
        }
        
        T objectBuilded = formPaneController.buildObject(object);
        
        boolean result = Database.getInstance().connectTransactionWithBool(session ->
        {
            session.persist(objectBuilded);
        });

        if (result)
        {
            close();
        }
    }
}
