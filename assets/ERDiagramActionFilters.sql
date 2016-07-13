
/* Drop Tables */

DROP TABLE FILTER_CONTEXT;
DROP TABLE FILTER_STATUS;
DROP TABLE FILTER_TOPIC;
DROP TABLE SORT;
DROP TABLE ACTIONLISTS;




/* Create Tables */

CREATE TABLE ACTIONLISTS
(
	LIST_ID INTEGER NOT NULL UNIQUE,
	LISTNAME TEXT NOT NULL,
	WEIGHT INTEGER NOT NULL,
	PRIMARY KEY (LIST_ID)
);


CREATE TABLE FILTER_CONTEXT
(
	LIST_ID INTEGER NOT NULL UNIQUE,
	CONTEXT_NAME TEXT,
	CONTEXT_ID INTEGER
);


CREATE TABLE FILTER_STATUS
(
	LIST_ID INTEGER NOT NULL UNIQUE,
	STATUS_NAME TEXT NOT NULL
);


CREATE TABLE FILTER_TOPIC
(
	LIST_ID INTEGER NOT NULL UNIQUE,
	TOPIC_NAME TEXT,
	TOPIC_ID INTEGER
);


CREATE TABLE SORT
(
	LIST_ID INTEGER NOT NULL UNIQUE,
	CRITERIA INTEGER NOT NULL,
	DIRECTION INTEGER,
	WEIGHT INTEGER,
	PRIMARY KEY (LIST_ID, CRITERIA)
);



/* Create Foreign Keys */

ALTER TABLE FILTER_CONTEXT
	ADD FOREIGN KEY (LIST_ID)
	REFERENCES ACTIONLISTS (LIST_ID)
	ON UPDATE CASCADE
	ON DELETE CASCADE
;


ALTER TABLE FILTER_STATUS
	ADD FOREIGN KEY (LIST_ID)
	REFERENCES ACTIONLISTS (LIST_ID)
	ON UPDATE CASCADE
	ON DELETE CASCADE
;


ALTER TABLE FILTER_TOPIC
	ADD FOREIGN KEY (LIST_ID)
	REFERENCES ACTIONLISTS (LIST_ID)
	ON UPDATE CASCADE
	ON DELETE CASCADE
;


ALTER TABLE SORT
	ADD FOREIGN KEY (LIST_ID)
	REFERENCES ACTIONLISTS (LIST_ID)
	ON UPDATE CASCADE
	ON DELETE CASCADE
;


