package com.ihrm.system.client;

import com.ihrm.common.entity.Result;
import com.ihrm.domain.company.Department;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *
 * 声明接口
 *
 * 利用该接口来调用其他微服务中的方法
        */
@FeignClient("ihrm-company")
public interface DepartmentFeignClient {

    @RequestMapping(value = "/company/department/{id}", method = RequestMethod.GET)
    public Result findById(@PathVariable(value = "id") String id);

    @RequestMapping(value = "/department/search", method = RequestMethod.POST)
    public Department findByCode(@RequestParam(value = "code") String code, @RequestParam(value = "companyId") String companyId);


}