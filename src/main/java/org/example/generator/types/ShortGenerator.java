package org.example.generator.types;

import org.example.generator.GenerationContext;

public class ShortGenerator implements TypeGenerator {
    @Override
    public boolean supports(Class<?> type) {
        return type == short.class || type == Short.class;
    }

    @Override
    public Object generate(Class<?> type, GenerationContext ctx) {
        return (short) ctx.random().nextInt(100);
    }
}
