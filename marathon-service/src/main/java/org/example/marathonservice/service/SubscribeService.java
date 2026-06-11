package org.example.marathonservice.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.marathondal.entity.SubscribeDO;
import org.example.marathondal.mapper.SubscribeMapper;
import org.springframework.stereotype.Service;

@Service
public class SubscribeService extends ServiceImpl<SubscribeMapper, SubscribeDO> {
}
