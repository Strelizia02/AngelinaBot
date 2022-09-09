package top.strelitzia.dao;

import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BirthdayRemindMapper {

    //增加指定的群组订阅的干员生日提醒
    Integer insertBirthdayRemind(@Param("groupId") Long groupId, @Param("name") String name);

    //根据群组ID查询订阅的干员名字
    List<String> selectNameByGroupId(Long groupId);

    //根据干员名字查询订阅的群组ID
    List<Long> selectGroupIdByName(String name);

    //删除指定的群组订阅的干员生日提醒
    Integer deleteBirthdayRemind(@Param("groupId") Long groupId, @Param("name") String name);

}
