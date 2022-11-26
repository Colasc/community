package com.nowcoder.community.service;

import com.nowcoder.community.dao.LoginTicketDao;
import com.nowcoder.community.dao.UserDao;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.MailClient;
import com.nowcoder.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class UserService implements CommunityConstant {

    @Resource
    private UserDao userDao;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;
    /*@Resource
    private LoginTicketDao loginTicketDao;*/

    @Resource
    private TemplateEngine templateEngine;

    @Resource
    private MailClient mailClient;

    @Resource
    RedisTemplate redisTemplate;

    public User findUserById(int id){
        //return userDao.selectById(id);
        User user = getCache(id);
        if (user == null) {
            user = initCache(id);
        }
        return user;
    }

    public Map<String,String> addUser(User user){
        Map<String,String> map = new HashMap<>();
        //判断user是否为空
        if (user == null){
            throw new IllegalArgumentException("参数不能为空!");
        }
        if (StringUtils.isBlank(user.getUsername())){
            map.put("usernameMsg","账号不能为空");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())){
            map.put("passwordMsg","密码不能为空");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())){
            map.put("emailMsg","邮箱不能为空");
            return map;
        }
        User u = userDao.selectByName(user.getUsername());
        if (u != null){
            map.put("usernameMsg","该用户已存在");
            return map;
        }
        u = userDao.selectByEmail(user.getEmail());
        if (u != null){
            map.put("emailMsg","该邮箱已被注册");
            return map;
        }
        //注册用户
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        user.setPassword(CommunityUtil.md5(user.getPassword()+user.getSalt()));
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setStatus(0);
        user.setType(0);
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userDao.insertUser(user);
        //激活邮件
        //context中存放需要的数据
        Context context = new Context();
        context.setVariable("email",user.getEmail());
        String url = domain + contextPath + "/activation/" + user.getId() +"/"+ user.getActivationCode();
        context.setVariable("url",url);
        //发送html邮件，使用模板thymeleaf，创建一个模板放在/mail/activation
        //通过template.process方法将html模板转为动态网页字符串
        String content  = templateEngine.process("/mail/activation",context);
        mailClient.sendMail(user.getEmail(),"激活账号",content);
        return map;
    }

    public int activation(int userId,String code){
        User user = userDao.selectById(userId);
        if (user.getStatus() != 0){
            return REGISTER_REPEAT;
        }
        if (user.getActivationCode().equals(code)){
            userDao.updateStatus(userId,1);
            clearCache(userId);
            return REGISTER_SUCCESS;
        }
        return REGISTER_FAIL;
    }

    public Map<String,Object> login(String username ,String password ,int expiredSeconds){
        Map<String,Object> map = new HashMap<>();
        if (StringUtils.isBlank(username)){
            map.put("usernameMsg","用户名不能为空");
            return map;
        }
        if (StringUtils.isBlank(password)){
            map.put("passwordMsg","密码不能为空");
            return map;
        }

        User user = userDao.selectByName(username);
        if (user == null){
            map.put("usernameMsg","该用户不存在");
            return map;
        }
        if (!CommunityUtil.md5(password + user.getSalt()).equals(user.getPassword())){
            map.put("passwordMsg","密码错误");
            return map;
        }
        String ticket = CommunityUtil.generateUUID();
        Date time = new Date(System.currentTimeMillis() + expiredSeconds *1000);
        LoginTicket loginTicket = new LoginTicket(user.getId(),ticket,0,time);
        //loginTicketDao.insertLoginTicket(loginTicket);
        String ticketKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(ticketKey,loginTicket);
        map.put("ticket",ticket);
        return map;
    }

    public void logout(String ticket){
        //loginTicketDao.updateTicketStatus(ticket,1);
        String ticketKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(ticketKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(ticketKey,loginTicket);

    }

    public LoginTicket queryTicket(String ticket){
//        return loginTicketDao.selectByTicket(ticket);
          String ticketKey = RedisKeyUtil.getTicketKey(ticket);
          return (LoginTicket) redisTemplate.opsForValue().get(ticketKey);
    }

    public int updateHeader(int id, String headerUrl){
       int row = userDao.updateHeader(id,headerUrl);
       clearCache(id);
       return row;
    }

    public int updatePassword(int id ,String password){
        int row =  userDao.updatePassword(id,password);
        clearCache(id);
        return row;
    }

    public User queryUserByName(String username){
        return userDao.selectByName(username);
    }

    //从缓存中获取用户信息 ---->redis
    public User getCache(int userId){
        String redisKey = RedisKeyUtil.getUserKey(userId);
        User user = (User) redisTemplate.opsForValue().get(redisKey);
        return user;
    }

    //如果缓存中获取不到，则初始化缓存
    public User initCache(int userId){
        User user = userDao.selectById(userId);
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(redisKey,user,3600, TimeUnit.SECONDS);
        return user;
    }

    //输就变更时清除缓存数据
    public void clearCache(int userId){
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(redisKey);
    }

    public Collection<? extends GrantedAuthority> getAuthorities(int userId){
        User  user = this.findUserById(userId);

        List<GrantedAuthority> list = new ArrayList<>();
        list.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                switch (user.getType()){
                    case 1:
                        return AUTHORITY_ADMIN;
                    case 2:
                        return AUTHORITY_MODERATOR;
                    default:
                        return AUTHORITY_USER;
                }
            }
        });
        return list;
    }


}
