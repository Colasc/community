package com.nowcoder.community.dao;

import com.nowcoder.community.entity.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MessageDao {

    // 查询当前用户的会话列表,针对每个会话只返回一条最新的私信.
    List<Message> selectMessageLists(int userId,int offset ,int limit);

    // 查询当前用户的会话数量.
    int selectMessageCount(int userId);

    // 查询某个会话所包含的私信列表.
    List<Message> selectMessageForConversation(String conversationId,int offset ,int limit );

    // 查询某个会话所包含的私信数量.
    int selectCountForConversation(String conversationId);

    // 查询未读私信的数量
    int selectUnreadCount(int userId,String conversationId);

    int updateMessageStatus(List<Integer> ids,int status);

    int insertMessage(Message message);

    int updateSingleMessage(int id,int status);

    //查询当前用户的通知列表，返回最新一条通知
    Message selectLatestNotice(int userId,String topic);

    //查询当前用户的通知数量
    int selectNoticeCount(int userId, String topic);

    //查询未读的通知数量
    int selectUnreadNoticeCount(int userId,String topic);

    //查询某个主题包含的消息列表
    List<Message> selectNoticeListByTopic(int userId,String topic,int offset ,int limit);

}
