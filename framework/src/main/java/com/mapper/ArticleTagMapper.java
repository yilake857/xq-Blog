package com.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.domain.entity.ArticleTag;

import java.util.List;

public interface ArticleTagMapper extends BaseMapper<ArticleTag> {
    List<Long> getTagIdsByArtcileId(Long id);
}
