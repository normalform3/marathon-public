package org.example.marathonwebapp.controller;

import org.example.marathonservice.param.OrderParam;
import org.example.marathonservice.service.OrderService;
import org.example.marathonwebapp.model.WebResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private OrderService orderService;

    //创建/查询已存在订单 返回订单号
    @PostMapping("/create")
    public WebResult<Long> createOrder(@RequestBody OrderParam param) {
        Long orderId = orderService.createOrder(param);
        if (orderId == null) {
            return WebResult.fail("只有中签待支付的报名才能创建订单");
        }
        return WebResult.success(orderId);
    }

    //支付成功
    @PostMapping("/paySuccess")
    public WebResult<String> paySuccess(@RequestBody OrderParam param) {
        if(orderService.paySuccess(param)){
            return WebResult.success("支付成功");
        } else {
            return WebResult.fail("支付失败");
        }
    }
}
