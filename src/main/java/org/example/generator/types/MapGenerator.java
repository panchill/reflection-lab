package org.example.generator.types;

import org.example.generator.GenerationContext;

import java.util.HashMap;
import java.util.Map;

public class MapGenerator implements TypeGenerator {
    @Override
    public boolean supports(Class<?> type) {
        return type == Map.class;
    }

    @Override
    public Object generate(Class<?> type, GenerationContext ctx) {
        return generateMap(String.class, Integer.class, ctx);
    }

    public static Map<Object, Object> generateMap(Class<?> keyType, Class<?> valueType, GenerationContext ctx) {
        int size = ctx.random().nextInt(1, 6);
        Map<Object, Object> map = new HashMap<>();
        for (int i = 0; i < size; i++) {
            map.put(ctx.generate(keyType), ctx.generate(valueType));
        }
        return map;
    }
}
