package ru.andreychuk.sandbox;

public class Main {

    public static void main(String[] args) {
        DeepCopyService deepCopyService = new DeepCopyService();
        Object object = new Object();
        Object anotherObject = deepCopyService.deepCopy(object);
        System.out.printf("New object is created: %b\n", object == anotherObject);
        //Object class is a bad example, but the idea is clear I hope
        System.out.printf("New object is the same as the old one: %b\n", object.equals(anotherObject));

    }

}