package top.strelitzia.arknightsDao;

import org.apache.ibatis.annotations.Param;
import top.strelitzia.model.SkillDesc;
import top.strelitzia.model.SkillInfo;

import java.util.List;

/**
 * @author wangzy
 * @Date 2021/4/2 17:17
 **/
public interface SkillDescMapper {

    List<SkillDesc> selectSkillDescByNameAndLevel(@Param("name") String name, @Param("level") Integer level);

    List<SkillInfo> selectAllSkillPng();

    String selectSkillPngByNameAndIndex(@Param("name") String name, @Param("index") Integer index);

}
