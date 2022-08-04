package com.nowcoder.community.service;

import com.nowcoder.community.dao.UserDao;
import com.nowcoder.community.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class UserService {

    @Resource
    private UserDao userDao;

    public User findUserById(int id){
        return userDao.selectById(id);
    }


}
