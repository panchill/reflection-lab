package org.example.generator.types;

import org.example.generator.GenerationContext;

public interface TypeGenerator {
    boolean supports(Class<?> type);
    Object generate(Class<?> type, GenerationContext ctx);
}