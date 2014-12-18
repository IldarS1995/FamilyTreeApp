package ru.kpfu.db.ildar.view.customcontrols;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import ru.kpfu.db.ildar.pojos.Person;

import java.io.IOException;
import java.util.List;

/** Represents a table with 6 fields. Person instances are placed here. */
public class TableViewCustomControl extends TableView<Person>
{
    @FXML
    private TableColumn firstnameCol;
    @FXML
    private TableColumn lastnameCol;
    @FXML
    private TableColumn birthDateCol;
    @FXML
    private TableColumn passportIdCol;
    @FXML
    private TableColumn birthPlaceCol;
    @FXML
    private TableColumn citizenshipCol;

    public TableViewCustomControl()
    {
        //Load FXML file where table is defined
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader()
                .getResource("customwindows/tableviewcustom.fxml"));
        loader.setController(this);
        loader.setRoot(this);

        try
        {
            loader.load();
        }
        catch(IOException exc) { throw new RuntimeException(exc); }

        initColumns();
    }
    /** @param dependBtns Buttons disabled property of which depends on table item selection. */
    public TableViewCustomControl(Button... dependBtns)
    {
        this();

        this.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) ->
                {
                    for(Button btn : dependBtns)
                        btn.setDisable(newVal == null);
                }
        );
    }
    public TableViewCustomControl(List<Person> people)
    {
        this();
        this.setItems(FXCollections.observableArrayList(people));
    }
    public TableViewCustomControl(List<Person> people, Button... dependBtns)
    {
        this(dependBtns);
        this.setItems(FXCollections.observableArrayList(people));
    }

    /** Binds table columns to the according fields */
    private void initColumns()
    {
        firstnameCol.setCellValueFactory(new PropertyValueFactory<Person, String>("firstname"));
        lastnameCol.setCellValueFactory(new PropertyValueFactory<Person, String>("lastname"));
        birthDateCol.setCellValueFactory(new PropertyValueFactory<Person, String>("birthDateAsString"));
        passportIdCol.setCellValueFactory(new PropertyValueFactory<Person, Integer>("passportId"));
        birthPlaceCol.setCellValueFactory(new PropertyValueFactory<Person, String>("birthPlace"));
        citizenshipCol.setCellValueFactory(new PropertyValueFactory<Person, String>("citizenship"));
    }
}






