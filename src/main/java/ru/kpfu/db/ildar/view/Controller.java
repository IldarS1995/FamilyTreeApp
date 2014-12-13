package ru.kpfu.db.ildar.view;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.Dialogs;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.dao.DuplicateKeyException;
import ru.kpfu.db.ildar.dao.PeopleDAO;
import ru.kpfu.db.ildar.pojos.Person;
import ru.kpfu.db.ildar.view.customcontrols.*;
import ru.kpfu.db.ildar.view.dialogs.*;

import java.net.URL;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.ResourceBundle;

public class Controller implements Initializable
{
    public static Stage primaryStage;

    private List<String> countries;

    @FXML
    private TabPane tabPane;
    @FXML
    public Button deletePersonBtn;
    @FXML
    private Button addChildrenBtn;
    @FXML
    private Button browseChildrenBtn;

    private PeopleDAO peopleDAO;

    @FXML
    private void onExit(ActionEvent evt)
    {
        System.exit(0);
    }

    private void initPeopleDAO()
    {
        ConfigurableApplicationContext context =
                new ClassPathXmlApplicationContext("dispatcher-servlet.xml");
        peopleDAO = context.getBean(PeopleDAO.class);
        context.close();
    }

    @FXML
    private void addPersonClicked(ActionEvent actionEvent)
    {
        AddNewPersonDialog dialog = new AddNewPersonDialog(primaryStage, "Add New Person", countries);
        Action result = dialog.showDialog();
        if(result == dialog.getSubmitAction())
        {
            Person p = dialog.getPerson();
            addNewPerson(p);
        }
    }

    private void addNewPerson(Person p)
    {
        try
        {
            peopleDAO.addPerson(p);
        }
        catch(DuplicateKeyException exc)
        {
            if(exc.getCause() instanceof SQLIntegrityConstraintViolationException)
            {
                Dialogs.create().title("Adding hasn't occurred")
                        .message("Couldn't add a person to the database." +
                                " Make sure you don't already have such record in " +
                                "the list.").showError();
                return;
            }
        }

        for(Tab tab : tabPane.getTabs())
        {
            if(tab.getContent() instanceof TableViewCustomControl)
            {
                TableViewCustomControl table = (TableViewCustomControl) tab.getContent();
                if(tab.getText().equals("All people"))
                    table.getItems().add(p);
            }
        }

        Tab tab = tabPane.getSelectionModel().getSelectedItem();
        if(tab.getText().equals("All people"))
        {
            TableViewCustomControl table = (TableViewCustomControl)tab.getContent();
            table.scrollTo(p);
            table.getSelectionModel().select(p);
        }
    }

    @FXML
    private void showAllPeopleClicked(ActionEvent actionEvent)
    {
        Tab tab = new Tab("All people");
        TableViewCustomControl c = new TableViewCustomControl(peopleDAO.findAllPeople(),
                        deletePersonBtn, addChildrenBtn, browseChildrenBtn);
        tab.setContent(c);
        tabPane.getTabs().add(tab);

        tabPane.getSelectionModel().select(tab);

        c.setOnMouseClicked((evt) ->
        {
            if (evt.getClickCount() == 2 && evt.getButton() == MouseButton.PRIMARY)
            {
                Person p = c.getSelectionModel().getSelectedItem();
                AddNewPersonDialog dialog = new AddNewPersonDialog(primaryStage,
                        "Change person data", countries, p);
                Action result = dialog.showDialog();
                if(result == dialog.getSubmitAction())
                {
                    update(p, dialog.getPerson());
                    peopleDAO.updatePerson(p);

                    for(Tab tb : tabPane.getTabs())
                    {
                        if(tab.getText().equals("All people"))
                        {
                            TableViewCustomControl table = (TableViewCustomControl)tb.getContent();

                            int idx = table.getSelectionModel().getSelectedIndex();
                            ObservableList<Person> items = FXCollections.observableArrayList();
                            FXCollections.copy(table.getItems(), items);
                            table.getItems().removeAll(items);
                            table.getItems().addAll(items);
                            table.getSelectionModel().select(idx);
                        }
                    }
                }

            }
        });
    }

    private void update(Person p, Person person)
    {
        p.setFirstname(person.getFirstname());
        p.setLastname(person.getLastname());
        p.setBirthDate(person.getBirthDate());
        p.setPassportId(person.getPassportId());
        p.setBirthPlace(person.getBirthPlace());
        p.setCitizenship(person.getCitizenship());
        p.setGender(person.getGender());
    }

    @FXML
    private void addChildrenClicked(ActionEvent actionEvent)
    {
        TableViewCustomControl table = (TableViewCustomControl)tabPane.getSelectionModel()
                .getSelectedItem().getContent();
        Person p = table.getSelectionModel().getSelectedItem();

        AddChildrenToPersonDialog dialog = new AddChildrenToPersonDialog(primaryStage,
                "Adding children to person", p, peopleDAO, countries);
        if(dialog.showDialog() == dialog.getSubmitAction())
        {
            List<Person> children = dialog.getNewChildren();
            peopleDAO.addChildrenToPerson(p, children);
        }
    }

    @FXML
    private void browseChildrenClicked(ActionEvent actionEvent)
    {
        TableViewCustomControl table = (TableViewCustomControl) tabPane.getSelectionModel()
                .getSelectedItem().getContent();
        Person p = table.getSelectionModel().getSelectedItem();
        TreeCustomControl tree = new TreeCustomControl(p, peopleDAO);

        Tab tab = new Tab(p.getLastname() + ", " + p.getFirstname().charAt(0) + ". children");
        tab.setContent(tree);
        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tab);
    }

    @FXML
    private void deletePersonClicked(ActionEvent actionEvent)
    {
        TableViewCustomControl table = (TableViewCustomControl)tabPane.getSelectionModel()
                .getSelectedItem().getContent();
        Person p = table.getSelectionModel().getSelectedItem();

        List<Person> relatives = peopleDAO.findChildren(p);
        relatives.addAll(peopleDAO.findParents(p));

        boolean delete = true;
        if(relatives.size() != 0)
        {
            Action a = Dialogs.create().title("Dependent people")
                    .message("There are relatives, such as children and parents, to this person. " +
                            "They will not be deleted, but any connections between them and " +
                            "this person would be lost.").showConfirm();
            if(a != Dialog.Actions.YES)
                delete = false;
        }

        if(delete)
        {
            peopleDAO.deletePerson(p);

            for(Tab tab : tabPane.getTabs())
            {
                if(tab.getContent() instanceof TableViewCustomControl)
                {
                    table = (TableViewCustomControl) tab.getContent();
                    if(tab.getText().equals("All people"))
                        table.getItems().remove(p);
                }
            }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        initPeopleDAO();

        showAllPeopleClicked(null);

        this.countries = peopleDAO.findAllCountries();

        tabPane.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) ->
                {
                    if(newVal != null && (newVal.getContent() instanceof TableViewCustomControl))
                    {
                        TableViewCustomControl table = (TableViewCustomControl)newVal.getContent();
                        if(table == null) return;

                        boolean disable = table.getSelectionModel().getSelectedItem() == null;
                        deletePersonBtn.setDisable(disable);
                        addChildrenBtn.setDisable(disable);
                        browseChildrenBtn.setDisable(disable);
                    }
                    else
                    {
                        deletePersonBtn.setDisable(true);
                        addChildrenBtn.setDisable(true);
                        browseChildrenBtn.setDisable(true);
                    }
                }
        );
    }
}









