package ru.andreychuk.sandbox;

import lombok.SneakyThrows;
import ru.andreychuk.sandbox.domain.ConstructorWithParams;

import java.lang.reflect.*;
import java.util.*;

import static ru.andreychuk.sandbox.utils.ClassUtils.classHasNoFieldsToCopy;
import static ru.andreychuk.sandbox.utils.ClassUtils.isImmutableCollection;

public class DeepCopyService {


    public <T> T deepCopy(T object) {
        Class<?> clazz = object.getClass();
        T copy = instantiateCopy(object, clazz);
        while (clazz != null) {
            for (Field field : clazz.getDeclaredFields()) {
                copyField(object, copy, field);
            }
            clazz = clazz.getSuperclass();
        }
        return copy;
    }

    private <T> T instantiateCopy(T object, Class<?> clazz) {
        ConstructorWithParams constructorWithParams = pickConstructorByParamTypes(object, clazz);
        Constructor<?> constructor = constructorWithParams.getConstructor();
        Object[] arguments = constructorWithParams.getArguments();
        T copy;
        try {
            copy = (T) constructor.newInstance(arguments);
        } catch (InstantiationException e) {
            throw new RuntimeException("Instantiation exception: " + e.getMessage());
        } catch (IllegalAccessException e) {
            throw new RuntimeException("IllegalAccessException exception: " + e.getMessage());
        } catch (InvocationTargetException e) {
            throw new RuntimeException("InvocationTargetException exception: " + e.getMessage());
        }
        return copy;
    }

    private <T> ConstructorWithParams pickConstructorByParamTypes(T object, Class<?> clazz) {
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        return Arrays.stream(constructors)
                .map(constructor -> pickParametersSequence(constructor, object, clazz))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No parameters set found for the constructor of class " + clazz.getName()));
    }

    @SneakyThrows
    private <T> Optional<ConstructorWithParams> pickParametersSequence(Constructor<?> constructor, T object,
                                                                              Class<?> clazz) {
        if (constructor.getParameterCount() == 0) {
            constructor.setAccessible(true);
            return Optional.of(new ConstructorWithParams(constructor, new Object[0]));
        }
        Class<?>[] parameterTypes = constructor.getParameterTypes();
        Object[] arguments = new Object[parameterTypes.length];
        if (!typesSequenceMatches(parameterTypes, clazz.getDeclaredFields())) {
            return Optional.empty();
        }
        for (int i = 0; i < parameterTypes.length; i++) {
            Field field = clazz.getDeclaredFields()[i];
            field.setAccessible(true);
            Object value = field.get(object);
            if (isImmutableCollection(value.getClass())) {
                value = createCopyOfImmutableCollection(value);
            }
            arguments[i] = value;
        }
        constructor.setAccessible(true);
        return Optional.of(new ConstructorWithParams(constructor, arguments));


    }

    private boolean typesSequenceMatches(Class<?>[] parameterTypes, Field[] declaredFields) {
        List<Class<?>> parameterTypesTemp = new ArrayList<>(Arrays.asList(parameterTypes));
        List<Field> declaredFieldsTemp = new ArrayList<>(Arrays.asList(declaredFields));
        outer:
        while (!parameterTypesTemp.isEmpty()) {
            for (int i = 0; i < declaredFieldsTemp.size(); i++) {
                for (int j = 0; j < parameterTypesTemp.size(); j++) {
                    if (parameterTypesTemp.get(j).isAssignableFrom(declaredFieldsTemp.get(i).getType())) {
                        parameterTypesTemp.remove(j);
                        declaredFieldsTemp.remove(i);
                        continue outer;
                    }
                }
            }
            return false;
        }
        return true;
    }

    private <T> void copyField(T object, T copy, Field field) {
        try {
            field.setAccessible(true);
            Object value = field.get(object);
            if (value == null || Modifier.isFinal(field.getModifiers())) {
                return;
            }
            if (isImmutableCollection(value.getClass())) {
                field.set(copy, createCopyOfImmutableCollection(value));
            } else if (value.getClass().isArray()) {
                copyArrayField(copy, field, value);
            } else if (classHasNoFieldsToCopy(value.getClass())) {
                field.set(copy, value);
            } else {
                field.set(copy, deepCopy(value));
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("IllegalAccessException exception: " + e.getMessage());
        }
    }

    private <T> void copyArrayField(T copy, Field field, Object value) throws IllegalAccessException {
        int length = Array.getLength(value);
        Object newArray = Array.newInstance(value.getClass().getComponentType(), length);
        for (int i = 0; i < length; i++) {
            Array.set(newArray, i, deepCopy(Array.get(value, i)));
        }
        field.set(copy, newArray);
    }


    private Object createCopyOfImmutableCollection(Object object) {
        if (object instanceof Collection) {
            return List.copyOf((Collection<?>) object);
        } else if (object instanceof Map) {
            return Map.copyOf((Map<?, ?>) object);
        } else {
            throw new IllegalArgumentException("Object is not an immutable collection");
        }
    }
}
