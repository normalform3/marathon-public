package org.example.marathonservice.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.marathondal.entity.NoticeDO;
import org.example.marathondal.entity.SubscribeDO;
import org.example.marathondal.mapper.NewsMapper;
import org.example.marathondal.mapper.NoticeMapper;
import org.example.marathondal.mapper.SubscribeMapper;
import org.example.marathonservice.VO.NoticeVO;
import org.example.marathonservice.utils.RedisIdWorker;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class NoticeService extends ServiceImpl<NoticeMapper, NoticeDO> {
    @Autowired
    private RedisIdWorker redisIdWorker;

    @Autowired
    private SubscribeMapper subscribeMapper;

    @Autowired
    private NoticeMapper noticeMapper;

    // 根据用户ID获取通知
    public List<NoticeVO> getNotice(Long userId) {
        // 从 subscribe 表中查询该用户的所有订阅通知记录
        QueryWrapper<SubscribeDO> subscribeQueryWrapper = new QueryWrapper<>();
        subscribeQueryWrapper.eq("user_id", userId);
        List<SubscribeDO> subscribeList = subscribeMapper.selectList(subscribeQueryWrapper);

        List<NoticeVO> noticeVOList = new ArrayList<>();
        for (SubscribeDO subscribe : subscribeList) {
            Long noticeId = subscribe.getNoticeId();
            // 根据通知 ID 从 notice 表中查询通知信息
            NoticeDO notice = noticeMapper.selectById(noticeId);
            if (notice != null) {
                NoticeVO noticeVO = new NoticeVO();
                // 将 NoticeDO 的属性复制到 NoticeVO 中
                BeanUtils.copyProperties(notice, noticeVO);
                noticeVO.setSubscribeId(subscribe.getId());
                noticeVO.setIsRead(subscribe.getIsRead());
                noticeVOList.add(noticeVO);
            }
        }
        // 将通知按创建时间降序排序
        noticeVOList.sort((n1, n2) -> n2.getCreateTime().compareTo(n1.getCreateTime()));
        return noticeVOList;
    }

    // 根据id将通知设为已读
    public void setRead(Long id) {
        UpdateWrapper<SubscribeDO> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id);
        SubscribeDO subscribe = new SubscribeDO();
        subscribe.setIsRead(1); // 设置为已读
        subscribeMapper.update(subscribe, updateWrapper);
    }
}
