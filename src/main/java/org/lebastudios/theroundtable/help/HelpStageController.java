package org.lebastudios.theroundtable.help;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.util.Callback;
import org.lebastudios.theroundtable.controllers.StageController;
import org.lebastudios.theroundtable.locale.LangFileLoader;
import org.lebastudios.theroundtable.logs.Logs;
import org.lebastudios.theroundtable.plugins.PluginLoader;
import org.lebastudios.theroundtable.ui.IconView;
import org.lebastudios.theroundtable.ui.SearchBox;
import org.lebastudios.theroundtable.ui.StageBuilder;

import java.io.File;
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
    private HelpEntry renderedHelpEntry;

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

            if (!helpEntry.metedata().helpEntryType.hasContentToShow()) return;

            renderedHelpEntry = helpEntry;
            MarkdownHelpToHtml markdownHelpToHtml = helpEntry.intoMarkdownHelp();

            new Thread(() ->
            {
                String content = markdownHelpToHtml.getContentAsHtml();
                Platform.runLater(() -> htmlView.getEngine().loadContent(content));
            }).start();
        });

        indexTreeView.rootProperty().addListener((_, oldValue, newValue) ->
        {
            if (newValue == null || oldValue == newValue) return;

            class Recursive
            {
                private void showIfOneChild(TreeItem<HelpEntry> treeItem)
                {
                    if (treeItem.isLeaf())
                    {
                        indexTreeView.getSelectionModel().select(treeItem);
                    }
                    
                    if (treeItem.getChildren().size() == 1)
                    {
                        showIfOneChild(treeItem.getChildren().getFirst());
                    }
                }
            }
            
            new Recursive().showIfOneChild(newValue);
        });
        
        searchBox.setOnSearch(this::searchHelpEntry);
        
        htmlView.getEngine().setOnAlert(event ->
        {
            if (renderedHelpEntry == null) 
            {
                Logs.getInstance().log(
                        Logs.LogType.WARNING,
                        "The web view sended an alert while no help entry was being renderer"
                );
                return;
            }
            
            String relFile = event.getData();
            
            String requestedFile = new File(renderedHelpEntry.path().getParentFile(), relFile).getPath();
            
            // Replacing the _lang.md by .yaml, pointing to the metadata of the file requested
            int langPos = requestedFile.lastIndexOf("_");
            String requestedFileMetadataFile = requestedFile.substring(0, langPos) + ".yaml";

            selectHelpEntryByFile(new File(requestedFileMetadataFile));
        });
    }

    public void selectHelpEntryByController(String identifier)
    {
        searchBox.clear();

        TreeItem<HelpEntry> reqHelpEntry = findItemByHelpEntryController(defaultreeViewRoot, identifier);

        if (reqHelpEntry == null)
        {
            Logs.getInstance().log(Logs.LogType.INFO, "HelpEntry with id '" + identifier + "' not found.");
            return;
        }

        indexTreeView.getSelectionModel().select(reqHelpEntry);
        reqHelpEntry.setExpanded(true);
    }

    private TreeItem<HelpEntry> findItemByHelpEntryController(TreeItem<HelpEntry> treeItem, String identifier)
    {
        HelpEntry entry = treeItem.getValue();

        if (entry != null
                && entry.metedata().controllers != null
                && Arrays.asList(entry.metedata().controllers).contains(identifier))
        {
            return treeItem;
        }

        for (TreeItem<HelpEntry> entryTreeItem : treeItem.getChildren())
        {
            TreeItem<HelpEntry> found = findItemByHelpEntryController(entryTreeItem, identifier);
            if (found != null) return found;
        }

        return null;
    }

    private void selectHelpEntryByFile(File file)
    {
        searchBox.clear();

        TreeItem<HelpEntry> reqHelpEntry = findItemByHelpEntryFile(defaultreeViewRoot, file);

        if (reqHelpEntry == null)
        {
            Logs.getInstance().log(Logs.LogType.WARNING, "File '" + file + "' not found inside the tree.");
            return;
        }

        indexTreeView.getSelectionModel().select(reqHelpEntry);
        reqHelpEntry.setExpanded(true);
    }

    private TreeItem<HelpEntry> findItemByHelpEntryFile(TreeItem<HelpEntry> treeItem, File file)
    {
        HelpEntry entry = treeItem.getValue();

        if (entry != null && entry.path().equals(file))
        {
            return treeItem;
        }

        for (TreeItem<HelpEntry> entryTreeItem : treeItem.getChildren())
        {
            TreeItem<HelpEntry> found = findItemByHelpEntryFile(entryTreeItem, file);
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

        text = text.toLowerCase().trim();
        String regex = text.matches("[\\w\\sñÑ]*") ? ".*" + text + ".*" : text;

        TreeItem<HelpEntry> filteredRoot = new TreeItem<>();
        
        for (var moduleHelpEntry : moduleHelpEntries)
        {
            HelpEntry filtereded = moduleHelpEntry.filteredByRegex(regex);

            if (filtereded.innerEntries().length > 0) 
            {
                filteredRoot.getChildren().add(filtereded.intoTreeItem(true));
            }
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
