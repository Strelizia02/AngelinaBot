package top.strelitzia.service;

import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.contact.MemberPermission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.angelinaBot.annotation.AngelinaGroup;
import top.angelinaBot.model.MessageInfo;
import top.angelinaBot.model.ReplayInfo;
import top.angelinaBot.model.TextLine;
import top.strelitzia.dao.AdminUserMapper;
import top.strelitzia.dao.IntegralMapper;
import top.strelitzia.dao.NickNameMapper;
import top.strelitzia.arknightsDao.OperatorInfoMapper;
import top.strelitzia.model.OperatorBasicInfo;
import top.strelitzia.util.AdminUtil;

import java.util.*;

@Service
@Slf4j
public class OperatorGuessService {

    @Autowired
    private OperatorInfoMapper operatorInfoMapper;

    @Autowired
    private NickNameMapper nickNameMapper;

    @Autowired
    private IntegralMapper integralMapper;

    @Autowired
    private AdminUserMapper adminUserMapper;

    //选出的干员map表
    private  static final Map<Long, List<String>> operatorSelectInfo =new HashMap<>();
    //猜题信息
    private  static final Map<Long,List<Integer>> operatorSelect =new HashMap<>();

    @AngelinaGroup(keyWords = {"的小小茶话会"}, description = "茶话会干员竞猜,默认十位（要出多的题可以在后面追加 □ （数字）")
    public ReplayInfo beginTopic(MessageInfo messageInfo) {
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
        int topicNum = 10 ;
        if(messageInfo.getArgs().size()>1){
            topicNum = Integer.parseInt(messageInfo.getArgs().get(1));
        }
        if(topicNum > 99){
            replayInfo.setReplayMessage("博士，这间屋子容不下那么多人的啦");
            return replayInfo;
        }
        List<String> operatorList = new ArrayList<>();
        if (!(operatorSelectInfo.get(messageInfo.getGroupId()) == null)){
            replayInfo.setReplayMessage("博士，这场茶话会还没有结束，我们还需要等待嘉宾们到达哦");
            return replayInfo;
        }else{
            List<String> allOperator = operatorInfoMapper.getAllOperator();
            for (int i = 0;i<topicNum;i++){
                String name = allOperator.get(new Random().nextInt(allOperator.size()));
                operatorList.add(name);
            }
            operatorSelectInfo.put(messageInfo.getGroupId(),operatorList);
            replayInfo.setReplayMessage("博士，我和风笛邀请了几位罗德岛干员来参加我们的下午茶聚会，他们还未到达，博士可以试着猜猜是谁要来参加我们的茶会呢" +
                    "\n那茶会正式开始了，博士您猜猜谁会第一个光临这小小的茶话会呢");
        }
        return replayInfo;
    }

