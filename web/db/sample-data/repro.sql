INSERT INTO ptaweb.reg_session VALUES (DEFAULT, TIMESTAMP '2011-11-05 00:00:00', TIMESTAMP '2011-12-12 00:00:00', 3, TIME '15:00:00', TIME '17:00:00', TIME '17:30:00', TIME '19:00:00', 7);

INSERT INTO ptaweb.days values (1, DATE '2011-12-12', 1, '');
INSERT INTO ptaweb.days values (2, DATE '2011-12-13', 1, '');

-- admin1:admin1pass
INSERT INTO ptaweb.admin values ('admin1', '226454965bd0091da1f0de4d1a0d9b05');
-- admin:password
INSERT INTO ptaweb.admin values ('admin', '5f4dcc3b5aa765d61d8327deb882cf99');

-- sample parents' passwords are all "pass"
INSERT INTO ptaweb.parents VALUES (1, 'Andrei', 'Soltan', 'asoltan', '1a1dc91c907325c69271ddf0c944bc72', 'andrei@email.com', 0);
INSERT INTO ptaweb.parents VALUES (2, 'Dan', 'Moon', 'dmoon', '1a1dc91c907325c69271ddf0c944bc72', 'dan@email.com', 1);
INSERT INTO ptaweb.parents VALUES (3, 'Jon', 'Prindiville', 'jon', '1a1dc91c907325c69271ddf0c944bc72', 'jon@email.com', 2);
INSERT INTO ptaweb.parents VALUES (4, 'Tyrone', 'Strangway', 'tstrangway', '1a1dc91c907325c69271ddf0c944bc72', 'tyrone@email.com', 0);

