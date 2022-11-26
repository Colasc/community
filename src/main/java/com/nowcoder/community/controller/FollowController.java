package com.nowcoder.community.controller;

import com.nowcoder.community.entity.Event;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.service.FollowService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Controller
public class FollowController implements CommunityConstant {
    @Resource
    FollowService followService;
    @Resource
    HostHolder hostHolder;
    @Resource
    UserService userService;
    @Resource
    EventProducer eventProducer;

    @RequestMapping(value = "/follow",method = RequestMethod.POST)
    @ResponseBody
    public String follow(int entityType,int entityId){
        User user = hostHolder.getUser();
        Event event = new Event().setTopic(TOPIC_FOLLOW)
                .setUserId(user.getId()).setEntityType(entityType)
                .setEntityId(entityId).setEntityUserId(entityId);
        eventProducer.fireEvent(event);

        followService.follow(user.getId(),entityType,entityId);

        return CommunityUtil.getJSONString(0,"已关注");
    }

    @RequestMapping(value = "/unfollow",method = RequestMethod.POST)
    @ResponseBody
    public String unfollow(int entityType,int entityId){
        User user =  hostHolder.getUser();

        followService.unfollow(user.getId(),entityType,entityId);
        return CommunityUtil.getJSONString(0,"已取消关注");
    }

    @RequestMapping(value = "/followees/{userId}",method = RequestMethod.GET)
    public String getFollowees(@PathVariable("userId") int userId, Page page, Model model){
        User user = userService.findUserById(userId);
        if (user == null){
            throw new IllegalArgumentException("该用户不存在！");
        }
        model.addAttribute("user",user);

        page.setLimit(5);
        page.setRow((int)followService.findFolloweeCount(userId,CommunityConstant.ENTITY_TYPE_USER));
        page.setPath("/followees/"+userId);
        List<Map<String,Object>> userList = followService.findFollowee(userId,page.getOffset(),page.getLimit());
        if (userList != null){
            for (Map<String, Object> map:userList) {
                User targetUser = (User) map.get("user");
                map.put("hasFollowed",hasFollowed(targetUser.getId()));
            }
        }
        model.addAttribute("users",userList);
        return "/site/followee";
    }

    @RequestMapping(value = "/followers/{userId}",method = RequestMethod.GET)
    public String getFollowers(@PathVariable("userId") int userId, Page page, Model model){
        User user = userService.findUserById(userId);
        if (user == null){
            throw new IllegalArgumentException("该用户不存在！");
        }
        model.addAttribute("user",user);

        page.setLimit(5);
        page.setRow((int)followService.findFollowerCount(CommunityConstant.ENTITY_TYPE_USER,userId));
        page.setPath("/followers/"+userId);
        List<Map<String,Object>> userList = followService.findFollower(userId,page.getOffset(),page.getLimit());
        if (userList != null){
            for (Map<String, Object> map:userList) {
                User targetUser = (User) map.get("user");
                map.put("hasFollowed",hasFollowed(targetUser.getId()));
            }
        }
        model.addAttribute("users",userList);
        return "/site/follower";
    }

    public boolean hasFollowed(int userId){
        if (hostHolder.getUser() ==null){
            return false;
        }
        return followService.hasFollowed(hostHolder.getUser().getId(),CommunityConstant.ENTITY_TYPE_USER,userId);
    }
}
