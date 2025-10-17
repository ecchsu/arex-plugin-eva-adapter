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
 * Instrumentation for all public methods under com.eva.adapter packages
 */
public class EvaAdapterPackageInstrumentation extends TypeInstrumentation {

    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        // Match all classes under com.eva.adapter packages and subpackages
        return nameStartsWith("com.eva.adapter");
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        // Match all public methods (excluding constructors, getters, setters)
        ElementMatcher<MethodDescription> matcher = isPublic()
                .and(not(isConstructor()))
                .and(not(isSynthetic()));

        return singletonList(
            new MethodInstrumentation(
                matcher, 
                EvaAdapterMethodAdvice.class.getName()
            )
        );
    }
}
