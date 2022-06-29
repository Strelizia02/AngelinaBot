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
import top.strelitzia.dao.NickNameMapper;
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
            }
            replayInfo.setReplayMessage("本群猜干员正式开始，请听题");
            sendMessageUtil.sendGroupMsg(replayInfo);
            replayInfo.setReplayMessage(null);
            List<String> allOperator = operatorInfoMapper.getAllOperator();
            List<String> list = new ArrayList<>(num);
            //随机抽取干员名
            for (int i = 0; i < num; i++) {
                list.add(allOperator.get(new Random().nextInt(allOperator.size())));
            }

            Map<String, Integer> score = new HashMap<>();
            //记录都提示了哪些信息
            List<Integer> hintsList = new ArrayList<>(7);
            //开始循环问
            for (int i = 0; i < num; i++) {
                log.info(list.get(i));

                boolean result = false;
                replayInfo.setReplayImg(getTitle(list.get(i), i, hintsList, 3).drawImage());
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
                        return replayInfo;
                    }

                    String name = recall.getText();
                    String realName = nickNameMapper.selectNameByNickName(name);
                    if (realName != null && !realName.equals(""))
                        name = realName;
                    if (name.equals(list.get(i))) {
                        //答对了，直接下一题
                        replayInfo.setReplayMessage(recall.getName() + " 回答正确，答案是 " + name + " ,下一题");
                        replayInfo.setReplayImg(new File(operatorInfoMapper.selectAvatarByName(name)));
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
                    } else if (name.equals("提示")) {
                        replayInfo.setReplayImg(getTitle(list.get(i), i, hintsList, 1).drawImage());
                        sendMessageUtil.sendGroupMsg(replayInfo);
                        replayInfo.getReplayImg().clear();
                        j++;
                    } else {
                        //答错了
                        replayInfo.setReplayMessage("回答错误");
                        replayInfo.setReplayImg(getTitle(list.get(i), i, hintsList, 1).drawImage());
                        sendMessageUtil.sendGroupMsg(replayInfo);
                        replayInfo.getReplayImg().clear();
                        replayInfo.setReplayMessage(null);
                        j++;
                    }
                }
                if (!result) {
                    replayInfo.setReplayMessage("5次还没回答正确，正确答案是：" + list.get(i) + " ,下一题");
                    replayInfo.setReplayImg(new File(operatorInfoMapper.selectAvatarByName(list.get(i))));
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
        for (AngelinaListener listener: AngelinaEventSource.getInstance().listenerSet.keySet()) {
            if (listener.getGroupId().equals(messageInfo.getGroupId())) {
                AngelinaEventSource.getInstance().listenerSet.remove(listener);
            }
        }
        return guessOperator(messageInfo);
    }

    /**
     * 获取一个干员的信息
     * @param name 干员名
     * @param i 这是第几道题
     * @param list 当前已经有了哪几个信息
     * @param num 需要添加几条信息
     */
    private TextLine getTitle(String name, int i, List<Integer> list, int num) {
        /**
         * 0-6分别代表 性别，稀有度，种族，出身地，技能图标，画师, 职业
         */
        TextLine textLine = new TextLine();
        textLine.addString("第" + i + "题：");
        textLine.nextLine();
        for (Integer integer : list) {
            drawInfo(textLine, name, integer);
        }
        int j = 0;
        while(j < num && list.size() < 7) {
            int addInfo = new Random().nextInt(7);
            if (!list.contains(addInfo)) {
                drawInfo(textLine, name, addInfo);
                list.add(addInfo);
                j++;
            }
        }
        return textLine;
    }

    /**
     * 把对应的信息条件画在图上
     * @param textLine 图片
     * @param i 画哪个条件
     */
    private void drawInfo(TextLine textLine, String name, int i) {
        OperatorBasicInfo operatorInfo = operatorInfoMapper.getOperatorInfoByName(name);
        switch (i) {
            case 0:
                textLine.addString("该干员的性别为：" + operatorInfo.getSex());
                textLine.nextLine();
                break;
            case 1:
                textLine.addString("该干员的稀有度为：" + operatorInfo.getOperatorRarity());
                textLine.nextLine();
                break;
            case 2:
                textLine.addString("该干员的种族为：" + operatorInfo.getRace());
                textLine.nextLine();
                break;
            case 3:
                textLine.addString("该干员的出身地为：" + operatorInfo.getComeFrom());
                textLine.nextLine();
                break;
            case 4:
                try {
                    if (operatorInfo.getOperatorRarity() == 6) {
                        textLine.addString("该干员的某个技能图标为：");
                        textLine.addImage(ImageIO.read(new File(skillDescMapper.selectSkillPngByNameAndIndex(name, new Random().nextInt(3) + 1))));
                    } else if (operatorInfo.getOperatorRarity() > 3) {
                        textLine.addString("该干员的某个技能图标为：");
                        textLine.addImage(ImageIO.read(new File(skillDescMapper.selectSkillPngByNameAndIndex(name, new Random().nextInt(2) + 1))));
                    } else if (operatorInfo.getOperatorRarity() == 3) {
                        textLine.addString("该干员的某个技能图标为：");
                        textLine.addImage(ImageIO.read(new File(skillDescMapper.selectSkillPngByNameAndIndex(name, 1))));
                    } else {
                        textLine.addString("该干员没有技能");
                    }
                } catch (IOException | NullPointerException e) {
                    textLine.addString("查找该干员技能失败");
                    e.printStackTrace();
                }
                textLine.nextLine();
                break;
            case 5:
                textLine.addString("该干员的立绘画师为：" + operatorInfo.getDrawName());
                textLine.nextLine();
                break;
            case 6:
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
                textLine.addString("该干员的职业为：" + className);
                textLine.nextLine();
                break;
        }
    }
}
