package org.example.generator;

import org.example.classes.Rectangle;
import org.example.classes.Shape;
import org.example.classes.Triangle;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class GeneratorTest {

    private final Generator g = new Generator();

    enum Color { RED, GREEN, BLUE }

    enum Empty { }

    static class Person {
        final String name;
        final int age;
        final Color favorite;

        Person(String name, int age, Color favorite) {
            this.name = name;
            this.age = age;
            this.favorite = favorite;
        }
    }

    static class Secret {
        final int value;

        private Secret(int value) {
            this.value = value;
        }
    }

    @Generatable(implementations = {Dog.class})
    static abstract class Animal { }

    static class Dog extends Animal {
        final String name;

        Dog(String name) {
            this.name = name;
        }
    }

    static abstract class NoAnnotation { }

    @Generatable
    interface EmptyImpls { }

    @Generatable(implementations = {String.class})
    interface WrongImpl { }

    static class Node {
        final Node next;

        Node(Node next) {
            this.next = next;
        }
    }

    static class Failing {
        Failing() {
            throw new IllegalStateException("boom");
        }
    }

    @RepeatedTest(50)
    void generatesIntInRange() {
        int n = g.generateValueOfType(int.class);
        assertTrue(n >= 0 && n < 100, "int out of range: " + n);
    }

    @Test
    void generatesIntegerWrapper() {
        Object n = g.generateValueOfType(Integer.class);
        assertInstanceOf(Integer.class, n);
    }

    @Test
    void generatesAllPrimitiveTypes() {
        assertInstanceOf(Byte.class, g.generateValueOfType(byte.class));
        assertInstanceOf(Short.class, g.generateValueOfType(short.class));
        assertInstanceOf(Long.class, g.generateValueOfType(long.class));
        assertInstanceOf(Float.class, g.generateValueOfType(float.class));
        assertInstanceOf(Double.class, g.generateValueOfType(double.class));
        assertInstanceOf(Boolean.class, g.generateValueOfType(boolean.class));
        assertInstanceOf(Character.class, g.generateValueOfType(char.class));
    }

    @RepeatedTest(50)
    void generatesLowercaseLetterForChar() {
        char c = g.generateValueOfType(char.class);
        assertTrue(c >= 'a' && c <= 'z', "unexpected char: " + (int) c);
    }

    @RepeatedTest(50)
    void generatesDoubleInRange() {
        double d = g.generateValueOfType(double.class);
        assertTrue(d >= 0 && d < 100, "double out of range: " + d);
    }

    @Test
    void booleanTakesBothValues() {
        Set<Boolean> seen = new HashSet<>();
        for (int i = 0; i < 200; i++) {
            seen.add(g.generateValueOfType(boolean.class));
        }
        assertEquals(Set.of(true, false), seen);
    }

    @Test
    void generatesNonEmptyString() {
        String s = g.generateValueOfType(String.class);
        assertNotNull(s);
        assertFalse(s.isEmpty());
    }

    @Test
    void stringsAreNotAlwaysTheSame() {
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 50; i++) {
            seen.add(g.generateValueOfType(String.class));
        }
        assertTrue(seen.size() > 1, "generator always returns the same string");
    }

    @Test
    void generatesEnumConstant() {
        Color c = g.generateValueOfType(Color.class);
        assertNotNull(c);
    }

    @Test
    void enumTakesDifferentValues() {
        Set<Color> seen = new HashSet<>();
        for (int i = 0; i < 200; i++) {
            seen.add(g.generateValueOfType(Color.class));
        }
        assertEquals(Set.of(Color.values()), seen);
    }

    @Test
    void failsForEmptyEnum() {
        assertThrows(GenerationException.class, () -> g.generateValueOfType(Empty.class));
    }

    @RepeatedTest(20)
    void generatesIntArray() {
        int[] arr = g.generateValueOfType(int[].class);
        assertNotNull(arr);
        assertTrue(arr.length >= 1 && arr.length <= 5, "length: " + arr.length);
    }

    @Test
    void generatesStringArrayWithoutNulls() {
        String[] arr = g.generateValueOfType(String[].class);
        assertTrue(arr.length > 0);
        for (String s : arr) {
            assertNotNull(s);
        }
    }

    @Test
    void generatesEnumArray() {
        Color[] arr = g.generateValueOfType(Color[].class);
        assertTrue(arr.length > 0);
        for (Color c : arr) {
            assertNotNull(c);
        }
    }

    @Test
    void generatesObjectArray() {
        Rectangle[] arr = g.generateValueOfType(Rectangle[].class);
        assertTrue(arr.length > 0);
        for (Rectangle r : arr) {
            assertNotNull(r);
        }
    }

    @Test
    void generatesTwoDimensionalArray() {
        int[][] arr = g.generateValueOfType(int[][].class);
        assertTrue(arr.length > 0);
        for (int[] row : arr) {
            assertNotNull(row);
            assertTrue(row.length > 0);
        }
    }

    @Test
    void generatesNonEmptyListOfIntegers() {
        List<?> list = g.generateValueOfType(List.class);
        assertNotNull(list);
        assertFalse(list.isEmpty());
        for (Object item : list) {
            assertInstanceOf(Integer.class, item);
        }
    }

    @Test
    void generatesNonEmptyMapOfStringToInteger() {
        Map<?, ?> map = g.generateValueOfType(Map.class);
        assertNotNull(map);
        assertFalse(map.isEmpty());
        for (Map.Entry<?, ?> e : map.entrySet()) {
            assertInstanceOf(String.class, e.getKey());
            assertInstanceOf(Integer.class, e.getValue());
        }
    }

    @Test
    void generatesTemplateClass() {
        Object r = g.generateValueOfType(Rectangle.class);
        assertInstanceOf(Rectangle.class, r);
    }

    @Test
    void generatesCustomClassAndFillsConstructorArgs() {
        Person p = g.generateValueOfType(Person.class);
        assertNotNull(p);
        assertNotNull(p.name);
        assertNotNull(p.favorite);
        assertTrue(p.age >= 0 && p.age < 100);
    }

    @Test
    void usesPrivateConstructor() {
        Secret s = g.generateValueOfType(Secret.class);
        assertNotNull(s);
        assertTrue(s.value >= 0 && s.value < 100);
    }

    @Test
    void generatesShapeImplementation() {
        Object s = g.generateValueOfType(Shape.class);
        assertInstanceOf(Shape.class, s);
    }

    @Test
    void shapeUsesAllDeclaredImplementations() {
        Set<Class<?>> seen = new HashSet<>();
        for (int i = 0; i < 200; i++) {
            seen.add(g.generateValueOfType(Shape.class).getClass());
        }
        assertTrue(seen.contains(Rectangle.class), "Rectangle was never generated");
        assertTrue(seen.contains(Triangle.class), "Triangle was never generated");
    }

    @Test
    void generatesAbstractClassImplementation() {
        Object a = g.generateValueOfType(Animal.class);
        assertInstanceOf(Dog.class, a);
        assertNotNull(((Dog) a).name);
    }

    @Test
    void failsForInterfaceWithoutAnnotation() {
        assertThrows(GenerationException.class, () -> g.generateValueOfType(Runnable.class));
    }

    @Test
    void failsForAbstractClassWithoutAnnotation() {
        assertThrows(GenerationException.class, () -> g.generateValueOfType(NoAnnotation.class));
    }

    @Test
    void failsForEmptyImplementationList() {
        assertThrows(GenerationException.class, () -> g.generateValueOfType(EmptyImpls.class));
    }

    @Test
    void failsForImplementationThatIsNotSubtype() {
        GenerationException e = assertThrows(GenerationException.class,
                () -> g.generateValueOfType(WrongImpl.class));
        assertTrue(e.getMessage().contains(String.class.getName()));
        assertTrue(e.getMessage().contains(WrongImpl.class.getName()));
    }

    @Test
    void failsForInfiniteRecursion() {
        assertThrows(GenerationException.class, () -> g.generateValueOfType(Node.class));
    }

    @Test
    void wrapsConstructorExceptionWithCause() {
        GenerationException e = assertThrows(GenerationException.class,
                () -> g.generateValueOfType(Failing.class));
        assertNotNull(e.getCause(), "original exception must be kept as cause");
    }

    @Test
    void errorMessageContainsTypeName() {
        GenerationException e = assertThrows(GenerationException.class,
                () -> g.generateValueOfType(Runnable.class));
        assertTrue(e.getMessage().contains(Runnable.class.getName()));
    }
}