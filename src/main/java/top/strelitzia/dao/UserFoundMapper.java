package top.strelitzia.dao;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import top.angelinaBot.model.Count;
import top.strelitzia.model.UserFoundInfo;

import java.util.List;

/**
 * @author wangzy
 * @Date 2020/12/7 13:50
 **/
public interface UserFoundMapper {

    //更新某用户今日抽卡数，同时更新这个用户所属群号以及群昵称
    //一个小bug，用户是唯一的，有可能同时存在于两个群中，每日日报只能获取最后一次抽卡/涩图的群号
    Integer updateUserFoundByQQ(@Param("qq") String qq, @Param("name") String name, @Param("groupId") String groupId, @Param("foundCount") Integer foundCount);

    //抽到六星，该用户六星数，每日六星数+1
    Integer updateSixByQq(@Param("qq") String qq);

    //抽到五星，该用户每日五星数+1
    Integer updateFiveByQq(@Param("qq") String qq);

    //查询某人的今日抽卡数，垫刀数
    UserFoundInfo selectUserFoundByQQ(String qq);

    //清空每日抽卡次数
    Integer cleanTodayCount();

    //抽卡数
    Integer selectTodaySearchByQQ(String qq);

    List<String> selectCakeGroups(@Param("uid") Long uid);

    Integer deleteNotBili();

    @Select("select count(qq) as count from a_user_found")
    Long selectQqCount();

    @Select("select count(group_id) as count from a_group_admin")
    Long selectGroupCount();
}
