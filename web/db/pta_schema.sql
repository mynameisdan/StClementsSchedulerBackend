
-- schema cleanup
DROP SCHEMA IF EXISTS ptaweb;
CREATE SCHEMA ptaweb;
SET SCHEMA ptaweb;

CREATE TABLE reg_session (
	rsid INT IDENTITY,
	startts TIMESTAMP DEFAULT '0001-01-01 00:00:00',
	endts TIMESTAMP DEFAULT '0001-01-01 00:00:01',
	status INT NOT NULL, -- 1 for new, 2 for open, 3 for closed, 4 for archived
	aft_start TIME NOT NULL, -- afternoon timeblock start time 
	aft_end TIME NOT NULL, -- afternoon timeblock end time
	eve_start TIME NOT NULL, -- evening timeblock start time
	eve_end TIME NOT NULL, -- evening timeblock end time
	meet_len INT NOT NULL, -- interview length in minutes
	PRIMARY KEY (rsid),
--	CONSTRAINT start_end_ck CHECK (startts < endts)
);

CREATE TABLE parents (
	pid INT IDENTITY,
	fname TEXT NOT NULL,
	lname TEXT NOT NULL,
	username VARCHAR(40) UNIQUE,
	passwd TEXT NOT NULL,
	email TEXT,
	tb INT, --Time-block: 0-both, 1-afternoon, 2-evening
	PRIMARY KEY (pid)
);

CREATE TABLE days (
	did INT IDENTITY,
	day DATE NOT NULL,
	rsid INT NOT NULL,
	comment TEXT,
	PRIMARY KEY (did),
	FOREIGN KEY (rsid) REFERENCES reg_session(rsid) ON DELETE CASCADE
);

CREATE TABLE teachers (
	tid INT IDENTITY,
	fname TEXT NOT NULL,
	lname TEXT NOT NULL,
	PRIMARY KEY (tid)
);

CREATE TABLE requests (
	rid INT IDENTITY,
	did INT NOT NULL,
	tid INT NOT NULL,
	pid INT NOT NULL,
	rsid INT NOT NULL,
	PRIMARY KEY (rid),
	FOREIGN KEY (did) REFERENCES days(did) ON DELETE CASCADE,
	FOREIGN KEY (tid) REFERENCES teachers(tid) ON DELETE CASCADE,
	FOREIGN KEY (pid) REFERENCES parents(pid) ON DELETE CASCADE,
	FOREIGN KEY (rsid) REFERENCES reg_session(rsid) ON DELETE CASCADE
);

CREATE TABLE schedules (
	sid INT IDENTITY,
	did INT NOT NULL, 
	rsid INT NOT NULL,
	comment TEXT,
	PRIMARY KEY (sid),
	FOREIGN KEY (did) REFERENCES days(did) ON DELETE CASCADE
);

CREATE TABLE appointments (
	sid INT NOT NULL,
	pid INT NOT NULL,
	tid INT NOT NULL,
	slot INT NOT NULL,
    tb INT, -- Time-block: 0-both, 1-afternoon, 2-evening
	PRIMARY KEY (sid, tid, pid, slot, tb),
	FOREIGN KEY (sid) REFERENCES schedules(sid) ON DELETE CASCADE,
	FOREIGN KEY (tid) REFERENCES teachers(tid) ON DELETE CASCADE,
	FOREIGN KEY (pid) REFERENCES parents(pid) ON DELETE CASCADE
);

CREATE TABLE admin (
	username VARCHAR(20) NOT NULL,
	passwd TEXT NOT NULL,
	PRIMARY KEY (username)
);
