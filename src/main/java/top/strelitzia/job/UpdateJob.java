package top.strelitzia.job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import top.strelitzia.service.UpdateDataService;

/**
 * @author wangzy
 * @Date 2020/12/8 15:53
 **/

@Component
@Slf4j
public class UpdateJob {

    @Autowired
    private UpdateDataService updateDataService;


    //每天凌晨四点重置抽卡次数
    @Scheduled(cron = "${scheduled.updateJob}")
    @Async
    public void cleanDayCountJob() {
        updateDataService.downloadDataFile(false);
    }
}
