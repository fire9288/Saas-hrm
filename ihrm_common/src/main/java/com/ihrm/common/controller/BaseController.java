package com.ihrm.common.controller;

import com.ihrm.domain.system.response.ProfileResult;
import io.jsonwebtoken.Claims;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.springframework.web.bind.annotation.ModelAttribute;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class BaseController {

    protected HttpServletRequest request;
    protected HttpServletResponse response;
    protected String companyId;
    protected String companyName;


    //使用jwt方式获取

//    @ModelAttribute
//    public void setResAnReq(HttpServletRequest request,HttpServletResponse response) {
//        this.request = request;
//        this.response = response;
//
//        Object obj  = request.getAttribute("user_claims");
//        if(obj!=null){
//            this.claims=(Claims)obj;
//            this.companyId = (String)claims.get("companyId");
//
//            this.companyName = (String)claims.get("companyName");
//
//        }
//        /**
//         * 目前使用 companyId = 1
//         *         companyName = "传智播客"
//         */
//
//    }


    //使用shiro方式获取
    @ModelAttribute
    public void setResAnReq(HttpServletRequest request,HttpServletResponse response) {
        this.request = request;
        this.response = response;

        Subject subject = SecurityUtils.getSubject();

        PrincipalCollection principals = subject.getPrincipals();





        if(principals!=null&&!principals.isEmpty()){

            System.out.println("Not null");
            ProfileResult result = (ProfileResult) principals.getPrimaryPrincipal();

            this.companyId = result.getCompanyId();
            System.out.println("The Company Id :"+this.companyId);
            this.companyName = result.getCompany();

        }
        /**
         * 目前使用 companyId = 1
         *         companyName = "传智播客"
         */

    }



}
