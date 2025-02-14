package org.lebastudios.theroundtable.help;

import lombok.SneakyThrows;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.lebastudios.theroundtable.apparience.ThemeLoader;

import java.io.File;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

record MarkdownHelpToHtml(File file)
{
    private static final Parser MD_PARSER = Parser.builder().build();
    private static final HtmlRenderer HTML_RENDERER = HtmlRenderer.builder().build();

    @SneakyThrows
    public String getContentAsHtml()
    {
        String md = Files.readString(file.toPath());

        Node document = MD_PARSER.parse(md);
        String body = processBody(HTML_RENDERER.render(document));

        String style = ThemeLoader.getHelpCss();

        return String.format("<head><style>%s</style></head><body>%s</body>", style, body);
    }

    private String processBody(String body)
    {
        // TODO: Avoid absolute paths and the ones that have a protocol (http, https, etc)
        // Setting img src to be a valid absolute URI from a relative one
        String regex = "<img[^>]*src=\"([^\"]*)\"";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(body);

        while (matcher.find())
        {
            String relativePath = matcher.group(1);
            String absolutePath = file.getParentFile().toURI().resolve(relativePath).toString();
            body = body.replace(relativePath, absolutePath);
        }

        return body;
    }
}
