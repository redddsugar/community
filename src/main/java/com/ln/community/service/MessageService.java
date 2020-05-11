package com.ln.community.service;

import com.ln.community.dao.MessageMapper;
import com.ln.community.entity.Message;
import com.ln.community.util.BanFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class MessageService {

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private BanFilter banFilter;

    public List<Message> findConversations(int userId, int offset, int limit) {
        return messageMapper.selectConversations(userId, offset, limit);
    }

    public int findConversationCount(int userId) {
        return messageMapper.selectConversationCount(userId);
    }

    public List<Message> findLetters(String conversationId, int offset, int limit) {
        return messageMapper.selectLetters(conversationId, offset, limit);
    }

    public int findLetterCount(String conversationId) {
        return messageMapper.selectLetterCount(conversationId);
    }

    public int findLetterUnreadCount(int userId, String conversationId) {
        return messageMapper.selectLetterUnreadCount(userId, conversationId);
    }

    public int addMessage(Message message) {
        message.setContent(HtmlUtils.htmlEscape(message.getContent()));
        message.setContent(banFilter.filter(message.getContent()));
        return messageMapper.insertMessage(message);
    }

    public int readMessage(List<Integer> ids) {
        return messageMapper.updateStatus(ids, 1);
    }

    public Message findLatestMessage(int userId, String topic){
        return messageMapper.selectLatestMessage(userId, topic);
    }

    public int findMessageCount(int userId, String topic) {
        return messageMapper.selectMessageCount(userId, topic);
    }

    public int findMessageUnreadCount(int userId, String topic) {
        return messageMapper.selectMessageUnreadCount(userId, topic);
    }

    public List<Message> findMessages(int userId, String topic, int offset, int limit){
        return messageMapper.selectMessages(userId, topic, offset, limit);
    }
}
