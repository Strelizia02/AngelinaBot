package top.strelitzia.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.strelitzia.dao.GroupAdminInfoMapper;
import top.strelitzia.model.GroupAdminInfo;

/**
 * @author strelitzia
 * @Date 2022/05/03 14:38
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
}
