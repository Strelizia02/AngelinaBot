package top.strelitzia.dao;

import org.apache.ibatis.annotations.Param;
import top.strelitzia.model.SkillDesc;

import java.util.List;

/**
 * @author wangzy
 * @Date 2021/4/2 17:17
 **/
public interface SkillDescMapper {

    List<SkillDesc> selectSkillDescByNameAndLevel(@Param("name") String name, @Param("level") Integer level);

}
