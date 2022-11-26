package com.nowcoder.community.controller;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.service.ElasticsearchService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SearchController implements CommunityConstant {
    @Resource
    private LikeService likeService;
    @Resource
    private ElasticsearchService elasticsearchService;
    @Resource
    private UserService userService;
    @Resource
    private CommentService commentService;
    @RequestMapping(value = "/search" ,method = RequestMethod.GET)
    public String search(String keyword , Page page , Model model) throws Exception {
        //搜索帖子
        List<DiscussPost> list = elasticsearchService.searchDiscussPost(keyword,page.getCurrent()-1,page.getLimit());
        //聚合数据
        List<Map<String,Object>> discussPosts = new ArrayList<>();
        for (DiscussPost post :list) {
            Map<String,Object> map = new HashMap<>();
            map.put("post",post);
            map.put("user",userService.findUserById(post.getUserId()));
            map.put("likeCount",likeService.findEntityLikeCount(ENTITY_TYPE_POST,post.getId()));
            map.put("commentCount",commentService.queryCountByEntity(ENTITY_TYPE_POST,post.getId()));
            discussPosts.add(map);
        }

        model.addAttribute("discussPosts",discussPosts);
        model.addAttribute("keyword",keyword);

        page.setPath("/search?keyword="+keyword);
        page.setRow(list != null?list.size():0);
        return "site/search";
    }
}
