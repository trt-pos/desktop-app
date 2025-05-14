package org.lebastudios.theroundtable.components;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import lombok.Getter;
import lombok.Setter;
import org.lebastudios.theroundtable.CorePlugin;
import org.lebastudios.theroundtable.locale.Translator;

import java.time.LocalDate;
import java.util.function.BiConsumer;

@Getter
@Setter
public class DateRangePicker extends HBox
{
    private final DatePicker startDatePicker = new DatePicker();
    private final DatePicker endDatePicker = new DatePicker();

    private final ObjectProperty<LocalDate> startDate = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> endDate = new SimpleObjectProperty<>();

    private DoubleProperty prefDatePickerWidth = new SimpleDoubleProperty();
    private DoubleProperty prefDatePickerHeight = new SimpleDoubleProperty();
    
    private DoubleProperty minDatePickerWidth = new SimpleDoubleProperty();
    private DoubleProperty minDatePickerHeight = new SimpleDoubleProperty();
    
    private DoubleProperty maxDatePickerWidth = new SimpleDoubleProperty();
    private DoubleProperty maxDatePickerHeight = new SimpleDoubleProperty();
    
    private BiConsumer<LocalDate, LocalDate> onDateChange = (_, _) -> {};
    
    public DateRangePicker()
    {
        super(10);

        Label fromLabel = new Label(Translator.getInstance().t("components.rangedatepicker.from", CorePlugin.class));
        Label toLabel = new Label(Translator.getInstance().t("components.rangedatepick.to", CorePlugin.class));
        
        getChildren().addAll(fromLabel, startDatePicker, toLabel, endDatePicker);
        this.setAlignment(Pos.CENTER);
        
        prefDatePickerHeight.bindBidirectional(startDatePicker.prefHeightProperty());
        prefDatePickerWidth.bindBidirectional(startDatePicker.prefWidthProperty());
        prefDatePickerHeight.bindBidirectional(endDatePicker.prefHeightProperty());
        prefDatePickerWidth.bindBidirectional(endDatePicker.prefWidthProperty());
        
        minDatePickerHeight.bindBidirectional(startDatePicker.prefHeightProperty());
        minDatePickerWidth.bindBidirectional(startDatePicker.prefWidthProperty());
        minDatePickerHeight.bindBidirectional(endDatePicker.prefHeightProperty());
        minDatePickerWidth.bindBidirectional(endDatePicker.prefWidthProperty());
        
        maxDatePickerHeight.bindBidirectional(startDatePicker.prefHeightProperty());
        maxDatePickerWidth.bindBidirectional(startDatePicker.prefWidthProperty());
        maxDatePickerHeight.bindBidirectional(endDatePicker.prefHeightProperty());
        maxDatePickerWidth.bindBidirectional(endDatePicker.prefWidthProperty());
        
        startDate.bindBidirectional(startDatePicker.valueProperty());
        endDate.bindBidirectional(endDatePicker.valueProperty());

        endDatePicker.valueProperty().addListener((_, _, newVal) ->
        {
            if (newVal == null) return;
            
            if (startDate.get() != null && newVal.isBefore(startDate.get()))
            {
                endDatePicker.setValue(startDate.get());
                return;
            }
            
            onDateChange.accept(startDate.get(), newVal);
        });

        startDatePicker.valueProperty().addListener((_, _, newVal) ->
        {
            if (newVal == null) return;
            
            if (endDate.get() != null && newVal.isAfter(endDate.get()))
            {
                startDatePicker.setValue(endDate.get());
                return;
            }
            
            onDateChange.accept(newVal, endDate.get());
        });
    }
}
