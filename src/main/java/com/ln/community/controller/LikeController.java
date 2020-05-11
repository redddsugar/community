package com.ln.community.controller;

import com.ln.community.entity.Event;
import com.ln.community.entity.Result;
import com.ln.community.entity.ResultGenerator;
import com.ln.community.entity.User;
import com.ln.community.event.EventProducer;
import com.ln.community.service.LikeService;
import com.ln.community.util.CommunityConstant;
import com.ln.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class LikeController implements CommunityConstant {

    @Autowired
    LikeService likeService;
    @Autowired
    HostHolder hostHolder;
    @Autowired
    EventProducer eventProducer;

    @PostMapping("/like")
    public Result like(int entityType, int entityId, int entityUserId, int postId) {
        User user = hostHolder.getUser();

        //点赞
        likeService.like(user.getId(), entityType, entityId, entityUserId);
        //数量
        long likeCount = likeService.findEntityLikeCount(entityType, entityId);
        //状态
        int likeStatus = likeService.findEntityLikeStatus(user.getId(), entityType, entityId);

        Map<String, Object> map = new HashMap<>();
        map.put("likeCount", likeCount);
        map.put("likeStatus", likeStatus);

        //触发点赞事件的发布
        if (likeStatus == 1) {
            Event event = new Event()
                    .setTopic(TOPIC_LIKE)
                    .setEntityType(entityType)
                    .setUsrId(hostHolder.getUser().getId())
                    .setEntityUserId(entityUserId)
                    .setEntityId(entityId)
                    .setData("postId", postId);
            eventProducer.fireEvent(event);
        }


        return ResultGenerator.genSuccessResult(map);
    }
}
