package com.domain.VO;

import com.domain.entity.Role;
import com.domain.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserVo {
    private List<Long> roleIds;
    private List<Role> roles;
    private User user;
}
