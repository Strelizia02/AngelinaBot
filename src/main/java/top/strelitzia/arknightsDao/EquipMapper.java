package top.strelitzia.arknightsDao;

import org.apache.ibatis.annotations.Param;
import top.strelitzia.model.EquipBuff;
import top.strelitzia.model.EquipInfo;
import top.strelitzia.model.MaterialInfo;

import java.util.List;

/**
 * @author wangzy
 * @Date 2021/3/29 17:07
 **/
public interface EquipMapper {

    Integer insertEquipInfo(EquipInfo equipInfo);

    Integer insertEquipCost(@Param("equipId") String equipId, @Param("materialId")String materialId, @Param("useNumber")Integer useNumber, @Param("level")Integer level);

    Integer insertEquipBuff(@Param("equipId")String equipId, @Param("buffKey")String buffKey, @Param("value")Double value, @Param("level")Integer level);

    Integer insertEquipMission(@Param("equipId")String equipId, @Param("missionId")String missionId, @Param("desc")String desc);

    List<EquipInfo> selectEquipByName(String name);

    List<EquipBuff> selectEquipBuffById(@Param("equipId") String equipId, @Param("level")Integer level);

    List<MaterialInfo> selectEquipCostById(@Param("equipId") String equipId, @Param("level")Integer level);

    List<String> selectEquipMissionById(String equipId);

    List<String> selectAllEquipId();

}
