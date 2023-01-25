package com.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.domain.ResponseResult;
import com.domain.VO.RoleVo;
import com.domain.dto.RoleDto;
import com.domain.entity.Role;

import java.util.List;


/**
 * 角色信息表(Role)表服务接口
 *
 * @author makejava
 * @since 2023-01-15 15:35:08
 */
public interface RoleService extends IService<Role> {

    List<String> selectRoleKeyByUserId(Long id);

    ResponseResult roleList(Integer pageNum, Integer pageSize, String roleName, String status);

    ResponseResult changeRoleStatus(RoleVo roleVo);

    ResponseResult addRole(RoleDto roleDto);

    ResponseResult getRoleById(Long id);

    ResponseResult updateRole(RoleDto roleDto);

    ResponseResult deleteRole(Long id);

    ResponseResult listAllRole();

}
