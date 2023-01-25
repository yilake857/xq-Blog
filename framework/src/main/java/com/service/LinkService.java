package com.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.domain.ResponseResult;
import com.domain.VO.LinkVo;
import com.domain.VO.PageVo;
import com.domain.dto.CategoryDto;
import com.domain.entity.Link;

public interface LinkService extends IService<Link> {
    ResponseResult getAllLink();

    ResponseResult<PageVo> pageLinkList(Integer pageNum, Integer pageSize, LinkVo linkVo);

    ResponseResult addLink(LinkVo linkVo);

    ResponseResult selectLinkById(Long id);

    ResponseResult updateLink(LinkVo linkVo);

    ResponseResult deleteLinkById(Long id);
}
