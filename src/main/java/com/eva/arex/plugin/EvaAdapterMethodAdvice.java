package com.eva.arex.plugin;

import io.arex.agent.bootstrap.model.MockResult;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.serializer.Serializer;
import io.arex.inst.runtime.util.MockUtils;
import net.bytebuddy.asm.Advice;

/**
 * Advice class for recording and replaying methods in com.eva.adapter package
 */
public class EvaAdapterMethodAdvice {

    /**
     * Execute before the target method
     * If replay data exists, skip the original method execution
     */
    @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class, suppress = Throwable.class)
    public static boolean onEnter(
            @Advice.Origin("#t") String className,
            @Advice.Origin("#m") String methodName,
            @Advice.AllArguments Object[] args,
            @Advice.Local("mockResult") MockResult mockResult) {

        // Check if we need to replay
        if (ContextManager.needReplay()) {
            mockResult = replay(className, methodName, args);
            return mockResult != null && mockResult.notIgnoreMockResult();
        }
        return false;
    }

    /**
     * Execute after the target method
     * Record the method result or return mocked result
     */
    @Advice.OnMethodExit(suppress = Throwable.class)
    public static void onExit(
            @Advice.Origin("#t") String className,
            @Advice.Origin("#m") String methodName,
            @Advice.AllArguments Object[] args,
            @Advice.Local("mockResult") MockResult mockResult,
            @Advice.Return(readOnly = false) Object result,
            @Advice.Thrown Throwable throwable) {

        // If replay succeeded, return the mocked result
        if (mockResult != null && mockResult.notIgnoreMockResult()) {
            result = mockResult.getResult();
            return;
        }

        // Record the method execution
        if (ContextManager.needRecord()) {
            record(className, methodName, args, result, throwable);
        }
    }

    /**
     * Record method execution
     */
    private static void record(String className, String methodName, Object[] args, 
                               Object result, Throwable throwable) {
        try {
            Mocker mocker = buildMocker(className, methodName, args);
            
            // Record the result or exception
            if (throwable != null) {
                mocker.getTargetResponse().setAttribute("exception", throwable.getClass().getName());
                mocker.getTargetResponse().setAttribute("exceptionMessage", throwable.getMessage());
            } else {
                mocker.getTargetResponse().setBody(Serializer.serialize(result));
            }
            
            MockUtils.recordMocker(mocker);
        } catch (Throwable t) {
            // Log but don't interrupt the application
            System.err.println("[AREX] Failed to record EvaAdapter method: " + t.getMessage());
        }
    }

    /**
     * Replay method execution
     */
    private static MockResult replay(String className, String methodName, Object[] args) {
        try {
            Mocker mocker = buildMocker(className, methodName, args);
            Object replayResult = MockUtils.replayBody(mocker);
            
            if (replayResult != null) {
                return MockResult.success(replayResult);
            }
        } catch (Throwable t) {
            System.err.println("[AREX] Failed to replay EvaAdapter method: " + t.getMessage());
        }
        return null;
    }

    /**
     * Build a Mocker object for the method call
     */
    private static Mocker buildMocker(String className, String methodName, Object[] args) {
        // Create a dynamic mocker with operation name
        String operationName = className + "." + methodName;
        Mocker mocker = MockUtils.createDynamicClass(operationName);
        
        // Set request parameters
        if (args != null && args.length > 0) {
            mocker.getTargetRequest().setBody(Serializer.serialize(args));
        }
        
        // Add metadata
        mocker.getTargetRequest().setAttribute("className", className);
        mocker.getTargetRequest().setAttribute("methodName", methodName);
        mocker.getTargetRequest().setAttribute("parameterCount", String.valueOf(args != null ? args.length : 0));
        
        return mocker;
    }
}
