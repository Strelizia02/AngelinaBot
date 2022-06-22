package top.strelitzia.service;

import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.contact.MemberPermission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.angelinaBot.annotation.AngelinaGroup;
import top.angelinaBot.container.AngelinaEventSource;
import top.angelinaBot.model.MessageInfo;
import top.angelinaBot.model.ReplayInfo;
import top.angelinaBot.model.TextLine;
import top.angelinaBot.util.SendMessageUtil;
import top.strelitzia.arknightsDao.OperatorInfoMapper;
import top.strelitzia.arknightsDao.SkillDescMapper;
import top.strelitzia.dao.AdminUserMapper;
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
            List<Integer> hintsList = new ArrayList<>(6);
            //开始循环问
            for (int i = 0; i < num; i++) {
                log.info(list.get(i));

                boolean result = false;
                replayInfo.setReplayImg(getTitle(list.get(i), i, hintsList, 2).drawImage());
                sendMessageUtil.sendGroupMsg(replayInfo);
                replayInfo.getReplayImg().clear();
                int j = 0;
                //仅有五次回答机会，超过五次就寄了，下一题
                while (j < 5) {
                    //等待一个“洁哥回答”
                    MessageInfo recall = AngelinaEventSource.waiter(message -> message.getKeyword().equals("回答")).getMessageInfo();
                    if (recall.getArgs().get(1).equals(list.get(i))) {
                        //答对了，直接下一题
                        replayInfo.setReplayMessage("回答正确，下一题");
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
                        replayInfo.setReplayImg(getTitle(list.get(i), i, hintsList, 1).drawImage());
                        sendMessageUtil.sendGroupMsg(replayInfo);
                        replayInfo.getReplayImg().clear();
                        replayInfo.setReplayMessage(null);
                        j++;
                    }
                }
                if (!result) {
                    System.out.println("5次还没回答正确，正确答案是：" + list.get(i) + "，下一题");
                }
            }
            groupList.remove(messageInfo.getGroupId());
            replayInfo.setReplayMessage("答题结束" + score);
            return replayInfo;
        }
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
         * 0-5分别代表 性别，稀有度，种族，出身地，技能图标，画师
         */
        TextLine textLine = new TextLine();
        textLine.addString("第" + i + "题：");
        textLine.nextLine();
        for (Integer integer : list) {
            drawInfo(textLine, name, integer);
        }
        int j = 0;
        while(j < num) {
            int addInfo = new Random().nextInt(6);
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
        }
    }
}
