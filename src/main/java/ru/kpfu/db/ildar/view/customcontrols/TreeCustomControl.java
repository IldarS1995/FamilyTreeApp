package ru.kpfu.db.ildar.view.customcontrols;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import ru.kpfu.db.ildar.dao.PeopleDAO;
import ru.kpfu.db.ildar.pojos.Person;

import java.io.IOException;
import java.util.List;

public class TreeCustomControl extends TreeView<Person>
{
    private PeopleDAO peopleDAO;

    public TreeCustomControl(Person rootPerson, PeopleDAO peopleDAO)
    {
        this.peopleDAO = peopleDAO;

        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader()
                .getResource("customwindows/treecustom.fxml"));
        loader.setController(this);
        loader.setRoot(this);

        try
        {
            loader.load();
        }
        catch(IOException exc) { throw new RuntimeException(exc); }

        TreeItem<Person> trRoot = new TreeItem<>(rootPerson);
        trRoot.setExpanded(true);
        this.setRoot(trRoot);

        this.findPersonChildren(trRoot);

        this.setOnKeyPressed(this::onKeyPressed);
    }

    private void onKeyPressed(KeyEvent evt)
    {
        TreeItem<Person> item = this.getSelectionModel().getSelectedItem();
        if(evt.getCode() == KeyCode.DELETE)
        {
            Person parent = item.getParent().getValue();
            Person child = item.getValue();

            peopleDAO.deletePeopleRelations(parent, child);
            item.getParent().getChildren().remove(item);
        }
    }

    private void findPersonChildren(TreeItem<Person> trRoot)
    {
        Person p = trRoot.getValue();
        List<Person> children = peopleDAO.findChildren(p);

        for(Person child : children)
        {
            final TreeItem<Person> item = new TreeItem<>(child);
            trRoot.getChildren().add(item);
            item.setExpanded(false);
            item.getChildren().add(new TreeItem<>(new Person(-1, null, null, null, -1, null, null, 'c')));
            item.expandedProperty().addListener(
                    (obs, oldVal, newVal) ->
                    {
                        if(newVal)
                        {
                            if(item.getChildren().size() == 1)
                            {
                                TreeItem<Person> tempChild = item.getChildren().get(0);
                                if(tempChild.getValue().getPersonId() == -1)
                                {
                                    item.getChildren().remove(0);
                                    findPersonChildren(item);
                                }
                            }
                        }
                    }
            );
        }
    }
}
