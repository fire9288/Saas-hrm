package com.ihrm.common.interceptor;

import com.ihrm.common.entity.ResultCode;
import com.ihrm.common.exception.CommonException;
import com.ihrm.common.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class JwtInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    private  JwtUtils jwtUtils;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //1.获取请求头信息：名称=Authorization
        String authorization = request.getHeader("Authorization");
        if(!StringUtils.isEmpty(authorization)&&authorization.startsWith("Bearer")){

            String token = authorization.replace("Bearer","");

            Claims claims = jwtUtils.  parseJwt(token);

            if (claims!=null){
                String apis = (String)claims.get("apis");
                HandlerMethod h  = (HandlerMethod)handler;
                RequestMapping annotation = h.getMethodAnnotation(RequestMapping.class);
                String name = annotation.name();
                if(apis.contains(name)) {
                    request.setAttribute("user_claims", claims);
                    return true;
                }else{
                    throw new CommonException(ResultCode.UNAUTHORISE);
                }
            }
        }
        throw new CommonException(ResultCode.UNAUTHENTICATED);
    }
}
