package com.domain.dto;

import com.domain.VO.MenuVo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MenuTreeDto {
    private List<MenuVo> menus;
    private List<Long> checkedKeys;
}
