package com.zky.job;

import com.xxl.job.core.handler.annotation.XxlJob;
import com.zky.algorithm.impl.ContentBasedModel;
import com.zky.algorithm.impl.UserCFModel;
import com.zky.algorithm.impl.UserProfileModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class RecommendationJobHandler {

    @Resource
    private ContentBasedModel contentBasedModel;
    @Resource
    private UserProfileModel userProfileModel;
    @Resource
    private UserCFModel userCFModel;

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

    /**
     * 基于用户画像的推荐算法训练任务
     * Cron: 0 0 * * * ? (每小时执行一次)
     */
    @XxlJob("UserProfileModelTrainingJob")
    public void UserProfileModelTrainingJob() {
        log.info("XXL-JOB: 开始执行基于用户画像的推荐算法训练任务...");
        try {
            userProfileModel.dynamicUpdateProductUserTags();
            log.info("XXL-JOB: 基于用户画像的推荐算法训练任务执行成功");
        } catch (Exception e) {
            log.error("XXL-JOB: 基于用户画像的推荐算法训练任务执行失败", e);
            throw e; // 抛出异常以便XXL-Job捕获失败状态
        }
    }

    /**
     * 基于用户的协同过滤推荐算法训练任务
     * Cron: 0 0 * * * ? (每小时执行一次)
     */
    @XxlJob("UserCFModelJob")
    public void UserCFModelJob() {
        log.info("XXL-JOB: 开始执行基于用户的协同过滤推荐算法训练任务...");
        try {
            userCFModel.train();
            log.info("XXL-JOB: 基于用户的协同过滤推荐算法训练任务执行成功");
        } catch (Exception e) {
            log.error("XXL-JOB: 基于用户的协同过滤推荐算法训练任务执行失败", e);
            throw e; // 抛出异常以便XXL-Job捕获失败状态
        }
    }
}
