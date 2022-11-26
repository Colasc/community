package com.nowcoder.community.quartz;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.ElasticsearchService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.RedisKeyUtil;
import org.apache.http.nio.pool.NIOConnFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PostScoreRefreshJob implements Job, CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(PostScoreRefreshJob.class);

    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private DiscussPostService discussPostService;
    @Resource
    private ElasticsearchService elasticsearchService;
    @Resource
    private LikeService likeService;
    //牛客纪元
    private static final Date epoch;
    static {
        try {
            epoch = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-08-01 00:00:00");
        } catch (ParseException e) {
            throw new IllegalArgumentException("初始化牛客纪元失败",e);
        }
    }

    //设置任务，计算分数
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        String redisKey = RedisKeyUtil.getScoreKey();
        BoundSetOperations operations = redisTemplate.boundSetOps(redisKey);

        if (operations == null){
            logger.info("[任务取消] 没有需要刷新的帖子！");
            return;
        }
        logger.info("[任务开始] 正在刷新帖子分数："+operations.size());
        while (operations.size()>0){
            this.refresh((Integer)operations.pop());
        }
        logger.info("[任务结束] 帖子分数刷新完毕！");
    }

    private void refresh(int postId){
        DiscussPost post = discussPostService.queryDiscussPost(postId);

        if (post == null){
            logger.error("该帖子不存在：id="+postId);
            return;
        }

        //是否精华
        boolean wonderful = post.getType() == 1;
        //评论数量
        int commentCount = post.getCommentCount();
        //点赞数量
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST,postId);

        //计算权重
        double w = (wonderful ? 75 : 0) + commentCount * 10 + likeCount * 2;
        //分数 = 帖子权重 + 距离天数
        double score = Math.log(Math.max(w,1)) + (post.getCreateTime().getTime() - epoch.getTime())/ (1000 * 3600 *24);
        //更新数据库
        discussPostService.updateScore(postId,score);
        post.setScore(score);
        //更新elasticsearch
        elasticsearchService.saveDiscussPost(post);
    }
}
