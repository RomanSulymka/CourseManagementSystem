package edu.sombra.coursemanagementsystem.util;

import edu.sombra.coursemanagementsystem.entity.User;
import edu.sombra.coursemanagementsystem.enums.RoleEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.stereotype.Component;

import java.beans.PropertyDescriptor;
import java.util.Arrays;

@Slf4j
@Component
public class BaseUtil {

    private BaseUtil() {
    }

    public static String[] getNullPropertyNames(Object entity) {
        final BeanWrapper src = new BeanWrapperImpl(entity);
        PropertyDescriptor[] pds = src.getPropertyDescriptors();

        return Arrays.stream(pds)
                .map(PropertyDescriptor::getName)
                .filter(name -> src.getPropertyValue(name) == null)
                .toArray(String[]::new);
    }

    public static void validateInstructor(User instructor, RoleEnum role) {
        if (instructor.getRole() != role) {
            log.error("User role should be: {}", role.name());
            throw new IllegalArgumentException("User role should be: " + role.name());
        }
    }
}
