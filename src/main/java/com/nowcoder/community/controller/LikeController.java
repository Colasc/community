package com.nowcoder.community.controller;

import com.nowcoder.community.entity.Event;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.EscapedErrors;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Controller
public class LikeController implements CommunityConstant {

    @Resource
    private LikeService likeService;

    @Resource
    private HostHolder hostHolder;

    @Resource
    private EventProducer eventProducer;

    @Resource
    private RedisTemplate redisTemplate;

    @RequestMapping(value = "/like",method = RequestMethod.POST)
    @ResponseBody
    public String like(int entityType, int entityId,int entityUserId , int postId){
        User user = hostHolder.getUser();

        //点赞
        likeService.like(user.getId(),entityType,entityId,entityUserId);
        //查询点赞数量
        long likeCount = likeService.findEntityLikeCount(entityType, entityId);
        //查询点赞状态
        int likeStatus = likeService.findEntityLikeStatus(user.getId(),entityType,entityId);

        Map<String,Object> map = new HashMap<>();
        map.put("likeCount",likeCount);
        map.put("likeStatus",likeStatus);

        if (likeStatus == 1){
            Event event = new Event().setTopic(TOPIC_LIKE)
                    .setEntityId(entityId).setEntityType(entityType)
                    .setEntityUserId(entityUserId).setUserId(user.getId()).setData("postId",postId);
            eventProducer.fireEvent(event);
        }
        if (entityType == ENTITY_TYPE_POST){
            //计算分数
            String redisKey = RedisKeyUtil.getScoreKey();
            redisTemplate.opsForSet().add(redisKey,postId);
        }

        return CommunityUtil.getJSONString(0,null,map);
    }
}
