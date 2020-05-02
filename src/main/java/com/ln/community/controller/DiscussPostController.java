package com.ln.community.controller;

import com.ln.community.entity.*;
import com.ln.community.service.CommentService;
import com.ln.community.service.DiscussPostService;
import com.ln.community.service.LikeService;
import com.ln.community.service.UserService;
import com.ln.community.util.CommunityConstant;
import com.ln.community.util.HostHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {

    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private UserService userService;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    CommentService commentService;
    @Autowired
    LikeService likeService;

    @PostMapping("/add")
    @ResponseBody
    public Result addDiscussPost(String title, String content) {
        User user = hostHolder.getUser();
        if (user == null) {
            return ResultGenerator.genAuthResult("用户未登录");
        }

        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle(title);
        post.setContent(content);
        post.setCreateTime(new Date());

        discussPostService.addDiscussPost(post);
        return ResultGenerator.genSuccessResult();
    }

    @GetMapping("/detail/{discussPostId}")
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model, Page page) {
        DiscussPost post = discussPostService.findDiscussPostById(discussPostId);
        model.addAttribute("post", post);
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user", user);
        //点赞数量与状态
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeCount", likeCount);
        int likeStatus = likeService.findEntityLikeStatus(user.getId(), ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeStatus", likeStatus);

        //评论分页信息
        page.setPath("/discuss/detail/"+discussPostId);
        page.setLimit(5);
        page.setRows(post.getCommentCount());


        // 一级评论
        // 二级评论
//        此处查出一级评论列表
        List<Comment> commentList = commentService.findCommentByEntity
                (ENTITY_TYPE_POST, post.getId(), page.getOffset(), page.getLimit());
        //一级评论的ViewObject         List = [map1(comment:XX,user:XX,likeCount:XX,likeStatus:XX), map2(comment:XX,user:XX,likeCount:XX,likeStatus:XX)...]
        List<Map<String, Object>> commentVOList = new ArrayList<>();
        if (commentList != null) {
            for (Comment comment:commentList) {
                HashMap<String, Object> commentVO = new HashMap<>();
                commentVO.put("comment", comment);
                commentVO.put("user", userService.findUserById(comment.getUserId()));
                likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, comment.getId());
                commentVO.put("likeCount", likeCount);
                likeStatus = likeService.findEntityLikeStatus(user.getId(), ENTITY_TYPE_COMMENT, comment.getId());
                commentVO.put("likeStatus", likeStatus);

                //此处查出二级评论列表  不做分页
                List<Comment> replyList = commentService.findCommentByEntity
                        (ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);
                //二级评论的ViewObject,    List = [map1(reply:XX,user:XX), map2(reply:XX,user:XX)...]
                ArrayList<Map<String, Object>> replyVOList = new ArrayList<>();
                if (replyList != null) {
                    for (Comment reply:replyList) {
                        HashMap<String, Object> replyVO = new HashMap<>();
                        replyVO.put("reply", reply);
                        replyVO.put("user", userService.findUserById(reply.getUserId()));
                        likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, reply.getId());
                        replyVO.put("likeCount", likeCount);
                        likeStatus = likeService.findEntityLikeStatus(user.getId(), ENTITY_TYPE_COMMENT, reply.getId());
                        replyVO.put("likeStatus", likeStatus);

                        //二级评论目标
                        User target = reply.getTargetId() == 0? null : userService.findUserById(reply.getTargetId());
                        replyVO.put("target", target);

                        replyVOList.add(replyVO);
                    }
                }
                commentVO.put("replys", replyVOList);

                //二级评论数量
                int replyCount = commentService.findCommentCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVO.put("replyCount", replyCount);

                commentVOList.add(commentVO);
            }
        }

        model.addAttribute("comments", commentVOList);
        return "/site/discuss-detail";
    }


}

