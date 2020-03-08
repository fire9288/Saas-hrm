package com.ihrm.system.controller;

import com.ihrm.common.controller.BaseController;
import com.ihrm.common.entity.PageResult;
import com.ihrm.common.entity.Result;
import com.ihrm.common.entity.ResultCode;

import com.ihrm.common.exception.CommonException;
import com.ihrm.common.utils.JwtUtils;
import com.ihrm.common.utils.PermissionConstants;
import com.ihrm.domain.system.Permission;
import com.ihrm.domain.system.Role;
import com.ihrm.domain.system.response.ProfileResult;
import com.ihrm.domain.system.User;
import com.ihrm.domain.system.response.UserResult;
import com.ihrm.system.client.DepartmentFeignClient;
import com.ihrm.system.service.PermissionService;
import com.ihrm.system.service.RoleService;
import com.ihrm.system.service.UserService;
import com.netflix.discovery.converters.Auto;
import com.sun.org.apache.xpath.internal.operations.Mult;
import io.jsonwebtoken.Claims;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//1.解决跨域
@CrossOrigin
//2.声明restContoller
@RestController
//3.设置父路径
@RequestMapping(value="/sys")
public class UserController extends BaseController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private DepartmentFeignClient departmentFeignClient;



    @RequestMapping(value = "/user/simple",method = RequestMethod.GET)
    public Result simpeltest() {

        return new Result(ResultCode.SUCCESS);
    }


    @RequestMapping("/user/upload/{id}")
    public Result upload(@PathVariable String id, @RequestParam(name="file")MultipartFile file) throws IOException {

        String imgUrl = userService.uploadImage(id,file);
        return new Result(ResultCode.SUCCESS,imgUrl);
    }

    /**
     *
     * 测试feign组件
     */
    @RequestMapping(value = "/test", method = RequestMethod.POST)
    public Result testFeign(@PathVariable(value = "id") String id){

          Result result = departmentFeignClient.findById(id);
         return result;

    }


    /**
     * 分配角色
     */

    @RequestMapping(value = "/user/assignRoles", method = RequestMethod.PUT)
    public Result save(@RequestBody Map<String,Object> map) {
        //1.获取被分配的用户id
        String userId = (String) map.get("id");
        //2.获取到角色的id列表
        List<String> roleIds = (List<String>) map.get("roleIds");
        //3.调用service完成角色分配
        userService.assignRoles(userId,roleIds);
        return new Result(ResultCode.SUCCESS);
    }

    /**
     * 保存
     */
    @RequestMapping(value = "/user", method = RequestMethod.POST)
    public Result save(@RequestBody User user) {
        //1.设置保存的企业id
        user.setCompanyId(companyId);
        user.setCompanyName(companyName);
        //2.调用service完成保存企业
        userService.save(user);
        //3.构造返回结果
        return new Result(ResultCode.SUCCESS);
    }

    /**
     * 查询企业的部门列表
     * 指定企业id
     */
    @RequestMapping(value = "/user", method = RequestMethod.GET)
    public Result findAll(int page, int size, @RequestParam Map map) {
        //1.获取当前的企业id
        map.put("companyId",companyId);
        //2.完成查询
        Page<User> pageUser = userService.findAll(map,page,size);
        //3.构造返回结果
        PageResult pageResult = new PageResult(pageUser.getTotalElements(),pageUser.getContent());
        return new Result(ResultCode.SUCCESS, pageResult);
    }

    /**
     * 根据ID查询user
     */
    @RequestMapping(value = "/user/{id}", method = RequestMethod.GET)
    public Result findById(@PathVariable(value = "id") String id) {
        // 添加 roleIds (用户已经具有的角色id数组)
        User user = userService.findById(id);
        UserResult userResult = new UserResult(user);
        return new Result(ResultCode.SUCCESS, userResult);
    }

    /**
     * 修改User
     */
    @RequestMapping(value = "/user/{id}", method = RequestMethod.PUT)
    public Result update(@PathVariable(value = "id") String id, @RequestBody User user) {
        //1.设置修改的部门id
        user.setId(id);
        //2.调用service更新
        userService.update(user);
        return new Result(ResultCode.SUCCESS);
    }

    /**
     * 根据id删除
     */
    @RequiresPermissions(value = "API-USER-DELETE")
    @RequestMapping(value = "/user/{id}", method = RequestMethod.DELETE,name="API-USER-DELETE")
    public Result delete(@PathVariable(value = "id") String id) {
        userService.deleteById(id);
        return new Result(ResultCode.SUCCESS);
    }


    /**
     *
     * 导入EXCEL
     * @return
     */
    @RequestMapping(value = "/user/import",method=RequestMethod.POST)
    public Result importuser(@RequestParam(name="file")MultipartFile file) throws IOException {

        Workbook wb = new XSSFWorkbook(file.getInputStream());

        Sheet sheet = wb.getSheetAt(0);

        List<User>list = new ArrayList<>();

        for(int rowNum=1;rowNum<=sheet.getLastRowNum();rowNum++){
            Row row = sheet.getRow(rowNum);
            Object[] values = new Object[row.getLastCellNum()];
            for(int cellNum=1;cellNum<row.getLastCellNum();cellNum++){
                Cell cell  = row.getCell(cellNum);
                Object value = getCellValue(cell);
                values[cellNum] =  value;

            }
            User user = new User(values);
            list.add(user);

        }

        userService.saveAll(list,companyId,companyName);
        return new Result(ResultCode.SUCCESS);
    }

    public static Object getCellValue(Cell cell) {
        //1.获取到单元格的属性类型
        CellType cellType = cell.getCellType();
        //2.根据单元格数据类型获取数据
        Object value = null;
        switch (cellType) {
            case STRING:
                value = cell.getStringCellValue();
                break;
            case BOOLEAN:
                value = cell.getBooleanCellValue();
                break;
            case NUMERIC:
                if(DateUtil.isCellDateFormatted(cell)) {
                    //日期格式
                    value = cell.getDateCellValue();
                }else{
                    //数字
                    value = cell.getNumericCellValue();
                }
                break;
            case FORMULA: //公式
                value = cell.getCellFormula();
                break;
            default:
                break;
        }
        return value;
    }


    /**
     * 用户登录
     *  1.通过service根据mobile查询用户
     *  2.比较password
     *  3.生成jwt信息
     *
     */
    @RequestMapping(value="/login",method = RequestMethod.POST)
    public Result login(@RequestBody Map<String,String> loginMap) {
        String mobile = loginMap.get("mobile");
        String password = loginMap.get("password");
        try{
            password  = new Md5Hash(password,mobile,3).toString();
            UsernamePasswordToken upToken = new UsernamePasswordToken(mobile,password);
            Subject subject = SecurityUtils.getSubject();
            subject.login(upToken);
            String sessionId = (String) subject.getSession().getId();
            return new Result(ResultCode.SUCCESS,sessionId);

        }catch(Exception e){
            return new Result(ResultCode.MOBILEORPASSWORDERROR);
        }
//        User user = userService.findByMobile(mobile);
//        //登录失败
//        if(user == null || !user.getPassword().equals(password)) {
//            return new Result(ResultCode.MOBILEORPASSWORDERROR);
//        }else {
//        //登录成功
//            //API权限字符串
//            StringBuilder sb = new StringBuilder();
//            for(Role role:user.getRoles()){
//                for(Permission permission:role.getPermissions()){
//                    if(permission.getType()== PermissionConstants.PERMISSION_API){
//                        sb.append(permission.getCode()).append(",");
//                    }
//                }
//            }
//            Map<String,Object> map = new HashMap<>();
//            map.put("companyId",user.getCompanyId());
//            map.put("companyName",user.getCompanyName());
//            map.put("apis",sb.toString());
//            String token = jwtUtils.createJwt(user.getId(), user.getUsername(), map);
//            return new Result(ResultCode.SUCCESS,token);
//        }
    }


    /**
     * 用户登录成功之后，获取用户信息
     *      1.获取用户id
     *      2.根据用户id查询用户
     *      3.构建返回值对象
     *      4.响应
     */
    @RequestMapping(value="/profile",method = RequestMethod.POST)
    public Result profile(HttpServletRequest request) throws Exception {

        /**
         * 从请求头信息中获取token数据
         *   1.获取请求头信息：名称=Authorization
         *   2.替换Bearer+空格
         *   3.解析token
         *   4.获取clamis
         */

//        String userid = claims.getId();
//        User user = userService.findById(userid);
//
//        ProfileResult result = null;
//
//        if("user".equals(user.getLevel())){
//            result = new ProfileResult(user);
//        }else{
//            Map map = new HashMap();
//            if("CoAdmin".equals(user.getLevel())){
//                map.put("enVisible","1");
//            }
//            List<Permission> list = permissionService.findAll(map);
//            result = new ProfileResult(user,list);
//
//        }


        Subject subject = SecurityUtils.getSubject();

        PrincipalCollection principals = subject.getPrincipals();

       ProfileResult result = (ProfileResult) principals.getPrimaryPrincipal();


        return new Result(ResultCode.SUCCESS,result);
    }
}
