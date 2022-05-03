package top.strelitzia.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.angelinaBot.annotation.AngelinaGroup;
import top.angelinaBot.model.MessageInfo;
import top.angelinaBot.model.ReplayInfo;
import top.strelitzia.dao.AdminUserMapper;
import top.strelitzia.dao.AgentMapper;
import top.strelitzia.dao.OperatorInfoMapper;
import top.strelitzia.dao.UserFoundMapper;
import top.strelitzia.model.AdminUserInfo;
import top.strelitzia.model.AgentInfo;
import top.strelitzia.model.UserFoundInfo;
import top.strelitzia.util.AdminUtil;
import top.strelitzia.util.FormatStringUtil;
import top.strelitzia.util.FoundAgentUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;

/**
 * @author strelitzia
 * @Date 2020/12/7 14:35
 **/
@Service
@Slf4j
public class AgentService {

    @Autowired
    private AgentMapper agentMapper;

    @Autowired
    private UserFoundMapper userFoundMapper;

    @Autowired
    private AdminUserMapper adminUserMapper;

    @Autowired
    private GroupAdminInfoService groupAdminInfoService;

    @Autowired
    private OperatorInfoMapper operatorInfoMapper;


    @AngelinaGroup(keyWords = {"单抽", "抽卡"}, description = "文字单次模拟抽卡")
    public ReplayInfo chouKa(MessageInfo messageInfo) {
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
        List<String> args = messageInfo.getArgs();
        String pool = "常规";
        if (args.size() > 1) {
            pool = args.get(1);
        }
        replayInfo.setReplayMessage(messageInfo.getName() + "\n抽取" + foundLimit(1, pool, messageInfo.getQq(), messageInfo.getName(), messageInfo.getGroupId()));
        return replayInfo;
    }

    @AngelinaGroup(keyWords = {"十连", "十抽"}, description = "文字十连模拟抽卡")
    public ReplayInfo shiLian(MessageInfo messageInfo) {
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
        List<String> args = messageInfo.getArgs();
        String pool = "常规";
        if (args.size() > 1) {
            pool = args.get(1);
        }
        replayInfo.setReplayMessage(messageInfo.getName() + "\n抽取" + foundLimit(10, pool, messageInfo.getQq(), messageInfo.getName(), messageInfo.getGroupId()));
        return replayInfo;
    }

    @AngelinaGroup(keyWords = {"十连寻访"}, description = "图片十连模拟抽卡")
    public ReplayInfo XunFang(MessageInfo messageInfo) throws IOException {
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
        List<String> args = messageInfo.getArgs();
        String pool = "常规";
        if (args.size() > 1) {
            pool = args.get(1);
        }
        UserFoundInfo userFoundInfo = userFoundMapper.selectUserFoundByQQ(messageInfo.getQq());
        Integer limit = groupAdminInfoService.getGroupFoundAdmin(messageInfo.getGroupId());
        if (userFoundInfo == null) {
            userFoundInfo = new UserFoundInfo();
            userFoundInfo.setQq(messageInfo.getQq());
            userFoundInfo.setFoundCount(0);
            userFoundInfo.setTodayCount(0);
        }
        //去数据库中查询这个人的垫刀数
        Integer sum = userFoundInfo.getFoundCount();
        //今日抽卡数
        Integer today = userFoundInfo.getTodayCount();
        List<AdminUserInfo> admins = adminUserMapper.selectAllAdmin();
        boolean b = AdminUtil.getFoundAdmin(messageInfo.getQq(), admins);

        if (today < limit || b) {
            //如果没输入卡池名或者卡池不存在
            if (pool == null || agentMapper.selectPoolIsExit(pool).size() == 0) {
                pool = "常规";
            }
            String s = FoundAgentByNum(10, pool, messageInfo.getQq(), sum, messageInfo.getName(), messageInfo.getGroupId());
            s = s.replace(" ", "");

            //干员立绘绘制的序号
            int No = 0;
            //创建图片画布
            BufferedImage image = new BufferedImage(960, 450, BufferedImage.TYPE_INT_BGR);
            Graphics g = image.getGraphics();
            // 画出抽卡背景
            File backgroundFile = new File("runFile/basicPng/background.jpg");
            if (!backgroundFile.exists()) {
                log.warn("{}素材图片缺失，改为文字发送", backgroundFile.getName());
                return shiLian(messageInfo);
            }
            BufferedImage background = ImageIO.read(backgroundFile);
            g.drawImage(background, 0, 0, 960, 450, null);

            String[] agents = s.split("\n");
            for (String agent : agents) {
                String[] split = agent.split("\t");
                String agentName = split[0];

                // 画出角色背景颜色
                int star = split[1].length();
                File starFile = new File("runFile/basicPng/star" + star + ".jpg");
                if (!starFile.exists()) {
                    log.warn("{}素材图片缺失，改为文字发送", starFile.getName());
                    return shiLian(messageInfo);
                }
                BufferedImage starPng = ImageIO.read(starFile);
                g.drawImage(starPng, 70 + No * 82, 0, 82, 450, null);
                //画出干员立绘
                String filePath = operatorInfoMapper.selectOperatorPngByName(agentName);
                File agentFile = new File(filePath);
                if (!agentFile.exists()) {
                    log.warn("{}素材图片缺失，改为文字发送", agentFile.getName());
                    return shiLian(messageInfo);
                }
                BufferedImage oldImg = ImageIO.read(agentFile);
                if (oldImg != null) {
                    int width = 252 * oldImg.getWidth() / oldImg.getHeight();
                    int x = width / 2 - 41;
                    int y = 0;
                    int h = 252;
                    BufferedImage newImg = new BufferedImage(width, h, TYPE_INT_ARGB);
                    Graphics2D graphics = newImg.createGraphics();
                    graphics.drawImage(oldImg, 0, 0, width, h, null);
                    graphics.dispose();
                    //裁剪立绘
                    BufferedImage charBase = newImg.getSubimage(x, y, 82, 252);
                    g.drawImage(charBase, 70 + No * 82, 110, 82, 252, null);
                }

                // 画出角色职业图标
                Integer classId = operatorInfoMapper.selectOperatorClassByName(agentName);
                File classFile = new File("runFile/basicPng/" + classId + ".jpg");
                if (!classFile.exists()) {
                    log.warn("{}素材图片缺失，改为文字发送", classFile.getName());
                    return shiLian(messageInfo);
                }
                BufferedImage bImage = ImageIO.read(classFile);
                g.drawImage(bImage, 81 + No * 82, 320, 60, 60, null);
                No++;
            }
            g.setFont(new Font("楷体", Font.BOLD, 20));
            g.setColor(Color.WHITE);
            g.drawString("结果仅供参考，详细代码请见：", 470, 420);
            g.drawString("http://www.angelina-bot.top/", 470, 440);
            g.dispose();
            replayInfo.setReplayImg(image);
            return replayInfo;
        }else {
            replayInfo.setReplayMessage("今日抽卡机会无了");
            return replayInfo;
        }
    }

