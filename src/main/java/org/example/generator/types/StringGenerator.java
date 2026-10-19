package org.example.generator.types;

import org.example.generator.GenerationContext;

public class StringGenerator implements TypeGenerator{
    @Override
    public boolean supports(Class<?> type) {
        return type == String.class;
    }

    @Override
    public Object generate(Class<?> type, GenerationContext ctx) {
        return "str" + ctx.random().nextInt(1000);
    }
}
