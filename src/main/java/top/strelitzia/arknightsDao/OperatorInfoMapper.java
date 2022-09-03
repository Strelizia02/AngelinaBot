package top.strelitzia.arknightsDao;

import org.apache.ibatis.annotations.Param;
import top.strelitzia.model.OperatorBasicInfo;
import top.strelitzia.model.OperatorName;
import top.strelitzia.model.TalentInfo;

import java.util.List;

/**
 * @author wangzy
 * @Date 2021/3/29 17:07
 **/
public interface OperatorInfoMapper {
    //根据各种信息查找对应干员
    List<String> getOperatorNameByInfo(String Info);

    //获取全部干员列表
    List<String> getAllOperator();

    //获取全部干员列表
    List<String> getAllOperatorId();

    //获取全部干员姓名
    List<OperatorName> getAllOperatorIdAndName();

    //查找干员档案
    OperatorBasicInfo getOperatorInfoByName(String name);

    //查找全部画师
    List<String> getAllDrawName();

    //条件模糊查询画师
    List<String> getAllDrawNameLikeStr(String str);

    //查找全部声优
    List<String> getAllInfoName();

    //条件模糊查询声优
    List<String> getAllInfoNameLikeStr(String str);

    //根据生日查找干员
    List<String> getOperatorByBirthday(String birthday);

    //根据ID查找干员名
    String getOperatorNameById(Integer id);

    //根据char_id查找干员id
    Integer getOperatorIdByChar(String charId);

    //根据干员id查找干员立绘
    String selectOperatorPngById(String id);

    //根据干员id查找干员头像
    String selectOperatorAvatarPngById(String id);

    //根据干员名查找头像
    String selectAvatarByName(String name);

    //根据干员名查找干员立绘
    String selectOperatorPngByName(String name);

    //插入干员立绘
    Integer insertOperatorPngById(@Param("id") String id, @Param("base") String base);

    //插入干员头像
    Integer insertOperatorAvatarPngById(@Param("id") String id, @Param("avatar") String avatar);

    //插入技能图标
    Integer insertOperatorSkillPngById(@Param("id") String id, @Param("base") String base);

    //根据干员名查找干员天赋
    List<TalentInfo> getOperatorTalent(String name);

    //根据干员名查找其职业
    Integer selectOperatorClassByName(String name);

    //根据干员编号和语音名查找语音是否存在
    Integer selectOperatorVoiceByCharIdAndName(@Param("charId") String charId, @Param("name") String name);

    //插入一条新的语音记录
    Integer insertOperatorVoice(@Param("charId") String charId, @Param("name") String name, @Param("file") String file);

    //根据干员名查找语音记录
    List<String> selectOperatorVoiceByName( @Param("name") String name);

    //根据干员名和语音名查找语音记录
    String selectOperatorVoiceByNameAndVoice(@Param("name") String name, @Param("voiceName") String voiceName);
}
