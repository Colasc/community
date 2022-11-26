package com.nowcoder.community;

import com.nowcoder.community.util.MailClient;


import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.annotation.Resource;


@SpringBootTest
//注意这个注解(classes,必须这样写，要不然component扫描不到)
@ContextConfiguration(classes = CommunityApplication.class)
public class MailTest {

    @Resource
    private MailClient mailClient;

    @Resource
    private TemplateEngine templateEngine;


    @Test
    public void testTextMail(){
        mailClient.sendMail("x1245042275@163.com","Test","Welcome");
    }
    @Test
    public void testHtml(){
        Context context = new Context();
        context.setVariable("username","sunday");
        String content = templateEngine.process("/mail/demo", context);
        System.out.println(content);

        mailClient.sendMail("x1245042275@163.com", "HTML", content);
    }
}
