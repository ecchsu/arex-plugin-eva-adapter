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
 * Instrumentation for methods annotated with @UseObjectPhase
 */
public class UseObjectPhaseAnnotationInstrumentation extends TypeInstrumentation {

    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        // Match any class that has methods with @UseObjectPhase annotation
        // Exclude AREX internal classes
        return declaresMethod(isAnnotatedWith(named("com.eva.adapter.UseObjectPhase")))
                .and(not(nameStartsWith("io.arex")));
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        // Match methods annotated with @UseObjectPhase
        ElementMatcher<MethodDescription> matcher = isAnnotatedWith(
            named("com.eva.adapter.UseObjectPhase")
        );

        return singletonList(
            new MethodInstrumentation(
                matcher, 
                UseObjectPhaseMethodAdvice.class.getName()
            )
        );
    }
}
