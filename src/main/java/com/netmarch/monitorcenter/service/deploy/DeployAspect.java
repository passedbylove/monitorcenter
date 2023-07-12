package com.netmarch.monitorcenter.service.deploy;

import com.netmarch.monitorcenter.bean.ServerDeploy;
import com.netmarch.monitorcenter.service.common.ExpiredMapContainer;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.lang.reflect.Method;

/** 
* @Description: DeployAspect 部署方法切面线程控制
* @Author: fengxiang
* @Date: 2019/1/10 13:32
*/ 
@Aspect
@Component
@Log4j2
public class DeployAspect {
    private static final String DEPLOY_LOCK = "deploy_lock_";
    private boolean isDeployMethod(Method method){
        return method.isAnnotationPresent(Deploy.class);
    }
    private void deployHandleBlock(@NotNull Integer id){
        String lockKey = DEPLOY_LOCK + id;
        // 等3秒
        Long millions =  3000L;
        Boolean lock = ExpiredMapContainer.get(lockKey);
        while (lock!=null && lock == true){
            try {
                Thread.sleep(millions);
            } catch (InterruptedException e) {
                log.error(e);
            }
        }
        ExpiredMapContainer.put(lockKey,new Boolean(true));
    }
    private void removeDeployLock(@NotNull Integer id){
        String lockKey = DEPLOY_LOCK + id;
        ExpiredMapContainer.put(lockKey,new Boolean(false));
    }
    /**
     * 定义切点
     */
    @Pointcut("execution(public * com.netmarch.monitorcenter.service.deploy.DeployExecutor.*(..))")
    public void deployHandle(){}
    @Around("deployHandle()")
    public Object deployHandleAround(ProceedingJoinPoint joinPoint){
        Object result = null;
        boolean isDeploy = false;
        Integer id = null;
        try {
            //获取访问目标方法
            MethodSignature methodSignature = (MethodSignature)joinPoint.getSignature();
            Method targetMethod = methodSignature.getMethod();
            isDeploy = isDeployMethod(targetMethod);
            //获取id
            id = null;
            if (isDeploy) {
                //参数
                Object[] args = joinPoint.getArgs();
                Object param = null;
                if (args != null && args.length > 0) {
                    param = args[0];
                }
                if (param != null && param instanceof ServerDeploy) {
                    id = ((ServerDeploy) param).getId();
                }
                if (param != null && param instanceof Integer) {
                    id = (Integer) param;
                }
            }
            //已经在执行就阻塞
            if (isDeploy && id!=null){
                deployHandleBlock(id);
            }
            result = joinPoint.proceed();

        } catch (Exception e) {
            log.error(e);
            return result;
        } catch (Throwable throwable) {
            log.error(throwable);
            return result;
        }finally {
            //释放资源
            if (isDeploy && id!=null){
                removeDeployLock(id);
            }
        }
        return result;
    }
}
