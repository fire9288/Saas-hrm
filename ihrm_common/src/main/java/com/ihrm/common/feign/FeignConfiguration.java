package com.ihrm.common.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

@Configuration
public class FeignConfiguration {

    public RequestInterceptor requestInterceptor(){

        return new RequestInterceptor(){


            @Override
            public void apply(RequestTemplate requestTemplate) {
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if(attributes!=null){
                    HttpServletRequest request = attributes.getRequest();

                    Enumeration<String> headerNames = request.getHeaderNames();

                    if(headerNames!=null){
                        while(headerNames.hasMoreElements()){

                            String name = headerNames.nextElement();
                            String value = request.getHeader(name);
                            requestTemplate.header(name,value);


                        }
                    }



                }


            }
        };

    }


}
