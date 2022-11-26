package com.nowcoder.community.service;

import com.nowcoder.community.dao.MessageDao;
import com.nowcoder.community.entity.Message;
import com.nowcoder.community.util.SensitiveFilter;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.Resource;
import java.util.List;

@Service
public class MessageService {

    @Resource
    private MessageDao messageDao;
    @Resource
    private SensitiveFilter sensitiveFilter;

    public List<Message> queryMessageList(int userId,int offset ,int limit){
        return messageDao.selectMessageLists(userId,offset,limit);
    }

    public int queryMessageCount(int userId){
        return messageDao.selectMessageCount(userId);
    }

    public List<Message> queryConversation(String conversationId,int offset,int limit){
        return messageDao.selectMessageForConversation(conversationId,offset,limit);
    }

    public int queryConversationCount(String conversationId){
        return messageDao.selectCountForConversation(conversationId);
    }

    public int queryUnreadMessage(int userId,String conversationId){
        return messageDao.selectUnreadCount(userId, conversationId);
    }

    public int readMessage(List<Integer> ids){
        return messageDao.updateMessageStatus(ids,1);
    }

    public int addMessage(Message message){
        message.setContent(HtmlUtils.htmlEscape(message.getContent()));
        message.setContent(sensitiveFilter.filter(message.getContent()));
        return messageDao.insertMessage(message);
    }

    public int deleteMessage(int id){
        return messageDao.updateSingleMessage(id,2);
    }

    public Message queryLatestNotice(int userId,String topic){
        return messageDao.selectLatestNotice(userId, topic);
    }

    public int queryNoticeCount(int userId,String topic){
        return messageDao.selectNoticeCount(userId,topic);
    }

    public int queryUnreadNoticeCount(int userId,String topic){
        return messageDao.selectUnreadNoticeCount(userId, topic);
    }

    public List<Message> queryNoticeListByTopic(int userId,String topic,int offset ,int limit){
        return messageDao.selectNoticeListByTopic(userId, topic, offset, limit);
    }
}
