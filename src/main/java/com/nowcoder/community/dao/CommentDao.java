package com.nowcoder.community.dao;

import com.nowcoder.community.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommentDao {

    int insertComment(Comment comment);

    List<Comment> selectCommentsByEntity(int entityType, int entityId, int offset , int limit);

    int selectCountByEntity(int entityType,int entityId);

    Comment selectCommentById(int id);
}
