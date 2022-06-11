package top.strelitzia.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.strelitzia.dao.AdminUserMapper;
import top.strelitzia.dao.AgentMapper;
import top.strelitzia.dao.UserFoundMapper;
import top.strelitzia.model.AdminUserInfo;
import top.strelitzia.model.UserFoundInfo;
import top.strelitzia.service.AgentService;
import top.strelitzia.service.GroupAdminInfoService;
import top.strelitzia.util.AdminUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@RequestMapping("angelina")
@RestController
@Slf4j
public class AngelinaController {

    @Autowired
    private AgentService agentService;

    @Autowired
    private UserFoundMapper userFoundMapper;

    @Autowired
    private GroupAdminInfoService groupAdminInfoService;

    @Autowired
    private AdminUserMapper adminUserMapper;

    @Autowired
    private AgentMapper agentMapper;

    @GetMapping(value = "found", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] webFoundAgentService(@RequestParam String pool, @RequestParam Long group, @RequestParam Long qq, @RequestParam String name) throws IOException {
        //如果没输入卡池名或者卡池不存在
        if (agentMapper.selectPoolIsExit(pool).size() == 0) {
            pool = "常规";
        }
        UserFoundInfo userFoundInfo = userFoundMapper.selectUserFoundByQQ(qq);
        Integer limit = groupAdminInfoService.getGroupFoundAdmin(group);
        if (userFoundInfo == null) {
            userFoundInfo = new UserFoundInfo();
            userFoundInfo.setQq(qq);
            userFoundInfo.setFoundCount(0);
            userFoundInfo.setTodayCount(0);
        }
        //去数据库中查询这个人的垫刀数
        Integer sum = userFoundInfo.getFoundCount();
        //今日抽卡数
        Integer today = userFoundInfo.getTodayCount();
        List<AdminUserInfo> admins = adminUserMapper.selectAllAdmin();
        boolean b = AdminUtil.getFoundAdmin(qq, admins);

        if (today < limit || b) {
            String s = agentService.FoundAgentByNum(10, pool, qq, sum, name, group);
            s = s.replace(" ", "");
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(agentService.drawPicByFound(s), "png", out);
            return out.toByteArray();
        } else {
            return null;
        }
    }
}
