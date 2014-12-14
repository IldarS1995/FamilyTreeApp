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

public class PeopleSearchDialog extends Dialog
{
    private Action submitAction;
    public Action getSubmitAction() { return submitAction; }

    private List<Person> existingChildren;

    private List<Person> newChildren = new ArrayList<>();
    public List<Person> getNewChildren() { return newChildren; }

    private PeopleDAO peopleDAO;
    private Object owner;
    private List<String> countries;
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

    public PeopleSearchDialog(Object owner, String title, Person parent, PeopleDAO peopleDAO, List<String> countries)
    {
        super(owner, title);
        this.peopleDAO = peopleDAO;
        this.owner = owner;
        this.countries = countries;

        this.existingChildren = peopleDAO.findChildren(parent);
        this.parent = parent;
    }

    @FXML
    private void nameAddBtnClicked(ActionEvent evt)
    {
        String firstname = firstnameField.getText(), lastname = lastnameField.getText();
        List<Person> people = peopleDAO.findPeopleByName(firstname, lastname, parent.getBirthDate());

        SelectPeopleFromTableDialog dial = new SelectPeopleFromTableDialog(owner, "Select children",
                people, this.existingChildren, parent, peopleDAO);
        if(dial.showDialog() == dial.getSubmitAction())
        {
            people = dial.getSelectedPeople();
            newChildren.addAll(people);
            table.getItems().addAll(people);
        }
    }

    @FXML
    private void passIdAddBtnClicked(ActionEvent evt)
    {
        int passportId = Integer.parseInt(passIdField.getText());
        String citizenship = citizenshipField.getValue();

        try
        {
            Person child = peopleDAO.findPersonByPassportIDAndCitizenship(passportId, citizenship);
            if(peopleDAO.checkFullParents(child))
            {
                Dialogs.create().title("Error: child has full parents")
                        .message("This child has already two parents").showError();
                return;
            }

            List<Person> parents = peopleDAO.findParents(child);
            if(parents.size() == 1 && parents.get(0).getGender() == parent.getGender())
            {
                Dialogs.create().title("Error: child has the parent with the same gender")
                        .message("This child already has a parent with the same gender.").showError();
                return;
            }

            Person parent1 = parents.get(0);
            List<Person> par1Children = peopleDAO.findChildren(parent1);
            List<Person> parChildren = peopleDAO.findChildren(parent);
            if(par1Children.contains(parent) || parChildren.contains(parent1))
            {
                Dialogs.create().title("Error: one parent is descendant of the other")
                        .message("One of the parents is either the descendant, or the " +
                                "parent of the other one.").showError();
                return;
            }

            if(child.getBirthDate().compareTo(parent.getBirthDate()) < 0)
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
        {
            Dialogs.create().title("Person finding error")
                    .message("Couldn't find a person with such passport ID and citizenship")
                    .showError();
        }
    }

    public Action showDialog()
    {
        HBox pane = getPane();
        citizenshipField.setItems(FXCollections.observableArrayList(countries));
        citizenshipField.getSelectionModel().select(0);

        nameAddBtn.disableProperty().bind(new BooleanBinding()
        {
            { super.bind(firstnameField.textProperty(), lastnameField.textProperty()); }

            @Override
            protected boolean computeValue()
            {
                return firstnameField.getText().length() == 0 ||
                        lastnameField.getText().length() == 0;
            }
        });

        passIdAddBtn.disableProperty().bind(new BooleanBinding()
        {
            { super.bind(passIdField.textProperty()); }

            @Override
            protected boolean computeValue()
            {
                return passIdField.getText().length() == 0;
            }
        });

        passIdField.textProperty().addListener((obs, oldVal, newVal) ->
        {
            if(!newVal.matches("\\d+") && !newVal.equals(""))
                passIdField.setText(oldVal);
        });

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
