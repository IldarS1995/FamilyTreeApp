--Create new user for our application
create user family_tree identified by ildar;
grant connect, resource to family_tree;

--create sequence - PERSON_ID column in PEOPLE table
-- will be generated using it
CREATE SEQUENCE family_tree.seq
START WITH     1
INCREMENT BY   1
NOCACHE
NOCYCLE;

--Table where countries names will be stored
create table family_tree.Countries(
	name varchar(70) not null primary key
);

--Table where people data will be stored
create table family_tree.People(
	person_id int,
	firstname varchar(70) not null,
	lastname varchar(70) not null,
	birth_date date not null,
	passport_id int not null,
	birth_place varchar(120) not null,
	citizenship varchar(70) not null,
  gender varchar(1) not null check (gender = 'm' or gender = 'f'),

	constraint pk_person primary key(person_id),
	constraint fk_country foreign key(citizenship)
		references family_tree.Countries(name),
	constraint un_passport_id_citizenship unique(passport_id, citizenship)
);

--Table where relations between people are stored. This table has many-to-many relationship
--with People table
create table family_tree.People_Relations(
  parent_id int not null,
  child_id int not null,

  constraint pk_people_relations primary key (parent_id, child_id),
  constraint fk_parent1 foreign key(parent_id)
  references family_tree.People(person_id),
  constraint fk_parent2 foreign key(child_id)
  references family_tree.People(person_id),
  constraint ck_parent_not_equal_child check (parent_id <> child_id)
);

--Procedure that adds new person to the database
create or replace procedure family_tree.add_person
  (firstname_ family_tree.people.firstname%TYPE,
   lastname_ family_tree.people.lastname%TYPE,
   birth_date_ family_tree.people.birth_date%TYPE,
   passport_id_ family_tree.people.passport_id%TYPE,
   birth_place_ family_tree.people.birth_place%TYPE,
   citizenship_ family_tree.people.citizenship%TYPE,
   gender_ family_tree.people.gender%TYPE) AS
  begin
    insert into PEOPLE (person_id, firstname, lastname, birth_date, passport_id,
                        birth_place, citizenship, gender)
    values(seq.nextval, firstname_, lastname_, birth_date_, passport_id_ ,
           birth_place_, citizenship_, gender_);
  end;

--Procedure that updates existing person in the database
create or replace procedure family_tree.update_person
  (firstname_ family_tree.people.firstname%TYPE,
   lastname_ family_tree.people.lastname%TYPE,
   birth_date_ family_tree.people.birth_date%TYPE,
   passport_id_ family_tree.people.passport_id%TYPE,
   birth_place_ family_tree.people.birth_place%TYPE,
   citizenship_ family_tree.people.citizenship%TYPE,
   gender_ family_tree.people.gender%TYPE,
   person_id_ family_tree.people.person_id%TYPE) AS
  begin
    update PEOPLE
    set firstname = firstname_, lastname = lastname_, birth_date = birth_date_,
      passport_id = passport_id_, birth_place = birth_place_, citizenship = citizenship_,
      gender = gender_
    where person_id = person_id_;
  end;

--Insert some countries to the Countries table
insert into family_tree.Countries(name) values('Austria');
insert into family_tree.Countries(name) values('Canada');
insert into family_tree.Countries(name) values('China');
insert into family_tree.Countries(name) values('France');
insert into family_tree.Countries(name) values('Germany');
insert into family_tree.Countries(name) values('India');
insert into family_tree.Countries(name) values('Japan');
insert into family_tree.Countries(name) values('Pakistan');
insert into family_tree.Countries(name) values('Republic of Korea');
insert into family_tree.Countries(name) values('Russia');
insert into family_tree.Countries(name) values('Sweden');
insert into family_tree.Countries(name) values('Switzerland');
insert into family_tree.Countries(name) values('Taiwan');
insert into family_tree.Countries(name) values('UK');
insert into family_tree.Countries(name) values('USA');
insert into family_tree.Countries(name) values('Ukraine');