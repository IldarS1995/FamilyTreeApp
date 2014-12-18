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

/** Dialog that is used in AddChildrenDialog - gives ability to select people
 * from the table and makes some data checks */
public class SelectPeopleFromTableDialog extends Dialog
{
    /** People that user has to select from */
    private List<Person> people;

    private Action submitAction;
    /** Button that indicates 'Submit' action */
    public Action getSubmitAction() { return submitAction; }

    private List<Person> selectedPeople;
    /** Returns list of people that were selected by user */
    public List<Person> getSelectedPeople() { return selectedPeople; }

    /** Interface that gives connection to the database */
    private PeopleDAO peopleDAO;

    /** This person is presumed to be parent of the selected people. Needed for
     * some data consistency checks */
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

    /** Open this dialog */
    public Action showDialog()
    {
        //Root control
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
                    //Checks if a child has already two parents
                    return;
                if(!checkExistingParentGender(table))
                    //Checks if a child's parents will have the same gender
                    return;
                if(parentIsChildOfOtherParent(table))
                    //Checks if one parent of a child is a descendant of the other parent
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
        this.setResizable(false);
        this.getActions().addAll(Actions.CANCEL, submitAction);
        this.setContent(pane);
        return this.show();
    }

    /** Checks if one parent of a child is a descendant of the other parent */
    private boolean parentIsChildOfOtherParent(TableViewCustomControl table)
    {
        List<Person> parChildren = peopleDAO.findChildren(parent);

        List<Person> selectedPeople = table.getSelectionModel().getSelectedItems();
        StringBuilder sb = new StringBuilder();
        for(Person child : selectedPeople)
        {
            List<Person> parents = peopleDAO.findParents(child);
            if(parents.size() == 0)
                continue;

            Person parent1 = parents.get(0);
            List<Person> par1Children = peopleDAO.findChildren(parent1);
            if(par1Children.contains(parent) || parChildren.contains(parent1))
                sb.append(child.getFirstname() + " " + child.getLastname() + ", " +
                        child.getPassportId() + "\n");
        }

        if(sb.length() != 0)
        {
            Dialogs.create().title("Error: one parent is a descendant of another")
                    .message("There are children selected who already have a parent and he" +
                            " is either the descendant, or the parent of the parent you" +
                            " want to make this child's parent: \n"
                            + sb.toString()).showError();
            return true;
        }
        else
            return false;
    }

    /** Checks if a child's parents will have the same gender */
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

    /** Checks if a child has already two parents */
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
