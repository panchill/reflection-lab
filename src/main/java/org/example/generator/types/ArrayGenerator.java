package org.example.generator.types;

import org.example.generator.GenerationContext;

import java.lang.reflect.Array;

public class ArrayGenerator implements TypeGenerator {
    @Override
    public boolean supports(Class<?> type) {
        return type.isArray();
    }

    @Override
    public Object generate(Class<?> type, GenerationContext ctx) {
        Class<?> typeComp = type.getComponentType();
        int size = ctx.random().nextInt(1, 6);
        Object arr = Array.newInstance(typeComp, size);
        for (int i = 0; i < size; i++) {
            Array.set(arr, i, ctx.generate(typeComp));
        }
        return arr;
    }
}
