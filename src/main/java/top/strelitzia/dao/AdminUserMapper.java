package top.strelitzia.dao;

import top.strelitzia.model.AdminUserInfo;

import java.util.List;

/**
 * @author wangzy
 * @Date 2020/12/26 0:38
 **/
public interface AdminUserMapper {

    //查询所有的管理员用户，以及对应的权限信息
    List<AdminUserInfo> selectAllAdmin();

    //查询所有的管理员用户，以及对应的权限信息
    List<AdminUserInfo> selectAllAdminByPage(Integer current);

    Integer selectAllAdminCount();

    //修改管理员的权限信息
    Integer updateUserAdmin(AdminUserInfo adminInfo);

    //添加一行
    Integer insertUserAdmin(AdminUserInfo adminInfo);

    //根据qq删除
    Integer deleteUserAdmin(Long qq);

    //清空所有管理员
    Integer truncateUserAdmin();
}
