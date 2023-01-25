package com.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.domain.ResponseResult;
import com.domain.VO.ArticleListVo;
import com.domain.dto.AddArticleDto;
import com.domain.dto.ArticleDetailDto;
import com.domain.entity.Article;

public interface ArticleService extends IService<Article> {
    ResponseResult hotArticleList();

    ResponseResult articleList(Integer pageNum, Integer pageSize, Long categoryId);

    ResponseResult getArticleDetail(Long id);

    ResponseResult updateViewCount(Long id);

    ResponseResult addArticle(AddArticleDto article);

    ResponseResult listArticle(Integer pageNum, Integer pageSize, ArticleListVo articleListVo);

    ResponseResult getArticleById(Long id);

    ResponseResult updateArticle(ArticleDetailDto articleDetailDto);

    ResponseResult deleteArticle(Long id);
}
