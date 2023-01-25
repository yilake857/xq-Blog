package com.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson.JSON;
import com.domain.ResponseResult;
import com.domain.VO.CategoryVo;
import com.domain.VO.ExcelCategoryVo;
import com.domain.VO.PageVo;
import com.domain.dto.CategoryDto;
import com.domain.entity.Category;
import com.enums.AppHttpCodeEnum;
import com.service.CategoryService;
import com.utils.BeanCopyUtils;
import com.utils.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/content/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;
    @GetMapping("/listAllCategory")
    public ResponseResult listAllCategory(){
        return categoryService.listAllCategory();
    }

    @GetMapping("/list")
    public ResponseResult<PageVo> pageCategoryList(Integer pageNum, Integer pageSize, CategoryDto categoryDto){
        return categoryService.pageCategoryList(pageNum,pageSize,categoryDto);
    }

    @PostMapping
    public ResponseResult addCategory(@RequestBody CategoryVo categoryVo){
        return categoryService.addCategory(categoryVo);
    }
    @GetMapping("/{id}")
    public ResponseResult selectCategoryById(@PathVariable Long id){
        return categoryService.selectCategoryById(id);
    }
    @PutMapping
    public ResponseResult updateCategory(@RequestBody Category category){
        return categoryService.updateCategory(category);
    }

    @DeleteMapping("/{id}")
    public ResponseResult deleteCategoryById(@PathVariable Long id){
        return categoryService.deleteCategoryById(id);
    }

    @GetMapping("/export")
    public void export(HttpServletResponse response){
        try {
            //设置下载文件的请求头
            WebUtils.setDownLoadHeader("分类.xlsx",response);
            //获取需要导出的数据
            List<Category> categoryVos = categoryService.list();

            List<ExcelCategoryVo> excelCategoryVos = BeanCopyUtils.copyBeanList(categoryVos, ExcelCategoryVo.class);
            //把数据写入到Excel中
            EasyExcel.write(response.getOutputStream(), ExcelCategoryVo.class).autoCloseStream(Boolean.FALSE).sheet("分类导出")
                    .doWrite(excelCategoryVos);

        } catch (Exception e) {
            //如果出现异常也要响应json
            ResponseResult result = ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR);
            WebUtils.renderString(response, JSON.toJSONString(result));
        }
    }
}
