package com.nowcoder.community.controller;

import com.nowcoder.community.entity.*;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {

    @Resource
    private DiscussPostService discussPostService;

    @Resource
    private CommentService commentService;

    @Resource
    private UserService userService;

    @Resource
    HostHolder hostHolder;

    @Resource
    private EventProducer eventProducer;

    @Resource
    private LikeService likeService;
    @Resource
    private RedisTemplate redisTemplate;

    @RequestMapping(value = "/add",method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPosts(String title,String content){
        User user = hostHolder.getUser();
        if (user == null){
            return CommunityUtil.getJSONString(403,"您还未登录",null);
        }
        DiscussPost discussPost = new DiscussPost();
        discussPost.setUserId(user.getId());
        discussPost.setTitle(title);
        discussPost.setContent(content);
        discussPost.setType(0);
        discussPost.setStatus(0);
        discussPost.setCommentCount(0);
        discussPost.setCreateTime(new Date());

        discussPostService.addDiscussPost(discussPost);
        //触发发帖事件
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(user.getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(discussPost.getId())
                .setEntityUserId(discussPost.getUserId());
        eventProducer.fireEvent(event);

        //计算帖子分数
        String redisKey = RedisKeyUtil.getScoreKey();
        redisTemplate.opsForSet().add(redisKey,discussPost.getId());

        return CommunityUtil.getJSONString(0,"发布成功!");

    }

    @RequestMapping(path = "/detail/{discussPostId}",method = RequestMethod.GET)
    public String queryDiscussPost(@PathVariable("discussPostId") int id, Model model , Page page){
        //帖子
        DiscussPost discussPost = discussPostService.queryDiscussPost(id);
        model.addAttribute("post",discussPost);
        //帖子点赞数量
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST,discussPost.getId());
        //帖子点赞状态
        int likeStatus = hostHolder.getUser() == null ? 0 :likeService.findEntityLikeStatus(hostHolder.getUser().getId(),ENTITY_TYPE_POST,discussPost.getId());
        model.addAttribute("likeCount",likeCount);
        model.addAttribute("likeStatus",likeStatus);
        //对应用户
        User user = userService.findUserById(discussPost.getUserId());
        model.addAttribute("user",user);
        page.setRow(discussPost.getCommentCount());
        page.setLimit(5);
        page.setPath("/discuss/detail/"+id);

        List<Comment> commentList = commentService.queryCommentsByEntity(ENTITY_TYPE_POST,discussPost.getId(),page.getOffset(),page.getLimit());
        List<Map<String,Object>> commentVoList = new ArrayList<>();
        if (commentList != null){
            for (Comment comment:commentList) {
                Map<String,Object> commentVo = new HashMap<>();
                //评论列表
                commentVo.put("comment",comment);
                commentVo.put("user",userService.findUserById(comment.getUserId()));
                //评论的点赞数量
                long commentLikeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT,comment.getId());
                //评论点赞状态
                int commentLikeStatus = hostHolder.getUser() == null ? 0 : likeService.findEntityLikeStatus(hostHolder.getUser().getId(),ENTITY_TYPE_COMMENT,comment.getId());
                commentVo.put("commentLikeCount",commentLikeCount);
                commentVo.put("commentLikeStatus",commentLikeStatus);

                //回复列表
                List<Comment> replyComment = commentService.queryCommentsByEntity(ENTITY_TYPE_COMMENT,comment.getId(),0,Integer.MAX_VALUE);
                List<Map<String,Object>> replyVoList = new ArrayList<>();
                if (replyComment != null){
                    for (Comment recomment:replyComment) {
                        Map<String,Object> replyVo = new HashMap<>();
                        replyVo.put("replyComment",recomment);
                        replyVo.put("user",userService.findUserById(recomment.getUserId()));
                        User target = recomment.getTargetId() == 0? null:userService.findUserById(recomment.getTargetId());
                        replyVo.put("target",target);
                        //回复的点赞数量
                        long replyLikeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT,recomment.getId());
                        //回复的点赞状态
                        int replyLikeStatus = hostHolder.getUser() == null ? 0 : likeService.findEntityLikeStatus(hostHolder.getUser().getId(),ENTITY_TYPE_COMMENT,recomment.getId());
                        replyVo.put("replyLikeCount",replyLikeCount);
                        replyVo.put("replyLikeStatus",replyLikeStatus);
                        replyVoList.add(replyVo);
                    }
                }
                commentVo.put("replys",replyVoList);

                //回复数量
                int replyCount = commentService.queryCountByEntity(ENTITY_TYPE_COMMENT,comment.getId());
                commentVo.put("replyCount",replyCount);
                commentVoList.add(commentVo);
            }
        }
        model.addAttribute("comments",commentVoList);


        return "/site/discuss-detail";
    }
    //置顶
    @RequestMapping(value = "/top" ,method = RequestMethod.POST)
    @ResponseBody
    public String setTop(int id){
        discussPostService.updateType(id,1);
        //触发发帖事件
        Event event = new Event()
                .setEntityId(id)
                .setTopic(TOPIC_PUBLISH)
                .setEntityType(ENTITY_TYPE_POST)
                .setUserId(hostHolder.getUser().getId());
        eventProducer.fireEvent(event);
        return CommunityUtil.getJSONString(0);
    }

    //精华
    @RequestMapping(value = "/wonderful" ,method = RequestMethod.POST)
    @ResponseBody
    public String setWonderful(int id){
        discussPostService.updateStatus(id,1);
        //触发发帖事件
        Event event = new Event()
                .setEntityId(id)
                .setTopic(TOPIC_PUBLISH)
                .setEntityType(ENTITY_TYPE_POST)
                .setUserId(hostHolder.getUser().getId());
        eventProducer.fireEvent(event);
        //计算分数
        String redisKey = RedisKeyUtil.getScoreKey();
        redisTemplate.opsForSet().add(redisKey,id);

        return CommunityUtil.getJSONString(0);
    }

    //删除
    @RequestMapping(value = "/delete" ,method = RequestMethod.POST)
    @ResponseBody
    public String setDelete(int id){
        discussPostService.updateStatus(id,2);
        //触发发帖事件
        Event event = new Event()
                .setEntityId(id)
                .setTopic(TOPIC_DELETE)
                .setEntityType(ENTITY_TYPE_POST)
                .setUserId(hostHolder.getUser().getId());
        eventProducer.fireEvent(event);
        return CommunityUtil.getJSONString(0);
    }
}
