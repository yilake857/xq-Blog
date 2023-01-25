package com.service.imp;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.domain.ResponseResult;
import com.domain.VO.PageVo;
import com.domain.VO.UserInfoVo;
import com.domain.VO.UserVo;
import com.domain.dto.UserDto;
import com.domain.entity.Role;
import com.domain.entity.User;
import com.enums.AppHttpCodeEnum;
import com.exception.SystemException;
import com.mapper.UserMapper;
import com.service.RoleService;
import com.service.UserRoleService;
import com.service.UserService;
import com.utils.ArrayUtils;
import com.utils.BeanCopyUtils;
import com.utils.SecurityUtils;
import com.domain.entity.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.jws.soap.SOAPBinding;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 用户表(User)表服务实现类
 *
 * @author makejava
 * @since 2023-01-07 12:17:38
 */
@Service("userService")
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserRoleService userRoleService;
    @Autowired
    private RoleService roleService;
    @Override
    public ResponseResult userInfo() {
        //获取当前用户id
        Long userId = SecurityUtils.getUserId();
        //根据id查询用户信息
        User user = getById(userId);
        //封装为UserInfo
        UserInfoVo userInfoVo = BeanCopyUtils.copyBean(user, UserInfoVo.class);
        return ResponseResult.okResult(userInfoVo);
    }

    @Override
    public ResponseResult updateUserInfo(User user) {
        save(user);
        return ResponseResult.okResult();
    }

    @Override
    public ResponseResult register(User user) {
        //对数据进行非空判断
        if(!StringUtils.hasText(user.getUserName())){
            throw new SystemException(AppHttpCodeEnum.USERNAME_NOT_NULL);
        }
        if(!StringUtils.hasText(user.getPassword())){
            throw new SystemException(AppHttpCodeEnum.PASSWORD_NOT_NULL);
        }
        if(!StringUtils.hasText(user.getEmail())){
            throw new SystemException(AppHttpCodeEnum.EMAIL_NOT_NULL);
        }
        if(!StringUtils.hasText(user.getNickName())){
            throw new SystemException(AppHttpCodeEnum.NICKNAME_NOT_NULL);
        }
        //对数据进行是否存在的判断
        if(userNameExist(user.getUserName())){
            throw new SystemException(AppHttpCodeEnum.USERNAME_EXIST);
        }
        if(nickNameExist(user.getNickName())){
            throw new SystemException(AppHttpCodeEnum.NICKNAME_EXIST);
        }
        //对密码进行加密
        String encodePassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodePassword);
        //存入数据库
        save(user);
        return ResponseResult.okResult();
    }



    private boolean nickNameExist(String nickName) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getNickName,nickName);
        return count(queryWrapper) > 0;
    }

    private boolean userNameExist(String userName) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserName,userName);
        return count(queryWrapper) > 0;
    }

    @Override
    public ResponseResult userList(Integer pageNum, Integer pageSize, String userName, String phonenumber, String status) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(userName),User::getUserName,userName);
        wrapper.eq(StringUtils.hasText(phonenumber),User::getPhonenumber,phonenumber);
        wrapper.eq(StringUtils.hasText(status),User::getStatus,status);
        Page page = new Page<>(pageNum, pageSize);
        page(page,wrapper);
        return ResponseResult.okResult(new PageVo(page.getRecords(),page.getTotal()));
    }

    @Override
    public ResponseResult addUser(UserDto userDto) {
        /**
         * 注意：新增用户时注意密码加密存储。
         *
         * ​	用户名不能为空，否则提示：必需填写用户名
         *
         * ​	用户名必须之前未存在，否则提示：用户名已存在
         *
         * ​    手机号必须之前未存在，否则提示：手机号已存在
         *
         * ​	邮箱必须之前未存在，否则提示：邮箱已存在
         */
        //户名不能为空，否则提示：必需填写用户名
        if(!StringUtils.hasText(userDto.getUserName())){
            return ResponseResult.errorResult(500,"必需填写用户名");
        }
        //用户名必须之前未存在，否则提示：用户名已存在
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StringUtils.hasText(userDto.getUserName()),User::getUserName, userDto.getUserName());
        User user1 = getBaseMapper().selectOne(queryWrapper);
        if(!ObjectUtils.isEmpty(user1)) return ResponseResult.errorResult(500,"用户名已存在");
        //手机号必须之前未存在，否则提示：手机号已存在
        queryWrapper.clear();
        queryWrapper.eq(StringUtils.hasText(userDto.getPhonenumber()),User::getPhonenumber, userDto.getPhonenumber());
        User user2 = getBaseMapper().selectOne(queryWrapper);
        if(!ObjectUtils.isEmpty(user2)) return ResponseResult.errorResult(500,"手机号已存在");
        //邮箱必须之前未存在，否则提示：邮箱已存在
        queryWrapper.clear();
        queryWrapper.eq(StringUtils.hasText(userDto.getEmail()),User::getEmail, userDto.getEmail());
        User user3 = getBaseMapper().selectOne(queryWrapper);
        if(!ObjectUtils.isEmpty(user3)) return ResponseResult.errorResult(500,"邮箱已存在");
        //增加User 注意要加密储存
        User user = BeanCopyUtils.copyBean(userDto, User.class);
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        user.setPassword(encoder.encode(user.getPassword()));
        save(user);
        //查询刚添加的userid
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(userDto.getUserName()),User::getUserName,userDto.getUserName());
        wrapper.eq(StringUtils.hasText(userDto.getNickName()),User::getNickName,userDto.getNickName());
        wrapper.eq(StringUtils.hasText(userDto.getPassword()),User::getPassword,userDto.getPassword());
        wrapper.eq(StringUtils.hasText(userDto.getPhonenumber()),User::getPhonenumber,userDto.getPhonenumber());
        wrapper.eq(StringUtils.hasText(userDto.getEmail()),User::getEmail,userDto.getEmail());
        wrapper.eq(StringUtils.hasText(userDto.getSex()),User::getSex,userDto.getSex());
        wrapper.eq(StringUtils.hasText(userDto.getStatus()),User::getStatus,userDto.getStatus());
        User one = getBaseMapper().selectOne(wrapper);
        //增加User-Role
        List<Long> roleIds = userDto.getRoleIds();
        List<UserRole> userRoles = roleIds.stream()
                .map(aLong -> new UserRole(one.getId(),aLong))
                .collect(Collectors.toList());
        userRoleService.saveBatch(userRoles);
        return ResponseResult.okResult();
    }

    @Override
    public ResponseResult deleteUser(Long id) {
        //删除用户
        getBaseMapper().deleteById(id);
        //删除user-role关联关系
        LambdaQueryWrapper<UserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRole::getUserId, id);
        userRoleService.getBaseMapper().delete(wrapper);

        return ResponseResult.okResult();
    }

    @Override
    public ResponseResult getUserById(Long id) {
        //查询用户信息
        User user = getBaseMapper().selectById(id);
        //用户所关联的角色id列表
        LambdaQueryWrapper<UserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRole::getUserId,id);
        List<UserRole> userRoles = userRoleService.list(wrapper);
        List<Long> roleIds = new ArrayList<>();
        for (UserRole userRole :userRoles) {
            roleIds.add(userRole.getRoleId());
        }
        //角色的列表
        LambdaQueryWrapper<Role> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Role::getStatus,0);
        List<Role> roles = roleService.list(queryWrapper);
        UserVo userVo = new UserVo(roleIds, roles, user);
        return ResponseResult.okResult(userVo);

    }

    @Override
    public ResponseResult updateUser(UserDto userDto) {
        //更新user
        User user = BeanCopyUtils.copyBean(userDto, User.class);
        getBaseMapper().updateById(user);
        //更新user-role
        //获取新的user-role
        List<Long> roleIds = userDto.getRoleIds();
        List<UserRole> newUserRoles = roleIds.stream()
                .map(aLong -> new UserRole(userDto.getId(), aLong))
                .collect(Collectors.toList());
        //获取旧的user-role
        LambdaQueryWrapper<UserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRole::getUserId,userDto.getId());
        List<UserRole> oldUserRoles = userRoleService.list(wrapper);
        //ArrayUtils 取相同集合
        List<UserRole> theSameList = ArrayUtils.getTheSameList(oldUserRoles, newUserRoles);
        //去重
        oldUserRoles = ArrayUtils.duplicateRemoval(oldUserRoles,theSameList);
        newUserRoles = ArrayUtils.duplicateRemoval(newUserRoles,theSameList);
        //增加new
        if(!newUserRoles.isEmpty()){
            userRoleService.saveBatch(newUserRoles);
        }

        //删掉old
        if(!oldUserRoles.isEmpty()){
            for (UserRole userRole :oldUserRoles) {
                LambdaQueryWrapper<UserRole> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(UserRole::getUserId,userRole.getUserId());
                queryWrapper.eq(UserRole::getRoleId,userRole.getRoleId());
                userRoleService.getBaseMapper().delete(queryWrapper);
            }

        }
        return ResponseResult.okResult();


    }
}

