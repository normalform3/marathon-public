package org.example.marathonwebapp.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import org.example.marathondal.entity.UserDO;
import org.example.marathonservice.VO.UserVO;
import org.example.marathonservice.param.UserParam;
import org.example.marathonservice.service.UserService;
import org.example.marathonwebapp.model.WebResult;
import org.example.marathonwebapp.utils.Jwt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.server.Session;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    //登录
    @PostMapping("/login")
    public WebResult<UserVO> login(@RequestParam("account") String account,@RequestParam("password") String password) {
        UserVO userVO = userService.login(account, password);
        if (userVO == null) {
            return WebResult.fail("登录失败");
        }
        String token = Jwt.createToken(account, userVO.getId(), userVO.getType());
        userVO.setToken(token);

        return WebResult.success(userVO);
    }

    //注册
    @PostMapping("/signup")
    public WebResult<String> signup(@RequestBody UserDO userDO) {
        if (userService.signup(userDO)) {
            return WebResult.success("注册成功");
        }
        return WebResult.fail("注册失败");
    }

    //获取用户详细信息
    @GetMapping("/detail")
    public WebResult<UserDO> detail(@RequestParam("id") Long id) {
        UserDO userDO = userService.getById(id);
        if (userDO == null) {
            return WebResult.fail("用户不存在");
        }
        return WebResult.success(userDO);
    }

    //发送手机验证码
    @PostMapping("/sendCode")
    public WebResult<String> sendCode(@RequestParam("phone") String phone) {
        if (userService.sendCode(phone)) {
            return WebResult.success("发送成功");
        }
        return WebResult.fail("发送失败");
    }

    //修改个人信息
    @PostMapping("/update")
    public WebResult<String> update(@RequestBody UserParam userParam) {
        Integer result = userService.updateUserInfo(userParam);
        if (result == 1) {
            return WebResult.success("修改成功");
        } else if (result == 2) {
            return WebResult.fail("用户不存在");
        } else if (result == 3) {
            return WebResult.fail("身份证号错误");
        }
        return WebResult.fail("修改失败");
    }
}
