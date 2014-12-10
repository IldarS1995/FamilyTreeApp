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
    public TableViewCustomControl(Button deletePersonBtn)
    {
        this();

        this.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> deletePersonBtn.setDisable(newVal == null)
        );
    }
    public TableViewCustomControl(List<Person> people)
    {
        this();
        this.setItems(FXCollections.observableArrayList(people));
    }
    public TableViewCustomControl(List<Person> people, Button deletePersonBtn)
    {
        this(deletePersonBtn);
        this.setItems(FXCollections.observableArrayList(people));
    }

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






