package ru.kpfu.db.ildar.view.dialogs;

import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import org.controlsfx.control.ButtonBar;
import org.controlsfx.control.action.AbstractAction;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.Dialogs;
import ru.kpfu.db.ildar.dao.PeopleDAO;
import ru.kpfu.db.ildar.pojos.Person;
import ru.kpfu.db.ildar.view.customcontrols.TableViewCustomControl;

import java.util.List;

public class SelectPeopleFromTableDialog extends Dialog
{
    private List<Person> people;

    private Action submitAction;
    public Action getSubmitAction() { return submitAction; }

    private List<Person> selectedPeople;
    public List<Person> getSelectedPeople() { return selectedPeople; }

    private PeopleDAO peopleDAO;

    private Person parent;

    public SelectPeopleFromTableDialog(Object owner, String title, List<Person> people,
                                       List<Person> usedPeople, Person parent, PeopleDAO peopleDAO)
    {
        super(owner, title);
        this.people = people;
        this.peopleDAO = peopleDAO;
        this.parent = parent;

        usedPeople.stream().forEach(people::remove);
    }

    public Action showDialog()
    {
        GridPane pane = new GridPane();
        pane.setVgap(10);
        pane.add(new Label("Select people you want be the person's children:"), 0, 0);
        TableViewCustomControl table = new TableViewCustomControl(people);
        pane.add(table, 0, 1);

        submitAction = new AbstractAction("Select")
        {
            @Override
            public void handle(ActionEvent actionEvent)
            {
                if(!checkFullParents(table))
                    return;
                if(!checkExistingParentGender(table))
                    return;

                selectedPeople = table.getSelectionModel().getSelectedItems();

                Dialog d = (Dialog)actionEvent.getSource();
                d.hide();
            }
        };

        table.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> submitAction.disabledProperty().set(newVal == null)
        );

        ButtonBar.setType(submitAction, ButtonBar.ButtonType.OK_DONE);
        this.getActions().addAll(Actions.CANCEL, submitAction);
        this.setContent(pane);
        return this.show();
    }

    private boolean checkExistingParentGender(TableViewCustomControl table)
    {
        List<Person> selectedPeople = table.getSelectionModel().getSelectedItems();
        StringBuilder sb = new StringBuilder();
        for(Person child : selectedPeople)
        {
            List<Person> parents = peopleDAO.findParents(child);
            if(parents.size() == 0)
                continue;
            Person parent1 = parents.get(0);
            if(parent1.getGender() == parent.getGender())
                sb.append(child.getFirstname() + " " + child.getLastname() + ", " +
                        child.getPassportId() + "\n");
        }

        if(sb.length() != 0)
        {
            Dialogs.create().title("Error: children with a parent with the same gender found")
                    .message("There are children selected who already have a parent and he" +
                            " has the same gender as the person you try to make child's parent: \n"
                            + sb.toString()).showError();
            return false;
        }
        else
            return true;
    }

    private boolean checkFullParents(TableViewCustomControl table)
    {
        List<Person> selectedPeople = table.getSelectionModel().getSelectedItems();
        StringBuilder sb = new StringBuilder();
        for(Person child : selectedPeople)
        {
            if(peopleDAO.checkFullParents(child))
                sb.append(child.getFirstname() + " " + child.getLastname() +
                        ", " + child.getPassportId() + "\n");
        }

        if(sb.length() != 0)
        {
            Dialogs.create().title("Error: children with full parents found")
                    .message("There are children selected that already have two parents: \n"
                            + sb.toString()).showError();
            return false;
        }
        else
            return true;
    }
}
