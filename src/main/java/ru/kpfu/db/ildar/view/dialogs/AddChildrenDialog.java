package ru.kpfu.db.ildar.view.dialogs;

import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import org.controlsfx.control.ButtonBar;
import org.controlsfx.control.action.AbstractAction;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.Dialogs;
import org.springframework.dao.EmptyResultDataAccessException;
import ru.kpfu.db.ildar.dao.PeopleDAO;
import ru.kpfu.db.ildar.pojos.Person;
import ru.kpfu.db.ildar.view.customcontrols.TableViewCustomControl;

import java.util.ArrayList;
import java.util.List;

/** Dialog that gives ability to find and add children to some person */
public class AddChildrenDialog extends Dialog
{
    private Action submitAction;
    /** Submit action button is 'OK' button */
    public Action getSubmitAction() { return submitAction; }

    /** List of children this person already has */
    private List<Person> existingChildren;

    private List<Person> newChildren = new ArrayList<>();
    /** Added children */
    public List<Person> getNewChildren() { return newChildren; }

    /** Object to communicate with database */
    private PeopleDAO peopleDAO;
    /** Dialog owner(usually other Window object) */
    private Object owner;
    /** All countries from database */
    private List<String> countries;
    /** Person for whom we will be searching and adding children to */
    private Person parent;

    @FXML
    private TextField firstnameField;
    @FXML
    private TextField lastnameField;
    @FXML
    private TextField passIdField;
    @FXML
    private ComboBox<String> citizenshipField;
    @FXML
    private TableViewCustomControl table;

    @FXML
    private Button nameAddBtn;
    @FXML
    private Button passIdAddBtn;

    public AddChildrenDialog(Object owner, String title, Person parent, PeopleDAO peopleDAO, List<String> countries)
    {
        super(owner, title);
        this.peopleDAO = peopleDAO;
        this.owner = owner;
        this.countries = countries;

        this.existingChildren = peopleDAO.findChildren(parent);
        this.parent = parent;
    }

    /** Search children by first name and last name */
    @FXML
    private void nameAddBtnClicked(ActionEvent evt)
    {
        String firstname = firstnameField.getText(), lastname = lastnameField.getText();
        List<Person> people = peopleDAO.findPeopleByDynamicCriteria(firstname,
                lastname, parent.getBirthDate(), null);

        SelectPeopleFromTableDialog dial = new SelectPeopleFromTableDialog(owner, "Select children",
                people, this.existingChildren, parent, peopleDAO);
        if(dial.showDialog() == dial.getSubmitAction())
        {
            people = dial.getSelectedPeople();
            newChildren.addAll(people);
            table.getItems().addAll(people);
        }
    }

    /** Search child by passport ID and citizenship */
    @FXML
    private void passIdAddBtnClicked(ActionEvent evt)
    {
        int passportId = Integer.parseInt(passIdField.getText());
        String citizenship = citizenshipField.getValue();

        try
        {
            Person child = peopleDAO.findPersonByPassportIDAndCitizenship(passportId, citizenship);
            if(peopleDAO.checkFullParents(child))
                //Child has already full parents
            {
                Dialogs.create().title("Error: child has full parents")
                        .message("This child has already two parents").showError();
                return;
            }

            List<Person> parents = peopleDAO.findParents(child);
            if(parents.size() == 1 && parents.get(0).getGender() == parent.getGender())
                //Attempt to add a parent with the sam gender as of existing child's parent
            {
                Dialogs.create().title("Error: child has the parent with the same gender")
                        .message("This child already has a parent with the same gender.").showError();
                return;
            }

            Person parent1 = parents.get(0);
            List<Person> par1Children = peopleDAO.findChildren(parent1);
            List<Person> parChildren = peopleDAO.findChildren(parent);
            if(par1Children.contains(parent) || parChildren.contains(parent1))
                //Attempt to make child's parent a person that is a descendant or a parent
                //of existing parent
            {
                Dialogs.create().title("Error: one parent is descendant of the other")
                        .message("One of the parents is either the descendant, or the " +
                                "parent of the other one.").showError();
                return;
            }

            if(child.getBirthDate().compareTo(parent.getBirthDate()) < 0)
                //Attempt to make child's parent a person that is younger than the child
            {
                Dialogs.create().title("Person adding error")
                        .message("You can't select a child that is older than you.")
                        .showError();
                return;
            }

            table.getItems().add(child);
            table.scrollTo(child);
            table.getSelectionModel().select(child);
            newChildren.add(child);
        }
        catch(EmptyResultDataAccessException exc)
                //Child not found
        {
            Dialogs.create().title("Person finding error")
                    .message("Couldn't find a person with such passport ID and citizenship")
                    .showError();
        }
    }

    /** Open the dialog */
    public Action showDialog()
    {
        HBox pane = getPane();

        //Set countries to the combo box
        citizenshipField.setItems(FXCollections.observableArrayList(countries));
        citizenshipField.getSelectionModel().select(0);

        table.setItems(FXCollections.observableArrayList(existingChildren));

        //Disable nameAddBtn button when first name and last name fields are empty
        //and enable otherwise
        nameAddBtn.disableProperty().bind(new BooleanBinding()
        {
            {
                super.bind(firstnameField.textProperty(), lastnameField.textProperty());
            }

            @Override
            protected boolean computeValue()
            {
                return firstnameField.getText().length() == 0 || lastnameField.getText().length() == 0;
            }
        });

        //Disable passIdAddBtn button when passport ID field is empty.
        //Citizenship field won't be null since user can't choose an empty value.
        passIdAddBtn.disableProperty().bind(new BooleanBinding()
        {
            { super.bind(passIdField.textProperty()); }

            @Override
            protected boolean computeValue()
            {
                return passIdField.getText().length() == 0;
            }
        });

        //Allow entering only numbers in passport ID field
        passIdField.textProperty().addListener((obs, oldVal, newVal) ->
        {
            if(!newVal.matches("\\d+") && !newVal.equals(""))
                passIdField.setText(oldVal);
        });

        //When person clicks 'Submit', close the dialog
        submitAction = new AbstractAction("Submit")
        {
            @Override
            public void handle(ActionEvent actionEvent)
            {
                Dialog d = (Dialog)actionEvent.getSource();
                d.hide();
            }
        };

        this.setContent(pane);
        this.setResizable(false);
        ButtonBar.setType(submitAction, ButtonBar.ButtonType.OK_DONE);
        this.getActions().addAll(Actions.CANCEL, submitAction);
        this.setGraphic(new ImageView(getClass().getClassLoader()
                .getResource("images/add_children.png").toString()));

        return this.show();
    }

    /** Load root control from FXML file */
    private HBox getPane()
    {
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader()
                .getResource("customwindows/addchildrentoperson.fxml"));

        HBox box = new HBox();
        loader.setRoot(box);
        loader.setController(this);

        try
        {
            loader.load();
        }
        catch(Exception exc) { throw new RuntimeException(exc); }

        return box;
    }
}
