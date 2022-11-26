package com.nowcoder.community.entity;

import java.util.Date;

public class Message {
    Integer id;
    Integer fromId;
    Integer toId;
    String conversationId;
    String content;
    Integer status;
    Date createTime;

    public Message(Integer id, Integer fromId, Integer toId, String conversationId, String content, Integer status, Date createTime) {
        this.id = id;
        this.fromId = fromId;
        this.toId = toId;
        this.conversationId = conversationId;
        this.content = content;
        this.status = status;
        this.createTime = createTime;
    }

    public Message() {
    }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", fromId=" + fromId +
                ", toId=" + toId +
                ", conversationId=" + conversationId +
                ", content='" + content + '\'' +
                ", status=" + status +
                ", createTime=" + createTime +
                '}';
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getFromId() {
        return fromId;
    }

    public void setFromId(Integer fromId) {
        this.fromId = fromId;
    }

    public Integer getToId() {
        return toId;
    }

    public void setToId(Integer toId) {
        this.toId = toId;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
