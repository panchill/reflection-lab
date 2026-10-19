package org.example.generator.types;

import org.example.generator.GenerationContext;

public class ByteGenerator implements TypeGenerator {
    @Override
    public boolean supports(Class<?> type) {
        return type == byte.class || type == Byte.class;
    }

    @Override
    public Object generate(Class<?> type, GenerationContext ctx) {
        return (byte) ctx.random().nextInt(100);
    }
}
