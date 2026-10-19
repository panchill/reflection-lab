package org.example.generator.types;

import org.example.generator.GenerationContext;

public class FloatGenerator implements TypeGenerator {
    @Override
    public boolean supports(Class<?> type) {
        return type == float.class || type == Float.class;
    }

    @Override
    public Object generate(Class<?> type, GenerationContext ctx) {
        return ctx.random().nextFloat(100);
    }
}
