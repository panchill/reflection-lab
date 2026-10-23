package org.example.generator.types;

import org.example.generator.GenerationContext;

public class CharacterGenerator implements TypeGenerator {
    @Override
    public boolean supports(Class<?> type) {
        return type == char.class || type == Character.class;
    }

    @Override
    public Object generate(Class<?> type, GenerationContext ctx) {
        return (char) ('a' + ctx.random().nextInt(26));
    }
}
