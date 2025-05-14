package org.lebastudios.theroundtable.ui;

import javafx.scene.Node;

public interface IReciclablePane<T, I>
{
    IReciclablePane<T, I> paneFactory();
    void updateItem(I item, T control);
    
    Node getGraphic();
    String getText();
}
