package org.example.generator.types;

import org.example.generator.GenerationContext;

public class DoubleGenerator implements TypeGenerator{
    @Override
    public boolean supports(Class<?> type) {
        return type == double.class || type == Double.class;
    }

    @Override
    public Object generate(Class<?> type, GenerationContext ctx) {
        return ctx.random().nextDouble(100);
    }
}
