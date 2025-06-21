package org.lebastudios.theroundtable.reports;

import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.swing.JRViewer;
import net.sf.jasperreports.swing.JRViewerPanel;
import org.lebastudios.theroundtable.controllers.PaneController;

import javax.swing.*;

public class ReportPaneController extends PaneController<ReportPaneController>
{
    @FXML public SwingNode swingNode;

    private JasperPrint actualReport;

    public ReportPaneController(JasperPrint actualReport)
    {
        this.actualReport = actualReport;
    }
    
    public ReportPaneController()
    {
        this(null);
    }

    @Override
    @FXML
    protected void initialize()
    {
        showReport(actualReport);
    }
    
    public void showReport(JasperPrint jasperPrint)
    {
        this.actualReport = jasperPrint;
        
        if(jasperPrint == null) return;
        
        JRViewer viewer = new JRViewer(jasperPrint);
        // Component toolBar = viewer.getComponent(0);
        // Component pagesCounter = viewer.getComponent(2);
        JScrollPane scrollPane = ((JScrollPane) ((JRViewerPanel) viewer.getComponents()[1]).getComponents()[0]);
        JComponent reportView = (JComponent) scrollPane.getComponent(0);

        reportView = (JComponent) reportView.getComponent(0);

        reportView = (JComponent) reportView.getComponent(0);

        swingNode.setContent(reportView);

        reportView = (JComponent) reportView.getComponent(0);

        reportView.remove(0);
        reportView.remove(0);
        reportView.remove(0);
        reportView.remove(0);
        reportView.remove(0);
        reportView.remove(0);
    }
    
    public void hideReport()
    {
        swingNode.setContent(null);
    }
}
