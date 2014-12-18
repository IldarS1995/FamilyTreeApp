package ru.kpfu.db.ildar.view.dialogs;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import ru.kpfu.db.ildar.dao.PeopleDAO;
import ru.kpfu.db.ildar.pojos.Person;
import ru.kpfu.db.ildar.view.customcontrols.TableViewCustomControl;

import java.util.Arrays;
import java.util.List;

/** Dialog that gives ability to browse person's parents, delete them and add new parents */
public class BrowseParentsDialog extends Dialog
{
    @FXML
    private Button addParentBtn;
    @FXML
    private Button removeParentBtn;
    @FXML
    private TableViewCustomControl table;

    /**Gives connection to the database*/
    private PeopleDAO peopleDAO;
    /** Child whose parents dialog needs to handle */
    private Person child;
    /** List of countries from the database */
    private List<String> countries;

    public BrowseParentsDialog(Object owner, String title, Person child, PeopleDAO peopleDAO,
                               List<String> countries)
    {
        super(owner, title);
        this.peopleDAO = peopleDAO;
        this.child = child;
        this.countries = countries;
    }

    /** Open this dialog */
    public Action showDialog()
    {
        //Root control taken from FXML file
        VBox pane = getPane();

        //Set label in the table if not parents found
        table.setPlaceholder(new Label("No parents found."));

        //Fetch all parents from the database and place them in the table
        List<Person> parents = peopleDAO.findParents(child);
        table.setItems(FXCollections.observableArrayList(parents));

        //If no person is selected in the table, disable the remove button
        table.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> removeParentBtn.setDisable(newVal == null)
        );

        //If child has already two parents, disable adding new parent
        if(peopleDAO.checkFullParents(child))
            addParentBtn.setDisable(true);

        this.setResizable(false);
        this.setContent(pane);
        this.getActions().add(Actions.CLOSE);
        return this.show();
    }

    /** Add new parent button clicked - open person search dialog */
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

    /** Remove parent button clicked - remove parent and child connection from database */
    @FXML
    private void removeParentBtnClicked(ActionEvent evt)
    {
        Person selParent = table.getSelectionModel().getSelectedItem();
        peopleDAO.deletePeopleRelations(selParent, child);
        table.getItems().remove(selParent);
    }

    /** Get root control from FXML file */
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
