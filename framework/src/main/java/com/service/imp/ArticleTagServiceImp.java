package com.service.imp;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.domain.entity.ArticleTag;
import com.mapper.ArticleTagMapper;
import com.service.ArticleTagService;
import org.springframework.stereotype.Service;

@Service
public class ArticleTagServiceImp extends ServiceImpl<ArticleTagMapper, ArticleTag> implements ArticleTagService {

}
