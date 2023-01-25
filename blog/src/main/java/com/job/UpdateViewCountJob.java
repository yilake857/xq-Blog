package com.job;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.domain.entity.Article;
import com.service.ArticleService;
import com.utils.RedisCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class UpdateViewCountJob {

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private ArticleService articleService;

    @Scheduled(cron = "0/5 * * * * ?")
    public void updateViewCount(){
        //获取redis中的浏览量
        Map<String, Integer> viewCountMap = redisCache.getCacheMap("article:viewCount");

        List<Article> articles = viewCountMap.entrySet()
                .stream()
                .map(entry -> new Article(Long.valueOf(entry.getKey()), entry.getValue().longValue()))
                .collect(Collectors.toList());
        //更新到数据库中
        /**
         * 此处直接使用mybatisplus的updateBatchById会造成
         * com.handler.mybatisplus.MyMetaObjectHandler的updateFill方法
         * 中的SecurityUtils.getUserId()为空 因为定时任务不需要token
         *  所以造成userId为空
         */


//        articleService.updateBatchById(articles);
        for (Article article : articles) {
            LambdaUpdateWrapper<Article> wrapper = Wrappers.lambdaUpdate(Article.class).eq(Article::getId, article.getId()).set(Article::getViewCount, article.getViewCount());
            articleService.update(wrapper);
        }

    }
}