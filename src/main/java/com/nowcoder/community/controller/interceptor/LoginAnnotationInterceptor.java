package com.nowcoder.community.controller.interceptor;

import com.nowcoder.community.annotation.LoginAnnotation;
import com.nowcoder.community.util.HostHolder;
import org.apache.ibatis.javassist.util.proxy.MethodHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;

@Component
public class LoginAnnotationInterceptor implements HandlerInterceptor {

    @Resource
    HostHolder hostHolder;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod){
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Method method = handlerMethod.getMethod();
            LoginAnnotation loginAnnotation = method.getAnnotation(LoginAnnotation.class);
            if (loginAnnotation != null && hostHolder.getUser() == null){
                response.sendRedirect(request.getContextPath()+"/login");
                return false;
            }
        }

        return true;
    }
}