    @AngelinaGroup(keyWords = {"抢答"}, description = "抢答当前提问题目")
    public ReplayInfo answerTopic(MessageInfo messageInfo) {
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
        if (operatorSelectInfo.get(messageInfo.getGroupId()) == null){
            replayInfo.setReplayMessage("博士别急，心急吃不了甜司康饼，还不能和我狞笑");
            return replayInfo;
        }
        List<String> operatorList = operatorSelectInfo.get(messageInfo.getGroupId());
        if (operatorSelect.get(messageInfo.getGroupId()) == null){
            //前面是题号，后面是尝试次数
            List<Integer> operatorSelectList =new ArrayList<>(Arrays.asList(0,0));
            operatorSelect.put(messageInfo.getGroupId(),operatorSelectList);
        }
        List<Integer> operatorSelectList = operatorSelect.get(messageInfo.getGroupId());
        Integer topicNum = operatorSelectList.get(0);
        Integer tryNum = operatorSelectList.get(1);
        log.info(operatorList.get(topicNum));
        if (messageInfo.getArgs().size() > 1) {
            String answerName = messageInfo.getArgs().get(1);//取得回答的答案
            String realName = nickNameMapper.selectNameByNickName(answerName);
            if (realName != null && !realName.equals("")) {
                answerName = realName;
            }
            // 利用输入的信息进行比对，依次输出比对结果是或否
            boolean answer =false, DrawName = false, OperatorRarity =false, sex = false, ComeFrom = false, Race = false, Infection = false, profession = false;
            //判断是否猜中
            if(answerName.equals(operatorList.get(topicNum))){
                answer =true;
            }else{
                OperatorBasicInfo operatorInfoGuess = this.operatorInfoMapper.getOperatorInfoByName(answerName);
                OperatorBasicInfo operatorInfoTrue = this.operatorInfoMapper.getOperatorInfoByName(operatorList.get(topicNum));//取得对应题号的干员信息
                //判断干员档案各个条件是否相同
                if(operatorInfoGuess.getDrawName().equals(operatorInfoTrue.getDrawName())){
                    DrawName =true;
                }
                if(operatorInfoGuess.getOperatorRarity().equals(operatorInfoTrue.getOperatorRarity())){
                    OperatorRarity =true;
                }
                if(operatorInfoGuess.getSex().equals(operatorInfoTrue.getSex())){
                    sex =true;
                }
                if(operatorInfoGuess.getComeFrom().equals(operatorInfoTrue.getComeFrom())){
                    ComeFrom =true;
                }
                if(operatorInfoGuess.getRace().equals(operatorInfoTrue.getRace())){
                    Race =true;
                }
                if(operatorInfoGuess.getInfection().equals(operatorInfoTrue.getInfection())){
                    Infection =true;
                }
                if(operatorInfoGuess.getOperatorClass().equals(operatorInfoTrue.getOperatorClass())){
                    profession =true;
                }
            }
            // 当结果为ture，topicNum+1并返回setTopic
            if(answer) {
                Integer integral = this.integralMapper.selectByName(messageInfo.getName());
                //猜谜答对一次的人加一分
                try{integral = integral + 1;
                }catch (NullPointerException e){
                    //log.info(e.toString());
                    integral = 1;
                }
                this.integralMapper.integralByGroupId(messageInfo.getGroupId(), messageInfo.getName(), messageInfo.getQq(), integral);
                //更新题号和猜测次数
                operatorSelectList.set(0,topicNum + 1);
                operatorSelectList.set(1,0);
                operatorSelect.put(messageInfo.getGroupId(),operatorSelectList);
                replayInfo.setReplayMessage("真棒"+messageInfo.getName()+"博士，恭喜您回答正确，正是干员"+operatorList.get(topicNum)+"呢，看，他已经加入茶会中了" +
                        "\n让我们续上茶水，继续猜测下一位嘉宾是谁吧");
            }else {
                //当结果为false，tryNum+1并返回answerTopic继续抢答
                TextLine textLine = new TextLine(100);
                textLine.addString("提示：");
                textLine.nextLine();
                if(DrawName){
                    textLine.addString("画师：√");
                }else{
                    textLine.addString("画师：X");
                }textLine.nextLine();
                if(OperatorRarity){
                    textLine.addString("星级：√");
                }else{
                    textLine.addString("星级：X");
                }textLine.nextLine();
                if(sex){
                    textLine.addString("性别：√");
                }else{
                    textLine.addString("性别：X");
                }textLine.nextLine();
                if(ComeFrom){
                    textLine.addString("出生地：√");
                }else{
                    textLine.addString("出生地：X");
                }textLine.nextLine();
                if(Race){
                    textLine.addString("种族：√");
                }else{
                    textLine.addString("种族：X");
                }textLine.nextLine();
                if(Infection){
                    textLine.addString("感染情况：√");
                }else{
                    textLine.addString("感染情况：X");
                }textLine.nextLine();
                if(profession){
                    textLine.addString("职业：√");
                }else{
                    textLine.addString("职业：X");
                }textLine.nextLine();
                replayInfo.setReplayImg(textLine.drawImage());
                replayInfo.setReplayMessage("不对不对，博士，再尝试一下吧");
                operatorSelectList.set(1,tryNum+1);
                operatorSelect.put(messageInfo.getGroupId(),operatorSelectList);
                // 判断tryNum是否达到十次，超过十次则公布答案并进行下一题
                if (tryNum > 10) {
                    operatorSelectList.set(0,topicNum + 1);
                    operatorSelectList.set(1,0);
                    operatorSelect.put(messageInfo.getGroupId(),operatorSelectList);
                    //replayInfo.clear();
                    replayInfo.setReplayMessage("都不对哦博士，你看他来了，是干员"+operatorList.get(topicNum)+"呢"+
                            "\n让我们续上茶水，继续猜测下一位嘉宾是谁吧");
                    replayInfo.getReplayImg().clear();
                }
            }
        }else{
            replayInfo.setReplayMessage("博士？");
        }
        return replayInfo;
    }

    @AngelinaGroup(keyWords = {"退出茶话会"}, description = "茶话会关闭")
    public ReplayInfo closeTopic(MessageInfo messageInfo) {
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
        boolean sqlAdmin = AdminUtil.getSqlAdmin(messageInfo.getQq(), adminUserMapper.selectAllAdmin());
        if (messageInfo.getUserAdmin() == MemberPermission.MEMBER && !sqlAdmin){
            replayInfo.setReplayMessage("（琴柳似乎沉浸在和桑葚的聊天中，并没有注意到你）");
        }else {
            operatorSelectInfo.remove(messageInfo.getGroupId());
            operatorSelect.remove(messageInfo.getGroupId());
            replayInfo.setReplayMessage("博士您要走了吗，那请帮忙把这包甜司康饼带给小刻吧，下次有空记得还来玩啊");
        }
        return replayInfo;
    }

}
