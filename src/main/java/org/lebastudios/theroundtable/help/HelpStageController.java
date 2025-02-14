package org.lebastudios.theroundtable.help;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.util.Callback;
import lombok.SneakyThrows;
import org.lebastudios.theroundtable.controllers.StageController;
import org.lebastudios.theroundtable.locale.LangFileLoader;
import org.lebastudios.theroundtable.logs.Logs;
import org.lebastudios.theroundtable.plugins.PluginLoader;
import org.lebastudios.theroundtable.ui.IconView;
import org.lebastudios.theroundtable.ui.SearchBox;
import org.lebastudios.theroundtable.ui.StageBuilder;
import org.w3c.dom.Document;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@OpenHelp(disabled = true)
public class HelpStageController extends StageController<HelpStageController>
{
    @FXML private TabPane manualsTabPane;
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

        manualsTabPane.getSelectionModel().selectedItemProperty().addListener((_, oldValue, newValue) ->
        {
            if (newValue == null || oldValue == newValue) return;

            if (oldValue != null) oldValue.setContent(null);

            newValue.setContent(indexTreeView.getParent());

            ManualType manualType = ManualType.values()[manualsTabPane.getTabs().indexOf(newValue)];

            moduleHelpEntries.clear();
            defaultreeViewRoot.getChildren().clear();
            searchBox.clear();

            PluginLoader.getRessourcesObjects().forEach(resObj ->
            {
                HelpEntry[] entries = HelpEntry.introspectManual(resObj.getClass(), manualType);

                for (var entry : entries)
                {
                    moduleHelpEntries.add(entry);
                    TreeItem<HelpEntry> moduleTreeItem = entry.intoTreeItem();
                    moduleTreeItem.setExpanded(true);

                    defaultreeViewRoot.getChildren().add(moduleTreeItem);
                }
            });
        });

        manualsTabPane.getTabs().clear();
        for (var manualType : ManualType.values())
        {
            Tab tab = new Tab(manualType.getManualName());
            IconView graphic = new IconView(manualType.getIconName());
            graphic.setIconSize(20);
            tab.setGraphic(graphic);
            manualsTabPane.getTabs().add(tab);
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

                        HelpEntryMetadata metedata = helpEntry.metedata();

                        setText(LangFileLoader.getTranslation(metedata.name));

                        String iconName = metedata.iconName != null
                                ? metedata.iconName
                                : metedata.helpEntryType.getIconName();

                        final var node = new IconView(iconName);
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

            if (helpEntry == null) return;

            if (helpEntry.metedata().helpEntryType == HelpEntry.Type.LINK) 
            {
                this.redirectTo(helpEntry.metedata().redirectTo, helpEntry);
                return;
            }
            
            if (!helpEntry.metedata().helpEntryType.hasContentToShow()) return;

            renderedHelpEntry = helpEntry;

            new Thread(() ->
            {
                MarkdownHelpToHtml markdownHelpToHtml = helpEntry.intoMarkdownHelp();
                
                String scrollToElementScript = """
                        <script>
                            function scrollToElement(id) {
                                var element = document.getElementById(id);
                                if (element) {
                                    element.scrollIntoView({ behavior: 'smooth', block: 'start' });
                                }
                            }
                        </script>""";

                String hrefHandlerScript = """
                        <script>
                            document.querySelectorAll('a').forEach(a => {
                                a.onclick = () => {
                                    alert(a.href);
                                };
                            });
                        </script>""";

                String content = String.format(
                        "%s\n%s\n%s", 
                        markdownHelpToHtml.getContentAsHtml(),
                        scrollToElementScript, hrefHandlerScript
                );
                

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

            redirectTo(event.getData(), renderedHelpEntry);
        });
    }

    @SneakyThrows
    private void redirectTo(String path, HelpEntry from)
    {
        // TODO: If the link has a protocol behave as expected
        String requestedFile = new File(from.path().getParentFile(), path).getPath();

        if (requestedFile.matches(".*\\.md")) 
        {
            // Replacing the _lang.md by .yaml, pointing to the metadata of the file requested
            int langPos = requestedFile.lastIndexOf("_");
            requestedFile = requestedFile.substring(0, langPos) + ".yaml";
        }

        selectHelpEntryByFile(new File(requestedFile).getCanonicalFile());
    }
    
    public void selectHelpEntryByController(String controller)
    {
        searchBox.clear();

        TreeItem<HelpEntry> reqHelpEntry = findItemByHelpEntryController(defaultreeViewRoot, controller);

        if (reqHelpEntry == null)
        {
            Logs.getInstance().log(Logs.LogType.INFO, "HelpEntry with id '" + controller + "' not found.");
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

    public void showElementWithId(String id)
    {
        htmlView.getEngine().documentProperty().addListener(new ChangeListener<>()
        {
            @Override
            public void changed(ObservableValue<? extends Document> observable, Document oldValue, Document newValue)
            {
                if (newValue == null) return;

                htmlView.getEngine().executeScript(String.format("scrollToElement('%s')", id));
                htmlView.getEngine().documentProperty().removeListener(this);
            }
        });
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
