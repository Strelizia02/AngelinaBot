package top.strelitzia.service;

import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.contact.MemberPermission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.angelinaBot.annotation.AngelinaGroup;
import top.angelinaBot.container.AngelinaEventSource;
import top.angelinaBot.container.AngelinaListener;
import top.angelinaBot.model.MessageInfo;
import top.angelinaBot.model.ReplayInfo;
import top.angelinaBot.model.TextLine;
import top.angelinaBot.util.SendMessageUtil;
import top.strelitzia.arknightsDao.OperatorInfoMapper;
import top.strelitzia.arknightsDao.SkillDescMapper;
import top.strelitzia.dao.AdminUserMapper;
import top.strelitzia.arknightsDao.OperatorInfoMapper;
import top.strelitzia.dao.NickNameMapper;
import top.strelitzia.model.GuessOperatorInfo;
import top.strelitzia.model.HintsInfo;
import top.strelitzia.model.HintsType;
import top.strelitzia.model.OperatorBasicInfo;
import top.strelitzia.util.AdminUtil;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.*;

@Service
@Slf4j
public class GuessOperator {

    @Autowired
    SendMessageUtil sendMessageUtil;

    @Autowired
    SkillDescMapper skillDescMapper;

    @Autowired
    OperatorInfoMapper operatorInfoMapper;

    @Autowired
    AdminUserMapper adminUserMapper;

    @Autowired
    NickNameMapper nickNameMapper;


    private static final Set<Long> groupList = new HashSet<>();

