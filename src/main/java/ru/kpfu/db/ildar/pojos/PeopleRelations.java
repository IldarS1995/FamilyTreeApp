package ru.kpfu.db.ildar.pojos;

public class PeopleRelations
{
    private int id;
    private Person parent;
    private Person child;

    public PeopleRelations() { }
    public PeopleRelations(int id, Person parent, Person child)
    {
        this.id = id;
        this.parent = parent;
        this.child = child;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public Person getParent()
    {
        return parent;
    }

    public void setParent(Person parent)
    {
        this.parent = parent;
    }

    public Person getChild()
    {
        return child;
    }

    public void setChild(Person child)
    {
        this.child = child;
    }
}
