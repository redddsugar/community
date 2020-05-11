package com.ln.community.dao;

import com.ln.community.entity.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MessageMapper {

    // 查询当前用户的会话列表,针对每个会话只返回一条最新的私信.
    List<Message> selectConversations(int userId, int offset, int limit);

    // 查询当前用户的会话数量.
    int selectConversationCount(int userId);

    // 查询某个会话所包含的私信列表.
    List<Message> selectLetters(String conversationId, int offset, int limit);

    // 查询某个会话所包含的私信数量.
    int selectLetterCount(String conversationId);

    // 查询未读私信的数量
    int selectLetterUnreadCount(int userId, String conversationId);

    // 新增消息
    int insertMessage(Message message);

    // 修改消息的状态
    int updateStatus(List<Integer> ids, int status);

    //查询某Topic下最新的通知
    Message selectLatestMessage(int userId, String topic);

    //查询某Topic包含的通知数量
    int selectMessageCount(int userId, String topic);

    //查询未读的通知的数量
    int selectMessageUnreadCount(int userId, String topic);

    //查询某个Topic所包含的通知列表
    List<Message> selectMessages(int userId, String topic, int offset, int limit);
}
