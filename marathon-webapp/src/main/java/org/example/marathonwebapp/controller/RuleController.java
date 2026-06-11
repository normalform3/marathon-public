package org.example.marathonwebapp.controller;

import org.example.marathondal.entity.RuleDO;
import org.example.marathonservice.service.RuleService;
import org.example.marathonwebapp.model.WebResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rule")
public class RuleController {
    @Autowired
    private RuleService ruleService;

    //查询全部规则
    @GetMapping("/all")
    public WebResult<List<RuleDO>> getAllRules() {
        List<RuleDO> rules = ruleService.list();
        if (rules != null && !rules.isEmpty()) {
            return WebResult.success(rules);
        } else {
            return WebResult.fail("没有规则");
        }
    }

    //启用规则
    @GetMapping("/enable")
    public WebResult<String> enableRule(Long ruleId) {
        ruleService.enableRule(ruleId);
        return WebResult.success("规则启用成功");
    }

    //新增规则
    @PostMapping("/add")
    public WebResult<String> addRule(@RequestBody RuleDO rule) {
        try {
            // 先验证DRL语法
            ruleService.validateDrlSyntax(rule.getContent());
            // 语法验证通过后再执行添加
            ruleService.add(rule);
            return WebResult.success("规则添加成功");
        } catch (IllegalArgumentException e) {
            return WebResult.fail("DRL语法错误: " + e.getMessage());
        } catch (Exception e) {
            return WebResult.fail("添加规则失败: " + e.getMessage());
        }
    }

    //删除规则
    @GetMapping("/delete")
    public WebResult<String> deleteRule(Long ruleId){
        ruleService.removeById(ruleId);
        return WebResult.success("规则删除成功");
    }
}
