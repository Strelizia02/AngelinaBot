package top.strelitzia.dao;

import org.apache.ibatis.annotations.Param;
import top.strelitzia.model.BiliCount;

import java.util.List;

/**
 * @author wangzy
 * @Date 2021/1/12 17:13
 **/
public interface BiliMapper {
    //获取所有uid
    List<BiliCount> getBiliCountList();

    //分页获取关注uid
    List<BiliCount> getBiliCountListByPage(@Param("name")String name, @Param("current")Integer current);

    Integer getBiliCountListCount(@Param("name")String name);

    //获取某群的关注列表
    List<BiliCount> getBiliCountListByGroupId(@Param("groupId") Long groupId);

    //获取某群没有关注列表
    List<BiliCount> getNotListenListByGroupId(@Param("groupId") Long groupId, @Param("name") String name);

    //更新uid的动态列表
    Integer updateNewDynamic(BiliCount bili);

    //根据up主昵称获取动态
    BiliCount getOneDynamicByName(String name);

    //某群关注某uid
    Integer insertGroupBiliRel(@Param("groupId") Long groupId, @Param("uid") Long uid);

    //某群取关某uid
    Integer deleteGroupBiliRel(@Param("groupId") Long groupId, @Param("uid") Long uid);

    //查询是否已关注
    Integer selectGroupBiliRel(@Param("groupId") Long groupId, @Param("uid") Long uid);

    //查询改uid是否已监听
    Integer existBiliUid(@Param("uid") Long uid);

    //关注某uid
    Integer insertBiliUid(@Param("uid") Long uid);

    //查询uid被哪些群关注
    List<Long> selectGroupByUid(@Param("uid") Long uid);

    //查询uid没有被哪些群关注
    List<Long> selectGroupByNotListenUid(@Param("uid") Long uid, @Param("groupId") String groupId);

    Integer deleteUid(@Param("uid") Long uid);
}
