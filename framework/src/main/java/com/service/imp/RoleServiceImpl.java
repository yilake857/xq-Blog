package com.service.imp;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.domain.ResponseResult;
import com.domain.VO.PageVo;
import com.domain.VO.RoleVo;
import com.domain.dto.RoleDto;
import com.domain.entity.Role;
import com.domain.entity.RoleMenu;
import com.domain.entity.UserRole;
import com.mapper.RoleMapper;
import com.service.MenuService;
import com.service.RoleMenuService;
import com.service.RoleService;
import com.service.UserRoleService;
import com.utils.ArrayUtils;
import com.utils.BeanCopyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 角色信息表(Role)表服务实现类
 *
 * @author makejava
 * @since 2023-01-15 15:35:08
 */
@Service("roleService")
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {
    @Autowired
    private RoleMenuService roleMenuService;
    @Autowired
    private UserRoleService userRoleService;
    @Override
    public List<String> selectRoleKeyByUserId(Long id) {
        //判断是否是管理员 如果是返回集合中只需要有admin
        if(id == 1L){
            List<String> list = new ArrayList<>();
            list.add("admin");
            return list;
        }
        //否则查询用户所具有的角色信息
        return getBaseMapper().selectRoleKeyByUserId(id);
    }

    @Override
    public ResponseResult roleList(Integer pageNum, Integer pageSize, String roleName, String status) {
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(roleName),Role::getRoleName,roleName);
        wrapper.eq(StringUtils.hasText(status),Role::getStatus,status);
        wrapper.orderByAsc(Role::getRoleSort);
        Page page = new Page<>(pageNum, pageSize);
        page(page,wrapper);
        return ResponseResult.okResult(new PageVo(page.getRecords(),page.getTotal()));
    }

    @Override
    public ResponseResult changeRoleStatus(RoleVo roleVo) {
        Role role = new Role();
        role.setId(roleVo.getRoleId());
        role.setStatus(roleVo.getStatus());
        getBaseMapper().updateById(role);
        return ResponseResult.okResult();
    }

    @Override
    public ResponseResult addRole(RoleDto roleDto) {
        //新增角色
        Role role = BeanCopyUtils.copyBean(roleDto, Role.class);
        getBaseMapper().insert(role);
        //查询刚添加的角色id
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Role::getRoleName,roleDto.getRoleName());
        wrapper.eq(Role::getRoleKey,roleDto.getRoleKey());
        wrapper.eq(Role::getRoleSort,roleDto.getRoleSort());
        wrapper.eq(Role::getStatus,roleDto.getStatus());
        wrapper.eq(Role::getRemark,roleDto.getRemark());
        Role role1 = getOne(wrapper);
        //增加关联的menus
        List<Long> menuIds = roleDto.getMenuIds();
        List<RoleMenu> roleMenus = menuIds.stream()
                .map(id -> new RoleMenu(role1.getId(), id))
                .collect(Collectors.toList());
        roleMenuService.saveBatch(roleMenus);
        return ResponseResult.okResult();
    }

    @Override
    public ResponseResult getRoleById(Long id) {
        Role role = getBaseMapper().selectById(id);
        return ResponseResult.okResult(role);
    }

    @Override
    public ResponseResult updateRole(RoleDto roleDto) {
        //更新角色
        Role role = BeanCopyUtils.copyBean(roleDto, Role.class);
        getBaseMapper().updateById(role);
        //更新角色-菜单列表
        //得到newRoleMenu list
        List<Long> menuIds = roleDto.getMenuIds();
        List<RoleMenu> newRoleMenus = menuIds.stream()
                .map(aLong -> new RoleMenu(roleDto.getId(), aLong))
                .collect(Collectors.toList());
        //得到oldRoleMenu list
        LambdaQueryWrapper<RoleMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RoleMenu::getRoleId,roleDto.getId());
        List<RoleMenu> oldRoleMenus = roleMenuService.list(wrapper);
        //工具集获得 sameList
        List<RoleMenu> theSameList = ArrayUtils.getTheSameList(oldRoleMenus, newRoleMenus);
        //获得去重后的new old list
        newRoleMenus = ArrayUtils.duplicateRemoval(newRoleMenus,theSameList);
        oldRoleMenus = ArrayUtils.duplicateRemoval(oldRoleMenus,theSameList);
        //对new添加 判断是否为空
        if(!newRoleMenus.isEmpty()){
            roleMenuService.saveBatch(newRoleMenus);
        }
        //对old删除 判断是否为空
        if(!oldRoleMenus.isEmpty()){
            for (RoleMenu roleMenu :oldRoleMenus) {
                LambdaQueryWrapper<RoleMenu> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(RoleMenu::getRoleId,roleMenu.getRoleId());
                queryWrapper.eq(RoleMenu::getMenuId,roleMenu.getMenuId());
                roleMenuService.getBaseMapper().delete(queryWrapper);
            }
        }
        //返回结果
        return ResponseResult.okResult();
    }

    @Override
    public ResponseResult deleteRole(Long id) {
        //删除角色
        getBaseMapper().deleteById(id);
        //删除对应的Role-Menu
        LambdaQueryWrapper<RoleMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RoleMenu::getRoleId,id);
        roleMenuService.getBaseMapper().delete(wrapper);
        //删除user-role关联关系
        LambdaQueryWrapper<UserRole> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserRole::getRoleId,id);
        userRoleService.getBaseMapper().delete(queryWrapper);
        return ResponseResult.okResult();
    }

    @Override
    public ResponseResult listAllRole() {
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Role::getStatus,0);
        List<Role> roles = list(wrapper);
        return ResponseResult.okResult(roles);
    }
}