    @AngelinaGroup(keyWords = {"猜干员"})
    public ReplayInfo guessOperator(MessageInfo messageInfo) {
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
        boolean sqlAdmin = AdminUtil.getSqlAdmin(messageInfo.getQq(), adminUserMapper.selectAllAdmin());
        if (groupList.contains(messageInfo.getGroupId())) {
            replayInfo.setReplayMessage("本群正在进行猜干员，请查看消息记录");
            return replayInfo;
        } else if (messageInfo.getUserAdmin() == MemberPermission.MEMBER && !sqlAdmin) {
            replayInfo.setReplayMessage("仅有本群群主和管理员有权限开启猜干员");
            return replayInfo;
        } else {
            //添加群组防止重复猜
            groupList.add(messageInfo.getGroupId());
            //默认是十道题
            int num = 10;
            if (messageInfo.getArgs().size() > 1) {
                num = Integer.parseInt(messageInfo.getArgs().get(1));
                if (num > 100 || num < 1) {
                    replayInfo.setReplayMessage("请输入1-100之间的数字");
                    return replayInfo;
                }
            }
            replayInfo.setReplayMessage("本群猜干员正式开始，请听题");
            sendMessageUtil.sendGroupMsg(replayInfo);
            replayInfo.setReplayMessage(null);
            List<String> allOperator = operatorInfoMapper.getAllOperator();
            List<String> list = new ArrayList<>(num);
            //随机抽取干员名
            for (int i = 0; i < num; i++) {
                int index = new Random().nextInt(allOperator.size());
                list.add(allOperator.get(index));
                allOperator.remove(index);
            }
            allOperator.addAll(list);

            Map<String, Integer> score = new HashMap<>();
            //记录都提示了哪些信息
            Map<HintsType, HintsInfo> hintsList = new HashMap<>();
            //开始循环问
            for (int i = 0; i < num; i++) {
                String name = list.get(i);
                log.info(name);

                boolean result = false;
                GuessOperatorInfo title = getTitle(name, i, hintsList, 3);
                replayInfo.setReplayImg(title.getTextLine().drawImage());
                if (title.getVoice() != null) {
                    replayInfo.setMp3(title.getVoice());
                } else {
                    replayInfo.setMp3((File) null);
                }
                sendMessageUtil.sendGroupMsg(replayInfo);
                replayInfo.getReplayImg().clear();
                int j = 0;
                //仅有五次回答机会，超过五次就寄了，下一题
                while (j < 5) {
                    //等待一个“洁哥回答”
                    AngelinaListener angelinaListener = new AngelinaListener() {
                        @Override
                        public boolean callback(MessageInfo message) {
                                String name = message.getText();
                                String realName = nickNameMapper.selectNameByNickName(name);
                                if (realName != null && !realName.equals("")) {
                                    name = realName;
                                }
                                return message.getGroupId().equals(messageInfo.getGroupId()) &&
                                        (allOperator.contains(name) || name.equals("提示"));
                        }
                    };

                    angelinaListener.setGroupId(messageInfo.getGroupId());
                    MessageInfo recall = AngelinaEventSource.waiter(angelinaListener).getMessageInfo();
                    if (recall == null) {
                        groupList.remove(messageInfo.getGroupId());
                        replayInfo.setReplayMessage("本轮猜干员已超时终止。");
                        replayInfo.setMp3((File) null);
                        return replayInfo;
                    }

                    String replayName = recall.getText();
                    String realName = nickNameMapper.selectNameByNickName(replayName);
                    if (realName != null && !realName.equals(""))
                        replayName = realName;

                    if (name.equals(replayName)) {
                        //答对了，直接下一题
                        replayInfo.setReplayMessage(recall.getName() + " 回答正确，答案是 " + name + " ,下一题");
                        replayInfo.setReplayImg(new File(operatorInfoMapper.selectAvatarByName(name)));
                        replayInfo.setMp3((File) null);
                        sendMessageUtil.sendGroupMsg(replayInfo);
                        //把消息内容清掉，后续复用
                        replayInfo.setReplayMessage(null);
                        replayInfo.getReplayImg().clear();
                        hintsList.clear();
                        //记录答对的人
                        if (score.containsKey(recall.getName())) {
                            score.put(recall.getName(), score.get(recall.getName()) + 1);
                        } else {
                            score.put(recall.getName(), 1);
                        }
                        result = true;
                        break;
                    } else {
                        //答错了
                        replayInfo.setReplayMessage("回答错误");
                        GuessOperatorInfo title1 = getTitle(name, i, hintsList, 1);
                        replayInfo.setReplayImg(title1.getTextLine().drawImage());
                        if (title1.getVoice() != null) {
                            replayInfo.setMp3(title1.getVoice());
                        } else {
                            replayInfo.setMp3((File) null);
                        }
                        sendMessageUtil.sendGroupMsg(replayInfo);
                        replayInfo.getReplayImg().clear();
                        replayInfo.setReplayMessage(null);
                        j++;
                    }
                }

                if (!result) {
                    replayInfo.setReplayMessage("5次还没回答正确，正确答案是：" + name + " ,下一题");
                    replayInfo.setReplayImg(new File(operatorInfoMapper.selectAvatarByName(name)));
                    replayInfo.setMp3((File) null);
                    sendMessageUtil.sendGroupMsg(replayInfo);
                    //把消息内容清掉，后续复用
                    replayInfo.setReplayMessage(null);
                    replayInfo.getReplayImg().clear();
                    hintsList.clear();
                }
            }
            groupList.remove(messageInfo.getGroupId());
            TextLine textLine = new TextLine();
            textLine.addString("答题结束，本轮的答题结果:");
            textLine.nextLine();
            for (String name: score.keySet()) {
                textLine.addString(name + "答对了" + score.get(name) + "题");
                textLine.nextLine();
            }
            replayInfo.setReplayImg(textLine.drawImage());
            return replayInfo;
        }
    }

    @AngelinaGroup(keyWords = {"重启猜干员"})
    public ReplayInfo reGuessOperator(MessageInfo messageInfo) {
        groupList.remove(messageInfo.getGroupId());
        AngelinaEventSource.remove(messageInfo.getGroupId());
        return guessOperator(messageInfo);
    }

    @AngelinaGroup(keyWords = {"结束猜干员", "终止猜干员", "中止猜干员"})
    public ReplayInfo stopGuessOperator(MessageInfo messageInfo) {
        groupList.remove(messageInfo.getGroupId());
        AngelinaEventSource.remove(messageInfo.getGroupId());
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
        replayInfo.setReplayMessage("本群猜干员已结束");
        return replayInfo;
    }

