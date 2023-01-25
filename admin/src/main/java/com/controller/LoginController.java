package com.controller;

import com.domain.ResponseResult;
import com.domain.VO.AdminUserInfoVo;
import com.domain.VO.RoutersVo;
import com.domain.VO.UserInfoVo;
import com.domain.entity.LoginUser;
import com.domain.entity.Menu;
import com.domain.entity.User;
import com.enums.AppHttpCodeEnum;
import com.exception.SystemException;
import com.service.LoginService;
import com.service.MenuService;
import com.service.RoleService;
import com.utils.BeanCopyUtils;
import com.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class LoginController {
    @Autowired
    private LoginService loginService;
    @Autowired
    private MenuService menuService;
    @Autowired
    private RoleService roleService;
    //admin 登录
    @RequestMapping("/user/login")
    public ResponseResult login(@RequestBody User user){
        if(!StringUtils.hasText(user.getUserName())){
            //提示 必须要传用户名
            throw new SystemException(AppHttpCodeEnum.REQUIRE_USERNAME);
        }
        return loginService.login(user);
    }
    //admin 退出登录
    @RequestMapping("/user/logout")
    public ResponseResult logout(){
        return loginService.logout();
    }
    //查询用户信息
    @GetMapping("getInfo")
    public ResponseResult<AdminUserInfoVo> getInfo(){
        //获取当前登录用户
        LoginUser loginUser = SecurityUtils.getLoginUser();
        //根据用户id查询权限信息
        List<String> permissions = menuService.selectPermsByUserId(loginUser.getUser().getId());
        //根据用户id查询角色信息
        List<String> roles = roleService.selectRoleKeyByUserId(loginUser.getUser().getId());
        //查询用户信息
        User user = loginUser.getUser();
        UserInfoVo userInfoVo = BeanCopyUtils.copyBean(user, UserInfoVo.class);
        //封装数据返回
        AdminUserInfoVo adminUserInfoVo = new AdminUserInfoVo(permissions, roles, userInfoVo);
        return ResponseResult.okResult(adminUserInfoVo);
    }
    //获取当前用户的菜单
    @GetMapping("getRouters")
    public ResponseResult<AdminUserInfoVo> getRouters(){
        Long userId = SecurityUtils.getUserId();
        //查询menu 结果是tree的形式
        List<Menu> menus = menuService.selectRouterMenuTreeByUserId(userId);
        //封装数据返回
        return ResponseResult.okResult(new RoutersVo(menus));
    }

}
