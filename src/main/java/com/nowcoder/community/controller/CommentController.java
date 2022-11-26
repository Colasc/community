package com.nowcoder.community.controller;

import com.nowcoder.community.dao.CommentDao;
import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Event;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.HostHolder;
import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import java.util.Date;

@Controller
@RequestMapping("/comment")
public class CommentController implements CommunityConstant {
    @Resource
    private HostHolder hostHolder;
    @Resource
    private CommentService commentService;
    @Resource
    private DiscussPostService discussPostService;
    @Resource
    private EventProducer eventProducer;
    @Resource
    private RedisTemplate redisTemplate;

    @RequestMapping(value = "/add/{discussPostId}",method = RequestMethod.POST)
    public String addComment(Comment comment, @PathVariable("discussPostId") int postId){
        comment.setUserId(hostHolder.getUser().getId());
        comment.setCreateTime(new Date());
        comment.setStatus(0);
        //评论通知
        Event event = new Event().setTopic(TOPIC_COMMENT)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(comment.getEntityType())
                .setEntityId(comment.getEntityId())
                .setData("postId",postId);

        if (comment.getTargetId() == null){
            comment.setTargetId(0);
        }
        commentService.addComment(comment);

        if (comment.getEntityType() == ENTITY_TYPE_POST){
            DiscussPost target = discussPostService.queryDiscussPost(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        }else if (comment.getEntityType() == ENTITY_TYPE_COMMENT){
            Comment comment1 = commentService.queryCommentById(comment.getEntityId());
            event.setEntityUserId(comment1.getUserId());
        }
        eventProducer.fireEvent(event);
        //触发发帖事件
        if (comment.getEntityType() == ENTITY_TYPE_POST){
            event = new Event()
                    .setTopic(TOPIC_COMMENT)
                    .setUserId(hostHolder.getUser().getId())
                    .setEntityType(comment.getEntityType())
                    .setEntityId(comment.getEntityId());
            eventProducer.fireEvent(event);

            //计算分数
            String redisKey  = RedisKeyUtil.getScoreKey();
            redisTemplate.opsForSet().add(redisKey,postId);
        }

        return "redirect:/discuss/detail/"+postId;

    }
}
