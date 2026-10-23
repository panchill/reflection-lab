package org.example.generator.types;

import org.example.generator.GenerationContext;

public class BooleanGenerator implements TypeGenerator{
    @Override
    public boolean supports(Class<?> type) {
        return type == boolean.class || type == Boolean.class;
    }

    @Override
    public Object generate(Class<?> type, GenerationContext ctx) {
        return ctx.random().nextBoolean();
    }
}
