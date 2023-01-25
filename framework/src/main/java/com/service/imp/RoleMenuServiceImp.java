package com.service.imp;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.domain.entity.RoleMenu;
import com.mapper.RoleMenuMapper;
import com.service.RoleMenuService;
import org.springframework.stereotype.Service;

@Service("roleMenuService")
public class RoleMenuServiceImp extends ServiceImpl<RoleMenuMapper, RoleMenu> implements RoleMenuService {
}
