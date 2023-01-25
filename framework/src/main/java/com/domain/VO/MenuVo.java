package com.domain.VO;

import com.domain.entity.Menu;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class MenuVo {

    private List<MenuVo> children;
    private Long id;
    private Long parentId;
    private String label;

}
