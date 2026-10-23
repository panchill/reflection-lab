package org.example.generator.types;

import org.example.generator.GenerationContext;
import org.example.generator.GenerationException;
import org.example.generator.Generatable;

import java.lang.reflect.Modifier;

public class AbstractTypeGenerator implements TypeGenerator {

    @Override
    public boolean supports(Class<?> type) {
        return type.isInterface() || Modifier.isAbstract(type.getModifiers());
    }

    @Override
    public Object generate(Class<?> type, GenerationContext ctx) {
        Generatable annotation = type.getAnnotation(Generatable.class);
        if (annotation == null) {
            throw new GenerationException(type.getName() + " не помечен аннотацией @Generatable");
        }

        Class<?>[] impls = annotation.implementations();
        if (impls.length == 0) {
            throw new GenerationException("@Generatable на " + type.getName() + " не имеет реализации");
        }

        Class<?> impl = impls[ctx.random().nextInt(impls.length)];
        if (!type.isAssignableFrom(impl)) {
            throw new GenerationException(impl.getName() + " не является подтипом " + type.getName());
        }

        return ctx.generate(impl);
    }
}