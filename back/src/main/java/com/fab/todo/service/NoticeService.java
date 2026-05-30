package com.fab.todo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fab.todo.entity.Notice;
import com.fab.todo.mapper.NoticeMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NoticeService {

    private final NoticeMapper noticeMapper;

    public NoticeService(NoticeMapper noticeMapper) {
        this.noticeMapper = noticeMapper;
    }

    public List<Notice> getAll() {
        return noticeMapper.selectList(new LambdaQueryWrapper<Notice>().orderByDesc(Notice::getCreatedAt));
    }

    public List<Notice> getLatestNotices() {
        return noticeMapper.selectList(
                new LambdaQueryWrapper<Notice>()
                        .orderByDesc(Notice::getCreatedAt)
                        .last("LIMIT 5")
        );
    }

    public Notice create(String title, String content) {
        if (title == null || title.isBlank() || content == null || content.isBlank()) {
            throw new RuntimeException("公告标题和内容不能为空");
        }

        Notice notice = new Notice();
        notice.setTitle(title.trim());
        notice.setContent(content.trim());
        noticeMapper.insert(notice);
        return notice;
    }

    public Notice update(Long id, String title, String content) {
        Notice notice = noticeMapper.selectById(id);
        if (notice == null) {
            throw new RuntimeException("公告不存在");
        }
        if (title == null || title.isBlank() || content == null || content.isBlank()) {
            throw new RuntimeException("公告标题和内容不能为空");
        }

        notice.setTitle(title.trim());
        notice.setContent(content.trim());
        noticeMapper.updateById(notice);
        return notice;
    }

    public void delete(Long id) {
        noticeMapper.deleteById(id);
    }

    public boolean hasUnreadNotices(LocalDateTime lastSeenAt) {
        if (lastSeenAt == null) {
            return noticeMapper.selectCount(null) > 0;
        }
        return noticeMapper.selectCount(new LambdaQueryWrapper<Notice>().gt(Notice::getCreatedAt, lastSeenAt)) > 0;
    }
}
