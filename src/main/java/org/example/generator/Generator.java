package org.example.generator;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.*;

public class Generator {

    private final Random random = new Random();

    @SuppressWarnings("unchecked")
    private <T> T generate(Class<T> type, int depth)
    {
        if (depth > 10)
            throw new GenerationException("Превышено кол-во вызовов рекурсии: " + type.getName());
        if (type == int.class || type == Integer.class) {
            return (T) Integer.valueOf(random.nextInt(100));
        }

        if (type == byte.class || type == Byte.class) {
            return (T) Byte.valueOf((byte) random.nextInt(100));
        }

        if (type == short.class || type == Short.class) {
            return (T) Short.valueOf((short) random.nextInt(100));
        }

        if (type == long.class || type == Long.class) {
            return (T) Long.valueOf(random.nextLong(100));
        }

        if (type == float.class || type == Float.class) {
            return (T) Float.valueOf(random.nextFloat(100));
        }

        if (type == double.class || type == Double.class) {
            return (T) Double.valueOf(random.nextDouble() * 100);
        }

        if (type == boolean.class || type == Boolean.class) {
            return (T) Boolean.valueOf(random.nextBoolean());
        }

        if (type == char.class || type == Character.class) {
            return (T) Character.valueOf((char) ('a' + random.nextInt(26)));
        }

        if (type == String.class) {
            return (T) ("str" + random.nextInt(1000));
        }

        if (type.isEnum()) {
            T[] values = type.getEnumConstants();
            if (values.length == 0) {
                throw new GenerationException("Enum пуст: " + type.getName());
            }
            return values[random.nextInt(values.length)];
        }

        if (type.isArray()) {
            Class<?> typeComp = type.getComponentType();
            int size = random.nextInt(1, 6);
            Object arr = Array.newInstance(typeComp, size);
            for (int i = 0; i < size; i++) {
                Array.set(arr, i, generate(typeComp, depth + 1));
            }
            return (T) arr;
        }

        if (type == List.class) {
            int size = random.nextInt(1, 6);
            List<Integer> list = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                list.add(generate(Integer.class, depth + 1));
            }
            return (T) list;
        }

        if (type == Map.class) {
            int size = random.nextInt(1, 6);
            Map<String, Integer> map = new HashMap<>(size);
            for (int i = 0; i < size; i++) {
                map.put(generate(String.class, depth + 1), generate(Integer.class, depth + 1));
            }
            return (T) map;
        }

        if (type.isInterface() || Modifier.isAbstract(type.getModifiers())) {
            Generatable g = type.getAnnotation(Generatable.class);
            if (g == null) {
                throw new GenerationException(type.getName() + " абстрактны и не имеет реализации @Generatable");
            }

            Class<?>[] impls = g.implementations();
            if (impls.length == 0) {
                throw new GenerationException("@Generatable " + type.getName() + " не имеет реализации");
            }

            Class<?> impl = impls[random.nextInt(impls.length)];
            if (!type.isAssignableFrom(impl)) {
                throw new GenerationException(impl.getName() + " не является подтипом " + type.getName());
            }
            return (T) generate(impl, depth + 1);
        }

        Constructor<?>[] constructors = type.getDeclaredConstructors();

        if (constructors.length == 0) {
            throw new GenerationException("Нет конструкторов для " + type.getName());
        }

        Constructor<?> constructor = constructors[random.nextInt(constructors.length)];
        Class<?>[] arr = constructor.getParameterTypes();
        Object[] arrObjects = new Object[arr.length];
        for (int i = 0; i < arr.length; i++)
        {
            arrObjects[i] = generate(arr[i], depth + 1);
        }
        constructor.setAccessible(true);
        try {
            return (T) constructor.newInstance(arrObjects);
        } catch (ReflectiveOperationException e) {
            throw new GenerationException("Невозможно создать " + type.getName(), e);
        }
    }


    public <T> T generateValueOfType(Class<T> type){
        return generate(type, 0);
    }
}
