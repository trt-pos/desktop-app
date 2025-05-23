package org.lebastudios.theroundtable.components;

import javafx.scene.control.Label;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import lombok.Getter;
import lombok.Setter;
import org.lebastudios.theroundtable.apparience.UIEffects;

@Setter
@Getter
public class Copy2Clipboard extends IconButton
{
    private String labelId;
    
    public Copy2Clipboard()
    {
        this.setIconName("copy.png");
        this.setOnAction(_ ->
        {
            UIEffects.showPopup(this, "Copied to clipboard");
            Label label = (Label) this.getParent().lookup("#" + labelId);
            
            if (label == null || label.getText() == null) return;
            
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(label.getText());
            clipboard.setContent(content);
        });
    }
}
