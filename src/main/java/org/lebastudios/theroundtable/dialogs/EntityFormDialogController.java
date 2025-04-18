package org.lebastudios.theroundtable.dialogs;

import jakarta.persistence.Entity;
import lombok.NonNull;
import org.lebastudios.theroundtable.controllers.FormPaneController;
import org.lebastudios.theroundtable.database.Database;

public class EntityFormDialogController<T> extends FormDialogController<T>
{
    public EntityFormDialogController(@NonNull FormPaneController<T> formPaneController, @NonNull T object)
    {
        super(formPaneController, object);

        if (!object.getClass().isAnnotationPresent(Entity.class))
        {
            throw new IllegalArgumentException("Object must be annotated with @Entity");
        }
        
        setOnDeleteAction(() ->
        {
            boolean result = Database.getInstance().connectTransactionWithBool(session ->
            {
                session.remove(object);
            });

            if (result)
            {
                close();
            }
        });
        
        setOnSaveAction(objectBuilded ->
        {
            return Database.getInstance().connectTransactionWithBool(session ->
            {
                session.persist(objectBuilded);
            });
        });
    }
}
