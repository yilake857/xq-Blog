package com.controller;

import com.domain.ResponseResult;
import com.domain.VO.ArticleListVo;
import com.domain.dto.AddArticleDto;
import com.domain.dto.ArticleDetailDto;
import com.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/content/article")
public class ArticleController {
    @Autowired
    private ArticleService articleService;
    @PostMapping
    public ResponseResult addArticle(@RequestBody AddArticleDto article){
        return articleService.addArticle(article);
    }

    @GetMapping("/list")
    public ResponseResult listArticle(Integer pageNum, Integer pageSize, ArticleListVo articleListVo){
        return articleService.listArticle(pageNum,pageSize,articleListVo);
    }

    @GetMapping("/{id}")
    public ResponseResult getArticleById(@PathVariable Long id){
        return articleService.getArticleById(id);
    }

    @PutMapping
    public ResponseResult updateArticle(@RequestBody ArticleDetailDto articleDetailDto){
        return articleService.updateArticle(articleDetailDto);

    }

    @DeleteMapping("/{id}")
    public ResponseResult deleteArticle(@PathVariable Long id){
        return articleService.deleteArticle(id);
    }

}