    /**
     * 获取一个干员的信息
     * @param name 干员名
     * @param i 这是第几道题
     * @param maps 当前已经有了哪几个信息
     * @param num 需要添加几条信息
     */
    private GuessOperatorInfo getTitle(String name, int i, Map<HintsType, HintsInfo> maps, int num) {
        GuessOperatorInfo guessOperatorInfo = new GuessOperatorInfo();
        TextLine textLine = new TextLine();
        textLine.addString("第" + i + "题：");
        textLine.nextLine();
        int j = 0;
        while(j < num && maps.size() < HintsType.values().length) {
            HintsType hintsType = HintsType.values()[new Random().nextInt(HintsType.values().length)];
            HintsInfo addInfo = new HintsInfo(hintsType);
            if (!maps.containsKey(hintsType)) {
                if (addHintsInfo(name, addInfo)) {
                    maps.put(hintsType, addInfo);
                    j++;
                } else {
                    maps.put(hintsType, addInfo);
                }
            }
        }

        for (HintsInfo hints: maps.values()) {
            drawInfo(textLine, hints);
            if (hints.getIsSendVoice()) {
                guessOperatorInfo.setVoice(hints.getVoice());
                hints.setSendVoice(false);
            }
        }
        guessOperatorInfo.setTextLine(textLine);

        return guessOperatorInfo;
    }

    /**
     * 把对应的信息条件画在图上
     * @param textLine 图片
     * @param hints 画哪个条件
     */
    private void drawInfo(TextLine textLine, HintsInfo hints) {
        textLine.addString(hints.getText());
        if (hints.getImg() != null) {
            try {
                textLine.addImage(ImageIO.read(new File(hints.getImg())));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        textLine.nextLine();
    }
    private boolean addHintsInfo(String name, HintsInfo i) {
        boolean add = true;

        OperatorBasicInfo operatorInfo = operatorInfoMapper.getOperatorInfoByName(name);
        switch (i.getHintsType()) {
            case Sex:
                i.setText("该干员的性别为：" + operatorInfo.getSex());
                break;
            case Rarity:
                i.setText("该干员的稀有度为：" + operatorInfo.getOperatorRarity());
                break;
            case Race:
                i.setText("该干员的种族为：" + operatorInfo.getRace());
                break;
            case ComeFrom:
                i.setText("该干员的出身地为：" + operatorInfo.getComeFrom());
                break;
            case SkillImg:
                try {
                    i.setText("该干员的某个技能图标为：");
                    if (operatorInfo.getOperatorRarity() == 6) {
                        i.setImg(skillDescMapper.selectSkillPngByNameAndIndex(name, new Random().nextInt(3) + 1));
                    } else if (operatorInfo.getOperatorRarity() > 3) {
                        i.setImg(skillDescMapper.selectSkillPngByNameAndIndex(name, new Random().nextInt(2) + 1));
                    } else if (operatorInfo.getOperatorRarity() == 3) {
                        i.setImg(skillDescMapper.selectSkillPngByNameAndIndex(name, 1));
                    } else {
                        i.setText("该干员没有技能");
                    }
                } catch (NullPointerException e) {
                    i.setText("查找该干员技能失败");
                    e.printStackTrace();
                    add = false;
                }
                break;
            case DrawName:
                i.setText("该干员的立绘画师为：" + operatorInfo.getDrawName());
                break;
            case Class:
                String className = "";
                switch (operatorInfo.getOperatorClass()) {
                    case 1:
                        className = "先锋";
                        break;
                    case 2:
                        className = "近卫";
                        break;
                    case 3:
                        className = "重装";
                        break;
                    case 4:
                        className = "狙击";
                        break;
                    case 5:
                        className = "术士";
                        break;
                    case 6:
                        className = "辅助";
                        break;
                    case 7:
                        className = "医疗";
                        break;
                    case 8:
                        className = "特种";
                        break;
                }
                i.setText("该干员的职业为：" + className);
                break;
            case Birthday:
                i.setText("该干员的生日为：" + operatorInfo.getBirthday());
                break;
            case Height:
                i.setText("该干员的身高为：" + operatorInfo.getHeight());
                break;
            case Infection:
                i.setText("该干员的感染情况为：" + operatorInfo.getInfection().replace("name", "xxx"));
                break;
            case Voice:
                List<String> voices = operatorInfoMapper.selectOperatorVoiceByNameAndVoice("voice", name, null);
                if (voices.size() > 0) {
                    i.setText("请听该干员的语音：");
                    i.setVoice(voices.get(new Random().nextInt(voices.size())));
                    i.setSendVoice(true);
                } else {
                    i.setText("语音缺失");
                    add = false;
                }
                break;
        }
        return add;
    }
}
