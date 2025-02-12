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
import org.lebastudios.theroundtable.locale.LangFileLoader;
import org.lebastudios.theroundtable.logs.Logs;
import org.lebastudios.theroundtable.plugins.PluginLoader;
import org.lebastudios.theroundtable.ui.IconView;
import org.lebastudios.theroundtable.ui.SearchBox;
import org.lebastudios.theroundtable.ui.StageBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HelpStageController extends StageController<HelpStageController>
{
    @FXML private SearchBox searchBox;
    @FXML private WebView htmlView;
    @FXML private TreeView<HelpEntry> indexTreeView;

    private TreeItem<HelpEntry> defaultreeViewRoot;
    private final List<HelpEntry> moduleHelpEntries = new ArrayList<>();

    @Override
    protected void initialize()
    {
        defaultreeViewRoot = new TreeItem<>();
        indexTreeView.setRoot(defaultreeViewRoot);
        indexTreeView.setShowRoot(false);

        PluginLoader.getRessourcesObjects().forEach(resObj ->
        {
            HelpEntry[] entries = HelpEntry.introspectHelp(resObj.getClass());

            for (var entry : entries)
            {
                moduleHelpEntries.add(entry);
                final var moduleTreeItem = entry.intoTreeItem();
                moduleTreeItem.setExpanded(true);

                defaultreeViewRoot.getChildren().add(moduleTreeItem);
            }
        });

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

                        setText(LangFileLoader.getTranslation(helpEntry.metedata().name));

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
                Platform.runLater(() -> htmlView.getEngine().loadContent(content));
            }).start();
        });

        searchBox.setOnSearch(this::searchHelpEntry);
    }

    public void openHelpEntryById(String identifier)
    {
        searchBox.clear();

        TreeItem<HelpEntry> reqHelpEntry = findItemByHelpEntryId(defaultreeViewRoot, identifier);

        if (reqHelpEntry == null)
        {
            Logs.getInstance().log(Logs.LogType.INFO, "HelpEntry with id '" + identifier + "' not found.");
            return;
        }

        indexTreeView.getSelectionModel().select(reqHelpEntry);
        reqHelpEntry.setExpanded(true);
    }

    private TreeItem<HelpEntry> findItemByHelpEntryId(TreeItem<HelpEntry> treeItem, String identifier)
    {
        HelpEntry entry = treeItem.getValue();

        if (entry != null
                && entry.metedata().relControllers != null
                && Arrays.asList(entry.metedata().relControllers).contains(identifier))
        {
            return treeItem;
        }

        for (TreeItem<HelpEntry> entryTreeItem : treeItem.getChildren())
        {
            TreeItem<HelpEntry> found = findItemByHelpEntryId(entryTreeItem, identifier);
            if (found != null) return found;
        }

        return null;
    }

    private void searchHelpEntry(String text)
    {
        if (text.isBlank())
        {
            indexTreeView.setRoot(defaultreeViewRoot);
            return;
        }

        String regex = text.matches("[\\w\\sñÑ]*") ? ".*" + text + ".*" : text;

        TreeItem<HelpEntry> filteredRoot = new TreeItem<>();

        for (var moduleHelpEntry : moduleHelpEntries)
        {
            HelpEntry filtereded = moduleHelpEntry.filteredByKeywords(regex);

            final var moduleTreeItem = filtereded.intoTreeItem();
            moduleTreeItem.setExpanded(true);

            filteredRoot.getChildren().add(moduleTreeItem);
        }

        indexTreeView.setRoot(filteredRoot);
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
