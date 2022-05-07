package top.strelitzia.arknightsDao;

import org.apache.ibatis.annotations.Param;
import top.strelitzia.model.MaterialInfo;
import top.strelitzia.model.OperatorData;

import java.util.List;

/**
 * @author wangzy
 * @Date 2020/12/14 11:12
 **/
public interface OperatorEvolveMapper {

    //根据干员名称和精英化等级获取对应的消耗材料列表
    List<MaterialInfo> selectOperatorEvolveByName(@Param("agent") String agent, @Param("level") Integer level);

    //根据干员名获取干员满级面板
    OperatorData selectOperatorData(String name);
}
