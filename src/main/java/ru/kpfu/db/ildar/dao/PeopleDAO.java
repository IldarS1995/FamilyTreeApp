package ru.kpfu.db.ildar.dao;

import org.springframework.stereotype.Repository;
import ru.kpfu.db.ildar.pojos.Person;

import java.util.Date;
import java.util.List;

/** Interface that declares basic methods with FAMILY_TREE schema interaction */
@Repository
public interface PeopleDAO
{
    /** Find a person in DB by his primary key (ID) */
    Person findPersonById(int id);

    /** Adds a person to the table. If there's constraint violation while adding
     * the record, a <code>DuplicateKeyException</code> will be thrown.*/
    int addPerson(Person p);

    /** Find person's children. Looks in PEOPLE_RELATIONS table */
    List<Person> findChildren(Person p);
    /** Find person's parents. Looks in PEOPLE_RELATIONS table */
    List<Person> findParents(Person p);
    /** Fetch all existing people from a database */
    List<Person> findAllPeople();
    /** Fetch all existing countries from a database */
    List<String> findAllCountries();

    /** Delete a person from the table. Person will be identified by his ID */
    int deletePerson(Person p);

    /** Find a person by two fields - passport ID and citizenship.
     * These two fields combined are unique, so maximum one record will be returned.
     * If now such person found, <code>EmptyResultDataAccessException</code> will be thrown.*/
    Person findPersonByPassportIDAndCitizenship(int passportId, String citizenship);

    /** Adds children to the specified person. Works with PEOPLE_RELATIONS table.
     * Some exceptional cases should be handled by the application business logic: <br />
     * 1) Child mustn't have more than two parents; <br />
     * 2) Children mustn't have parents with identical genders; <br />
     * 3) Children mustn't be older than parents; <br />
     * 4) Child's grandfather mustn't be also his father; <br />
     * And so on.*/
    void addChildrenToPerson(Person selectedItem, List<Person> children);

    /** Update person - person is identified by his ID, other fields are updated. */
    void updatePerson(Person p);

    /** Check if the specified person has exactly two parents */
    boolean checkFullParents(Person child);

    /** Delete connection between two people */
    void deletePeopleRelations(Person parent, Person child);

    /** Find people by four fields - first name, last name, birth date start interval
     * and birth date end interval. Some of these fields may be omitted(that is, null). In
     * that case, search will be based on non-null fields values. */
    List<Person> findPeopleByDynamicCriteria(String firstName, String lastName,
                                             Date birthDateFrom, Date birthDateTo);
}