    @AngelinaGroup(keyWords = {"卡池", "卡池列表"}, description = "展示现有所有卡池")
    public ReplayInfo selectPool(MessageInfo messageInfo) {
        String pool = "";
        if (messageInfo.getArgs().size() > 1) {
            pool = messageInfo.getArgs().get(1);
        }
        List<String> poolList = agentMapper.selectPool(pool);
        StringBuilder str = new StringBuilder();
        for (String line : poolList) {
            str.append("\n").append(line);
        }
        //去掉头部换行
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
        replayInfo.setReplayMessage(str.substring(1));
        return replayInfo;
    }

    @AngelinaGroup(keyWords = {"垫刀"}, description = "展示当前垫刀数、抽卡记录")
    public ReplayInfo selectFoundCount(MessageInfo messageInfo) {
        UserFoundInfo userFoundInfo = userFoundMapper.selectUserFoundByQQ(messageInfo.getQq());
        Integer todayCount = 0;
        Integer foundCount = 0;
        Integer allCount = 0;
        Integer allSix = 0;
        Integer todayFive = 0;
        if (userFoundInfo != null) {
            foundCount = userFoundInfo.getFoundCount();
            todayCount = userFoundInfo.getTodayCount();
            allCount = userFoundInfo.getAllCount();
            allSix = userFoundInfo.getAllSix();
            todayFive = userFoundInfo.getAllFive();
        }
        int sixStar;
        if (foundCount > 50) {
            sixStar = 2 + (foundCount - 50) * 2;
        } else {
            //六星概率默认2%
            sixStar = 2;
        }

        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
        replayInfo.setReplayMessage(messageInfo.getName() + "的当前垫刀数为：" + foundCount + "\n当前六星概率为："
                + sixStar + "%" + "\n今日已抽卡：" + todayCount
                + "次\n累计共抽取了：" + allCount + "次\n累计获得了" + allSix + "个六星和" + todayFive + "个五星干员");

        return replayInfo;
    }

    @AngelinaGroup(keyWords = {"卡池清单", "卡池干员", "卡池up"}, description = "展示某个卡池的up干员")
    public ReplayInfo selectPoolAgent(MessageInfo messageInfo) {
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
        if (messageInfo.getArgs().size() < 2) {
            replayInfo.setReplayMessage("请输入卡池名称");
        }else {
            String pool = messageInfo.getArgs().get(1);
            if (pool.equals("凭证兑换") || pool.equals("活动") || pool.equals("公招") || pool.equals("初始") || pool.equals("常规")) {
                replayInfo.setReplayMessage("没有找到该卡池哦");
                return replayInfo;
            }
            List<AgentInfo> agents = agentMapper.selectPoolAgent(pool);
            StringBuilder s = new StringBuilder("卡池[" + pool + "]中概率up干员为：");
            for (AgentInfo agent : agents) {
                String limit = "";
                if (agent.getLimit() == 3) {
                    limit = "5倍权值";
                }
                s.append("\n").append(agent.getName()).append(FormatStringUtil.FormatStar(agent.getStar())).append(" ").append(limit);
            }
            replayInfo.setReplayMessage(s.toString());
        }
        return replayInfo;
    }

