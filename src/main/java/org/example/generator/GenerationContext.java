package org.example.generator;

import java.util.List;
import java.util.Random;

public class GenerationContext {
    private final Generator generator;
    private final Random random;
    private final int depth;
    private final List<Class<?>> path;

    GenerationContext(Generator generator, Random random, int depth, List<Class<?>> path) {
        this.generator = generator;
        this.random = random;
        this.depth = depth;
        this.path = path;
    }

    public Random random() {
        return random;
    }

    public int depth() {
        return depth;
    }

    public <T> T generate(Class<T> type) {
        return generator.generate(type, depth + 1, path);
    }

    public boolean isCyclic(Class<?> type) {
        if (type == Object.class) {
            return false;
        }
        for (Class<?> c : path) {
            if (type.isAssignableFrom(c)) {
                return true;
            }
        }
        return false;
    }
}