package org.lebastudios.theroundtable.printers;

import com.github.anastaciocintra.escpos.EscPosConst;
import com.github.anastaciocintra.escpos.Style;

public class Styles
{
    public static final Style BOLD = new Style().setBold(true);
    public static final Style CENTERED = new Style().setJustification(EscPosConst.Justification.Center);
    public static final Style SUBTITLE = new Style().setBold(true).setFontSize(Style.FontSize._1, Style.FontSize._1)
            .setJustification(EscPosConst.Justification.Center);
    public static final Style TITLE = new Style().setBold(true).setFontSize(Style.FontSize._2, Style.FontSize._2)
            .setJustification(EscPosConst.Justification.Center);
    public static final Style RIGHT = new Style().setJustification(EscPosConst.Justification.Right);
    public static final Style LEFT = new Style().setJustification(EscPosConst.Justification.Left_Default);
}

