CREATE DATABASE "management-system";

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

CREATE TABLE user_course_marks
(
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT NOT NULL,
    course_id  BIGINT NOT NULL,
    total_score NUMERIC(10, 2),
    passed      BOOLEAN NOT NULL DEFAULT false,
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (course_id) REFERENCES courses (id)
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