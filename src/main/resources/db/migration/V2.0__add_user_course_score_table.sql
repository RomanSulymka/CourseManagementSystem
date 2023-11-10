--Added new table for final mark

CREATE TABLE IF NOT EXISTS user_course_marks
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

ALTER TABLE homework
    DROP CONSTRAINT fk_file_id;

ALTER TABLE homework
    ADD CONSTRAINT fk_file_id FOREIGN KEY (file_id) REFERENCES files (id) ON DELETE CASCADE;
