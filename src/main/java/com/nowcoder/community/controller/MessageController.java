package com.nowcoder.community.controller;

import com.alibaba.fastjson.JSONObject;
import com.nowcoder.community.entity.Message;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.MessageService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.apache.ibatis.ognl.ObjectElementsAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.Resource;
import java.util.*;

@Controller
public class MessageController implements CommunityConstant {

    @Resource
    HostHolder hostHolder;
    @Resource
    MessageService messageService;
    @Resource
    UserService userService;
    @RequestMapping(value = "/letter/list",method = RequestMethod.GET)
    public String getLetterList(Model model, Page page){
        //分页信息
        User user = hostHolder.getUser();
        page.setPath("/letter/list");
        page.setLimit(5);
        page.setRow(messageService.queryMessageCount(user.getId()));
        //私信列表
        List<Message> messageList = messageService.queryMessageList(user.getId(),page.getOffset(),page.getLimit());
        List<Map<String, Object>> conversations = new ArrayList<>();
        if (messageList != null){
            for (Message message :messageList) {
                Map<String,Object> map = new HashMap<>();
                map.put("conversation",message);
                map.put("letterCount",messageService.queryConversationCount(message.getConversationId()));
                map.put("unreadCount",messageService.queryUnreadMessage(user.getId(),message.getConversationId()));
                //int targetId = user.getId() == message.getFromId()?message.getToId():message.getFromId();
                map.put("target",getLetterTarget(message.getConversationId()));

                conversations.add(map);
            }
        }
        model.addAttribute("conversations",conversations);

        int letterUnreadCount = messageService.queryUnreadMessage(user.getId(),null);
        model.addAttribute("letterUnreadCount",letterUnreadCount);
        int noticeUnreadCount = messageService.queryUnreadNoticeCount(user.getId(),null);
        model.addAttribute("noticeUnreadCount",noticeUnreadCount);

        return "/site/letter";

    }
    @RequestMapping(value = "/letter/detail/{conversationId}",method = RequestMethod.GET)
    public String getLetterDetail(Model model, Page page , @PathVariable("conversationId") String conversationId){
        //分页信息
        page.setRow(messageService.queryConversationCount(conversationId));
        page.setLimit(5);
        page.setPath("/letter/detail/"+conversationId);
        //会话详情
        List<Message> letterList = messageService.queryConversation(conversationId,page.getOffset(),page.getLimit());
        List<Map<String,Object>> letters = new ArrayList<>();
        if (letterList!=null){
            for (Message message :letterList) {
                Map<String,Object> map = new HashMap<>();
                map.put("message",message);
                map.put("fromUser",userService.findUserById(message.getFromId()));
                letters.add(map);
            }
        }
        model.addAttribute("letters",letters);
        //私信目标
        model.addAttribute("target",getLetterTarget(conversationId));

        //进入详情，设置已读
        List<Integer> ids = getLetterIds(letterList);
        if (!ids.isEmpty()){
            messageService.readMessage(ids);
        }
        return "/site/letter-detail";

    }

    private User getLetterTarget(String conversationId) {
        String[] ids = conversationId.split("_");
        int id0 = Integer.parseInt(ids[0]);
        int id1 = Integer.parseInt(ids[1]);

        if (hostHolder.getUser().getId() == id0) {
            return userService.findUserById(id1);
        } else {
            return userService.findUserById(id0);
        }
    }

    private List<Integer> getLetterIds(List<Message> messageList){
        List<Integer> ids = new ArrayList<>();
        User user = hostHolder.getUser();

        if (messageList != null){
            for (Message message: messageList) {
                if (user.getId().equals(message.getToId())   && message.getStatus() == 0){
                    ids.add(message.getId());
                }
            }
        }

        return ids;

    }
    @RequestMapping(value = "/letter/send",method = RequestMethod.POST)
    @ResponseBody
    public String sendMessage(String toName,String content){
        Message message = new Message();
        User toUser = userService.queryUserByName(toName);
        if (toUser == null){
            return CommunityUtil.getJSONString(1,"该用户不存在");
        }
        message.setToId(toUser.getId());
        message.setFromId(hostHolder.getUser().getId());
        if (toUser.getId() < hostHolder.getUser().getId()){
            message.setConversationId(toUser.getId() + "_" + hostHolder.getUser().getId());
        }else {
            message.setConversationId(hostHolder.getUser().getId() + "_" + toUser.getId());
        }
        message.setContent(content);
        message.setCreateTime(new Date());
        message.setStatus(0);
        messageService.addMessage(message);
        return CommunityUtil.getJSONString(0);
    }

