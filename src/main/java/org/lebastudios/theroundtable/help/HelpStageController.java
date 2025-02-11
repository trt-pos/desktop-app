package org.lebastudios.theroundtable.help;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.util.Callback;
import lombok.SneakyThrows;
import org.lebastudios.theroundtable.Launcher;
import org.lebastudios.theroundtable.controllers.StageController;
import org.lebastudios.theroundtable.ui.IconView;
import org.lebastudios.theroundtable.ui.StageBuilder;

import java.io.File;
import java.net.URL;

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

                        setText(helpEntry.path.getName());
                        
                        final var node = new IconView(helpEntry.type().getIconName());
                        node.setIconSize(20);
                        
                        setGraphic(node);
                    }
                };
            }
        });

        indexTreeView.getSelectionModel().selectedItemProperty().addListener((_, oldValue, newValue) ->
        {
            if (newValue == null || oldValue == newValue) return;

            if (newValue.getValue().type != HelpEntry.Type.MD) return;

            MarkdownHelp markdownHelp = new MarkdownHelp(newValue.getValue().path);
            
            new Thread(() ->
            {
                String content = markdownHelp.getContentAsHtml();
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

    private record HelpEntry(File path, Type type, HelpEntry[] innerEntries)
    {
        public enum Type
        {
            MD, DIR, MODULE;
            
            public String getIconName()
            {
                return switch (this)
                {
                    case MD -> "md-help-file.png";
                    case DIR -> "directory.png";
                    case MODULE -> "module.png";
                };
            }
        }

        public TreeItem<HelpEntry> intoTreeItem()
        {
            TreeItem<HelpEntry> root = new TreeItem<>(this);

            for (var entry : innerEntries)
            {
                root.getChildren().add(entry.intoTreeItem());
            }

            return root;
        }

        @SneakyThrows
        public static HelpEntry introspectHelp(Class<?> clazz)
        {
            URL helpUrl = clazz.getResource("/help");

            if (helpUrl == null) return null;

            File entry = new File(helpUrl.toURI());

            return new HelpEntry(
                    entry,
                    Type.MODULE,
                    introspectHelp(entry)
            );
        }

        public static HelpEntry[] introspectHelp(File file)
        {
            File[] entries = file.listFiles();

            if (entries == null) return new HelpEntry[0];

            HelpEntry[] helpEntries = new HelpEntry[entries.length];

            for (int i = 0; i < helpEntries.length; i++)
            {
                File entry = entries[i];

                HelpEntry helpEntry = new HelpEntry(
                        entry,
                        entry.isDirectory() ? Type.DIR : Type.MD,
                        introspectHelp(entry)
                );

                helpEntries[i] = helpEntry;
            }

            return helpEntries;
        }
    }
}
