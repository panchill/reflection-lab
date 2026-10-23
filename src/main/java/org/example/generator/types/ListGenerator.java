package org.example.generator.types;

import org.example.generator.GenerationContext;

import java.util.ArrayList;
import java.util.List;

public class ListGenerator implements TypeGenerator {
    @Override
    public boolean supports(Class<?> type) {
        return type == List.class;
    }

    @Override
    public Object generate(Class<?> type, GenerationContext ctx) {
        return generateList(Integer.class, ctx);
    }

    public static List<Object> generateList(Class<?> elementType, GenerationContext ctx) {
        int size = ctx.random().nextInt(1, 6);
        List<Object> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(ctx.generate(elementType));
        }
        return list;
    }
}
