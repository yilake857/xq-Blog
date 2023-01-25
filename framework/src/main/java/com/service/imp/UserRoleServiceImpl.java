package com.service.imp;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.domain.entity.UserRole;
import com.mapper.UserRoleMapper;
import com.service.UserRoleService;
import org.springframework.stereotype.Service;

/**
 * 用户和角色关联表(UserRole)表服务实现类
 *
 * @author makejava
 * @since 2023-01-19 15:32:37
 */
@Service("userRoleService")
public class UserRoleServiceImpl extends ServiceImpl<UserRoleMapper, UserRole> implements UserRoleService {

}
