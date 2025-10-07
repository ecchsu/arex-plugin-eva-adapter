package com.eva.arex.plugin;

import io.arex.inst.extension.MethodInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.List;

import static java.util.Collections.singletonList;
import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * Instrumentation for all public methods in com.eva.adapter package
 */
public class EvaAdapterPackageInstrumentation extends TypeInstrumentation {

    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        // Match all classes in com.eva.adapter package and subpackages
        // Exclude AREX internal classes
        return nameStartsWith("com.eva.adapter")
                .and(not(nameStartsWith("io.arex")));
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        // Match all public methods (excluding constructors, getters, setters)
        ElementMatcher<MethodDescription> matcher = isPublic()
                .and(not(isConstructor()))
                .and(not(isGetter()))
                .and(not(isSetter()))
                .and(not(isSynthetic()));

        return singletonList(
            new MethodInstrumentation(
                matcher, 
                EvaAdapterMethodAdvice.class.getName()
            )
        );
    }

    /**
     * Helper method to check if method is a getter
     */
    private static ElementMatcher<MethodDescription> isGetter() {
        return nameStartsWith("get")
                .and(takesNoArguments())
                .and(not(returns(void.class)));
    }

    /**
     * Helper method to check if method is a setter
     */
    private static ElementMatcher<MethodDescription> isSetter() {
        return nameStartsWith("set")
                .and(takesArguments(1))
                .and(returns(void.class));
    }
}
