package com.service.imp;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.constants.SystemConstants;
import com.domain.ResponseResult;
import com.domain.VO.MenuVo;
import com.domain.dto.MenuTreeDto;
import com.domain.entity.Menu;
import com.domain.entity.RoleMenu;
import com.mapper.MenuMapper;
import com.service.MenuService;
import com.service.RoleMenuService;
import com.utils.BeanCopyUtils;
import com.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 菜单权限表(Menu)表服务实现类
 *
 * @author makejava
 * @since 2023-01-15 15:35:28
 */
@Service("menuService")
public class MenuServiceImpl extends ServiceImpl<MenuMapper, Menu> implements MenuService {

    @Autowired
    private RoleMenuService roleMenuService;
    @Override
    public List<String> selectPermsByUserId(Long id) {
        //如果是管理员，返回所有的权限
        if(id == 1L){
            LambdaQueryWrapper<Menu> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.in(Menu::getMenuType,SystemConstants.MENU,SystemConstants.BUTTON);
            lambdaQueryWrapper.eq(Menu::getStatus, SystemConstants.STATUS_NORMAL);
            List<Menu> menuList = list(lambdaQueryWrapper);
            List<String> perms = menuList.stream()
                    .map(Menu::getPerms)
                    .collect(Collectors.toList());
            return perms;

        }
        //否则返回所具有的权限
        return getBaseMapper().selectPermsByUserId(id);
    }

    @Override
    public List<Menu> selectRouterMenuTreeByUserId(Long userId) {
        MenuMapper menuMapper = getBaseMapper();
        List<Menu> menus = null;
        //判断是否是管理员
        if(SecurityUtils.isAdmin()){
            //如果是 获取所有符合要求的Menu
            menus = menuMapper.selectAllRouterMenu();
        }else{
            //否则  获取当前用户所具有的Menu
            menus = menuMapper.selectRouterMenuTreeByUserId(userId);
        }
        //构建tree
        //先找出第一层的菜单  然后去找他们的子菜单设置到children属性中
        List<Menu> menuTree = builderMenuTree(menus,0L);
        return menuTree;
    }


    //构建树
    private List<Menu> builderMenuTree(List<Menu> menus, Long parentId) {
        List<Menu> menuList = menus.stream()
                //过滤 留下menu.getParentId == parentId
                .filter(menu -> menu.getParentId().equals(parentId))
                //设置集合中的每个menu的children
                .map(menu -> menu.setChildren(getChildren(menu,menus)))
                .collect(Collectors.toList());
        return menuList;
    }

    /**
     * 获取存入参数的 子Menu集合
     * @param menu
     * @param menus
     * @return
     */
    private List<Menu> getChildren(Menu menu, List<Menu> menus) {
        List<Menu> childrenList = menus.stream()
                .filter(m -> m.getParentId().equals(menu.getId()))
                //设置集合中的每个menu的children
                .map(m -> m.setChildren(getChildren(m,menus)))
                .collect(Collectors.toList());

        return childrenList;
    }

    @Override
    public ResponseResult menuList(String status, String menuName) {
        LambdaQueryWrapper<Menu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(status),Menu::getStatus,status);
        wrapper.like(StringUtils.hasText(menuName),Menu::getMenuName,menuName);
        wrapper.orderByAsc(Menu::getOrderNum);
        List<Menu> menuList = list(wrapper);
        return ResponseResult.okResult(menuList);
    }

    @Override
    public ResponseResult addMenu(Menu menu) {
        getBaseMapper().insert(menu);
        return ResponseResult.okResult();
    }

    @Override
    public ResponseResult selectMenuById(Long id) {
        Menu menu = getBaseMapper().selectById(id);
        return ResponseResult.okResult(menu);
    }

    @Override
    public ResponseResult updateMenu(Menu menu) {
        Long id = menu.getId();
        Long parentId = menu.getParentId();
        if(id.equals(parentId) ) return ResponseResult.errorResult(500,"修改菜单'" + menu.getMenuName() + "'失败，上级菜单不能选择自己");
        getBaseMapper().updateById(menu);
        return ResponseResult.okResult();
    }

    @Override
    public ResponseResult deleteMenu(Long menuId) {
        Menu menu = getBaseMapper().selectById(menuId);
        LambdaQueryWrapper<Menu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Menu::getParentId,menuId);
        wrapper.eq(Menu::getStatus,0);
        wrapper.eq(Menu::getDelFlag,0);
        List<Menu> list = list(wrapper);
        if(list.isEmpty()){
            getBaseMapper().deleteById(menuId);
            //并将role_menu关系删除
            LambdaQueryWrapper<RoleMenu> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(RoleMenu::getMenuId,menuId);
            roleMenuService.getBaseMapper().delete(queryWrapper);
            return ResponseResult.okResult();
        }else {
            return ResponseResult.errorResult(500,"存在子菜单不允许删除");
        }

    }
    // 获取菜单树接口
    @Override
    public ResponseResult treeSelect() {
        //查询所有Menu
        LambdaQueryWrapper<Menu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Menu::getStatus,0);
        List<Menu> list = list(wrapper);
        //获得没有children的MenuVos
        List<MenuVo> menuVos = new ArrayList<>();
        for (Menu menu :list) {
            MenuVo menuVo = new MenuVo();
            menuVo.setId(menu.getId());
            menuVo.setLabel(menu.getMenuName());
            menuVo.setParentId(menu.getParentId());
            menuVos.add(menuVo);
        }
        List<MenuVo> menuVoList = VoToTree(menuVos, 0L);
        return ResponseResult.okResult(menuVoList);
    }



    //将MenuVo 转为Tree结构
    public List<MenuVo> VoToTree(List<MenuVo> menuVos,Long parentId){
        List<MenuVo> menuVoList = menuVos.stream()
                .filter(menuVo -> menuVo.getParentId().equals(parentId))
                .map(menuVo -> menuVo.setChildren(getVoChildren(menuVo, menuVos)))
                .collect(Collectors.toList());
        return menuVoList;
    }
    //MenuVo
    private List<MenuVo> getVoChildren(MenuVo menuVo, List<MenuVo> menuVos) {
        List<MenuVo> collect = menuVos.stream()
                .filter(m -> m.getParentId().equals(menuVo.getId()))
                .map(m -> m.setChildren(getVoChildren(m, menuVos)))
                .collect(Collectors.toList());
        return collect;
    }

    @Override
    public ResponseResult roleMenuTreeselect(Long id) {
        //查询role_id的menu_id集合
        LambdaQueryWrapper<RoleMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RoleMenu::getRoleId,id);
        List<RoleMenu> roleMenus = roleMenuService.list(wrapper);
        List<Long> menus = new ArrayList<>();
        for (RoleMenu roleMenu :roleMenus) {
            menus.add(roleMenu.getMenuId());
        }
        //获取menu实体集合
        if(menus.isEmpty()) return ResponseResult.okResult();
            List<Menu> menuList = getBaseMapper().selectBatchIds(menus);
            //先转化为MenuVo
            //获得没有children的MenuVos
            List<MenuVo> menuVos = new ArrayList<>();
            for (Menu menu :menuList) {
                MenuVo menuVo = new MenuVo();
                menuVo.setId(menu.getId());
                menuVo.setLabel(menu.getMenuName());
                menuVo.setParentId(menu.getParentId());
                menuVos.add(menuVo);
            }
            //转换成树结构
            List<MenuVo> menuVoList = VoToTree(menuVos, 0L);
//        checkedKeys角色所关联的菜单权限id列表。
        // menuVoList menus


        return ResponseResult.okResult(new MenuTreeDto(menuVoList,menus));
    }

}
