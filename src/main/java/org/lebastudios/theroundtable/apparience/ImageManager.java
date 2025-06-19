package org.lebastudios.theroundtable.apparience;

import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import lombok.SneakyThrows;
import org.lebastudios.theroundtable.communications.AppHttpClient;
import org.lebastudios.theroundtable.env.Directories;
import org.lebastudios.theroundtable.locale.Translator;
import org.lebastudios.theroundtable.logs.Logs;
import org.lebastudios.theroundtable.plugins.IPlugin;
import org.lebastudios.theroundtable.plugins.PluginsManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

public class ImageManager
{
    private static ImageManager instance;

    public static ImageManager getInstance()
    {
        if (instance == null) instance = new ImageManager();

        return instance;
    }

    public enum ImageType
    {
        ICON,
        PERSISTED,
        TEXTURE,
        WEB
    }

    private ImageManager() {}

    private static final ILoader[] LOADERS = new ILoader[]{
            new IconLoader(),
            new PersistedImageLoader(),
            new TextureLoader(),
            new WebImageLoader(),
    };

    public Image get(String imageId, ImageType type)
    {
        return LOADERS[type.ordinal()].load(imageId);
    }

    public Image get(String pluginId, String imageName, ImageType type)
    {
        return get(pluginId + ":" + imageName, type);
    }

    public Image get(Class<? extends IPlugin> plugin, String imageName, ImageType type)
    {
        String pluginId = PluginsManager.getInstance()
                .getPluginOf(plugin)
                .orElseThrow()
                .getPluginData()
                .id;

        return get(pluginId, imageName, type);
    }

    public File persistImageFile(File imgFile)
    {
        try (var inputStream = new FileInputStream(imgFile))
        {
            var image = new Image(inputStream);
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException("This file can't be loaded as an image.");
        }

        var directory = new File(Directories.getPersistedImagesDir());
        var fileExtension = imgFile.getName().substring(imgFile.getName().lastIndexOf("."));
        if (!directory.exists()) directory.mkdir();
        var savedImgPath = directory + "/" + System.currentTimeMillis() + fileExtension;

        // Copy the image to the saved-images folder
        try
        {
            Files.copy(imgFile.toPath(), Paths.get(savedImgPath), StandardCopyOption.REPLACE_EXISTING);
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException("This file can't be saved as an image.");
        }

        return new File(savedImgPath);
    }

    private static boolean imageChooserIsOpen = false;

    public synchronized static ImageChooserResult showImageChooser(Window owner)
    {
        if (imageChooserIsOpen) return null;

        var fileChooser = new FileChooser();

        fileChooser.setTitle(Translator.getInstance().t("core:title.imagechooser"));
        fileChooser.setInitialDirectory(new File(Directories.getPersistedImagesDir()));
        fileChooser.setSelectedExtensionFilter(
                new FileChooser.ExtensionFilter("Image files", "*.png", "*.jpg", "*.jpeg")
        );

        imageChooserIsOpen = true;
        var imageFile = fileChooser.showOpenDialog(owner);
        imageChooserIsOpen = false;

        if (imageFile == null) return null;

        try (var inputStream = new FileInputStream(imageFile))
        {
            var image = new Image(inputStream);
            if (image.isError()) return null;

            return new ImageChooserResult(imageFile, image);
        }
        catch (IOException e)
        {
            return null;
        }
    }

    public record ImageChooserResult(File imageFile, Image image) {}

    private interface ILoader
    {
        Image load(String imageId);
    }

    private static class IconLoader implements ILoader
    {
        private final Map<String, WeakReference<Image>> cache = new HashMap<>();

