package org.lebastudios.theroundtable.help;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.util.Callback;
import org.lebastudios.theroundtable.Launcher;
import org.lebastudios.theroundtable.controllers.StageController;
import org.lebastudios.theroundtable.ui.IconView;
import org.lebastudios.theroundtable.ui.StageBuilder;

public class HelpStageController extends StageController<HelpStageController>
{
    @FXML private WebView htmlView;
    @FXML private TreeView<HelpEntry> indexTreeView;

    @Override
    protected void initialize()
    {
        TreeItem<HelpEntry> root = new TreeItem<>();
        indexTreeView.setRoot(root);
        indexTreeView.setShowRoot(false);

        HelpEntry entry = HelpEntry.introspectHelp(Launcher.class);

        if (entry != null)
        {
            root.getChildren().add(entry.intoTreeItem());
        }
        
        indexTreeView.setCellFactory(new Callback<>()
        {
            @Override
            public TreeCell<HelpEntry> call(TreeView<HelpEntry> param)
            {
                return new TreeCell<>()
                {
                    {
                        this.setStyle("-fx-padding: 2;");
                    }
                    
                    @Override
                    protected void updateItem(HelpEntry helpEntry, boolean empty)
                    {
                        super.updateItem(helpEntry, empty);

                        if (helpEntry == null || empty)
                        {
                            setText(null);
                            setGraphic(null);
                            return;
                        }

                        setText(helpEntry.metedata().uiName);
                        
                        final var node = new IconView(helpEntry.metedata().helpEntryType.getIconName());
                        node.setIconSize(20);
                        
                        setGraphic(node);
                    }
                };
            }
        });

        indexTreeView.getSelectionModel().selectedItemProperty().addListener((_, oldValue, newValue) ->
        {
            if (newValue == null || oldValue == newValue) return;

            HelpEntry helpEntry = newValue.getValue();
            
            if (helpEntry.metedata().helpEntryType != HelpEntry.Type.MD) return;

            MarkdownHelpToHtml markdownHelpToHtml = helpEntry.intoMarkdownHelp();
            
            new Thread(() ->
            {
                String content = markdownHelpToHtml.getContentAsHtml();
                Platform.runLater(() ->  htmlView.getEngine().loadContent(content));
            }).start();
        });
    }

    @Override
    protected void customizeStageBuilder(StageBuilder stageBuilder)
    {
        stageBuilder.setModality(Modality.APPLICATION_MODAL)
                .setResizeable(true)
                .setIconName("help.png");
    }

    @Override
    public String getTitle()
    {
        return "Help";
    }

    @Override
    public Class<?> getBundleClass()
    {
        return HelpStageController.class;
    }

    @Override
    public boolean hasFXMLControllerDefined()
    {
        return true;
    }
}
