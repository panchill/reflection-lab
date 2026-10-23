package org.example.generator;

import org.example.generator.types.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Generator {
    private static final int MAX_DEPTH = 10;

    private final Random random = new Random();

    private final List<TypeGenerator> generators = List.of(
            new IntegerGenerator(),
            new ByteGenerator(),
            new ShortGenerator(),
            new LongGenerator(),
            new BooleanGenerator(),
            new CharacterGenerator(),
            new LongGenerator(),
            new FloatGenerator(),
            new DoubleGenerator(),
            new StringGenerator(),
            new EnumGenerator(),
            new ArrayGenerator(),
            new ListGenerator(),
            new MapGenerator(),
            new AbstractTypeGenerator(),
            new ObjectGenerator()
    );

    public <T> T generateValueOfType(Class<T> type) {
        return generate(type, 0, List.of());
    }

    @SuppressWarnings("unchecked")
    <T> T generate(Class<T> type, int depth, List<Class<?>> parentPath) {
        if (depth > MAX_DEPTH) {
            throw new GenerationException("Максимальная глубина рекурсии достигнута для " + type.getName());
        }
        List<Class<?>> path = new ArrayList<>(parentPath);
        path.add(type);

        GenerationContext ctx = new GenerationContext(this, random, depth, path);
        for (TypeGenerator g : generators) {
            if (g.supports(type)) {
                return (T) g.generate(type, ctx);
            }
        }
        throw new GenerationException("Неподдерживаемый тип: " + type.getName());
    }
}