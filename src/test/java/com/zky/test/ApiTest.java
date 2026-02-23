package com.zky.test;

import com.zky.api.IMarketIndexService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author ：zky
 * @description：
 * @date ：2026/2/6 11:38
 */
@Slf4j
@SpringBootTest
public class ApiTest {

    @DubboReference(interfaceClass = IMarketIndexService.class,version = "1.0")
    private IMarketIndexService iMarketIndexService;
    @Test
    public void test(){
        iMarketIndexService.testDubbo();
    }
}
