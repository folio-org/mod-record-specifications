package org.folio.rspec.service;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RunOnProfile {
  String[] value();

  @Aspect
  @Configuration
  class RunOnProfileAspect {

    @Autowired
    Environment env;

    @Around("@annotation(runOnProfile)")
    public Object around(ProceedingJoinPoint joinPoint, RunOnProfile runOnProfile) throws Throwable {
      if (env.acceptsProfiles(Profiles.of(runOnProfile.value()))) {
        return joinPoint.proceed();
      }
      return null;
    }
  }
}
