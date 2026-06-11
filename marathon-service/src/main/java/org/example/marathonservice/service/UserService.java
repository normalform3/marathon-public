package org.example.marathonservice.service;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.marathondal.entity.RaceDO;
import org.example.marathondal.entity.UserDO;
import org.example.marathondal.mapper.RaceMapper;
import org.example.marathondal.mapper.UserMapper;
import org.example.marathonservice.VO.UserVO;
import org.example.marathonservice.param.UserParam;
import org.example.marathonservice.utils.RedisIdWorker;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
public class UserService extends ServiceImpl<UserMapper, UserDO> {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RaceMapper raceMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedisIdWorker redisIdWorker;

    // 登录
    public UserVO login(String account, String password) {
        // 根据账户名查询用户
        QueryWrapper<UserDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("account", account);
        UserDO userDO = userMapper.selectOne(queryWrapper);

        // 检查用户是否存在
        if (Objects.isNull(userDO)) {
            return null;
        }
        // 验证密码
        if (!Objects.equals(userDO.getPassword(), password)) {
            return null;
        }
        // 将 UserDO 转换为 UserVO
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(userDO, userVO);

        //如果是管理员，返回赛事id
        if (userDO.getType() == 2) {
            //根据用户id查询赛事id
            QueryWrapper<RaceDO> queryWrapper2 = new QueryWrapper<>();
            queryWrapper2.eq("organizer_id", userDO.getId());
            RaceDO raceDO = raceMapper.selectOne(queryWrapper2);
            if (raceDO != null) {
                userVO.setRaceId(raceDO.getId());
            }
        }
        return userVO;
    }

    // 注册
    public boolean signup(UserDO userDO) {
        userDO.setId(redisIdWorker.nextId("user"));
        userDO.setType(1);
        userDO.setHealthStatus(0);
        return userMapper.insert(userDO) > 0;
    }

    // 发送手机验证码
    public boolean sendCode(String phone) {
        // 1.生成验证码
        String code = String.valueOf((int) ((Math.random() * 9 + 1) * 100000));

        // 2.保存验证码到redis 5分钟
        stringRedisTemplate.opsForValue().set("phone:" + phone, code, 5, TimeUnit.MINUTES);

        // 3.发送验证码到手机

        return false;

    }

    // 修改个人信息
    public Integer updateUserInfo(UserParam userParam) {
        // 1.根据用户id查询用户
        QueryWrapper<UserDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", userParam.getUserId());
        UserDO user = userMapper.selectOne(queryWrapper);

        // 2.检查用户是否存在
        if (Objects.isNull(user)) {
            return 2; // 用户不存在
        }

        // 3.校验身份证号是否正确
        if (!user.getIdentificationNumber().equals(userParam.getIdentificationNumber())) {
            return 3; // 身份证号不正确
        }

        // 4.更新用户信息
        user.setPhone(userParam.getPhone());
        user.setEmergencyPhone(userParam.getEmergencyPhone());
        UpdateWrapper<UserDO> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", userParam.getUserId());
        userMapper.update(user, updateWrapper);
        return 1; // 更新成功
    }
}
