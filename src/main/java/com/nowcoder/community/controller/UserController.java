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
        //上传文件名称
        String fileName = CommunityUtil.generateUUID();
        //设置相应信息
        StringMap policy = new StringMap();
        policy.put("returnBody",CommunityUtil.getJSONString(0));
        //生成上传凭证
        Auth auth = Auth.create(assessKey,secretKey);
        String uploadToken =  auth.uploadToken(headerBucketName,fileName,3600,policy);

        model.addAttribute("uploadToken",uploadToken);
        model.addAttribute("fileName",fileName);

        return "/site/setting";
    }

    //更新头像路径
    @RequestMapping(value = "/header/url",method = RequestMethod.POST)
    @ResponseBody
    public String updateHeaderUrl(String fileName){
        if (StringUtils.isBlank(fileName)){
            return CommunityUtil.getJSONString(1,"文件名不能为空！");
        }

        String url = headerBucketUrl + "/" + fileName;
        userService.updateHeader(hostHolder.getUser().getId(),url);

        return CommunityUtil.getJSONString(0);
    }


    //废弃
    @LoginAnnotation
    @RequestMapping(value = "/upload",method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model){
        //判断是否上传图片
        if (headerImage == null){
            model.addAttribute("error","您还没有选择图片！");
            return "/site/setting";
        }
        //判断图片的格式是否正确
        String fileName = headerImage.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        if (StringUtils.isBlank(suffix)){
            model.addAttribute("error","上传的文件格式错误！");
            return "site/setting";
        }
        //上传图片到本地，修改user中图片链接
        //生成随机的文件名
        fileName = CommunityUtil.generateUUID()+suffix;
        //确定文件的存放路径
        File dest = new File(uploadPath + "/"+fileName);
        //存储文件
        try {
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败"+e.getMessage());
            throw new RuntimeException("上传文件失败，服务器异常！",e);
        }

        //更新当前用户的头像路径
        //http://localhost:8080/community/user/header/xxx.png
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath +"/user/header/"+fileName;
        userService.updateHeader(user.getId(),headerUrl);
        return "redirect:/index";

    }
    //废弃
    @RequestMapping(value = "/header/{fileName}" ,method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response){
        //服务器存放路径
        fileName = uploadPath + "/" + fileName;
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        //响应图片
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
            logger.error("读取头像失败:"+e.getMessage());
        }
    }

    @LoginAnnotation
    @RequestMapping(value = "/updatePassword" ,method = RequestMethod.POST)
    public String updataPassword(String oldPassword,String newPassword,String confirmPassword, Model model){
        if (StringUtils.isBlank(oldPassword)){
            model.addAttribute("oldPMsg","请输入原密码");
            return "site/setting";
        }
        if (StringUtils.isBlank(newPassword)){
            model.addAttribute("newPMsg","请输入密码");
            return "site/setting";
        }
        if (newPassword.length()<8){
            model.addAttribute("newPMsg","密码长度不能小于8位");
            return "site/setting";
        }
        //验证两次密码输入是否正确
        if (!newPassword.equals(confirmPassword)){
            model.addAttribute("conPasswordMsg","两次密码输入不一致");
            return "site/setting";
        }

        //验证原密码是否正确
        User user = hostHolder.getUser();
        if (!CommunityUtil.md5(oldPassword + user.getSalt()).equals(user.getPassword())){
            model.addAttribute("oldPMsg","原密码错误,请重新输入");
            return "site/setting";
        }


        //更新密码
        newPassword = CommunityUtil.md5(newPassword+user.getSalt());
        userService.updatePassword(user.getId(),newPassword);
        return "redirect:/index";
        

    }

    @RequestMapping(value = "/profile/{userId}",method = RequestMethod.GET)
    public String getLikeCount(@PathVariable("userId") int userId,Model model){
        User user = userService.findUserById(userId);
        if (user == null){
            throw new IllegalArgumentException("该用户不存在!");
        }
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount",likeCount);
        model.addAttribute("user",user);

        //查询关注的实体数量
        long followeeCount = followService.findFolloweeCount(userId,CommunityConstant.ENTITY_TYPE_USER);
        model.addAttribute("followeeCount",followeeCount);
        //查询粉丝数量
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
