package top.strelitzia.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.angelinaBot.annotation.AngelinaGroup;
import top.angelinaBot.model.MessageInfo;
import top.angelinaBot.model.ReplayInfo;
import top.strelitzia.dao.NickNameMapper;
import top.strelitzia.arknightsDao.SkinInfoMapper;
import top.strelitzia.model.SkinInfo;

import java.io.File;
import java.util.List;

/**
 * @author strelitzia
 * @Date 2022/05/03 14:38
 **/
@Service
public class SkinInfoService {

    @Autowired
    private SkinInfoMapper skinInfoMapper;

    @Autowired
    private NickNameMapper nickNameMapper;


//    @AngelinaGroup(keyWords = {"时装", "时装查询", "皮肤", "皮肤查询"}, description = "查询干员的时装信息")
    public ReplayInfo getOperatorSkinByInfo(MessageInfo messageInfo) {
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
        if (messageInfo.getArgs().size() > 1) {
            String info = messageInfo.getArgs().get(1);
            String realName = nickNameMapper.selectNameByNickName(info);
            if (realName != null && !realName.equals("")) {
                info = realName;
            }
            List<SkinInfo> skinInfos = skinInfoMapper.selectSkinByInfo(info);
            if (skinInfos != null && skinInfos.size() > 0) {
                if (skinInfos.size() <= 5) {
                    for (SkinInfo skinInfo : skinInfos) {
                        String result = skinInfo.getOperatorName() + " " + skinInfo.getSkinName() +
                                "\n画师：" + skinInfo.getDrawerName() + " " + skinInfo.getSkinGroupName() + "系列\n" +
                                skinInfo.getDialog();
                        replayInfo.setReplayMessage(result);
                        replayInfo.setReplayImg(new File(skinInfo.getSkinBase64()));
                    }
                } else {
                    StringBuilder result = new StringBuilder("当前搜索结果过多，如需查看皮肤立绘请缩小搜索范围");
                    for (SkinInfo skinInfo : skinInfos) {
                        result.append("\n干员名：").append(skinInfo.getOperatorName()).append(" 皮肤名：").append(skinInfo.getSkinName()).append("\n画师：").append(skinInfo.getDrawerName()).append(" ").append(skinInfo.getSkinGroupName()).append("系列\n").append(skinInfo.getDialog()).append("\n");
                    }
                    replayInfo.setReplayMessage(result.toString());
                }
            }
        } else {
            replayInfo.setReplayMessage("请输入时装的信息");
        }
        return replayInfo;
    }
}
