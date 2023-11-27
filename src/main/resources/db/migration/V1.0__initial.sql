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
    start_date DATE         NOT NULL,
    started   BOOLEAN,
    UNIQUE (name)
);

CREATE TABLE enrollments
(
    id        BIGSERIAL PRIMARY KEY,
    user_id   BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (course_id) REFERENCES courses (id)
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
    CONSTRAINT unique_file_id UNIQUE (file_id),
    CONSTRAINT fk_file_id FOREIGN KEY (file_id) REFERENCES files (id) ON DELETE CASCADE
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
    FOREIGN KEY (course_id) REFERENCES courses (id),
    CONSTRAINT unique_mark_user_course UNIQUE (user_id, course_id)
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
VALUES
    ('user1', 'user1 last name', 'password1', 'user1@example.com', 'STUDENT'),
    ('admin', 'admin', '$2a$10$6HZfDrXpJT5Vh5MtZR7U8e31MeRJFc3UldWVixD/QQ0hYGXR9mM1y', 'admin@gmail.com', 'ADMIN'),
    ('instructor', 'instructor', '$2a$10$6HZfDrXpJT5Vh5MtZR7U8e31MeRJFc3UldWVixD/QQ0hYGXR9mM1y', 'instructor@gmail.com', 'INSTRUCTOR'),
    ('student', 'student', '$2a$10$6HZfDrXpJT5Vh5MtZR7U8e31MeRJFc3UldWVixD/QQ0hYGXR9mM1y', 'student@gmail.com', 'STUDENT'),
    ('student2', 'student2', '$2a$10$6HZfDrXpJT5Vh5MtZR7U8e31MeRJFc3UldWVixD/QQ0hYGXR9mM1y', 'student2@gmail.com', 'STUDENT'),
    ('instructor1', 'user3 last name', 'password3', 'instructor1@example.com', 'INSTRUCTOR'),
    ('instructor2', 'user4 last name', 'password4', 'instructor2@example.com', 'INSTRUCTOR');

INSERT INTO courses (name, status, start_date, started)
VALUES
    ('Course A', 'STOP', '2023-01-01', true),
    ('Course B', 'STARTED', '2023-02-01', false),
    ('Course C', 'WAIT', '2023-03-01', true);

INSERT INTO enrollments (user_id, course_id)
VALUES
    (1, 1),
    (2, 2),
    (3, 2),
    (4, 3),
    (5, 1);

INSERT INTO lessons (name, course_id)
VALUES
    ('Lesson 1', 1),
    ('Lesson 2', 1),
    ('Lesson 3', 2),
    ('Lesson 4', 3);

INSERT INTO files (file_name, file_data)
VALUES
    ('file1.txt', E'\\x48656c6c6f20576f726c64'),
    ('file2.txt', E'\\x576f726c642031322032303231');

INSERT INTO homework (mark, user_id, lesson_id, file_id)
VALUES
    (90, 1, 1, 1),
    (85, 2, 1, 2),
    (75, 3, 2, NULL);

INSERT INTO course_feedback (feedback_text, course_id, instructor_id, student_id)
VALUES
    ('Great course!', 1, 6, 1),
    ('Needs improvement', 2, 6, 2),
    ('Excellent instructor', 1, 7, 2);

INSERT INTO user_course_marks (user_id, course_id, total_score, passed)
VALUES
    (1, 1, 95.5, true),
    (2, 2, 88.0, true),
    (3, 2, 70.2, false);

INSERT INTO tokens (token, token_type, revoked, expired, user_id)
VALUES
    ('token123', 'Access', false, false, 1),
    ('token456', 'Refresh', false, false, 2);

