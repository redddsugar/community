package com.ln.community.controller;

import com.ln.community.entity.*;
import com.ln.community.event.EventProducer;
import com.ln.community.service.FollowService;
import com.ln.community.service.UserService;
import com.ln.community.util.CommunityConstant;
import com.ln.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class FollowController implements CommunityConstant {

    @Autowired
    FollowService followService;
    @Autowired
    HostHolder hostHolder;
    @Autowired
    UserService userService;
    @Autowired
    EventProducer eventProducer;

    @PostMapping("/follow")
    @ResponseBody
    public Result follow(int entityType, int entityId) {
        User user = hostHolder.getUser();

        followService.follow(user.getId(), entityType, entityId);

        //触发关注事件的发布
        Event event = new Event();
        event.setTopic(TOPIC_FOLLOW)
                .setUsrId(hostHolder.getUser().getId())
                .setEntityId(entityId)
                .setEntityUserId(entityId)
                .setEntityType(entityType);
        eventProducer.fireEvent(event);

        return ResultGenerator.genSuccessResult();
    }

    @PostMapping("/unfollow")
    public Result unFollow(int entityType, int entityId) {
        User user = hostHolder.getUser();

        followService.unFollow(user.getId(), entityType, entityId);

        return ResultGenerator.genSuccessResult();
    }

    @GetMapping("/followees/{userId}")
    public String getFollowees(@PathVariable("userId") int userId, Page page, Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在");
        }
        model.addAttribute("user", user);

        page.setLimit(5);
        page.setPath("/followees/"+userId);
        page.setRows((int)followService.findFolloweeCount(userId, CommunityConstant.ENTITY_TYPE_USER));

        List<Map<String, Object>> userList = followService.findFollowees(userId, page.getOffset(), page.getLimit());
        if (userList != null) {
            for (Map<String, Object> map:userList) {
                User u = (User) map.get("user");
                map.put("isFollowed", isFollowed(u.getId()));
            }
        }
        model.addAttribute("users", userList);

        return "/site/followee";
    }

    @GetMapping("/followers/{userId}")
    public String getFollowers(@PathVariable("userId") int userId, Page page, Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在");
        }
        model.addAttribute("user", user);

        page.setLimit(5);
        page.setPath("/followers/"+userId);
        page.setRows((int)followService.findFolloweeCount(userId, CommunityConstant.ENTITY_TYPE_USER));

        List<Map<String, Object>> userList = followService.findFollowers(userId, page.getOffset(), page.getLimit());
        if (userList != null) {
            for (Map<String, Object> map:userList) {
                User u = (User) map.get("user");
                map.put("isFollowed", isFollowed(u.getId()));
            }
        }
        model.addAttribute("users", userList);

        return "/site/follower";
    }

    public boolean isFollowed(int userId){
        if (hostHolder.getUser() == null) {
            return false;
        }

        return followService.isFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
    }
}
