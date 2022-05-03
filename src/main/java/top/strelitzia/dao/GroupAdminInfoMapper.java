package top.strelitzia.dao;

import top.strelitzia.model.GroupAdminInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author strelitzia
 * @Date 2021/3/17 17:52
 **/
public interface GroupAdminInfoMapper {

    GroupAdminInfo getGroupAdminNum(Long groupId);

    List<GroupAdminInfo> getAllGroupAdmin(Integer current);

    Integer getAllGroupAdminCount();

    Integer insertGroupId(Long groupId);

    Integer updatePictureAdmin(@Param("groupId") Long groupId, @Param("picture") Integer picture);

    Integer updateGroupAdmin(GroupAdminInfo groupAdminInfo);

    Integer existGroupId(@Param("groupId")Long groupId);

}
