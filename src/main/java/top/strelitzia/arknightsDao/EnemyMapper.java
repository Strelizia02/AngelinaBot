package top.strelitzia.arknightsDao;

import top.strelitzia.model.EnemyInfo;

import java.util.List;

/**
 * @author wangzy
 * @Date 2021/1/17 16:22
 **/
public interface EnemyMapper {
    //获取单个敌人信息
    List<EnemyInfo> selectEnemyByName(String name);

    //关键字模糊搜素敌人列表
    List<String> selectEnemyListByName(String name);

    List<EnemyInfo> selectEnemyLikeName(String name);

    //获取全部敌人Id
    List<String> selectAllEnemyId();
}
