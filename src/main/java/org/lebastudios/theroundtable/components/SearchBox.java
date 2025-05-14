package org.lebastudios.theroundtable.components;

import javafx.geometry.Pos;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import lombok.Getter;
import lombok.Setter;

import java.util.function.Consumer;

public class SearchBox extends HBox
{
    private final TextField searchField;
    private final IconButton searchButton;
    
    @Setter @Getter private Consumer<String> onSearch = s -> {};
    
    public SearchBox()
    {
        super();
        
        HBox textFieldContainer = new HBox();
        HBox.setHgrow(textFieldContainer, Priority.ALWAYS);
        textFieldContainer.setAlignment(Pos.CENTER_LEFT);
        textFieldContainer.setSpacing(5);
        
        searchField = new TextField();
        searchField.setPromptText("Search...");

        textFieldContainer.getStyleClass().addAll(searchField.getStyleClass());
        searchField.getStyleClass().clear();
        HBox.setHgrow(searchField, Priority.ALWAYS);
        
        searchButton = new IconButton();
        searchButton.setIconName("find.png");
        searchButton.setIconSize(16);
        
        searchButton.setOnAction(e -> onSearch.accept(searchField.getText()));

        textFieldContainer.getChildren().addAll(searchField, searchButton);
        
        this.setSpacing(10);
        this.setAlignment(Pos.CENTER_LEFT);
        this.getChildren().addAll(textFieldContainer);
        
        this.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.ENTER)
            {
                accept();
                e.consume();
            }
        });
    }
    
    public String getText()
    {
        return searchField.getText();
    }

    public void clear()
    {
        searchField.clear();

        accept();
    }
    
    private void accept()
    {
        onSearch.accept(this.getText());
        this.setFocused(false);
    }
}