        @SneakyThrows
        @Override
        public synchronized Image load(String imageId)
        {
            WeakReference<Image> imageWeakRef = cache.get(imageId);
            Image image = imageWeakRef == null ? null : imageWeakRef.get();

            if (image != null) return image;

            int colonIndex = imageId.indexOf(':');
            
            if (colonIndex == -1)
            {
                Logs.getInstance().log(
                        Logs.LogType.WARNING,
                        "The imageId doesn't contain a pluginId: " + imageId
                );
                
                return load("core:icon-not-found.png");
            }
            
            String pluginId = imageId.substring(0, colonIndex);
            String iconName = imageId.substring(colonIndex + 1);

            Class<? extends IPlugin> pluginClass = PluginsManager.getInstance()
                    .getPluginsLoaded()
                    .get(pluginId)
                    .getClass();
            
            try (var resource = pluginClass.getResourceAsStream("icons/" + iconName))
            {
                if (resource != null)
                {
                    image = new Image(resource);
                }
                else
                {
                    Logs.getInstance().log(
                            Logs.LogType.WARNING,
                            "Icon not found: " + iconName + " in plugin: " + pluginId
                    );
                }
            }
            
            if (image == null) image = load("core:icon-not-found.png");

            cache.put(imageId, new WeakReference<>(image));

            return image;
        }
    }

    private static class TextureLoader implements ILoader
    {
        private final Map<String, WeakReference<Image>> cache = new HashMap<>();

        @SneakyThrows
        @Override
        public synchronized Image load(String imageId)
        {
            WeakReference<Image> imageWeakRef = cache.get(imageId);
            Image image = imageWeakRef == null ? null : imageWeakRef.get();

            if (image != null) return image;

            int colonIndex = imageId.indexOf(':');

            if (colonIndex == -1)
            {
                throw new IllegalArgumentException("Invalid image id: " + imageId);
            }

            String pluginId = imageId.substring(0, colonIndex);
            String iconName = imageId.substring(colonIndex + 1);

            Class<? extends IPlugin> pluginClass = PluginsManager.getInstance()
                    .getPluginsLoaded()
                    .get(pluginId)
                    .getClass();

            try (var resource = pluginClass.getResourceAsStream("textures/" + iconName))
            {
                if (resource != null)
                {
                    image = new Image(resource);
                }
                else
                {
                    Logs.getInstance().log(
                            Logs.LogType.WARNING,
                            "Texture not found: " + iconName + " in plugin: " + pluginId
                    );
                }
            }

            if (image == null) return load("core:texture-not-found.png");

            cache.put(imageId, new WeakReference<>(image));
            
            return image;
        }
    }

    private static class PersistedImageLoader implements ILoader
    {
        private final Map<String, WeakReference<Image>> cache = new HashMap<>();

        @Override
        public synchronized Image load(String imageId)
        {
            WeakReference<Image> imageWeakRef = cache.get(imageId);
            Image image = imageWeakRef == null ? null : imageWeakRef.get();

            if (image != null) return image;

            try (FileInputStream resource = new FileInputStream(imageId))
            {
                image = new Image(resource, 100, 100, true, true);
            }
            catch (IOException _) {}

            if (image == null) return ImageManager.getInstance().get("core:blank-image.png", ImageType.ICON);

            cache.put(imageId, new WeakReference<>(image));
            
            return image;
        }
    }

    private static class WebImageLoader implements ILoader
    {
        private final Map<String, WeakReference<Image>> cache = new HashMap<>();

        @SneakyThrows
        @Override
        public synchronized Image load(String imageId)
        {
            WeakReference<Image> imageWeakRef = cache.get(imageId);
            Image image = imageWeakRef == null ? null : imageWeakRef.get();

            if (image != null) return image;

            HttpClient client = AppHttpClient.getInstance().getClient();

            HttpRequest request = HttpRequest.newBuilder(new URI(imageId))
                    .GET()
                    .build();

            try (InputStream is = client.send(request, HttpResponse.BodyHandlers.ofInputStream()).body())
            {
                image = new Image(is, 100, 100, true, true);
            }
            catch (InterruptedException | IOException e)
            {
                Logs.getInstance().log(
                        "Error while downloading image from URL: " + imageId,
                        e
                );

                return ImageManager.instance.get("core:icon-not-found.png", ImageType.ICON);
            }

            cache.put(imageId, new WeakReference<>(image));

            return image;
        }
    }
}
