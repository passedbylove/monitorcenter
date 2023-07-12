package com.netmarch.monitorcenter.aspect;

import com.alibaba.fastjson.JSON;
import com.netmarch.monitorcenter.bean.BeanFactory;
import com.netmarch.monitorcenter.bean.Logs;
import com.netmarch.monitorcenter.service.LogsService;
import com.netmarch.monitorcenter.util.CkeckSession;
import com.netmarch.monitorcenter.util.EnumsUtil;
import com.netmarch.monitorcenter.util.IpUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.thymeleaf.util.ArrayUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @ClassName HttpLogAspect
 * @Description 操作日志
 * @Author 王顶奎
 * @Date 2018/12/1215:30
 * @Version 1.0
 **/
@Aspect
@Component
public class HttpLogAspect {

    private final static Logger logger = LoggerFactory.getLogger(HttpLogAspect.class);

    @Autowired
    LogsService logsService;

    //定义一个公用方法
    @Pointcut("execution(* com.netmarch.monitorcenter.controller.*.*(..))")
    public void HttpLog(){
    }

    /**
     * Around 环绕通知,环绕增强
     * @param jionpoint
     * @return
     * @throws Throwable
     */
    @Around("HttpLog()")
    public Object myAroundAdvice(ProceedingJoinPoint jionpoint)
            throws Throwable {
        Object o = null ;
        try {

            HttpServletRequest request =((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            HttpSession session =request.getSession();
            Map<String, String> map = CkeckSession.GET_MAP_MODULE_SESSION(session);
            /*初始化系统模块到session中*/
            if(map == null){
                map = EnumsUtil.getEnumValues("com.netmarch.monitorcenter.util.ModuleEnums");
                CkeckSession.SET_MAP_MODULE_SESSION(session,map);
            }

            Logs logs = BeanFactory.getLogs();
            //得到访问模块
            String module = jionpoint.getSignature().getDeclaringType().getSimpleName();

            //遍历枚举模块
            for (String key : map.keySet()) {
                if(module.equalsIgnoreCase(key)){
                    //访问模块
                    logs.setModule(map.get(key));
                    break;
                }
            }

            //返回发出请求的IP地址
            String ip = IpUtil.GET_IP(request).equals("0:0:0:0:0:0:0:1")?"127.0.0.1":IpUtil.GET_IP(request);
            logs.setIp(ip);

            //获取访问方法
            logs.setAction(jionpoint.getSignature().getName());
            Object[] args = jionpoint.getArgs();

            //序列化时过滤掉request和response
            List<Object> logArgs = this.streamOf(args)
                    .filter(arg -> (!(arg instanceof HttpServletRequest) && !(arg instanceof HttpServletResponse)))
                    .collect(Collectors.toList());

            StringBuffer str = BeanFactory.getStringBuffer();

            if (logArgs.size() > 0) {
                for (Object obj : logArgs) {
                    //过滤掉Map参数
                    if (!(obj instanceof HashMap) && obj != null) {
                        str.append("{");
                        str.append(JSON.toJSONString(obj).replace("{","").replace("}",""));
                        str.append("}");
                    }
                }
            }

            //获取访问内容
            logs.setDetail(str.toString());

            //进行下一步
            o = jionpoint.proceed();
            //获取处理结果
            logs.setResult(o.toString());
            logs.setExecuteTime(new Date());
            logsService.save(logs);

            return o;
        } catch (Exception e) {
            logger.info(e.getLocalizedMessage());
        }
        return o;
    }

    public static <T> Stream<T> streamOf(T[] array) {
        return ArrayUtils.isEmpty(array) ? Stream.empty() : Arrays.asList(array).stream();
    }

}