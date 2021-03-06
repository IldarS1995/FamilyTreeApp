package ru.kpfu.db.ildar.view.dialogs;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import org.controlsfx.control.ButtonBar;
import org.controlsfx.control.action.AbstractAction;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.Dialogs;
import org.springframework.dao.EmptyResultDataAccessException;
import ru.kpfu.db.ildar.dao.PeopleDAO;
import ru.kpfu.db.ildar.pojos.Person;

import java.util.List;

public class ParentSearchDialog extends Dialog
{
    private Person parent;
    private Person child;
    private PeopleDAO peopleDAO;
    private List<String> countries;

    public Person getParent() { return parent; }

    private Action submitAction;
    public Action getSubmitAction() { return submitAction; }

    public ParentSearchDialog(Object owner, String title, Person child, PeopleDAO peopleDAO,
                              List<String> countries)
    {
        super(owner, title);

        this.child = child;
        this.peopleDAO = peopleDAO;
        this.countries = countries;
    }

    public Action showDialog()
    {
        GridPane pane = new GridPane();
        pane.setHgap(10); pane.setVgap(10);

        pane.add(new Label("Passport ID:"), 0, 0);
        pane.add(new Label("Citizenship:"), 0, 1);

        TextField passIdField = new TextField();
        ComboBox<String> citizenshipField = new ComboBox<>();

        citizenshipField.setItems(FXCollections.observableArrayList(countries));
        citizenshipField.getSelectionModel().select(0);

        pane.add(passIdField, 1, 0);
        pane.add(citizenshipField, 1, 1);

        submitAction = new AbstractAction("Add")
        {
            @Override
            public void handle(ActionEvent actionEvent)
            {
                try
                {
                    parent = peopleDAO.findPersonByPassportIDAndCitizenship
                            (Integer.parseInt(passIdField.getText()),
                             citizenshipField.getSelectionModel().getSelectedItem());
                }
                catch(EmptyResultDataAccessException exc)
                        //Generated if a person wasn't found
                {
                    Dialogs.create().title("Error: not found")
                            .message("The person with such passport ID and citizenship wasn't found.")
                            .showError();
                    return;
                }

                if(parent.getBirthDate().compareTo(child.getBirthDate()) >= 0)
                    //Enter person is younger than the child
                {
                    Dialogs.create().title("Error: birth order")
                            .message("Child can't be older than his parent")
                            .showError();
                    return;
                }

                List<Person> children = peopleDAO.findChildren(parent);
                if(children.contains(child))
                    //Specified person that is in the list
                {
                    Dialogs.create().title("Error: person is already parent of this child")
                            .message("The person specified is already parent of this child")
                            .showError();
                    return;
                }

                List<Person> parents = peopleDAO.findParents(child);
                if(parents.size() != 0)
                {
                    Person parent1 = parents.get(0);
                    if(parent.getGender() == parent1.getGender())
                        //Attempt to add a parent that has the same gender
                    {
                        Dialogs.create().title("Error: attempt to add person with same gender")
                                .message("Attempt to add a parent with the same gender as of" +
                                        " existing parent was made.").showError();
                        return;
                    }
                }

                Dialog dialog = (Dialog)actionEvent.getSource();
                dialog.hide();
            }
        };

        submitAction.disabledProperty().set(true);

        passIdField.textProperty().addListener((obs, oldVal, newVal) ->
        {
            if(!newVal.matches("\\d+") && !newVal.equals(""))
                passIdField.setText(oldVal);
        });

        passIdField.textProperty().addListener((obs, oldVal, newVal) ->
        {
            submitAction.disabledProperty().set(newVal.trim().length() == 0);
        });

        ButtonBar.setType(submitAction, ButtonBar.ButtonType.OK_DONE);
        this.setResizable(false);
        this.setContent(pane);
        this.getActions().addAll(Actions.CANCEL, submitAction);
        return this.show();
    }
}
