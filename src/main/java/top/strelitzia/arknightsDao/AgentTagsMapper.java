package top.strelitzia.arknightsDao;

import org.apache.ibatis.annotations.Param;
import top.strelitzia.model.AgentTagsInfo;

import java.util.List;

/**
 * @author wangzy
 * @Date 2020/12/14 18:07
 **/
public interface AgentTagsMapper {

    //根据tag列表，循环获取所有tag对应的干员列表
    List<AgentTagsInfo> selectAgentByTag(@Param("tags") List<String> tags);

    //根据tag列表，循环获取所有六星干员
    List<AgentTagsInfo> selectSixAgentByTag(@Param("tags") List<String> tags);

    //获取所有公招干员
    List<AgentTagsInfo> selectAgentAll();

    //获取所有公招干员名称
    List<String> selectAgentNameAll();
}
