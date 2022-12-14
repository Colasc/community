package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginAnnotation;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.FollowService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

@Controller
@RequestMapping("/user")
public class UserController {

    public static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.domain}")
    private String domain;

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Resource
    private UserService userService;

    @Resource
    private HostHolder hostHolder;

    @Resource
    private LikeService likeService;

    @Resource
    private FollowService followService;

    @Value("${qiniu.key.access}")
    private String assessKey;

    @Value("${qiniu.key.secret}")
    private String secretKey;

    @Value("${qiniu.bucket.header.name}")
    private String headerBucketName;

    @Value("${quniu.bucket.header.url}")
    private String headerBucketUrl;

    @LoginAnnotation
    @RequestMapping(value = "/setting",method = RequestMethod.GET)
    public String settingHtml(Model model){
        //??????????????????
        String fileName = CommunityUtil.generateUUID();
        //??????????????????
        StringMap policy = new StringMap();
        policy.put("returnBody",CommunityUtil.getJSONString(0));
        //??????????????????
        Auth auth = Auth.create(assessKey,secretKey);
        String uploadToken =  auth.uploadToken(headerBucketName,fileName,3600,policy);

        model.addAttribute("uploadToken",uploadToken);
        model.addAttribute("fileName",fileName);

        return "/site/setting";
    }

    //??????????????????
    @RequestMapping(value = "/header/url",method = RequestMethod.POST)
    @ResponseBody
    public String updateHeaderUrl(String fileName){
        if (StringUtils.isBlank(fileName)){
            return CommunityUtil.getJSONString(1,"????????????????????????");
        }

        String url = headerBucketUrl + "/" + fileName;
        userService.updateHeader(hostHolder.getUser().getId(),url);

        return CommunityUtil.getJSONString(0);
    }


    //??????
    @LoginAnnotation
    @RequestMapping(value = "/upload",method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model){
        //????????????????????????
        if (headerImage == null){
            model.addAttribute("error","???????????????????????????");
            return "/site/setting";
        }
        //?????????????????????????????????
        String fileName = headerImage.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        if (StringUtils.isBlank(suffix)){
            model.addAttribute("error","??????????????????????????????");
            return "site/setting";
        }
        //??????????????????????????????user???????????????
        //????????????????????????
        fileName = CommunityUtil.generateUUID()+suffix;
        //???????????????????????????
        File dest = new File(uploadPath + "/"+fileName);
        //????????????
        try {
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("??????????????????"+e.getMessage());
            throw new RuntimeException("???????????????????????????????????????",e);
        }

        //?????????????????????????????????
        //http://localhost:8080/community/user/header/xxx.png
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath +"/user/header/"+fileName;
        userService.updateHeader(user.getId(),headerUrl);
        return "redirect:/index";

    }
    //??????
    @RequestMapping(value = "/header/{fileName}" ,method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response){
        //?????????????????????
        fileName = uploadPath + "/" + fileName;
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        //????????????
        response.setContentType("image/"+suffix);

        try (
                FileInputStream fis = new FileInputStream(fileName);
                OutputStream os = response.getOutputStream();
        ){
            byte[] buffer = new byte[1024];
            int b = 0;
            while ( (b = fis.read(buffer)) != -1){
                os.write(buffer,0,b);
            }
        } catch (IOException e) {
            logger.error("??????????????????:"+e.getMessage());
        }
    }

    @LoginAnnotation
    @RequestMapping(value = "/updatePassword" ,method = RequestMethod.POST)
    public String updataPassword(String oldPassword,String newPassword,String confirmPassword, Model model){
        if (StringUtils.isBlank(oldPassword)){
            model.addAttribute("oldPMsg","??????????????????");
            return "site/setting";
        }
        if (StringUtils.isBlank(newPassword)){
            model.addAttribute("newPMsg","???????????????");
            return "site/setting";
        }
        if (newPassword.length()<8){
            model.addAttribute("newPMsg","????????????????????????8???");
            return "site/setting";
        }
        //????????????????????????????????????
        if (!newPassword.equals(confirmPassword)){
            model.addAttribute("conPasswordMsg","???????????????????????????");
            return "site/setting";
        }

        //???????????????????????????
        User user = hostHolder.getUser();
        if (!CommunityUtil.md5(oldPassword + user.getSalt()).equals(user.getPassword())){
            model.addAttribute("oldPMsg","???????????????,???????????????");
            return "site/setting";
        }


        //????????????
        newPassword = CommunityUtil.md5(newPassword+user.getSalt());
        userService.updatePassword(user.getId(),newPassword);
        return "redirect:/index";
        

    }

    @RequestMapping(value = "/profile/{userId}",method = RequestMethod.GET)
    public String getLikeCount(@PathVariable("userId") int userId,Model model){
        User user = userService.findUserById(userId);
        if (user == null){
            throw new IllegalArgumentException("??????????????????!");
        }
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount",likeCount);
        model.addAttribute("user",user);

        //???????????????????????????
        long followeeCount = followService.findFolloweeCount(userId,CommunityConstant.ENTITY_TYPE_USER);
        model.addAttribute("followeeCount",followeeCount);
        //??????????????????
        long followerCount = followService.findFollowerCount(CommunityConstant.ENTITY_TYPE_USER,userId);
        model.addAttribute("followerCount",followerCount);

        boolean hasFollowed = false;
        if (hostHolder.getUser() != null){
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), CommunityConstant.ENTITY_TYPE_USER,userId);
        }
        model.addAttribute("hasFollowed",hasFollowed);

        return "/site/profile";
    }




}
