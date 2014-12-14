package ru.kpfu.db.ildar.view.dialogs;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import ru.kpfu.db.ildar.dao.PeopleDAO;
import ru.kpfu.db.ildar.pojos.Person;
import ru.kpfu.db.ildar.view.customcontrols.TableViewCustomControl;

import java.util.Arrays;
import java.util.List;

public class BrowseParentsDialog extends Dialog
{
    @FXML
    private Button addParentBtn;
    @FXML
    private Button removeParentBtn;
    @FXML
    private TableViewCustomControl table;

    private PeopleDAO peopleDAO;
    private Person child;
    private List<String> countries;

    public BrowseParentsDialog(Object owner, String title, Person child, PeopleDAO peopleDAO,
                               List<String> countries)
    {
        super(owner, title);
        this.peopleDAO = peopleDAO;
        this.child = child;
        this.countries = countries;
    }

    public Action showDialog()
    {
        VBox pane = getPane();

        table.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> removeParentBtn.setDisable(newVal == null)
        );

        if(peopleDAO.checkFullParents(child))
            addParentBtn.setDisable(true);

        this.setResizable(false);
        this.setContent(pane);
        this.getActions().add(Actions.CLOSE);
        return this.show();
    }

    @FXML
    private void addParentBtnClicked(ActionEvent evt)
    {
        ParentSearchDialog dialog = new ParentSearchDialog(this.getWindow(), "Add a parent",
                child, peopleDAO, countries);
        if(dialog.showDialog() == dialog.getSubmitAction())
        {
            Person parent = dialog.getParent();
            peopleDAO.addChildrenToPerson(parent, Arrays.asList(child));
            table.getItems().add(parent);
        }
    }

    @FXML
    private void removeParentBtnClicked(ActionEvent evt)
    {
        Person selParent = table.getSelectionModel().getSelectedItem();
        peopleDAO.deletePeopleRelations(selParent, child);
        table.getItems().remove(selParent);
    }

    public VBox getPane()
    {
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader()
                .getResource("customwindows/browseparents.fxml"));

        VBox box = new VBox();
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
