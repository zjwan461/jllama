//package com.itsu.oa.service.aop;
//
//import com.itsu.oa.service.ModelService;
//import org.aspectj.lang.JoinPoint;
//import org.aspectj.lang.ProceedingJoinPoint;
//import org.aspectj.lang.annotation.Around;
//import org.aspectj.lang.annotation.Aspect;
//import org.aspectj.lang.annotation.Pointcut;
//import org.springframework.stereotype.Component;
//
//import javax.annotation.Resource;
//
//@Component
//@Aspect
//public class ModelDownloadAspect {
//
//    @Resource
//    private ModelService modelService;
//
//
//    @Pointcut(value = "execution(public com.itsu.oa.service.impl.ModelScopeModelDownload.download(..)" )
//    public void rule() {
//    }
//
//
//    @Around(value = "rule()")
//    public Object handle(ProceedingJoinPoint joinPoint) throws Throwable {
//        Object result = null;
//        try {
//            Object[] args = joinPoint.getArgs();
//            result = joinPoint.proceed();
//
//        } catch (Throwable e) {
//            throw e;
//        }
//        return result;
//    }
//}
