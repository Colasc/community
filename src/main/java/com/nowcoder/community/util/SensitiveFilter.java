package com.nowcoder.community.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.tiles3.TilesViewResolver;

import javax.annotation.PostConstruct;
import javax.imageio.IIOException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {

    private static  final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);
    // 替换符
    private static final String REPLACEMENT = "***";
    //根节点
    private TrieNode rootNode = new TrieNode();

    @PostConstruct
    public void init(){
        try (
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                ) {
            String keyword;
            while ((keyword = reader.readLine()) != null) {
                this.addKeyword(keyword);
            }
        } catch (IOException e) {
            logger.error("加载敏感词文件失败: " + e.getMessage());
        }

    }
    //将敏感词输入前缀树
    private void addKeyword(String keyword){
        TrieNode tempnode = rootNode;
        for (int i = 0;i<keyword.length();i++){
            char c =  keyword.charAt(i);
            TrieNode subNodes = tempnode.getSubNodes(c);
            if (subNodes ==null){
                //初始化子节点
                subNodes = new TrieNode();
                tempnode.setSubNodes(c,subNodes);
            }
            //临时节点指向下一子节点，进入下一轮循环
            tempnode = subNodes;
            if (i == keyword.length()-1){
                tempnode.setKeywordEnd(true);
            }
        }
    }

    //前缀树
    private class TrieNode{
        //过滤词结束标志
        private boolean isKeywordEnd;
        //子节点
        private Map<Character,TrieNode> subNodes = new HashMap<>();

        private void setKeywordEnd(boolean keywordEnd){
            isKeywordEnd = keywordEnd;
        }

        private void setSubNodes(Character c,TrieNode node){
            subNodes.put(c,node);
        }

        private TrieNode getSubNodes(Character c){
            return subNodes.get(c);
        }

    }
    //判断字符是否为符号
    private boolean isSymbol(char c){
        //0x2E80~0x9FFF是东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c)  && (c < 0x2E80 || c > 0x9FFF);
    }
    //过滤敏感词
    public String filter(String text){
        if (StringUtils.isBlank(text)){
            return null;
        }
        //指针1
        TrieNode tempNode = rootNode;
        //指针2
        int begin = 0;
        //指针3
        int post = 0;
        StringBuilder sb = new StringBuilder();

        while(post < text.length()){
            char c = text.charAt(post);
            //判断是字符是否为符号，如果是直接跳过
            if (isSymbol(c)){
                if (tempNode == rootNode){
                    sb.append(c);
                    begin++;
                }
                //无论符号在开头还是中间，post指针都向后走，继续循环
                post++;
                continue;
            }
            //验证下一个子节点
            tempNode = tempNode.getSubNodes(c);
            if (tempNode == null){
                //如果子节点为空，则不是敏感词汇
                sb.append(text.charAt(begin));
                //进入下一个位置
                post = ++begin;
                //重新指向gen节点、
                tempNode = rootNode;
            }else if (tempNode.isKeywordEnd){
                //是敏感词汇
                sb.append(REPLACEMENT);
                //指向下一个位置
                begin = ++post;
                //重新指向根节点
                tempNode = rootNode;
            }else {
                //检查下一个字符
                post++;
            }
        }
        sb.append(text.substring(begin));
        return sb.toString();
    }
}