INSERT INTO ptaweb.teachers VALUES (default, 'Steve', 'Engels');
INSERT INTO ptaweb.teachers VALUES (default, 'Danny', 'Heap');
INSERT INTO ptaweb.teachers VALUES (default, 'Paul', 'Gries');
INSERT INTO ptaweb.teachers VALUES (default, 'Sam', 'Toueg');
INSERT INTO ptaweb.teachers VALUES (default, 'Diane', 'Horton');
INSERT INTO ptaweb.teachers VALUES (default, 'Karen', 'Reid');
INSERT INTO ptaweb.teachers VALUES (default, 'Gary', 'Baumgartner');
INSERT INTO ptaweb.teachers VALUES (default, 'Teachy', 'McTeacherton');
INSERT INTO ptaweb.teachers VALUES (default, 'Teacher', 'Teachian');
INSERT INTO ptaweb.teachers VALUES (default, 'Teacher', 'Al-Teachbari');
INSERT INTO ptaweb.teachers VALUES (default, 'Teacher', 'OTeachigan');
INSERT INTO ptaweb.teachers VALUES (default, 'Teacher', 'MacTeach');
INSERT INTO ptaweb.teachers VALUES (default, 'Teacher', 'Foo');
INSERT INTO ptaweb.teachers VALUES (default, 'Teacher', 'Bar');
INSERT INTO ptaweb.teachers VALUES (default, 'Teacher', 'Baz');
INSERT INTO ptaweb.teachers VALUES (default, 'Teacher', 'Biko');
INSERT INTO ptaweb.teachers VALUES (default, 'Teacher', 'King');
INSERT INTO ptaweb.teachers VALUES (default, 'Teacher', 'Parks');
INSERT INTO ptaweb.teachers VALUES (default, 'Teacher', 'Escher');
INSERT INTO ptaweb.teachers VALUES (default, 'Teacher', 'Dick');
INSERT INTO ptaweb.teachers VALUES (default, 'Teacher', 'Heinlen');
INSERT INTO ptaweb.teachers VALUES (default, 'Teacher', 'Pournelle');
INSERT INTO ptaweb.teachers VALUES (default, 'Teacher', 'Niven');
INSERT INTO ptaweb.teachers VALUES (default, 'Teacher', 'Sugar');
INSERT INTO ptaweb.teachers VALUES (default, 'Teacher', 'Sagan');
INSERT INTO ptaweb.teachers VALUES (default, 'Teacher', 'Bailey');
INSERT INTO ptaweb.teachers VALUES (default, 'Teacher', 'Smith');
INSERT INTO ptaweb.teachers VALUES (default, 'Teacher', 'Stevens');
INSERT INTO ptaweb.teachers VALUES (default, 'Teacher', 'Alpert');
INSERT INTO ptaweb.teachers VALUES (default, 'Teacher', 'Tran');
INSERT INTO ptaweb.teachers VALUES (default, 'Teacher', 'Nguyen');
INSERT INTO ptaweb.teachers VALUES (default, 'Teacher', 'van Teach');
INSERT INTO ptaweb.teachers VALUES (default, 'Teacher', 'Waterloo');
INSERT INTO ptaweb.teachers VALUES (default, 'Teacher', 'Kitchener');
INSERT INTO ptaweb.teachers VALUES (default, 'Teacher', 'Hamilton');
INSERT INTO ptaweb.teachers VALUES (default, 'Teacher', 'London');
INSERT INTO ptaweb.teachers VALUES (default, 'Teacher', 'Meow');
INSERT INTO ptaweb.teachers VALUES (default, 'Teacher', 'What');
INSERT INTO ptaweb.teachers VALUES (default, 'Teacher', 'Carrol');
INSERT INTO ptaweb.teachers VALUES (default, 'Teacher', 'Lewis');
INSERT INTO ptaweb.teachers VALUES (default, 'Teacher', 'LaSalle');
INSERT INTO ptaweb.teachers VALUES (default, 'Teacher', 'Desjardin');
INSERT INTO ptaweb.teachers VALUES (default, 'Teacher', 'Elephino');
INSERT INTO ptaweb.teachers VALUES (default, 'Teacher', 'James');
INSERT INTO ptaweb.teachers VALUES (default, 'Teacher', 'Matthew');
INSERT INTO ptaweb.teachers VALUES (default, 'Teacher', 'Luke');
INSERT INTO ptaweb.teachers VALUES (default, 'Teacher', 'John');
INSERT INTO ptaweb.teachers VALUES (default, 'Teacher', 'Carlsson');
INSERT INTO ptaweb.teachers VALUES (default, 'Teacher', 'Ericsson');
INSERT INTO ptaweb.teachers VALUES (default, 'Teacher', 'Sony');
INSERT INTO ptaweb.teachers VALUES (default, 'Teacher', 'Bravia');
INSERT INTO ptaweb.teachers VALUES (default, 'Teacher', 'Samsung');
INSERT INTO ptaweb.teachers VALUES (default, 'Teacher', 'EllGee');
INSERT INTO ptaweb.teachers VALUES (default, 'Teacher', 'Philips');
INSERT INTO ptaweb.teachers VALUES (default, 'Teacher', 'Coen');
INSERT INTO ptaweb.teachers VALUES (default, 'Teacher', 'Cone');
INSERT INTO ptaweb.teachers VALUES (default, 'Teacher', 'Tardos');
INSERT INTO ptaweb.teachers VALUES (default, 'Teacher', 'Kleinberg');
INSERT INTO ptaweb.teachers VALUES (default, 'Teacher', 'Poe');
INSERT INTO ptaweb.teachers VALUES (default, 'Teacher', 'Raven');
INSERT INTO ptaweb.teachers VALUES (default, 'Teacher', 'Gateway');
INSERT INTO ptaweb.teachers VALUES (default, 'Teacher', 'Asus');
INSERT INTO ptaweb.teachers VALUES (default, 'Teacher', 'Cowboy');
INSERT INTO ptaweb.teachers VALUES (default, 'Teacher', 'Bebop');
INSERT INTO ptaweb.teachers VALUES (default, 'Teacher', 'Teacher');
INSERT INTO ptaweb.teachers VALUES (default, 'Eacher', 'Tay');

INSERT INTO ptaweb.requests VALUES (default, 1, 1, 1, 1);
INSERT INTO ptaweb.requests VALUES (default, 1, 2, 1, 1);
INSERT INTO ptaweb.requests VALUES (default, 1, 3, 1, 1);
INSERT INTO ptaweb.requests VALUES (default, 1, 7, 1, 1);
