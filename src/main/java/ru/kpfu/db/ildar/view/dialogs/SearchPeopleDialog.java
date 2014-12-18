package ru.kpfu.db.ildar.view.dialogs;

import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.Dialogs;
import org.springframework.dao.EmptyResultDataAccessException;
import ru.kpfu.db.ildar.dao.PeopleDAO;
import ru.kpfu.db.ildar.pojos.Person;

import java.time.LocalDate;
import java.util.*;

/** Dialog that gives ability to search people in the database by certain criteria */
public class SearchPeopleDialog extends Dialog
{
    @FXML
    private Button nameAddBtn;
    @FXML
    private Button passIdAddBtn;

    @FXML
    private TextField firstnameField;
    @FXML
    private TextField lastnameField;
    @FXML
    private DatePicker fromBirthDatePicker;
    @FXML
    private DatePicker toBirthDatePicker;

    @FXML
    private TextField passIdField;
    @FXML
    private ComboBox<String> citizenshipField;

    private List<Person> foundPeople;
    /** List of found people */
    public List<Person> getFoundPeople()
    {
        return foundPeople;
    }

    /** Given connection to the database */
    private PeopleDAO peopleDAO;
    /** List of countries from the DB */
    private List<String> countries;

    private boolean search;
    /** True, if search was performed successfully */
    public boolean isSearch() { return search; }

    public SearchPeopleDialog(Object owner, String title, PeopleDAO peopleDAO,
                              List<String> countries)
    {
        super(owner, title);

        this.peopleDAO = peopleDAO;
        this.countries = countries;
    }

    /** Open the dialog */
    public Action showDialog()
    {
        //Root dialog control taken from FXML file
        HBox box = getPane();

        //Set countries list in the citizenship combobox
        citizenshipField.setItems(FXCollections.observableArrayList(countries));
        citizenshipField.getSelectionModel().select(0);

        //Disable search by name and birth button if all values are empty,
        //or both start birth date and end birth date are specified and start birth date
        //is later than end birth date
        nameAddBtn.disableProperty().bind(new BooleanBinding()
        {
            {
                super.bind(firstnameField.textProperty(), lastnameField.textProperty(),
                        fromBirthDatePicker.valueProperty(), toBirthDatePicker.valueProperty());
            }

            @Override
            protected boolean computeValue()
            {
                if(firstnameField.getText().trim().length() == 0
                        && lastnameField.getText().trim().length() == 0
                        && fromBirthDatePicker.getValue() == null
                        && toBirthDatePicker.getValue() == null)
                    return true;
                else
                {
                    if(fromBirthDatePicker.getValue() != null && toBirthDatePicker.getValue() != null
                            && fromBirthDatePicker.getValue().compareTo(toBirthDatePicker.getValue()) >= 0)
                        return true;
                    else
                        return false;
                }
            }
        });

        //Disable search by passport ID and citizenship button if passport ID field is empty
        passIdAddBtn.disableProperty().bind(new BooleanBinding()
        {
            { super.bind(passIdField.textProperty()); }

            @Override
            protected boolean computeValue()
            {
                return passIdField.getText().length() == 0;
            }
        });

        //Allow entering in passport ID field only numerical values
        passIdField.textProperty().addListener((obs, oldVal, newVal) ->
        {
            if(!newVal.matches("\\d+") && !newVal.equals(""))
                passIdField.setText(oldVal);
        });

        this.getActions().addAll(Actions.CANCEL);
        this.setResizable(false);
        this.setContent(box);
        this.setResizable(false);
        this.setGraphic(new ImageView(getClass().getClassLoader()
                .getResource("images/search_person.png").toString()));
        return this.show();
    }

    /** Add by name and birth date button clicked */
    @FXML
    private void nameAddBtnClicked(ActionEvent evt)
    {
        search = true;

        String firstName = firstnameField.getText();
        String lastName = lastnameField.getText();

        Date birthDateFrom = null;
        Date birthDateTo = null;

        if(fromBirthDatePicker.getValue() != null)
            //if start birth date not null, fetch it from date picker
        {
            LocalDate lDate = fromBirthDatePicker.getValue();
            Calendar c = new GregorianCalendar(lDate.getYear(),
                    lDate.getMonthValue() - 1, lDate.getDayOfMonth());

            birthDateFrom = c.getTime();
        }
        if(toBirthDatePicker.getValue() != null)
            //if end birth date not null, fetch it from date picker
        {
            LocalDate lDate = toBirthDatePicker.getValue();
            Calendar c = new GregorianCalendar(lDate.getYear(),
                    lDate.getMonthValue() - 1, lDate.getDayOfMonth());
            birthDateTo = c.getTime();
        }

        this.foundPeople = peopleDAO.findPeopleByDynamicCriteria(firstName,
                lastName, birthDateFrom, birthDateTo);

        this.hide();
    }

    /** Add by passport ID and citizenship button clicked. Only one person can be found
     * using this method since passport ID and citizenship combined are unique in the database. */
    @FXML
    private void passIdAddBtnClicked(ActionEvent evt)
    {
        search = true;

        int passportId = Integer.parseInt(passIdField.getText());
        String citizenship = citizenshipField.getSelectionModel().getSelectedItem();

        try
        {
            this.foundPeople = Arrays.asList(peopleDAO.findPersonByPassportIDAndCitizenship
                    (passportId, citizenship));

            this.hide();
        }
        catch(EmptyResultDataAccessException exc)
                //Person not found
        {
            Dialogs.create().title("Error: not found")
                    .message("Couldn't find a person with such passport ID and citizenship.")
                    .showError();

            search = false;
        }
    }

    /** Get root control from FXML file */
    private HBox getPane()
    {
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader()
                .getResource("customwindows/searchperson.fxml"));

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
