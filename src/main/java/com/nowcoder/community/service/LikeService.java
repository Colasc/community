package com.nowcoder.community.service;

import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class LikeService {
    @Resource
    private RedisTemplate redisTemplate;
    //点赞
    public void like(int userId,int entityType,int entityId,int entityUserId){
        /*String redisKey = RedisKeyUtil.getEntityLikeKey(entityType,entityId);
        boolean ismemeber = redisTemplate.opsForSet().isMember(redisKey,userId);
        if (ismemeber){
            redisTemplate.opsForSet().remove(redisKey,userId);
        }else {
            redisTemplate.opsForSet().add(redisKey,userId);
        }*/
        //重构点赞，记录用户的被点赞数量
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType,entityId);
                String userLikeKey = RedisKeyUtil.getUserLikeKey(entityUserId);
                boolean ismember = operations.opsForSet().isMember(entityLikeKey,userId);
                //开启事务
                redisTemplate.multi();
                if (ismember){
                    operations.opsForSet().remove(entityLikeKey,userId);
                    operations.opsForValue().decrement(userLikeKey);
                }else {
                    operations.opsForSet().add(entityLikeKey,userId);
                    operations.opsForValue().increment(userLikeKey);
                }
                return operations.exec();
            }
        });
    }

    //查找点赞的数量
    public long findEntityLikeCount(int entityType,int entityId){
        String redisKey = RedisKeyUtil.getEntityLikeKey(entityType,entityId);
        return  redisTemplate.opsForSet().size(redisKey);
    }

    //判断是否已赞,查看点赞状态
    public int findEntityLikeStatus(int userId,int entityType,int entityId){
        String redisKey = RedisKeyUtil.getEntityLikeKey(entityType,entityId);
        return redisTemplate.opsForSet().isMember(redisKey,userId) ? 1 : 0;
    }

    //查询某个用户获得的点赞数
    public int findUserLikeCount(int userId){
        String userLikeKey = RedisKeyUtil.getUserLikeKey(userId);
        Integer likeCount = (Integer) redisTemplate.opsForValue().get(userLikeKey);
        return likeCount == null ? 0 : likeCount;
    }
}
