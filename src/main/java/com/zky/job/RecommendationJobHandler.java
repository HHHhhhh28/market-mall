package com.zky.job;

import com.xxl.job.core.handler.annotation.XxlJob;
import com.zky.algorithm.impl.ContentBasedModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class RecommendationJobHandler {

    @Resource
    private ContentBasedModel contentBasedModel;


    /**
     * 基于内容的推荐算法训练任务
     * Cron: 0 0 * * * ? (每小时执行一次)
     */
    @XxlJob("ContentBasedModelTrainingJob")
    public void ContentBasedModelTrainingJob() {
        log.info("XXL-JOB: 开始执行基于内容的推荐算法训练任务...");
        try {
            contentBasedModel.train();
            log.info("XXL-JOB: 基于内容的推荐算法训练任务执行成功");
        } catch (Exception e) {
            log.error("XXL-JOB: 基于内容的推荐算法训练任务务执行失败", e);
            throw e; // 抛出异常以便XXL-Job捕获失败状态
        }
    }


}
