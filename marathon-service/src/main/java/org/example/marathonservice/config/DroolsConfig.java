package org.example.marathonservice.config;

import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.api.runtime.KieContainer;
import org.kie.internal.io.ResourceFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//静态配置规则
@Configuration
public class DroolsConfig {
//    private static final KieServices kieServices = KieServices.Factory.get();
//
//    //规则文件的路径,在resources下
//    private static final String RULES_PATH = "rules/health.drl";
//
//    @Bean
//    public KieContainer kieContainer() {
//        KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
//        kieFileSystem.write(ResourceFactory.newClassPathResource(RULES_PATH));
//
//        KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
//        kieBuilder.buildAll();
//
//        KieModule kieModule = kieBuilder.getKieModule();
//        KieContainer kieContainer = kieServices.newKieContainer(kieModule.getReleaseId());
//        return kieContainer;
//    }
}
