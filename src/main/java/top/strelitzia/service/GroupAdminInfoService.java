package top.strelitzia.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.strelitzia.dao.GroupAdminInfoMapper;
import top.strelitzia.model.GroupAdminInfo;

/**
 * @author wangzy
 * @Date 2021/3/17 18:05
 **/
@Service
public class GroupAdminInfoService {

    @Autowired
    private GroupAdminInfoMapper groupAdminInfoMapper;

    public Integer getGroupFoundAdmin(Long groupId) {
        GroupAdminInfo groupAdminNum = groupAdminInfoMapper.getGroupAdminNum(groupId);
        if (groupAdminNum == null) {
            groupAdminInfoMapper.insertGroupId(groupId);
            groupAdminNum = groupAdminInfoMapper.getGroupAdminNum(groupId);
        }
        return groupAdminNum.getFound();
    }

    public Integer getGroupPictureAdmin(Long groupId) {
        GroupAdminInfo groupAdminNum = groupAdminInfoMapper.getGroupAdminNum(groupId);
        if (groupAdminNum == null) {
            groupAdminInfoMapper.insertGroupId(groupId);
            groupAdminNum = groupAdminInfoMapper.getGroupAdminNum(groupId);
        }
        return groupAdminNum.getPicture();
    }
}
