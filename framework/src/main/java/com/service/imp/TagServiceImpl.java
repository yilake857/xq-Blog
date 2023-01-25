package com.service.imp;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.constants.SystemConstants;
import com.domain.ResponseResult;
import com.domain.VO.PageVo;
import com.domain.VO.TagVo;
import com.domain.dto.TagListDto;
import com.domain.entity.ArticleTag;
import com.domain.entity.Tag;
import com.mapper.TagMapper;
import com.service.ArticleTagService;
import com.utils.BeanCopyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.service.TagService;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 标签(Tag)表服务实现类
 *
 * @author makejava
 * @since 2023-01-09 20:33:14
 */
@Service("tagService")
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {
    @Autowired
    private ArticleTagService articleTagService;
    @Override
    public ResponseResult<PageVo> pageTagList(Integer pageNum, Integer pageSize, TagListDto tagListDto) {
        //分页查询
        LambdaQueryWrapper<Tag> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.hasText(tagListDto.getName()),Tag::getName,tagListDto.getName());
        queryWrapper.like(StringUtils.hasText(tagListDto.getRemark()),Tag::getRemark,tagListDto.getRemark());
        queryWrapper.eq(Tag::getDelFlag,0);
        Page<Tag> page = new Page<>();
        page.setCurrent(pageNum);
        page.setSize(pageSize);
        page(page, queryWrapper);
        //封装数据返回
        List<Tag> tags = page.getRecords();
        List<TagVo> tagVos = BeanCopyUtils.copyBeanList(tags, TagVo.class);
        PageVo pageVo = new PageVo(tagVos,page.getTotal());
        return ResponseResult.okResult(pageVo);

    }

    @Override
    public ResponseResult addTag(TagListDto tagListDto) {
        Tag tag = new Tag();
        tag.setDelFlag(0);
        tag.setName(tagListDto.getName());
        tag.setRemark(tagListDto.getRemark());
        getBaseMapper().insert(tag);
        return ResponseResult.okResult();
    }

    @Override
    public ResponseResult deleteTagById(long id) {
        //注意 有个article_tag 将其中的关联关系删除
        getBaseMapper().deleteById(id);
        LambdaQueryWrapper<ArticleTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ArticleTag::getTagId,id);
        articleTagService.getBaseMapper().delete(wrapper);


        return ResponseResult.okResult();
    }

    @Override
    public ResponseResult getTagById(long id) {
        Tag tag = getById(id);
        TagVo tagVo = BeanCopyUtils.copyBean(tag, TagVo.class);
        return ResponseResult.okResult(tagVo);
    }

    @Override
    public ResponseResult updateTag(Tag tag) {
        updateById(tag);
        return ResponseResult.okResult();
    }

    @Override
    public ResponseResult listAllTag() {
        LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Tag::getDelFlag, SystemConstants.NORMAL);
        List<Tag> tags = list(wrapper);
        List<TagVo> tagVos = BeanCopyUtils.copyBeanList(tags, TagVo.class);

        return ResponseResult.okResult(tagVos);
    }
}
