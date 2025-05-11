package org.lebastudios.theroundtable.dialogs;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.lebastudios.theroundtable.controllers.FormPaneController;
import org.lebastudios.theroundtable.database.Database;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.function.Consumer;

public class EntityFormDialogController<T> extends FormDialogController<T>
{
    private boolean persisted;
    
    @SneakyThrows
    public EntityFormDialogController(@NonNull FormPaneController<T> formPaneController, @NonNull T object)
    {
        super(formPaneController, object);

        if (!object.getClass().isAnnotationPresent(Entity.class))
        {
            throw new IllegalArgumentException("Object must be annotated with @Entity");
        }
        
        this.onDeleteAction = objectDeleted ->
        {
            return Database.getInstance().connectTransactionWithBool(session ->
            {
                session.remove(objectDeleted);
            });
        };
        
        this.onSaveAction = objectBuilded ->
        {
            return Database.getInstance().connectTransactionWithBool(session ->
            {
                Consumer<T> f = persisted ? session::merge : session::persist;
                f.accept(objectBuilded);
            });
        };

        Field[] fields = object.getClass().getDeclaredFields();
        Field fieldId = Arrays.stream(fields)
                .parallel()
                .filter(field -> field.isAnnotationPresent(Id.class))
                .findFirst().orElseThrow();
        String capitalizedName = fieldId.getName().substring(0, 1).toUpperCase() + fieldId.getName().substring(1);
        Method idGetter = object.getClass().getMethod("get" + capitalizedName);
        Object objectId = idGetter.invoke(object);

        persisted = objectId != null
                && Database.getInstance().connectQuery(s -> s.get(object.getClass(), objectId) != null);
    }

    @Override
    protected void initialize()
    {
        super.initialize();
        
        deleteButton.setVisible(persisted);
    }

    @Override
    protected void loadFXML()
    {
        this.root = new org.lebastudios.theroundtable.dialogs.FormDialog$View(this);
        this.initialize();
    }
}
