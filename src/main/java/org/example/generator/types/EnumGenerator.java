package org.example.generator.types;

import org.example.generator.GenerationContext;
import org.example.generator.GenerationException;

public class EnumGenerator implements TypeGenerator {
    @Override
    public boolean supports(Class<?> type) {
        return type.isEnum();
    }

    @Override
    public Object generate(Class<?> type, GenerationContext ctx) {
        Object[] values = type.getEnumConstants();
        if (values.length == 0) {
            throw new GenerationException("Enum has no constants: " + type.getName());
        }
        return values[ctx.random().nextInt(values.length)];
    }
}
