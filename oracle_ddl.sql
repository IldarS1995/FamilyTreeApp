create user family_tree identified by ildar;
grant connect, resource to family_tree;

CREATE SEQUENCE family_tree.seq
START WITH     1
INCREMENT BY   1
NOCACHE
NOCYCLE;

create table family_tree.Countries(
	name varchar(70) not null primary key
);

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