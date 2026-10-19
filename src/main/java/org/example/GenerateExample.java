package org.example;

import org.example.classes.BinaryTreeNode;
import org.example.classes.Cart;
import org.example.classes.Example;
import org.example.classes.Product;
import org.example.classes.Rectangle;
import org.example.classes.Shape;
import org.example.classes.Triangle;
import org.example.generator.Generatable;
import org.example.generator.GenerationException;
import org.example.generator.Generator;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public class GenerateExample {

    private static final Generator GEN = new Generator();

    enum Color { RED, GREEN, BLUE }

    static class Person {
        private final String name;
        private final int age;
        private final Color favorite;

        Person(String name, int age, Color favorite) {
            this.name = name;
            this.age = age;
            this.favorite = favorite;
        }
    }

    @Generatable(implementations = {Dog.class, Cat.class})
    static abstract class Animal { }

    static class Dog extends Animal {
        private final String name;

        Dog(String name) {
            this.name = name;
        }
    }

    static class Cat extends Animal {
        private final int lives;

        Cat(int lives) {
            this.lives = lives;
        }
    }

    static class Node {
        private final int value;
        private final Node next;

        Node(int value, Node next) {
            this.value = value;
            this.next = next;
        }
    }

    interface NoAnnotation { }

    @Generatable(implementations = {String.class})
    interface Broken { }

    static class Failing {
        Failing() {
            throw new IllegalStateException("constructor failed");
        }
    }

    public static void main(String[] args) {
        section("Примитивы и обёртки");
        show(int.class);
        show(Integer.class);
        show(long.class);
        show(double.class);
        show(boolean.class);
        show(char.class);

        section("Строки и enum");
        show(String.class);
        show(Color.class);

        section("Массивы");
        show(int[].class);
        show(String[].class);
        show(Color[].class);
        show(int[][].class);

        section("List и Map");
        show(List.class);
        show(Map.class);

        section("Классы из шаблона");
        show(Rectangle.class);
        show(Triangle.class);
        show(Product.class);
        show(Example.class);
        show(Cart.class);
        show(BinaryTreeNode.class);

        section("Интерфейсы и абстрактные классы (@Generatable)");
        for (int i = 0; i < 3; i++) {
            show(Shape.class);
        }
        for (int i = 0; i < 3; i++) {
            show(Animal.class);
        }

        section("Собственные классы");
        show(Person.class);
        show(Node.class);

        section("Ошибки генерации");
        show(NoAnnotation.class);
        show(Broken.class);
        show(Failing.class);
        show(int[][][][][][][][][][][][].class);
    }

    private static void section(String title) {
        System.out.println();
        System.out.println("=== " + title + " ===");
    }

    private static void show(Class<?> type) {
        String label = type.getSimpleName();
        try {
            Object value = GEN.generateValueOfType(type);
            System.out.println(label + " -> " + describe(value, 0));
        } catch (GenerationException e) {
            System.out.println(label + " -> ОШИБКА: " + e.getMessage()
                    + (e.getCause() != null ? " (причина: " + e.getCause() + ")" : ""));
        }
    }

    private static String describe(Object obj, int depth) {
        if (obj == null) {
            return "null";
        }
        Class<?> c = obj.getClass();

        if (c.isArray()) {
            StringJoiner j = new StringJoiner(", ", "[", "]");
            for (int i = 0; i < Array.getLength(obj); i++) {
                j.add(describe(Array.get(obj, i), depth + 1));
            }
            return j.toString();
        }
        if (obj instanceof Collection<?> col) {
            StringJoiner j = new StringJoiner(", ", "[", "]");
            for (Object item : col) {
                j.add(describe(item, depth + 1));
            }
            return j.toString();
        }
        if (obj instanceof Map<?, ?> map) {
            StringJoiner j = new StringJoiner(", ", "{", "}");
            for (Map.Entry<?, ?> e : map.entrySet()) {
                j.add(describe(e.getKey(), depth + 1) + "=" + describe(e.getValue(), depth + 1));
            }
            return j.toString();
        }
        if (obj instanceof String s) {
            return "\"" + s + "\"";
        }
        if (hasOwnToString(c)) {
            return obj.toString();
        }
        if (depth > 5) {
            return c.getSimpleName() + "{...}";
        }

        StringJoiner j = new StringJoiner(", ", c.getSimpleName() + "{", "}");
        for (Field f : allInstanceFields(c)) {
            try {
                f.setAccessible(true);
                j.add(f.getName() + "=" + describe(f.get(obj), depth + 1));
            } catch (ReflectiveOperationException | RuntimeException e) {
                j.add(f.getName() + "=?");
            }
        }
        return j.toString();
    }

    private static boolean hasOwnToString(Class<?> c) {
        try {
            return c.getMethod("toString").getDeclaringClass() != Object.class;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    private static List<Field> allInstanceFields(Class<?> c) {
        List<Field> fields = new ArrayList<>();
        for (Class<?> k = c; k != null && k != Object.class; k = k.getSuperclass()) {
            for (Field f : k.getDeclaredFields()) {
                if (!Modifier.isStatic(f.getModifiers()) && !f.isSynthetic()) {
                    fields.add(f);
                }
            }
        }
        return fields;
    }
}