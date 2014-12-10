package ru.kpfu.db.ildar.view.dialogs;

import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import org.controlsfx.control.ButtonBar;
import org.controlsfx.control.action.AbstractAction;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
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

    public SelectPeopleFromTableDialog(Object owner, String title, List<Person> people,
                                       List<Person> usedPeople, Person parent)
    {
        super(owner, title);
        this.people = people;

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
}
