package org.example.generator.types;

import org.example.generator.GenerationContext;

public class IntegerGenerator implements TypeGenerator {
    @Override
    public boolean supports(Class<?> type) {
        return type == int.class || type == Integer.class;
    }

    @Override
    public Object generate(Class<?> type, GenerationContext ctx) {
        return ctx.random().nextInt(100);
    }
}