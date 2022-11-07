package top.strelitzia.dao;

import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface DailyRemindMapper {
    //获取本群所有提醒
    List<String> getDailyRemindByGroupId(@Param("groupId") Long groupId);

    //某群订阅某提醒
    Integer insertDailyRemind(@Param("groupId") Long groupId, @Param("remindContent") String remindContent);

    //某群取消某提醒
    Integer deleteDailyRemind(@Param("groupId") Long groupId, @Param("remindContent") String remindContent);
}
