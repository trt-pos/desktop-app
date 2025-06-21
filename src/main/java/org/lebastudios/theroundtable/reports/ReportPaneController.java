package org.lebastudios.theroundtable.reports;

import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.xml.print.JasperPrintLoader;
import net.sf.jasperreports.swing.JRViewer;
import net.sf.jasperreports.swing.JRViewerPanel;
import org.lebastudios.theroundtable.controllers.PaneController;
import org.lebastudios.theroundtable.controllers.StageController;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class ReportPaneController extends PaneController<ReportPaneController>
{
    @FXML public SwingNode swingNode;

    @Override
    @FXML
    protected void initialize()
    {
        try (InputStream input = ReportPaneController.class.getResourceAsStream("test.jasper"))
        {
            JasperReport jasperReport = (JasperReport) JRLoader.loadObject(input);
            Map<String, Object> parameters = new HashMap<>();
            JRDataSource dataSource = new JREmptyDataSource();

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
            JRViewer viewer = new JRViewer(jasperPrint);
            Component toolBar = viewer.getComponent(0);
            Component pagesCounter = viewer.getComponent(2);
            JScrollPane scrollPane = ((JScrollPane) ((JRViewerPanel) viewer.getComponents()[1]).getComponents()[0]);
            JComponent reportView = (JComponent) scrollPane.getComponent(0);
            reportView = (JComponent) reportView.getComponent(0);
            reportView = (JComponent) reportView.getComponent(0);
            reportView = (JComponent) reportView.getComponent(0);
            
            reportView.remove(0);
            reportView.remove(0);
            reportView.remove(0);
            reportView.remove(0);
            reportView.remove(0);
            reportView.remove(0);
            
            reportView = (JComponent) reportView.getComponent(0);
            reportView.setMaximumSize(new Dimension(800, 500));
            reportView.setOpaque(false);
            
            swingNode.setContent((JComponent) reportView);
        }
        catch (JRException | IOException e)
        {
            throw new RuntimeException(e);
        }
    }
}