    /*@RequestMapping(value = "/letter/delete",method = RequestMethod.POST)
    @ResponseBody
    public String deleteMessage(String id){
        int ret = messageService.deleteMessage(Integer.valueOf(id));
        if (ret == 1){
            return CommunityUtil.getJSONString(0);
        }else {
            return CommunityUtil.getJSONString(1,"删除失败");
        }
    }*/

    @RequestMapping("/notice/list")
    public String noticeList(Model model){
        User user = hostHolder.getUser();

        //查询评论类通知
        Message message = messageService.queryLatestNotice(user.getId(),TOPIC_COMMENT);
        if (message != null){
            Map<String,Object> messageVo = new HashMap<>();
            messageVo.put("message",message);

            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String,Object> data = JSONObject.parseObject(content,HashMap.class);

            messageVo.put("user",userService.findUserById((Integer) data.get("userId")));
            messageVo.put("entityType",data.get("entityType"));
            messageVo.put("entityId",data.get("entityId"));
            messageVo.put("postId",data.get("postId"));

            int count = messageService.queryNoticeCount(user.getId(),TOPIC_COMMENT);
            messageVo.put("count",count);

            int unread = messageService.queryUnreadNoticeCount(user.getId(),TOPIC_COMMENT);
            messageVo.put("unread",unread);
            model.addAttribute("commentNotice",messageVo);
        }


        //查询点赞类通知
        message = messageService.queryLatestNotice(user.getId(),TOPIC_LIKE);
        if (message != null){
            Map<String,Object> messageVo = new HashMap<>();
            messageVo.put("message",message);

            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String,Object> data = JSONObject.parseObject(content,HashMap.class);

            messageVo.put("user",userService.findUserById((Integer) data.get("userId")));
            messageVo.put("entityType",data.get("entityType"));
            messageVo.put("entityId",data.get("entityId"));
            messageVo.put("postId",data.get("postId"));

            int count = messageService.queryNoticeCount(user.getId(),TOPIC_LIKE);
            messageVo.put("count",count);

            int unread = messageService.queryUnreadNoticeCount(user.getId(),TOPIC_LIKE);
            messageVo.put("unread",unread);
            model.addAttribute("likeNotice",messageVo);
        }


        //查询关注类通知
        message = messageService.queryLatestNotice(user.getId(),TOPIC_FOLLOW);
        if (message != null){
            Map<String,Object> messageVo = new HashMap<>();
            messageVo.put("message",message);

            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String,Object> data = JSONObject.parseObject(content,HashMap.class);

            messageVo.put("user",userService.findUserById((Integer) data.get("userId")));
            messageVo.put("entityType",data.get("entityType"));
            messageVo.put("entityId",data.get("entityId"));

            int count = messageService.queryNoticeCount(user.getId(),TOPIC_FOLLOW);
            messageVo.put("count",count);

            int unread = messageService.queryUnreadNoticeCount(user.getId(),TOPIC_FOLLOW);
            messageVo.put("unread",unread);
            model.addAttribute("followNotice",messageVo);
        }


        //查询未读消息数量
        int letterUnreadCount = messageService.queryUnreadMessage(user.getId(),null);
        model.addAttribute("letterUnreadCount",letterUnreadCount);
        int noticeUnreadCount = messageService.queryUnreadNoticeCount(user.getId(),null);
        model.addAttribute("noticeUnreadCount",noticeUnreadCount);
        return "/site/notice";
    }

    @RequestMapping("/notice/detail/{topic}")
    public String noticeDetail(@PathVariable("topic") String topic, Model model, Page page){
        User user = hostHolder.getUser();
        page.setPath("/notice/detail/"+topic);
        page.setLimit(5);
        page.setRow(messageService.queryNoticeCount(user.getId(),topic));

        //通知列表
        List<Map<String,Object>> noticeVo = new ArrayList<>();
        List<Message> noticeList = messageService.queryNoticeListByTopic(user.getId(),topic,page.getOffset(),page.getLimit());
        for (Message notice:noticeList){
            Map<String,Object> map = new HashMap<>();

            map.put("notice",notice);
            String content = HtmlUtils.htmlUnescape(notice.getContent());
            Map<String,Object> data = JSONObject.parseObject(content,HashMap.class);
            map.put("user",userService.findUserById((Integer) data.get("userId")));
            map.put("entityType",data.get("entityType"));
            map.put("entityId",data.get("entityId"));
            map.put("postId",data.get("postId"));
            //通知作者
            map.put("fromUser",userService.findUserById(notice.getFromId()));

            noticeVo.add(map);
        }
        model.addAttribute("notices",noticeVo);

        //设置已读
        List<Integer> ids = getLetterIds(noticeList);
        if (!ids.isEmpty()){
            messageService.readMessage(ids);
        }
        return "/site/notice-detail";


    }
}
