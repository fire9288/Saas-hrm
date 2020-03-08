package com.ihrm.company;


import com.ihrm.common.shiro.realm.IhrmRealm;
import com.ihrm.common.shiro.session.CustomSessionManager;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.crazycake.shiro.RedisCacheManager;
import org.crazycake.shiro.RedisManager;
import org.crazycake.shiro.RedisSessionDAO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class ShiroConfiguration {

    @Bean
    public IhrmRealm getRealm(){
        return new IhrmRealm();
    }

    @Bean
    public SecurityManager getSecurityManager (IhrmRealm  ihrmRealm){

        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();

        securityManager.setRealm(ihrmRealm);

        securityManager.setSessionManager(sessionManager());

        securityManager.setCacheManager(redisCacheManager());

        return securityManager;
    }

    @Bean
    public ShiroFilterFactoryBean shiroFilter(SecurityManager securityManager){
        ShiroFilterFactoryBean filterFactoryBean = new ShiroFilterFactoryBean();
        filterFactoryBean.setSecurityManager(securityManager);
        filterFactoryBean.setLoginUrl("/autherror?code=1");
        filterFactoryBean.setUnauthorizedUrl("/autherror?code=2");

        Map<String,String> filterMap = new LinkedHashMap<>();
        //filterMap.put("/user/home","anon");
        filterMap.put("/sys/login","anon");
        filterMap.put("/autherror","anon");
        filterMap.put("/**","authc");
        //特定权限使用注解进行授权

        filterFactoryBean.setFilterChainDefinitionMap(filterMap);

        return filterFactoryBean;

    }

    @Value("${spring.redis.host}")
    private String host;
    @Value("${spring.redis.port}")
    private int port;

    public RedisManager redisManager(){
        RedisManager redisManager = new RedisManager();
        redisManager.setHost(host);
        redisManager.setPort(port);
        return redisManager;
    }

    public RedisSessionDAO redisSessionDAO(){
        RedisSessionDAO redisSessionDAO = new RedisSessionDAO();
        redisSessionDAO.setRedisManager(redisManager());
        return redisSessionDAO;
    }

    public DefaultWebSessionManager sessionManager(){

        CustomSessionManager customSessionManager  = new CustomSessionManager();
        customSessionManager.setSessionDAO(redisSessionDAO());
        return customSessionManager;
    }


    public RedisCacheManager redisCacheManager (){


        RedisCacheManager redisCacheManager = new RedisCacheManager();
        redisCacheManager.setRedisManager(redisManager());
        return redisCacheManager;
    }




    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(SecurityManager securityManager){
        AuthorizationAttributeSourceAdvisor advisor = new AuthorizationAttributeSourceAdvisor();
        advisor.setSecurityManager(securityManager);
        return advisor;

    }
}
