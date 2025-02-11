package org.lebastudios.theroundtable.help;

import lombok.SneakyThrows;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import java.io.File;
import java.nio.file.Files;

record MarkdownHelp(File file)
{
    private static final Parser MD_PARSER = Parser.builder().build();
    private static final HtmlRenderer HTML_RENDERER = HtmlRenderer.builder().build();
    
    @SneakyThrows
    public String getContentAsHtml()
    {
        String md = Files.readString(file.toPath());
        
        Node document = MD_PARSER.parse(md);
        return HTML_RENDERER.render(document);
    }
}
