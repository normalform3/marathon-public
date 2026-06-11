package org.example.marathonservice.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.marathondal.entity.RuleDO;
import org.example.marathondal.mapper.RuleMapper;
import org.example.marathonservice.utils.RedisIdWorker;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RuleService extends ServiceImpl<RuleMapper, RuleDO> {
    @Autowired
    private RuleMapper ruleMapper;

    @Autowired
    private RedisIdWorker redisIdWorker;

    //从DB加载当前启用规则
    public KieSession loadEnabledRule() {
        //获取 Drools 的核心服务对象 KieServices，是操作 Drools 所有组件的入口
        KieServices kieServices = KieServices.Factory.get();
        //创建一个“虚拟的规则文件系统”(临时的内存空间
        KieFileSystem kieFileSystem = kieServices.newKieFileSystem();

        //从数据库中查询启用的规则
        RuleDO rule = ruleMapper.selectOne(
                new LambdaQueryWrapper<RuleDO>().eq(RuleDO::getStatus, 1)
        );

        if (rule == null) {
            throw new RuntimeException("没有启用的规则！");
        }
        //将数据库中拿到的规则内容写入到内存文件系统中，虚拟成一个叫 health.drl 的规则文件
        kieFileSystem.write("src/main/resources/rules/health.drl", rule.getContent());

        //创建 Drools 的规则构建器，然后对这个内存规则系统进行编译（build）
        KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
        kieBuilder.buildAll();
        if (kieBuilder.getResults().hasMessages(Message.Level.ERROR)) {
            throw new RuntimeException("规则构建失败: " + kieBuilder.getResults());
        }
        //构建规则容器，类似于把刚刚构建的 .drl 文件打成一个临时 jar 包，放进一个可用的容器中。
        KieContainer kieContainer = kieServices.newKieContainer(kieServices.getRepository().getDefaultReleaseId());

        //从容器中创建会话，来执行规则
        return kieContainer.newKieSession();
    }

    //启用某条规则
    @Transactional
    public void enableRule(Long ruleId) {
        // 取消所有规则的启用状态
        ruleMapper.update(null,
                new UpdateWrapper<RuleDO>().set("status", 0)
        );
        // 设置选中的规则为启用
        ruleMapper.update(null,
                new UpdateWrapper<RuleDO>()
                        .eq("id", ruleId)
                        .set("status", 1)
        );
    }

    //新增规则
    public void add(RuleDO rule) {
        rule.setId(redisIdWorker.nextId("rule"));
        rule.setStatus(0); //默认不启用
        rule.setCreateTime(LocalDateTime.now());
        ruleMapper.insert(rule);
    }

    //语法验证
    public void validateDrlSyntax(String drlContent) {
        if (drlContent == null || drlContent.trim().isEmpty()) {
            throw new IllegalArgumentException("DRL内容不能为空");
        }

        // 创建KieServices实例
        KieServices kieServices = KieServices.Factory.get();
        KieFileSystem kfs = kieServices.newKieFileSystem();

        // 创建临时DRL文件
        String drlFileName = "temp-rule-" + UUID.randomUUID() + ".drl";
        kfs.write("src/main/resources/" + drlFileName, drlContent);

        // 构建KieBuilder进行验证
        KieBuilder kieBuilder = kieServices.newKieBuilder(kfs);
        kieBuilder.buildAll();

        // 检查构建过程中是否有错误
        if (kieBuilder.getResults().hasMessages(Message.Level.ERROR)) {
            StringBuilder errorBuilder = new StringBuilder("DRL语法错误:\n");

            kieBuilder.getResults().getMessages(Message.Level.ERROR)
                    .forEach(message -> {
                        // 提取行号和列号
                        int line = message.getLine();
                        int column = message.getColumn();
                        String text = message.getText();

                        // 构建更友好的错误信息
                        if (line > 0 && column > 0) {
                            errorBuilder.append(String.format("第 %d 行, 第 %d 列: %s\n", line, column, text));
                        } else if (line > 0) {
                            errorBuilder.append(String.format("第 %d 行: %s\n", line, text));
                        } else {
                            errorBuilder.append(String.format("位置未知: %s\n", text));
                        }
                    });

            throw new IllegalArgumentException(errorBuilder.toString());
        }
    }

}
