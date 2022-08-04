package com.nowcoder.community;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.UserService;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import javax.annotation.Resource;


@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
class CommunityApplicationTests {

    @Resource
    private UserService userService;

    @Resource
    private DiscussPostService discussPostService;

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
}