    /**
     * 限制每日的抽卡次数
     */
    public String foundLimit(int count, String pool, Long qq, String name, Long groupId) {
        UserFoundInfo userFoundInfo = userFoundMapper.selectUserFoundByQQ(qq);
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
        String s = "今日抽卡机会无了";
        List<AdminUserInfo> admins = adminUserMapper.selectAllAdmin();
        boolean b = AdminUtil.getFoundAdmin(qq, admins);
        Integer limit = groupAdminInfoService.getGroupFoundAdmin(groupId);
        if (today < limit || b) {
            //如果没输入卡池名或者卡池不存在
            if (pool == null || agentMapper.selectPoolIsExit(pool).size() == 0) {
                pool = "常规";
            }
            s = pool + "池：\n" + FoundAgentByNum(count, pool, qq, sum, name, groupId);
        }
        return s;
    }

    /**
     * 抽卡通用方法
     *
     * @param count 抽几张卡
     * @param pool  卡池名称
     * @param qq    抽取人qq
     */
    public String FoundAgentByNum(int count, String pool, Long qq, Integer sum, String name, Long groupId) {
        List<AdminUserInfo> admins = adminUserMapper.selectAllAdmin();
        boolean b = AdminUtil.getSixAdmin(qq, admins);
        //如果没输入卡池名或者卡池不存在
        if (pool == null || agentMapper.selectPoolIsExit(pool).size() == 0) {
            pool = "常规";
        }
        StringBuilder s = new StringBuilder();
        //循环抽卡
        for (int j = 0; j < count; j++) {
            if (b) {
                sum = 100;
            }
            //获取干员稀有度
            int star = FoundAgentUtil.FoundOneByMath(qq, sum);
            if (star == 6) {
                //抽到六星垫刀归零
                sum = 0;
                userFoundMapper.updateUserFoundByQQ(qq, name, groupId, sum);
                userFoundMapper.updateSixByQq(qq);
            } else if (star == 5) {
                //没有六星垫刀+1
                sum = sum + 1;
                userFoundMapper.updateUserFoundByQQ(qq, name, groupId, sum);
                userFoundMapper.updateFiveByQq(qq);
            } else {
                //没有六星垫刀+1
                sum = sum + 1;
                userFoundMapper.updateUserFoundByQQ(qq, name, groupId, sum);
            }
            //保存结果集
            List<AgentInfo> agentList;
            //使用不同的方法Math/Random进行随机运算，尽可能取消同一时间戳导致的相同随机数(虽然两个算法本质一样，这样做基本屁用没有)
            double r = Math.random();
            //是不是限定池 0->普通 1->周年限定 2->联动限定 3->5倍权值 4->新年限定
            int limit = agentMapper.selectPoolLimit(pool);
            int integers = limit == 0 ? 0 : 1;
            if (star == 6) {
                if (r <= 0.5 + 0.2 * integers) {
                    //获取当前卡池三星/四星/五星/六星列表
                    agentList = agentMapper.selectAgentByStar(pool, star);
                } else {
                    agentList = agentMapper.selectAgentByStar("常规", star);
                    if (limit == 1 || limit == 4) {
                        //如果是限定池，就再加上前期可歪的限定干员
                        agentList.addAll(agentMapper.selectLimitAgent(limit));
                        //五倍权值（因为上面加过一个，所以再加四个就可以）
                        List<AgentInfo> fiveLimit = agentMapper.selectLimitAgentByPool(pool);
                        if (fiveLimit.size() > 0) {
                            for (int i = 0; i < 4; i++) {
                                agentList.addAll(fiveLimit);
                            }
                        }
                    }
                }
            } else if (star == 5) {
                if (r <= 0.5) {
                    //获取当前卡池三星/四星/五星/六星列表
                    agentList = agentMapper.selectAgentByStar(pool, star);
                } else {
                    agentList = agentMapper.selectAgentByStar("常规", star);
                }
            } else if (star == 4) {
                if (r <= 0.2) {
                    //获取当前卡池三星/四星/五星/六星列表
                    agentList = agentMapper.selectAgentByStar(pool, star);
                } else {
                    agentList = agentMapper.selectAgentByStar("常规", star);
                }
            } else {
                agentList = agentMapper.selectAgentByStar("常规", star);
            }
            //有可能三星还去up池里找，因为三星不存在up所以报空，重新去常规池里找
            if (agentList.size() == 0) {
                agentList = agentMapper.selectAgentByStar("常规", star);
            }
            //随机数种子采用纳秒数+毫秒/qq，尽可能减少时间戳导致的不随机
            Random random = new Random(System.nanoTime() + System.currentTimeMillis() / qq);
            int i = random.nextInt(agentList.size());
            String levelStar = FormatStringUtil.FormatStar(star);
            try {
                s.append(" ").append(agentList.get(i).getName()).append("\t").append(levelStar).append("\n");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return s.toString();
    }
}
