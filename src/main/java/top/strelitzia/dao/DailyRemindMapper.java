package top.strelitzia.dao;

import org.apache.ibatis.annotations.Param;
import top.strelitzia.model.DailyRemindInfo;

import java.util.List;

public interface DailyRemindMapper {
    //获取本群所有提醒
    List<DailyRemindInfo> getDailyRemindByGroupId(@Param("groupId") Long groupId);

    //某群订阅某提醒
    Integer insertDailyRemind(DailyRemindInfo dailyRemindInfo);

    //更新天数
    Integer updateDayLeft(DailyRemindInfo dailyRemindInfo);

    //某群取消某提醒
    Integer deleteDailyRemind(@Param("groupId") Long groupId, @Param("remindContent") String remindContent,@Param("userId") Long userId);
}
