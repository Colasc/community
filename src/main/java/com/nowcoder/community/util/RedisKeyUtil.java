package com.nowcoder.community.util;

import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import javax.security.auth.callback.TextInputCallback;
import java.lang.ref.PhantomReference;
import java.util.Date;

public class RedisKeyUtil {

    private static final String SPLIT = ":";
    private static final String PREFIX_ENTITY_LIKE = "like:entity";
    private static final String PREFIX_USER_LIKE = "like:user";
    private static final String PREFIX_FOLLOWEE = "followee";
    private static final String PREFIX_FOLLOWER = "follower";
    private static final String PREFIX_USER = "user";
    private static final String PREFIX_KAPTCHA = "kaptcha";
    private static final String PREFIX_TICKET = "ticket";
    private static final String PREFIX_UV = "uv";
    private static final String PREFIX_DAU = "dau";
    private static final String PREFIX_SCORE = "post";


    public static String getEntityLikeKey(int entityType,int entityId){
        return PREFIX_ENTITY_LIKE+SPLIT+entityType+SPLIT+entityId;
    }

    public static String getUserLikeKey(int userId){
        return PREFIX_USER_LIKE + SPLIT + userId;
    }

    //用户关注的实体
    //followee:userId:entityType--->zset(entityType,now)
    public static String getFolloweeKey(int userId,int entityType){
        return PREFIX_FOLLOWEE+SPLIT+userId+SPLIT+entityType;
    }
    //某个实体拥有的粉丝
    //follower:entityType:userId--->zset(userId,now)
    public static String getFollowerKey(int entityType,int entityId){
        return PREFIX_FOLLOWER+SPLIT+entityType+SPLIT+entityId;
    }
    //用户
    public static String getUserKey(int userId){
        return PREFIX_USER + SPLIT + userId;
    }
    //验证码
    public static String getKaptchaKey(String owner){
        return PREFIX_KAPTCHA + SPLIT + owner;
    }
    //登陆凭证
    public static String getTicketKey(String ticket){
        return PREFIX_TICKET + SPLIT + ticket;
    }
    //当日访问的ip
    public static String getUVKey(String date){
        return PREFIX_UV + SPLIT + date;
    }
    //区间UV
    public static String getUVKey(String start,String end){
        return PREFIX_UV + SPLIT + start + SPLIT + end;
    }
    //当日DAU
    public static String getDAUKey(String date){
        return PREFIX_DAU + SPLIT + date;
    }

    //区间DAU
    public static String getDAUKey(String start,String end){
        return PREFIX_DAU +SPLIT + start +SPLIT +end;
    }
    //帖子分数
    public static String getScoreKey(){
        return PREFIX_SCORE + SPLIT + "score";
    }
}
