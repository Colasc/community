package com.nowcoder.community;

import com.alibaba.fastjson.JSONObject;
import com.nowcoder.community.dao.DiscussPostDao;
import com.nowcoder.community.dao.elasticsearch.DiscussPostRepository;
import com.nowcoder.community.entity.DiscussPost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;


import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;

import org.springframework.test.context.ContextConfiguration;


import javax.annotation.Resource;
import java.util.LinkedList;
import java.util.List;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class ElasticsearchTest {
    @Resource
    private DiscussPostDao discussPostDao;

    @Resource
    private DiscussPostRepository discussPostRepository;

    @Resource
    private RestHighLevelClient restHighLevelClient;

    @Test
    public void testInsert(){
        discussPostRepository.save(discussPostDao.selectDiscussPostById(241));
        discussPostRepository.save(discussPostDao.selectDiscussPostById(242));
        discussPostRepository.save(discussPostDao.selectDiscussPostById(243));
    }

    @Test
    public void testInsertList(){

        discussPostRepository.saveAll(discussPostDao.selectDiscussPosts(101,0,100,0));
        discussPostRepository.saveAll(discussPostDao.selectDiscussPosts(102,0,100,0));
        discussPostRepository.saveAll(discussPostDao.selectDiscussPosts(103,0,100,0));
        discussPostRepository.saveAll(discussPostDao.selectDiscussPosts(111,0,100,0));
        discussPostRepository.saveAll(discussPostDao.selectDiscussPosts(112,0,100,0));
        discussPostRepository.saveAll(discussPostDao.selectDiscussPosts(131,0,100,0));
        discussPostRepository.saveAll(discussPostDao.selectDiscussPosts(132,0,100,0));
        discussPostRepository.saveAll(discussPostDao.selectDiscussPosts(133,0,100,0));
        discussPostRepository.saveAll(discussPostDao.selectDiscussPosts(134,0,100,0));
    }

    @Test
    public void testUpdate(){
        DiscussPost post = discussPostDao.selectDiscussPostById(231);
        post.setContent("我是新人，使劲灌水");
        discussPostRepository.save(post);
    }

    @Test
    public void testSearchByRepository() throws Exception {
        SearchRequest searchRequest = new SearchRequest("discusspost");

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                .query(QueryBuilders.multiMatchQuery("互联网寒冬", "title", "content"))
                .sort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .sort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .sort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .from(0).size(10);

        // elasticTemplate.queryForPage(searchQuery, class, SearchResultMapper)
        // 底层获取得到了高亮显示的值, 但是没有返回.
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        System.out.println(JSONObject.toJSON(searchResponse));

        List<DiscussPost> list = new LinkedList<>();
        for (SearchHit hit:searchResponse.getHits().getHits()) {
            DiscussPost discussPost = JSONObject.parseObject(hit.getSourceAsString(),DiscussPost.class);
            System.out.println(discussPost);
            list.add(discussPost);
        }
    }

    @Test
    public void testHighlightQuery() throws Exception{
        SearchRequest searchRequest = new SearchRequest("discusspost");
        //高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title");
        highlightBuilder.field("content");
        highlightBuilder.requireFieldMatch(false);
        highlightBuilder.preTags("<em>");
        highlightBuilder.postTags("</em>");
        //构建搜索条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                .query(QueryBuilders.multiMatchQuery("互联网寒冬","title","content"))
                .sort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .sort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .sort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .from(0)
                .size(10)
                .highlighter(highlightBuilder);

        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest,RequestOptions.DEFAULT);

        List<DiscussPost> list = new LinkedList<>();
        for (SearchHit hit :searchResponse.getHits().getHits()) {
            DiscussPost discussPost = JSONObject.parseObject(hit.getSourceAsString(),DiscussPost.class);

            //处理高亮显示的结果
            HighlightField titleField  = hit.getHighlightFields().get("title");
            if (titleField!= null){
                discussPost.setTitle(titleField.getFragments()[0].toString());
            }
            HighlightField contentField  = hit.getHighlightFields().get("title");
            if (contentField!= null){
                discussPost.setContent(contentField.getFragments()[0].toString());
            }
            System.out.println(discussPost);
            list.add(discussPost);
        }
    }

}
