package ru.kpfu.db.ildar.dao.templatesimpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.kpfu.db.ildar.dao.PeopleDAO;
import ru.kpfu.db.ildar.dao.templatesimpl.mappers.PersonMapper;
import ru.kpfu.db.ildar.pojos.*;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.util.Date;
import java.util.List;

@Repository
public class PeopleDAOImpl implements PeopleDAO
{
    @Autowired
    private DataSource dataSource;

    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    private void initJdbcTemplate()
    {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public Person findPersonById(int id)
    {
        String sql = "select * from PEOPLE where person_id=?";
        return jdbcTemplate.queryForObject(sql, new PersonMapper(), id);
    }

    @Override
    public List<Person> findPeopleByName(String firstname, String lastname, Date birthDateUpLim)
    {
        String sql = "select * from PEOPLE where firstname = ? and lastname = ? " +
                "and birth_date > ?";
        return jdbcTemplate.query(sql, new PersonMapper(), firstname, lastname, birthDateUpLim);
    }

    @Override
    public int addPerson(Person p)
    {
        String sql = "insert into PEOPLE" +
                "(person_id, firstname, lastname, birth_date, passport_id, " +
                "birth_place, citizenship, gender) values(seq.nextval,?,?,?,?,?,?,?)";
        int rows = jdbcTemplate.update(sql,
                p.getFirstname(),
                p.getLastname(),
                p.getBirthDate(),
                p.getPassportId(),
                p.getBirthPlace(),
                p.getCitizenship(),
                String.valueOf(p.getGender()));

        sql = "select seq.currval from dual";
        p.setPersonId(jdbcTemplate.queryForObject(sql, Integer.class));

        return rows;
    }

    @Override
    public List<Person> findChildren(Person p)
    {
        String sql = "select * from PEOPLE where PERSON_ID in ("
                + "select CHILD_ID from PEOPLE_RELATIONS where PARENT_ID = ?)";
        return jdbcTemplate.query(sql, new PersonMapper(), p.getPersonId());
    }

    @Override
    public List<Person> findParents(Person p)
    {
        String sql = "select * from PEOPLE where PERSON_ID in ("
                + "select PARENT_ID from PEOPLE_RELATIONS where CHILD_ID = ?)";
        return jdbcTemplate.query(sql, new PersonMapper(), p.getPersonId());
    }

    @Override
    public List<Person> findAllPeople()
    {
        String sql = "select * from PEOPLE";
        return jdbcTemplate.query(sql, new PersonMapper());
    }

    @Override
    public List<String> findAllCountries()
    {
        String sql = "select NAME from COUNTRIES";
        return jdbcTemplate.query(sql, (set, i) -> set.getString("NAME"));
    }

    @Override
    public int deletePerson(Person p)
    {
        String sql = "delete from PEOPLE_RELATIONS where parent_id = ? or child_id = ?";
        jdbcTemplate.update(sql, p.getPersonId(), p.getPersonId());

        sql = "delete from PEOPLE where person_id = ?";
        return jdbcTemplate.update(sql, p.getPersonId());
    }

    @Override
    public Person findPersonByPassportIDAndCitizenship(int passportId, String citizenship)
    {
        String sql = "select * from PEOPLE where passport_id = ? and citizenship = ?";
        return jdbcTemplate.queryForObject(sql, new PersonMapper(), passportId, citizenship);
    }

    @Override
    public void addChildrenToPerson(Person selectedItem, List<Person> children)
    {
        String sql = "insert into PEOPLE_RELATIONS(parent_id, child_id) values(?,?)";
        children.stream().map(Person::getPersonId).forEach((personId) ->
        {
            jdbcTemplate.update(sql, selectedItem.getPersonId(), personId);
        });
    }

    @Override
    public void updatePerson(Person p)
    {
        String sql = "update PEOPLE set firstname = ?, lastname = ?, birth_date = ?, " +
                "passport_id = ?, birth_place = ?, citizenship = ?, gender = ? " +
                "where person_id = ?";
        jdbcTemplate.update(sql, p.getFirstname(),
                p.getLastname(),
                p.getBirthDate(),
                p.getPassportId(),
                p.getBirthPlace(),
                p.getCitizenship(),
                String.valueOf(p.getGender()),
                p.getPersonId());
    }

    @Override
    public boolean checkFullParents(Person child)
    {
        String sql = "select count(*) from PEOPLE_RELATIONS where child_id = ?";
        int count = jdbcTemplate.queryForObject(sql, Integer.class, child.getPersonId());
        return count >= 2;
    }

    @Override
    public void deleteChild(Person parent, Person child)
    {
        String sql = "delete from PEOPLE_RELATIONS where parent_id = ? and child_id = ?";
        jdbcTemplate.update(sql, parent.getPersonId(), child.getPersonId());
    }
}












