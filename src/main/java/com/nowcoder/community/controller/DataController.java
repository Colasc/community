package com.nowcoder.community.controller;

import com.nowcoder.community.service.DataService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import javax.xml.crypto.Data;
import java.util.Date;

@Controller
public class DataController {

    @Resource
    private DataService dataService;

    @RequestMapping(value = "/data",method = {RequestMethod.GET,RequestMethod.POST})
    public String getData(){
        return "/site/admin/data";
    }
    //统计网站uv
    @RequestMapping(value = "/data/uv",method = RequestMethod.POST)
    public String getUVData(@DateTimeFormat(pattern = "yyyy-MM-dd") Date start ,
                            @DateTimeFormat(pattern = "yyyy-MM-dd") Date end,
                            Model model){
        long uv = dataService.calculateUV(start, end);
        model.addAttribute("uvResult",uv);
        model.addAttribute("startUV",start);
        model.addAttribute("endUV",end);
        return "forward:/data";
    }
    //统计活跃用户
    @RequestMapping(value = "/data/dau",method = RequestMethod.POST)
    public String getDAUData(@DateTimeFormat(pattern = "yyyy-MM-dd") Date start ,
                             @DateTimeFormat(pattern = "yyyy-MM-dd") Date end,
                             Model model){
        long dau = dataService.calculateDAU(start,end);
        model.addAttribute("dauResult",dau);
        model.addAttribute("startDAU",start);
        model.addAttribute("endDAU",end);
        return "forward:/data";
    }
}
