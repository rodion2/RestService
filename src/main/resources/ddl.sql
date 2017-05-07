USE WINAPI_HANDBOOK;

DROP TABLE IF EXISTS WINAPI_PARAMETER;
DROP TABLE IF EXISTS WINAPI_FUNCTION;
DROP TABLE IF EXISTS WINAPI_CLASS;

CREATE TABLE WINAPI_CLASS (
  ID          BIGINT        NOT NULL PRIMARY KEY AUTO_INCREMENT,
  NAME        CHAR(30)      NOT NULL,
  DESCRIPTION VARCHAR(1000) NOT NULL
);

CREATE UNIQUE INDEX CLASSX
  ON WINAPI_CLASS (ID);

CREATE TABLE WINAPI_FUNCTION (
  ID          BIGINT        NOT NULL PRIMARY KEY AUTO_INCREMENT,
  NAME        CHAR(30)      NOT NULL,
  DESCRIPTION VARCHAR(1000) NOT NULL,
  CLASS_ID    BIGINT,
  FOREIGN KEY (CLASS_ID)
  REFERENCES WINAPI_CLASS (ID)
    ON DELETE CASCADE
);

CREATE UNIQUE INDEX FUNCTIONX
  ON WINAPI_FUNCTION (ID);

CREATE TABLE WINAPI_PARAMETER (
  ID          BIGINT   NOT NULL PRIMARY KEY AUTO_INCREMENT,
  TYPE        CHAR(30) NOT NULL,
  NAME        CHAR(30) NOT NULL,
  FUNCTION_ID BIGINT,
  FOREIGN KEY (FUNCTION_ID) REFERENCES WINAPI_FUNCTION (ID)
    ON DELETE CASCADE
);

CREATE UNIQUE INDEX PARAMX
  ON WINAPI_PARAMETER (ID);

INSERT INTO WINAPI_CLASS (NAME, DESCRIPTION) VALUES ('LIST',
                                                     'The STL list class is a template class of sequence containers that maintain their elements in a linear arrangement and allow efficient insertions and deletions at any location within the sequence. The sequence is stored as a bidirectional linked list of elements, each containing a member of some type Type.');
INSERT INTO WINAPI_FUNCTION (NAME, DESCRIPTION, CLASS_ID) VALUES
  ('max_size', 'Returns the maximum length of a list.', 1),
  ('merge',
   'Removes the elements from the argument list, inserts them into the target list, and orders the new, combined set of elements in ascending order or in some other specified order.',
   1);
INSERT INTO WINAPI_PARAMETER (TYPE, NAME, FUNCTION_ID) VALUES
  (
    'list<Type, Allocator>& ', 'right', 2
  ),
  ('Traits', 'comp', 2);