package edu.sombra.coursemanagementsystem.query;

public class SqlQueryConstants {
    public static final String EXIST_USER_BY_EMAIL_QUERY = "SELECT COUNT(u) FROM users u WHERE u.email = :email";
    public static final String FIND_USER_BY_EMAIL_QUERY = "SELECT u FROM users u WHERE email =: email";
    public static final String UPDATE_ROLE_BY_EMAIL_QUERY = "UPDATE users u SET u.role = :role WHERE u.email = :email";
    public static final String FIND_USERS_BY_EMAIL_QUERY = "SELECT u FROM users u WHERE u.email IN :emails";
    public static final String FIND_COURSE_BY_NAME_QUERY = "SELECT c FROM courses c WHERE c.name = :name";
    public static final String FIND_ALL_COURSES_QUERY = "SELECT c FROM courses c";
    public static final String FIND_ALL_USERS_QUERY = "SELECT u FROM users u";
    public static final String EXIST_COURSE_BY_NAME_QUERY = "SELECT COUNT(c) FROM courses c WHERE c.name = :name";

}
