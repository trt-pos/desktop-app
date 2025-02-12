package org.lebastudios.theroundtable.help;

import lombok.SneakyThrows;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.lebastudios.theroundtable.apparience.ThemeLoader;

import java.io.File;
import java.nio.file.Files;

record MarkdownHelpToHtml(File file)
{
    private static final Parser MD_PARSER = Parser.builder().build();
    private static final HtmlRenderer HTML_RENDERER = HtmlRenderer.builder().sanitizeUrls(true).build();
    
    @SneakyThrows
    public String getContentAsHtml()
    {
        String md = Files.readString(file.toPath());
        
        Node document = MD_PARSER.parse(md);
        String body = HTML_RENDERER.render(document);
        
        String style = ThemeLoader.getHelpCss();
        
        return String.format("<head><style>%s</style></head><body>%s</body", style, body);
    }
}
