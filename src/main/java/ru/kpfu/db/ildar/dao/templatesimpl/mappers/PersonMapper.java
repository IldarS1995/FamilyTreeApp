package ru.kpfu.db.ildar.dao.templatesimpl.mappers;

import org.springframework.jdbc.core.RowMapper;
import ru.kpfu.db.ildar.pojos.Person;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PersonMapper implements RowMapper<Person>
{
    @Override
    public Person mapRow(ResultSet resultSet, int i) throws SQLException
    {
        return new Person(
                resultSet.getInt("person_id"),
                resultSet.getString("firstname"),
                resultSet.getString("lastname"),
                resultSet.getDate("birth_date"),
                resultSet.getInt("passport_id"),
                resultSet.getString("birth_place"),
                resultSet.getString("citizenship"),
                resultSet.getString("gender").charAt(0)
        );
    }
}
