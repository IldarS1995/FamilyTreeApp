package ru.kpfu.db.ildar.view;

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
import org.springframework.transaction.CannotCreateTransactionException;
import ru.kpfu.db.ildar.dao.PeopleDAO;
import ru.kpfu.db.ildar.pojos.Person;
import ru.kpfu.db.ildar.view.customcontrols.*;
import ru.kpfu.db.ildar.view.dialogs.*;

import java.net.URL;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.ResourceBundle;

/** Controller of the main window - initializes and runs the application */
public class Controller implements Initializable
{
    public static Stage primaryStage;

    /** List of countries from the database */
    private List<String> countries;

    /** Pane where tables and tree controls are placed in tabs */
    @FXML
    private TabPane tabPane;
    /** Delete person button */
    @FXML
    public Button deletePersonBtn;
    /** Add children button */
    @FXML
    private Button addChildrenBtn;
    /** Browse person's children button */
    @FXML
    private Button browseChildrenBtn;
    /** Browse person's parents button */
    @FXML
    private Button browseParentsBtn;

    /** Interface that gives connection to the database */
    private PeopleDAO peopleDAO;

    /** Exit from the application */
    @FXML
    private void onExit(ActionEvent evt)
    {
        System.exit(0);
    }

    /** Initialize DAO interface */
    private void initPeopleDAO()
    {
        //Spring framework JDBC templates is used
        ConfigurableApplicationContext context =
                new ClassPathXmlApplicationContext("spring-beans.xml");
        peopleDAO = context.getBean(PeopleDAO.class);
        context.close();
    }

    /** Add new person button clicked */
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

    /** Adding person to the database and processing exceptions that occured */
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

    /** Browse parents button clicked */
    @FXML
    private void browseParentsBtnClicked(ActionEvent actionEvent)
    {
        Tab tab = tabPane.getSelectionModel().getSelectedItem();
        TableViewCustomControl c = (TableViewCustomControl)tab.getContent();
        Person p = c.getSelectionModel().getSelectedItem();

        String name = p.getLastname() + ", " + p.getFirstname().charAt(0) + ".";
        BrowseParentsDialog dialog = new BrowseParentsDialog(primaryStage,
                "Browse " + name + " parents", p, peopleDAO, countries);
        dialog.showDialog();
    }

    /** 'All people' button clicked - show tab with all people from the database */
    @FXML
    private void showAllPeopleClicked(ActionEvent actionEvent)
    {
        Tab tab = new Tab("All people");
        TableViewCustomControl c;
        try
        {
            c = new TableViewCustomControl(peopleDAO.findAllPeople(), deletePersonBtn,
                    addChildrenBtn, browseChildrenBtn, browseParentsBtn);
        }
        catch(CannotCreateTransactionException exc)
                //It seems that the database is off
        {
            Dialogs.create().title("Error: Couldn't connect")
                    .message("Oracle database is not available. " +
                            "Please make sure it's on.").showError();
            return;
        }

        tab.setContent(c);
        tabPane.getTabs().add(tab);

        tabPane.getSelectionModel().select(tab);

        final TableViewCustomControl v = c;
        c.setOnMouseClicked((evt) ->
                //Double mouse click on person - open dialog for person data modification
        {
            if (evt.getClickCount() == 2 && evt.getButton() == MouseButton.PRIMARY)
            {
                Person p = v.getSelectionModel().getSelectedItem();
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

    /** Assign new values to person fields */
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

    /** Add children to the person button clicked - open dialog for children adding */
    @FXML
    private void addChildrenClicked(ActionEvent actionEvent)
    {
        TableViewCustomControl table = (TableViewCustomControl)tabPane.getSelectionModel()
                .getSelectedItem().getContent();
        Person p = table.getSelectionModel().getSelectedItem();

        AddChildrenDialog dialog = new AddChildrenDialog(primaryStage,
                "Adding children to person", p, peopleDAO, countries);
        if(dialog.showDialog() == dialog.getSubmitAction())
        {
            List<Person> children = dialog.getNewChildren();
            peopleDAO.addChildrenToPerson(p, children);
        }
    }

    /** Browse person's children - tree view control will be opened in new tab */
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

    /** Find people button clicked - dialog for people search by some criteria is opened */
    @FXML
    private void findPeopleClicked(ActionEvent actionEvent)
    {
        SearchPeopleDialog dialog = new SearchPeopleDialog(primaryStage,
                "Search people", peopleDAO, countries);
        dialog.showDialog();
        if(dialog.isSearch())
        {
            List<Person> foundPeople = dialog.getFoundPeople();

            Tab tab = new Tab();
            tab.setText("Found people");
            TableViewCustomControl table = new TableViewCustomControl(foundPeople,
                    deletePersonBtn, addChildrenBtn, browseChildrenBtn, browseParentsBtn);
            tab.setContent(table);
            tabPane.getTabs().add(tab);
            tabPane.getSelectionModel().select(tab);
        }
    }

    /** 'About' button clicked */
    @FXML
    private void aboutClicked(ActionEvent actionEvent)
    {
        Dialogs.create().title("About").message("Written by Ildar").showInformation();
    }

    /** Delete person button clicked */
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
            //There are relatives of this person - if user still wants this person to delete,
            //all connections with his relatives will also be deleted
        {
            Action a = Dialogs.create().title("Dependent people")
                    .message("There are relatives, such as children and parents, to this person. " +
                            "They will not be deleted, but any connections between them and " +
                            "this person would be lost.").showConfirm();
            if(a != Dialog.Actions.YES)
                delete = false;
        }

        if(delete)
            //User chose to delete person
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

    /** Initialize the application - connection to the database, user interface etc. */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        //initialize DAO interface
        initPeopleDAO();

        //Open one tab with all people from the DB
        showAllPeopleClicked(null);

        //Fetch countries from the DB
        this.countries = peopleDAO.findAllCountries();

        //Configure buttons dependent on the person selection in table -
        //disable them when no person is selected and enable otherwise
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
                        browseParentsBtn.setDisable(disable);
                    }
                    else
                    {
                        deletePersonBtn.setDisable(true);
                        addChildrenBtn.setDisable(true);
                        browseChildrenBtn.setDisable(true);
                        browseParentsBtn.setDisable(true);
                    }
                }
        );
    }
}









