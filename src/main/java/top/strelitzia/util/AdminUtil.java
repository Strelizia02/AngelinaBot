package top.strelitzia.util;

import top.strelitzia.model.AdminUserInfo;

import java.util.List;

/**
 * @author strelitzia
 * @Date 2022/05/03 14:38
 **/
public class AdminUtil {

    /**
     * 返回用户无限抽卡权限
     * @param qq     qqMD5加密字符串
     * @param admins 权限列表
     * @return
     */
    public static boolean getFoundAdmin(Long qq, List<AdminUserInfo> admins) {
        for (AdminUserInfo admin : admins) {
            if (admin.getQq().equals(qq) && admin.getFound() == 1) {
                return true;
            }
        }
        return false;
    }

    /**
     * 返回用户有无爆率拉满权限
     * @param qq     qqMD5加密字符串
     * @param admins 权限列表
     * @return
     */
    public static boolean getSixAdmin(Long qq, List<AdminUserInfo> admins) {
        for (AdminUserInfo admin : admins) {
            if (admin.getQq().equals(qq) && admin.getSix() == 1) {
                return true;
            }
        }
        return false;
    }

    /**
     * 返回用户有无sql权限
     * @param qq     qqMD5加密字符串
     * @param admins 权限列表
     * @return
     */
    public static boolean getSqlAdmin(Long qq, List<AdminUserInfo> admins) {
        for (AdminUserInfo admin : admins) {
            if (admin.getQq().equals(qq) && admin.getSql() == 1) {
                return true;
            }
        }
        return false;
    }
}
