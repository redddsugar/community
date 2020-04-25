package com.ln.community.controller;

import com.ln.community.annotation.LoginRequired;
import com.ln.community.entity.User;
import com.ln.community.service.UserService;
import com.ln.community.util.CommunityUtil;
import com.ln.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.upload}")
    private String uploadPath;
    @Value("${community.path.domain}")
    private String domain;
    @Value("${server.servlet.context-path}")
    private String contextPath;
    @Autowired
    private UserService userService;
    @Autowired
    private HostHolder hostHolder;

    @LoginRequired
    @GetMapping("/setting")
    public String getSettingPage() {
        return "/site/setting";
    }

    @LoginRequired
    @PostMapping("/upload")
    public String uploadHeader(MultipartFile headerImage, Model model) {
        if (headerImage == null){
            model.addAttribute("error", "无有效的图片对象");
            return "/site/setting";
        }

        String filename = headerImage.getOriginalFilename();
        String suffix = filename.substring(filename.lastIndexOf("."));
        if (StringUtils.isBlank(suffix)) {
            model.addAttribute("error", "无效的后缀名");
            return "/site/setting";
        }

        //生成随机的文件名   "random.suffix"
        filename = CommunityUtil.generateUUID() + suffix;
        //存放的本地路径
        File dest = new File(uploadPath+"/"+filename);
        try {
            //存
            headerImage.transferTo(dest);
        } catch (IOException e){
            logger.error("上传失败 "+e.getMessage());
            throw new RuntimeException("上传文件失败,服务异常"+e);
        }

        //更新头像路径   http://localhost:8080/community/user/header/random.suffix
        User user = hostHolder.getUser();
        String headUrl = domain+contextPath+"/user/header/"+filename;
        userService.updateHeader(user.getId(), headUrl);

        return "redirect:/index";
    }

    @GetMapping("/header/{fileName}")
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        //服务器存放路径
        fileName = uploadPath+"/"+fileName;
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        //响应图片
        response.setContentType("image/"+suffix);
        try (
                FileInputStream fis = new FileInputStream(fileName);
                ServletOutputStream os = response.getOutputStream();
        ){
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b=fis.read(buffer)) != -1){
                os.write(buffer, 0 ,b);
            }
        } catch (IOException e) {
            logger.error("图像读取失败"+e.getMessage());
        }
    }

    @PostMapping("/updatePwd")
    public String updatePwd(String oldPwd, String newPwd, String confirmPassword, Model model) {
        Map<String, Object> map = userService.updatePwd(oldPwd, newPwd, confirmPassword);
        if (map != null) {
            model.addAttribute("oldMsg", map.get("oldMsg"));
            model.addAttribute("newMsg", map.get("newMsg"));
            model.addAttribute("newMsg2", map.get("newMsg2"));
            model.addAttribute("pwd", map.get("pwd"));
            return "/site/setting";
        }
        return "redirect:/login";
    }
}
