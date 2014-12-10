package ru.kpfu.db.ildar.dao;

import org.springframework.stereotype.Repository;
import ru.kpfu.db.ildar.pojos.PeopleRelations;
import ru.kpfu.db.ildar.pojos.Person;

import java.util.Date;
import java.util.List;

@Repository
public interface PeopleDAO
{
    Person findPersonById(int id);
    List<Person> findPeopleByName(String firstname, String lastname, Date birthDateUpLim);

    int addPerson(Person p);

    List<Person> findChildren(Person p);
    List<Person> findParents(Person p);

    List<Person> findAllPeople();

    List<String> findAllCountries();

    int deletePerson(Person p);

    Person findPersonByPassportIDAndCitizenship(int passportId, String citizenship);

    void addChildrenToPerson(Person selectedItem, List<Person> children);

    void updatePerson(Person p);
}
