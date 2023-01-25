package com.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.domain.ResponseResult;
import com.domain.VO.CategoryVo;
import com.domain.VO.PageVo;
import com.domain.dto.CategoryDto;
import com.domain.entity.Category;


/**
 * 分类表(Category)表服务接口
 *
 * @author makejava
 * @since 2022-10-31 17:39:07
 */
public interface CategoryService extends IService<Category> {
    ResponseResult<PageVo> pageCategoryList(Integer pageNum, Integer pageSize, CategoryDto categoryDto);

    ResponseResult getCategoryList();

    ResponseResult listAllCategory();

    ResponseResult addCategory(CategoryVo categoryVo);

    ResponseResult selectCategoryById(Long id);

    ResponseResult updateCategory(Category category);

    ResponseResult deleteCategoryById(Long id);
}
