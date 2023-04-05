package ru.andreychuk.sandbox.utils;

import java.util.Set;

public class ClassUtils {

    public static boolean classHasNoFieldsToCopy(Class<?> clazz) {
        return clazz.isPrimitive() || clazz.equals(String.class) || getWrapperTypes().contains(clazz);
    }

    public static boolean isImmutableCollection(Class<?> clazz) {
        return clazz.getName().contains("Immutable")
                && clazz.getModule().getName().equals("java.base");
    }

    private static Set<Class<?>> getWrapperTypes() {
        return Set.of(
                Boolean.class,
                Character.class,
                Byte.class,
                Short.class,
                Integer.class,
                Long.class,
                Float.class,
                Double.class,
                Void.class
        );
    }
}
