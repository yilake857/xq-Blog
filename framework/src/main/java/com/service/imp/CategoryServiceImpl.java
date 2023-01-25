package com.service.imp;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.constants.SystemConstants;
import com.domain.ResponseResult;
import com.domain.VO.CategoryVo;
import com.domain.VO.PageVo;
import com.domain.dto.CategoryDto;
import com.domain.entity.Article;
import com.domain.entity.Category;
import com.mapper.CategoryMapper;
import com.service.ArticleService;
import com.service.CategoryService;
import com.utils.BeanCopyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 分类表(Category)表服务实现类
 *
 * @author makejava
 * @since 2022-10-31 17:39:08
 */
@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    @Autowired
    private ArticleService articleService;

    //分页查询分类列表
    @Override
    public ResponseResult<PageVo> pageCategoryList(Integer pageNum, Integer pageSize, CategoryDto categoryDto) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(categoryDto.getName()),Category::getName,categoryDto.getName());
        wrapper.like(StringUtils.hasText(categoryDto.getStatus()),Category::getStatus,categoryDto.getStatus());
        //分页查询
        Page<Category> page = new Page<>(pageNum,pageSize);
        page(page,wrapper);
        //封装成pageVO
        List<CategoryVo> categoryVos = BeanCopyUtils.copyBeanList(page.getRecords(), CategoryVo.class);
        PageVo pageVo = new PageVo(categoryVos, page.getTotal());
        return ResponseResult.okResult(pageVo);
    }

    @Override
    public ResponseResult getCategoryList() {
//        //查询文章表  状态为已发布的文章
//        LambdaQueryWrapper<Article> articleWrapper = new LambdaQueryWrapper<>();
//        articleWrapper.eq(Article::getStatus,SystemConstants.ARTICLE_STATUS_NORMAL);
//        List<Article> articleList = articleService.list(articleWrapper);
//        //获取文章的分类id，并且去重
//        Set<Long> categoryIds = articleList.stream()
//                .map(article -> article.getCategoryId())
//                .collect(Collectors.toSet());
//
//        //查询分类表
//        List<Category> categories = listByIds(categoryIds);
//        categories = categories.stream().
//                filter(category -> SystemConstants.STATUS_NORMAL.equals(category.getStatus()))
//                .collect(Collectors.toList());
//        //封装vo
//        List<CategoryVo> categoryVos = BeanCopyUtils.copyBeanList(categories, CategoryVo.class);
//
//        return ResponseResult.okResult(categoryVos);
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Category::getStatus,SystemConstants.STATUS_NORMAL);
        List<Category> categories = list(queryWrapper);
        List<CategoryVo> categoryVos = BeanCopyUtils.copyBeanList(categories, CategoryVo.class);
        return ResponseResult.okResult(categoryVos);
    }

    @Override
    public ResponseResult listAllCategory() {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getStatus,SystemConstants.NORMAL);
        List<Category> categories = list(wrapper);
        List<CategoryVo> categoryVos = BeanCopyUtils.copyBeanList(categories, CategoryVo.class);
        return ResponseResult.okResult(categoryVos);
    }

    //新增分类功能
    @Override
    public ResponseResult addCategory(CategoryVo categoryVo) {
        Category category = BeanCopyUtils.copyBean(categoryVo, Category.class);
        save(category);
        return ResponseResult.okResult();
    }

    //根据id查询分类
    @Override
    public ResponseResult selectCategoryById(Long id) {
        Category category = getBaseMapper().selectById(id);
        CategoryVo categoryVo = BeanCopyUtils.copyBean(category, CategoryVo.class);
        return ResponseResult.okResult(categoryVo);
    }

    @Override
    public ResponseResult updateCategory(Category category) {
        updateById(category);
        return ResponseResult.okResult();
    }

    @Override
    public ResponseResult deleteCategoryById(Long id) {
        getBaseMapper().deleteById(id);
        return ResponseResult.okResult();
    }
}
