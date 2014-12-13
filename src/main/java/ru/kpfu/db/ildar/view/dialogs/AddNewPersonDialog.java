package ru.kpfu.db.ildar.view.dialogs;

import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import org.controlsfx.control.ButtonBar;
import org.controlsfx.control.action.AbstractAction;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import ru.kpfu.db.ildar.pojos.Person;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class AddNewPersonDialog extends Dialog
{
    private Action submitAction;
    private Person person;

    public Action getSubmitAction() { return submitAction; }
    public Person getPerson() { return person; }

    private List<String> countries;

    private int personId = 0;

    @FXML
    private TextField firstnameField;
    @FXML
    private TextField lastnameField;
    @FXML
    private DatePicker birthDateField;
    @FXML
    private TextField passportIdField;
    @FXML
    private TextField birthPlaceField;
    @FXML
    private ComboBox<String> citizenshipField;
    @FXML
    private ComboBox<Character> genderField;

    public AddNewPersonDialog(Object owner, String title, List<String> countries)
    {
        super(owner, title);

        this.countries = countries;
    }
    public AddNewPersonDialog(Object owner, String title, List<String> countries, Person person)
    {
        this(owner, title, countries);
        this.person = person;
    }

    public Action showDialog()
    {
        GridPane pane = getPane();

        citizenshipField.setItems(FXCollections.observableArrayList(countries));
        genderField.setItems(FXCollections.observableArrayList('m', 'f'));

        if(person == null)
        {
            genderField.getSelectionModel().select(0);
            citizenshipField.getSelectionModel().select(0);
        }
        else
        {
            genderField.getSelectionModel().select((Character)person.getGender());
            citizenshipField.getSelectionModel().select(person.getCitizenship());

            firstnameField.setText(person.getFirstname());
            lastnameField.setText(person.getLastname());

            Calendar c = Calendar.getInstance();
            c.setTime(person.getBirthDate());
            birthDateField.setValue(LocalDate.of(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH)));
            passportIdField.setText(String.valueOf(person.getPassportId()));
            birthPlaceField.setText(person.getBirthPlace());
        }

        submitAction = new AbstractAction(person == null ? "Submit" : "Update")
        {
            @Override
            public void handle(ActionEvent actionEvent)
            {
                LocalDate lDate = birthDateField.getValue();
                Calendar c = new GregorianCalendar(lDate.getYear(),
                        lDate.getMonthValue() - 1, lDate.getDayOfMonth());

                person = new Person(personId,
                        firstnameField.getText(),
                        lastnameField.getText(),
                        new Date(c.getTime().getTime()),
                        Integer.parseInt(passportIdField.getText()),
                        birthPlaceField.getText(),
                        citizenshipField.getValue(),
                        genderField.getValue());

                Dialog d = (Dialog)actionEvent.getSource();
                d.hide();
            }
        };

        submitAction.disabledProperty().set(true);

        submitAction.disabledProperty().bind(new BooleanBinding()
        {
            { super.bind(firstnameField.textProperty(), lastnameField.textProperty(),
                birthDateField.valueProperty(), passportIdField.textProperty(),
                birthPlaceField.textProperty(), citizenshipField.valueProperty(),
                genderField.valueProperty()); }

            @Override
            protected boolean computeValue()
            {
                if(firstnameField.getText().length() == 0 || lastnameField.getText().length() == 0
                    || birthDateField.getValue() == null || passportIdField.getText().length() == 0
                    || birthPlaceField.getText().length() == 0
                    || citizenshipField.getValue().length() == 0)
                    return true;

                try
                {
                    Integer.parseInt(passportIdField.getText());
                }
                catch(NumberFormatException exc) { return true; }

                return false;
            }
        });

        passportIdField.textProperty().addListener((obs, oldVal, newVal) ->
        {
            if(!newVal.matches("\\d+") && !newVal.equals(""))
                passportIdField.setText(oldVal);
        });

        ButtonBar.setType(submitAction, ButtonBar.ButtonType.OK_DONE);
        this.getActions().addAll(Actions.CANCEL, submitAction);
        this.setContent(pane);
        this.setGraphic(new ImageView(getClass().getClassLoader()
                .getResource("images/add_person.png").toString()));

        return this.show();
    }

    public GridPane getPane()
    {
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader()
                .getResource("customwindows/addnewperson.fxml"));

        HBox hBox = new HBox();
        loader.setRoot(hBox);
        loader.setController(this);

        try
        {
            loader.load();
        }
        catch(Exception exc) { throw new RuntimeException(exc); }

        return (GridPane)hBox.getChildren().get(0);
    }
}
