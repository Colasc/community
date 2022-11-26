package com.nowcoder.community.dao;

import com.nowcoder.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiscussPostDao {

    List<DiscussPost> selectDiscussPosts(int userId,int offset,int limit,int orderMode);

    int selectDiscussPostRows(@Param("userId") int userId);

    int insertDiscussPost(DiscussPost discussPost);

    DiscussPost selectDiscussPostById(@Param("id") int id);

    int updateCommentCount(int postId,int commentCount);

    int updateTypeById(int id,int type);

    int updateStatusById(int id,int status);

    int updateScoreById(int id,double score);
}
