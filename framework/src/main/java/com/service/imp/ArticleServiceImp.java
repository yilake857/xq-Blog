package com.service.imp;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.constants.SystemConstants;
import com.domain.ResponseResult;
import com.domain.VO.ArticleDetailVO;
import com.domain.VO.ArticleListVo;
import com.domain.VO.HotArticleVo;
import com.domain.VO.PageVo;
import com.domain.dto.AddArticleDto;
import com.domain.dto.ArticleDetailDto;
import com.domain.entity.Article;
import com.domain.entity.ArticleTag;
import com.domain.entity.Category;
import com.mapper.ArticleMapper;
import com.mapper.ArticleTagMapper;
import com.service.ArticleService;
import com.service.ArticleTagService;
import com.service.CategoryService;
import com.utils.BeanCopyUtils;
import com.utils.RedisCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ArticleServiceImp  extends ServiceImpl<ArticleMapper, Article> implements ArticleService  {
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private RedisCache redisCache;
    @Autowired
    private ArticleTagService articleTagService;
    @Autowired
    private ArticleTagMapper articleTagMapper;
    @Autowired
    private ArticleService articleService;
    @Override
    public ResponseResult hotArticleList() {
        //查询热门文章 封装成ResponseResult返回
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        //必须是正式文章
        queryWrapper.eq(Article::getStatus, SystemConstants.ARTICLE_STATUS_NORMAL);
        //按照浏览量进行排序
        queryWrapper.orderByDesc(Article::getViewCount);
        //最多只查询10条
        Page<Article> page = new Page(1,10);
        page(page,queryWrapper);

        List<Article> articles = page.getRecords();
        List<HotArticleVo> hotArticleVos = new ArrayList<>();
//        //bean拷贝
//        for(Article article:articles){
//            HotArticleVo vo = new HotArticleVo();
//            BeanUtils.copyProperties(article,vo);
//            hotArticleVos.add(vo);
//        }
        hotArticleVos = BeanCopyUtils.copyBeanList(articles, HotArticleVo.class);
        //从redis更新viewCount
        for (HotArticleVo hotArticleVo :hotArticleVos) {
            Long id = hotArticleVo.getId();
            //从redis中获取viewCount
            Integer viewCount = redisCache.getCacheMapValue("article:viewCount", String.valueOf(id));
            hotArticleVo.setViewCount(viewCount.longValue());
        }


        return ResponseResult.okResult(hotArticleVos);
    }

    @Override
    public ResponseResult articleList(Integer pageNum, Integer pageSize, Long categoryId) {
        //查询条件
        LambdaQueryWrapper<Article> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        // 如果 有categoryId 就要 查询时要和传入的相同
        lambdaQueryWrapper.eq(Objects.nonNull(categoryId)&&categoryId>0 ,Article::getCategoryId,categoryId);
        // 状态是正式发布的
        lambdaQueryWrapper.eq(Article::getStatus,SystemConstants.ARTICLE_STATUS_NORMAL);
        // 对isTop进行降序
        lambdaQueryWrapper.orderByDesc(Article::getIsTop);

        //分页查询
        Page<Article> page = new Page<>(pageNum,pageSize);
        page(page,lambdaQueryWrapper);

        List<Article> articles = page.getRecords();
        //查询categoryName
        //articleId去查询articleName进行设置
//        for (Article article : articles) {
//            Category category = categoryService.getById(article.getCategoryId());
//            article.setCategoryName(category.getName());
//        }

        //封装查询结果
        List<ArticleListVo> articleListVos = BeanCopyUtils.copyBeanList(page.getRecords(), ArticleListVo.class);
        //从redis更新viewCount
        for (ArticleListVo articleListVo :articleListVos) {
            Long id = articleListVo.getId();
            //从redis中获取viewCount
            Integer viewCount = redisCache.getCacheMapValue("article:viewCount", String.valueOf(id));
            articleListVo.setViewCount(viewCount.longValue());
        }
        PageVo pageVo = new PageVo(articleListVos,page.getTotal());
        return ResponseResult.okResult(pageVo);
    }

    @Override
    public ResponseResult getArticleDetail(Long id) {
        //根据id查询文章
        Article article = getById(id);
        //从redis中获取viewCount
        Integer viewCount = redisCache.getCacheMapValue("article:viewCount", String.valueOf(id));
        article.setViewCount(viewCount.longValue());
        //转化为VO
        ArticleDetailVO articleDetailVO = BeanCopyUtils.copyBean(article, ArticleDetailVO.class);
        //根据id查询分类名
        Long categoryId = articleDetailVO.getCategoryId();
        Category category = categoryService.getById(categoryId);
        if(category != null){
            articleDetailVO.setCategoryName(category.getName());
        }
        //封装响应
        return ResponseResult.okResult(articleDetailVO);
    }

    @Override
    public ResponseResult updateViewCount(Long id) {
        //更新redis中对应 id的浏览量
        redisCache.incrementCacheMapValue("article:viewCount",id.toString(),1);
        return ResponseResult.okResult();
    }

    @Override
    public ResponseResult addArticle(AddArticleDto articleDto) {
        Article article = BeanCopyUtils.copyBean(articleDto, Article.class);
        save(article);
        List<ArticleTag> articleTags = articleDto.getTags().stream()
                .map(tagId -> new ArticleTag(article.getId(), tagId))
                .collect(Collectors.toList());
        //添加 博客和标签的关联
        articleTagService.saveBatch(articleTags);
        return ResponseResult.okResult();
    }

    @Override
    public ResponseResult listArticle(Integer pageNum, Integer pageSize, ArticleListVo articleListVo) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(articleListVo.getTitle()),Article::getTitle,articleListVo.getTitle());
        wrapper.like(StringUtils.hasText(articleListVo.getSummary()),Article::getSummary,articleListVo.getSummary());
        Page<Article> page = new Page<>(pageNum, pageSize);
        page(page,wrapper);

        return ResponseResult.okResult(new PageVo(page.getRecords(),page.getTotal()));

    }

    @Override
    public ResponseResult getArticleById(Long id) {
        Article article = getBaseMapper().selectById(id);
        List<Long> tagIdsByArtcileId = articleTagMapper.getTagIdsByArtcileId(id);
        ArticleDetailDto articleDetailDto = BeanCopyUtils.copyBean(article, ArticleDetailDto.class);
        articleDetailDto.setTags(tagIdsByArtcileId);
        return ResponseResult.okResult(articleDetailDto);
    }

    @Override
    public ResponseResult updateArticle(ArticleDetailDto articleDetailDto) {

        //更新Article表中的数据
        Article article = BeanCopyUtils.copyBean(articleDetailDto, Article.class);
        updateById(article);

        //更新`article_tag`表中的数据
        //新的tag生成数组，旧的tag读出数组，两个数组做比较，然后做删除、添加
        //旧数组
        LambdaQueryWrapper<ArticleTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ArticleTag::getArticleId,articleDetailDto.getId());
        List<ArticleTag> oldArticleTags = articleTagService.getBaseMapper().selectList(wrapper);
        //新数组
        List<ArticleTag> newArticleTags = articleDetailDto.getTags().stream()
                .map(tagId -> new ArticleTag(articleDetailDto.getId(), tagId))
                .collect(Collectors.toList());
        //相同元素数组
        ArrayList<ArticleTag> sameArticleTags = new ArrayList<>();
        for (ArticleTag articleTag : oldArticleTags) {
            if(newArticleTags.contains(articleTag)){
                sameArticleTags.add(articleTag);
            }
        }
        //去重
        oldArticleTags.addAll(sameArticleTags);
        oldArticleTags = oldArticleTags.stream()
                .distinct().collect(Collectors.toList());

        newArticleTags.addAll(sameArticleTags);
        newArticleTags = newArticleTags.stream()
                .distinct().collect(Collectors.toList());

        if(!oldArticleTags.isEmpty()){
            for (ArticleTag articleTag : oldArticleTags) {
                LambdaQueryWrapper<ArticleTag> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(ArticleTag::getArticleId,articleTag.getArticleId());
                queryWrapper.eq(ArticleTag::getTagId,articleTag.getTagId());
                articleTagService.getBaseMapper().delete(queryWrapper);
            }
        }


        if(!newArticleTags.isEmpty()){
            articleTagService.saveBatch(newArticleTags);
        }



        return ResponseResult.okResult();
    }

    @Override
    public ResponseResult deleteArticle(Long id) {
        //删除文章
        getBaseMapper().deleteById(id);
        //删除ArticleTag关联关系
        LambdaQueryWrapper<ArticleTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ArticleTag::getArticleId,id);
        articleTagService.getBaseMapper().delete(wrapper);

        return ResponseResult.okResult();

    }


}
