package com.nowcoder.community;

import com.nowcoder.community.dao.LoginTicketDao;
import com.nowcoder.community.dao.MessageDao;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.Message;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.UserService;


import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;


@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
class CommunityApplicationTests {

    @Resource
    private UserService userService;

    @Resource
    private DiscussPostService discussPostService;

    @Resource
    private LoginTicketDao loginTicketDao;

    @Resource
    private MessageDao messageDao;

    private static final Logger logger = LoggerFactory.getLogger(CommunityApplicationTests.class);

    @Test
    public void testSelectUser(){
        User user = userService.findUserById(101);
        System.out.println(user);


        int rows = discussPostService.queryDiscussPortRows(0);
        System.out.println(rows);
    }

    @Test
    public void testLogger(){
        System.out.println(logger.getName());

        logger.debug("debug log");
        logger.info("info log");
        logger.warn("warn log");
        logger.error("error.log");

    }

    @Test
    public void testLoginTicket(){
        LoginTicket loginTicket = new LoginTicket(101,"abc",0,new Date());
        loginTicketDao.insertLoginTicket(loginTicket);
    }
    @Test
    public void testTicket(){
        LoginTicket loginTicket = loginTicketDao.selectByTicket("abc");
        System.out.println(loginTicket.toString());
        loginTicketDao.updateTicketStatus(loginTicket.getTicket(),1);
    }

    @Test
    public void testMessage(){
        List<Message> messageList = messageDao.selectMessageLists(111,0,20);
        System.out.println(messageList);

        System.out.println(messageDao.selectMessageCount(111));

        List<Message> conversationList = messageDao.selectMessageForConversation("111_112",0,20);
        System.out.println(messageDao.selectCountForConversation("111_112"));

        System.out.println(messageDao.selectUnreadCount(111,"111_112"));


    }
}
