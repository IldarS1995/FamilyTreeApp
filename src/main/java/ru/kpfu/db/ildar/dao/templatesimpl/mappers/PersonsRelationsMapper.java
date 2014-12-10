package ru.kpfu.db.ildar.dao.templatesimpl.mappers;

import org.springframework.jdbc.core.RowMapper;
import ru.kpfu.db.ildar.dao.PeopleDAO;
import ru.kpfu.db.ildar.pojos.PeopleRelations;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PersonsRelationsMapper implements RowMapper<PeopleRelations>
{
    private PeopleDAO peopleDAO;

    public PersonsRelationsMapper(PeopleDAO peopleDAO) { this.peopleDAO = peopleDAO; }

    @Override
    public PeopleRelations mapRow(ResultSet resultSet, int i) throws SQLException
    {
        return new PeopleRelations(
                resultSet.getInt("id"),
                peopleDAO.findPersonById(resultSet.getInt("parent_id")),
                peopleDAO.findPersonById(resultSet.getInt("child_id"))
        );
    }
}
