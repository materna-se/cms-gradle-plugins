/*
 * Copyright 2025 Materna Information & Communications SE
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.materna.cms.tools.tracelog;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * AspectJ Aspekt, welcher alle Methodenaufrufe auf trace loggt.
 */
@Aspect
@SuppressWarnings({"SpringAopErrorsInspection", "MissingAspectjAutoproxyInspection"})
public class LoggingAspect {

  private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

  private ThreadLocal<Integer> depth = ThreadLocal.withInitial(() -> 1);

  @Pointcut("execution(* *(..))"
      + " && !within(de.materna.cms.tools.tracelog.LoggingAspect)"
      + " && !@annotation(lombok.Generated)")
  public void cmsMethod() {
  }

  @Before("cmsMethod()")
  public void logEnter(JoinPoint joinPoint) {
    Logger logger = LoggerFactory.getLogger(joinPoint.getSignature().getDeclaringTypeName());

    if (logger.isTraceEnabled()) {
      int currentDepth = Math.max(depth.get(), 1);
      depth.set(currentDepth + 1);

      String callString = buildCallString(joinPoint);

      String indent = getIndent(currentDepth);

      logger.trace("{}> {}", indent, callString);
    }
  }

  @AfterReturning(value = "cmsMethod()", returning = "retVal")
  public void logExit(JoinPoint joinPoint, Object retVal) {
    Logger logger = LoggerFactory.getLogger(joinPoint.getSignature().getDeclaringTypeName());

    if (logger.isTraceEnabled()) {
      int currentDepth = Math.max(depth.get(), 2);
      depth.set(--currentDepth);

      String callString = buildCallString(joinPoint);
      if (((MethodSignature) joinPoint.getSignature()).getReturnType().equals(Void.TYPE)) {
        logger.trace("{}< {}", getIndent(currentDepth), callString);
      } else {
        logger.trace("{}< {} = {}", getIndent(currentDepth), callString, retVal);
      }
    }
  }

  @AfterThrowing(value = "cmsMethod()", throwing = "exception")
  public void logThrowing(JoinPoint joinPoint, Exception exception) {
    Logger logger = LoggerFactory.getLogger(joinPoint.getSignature().getDeclaringTypeName());

    if (logger.isTraceEnabled()) {
      int currentDepth = Math.max(depth.get(), 2);
      depth.set(--currentDepth);

      String callString = buildCallString(joinPoint);
      logger.trace("{}< {} throws {}({})",
          getIndent(currentDepth),
          callString,
          exception.getClass().getSimpleName(),
          exception.getLocalizedMessage());
    }
  }

  private String getIndent(int depth) {
    StringBuilder stringBuilder = new StringBuilder(depth);
    for (int i = 0; i < depth; i++) {
      stringBuilder.append('-');
    }
    return stringBuilder.toString();
  }

  private String buildCallString(JoinPoint joinPoint) {
    StringBuilder sb = new StringBuilder();
    MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();

    sb.append(methodSignature.getName());
    sb.append('(');

    String[] parameterNames = methodSignature.getParameterNames();
    Object[] args = joinPoint.getArgs();
    for (int i = 0; i < args.length; i++) {

      if (i > 0) {
        sb.append(", ");
      }

      if (parameterNames != null) {
        String name = parameterNames[i];
        sb.append(name).append(": ");
      }

      String value;
      try {
        value = Objects.toString(args[i], "<null>");
      } catch (Exception e) {
        log.debug("Error in toString()", e);
        value = "<error>";
      }
      sb.append(value);
    }
    sb.append(')');

    return sb.toString();
  }

}
