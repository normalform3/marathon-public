package org.example.marathonservice.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.marathondal.entity.OrderDO;
import org.example.marathondal.entity.RaceDO;
import org.example.marathondal.entity.RegistrationDO;
import org.example.marathondal.mapper.OrderMapper;
import org.example.marathonservice.enums.RegistrationStatus;
import org.example.marathonservice.param.OrderParam;
import org.example.marathonservice.utils.RedisIdWorker;
import org.example.marathoncommon.constants.RedisKeyConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class OrderService extends ServiceImpl<OrderMapper, OrderDO> {
    @Autowired
    private RedisIdWorker redisIdWorker;
    @Autowired
    private RaceService raceService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RegistrationService registrationService;

    // 创建订单
    public Long createOrder(OrderParam param) {
        QueryWrapper<RegistrationDO> queryRegistration = new QueryWrapper<>();
        queryRegistration.eq("user_id", param.getUserId())
                .eq("race_id", param.getRaceId())
                .eq("status", RegistrationStatus.LOTTERY_WON_PENDING_PAY.getCode());
        if (registrationService.count(queryRegistration) <= 0) {
            return null;
        }

        //查询订单是否已创建
        QueryWrapper<OrderDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",param.getUserId())
                    .eq("race_id",param.getRaceId())
                    .eq("pay_status",0); // 查询待支付状态的订单
        OrderDO order = this.getOne(queryWrapper);

        if(order == null){
            //订单不存在，创建新的订单
            order = new OrderDO();
            order.setId(redisIdWorker.nextId("order"));
            order.setUserId(param.getUserId());
            order.setRaceId(param.getRaceId());
            order.setPayStatus(0); // 设置为待支付状态
            order.setAmount(raceService.getRaceById(param.getRaceId()).getFee());
            order.setCreateTime(LocalDateTime.now());
            this.save(order);
            return order.getId(); //订单创建成功,返回订单ID
        }
        return order.getId(); //订单已存在,返回订单ID
    }

    //支付成功
    public boolean paySuccess(OrderParam param) {
        OrderDO order = this.getById(param.getId());
        if (order != null && order.getPayStatus() == 0) {
            order.setPayStatus(1); // 设置为已支付状态
            order.setPayTime(LocalDateTime.now());

            //生成参赛号
            //赛事唯一报名号计数器
            RaceDO race = raceService.getRaceById(param.getRaceId());
            Long raceNumber = stringRedisTemplate.opsForValue().increment(RedisKeyConstant.RACE_NUMBER_KEY + race.getRaceId());
            //将参赛号补全到7位
            raceNumber = Long.valueOf(String.format("%07d", raceNumber));

            //修改报名信息
            if (!registrationService.paySuccess(param.getUserId(), param.getRaceId(), raceNumber.intValue())) {
                return false;
            }
            return this.updateById(order);
        }
        return false;
    }


    // 更新订单状态
    public boolean updateOrderStatus(Long orderId, Integer status) {
        OrderDO order = this.getById(orderId);
        if (order != null) {
            order.setPayStatus(status);
            if (status == 2) {
                registrationService.markPayTimeout(order.getUserId(), order.getRaceId());
            }
            return this.updateById(order);
        }
        return false;
    }

    // 查询订单详情
    public OrderDO getOrderDetails(Long orderId) {
        return this.getById(orderId);
    }
}
