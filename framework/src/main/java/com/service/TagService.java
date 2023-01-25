package com.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.domain.ResponseResult;
import com.domain.VO.PageVo;
import com.domain.dto.TagListDto;
import com.domain.entity.Tag;


/**
 * 标签(Tag)表服务接口
 *
 * @author makejava
 * @since 2023-01-09 20:33:14
 */
public interface TagService extends IService<Tag> {

    ResponseResult<PageVo> pageTagList(Integer pageNum, Integer pageSize, TagListDto tagListDto);

    ResponseResult addTag(TagListDto tagListDto);

    ResponseResult deleteTagById(long id);

    ResponseResult getTagById(long id);

    ResponseResult updateTag(Tag tag);

    ResponseResult listAllTag();

}
