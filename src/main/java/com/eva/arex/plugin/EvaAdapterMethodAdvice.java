package com.eva.arex.plugin;

import io.arex.agent.bootstrap.model.MockResult;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.serializer.Serializer;
import io.arex.inst.runtime.util.MockUtils;
import net.bytebuddy.asm.Advice;

/**
 * Advice class for recording and replaying methods in defined packages
 * Records everything: setter, getter and void methods, all business logic
 *
 * Replay behavior:
 * All methods (including void) skip execution and use recorded data
 * Recorded packages act as complete mocks during replay
 */
public class EvaAdapterMethodAdvice {

    /**
     * Execute before the target method
     * If replay data exists, skip the original method execution
     */
    @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class, suppress = Throwable.class)
    public static Object onEnter(
            @Advice.Origin("#t") String className,
            @Advice.Origin("#m") String methodName,
            @Advice.AllArguments Object[] args) {

        System.out.println("[AREX] Method ENTER: " + className + "." + methodName);

        // Check if we need to replay
        if (ContextManager.needReplay()) {
            System.out.println("[AREX] Mode: REPLAY");
            MockResult mockResult = replay(className, methodName, args);
            if (mockResult != null && mockResult.notIgnoreMockResult()) {
                Object replayedData = mockResult.getResult();
                System.out.println("[AREX] Replay data found");

                //Check if this is a void method maker
                if (replayedData instanceof VoidMethodMarker) {
                    System.out.println("[AREX] This is a VOID method - will slip execution");
                    return true; //Skip orginal method
                }

                System.out.println("[AREX] Non-void method - will skip execution and return mocked result");
                return replayedData; // Return the replayed data, skip method execution
            } else {
                System.out.println("[AREX] No replay data - will execute original method");
            }
        } else if (ContextManager.needRecord()) {
            System.out.println("[AREX] Mode: RECORD");
        } else {
            System.out.println("[AREX] Mode: Normal (no record/replay)");
        }
        return null;
    }

    /**
     * Execute after the target method
     * Record the method result WITHOUT modifying it
     */
    @Advice.OnMethodExit(suppress = Throwable.class, onThrowable = Throwable.class)
    public static void onExit(
            @Advice.Origin("#t") String className,
            @Advice.Origin("#m") String methodName,
            @Advice.Origin("#r") String returnType,
            @Advice.AllArguments Object[] args,
            @Advice.Return Object result) {

        //Record the method execution
        if (ContextManager.needRecord()) {
            System.out.println("[AREX] Recording method execution...");

            //Check if this is a void method by checking the return type descriptor
            if ("void".equals(returnType)) {
                System.out.println("[AREX] Detected: Void method, creating VoidMethodMarker");

                //Record void method with VoidMethodMArker - use no-arg constructor
                VoidMethodMarker marker = new VoidMethodMarker();
                System.out.println("[AREX] VoidMethodMarker created for: " + className + "." + methodName);
                record(className, methodName, args, marker);
                System.out.println("[AREX] Void method recorded");
            } else {
                System.out.println("[AREX] Detected non-void method");

                if (result == null) {
                    System.out.println("[AREX] Return value is null");
                } else {
                    System.out.println("[AREX] Return value: " + result.getClass().getName());
                }

                //Record non-void method normally
                record(className, methodName, args, result);
                System.out.println("[AREX] Non-void method recorded");
            }
        } else if (ContextManager.needReplay()) {
            //In replay mode, log what happened
            if ("void".equals(returnType)) {
                System.out.println("[AREX] Void method was skipped during replay (using recorded data)");
            } else {
                System.out.println("[AREX] Non-void method was skipped during replay (returned recorded data)");
            }
        }
    }

    /**
     * Record method execution
     */
    public static void record(String className, String methodName, Object[] args, Object result) {
        try {
            System.out.println("[AREX] Building mocker for: " + className + "." + methodName);
            Mocker mocker = buildMocker(className, methodName, args);

            //Record the result
            if (result != null) {
                System.out.println("[AREX] Serializing result of type: " + className + "." + methodName);
                mocker.getTargetResponse().setBody(Serializer.serialize(result));
                mocker.getTargetResponse().setType(result.getClass().getName());
                System.out.println("[AREX] Result serialized successfully");
            } else {
                System.out.println("[AREX] Result is null, storing empty response");
            }

            System.out.println("[AREX] Calling MockUtils.recordMocker()...");
            MockUtils.recordMocker(mocker);;
            System.out.println("[AREX] Recorded method successfully: " + className + "." + methodName);
        } catch (Throwable t) {
            //Log but don't interrupt the application
            System.err.println("[AREX] Failed to record method: " + className + "." + methodName + " - " + t.getMessage());
            t.printStackTrace();
        }
    }

    /**
     * Replay method execution
     */
    public static MockResult replay(String className, String methodName, Object[] args) {
        try {
            System.out.println("[AREX] Building mocker for replay: " + className + "." + methodName);
            Mocker mocker = buildMocker(className, methodName, args);
            Object replayResult = MockUtils.replayBody(mocker);

            if (replayResult != null) {
                System.out.println("[AREX] Replayed method: " + className + "." + methodName);
                System.out.println("[AREX] Replayed result type: " + replayResult.getClass().getSimpleName());
                return MockResult.success(replayResult);
            } else {
                System.out.println("[AREX] No replay data found");
            }
        } catch (Throwable t) {
            System.err.println("[AREX] Failed to replay method: " + className + "." + methodName + " - " + t.getMessage());
        }
        return null;
    }

    /**
     * Build a Mocker object for the method call
     */
    public static Mocker buildMocker(String className, String methodName, Object[] args) {
        Mocker mocker = MockUtils.createDynamicClass(className, methodName);

        //Set request parameters
        if (args != null && args.length > 0) {
            try {
                mocker.getTargetRequest().setBody(Serializer.serialize(args));
                mocker.getTargetResponse().setType(args.getClass().getName());
            } catch (Exception e) {
                System.err.println("[AREX] Failed to serialize arguments for: " + className + "." + methodName);
            }
        }

        System.out.println("[AREX] Mocker created with operation: " + className + "." + methodName + " (type: DynamicClass)");

        return mocker;
    }
}
