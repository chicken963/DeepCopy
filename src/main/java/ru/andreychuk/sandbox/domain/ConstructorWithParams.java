package ru.andreychuk.sandbox.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.lang.reflect.Constructor;

@Data
@AllArgsConstructor
public class ConstructorWithParams {
    private Constructor<?> constructor;
    private Object[] arguments;
}
