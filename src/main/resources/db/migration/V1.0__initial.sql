CREATE TABLE users
(
    id         BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name  VARCHAR(255) NOT NULL,
    password   VARCHAR(255) NOT NULL,
    email      VARCHAR(255) NOT NULL,
    role       VARCHAR(255)
);

CREATE TABLE courses
(
    id        BIGSERIAL PRIMARY KEY,
    name      VARCHAR(255) NOT NULL,
    status    VARCHAR(255),
    startDate DATE         NOT NULL,
    started   BOOLEAN,
    UNIQUE (name)
);

CREATE TABLE enrollments
(
    id        BIGSERIAL PRIMARY KEY,
    user_id   BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (course_id) REFERENCES courses (id),
    CONSTRAINT unique_user_course UNIQUE (user_id, course_id)
);

CREATE TABLE lessons
(
    id        BIGSERIAL PRIMARY KEY,
    name      VARCHAR(255) NOT NULL,
    course_id BIGINT       NOT NULL,
    FOREIGN KEY (course_id) REFERENCES courses (id)
);

CREATE TABLE files
(
    id        BIGSERIAL PRIMARY KEY,
    file_name VARCHAR(255),
    file_data BYTEA NOT NULL
);

CREATE TABLE homework
(
    id        BIGSERIAL PRIMARY KEY,
    mark      BIGINT,
    user_id   BIGINT NOT NULL,
    lesson_id BIGINT NOT NULL,
    file_id   BIGINT,
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (lesson_id) REFERENCES lessons (id),
    FOREIGN KEY (file_id) REFERENCES files (id)
);

CREATE TABLE course_feedback
(
    id            BIGSERIAL PRIMARY KEY,
    feedback_text TEXT   NOT NULL,
    course_id     BIGINT NOT NULL,
    instructor_id BIGINT NOT NULL,
    student_id    BIGINT NOT NULL,
    FOREIGN KEY (course_id) REFERENCES courses (id),
    FOREIGN KEY (instructor_id) REFERENCES users (id),
    FOREIGN KEY (student_id) REFERENCES users (id)
);

CREATE TABLE tokens
(
    id         BIGSERIAL PRIMARY KEY,
    token      VARCHAR(250),
    token_type VARCHAR(50) NOT NULL,
    revoked    BOOLEAN     NOT NULL,
    expired    BOOLEAN     NOT NULL,
    user_id    BIGINT,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE ON UPDATE CASCADE
);


INSERT INTO users (first_name, last_name, password, email, role)
VALUES ('user1', 'user1 last name', 'password1', 'user1@example.com', 'STUDENT'),
       ('admin', 'admin', '$2a$10$6HZfDrXpJT5Vh5MtZR7U8e31MeRJFc3UldWVixD/QQ0hYGXR9mM1y', 'admin@gmail.com', 'ADMIN'),
       ('instructor1', 'user3 last name', 'password3', 'instructor1@example.com', 'INSTRUCTOR'),
       ('instructor2', 'user4 last name', 'password4', 'instructor2@example.com', 'INSTRUCTOR');

INSERT INTO courses (name, status, startDate, started)
VALUES ('Course A', 'RUNNING', '2023-01-01', TRUE),
       ('Course B', 'RUNNING', '2023-02-15', TRUE),
       ('Course C', 'RUNNING', '2023-03-10', TRUE);

INSERT INTO enrollments (user_id, course_id)
VALUES (1, 1),
       (2, 1),
       (1, 2),
       (3, 3),
       (4, 3);

INSERT INTO lessons (name, course_id)
VALUES ('Lesson 1', 1),
       ('Lesson 2', 1),
       ('Lesson 1', 2),
       ('Lesson 1', 3);

INSERT INTO files (file_name, file_data)
VALUES ('file1.txt', E'\\x5465737420746578742066696C65'),
       ('file2.txt', E'\\x5465737420746578742066696C6532'),
       ('file3.txt', E'\\x5465737420746578742066696C6533'),
       ('file4.txt', E'\\x5465737420746578742066696C6534'),
       ('file5.txt', E'\\x5465737420746578742066696C6535');

INSERT INTO homework (mark, user_id, lesson_id, file_id)
VALUES (95, 1, 1, 1),
       (88, 2, 1, 2),
       (92, 1, 2, 3),
       (87, 2, 2, 4),
       (94, 1, 3, 5);

INSERT INTO course_feedback (feedback_text, course_id, instructor_id, student_id)
VALUES ('Great course!', 1, 3, 1),
       ('Excellent instructor!', 1, 3, 2),
       ('Good content', 2, 4, 1),
       ('Very informative', 3, 3, 4);
