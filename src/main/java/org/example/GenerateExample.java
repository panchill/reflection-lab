package org.example;


import org.example.classes.Example;
import org.example.classes.Shape;
import org.example.generator.Generatable;
import org.example.generator.GenerationException;
import org.example.generator.Generator;

public class GenerateExample {
    public static void main(String[] args) {
        var gen = new Generator();
        var clas = int.class;
        var clas2 = Integer.class;
        var class3 = char.class;
        var class4 = boolean.class;
        enum niga
        {
            BLACK,
            WHITE,
            BLUE
        }
        var class5 = niga.class;
        var class8 = char.class;
        var a = int[].class;
        var s = String[].class;
        var c = niga[].class;
        var d = Example.class;
        var cc = Shape.class;
        // Случай 2: интерфейс БЕЗ аннотации
        interface Animal {}

        // Случай 3: аннотация с неправильной реализацией
        @Generatable(implementations = {String.class})
        interface Broken {}

        var aa = Animal2.class;

        try {
            Object generated = gen.generateValueOfType(aa);
            System.out.println(generated);
        } catch (GenerationException e) {
            throw new RuntimeException(e);
        }
    }
    @Generatable(implementations = {Dog.class})
    abstract class Animal2 {}

    class Dog extends Animal2 {
        Dog(String name) {}
    }
}