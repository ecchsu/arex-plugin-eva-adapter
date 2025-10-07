package com.eva.adapter;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark methods for AREX traffic capture and replay.
 * 
 * <p>Methods annotated with {@code @UseObjectPhase} will be instrumented by the
 * AREX plugin to record method invocations during normal operation and replay
 * them during testing.
 * 
 * <p>Usage example:
 * <pre>
 * public class PaymentService {
 *     &#64;UseObjectPhase
 *     public Payment processPayment(PaymentRequest request) {
 *         // This method will be captured and replayed by AREX
 *         return paymentGateway.process(request);
 *     }
 * }
 * </pre>
 * 
 * <p><b>Note:</b> This annotation requires the AREX agent plugin to be installed
 * and configured in your application.
 * 
 * @see <a href="https://doc.arextest.com">AREX Documentation</a>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface UseObjectPhase {
}
