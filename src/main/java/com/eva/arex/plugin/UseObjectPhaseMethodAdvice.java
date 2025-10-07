package com.eva.arex.plugin;

import io.arex.agent.bootstrap.model.MockResult;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.serializer.Serializer;
import io.arex.inst.runtime.util.MockUtils;
import net.bytebuddy.asm.Advice;

/**
 * Advice class for recording and replaying methods annotated with @UseObjectPhase
 */
public class UseObjectPhaseMethodAdvice {

    @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class, suppress = Throwable.class)
    public static boolean onEnter(
            @Advice.Origin("#t") String className,
            @Advice.Origin("#m") String methodName,
            @Advice.AllArguments Object[] args,
            @Advice.Local("mockResult") MockResult mockResult) {

        if (ContextManager.needReplay()) {
            mockResult = replay(className, methodName, args);
            return mockResult != null && mockResult.notIgnoreMockResult();
        }
        return false;
    }

    @Advice.OnMethodExit(suppress = Throwable.class)
    public static void onExit(
            @Advice.Origin("#t") String className,
            @Advice.Origin("#m") String methodName,
            @Advice.AllArguments Object[] args,
            @Advice.Local("mockResult") MockResult mockResult,
            @Advice.Return(readOnly = false) Object result,
            @Advice.Thrown Throwable throwable) {

        if (mockResult != null && mockResult.notIgnoreMockResult()) {
            result = mockResult.getResult();
            return;
        }

        if (ContextManager.needRecord()) {
            record(className, methodName, args, result, throwable);
        }
    }

    private static void record(String className, String methodName, Object[] args, 
                               Object result, Throwable throwable) {
        try {
            Mocker mocker = buildMocker(className, methodName, args);
            
            if (throwable != null) {
                mocker.getTargetResponse().setAttribute("exception", throwable.getClass().getName());
                mocker.getTargetResponse().setAttribute("exceptionMessage", throwable.getMessage());
            } else {
                mocker.getTargetResponse().setBody(Serializer.serialize(result));
            }
            
            // Mark this as a UseObjectPhase method
            mocker.getTargetRequest().setAttribute("annotationType", "UseObjectPhase");
            
            MockUtils.recordMocker(mocker);
            
            System.out.println("[AREX] Recorded @UseObjectPhase method: " + className + "." + methodName);
        } catch (Throwable t) {
            System.err.println("[AREX] Failed to record @UseObjectPhase method: " + t.getMessage());
        }
    }

    private static MockResult replay(String className, String methodName, Object[] args) {
        try {
            Mocker mocker = buildMocker(className, methodName, args);
            Object replayResult = MockUtils.replayBody(mocker);
            
            if (replayResult != null) {
                System.out.println("[AREX] Replayed @UseObjectPhase method: " + className + "." + methodName);
                return MockResult.success(replayResult);
            }
        } catch (Throwable t) {
            System.err.println("[AREX] Failed to replay @UseObjectPhase method: " + t.getMessage());
        }
        return null;
    }

    private static Mocker buildMocker(String className, String methodName, Object[] args) {
        String operationName = "@UseObjectPhase:" + className + "." + methodName;
        Mocker mocker = MockUtils.createDynamicClass(operationName);
        
        if (args != null && args.length > 0) {
            mocker.getTargetRequest().setBody(Serializer.serialize(args));
        }
        
        mocker.getTargetRequest().setAttribute("className", className);
        mocker.getTargetRequest().setAttribute("methodName", methodName);
        mocker.getTargetRequest().setAttribute("annotationType", "UseObjectPhase");
        
        return mocker;
    }
}
