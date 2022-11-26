package com.nowcoder.community.service;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.nowcoder.community.dao.DiscussPostDao;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.util.SensitiveFilter;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class DiscussPostService {
    private static final Logger logger = LoggerFactory.getLogger(DiscussPostService.class);

    @Resource
    private DiscussPostDao discussPostDao;

    @Resource
    private SensitiveFilter sensitiveFilter;
    @Value("${caffeine.posts.max-size}")
    private int maxSize;
    @Value("${caffeine.posts.expire-seconds}")
    private int expireSeconds;

    //Caffeine核心接口 ：Cache ,LoadingCache ,AsyncLoadingCache
    //帖子列表缓存
    private LoadingCache<String,List<DiscussPost>> postListCache;
    //帖子总数缓存
    private LoadingCache<Integer,Integer> postRowCache;

    @PostConstruct
    public void init(){
        //初始化帖子列表缓存
        postListCache = Caffeine.newBuilder()
                        .maximumSize(maxSize)
                        .expireAfterAccess(expireSeconds, TimeUnit.SECONDS)
                        .build(new CacheLoader<String, List<DiscussPost>>() {
                            @Nullable
                            @Override
                            public List<DiscussPost> load(String key) throws Exception {
                                if (key == null || key.length() == 0){
                                    throw new IllegalArgumentException("参数错误！");
                                }

                                String[] params = key.split(":");
                                if (params == null || params.length != 2){
                                    throw new IllegalArgumentException("参数错误！");
                                }
                                int offset = Integer.valueOf(params[0]);
                                int limit = Integer.valueOf(params[1]);
                                //二级缓存：Redis --> mysql

                                logger.debug("load post list from DB");
                                return discussPostDao.selectDiscussPosts(0,offset,limit,1);
                            }
                        });
        //初始化帖子总数缓存
        postRowCache = Caffeine.newBuilder()
                    .maximumSize(maxSize)
                    .expireAfterAccess(expireSeconds,TimeUnit.SECONDS)
                    .build(new CacheLoader<Integer, Integer>() {
                        @Nullable
                        @Override
                        public Integer load(Integer integer) throws Exception {
                            logger.debug("load post rows form DB");
                            return discussPostDao.selectDiscussPostRows(integer);
                        }
                    });
    }



    public List<DiscussPost> queryDiscussPosts(int userId,int offset, int limit,int orderMode){
        if (userId == 0 && orderMode == 1){
            return postListCache.get(offset+":"+limit);
        }
        logger.debug("load post list from DB");
        List<DiscussPost> posts = discussPostDao.selectDiscussPosts(userId,offset,limit,orderMode);
        return posts;
    }

    public int queryDiscussPortRows(int userId){
        if (userId == 0){
            return postRowCache.get(userId);
        }
        logger.debug("load post rows from DB");
        int rows = discussPostDao.selectDiscussPostRows(userId);
        return rows;
    }

    public int addDiscussPost(DiscussPost discussPost){
        if (discussPost == null){
            throw new IllegalArgumentException("参数不能为空!");
        }
        //转义HTML标记
        discussPost.setTitle(HtmlUtils.htmlEscape(discussPost.getTitle()));
        discussPost.setContent(HtmlUtils.htmlEscape(discussPost.getContent()));
        //过滤敏感词
        discussPost.setTitle(sensitiveFilter.filter(discussPost.getTitle()));
        discussPost.setContent(sensitiveFilter.filter(discussPost.getContent()));
        return discussPostDao.insertDiscussPost(discussPost);
    }

    public DiscussPost queryDiscussPost(int id){
        return discussPostDao.selectDiscussPostById(id);
    }

    public int updateCommentCount(int id,int commentCount){
        return discussPostDao.updateCommentCount(id,commentCount);
    }

    public int updateType(int id,int type){
        return discussPostDao.updateTypeById(id, type);
    }

    public int updateStatus(int id,int status){
        return discussPostDao.updateStatusById(id, status);
    }

    public int updateScore(int id ,double score){
        return discussPostDao.updateScoreById(id, score);
    }
}
