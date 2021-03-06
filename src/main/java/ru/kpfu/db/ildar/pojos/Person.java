package ru.kpfu.db.ildar.pojos;

import java.sql.Date;
import java.text.SimpleDateFormat;

/** POJO class that represents PEOPLE table record */
public class Person
{
    private int personId;
    private String firstname;
    private String lastname;
    private Date birthDate;
    private int passportId;
    private String birthPlace;
    private String citizenship;
    private char gender;

    public Person() { }
    public Person(int personId, String firstname, String lastname, Date birthDate, int passportId,
                  String birthPlace, String citizenship, char gender)
    {
        this.personId = personId;
        this.firstname = firstname;
        this.lastname = lastname;
        this.birthDate = birthDate;
        this.passportId = passportId;
        this.birthPlace = birthPlace;
        this.citizenship = citizenship;
        this.gender = gender;
    }

    @Override
    public String toString()
    {
        return firstname + " " + lastname + ", passport ID - " + passportId;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Person person = (Person) o;

        if (personId != person.personId) return false;
        if (gender != person.gender) return false;
        if (passportId != person.passportId) return false;
        if (!birthDate.equals(person.birthDate)) return false;
        if (!birthPlace.equals(person.birthPlace)) return false;
        if (!citizenship.equals(person.citizenship)) return false;
        if (!firstname.equals(person.firstname)) return false;
        if (!lastname.equals(person.lastname)) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = personId;
        result = 31 * result + firstname.hashCode();
        result = 31 * result + lastname.hashCode();
        result = 31 * result + birthDate.hashCode();
        result = 31 * result + passportId;
        result = 31 * result + birthPlace.hashCode();
        result = 31 * result + citizenship.hashCode();
        result = 31 * result + (int) gender;
        return result;
    }

    /** Get birth date in 'dd/MM/yyyy' format */
    public String getBirthDateAsString()
    {
        if(birthDate == null)
            return null;

        SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy");
        return fmt.format(birthDate);
    }


    /** Gender of the person - either 'm', or 'f' */
    public char getGender()
    {
        return gender;
    }

    public void setGender(char gender)
    {
        this.gender = gender;
    }

    /** Primary key (PERSON_ID) of the person */
    public int getPersonId()
    {
        return personId;
    }

    public void setPersonId(int personId)
    {
        this.personId = personId;
    }

    /** Represents FIRST_NAME column */
    public String getFirstname()
    {
        return firstname;
    }

    public void setFirstname(String firstname)
    {
        this.firstname = firstname;
    }

    /** Represents LAST_NAME column */
    public String getLastname()
    {
        return lastname;
    }

    public void setLastname(String lastname)
    {
        this.lastname = lastname;
    }

    /** Represents BIRTH_DATE column */
    public Date getBirthDate()
    {
        return birthDate;
    }

    public void setBirthDate(Date birthDate)
    {
        this.birthDate = birthDate;
    }

    /** Represents PASSPORT_ID column */
    public int getPassportId()
    {
        return passportId;
    }

    public void setPassportId(int passportId)
    {
        this.passportId = passportId;
    }

    /** Represents BIRTH_PLACE column */
    public String getBirthPlace()
    {
        return birthPlace;
    }

    public void setBirthPlace(String birthPlace)
    {
        this.birthPlace = birthPlace;
    }

    /** Represents CITIZENSHIP column */
    public String getCitizenship()
    {
        return citizenship;
    }

    public void setCitizenship(String citizenship)
    {
        this.citizenship = citizenship;
    }
}
