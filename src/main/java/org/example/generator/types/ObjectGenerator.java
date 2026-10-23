package org.example.generator.types;

import org.example.generator.GenerationContext;
import org.example.generator.GenerationException;

import java.lang.reflect.*;
import java.util.List;
import java.util.Map;

public class ObjectGenerator implements TypeGenerator {

    private static final int MAX_CYCLE_DEPTH = 4;

    @Override
    public boolean supports(Class<?> type) {
        return !type.isPrimitive()
                && !type.isArray()
                && !type.isInterface()
                && !Modifier.isAbstract(type.getModifiers());
    }

    @Override
    public Object generate(Class<?> type, GenerationContext ctx) {
        Constructor<?>[] constructors = type.getDeclaredConstructors();
        if (constructors.length == 0) {
            throw new GenerationException("No constructors for " + type.getName());
        }

        Constructor<?> constructor = constructors[ctx.random().nextInt(constructors.length)];

        Class<?>[] paramTypes = constructor.getParameterTypes();
        Type[] genericTypes = constructor.getGenericParameterTypes();
        boolean hasGenerics = genericTypes.length == paramTypes.length;

        Object[] args = new Object[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            Type generic = hasGenerics ? genericTypes[i] : paramTypes[i];
            args[i] = generateArgument(paramTypes[i], generic, ctx);
        }

        try {
            constructor.setAccessible(true);
        } catch (InaccessibleObjectException e) {
            throw new GenerationException("Constructor of " + type.getName() + " is not accessible", e);
        }

        try {
            return constructor.newInstance(args);
        } catch (ReflectiveOperationException e) {
            throw new GenerationException("Cannot create " + type.getName(), e);
        }
    }

    private Object generateArgument(Class<?> raw, Type generic, GenerationContext ctx) {
        if (!raw.isPrimitive() && ctx.isCyclic(raw)) {
            if (ctx.depth() >= MAX_CYCLE_DEPTH || ctx.random().nextBoolean()) {
                return null;
            }
        }
        if (generic instanceof ParameterizedType p) {
            Type[] typeArgs = p.getActualTypeArguments();
            if (raw == List.class && typeArgs[0] instanceof Class<?> element) {
                return ListGenerator.generateList(element, ctx);
            }
            if (raw == Map.class
                    && typeArgs[0] instanceof Class<?> key
                    && typeArgs[1] instanceof Class<?> value) {
                return MapGenerator.generateMap(key, value, ctx);
            }
        }
        return ctx.generate(raw);
    }
}