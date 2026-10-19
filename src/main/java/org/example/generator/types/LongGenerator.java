package org.example.generator.types;

import org.example.generator.GenerationContext;

public class LongGenerator implements TypeGenerator {
    @Override
    public boolean supports(Class<?> type) {
        return type == long.class || type == Long.class;
    }

    @Override
    public Object generate(Class<?> type, GenerationContext ctx) {
        return ctx.random().nextLong(100);
    }
}
