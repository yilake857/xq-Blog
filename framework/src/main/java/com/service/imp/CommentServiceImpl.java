package com.service.imp;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.constants.SystemConstants;
import com.domain.ResponseResult;
import com.domain.VO.CommentVo;
import com.domain.VO.PageVo;
import com.domain.entity.Comment;
import com.domain.entity.User;
import com.enums.AppHttpCodeEnum;
import com.exception.SystemException;
import com.mapper.CommentMapper;
import com.service.CommentService;
import com.service.UserService;
import com.utils.BeanCopyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 评论表(SgComment)表服务实现类
 *
 * @author makejava
 * @since 2023-01-06 20:35:08
 */
@Service("CommentService")
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {
    @Autowired
    private UserService userService;
    public ResponseResult commentList(String commentType, Long articleId, Integer pageNum, Integer pageSize){
        //查询对应文章的根评论
        LambdaQueryWrapper<Comment> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        //对articleId进行判断
        lambdaQueryWrapper.eq(SystemConstants.ARTICLE_COMMENT.equals(commentType),Comment::getArticleId,articleId);
        //根评论 rootId为-1
        lambdaQueryWrapper.eq(Comment::getRootId,-1);
        //评论类型
        lambdaQueryWrapper.eq(Comment::getType,commentType);
        //分页查询
        Page<Comment> page = new Page(pageNum, pageSize);
        page(page,lambdaQueryWrapper);

        List<Comment> records = page.getRecords();
        List<CommentVo> commentVos = toCommentVoList(records);
        //查询所有根评论对应的子评论集合，并且赋值给对应的属性
        if(!ObjectUtils.isEmpty(commentVos)){
            for (CommentVo commentVo :commentVos) {
                //查询对应的子评论
                List<CommentVo> children = getChildren(commentVo.getId());
                //赋值
                commentVo.setChildren(children);
            }
        }


        return ResponseResult.okResult(new PageVo(commentVos,page.getTotal()));
    }



    /**
     * 根据根评论的id查询所对应的子评论的集合
     * @param id 根评论的id
     * @return
     */
    private List<CommentVo> getChildren(Long id) {
        LambdaQueryWrapper<Comment> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Comment::getRootId,id);
        lambdaQueryWrapper.orderByAsc(Comment::getCreateTime);
        List<Comment> commentList = list(lambdaQueryWrapper);

        List<CommentVo> commentVos = toCommentVoList(commentList);
        return commentVos;
    }
    private List<CommentVo> toCommentVoList(List<Comment> list){
        List<CommentVo> commentVos = BeanCopyUtils.copyBeanList(list, CommentVo.class);
        //遍历vo集合
        for (CommentVo commentVo : commentVos) {
            //通过creatyBy查询用户的昵称并赋值
            User user = userService.getById(commentVo.getCreateBy());
            String nickName = "已注销用户";
            if(!ObjectUtils.isEmpty(user)){
                nickName = user.getNickName();
            }

            commentVo.setUsername(nickName);
            //通过toCommentUserId查询用户的昵称并赋值
            //如果toCommentUserId不为-1才进行查询
            if(commentVo.getToCommentUserId() != -1){
                String toCommentUserName = userService.getById(commentVo.getToCommentUserId()).getNickName();
                commentVo.setToCommentUserName(toCommentUserName);
            }
        }
        return commentVos;
    }



    @Override
    public ResponseResult addComment(Comment comment) {
        //评论内容不能为空
        if(!StringUtils.hasText(comment.getContent())){
            throw new SystemException(AppHttpCodeEnum.CONTENT_NOT_NULL);
        }
        save(comment);
        return ResponseResult.okResult();
    }
}
