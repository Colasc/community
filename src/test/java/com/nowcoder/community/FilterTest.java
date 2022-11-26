package com.nowcoder.community;

import com.nowcoder.community.util.SensitiveFilter;


import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import javax.annotation.Resource;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class FilterTest {
    @Resource
    SensitiveFilter sensitiveFilter;
    @Test
    public void test(){
        String text = "我要去⭐赌☆博⭐，然后⭐吸☆毒⭐，还有⭐嫖☆娼⭐,最后还要给⭐我⭐开⭐⭐票⭐";
        String result = sensitiveFilter.filter(text);
        System.out.println(result);

    }
}
