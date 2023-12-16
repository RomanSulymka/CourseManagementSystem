package edu.sombra.coursemanagementsystem.util;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.stereotype.Component;

import java.beans.PropertyDescriptor;
import java.util.Arrays;

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
}
