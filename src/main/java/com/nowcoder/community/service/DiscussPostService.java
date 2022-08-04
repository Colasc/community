package com.nowcoder.community.service;

import com.nowcoder.community.dao.DiscussPostDao;
import com.nowcoder.community.entity.DiscussPost;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class DiscussPostService {
    @Resource
    private DiscussPostDao discussPostDao;

    public List<DiscussPost> queryDiscussPorts(int userId,int offset, int limit){
        List<DiscussPost> posts = discussPostDao.selectDiscussPosts(userId,offset,limit);
        return posts;
    }

    public int queryDiscussPortRows(int userId){
        int rows = discussPostDao.selectDiscussPostRows(userId);
        return rows;
    }
}
