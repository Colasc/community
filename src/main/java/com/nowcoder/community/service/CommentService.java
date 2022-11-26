package com.nowcoder.community.service;

import com.nowcoder.community.dao.CommentDao;
import com.nowcoder.community.dao.DiscussPostDao;
import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.SensitiveFilter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.Resource;
import java.util.List;

@Service
public class CommentService implements CommunityConstant {

    @Resource
    CommentDao commentDao;
    @Resource
    SensitiveFilter sensitiveFilter;
    @Resource
    DiscussPostDao discussPostDao;

    public List<Comment> queryCommentsByEntity(int entityType,int entityId,int offset,int limit){
        return commentDao.selectCommentsByEntity(entityType,entityId,offset,limit);
    }

    public int queryCountByEntity(int entityType,int entityId){
        return commentDao.selectCountByEntity(entityType,entityId);
    }
    @Transactional(isolation = Isolation.READ_COMMITTED,propagation = Propagation.REQUIRED)
    public int addComment(Comment comment){
        if (comment == null){
            throw new IllegalArgumentException("参数不能为空");
        }
        //添加评论
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        comment.setContent(sensitiveFilter.filter(comment.getContent()));
        int rows = commentDao.insertComment(comment);

        //更新帖子评论数量
        if (comment.getEntityType() == ENTITY_TYPE_POST){
            int count = commentDao.selectCountByEntity(ENTITY_TYPE_POST,comment.getEntityId());
            discussPostDao.updateCommentCount(comment.getEntityId(),count);
        }

        return rows;
    }

    public Comment queryCommentById(int id){
        return commentDao.selectCommentById(id);
    }


}
