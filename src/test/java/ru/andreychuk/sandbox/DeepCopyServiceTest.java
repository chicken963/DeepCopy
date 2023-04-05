package ru.andreychuk.sandbox;

import org.junit.Assert;
import org.junit.Test;
import ru.andreychuk.sandbox.data.Man;

import java.util.List;

public class DeepCopyServiceTest {

    private final DeepCopyService uut = new DeepCopyService();

    @Test
    public void shouldCopyObject_whenImmutableFieldsProvided(){
        Man man = new Man("Max", 42, List.of("Fight club", "1984", "Lord of the rings"));
        Man anotherMan = uut.deepCopy(man);
        Assert.assertEquals(man.getName(), anotherMan.getName());
        Assert.assertEquals(man.getAge(), anotherMan.getAge());
        for (int i = 0; i < man.getFavoriteBooks().size(); i++) {
            Assert.assertEquals(man.getFavoriteBooks().get(i), anotherMan.getFavoriteBooks().get(i));
        }
        Assert.assertNotSame(man, anotherMan);
    }
}
