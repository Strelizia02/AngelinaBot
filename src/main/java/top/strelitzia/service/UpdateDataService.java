package top.strelitzia.service;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import top.angelinaBot.annotation.AngelinaFriend;
import top.angelinaBot.annotation.AngelinaGroup;
import top.angelinaBot.model.MessageInfo;
import top.angelinaBot.model.ReplayInfo;
import top.strelitzia.arknightsDao.*;
import top.strelitzia.dao.AdminUserMapper;
import top.strelitzia.model.*;
import top.strelitzia.util.AdminUtil;
import top.strelitzia.util.FormatStringUtil;

import java.io.*;
import java.math.BigDecimal;
import java.net.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author strelitzia
 * @Date 2022/05/03 14:38
 **/
@Service
@Slf4j
public class UpdateDataService {

    @Autowired
    private UpdateMapper updateMapper;

    @Autowired
    protected RestTemplate restTemplate;

    @Autowired
    private BuildingSkillMapper buildingSkillMapper;

    @Autowired
    private SkinInfoMapper skinInfoMapper;

    @Autowired
    private OperatorInfoMapper operatorInfoMapper;

    @Autowired
    private MaterialMadeMapper materialMadeMapper;

    @Autowired
    private EnemyMapper enemyMapper;

    @Autowired
    private EquipMapper equipMapper;

    @Autowired
    private AdminUserMapper adminUserMapper;

    @Autowired
    private SkillDescMapper skillDescMapper;

    @Autowired
    private AgentTagsMapper agentTagsMapper;

//    private String url = "https://cdn.jsdelivr.net/gh/Kengxxiao/ArknightsGameData@master/zh_CN/gamedata/";
//    private String url = "https://raw.githubusercontent.com/yuanyan3060/Arknights-Bot-Resource/master/";
//    private String url = "http://vivien8261.gitee.io/arknights-bot-resource/gamedata/";
    private final String url = "https://raw.fastgit.org/yuanyan3060/Arknights-Bot-Resource/master/";

    /** 先判断版本是否相同→如果版本不同，开始更新→置位1→下载数据文件→置位0→下载完成→置位2→写入数据→置位0→写入完成 */
    private static int updateStatus = 0;

    @AngelinaGroup(keyWords = {"更新"}, description = "尝试更新数据")
    @AngelinaFriend(keyWords = {"更新"}, description = "尝试更新数据")
    public ReplayInfo downloadDataFile(MessageInfo messageInfo) {
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
        List<AdminUserInfo> admins = adminUserMapper.selectAllAdmin();
        if (!AdminUtil.getSqlAdmin(messageInfo.getQq(), admins)) {
            replayInfo.setReplayMessage("您无更新权限");
        } else {
            if (updateStatus == 0) {
                DownloadOneFileInfo downloadInfo = new DownloadOneFileInfo();

                if (messageInfo.getArgs().size() > 2) {
                    downloadInfo.setHostname(messageInfo.getArgs().get(1));
                    downloadInfo.setPort(Integer.parseInt(messageInfo.getArgs().get(2)));
                    downloadInfo.setUseHost(true);
                } else {
                    downloadInfo.setUseHost(false);
                }
                downloadInfo.setForce(false);
                downloadDataFile(downloadInfo);
                replayInfo.setReplayMessage("更新结束，请从后台日志查看更新情况");
            } else if (updateStatus == 1) {
                replayInfo.setReplayMessage("正在下载数据文件中，请稍后再试");
            } else {
                replayInfo.setReplayMessage("正在写入数据库中，请稍后再试");
            }
        }
        return replayInfo;
    }

    @AngelinaGroup(keyWords = {"全量更新"}, description = "强制全量更新数据")
    @AngelinaFriend(keyWords = {"全量更新"}, description = "强制全量更新数据")
    public ReplayInfo downloadDataFileForce(MessageInfo messageInfo) {
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
        List<AdminUserInfo> admins = adminUserMapper.selectAllAdmin();
        if (!AdminUtil.getSqlAdmin(messageInfo.getQq(), admins)) {
            replayInfo.setReplayMessage("您无更新权限");
        } else {
            if (updateStatus == 0) {
                DownloadOneFileInfo downloadInfo = new DownloadOneFileInfo();
                rebuildDatabase();
                if (messageInfo.getArgs().size() > 2) {
                    downloadInfo.setHostname(messageInfo.getArgs().get(1));
                    downloadInfo.setPort(Integer.parseInt(messageInfo.getArgs().get(2)));
                    downloadInfo.setUseHost(true);
                } else {
                    downloadInfo.setUseHost(false);
                }
                downloadInfo.setForce(true);
                boolean finish = downloadDataFile(downloadInfo);
                if (finish) {
                    replayInfo.setReplayMessage("更新完成");
                } else {
                    replayInfo.setReplayMessage("更新失败，请从后台日志查看更新情况");
                }
            } else if (updateStatus == 1) {
                replayInfo.setReplayMessage("正在下载数据文件中，请重启Bot后再试");
            } else {
                replayInfo.setReplayMessage("正在写入数据库中，请重启Bot后再试");
            }
        }
        return replayInfo;
    }

    @AngelinaGroup(keyWords = {"更新素材", "更新图片", "更新图标"}, description = "更新素材数据")
    @AngelinaFriend(keyWords = {"更新素材", "更新图片", "更新图标"}, description = "更新素材数据")
    public ReplayInfo updateImgFile(MessageInfo messageInfo) {
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
        List<AdminUserInfo> admins = adminUserMapper.selectAllAdmin();
        if (!AdminUtil.getSqlAdmin(messageInfo.getQq(), admins)) {
            replayInfo.setReplayMessage("您无更新权限");
        } else {
            DownloadOneFileInfo downloadInfo = new DownloadOneFileInfo();
            if(messageInfo.getArgs().size()>2){
                downloadInfo.setHostname(messageInfo.getArgs().get(1));
                downloadInfo.setPort(Integer.parseInt(messageInfo.getArgs().get(2)));
                downloadInfo.setUseHost(true);
            }else {
                downloadInfo.setUseHost(false);
            }
            //            updateSkin();
            updateItemIcon(downloadInfo);
            updateOperatorPng(downloadInfo);
            updateOperatorSkillPng(downloadInfo);
            replayInfo.setReplayMessage("正在更新素材");
        }
        return replayInfo;
    }

    @AngelinaGroup(keyWords = {"更新语音"}, description = "更新语音数据")
    @AngelinaFriend(keyWords = {"更新语音"}, description = "更新语音数据")
    public ReplayInfo updateVoiceFile(MessageInfo messageInfo) {
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
        List<AdminUserInfo> admins = adminUserMapper.selectAllAdmin();
        if (!AdminUtil.getSqlAdmin(messageInfo.getQq(), admins)) {
            replayInfo.setReplayMessage("您无更新权限");
        } else {
            DownloadOneFileInfo downloadInfo = new DownloadOneFileInfo();
            if(messageInfo.getArgs().size()>2){
                downloadInfo.setHostname(messageInfo.getArgs().get(1));
                downloadInfo.setPort(Integer.parseInt(messageInfo.getArgs().get(2)));
                downloadInfo.setUseHost(true);
            }else {
                downloadInfo.setUseHost(false);
            }
            updateOperatorVoice(downloadInfo);
            replayInfo.setReplayMessage("更新语音完成");
        }
        return replayInfo;
    }

    public boolean downloadDataFile(DownloadOneFileInfo downloadInfo) {
        String koKoDaYoKeyUrl = url + "gamedata/excel/data_version.txt";
        String charKey = getJsonStringFromUrl(koKoDaYoKeyUrl);
        //Integer versionStatus = updateMapper.getVersionStatus();
        File dataVersionFile = new File("runFile/download/data_version.txt");
        //确保状态是未正在下载
        if (updateStatus == 0) {
            boolean canDownload = true;
            //version文件不存在时，进行下载操作
            if (dataVersionFile.exists()) {
                String dataVersion = getJsonStringFromFile("data_version.txt");
                //version文件存在且和线上version不相等时，进行下载操作
                if (dataVersion.replace("\n", "").equals(charKey.replace("\n", ""))) {
                    log.info("线上版本和当前数据文件相同，无需下载");
                    canDownload = false;
                } else {
                    log.info("线上版本和当前数据文件不同，准备下载");
                }
            } else {
                log.info("数据文件不存在，准备下载");
            }
            if (canDownload || downloadInfo.isForce()) {
                updateStatus = 1;
                //重置数据目录
                rebuildDir();
                try {
                    log.info("开始下载数据文件");
                    downloadInfo.setSecond(600);
                    //updateMapper.doingDownloadVersion();
                    downloadInfo.setFileName("runFile/download/character_table.json");
                    downloadInfo.setUrl( url + "gamedata/excel/character_table.json");
                    downloadOneFile(downloadInfo);
                    downloadInfo.setFileName("runFile/download/gacha_table.json");
                    downloadInfo.setUrl( url+ "gamedata/excel/gacha_table.json");
                    downloadOneFile(downloadInfo);
                    downloadInfo.setFileName("runFile/download/skill_table.json");
                    downloadInfo.setUrl( url+ "gamedata/excel/skill_table.json");
                    downloadOneFile(downloadInfo);
                    downloadInfo.setFileName("runFile/download/building_data.json");
                    downloadInfo.setUrl( url+ "gamedata/excel/building_data.json");
                    downloadOneFile(downloadInfo);
                    downloadInfo.setFileName("runFile/download/handbook_info_table.json");
                    downloadInfo.setUrl( url+ "gamedata/excel/handbook_info_table.json");
                    downloadOneFile(downloadInfo);
                    downloadInfo.setFileName("runFile/download/charword_table.json");
                    downloadInfo.setUrl( url+ "gamedata/excel/charword_table.json");
                    downloadOneFile(downloadInfo);
                    downloadInfo.setFileName("runFile/download/char_patch_table.json");
                    downloadInfo.setUrl( url+ "gamedata/excel/char_patch_table.json");
                    downloadOneFile(downloadInfo);
                    downloadInfo.setFileName("runFile/download/item_table.json");
                    downloadInfo.setUrl( url+ "gamedata/excel/item_table.json");
                    downloadOneFile(downloadInfo);
                    downloadInfo.setFileName("runFile/download/skin_table.json");
                    downloadInfo.setUrl( url+ "gamedata/excel/skin_table.json");
                    downloadOneFile(downloadInfo);
                    downloadInfo.setFileName("runFile/download/battle_equip_table.json");
                    downloadInfo.setUrl( url+ "gamedata/excel/battle_equip_table.json");
                    downloadOneFile(downloadInfo);
                    downloadInfo.setFileName("runFile/download/uniequip_table.json");
                    downloadInfo.setUrl( url+ "gamedata/excel/uniequip_table.json");
                    downloadOneFile(downloadInfo);
                    downloadInfo.setFileName("runFile/download/enemy_database.json");
                    downloadInfo.setUrl( url+ "gamedata/levels/enemydata/enemy_database.json");
                    downloadOneFile(downloadInfo);
                    downloadInfo.setFileName("runFile/download/data_version.txt");
                    downloadInfo.setUrl( url+ "gamedata/excel/data_version.txt");
                    downloadOneFile(downloadInfo);
                    downloadInfo.setSecond(300);
                    downloadInfo.setFileName(null);
                    downloadInfo.setUrl(null);

                    log.info("数据文件下载完成");
                    //updateMapper.doneUpdateVersion();
                    updateStatus = 0;
                } catch (IOException e) {
                    log.error("下载数据文件失败");
                    log.error(e.toString());
                }
            }
            updateAllData(downloadInfo);
            return true;
        } else if (updateStatus == 1) {
            log.warn("数据文件正在下载中，无法重复下载，请等待文件下载完成");
            return false;
        } else {
            log.warn("数据库正在写入数据中，请等待更新完成");
            return false;
        }
    }

    private void downloadOneFile(DownloadOneFileInfo downloadInfo) throws IOException {
        File file = new File(downloadInfo.getFileName());
        if (file.exists()) {
            log.info("{}文件已存在，无需下载", downloadInfo.getFileName());
            return;
        }
        URL u = new URL(downloadInfo.getUrl());
        HttpURLConnection httpUrl;
        if (downloadInfo.isUseHost()) {
            Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(downloadInfo.getHostname(), downloadInfo.getPort()));
            httpUrl = (HttpURLConnection) u.openConnection(proxy);
        }else {
             httpUrl = (HttpURLConnection) u.openConnection();
        }
        //5分钟超时时间
        httpUrl.setConnectTimeout(downloadInfo.getSecond()*1000);
        httpUrl.setReadTimeout(downloadInfo.getSecond()*1000);

        httpUrl.connect();
        try (InputStream is = httpUrl.getInputStream();FileOutputStream fs = new FileOutputStream(downloadInfo.getFileName())){
            byte[] buffer = new byte[1024];
            int len;
            while ((len = is.read(buffer)) != -1) {
                fs.write(buffer, 0, len);
            }
            log.info("下载{}文件成功", downloadInfo.getFileName());
        } catch (IOException e) {
            log.error("下载{}文件失败", downloadInfo.getFileName());
            log.error(e.toString());
            throw e;
        }
        httpUrl.disconnect();
    }

    public void updateAllData(DownloadOneFileInfo downloadOneFileInfo) {
        String charKey = getJsonStringFromFile("data_version.txt");
        String dataVersion = updateMapper.getVersion();
        if (dataVersion == null) updateMapper.insertVersion();//如果不存在，手动更新一个0出来避免后续无法更新数据库的版本号
        //Integer versionStatus = updateMapper.getVersionStatus();
        try {

            if (updateStatus == 0) {
                if (!charKey.equals(dataVersion)) {
                    log.info("数据库和数据文件版本不同，开始更新全部数据");
                    updateStatus = 2;
                    //updateMapper.doingUpdateVersion();
                    //清理干员数据(因部分召唤物无char_id，不方便进行增量更新)
                    log.info("清理干员数据");
                    updateMapper.clearOperatorData();
                    updateAllOperator();
                    updateAllEnemy();
                    updateMapAndItem();
//                updateSkin(downloadOneFileInfo);
                    updateItemIcon(downloadOneFileInfo);
                    updateOperatorPng(downloadOneFileInfo);
                    updateOperatorSkillPng(downloadOneFileInfo);
//                updateOperatorVoice(downloadOneFileInfo);
                    updateMapper.updateVersion(charKey);
                    updateStatus = 0;
                    //updateMapper.doneUpdateVersion();
                    log.info("游戏数据更新完成--");
                } else {
                    log.info("数据库和数据文件版本相同，无需更新");
                }
            } else if (updateStatus == 1) {
                log.info("数据文件正在下载中，无法重复下载，请等待文件下载完成");
            } else {
                log.warn("数据库正在写入数据中，请等待更新完成");
            }
        } catch (JSONException e) {
            updateStatus = 0;
            throw e;
        }
    }

    /**
     * 全量更新干员相关信息
     */
    public void updateAllOperator() {
        //获取全部干员基础数据
        JSONObject operatorObj = new JSONObject(getJsonStringFromFile("character_table.json"));
        //获取游戏公招描述数据
        log.info("更新公招描述数据");
        String recruit = new JSONObject(getJsonStringFromFile("gacha_table.json")).getString("recruitDetail");
        Pattern pattern = Pattern.compile("<(.*?)>");
        Matcher matcher = pattern.matcher(recruit);
        String replaceAll = matcher.replaceAll("").replace(" ","");
        String[] split = replaceAll.split("\n");
        //解析出全部的公招干员
        List<String> gachaCharList = new ArrayList<>();
        for (String s : split) {
            if (s.startsWith("★")) {
                String[] chars = s.replace("★", "").replace("\\n", "").split("/");
                gachaCharList.addAll(Arrays.asList(chars));
            }
        }
        //获取全部干员技能数据
        log.info("更新全部干员技能数据");
        JSONObject skillObj = new JSONObject(getJsonStringFromFile("skill_table.json"));
        //获取全部基建技能数据
        log.info("更新全部基建技能数据");
        JSONObject buildingObj = new JSONObject(getJsonStringFromFile("building_data.json"));
        //获取全部干员档案数据
        log.info("更新全部干员档案数据");
        JSONObject infoTableObj = new JSONObject(getJsonStringFromFile("handbook_info_table.json")).getJSONObject("handbookDict");
        //获取配音演员档案数据
        JSONObject CVNameObj = new JSONObject(getJsonStringFromFile("charword_table.json")).getJSONObject("voiceLangDict");
        log.info("更新全部干员基础数据");
        Iterator<String> keys = operatorObj.keys();
        while (keys.hasNext()){
            String charId = keys.next();
            JSONObject operator = operatorObj.getJSONObject(charId);

            String name = operator.getString("name").trim();
            // 判断干员名是否存在公招描述中
            if (gachaCharList.contains(name)) {
                updateOperatorTag(operator);
            }

            Integer operatorId = updateOperatorByJson(charId, operator, skillObj, buildingObj);

            if (infoTableObj.has(charId)) {
                JSONObject jsonObject = infoTableObj.getJSONObject(charId);
                JSONObject jsonObject1 = CVNameObj.getJSONObject(charId);
                updateOperatorInfoById(charId, operatorId, jsonObject);
                updateDubberInfoById(operatorId,jsonObject1);
            }
        }
        //更新模组信息
        updateOperatorEquipByJson();

        //近卫兔兔单独处理
        JSONObject amiya2Json = new JSONObject(getJsonStringFromFile("char_patch_table.json")).getJSONObject("patchChars").getJSONObject("char_1001_amiya2");
        Integer operatorId = updateOperatorByJson("char_1001_amiya2", amiya2Json, skillObj, buildingObj);
        JSONObject amiyaInfo = infoTableObj.getJSONObject("char_002_amiya");
        JSONObject amiyaCV = CVNameObj.getJSONObject("char_002_amiya");
        updateOperatorInfoById("char_1001_amiya2", operatorId, amiyaInfo);
        updateDubberInfoById(operatorId,amiyaCV);

        log.info("干员数据更新完成");
    }

    /**
     * 插入一条干员基础信息（档案、画师）
     *
     * @param charId  干员char_id
     * @param operatorId 数据库中的干员Id
     */
    private void updateOperatorInfoById(String charId, Integer operatorId, JSONObject infoJsonObj) {
        OperatorBasicInfo operatorBasicInfo = new OperatorBasicInfo();
        operatorBasicInfo.setOperatorId(operatorId);
        operatorBasicInfo.setCharId(charId);
        operatorBasicInfo.setDrawName(infoJsonObj.getString("drawName"));
        JSONArray storyTextAudio = infoJsonObj.getJSONArray("storyTextAudio");
        for (int i = 0; i < storyTextAudio.length(); i++) {
            JSONObject story = storyTextAudio.getJSONObject(i);
            String storyText = story.getJSONArray("stories").getJSONObject(0).getString("storyText");
            String storyTitle = story.getString("storyTitle");
            switch (storyTitle) {
                case "基础档案" : {
                    String[] split = storyText.split("\n");
                    int point = storyText.lastIndexOf("【矿石病感染情况】");
                    if(point != -1){
                        String infection = storyText.substring(point+9);
                        operatorBasicInfo.setInfection(infection);
                    }else {
                        int platformPoint = storyText.lastIndexOf("【维护检测报告】");
                        String infection = storyText.substring(platformPoint+8);
                        operatorBasicInfo.setInfection(infection);
                    }
                    for (String s : split) {
                        if (s.length() < 1) {
                            break;
                        }
                        String[] basicText = s.substring(1).split("】");
                        switch (basicText[0]) {
                            case "代号" :
                            case "型号" : operatorBasicInfo.setCodeName(basicText[1]);break;
                            case "性别" :
                            case "设定性别" : operatorBasicInfo.setSex(basicText[1]);break;
                            case "出身地":
                            case "产地" : operatorBasicInfo.setComeFrom(basicText[1]);break;
                            case "生日" :
                            case "出厂日" : operatorBasicInfo.setBirthday(basicText[1]);break;
                            case "种族" :
                            case "制造商" : operatorBasicInfo.setRace(basicText[1]);break;
                            case "身高" :
                            case "高度" : {
                                String str = basicText[1];
                                StringBuilder str2 = new StringBuilder();
                                if (str != null && !"".equals(str)) {
                                    for (int j = 0; j < str.length(); j++) {
                                        if (str.charAt(j) >= 48 && str.charAt(j) <= 57) {
                                            str2.append(str.charAt(j));
                                        }
                                    }
                                }
                                try {
                                    operatorBasicInfo.setHeight(Integer.parseInt(str2.toString()));
                                } catch (NumberFormatException e) {
                                    log.error("缺少身高数据");
                                }
                            }break;
                        }
                    }
                }break;
                case "综合体检测试" : operatorBasicInfo.setComprehensiveTest(storyText);break;
                case "客观履历" : operatorBasicInfo.setObjectiveResume(storyText);break;
                case "临床诊断分析" : operatorBasicInfo.setClinicalDiagnosis(storyText);break;
                case "档案资料一" : operatorBasicInfo.setArchives1(storyText);break;
                case "档案资料二" : operatorBasicInfo.setArchives2(storyText);break;
                case "档案资料三" : operatorBasicInfo.setArchives3(storyText);break;
                case "档案资料四" : operatorBasicInfo.setArchives4(storyText);break;
                case "晋升记录" :
                case "晋升资料" : operatorBasicInfo.setPromotionInfo(storyText);break;
            }
            updateMapper.updateOperatorInfo(operatorBasicInfo);
        }
    }

    /**
     *  插入干员配音信息
     *
     * @param operatorId 数据库中的干员Id
     * @param CVNameJsonObj 配音部分的JSON
     */
    private void updateDubberInfoById(Integer operatorId, JSONObject CVNameJsonObj){
        OperatorBasicInfo operatorBasicInfo = new OperatorBasicInfo();
        operatorBasicInfo.setOperatorId(operatorId);
        JSONObject dict = CVNameJsonObj.getJSONObject("dict");
        for (String area : dict.keySet()){
            JSONObject voiceLangType = dict.getJSONObject(area);
            switch (area) {
                case "CN_MANDARIN" : operatorBasicInfo.setCvNameOfCNMandarin(voiceLangType.getString("cvName").trim());break;
                case "CN_TOPOLECT" : operatorBasicInfo.setCvNameOfCNTopolect(voiceLangType.getString("cvName").trim());break;
                case "JP" : operatorBasicInfo.setCvNameOfJP(voiceLangType.getString("cvName").trim());break;
                case "KR" : operatorBasicInfo.setCvNameOfKR(voiceLangType.getString("cvName").trim());break;
                case "EN" : operatorBasicInfo.setCvNameOfEN(voiceLangType.getString("cvName").trim());break;
            }
        }
        updateMapper.updateCVNameByOperatorId(operatorBasicInfo);
    }

    /**
     * 获取干员的标签tag
     * @param operator 干员Json数据
     */
    private void updateOperatorTag(JSONObject operator) {
        String name = operator.getString("name");
        List<String> agentTagsInfos = agentTagsMapper.selectAgentNameAll();
        if (agentTagsInfos.contains(name)) {
            log.info("干员{}已有公招tag", name);
            return;
        }
        JSONArray tags = operator.getJSONArray("tagList");
        int rarity = operator.getInt("rarity") + 1;
        StringBuilder position = new StringBuilder(operator.getString("position").equals("MELEE") ? "近战位" : "远程位");

        for (int i = 0; i < tags.length(); i++) {
            position.append(",").append(tags.getString(i));
        }

        if (rarity == 5) {
            position.append(",资深干员");
        } else if (rarity == 6) {
            position.append(",高级资深干员");
        } else if (rarity == 1)
        {
            position.append(",支援机械");
        }

        String profession = operator.getString("profession");

        Map<String, String> operatorClass = new HashMap<>(8);
        operatorClass.put("PIONEER", "先锋干员");
        operatorClass.put("WARRIOR", "近卫干员");
        operatorClass.put("TANK", "重装干员");
        operatorClass.put("SNIPER", "狙击干员");
        operatorClass.put("CASTER", "术师干员");
        operatorClass.put("SUPPORT", "辅助干员");
        operatorClass.put("MEDIC", "医疗干员");
        operatorClass.put("SPECIAL", "特种干员");

        position.append(",").append(operatorClass.get(profession));

        updateMapper.updateTags(name, rarity, position.toString());
        log.info("{}干员tag信息更新成功", name);
    }

    /**
     * 增量更新敌人面板信息
     */
    public void updateAllEnemy() {
        log.info("开始更新敌人信息");
        //获取全部敌人数据
        JSONArray enemyObj = new JSONObject(getJsonStringFromFile("enemy_database.json")).getJSONArray("enemies");

        int length = 0;
        List<String> allEnemyId = enemyMapper.selectAllEnemyId();
        for (int i = 0; i < enemyObj.length(); i++) {
            String enemyId = enemyObj.getJSONObject(i).getString("Key");
            if (!allEnemyId.contains(enemyId)) {
                JSONObject oneEnemy = enemyObj.getJSONObject(i);
                JSONArray enemyJsonObj = oneEnemy.getJSONArray("Value");
                String name = enemyJsonObj.getJSONObject(0).getJSONObject("enemyData").getJSONObject("name").getString("m_value");
                for (int j = 0; j < enemyJsonObj.length(); j++) {
                    //一个敌人可能有多个阶段，比如我老婆霜星
                    JSONObject enemyData = enemyJsonObj.getJSONObject(j).getJSONObject("enemyData");
                    JSONObject attributes = enemyData.getJSONObject("attributes");
                    Integer atk = attributes.getJSONObject("atk").getInt("m_value");
                    Double baseAttackTime = attributes.getJSONObject("baseAttackTime").getDouble("m_value");
                    Integer def = attributes.getJSONObject("def").getInt("m_value");
                    Integer hpRecoveryPerSec = attributes.getJSONObject("hpRecoveryPerSec").getInt("m_value");
                    Integer magicResistance = attributes.getJSONObject("magicResistance").getInt("m_value");
                    Integer massLevel = attributes.getJSONObject("massLevel").getInt("m_value");
                    Integer maxHp = attributes.getJSONObject("maxHp").getInt("m_value");
                    Double moveSpeed = attributes.getJSONObject("moveSpeed").getDouble("m_value");
                    Double rangeRadius = enemyData.getJSONObject("rangeRadius").getDouble("m_value");
                    Integer silenceImmune = attributes.getJSONObject("silenceImmune").getBoolean("m_value") ? 0 : 1;
                    Integer sleepImmune = attributes.getJSONObject("sleepImmune").getBoolean("m_value") ? 0 : 1;
                    Integer stunImmune = attributes.getJSONObject("stunImmune").getBoolean("m_value") ? 0 : 1;

                    EnemyInfo enemyInfo = new EnemyInfo(enemyId, name, atk, baseAttackTime,
                            def, hpRecoveryPerSec, magicResistance, massLevel, maxHp,
                            moveSpeed, rangeRadius, silenceImmune, sleepImmune, stunImmune, j);

                    updateMapper.updateEnemy(enemyInfo);
                    length++;
                }
            }
        }
        log.info("敌人信息更新完成，共更新了{}个敌人信息", length);
    }

    /**
     * 更新地图、材料基础信息
     */
    public void updateMapAndItem() {

        log.info("从企鹅物流中拉取地图、材料数据");
        //地图列表
        String mapListUrl = "https://penguin-stats.cn/PenguinStats/api/v2/stages?server=CN";

        MapJson[] maps = restTemplate
                .getForObject(mapListUrl, MapJson[].class);
        int newMap = 0;
        for (MapJson map : maps) {
            List<String> mapIds = materialMadeMapper.selectAllMapId();
            if (!mapIds.contains(map.getStageId())) {
                updateMapper.updateStageData(map);
                newMap++;
            }
        }

        log.info("新增地图{}个", newMap);

        //章节列表
        String zoneListUrl = "https://penguin-stats.cn/PenguinStats/api/v2/zones";

        int newZone = 0;
        ZoneJson[] zones = restTemplate.getForObject(zoneListUrl, ZoneJson[].class);
        for (ZoneJson zone : zones) {
            List<String> zoneIds = materialMadeMapper.selectAllZoneId();
            if (!zoneIds.contains(zone.getZoneId())) {
                updateMapper.updateZoneData(zone);
                newZone++;
            }
        }

        log.info("新增章节{}个", newZone);

        updateItemAndFormula();

        //地图掉落关联表
        String matrixListUrl = "https://penguin-stats.cn/PenguinStats/api/v2/_private/result/matrix/CN/global";

        //全量更新所有掉落信息
        clearMatrixData();
        String matrixJsonStr = restTemplate.getForObject(matrixListUrl, String.class);
        JSONArray matrixJsons = new JSONObject(matrixJsonStr).getJSONArray("matrix");
        int length = matrixJsons.length();
        for (int i = 0; i < length; i++) {
            JSONObject matrix = matrixJsons.getJSONObject(i);
            try {
                String stageId = matrix.getString("stageId");
                Integer itemId = Integer.parseInt(matrix.getString("itemId"));
                Integer quantity = matrix.getInt("quantity");
                Integer times = matrix.getInt("times");
                updateMapper.updateMatrixData(stageId, itemId, quantity, times);
            } catch (NumberFormatException e) {
                //忽略家具材料
            }
        }
        log.info("企鹅物流数据更新完成--");
    }

    /**
     * 增量更新材料以及合成公式
     */
    public void updateItemAndFormula() {
        //材料列表
        List<String> ids = materialMadeMapper.selectAllMaterId();
        String jsonStringFromUrl = getJsonStringFromFile("item_table.json");
        if (jsonStringFromUrl != null) {
            JSONObject items = new JSONObject(jsonStringFromUrl).getJSONObject("items");
            Iterator<String> keys = items.keys();
            int newItem = 0;
            while (keys.hasNext()) {
                String key = keys.next();
                JSONObject itemObj = items.getJSONObject(key);
                String id = itemObj.getString("itemId");
                //增量更新
                if (!ids.contains(id)) {
                    String name = itemObj.getString("name");
                    String icon = itemObj.getString("iconId");
                    updateMapper.updateItemData(id, name, icon);
                    //更新合成信息
                    updateItemFormula(id);
                    newItem++;
                }
            }
            log.info("材料合成数据更新完成--");
            log.info("新增材料{}个", newItem);
        }
    }

    /**
     * 根据材料Id获取合成公式
     *
     * @param itemId 材料Id
     */
    public void updateItemFormula(String itemId) {
        //根据材料id，更新材料合成公式
        JSONArray buildingProductList = new JSONObject(getJsonStringFromFile("item_table.json")).getJSONObject("items").getJSONObject(itemId).getJSONArray("buildingProductList");
        if (buildingProductList != null && buildingProductList.length() > 0) {
            String roomType = buildingProductList.getJSONObject(0).getString("roomType");
            String formulaId = buildingProductList.getJSONObject(0).getString("formulaId");

            JSONArray formulaObj;
            if (roomType.equals("WORKSHOP")) {
                formulaObj = new JSONObject(getJsonStringFromFile("building_data.json")).getJSONObject("workshopFormulas").getJSONObject(formulaId).getJSONArray("costs");
                for (int i = 0; i < formulaObj.length(); i++) {
                    updateMapper.insertMaterialMade(itemId
                            , Integer.parseInt(formulaObj.getJSONObject(i).getString("id"))
                            , formulaObj.getJSONObject(i).getInt("count"));
                }
            } else if (roomType.equals("MANUFACTURE")) {
                formulaObj = new JSONObject(getJsonStringFromFile("building_data.json")).getJSONObject("manufactFormulas").getJSONObject(formulaId).getJSONArray("costs");
                for (int i = 0; i < formulaObj.length(); i++) {
                    updateMapper.insertMaterialMade(itemId
                            , Integer.parseInt(formulaObj.getJSONObject(i).getString("id"))
                            , formulaObj.getJSONObject(i).getInt("count"));
                }
            }
            log.info("{}材料合成信息更新成功", itemId);
        }
    }

    /**
     * 增量更新皮肤信息
     */
    public void updateSkin(DownloadOneFileInfo downloadInfo) {
        log.info("拉取时装数据");
        JSONObject skinJson = new JSONObject(getJsonStringFromFile("skin_table.json")).getJSONObject("charSkins");
        //皮肤只需要增量更新
        List<String> skinNames = skinInfoMapper.selectAllNames();

        Iterator<String> keys = skinJson.keys();
        while (keys.hasNext()) {
            JSONObject skinObj = skinJson.getJSONObject(keys.next());
            if(skinObj.getJSONObject("displaySkin").get("skinName") instanceof String) {
                String name = skinObj.getJSONObject("displaySkin").getString("skinName");
                if (!skinNames.contains(name)) {
                    log.info("新增时装：" + name);
                    SkinInfo skinInfo = new SkinInfo();
                    skinInfo.setSkinName(name);
                    skinInfo.setDialog(skinObj.getJSONObject("displaySkin").getString("dialog"));
                    skinInfo.setDrawerName(skinObj.getJSONObject("displaySkin").getString("drawerName"));
                    skinInfo.setOperatorId(skinObj.getString("charId"));
                    skinInfo.setSkinGroupName(skinObj.getJSONObject("displaySkin").getString("skinGroupName"));
                    String avatarId = skinObj.getString("avatarId");
                    String[] split = avatarId.split("#");
                    try {
                        String fileName = "runFile/skin/" + split[0] + "_" + split[1] + ".png";
                        downloadInfo.setSecond(300);
                        downloadInfo.setFileName(fileName);
                        downloadInfo.setUrl(url + "skin/" + split[0] + "_" + split[1] + ".png");
                        downloadOneFile(downloadInfo);
                        downloadInfo.setFileName(null);
                        downloadInfo.setUrl(null);
                        //downloadOneFile(fileName, url + "skin/" + split[0] + "_" + split[1] + ".png");
                        skinInfo.setSkinBase64(fileName);
                        skinInfoMapper.insertBySkinInfo(skinInfo);
                    } catch (IOException e) {
                        log.error("下载{}时装失败", name);
                    }
                }
            }
        }
        log.info("原有时装{}个，当前时装{}个", skinNames.size(), skinJson.length());
        log.info("时装数据更新完成--");
    }

    /**
     * 更新材料图标，以材料表为基础update，只更新非base64的字段
     */
    public void updateItemIcon(DownloadOneFileInfo downloadInfo) {
        log.info("开始拉取最新材料图标");
        List<String> maters = materialMadeMapper.selectAllMaterId();
        for (String id : maters) {
            String picBase64 = materialMadeMapper.selectMaterialPicById(id);
            if (picBase64 == null) {
                String iconId = materialMadeMapper.selectAllMaterIconId(id);
                try {
                    String fileName = "runFile/itemIcon/" + iconId + ".png";
                    downloadInfo.setSecond(300);
                    downloadInfo.setFileName(fileName);
                    downloadInfo.setUrl(url + "item/" + iconId + ".png");
                    downloadOneFile(downloadInfo);
                    downloadInfo.setFileName(null);
                    downloadInfo.setUrl(null);
                    materialMadeMapper.updateBase64ById(fileName, id);
                } catch (IOException e) {
                    log.error("下载{}材料图标失败", id);
                }
            }
        }
        log.info("材料图标更新完成--");
    }

    /**
     * 更新干员半身照，增量更新
     */
    public void updateOperatorPng(DownloadOneFileInfo downloadInfo) {
        log.info("开始更新干员半身照");
        List<String> allOperatorId = operatorInfoMapper.getAllOperatorId();
        for (String id : allOperatorId) {
            String base = operatorInfoMapper.selectOperatorPngById(id);
            if (base == null) {
                log.info(id + "半身照正在更新");
                try {
                    String fileName = "runFile/operatorPng/" + id + "_1.png";
                    downloadInfo.setSecond(300);
                    downloadInfo.setFileName(fileName);
                    downloadInfo.setUrl(url + "portrait/" + id + "_1.png");
                    downloadOneFile(downloadInfo);
                    downloadInfo.setFileName(null);
                    downloadInfo.setUrl(null);
                    operatorInfoMapper.insertOperatorPngById(id, fileName);
                } catch (IOException e) {
                    log.error("下载{}干员半身照失败", id);
                }
            }
            String avatar = operatorInfoMapper.selectOperatorAvatarPngById(id);
            if (avatar == null) {
                log.info(id + "头像正在更新");
                try {
                    String avatarFile = "runFile/avatar/" + id + ".png";
                    downloadInfo.setSecond(300);
                    downloadInfo.setFileName(avatarFile);
                    downloadInfo.setUrl(url + "avatar/" + id + ".png");
                    downloadOneFile(downloadInfo);
                    downloadInfo.setFileName(null);
                    downloadInfo.setUrl(null);
                    operatorInfoMapper.insertOperatorAvatarPngById(id, avatarFile);
                } catch (IOException e) {
                    log.error("下载{}干员头像失败", id);
                }
            }
        }
        log.info("干员半身照更新完成--");
    }

    /**
     * 更新干员技能图标
     */
    public void updateOperatorSkillPng(DownloadOneFileInfo downloadInfo) {
        log.info("开始更新干员技能图标");
        List<SkillInfo> skillInfo = skillDescMapper.selectAllSkillPng();
        for (SkillInfo skill : skillInfo) {
            String png = skill.getSkillPng();
            if (png == null) {
                log.info(skill.getSkillName() + "技能图标正在更新");
                try {
                    String fileName = "runFile/skill/skill_icon_" + skill.getSkillIdYj() + ".png";
                    downloadInfo.setSecond(300);
                    downloadInfo.setFileName(fileName);
                    downloadInfo.setUrl(url + "skill/skill_icon_" + skill.getSkillIdYj() + ".png");
                    downloadOneFile(downloadInfo);
                    downloadInfo.setFileName(null);
                    downloadInfo.setUrl(null);
                    operatorInfoMapper.insertOperatorSkillPngById(skill.getSkillIdYj(), fileName);
                } catch (IOException e) {
                    log.error("下载{}干员技能图标失败", skill.getSkillName());
                }
            }
        }
        log.info("干员技能图标更新完成--");
    }

    /**
     * 更新干员语音，增量更新
     */
    public void updateOperatorVoice(DownloadOneFileInfo downloadInfo) {
        log.info("开始更新干员语音");

        //原配
        downloadVoiceByType("voice", downloadInfo);
        //中配
        downloadVoiceByType("voice_cn", downloadInfo);
        //方言
        downloadVoiceByType("voice_custom", downloadInfo);
        //英语
        downloadVoiceByType("voice_en", downloadInfo);
        //傻逼棒子话
        downloadVoiceByType("voice_kr", downloadInfo);

        log.info("更新干员语音完成--");
    }

    private void downloadVoiceByType(String type, DownloadOneFileInfo downloadInfo) {
        String area;

        switch (type) {
            case "voice_cn":
                area = "CN_mandarin";
                break;
            case "voice_custom":
                area = "CN_topolect";
                break;
            case "voice_en":
                area = "EN";
                break;
            case "voice_kr":
                area = "KR";
                break;
            default:
                area = "JP";
                break;
        }

        List<OperatorName> allOperatorId = operatorInfoMapper.getAllOperatorIdAndNameAndCV(area);
        String url = "https://static.prts.wiki/" + type + "/";
        for (OperatorName name : allOperatorId) {
            String voiceCharId = name.getCharId();
            if (type.equals("voice_custom")) {
                voiceCharId = name.getCharId() + "_cn_topolect";
            }
            File file = new File("runFile/" + type + "/" + voiceCharId);
            if (!file.exists()) {
                file.mkdirs();
            }
            for (String voiceName : VoiceService.voiceList) {
                //判断是否存在该语音
                if (operatorInfoMapper.selectOperatorVoiceByCharIdAndName(type, name.getCharId(), voiceName) == 0) {
                    if (!"近卫阿米娅".equals(name.getOperatorName())) {
                        String path = voiceCharId + "/" + name.getOperatorName() + "_" + voiceName + ".wav";
                        try {
                            downloadInfo.setSecond(300);
                            String filePath = "runFile/" + type + "/" + path;
                            downloadInfo.setFileName(filePath);
                            downloadInfo.setUrl(url + path);
                            downloadOneFile(downloadInfo);
                            downloadInfo.setFileName(null);
                            downloadInfo.setUrl(null);
                            //写入数据库
                            operatorInfoMapper.insertOperatorVoice(name.getCharId(), type, voiceName, filePath);
                            Thread.sleep(new Random().nextInt(5) * 1000);
                        } catch (IOException e) {
                            log.error("下载{}类型{}语音失败", type, name.getCharId() + "/" + voiceName);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    /**
     * 发送url的get请求获取结果json字符串
     * @param url url
     * @return 返回结果String
     */
    public String getJsonStringFromUrl(String url) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("User-Agent", "PostmanRuntime/7.26.8");
        httpHeaders.set("Authorization", "2");
        httpHeaders.set("Host", "andata.somedata.top");
        HttpEntity<String> httpEntity = new HttpEntity<>(httpHeaders);
        String s = null;
        try {
            s = restTemplate
                    .exchange(url, HttpMethod.GET, httpEntity, String.class).getBody();
        } catch (Exception ignored) {

        }
        return s;
    }

    /**
     * 读取文件的内容字符串
     * @param fileName url
     * @return 返回结果String
     */
    public String getJsonStringFromFile(String fileName) {
        File file = new File("runFile/download/" + fileName);
        StringBuilder laststr = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))){
            // System.out.println("以行为单位读取文件内容，一次读一整行：");
            String tempString;
            // 一次读入一行，直到读入null为文件结束
            while ((tempString = reader.readLine()) != null) {
                laststr.append(tempString);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return laststr.toString();
    }

    /**
     * 更新单个干员详细信息。包括技能天赋
     *
     * @param jsonObj 单个干员详细json
     * @return 返回更新数量
     */
    public Integer updateOperatorByJson(String charId, JSONObject jsonObj, JSONObject skillObj, JSONObject buildingObj) {
        Integer id = operatorInfoMapper.getOperatorIdByChar(charId);
        if (id != null) {
            log.info("干员{}已存在", charId);
            return id;
        }
        Map<String, Integer> operatorClass = new HashMap<>(8);
        operatorClass.put("PIONEER", 1);
        operatorClass.put("WARRIOR", 2);
        operatorClass.put("TANK", 3);
        operatorClass.put("SNIPER", 4);
        operatorClass.put("CASTER", 5);
        operatorClass.put("SUPPORT", 6);
        operatorClass.put("MEDIC", 7);
        operatorClass.put("SPECIAL", 8);

        String name = jsonObj.getString("name");
        //近卫兔兔改个名
        if (jsonObj.getJSONArray("phases").getJSONObject(0).getString("characterPrefabKey").equals("char_1001_amiya2")) {
            name = "近卫阿米娅";
        }
        int rarity = jsonObj.getInt("rarity") + 1;
        boolean isNotObtainable = jsonObj.getBoolean("isNotObtainable");

        //封装干员信息
        OperatorInfo operatorInfo = new OperatorInfo();
        operatorInfo.setOperator_name(name.trim());
        operatorInfo.setOperator_rarity(rarity);
        if (!isNotObtainable) {
            operatorInfo.setAvailable(1);
        } else {
            operatorInfo.setAvailable(0);
        }
        operatorInfo.setIn_limit(0);
        operatorInfo.setOperator_class(operatorClass.get(jsonObj.getString("profession")));

        updateMapper.insertOperator(operatorInfo);
        log.info("更新干员{}基本信息成功", name);
        Integer operatorId = updateMapper.selectOperatorIdByName(name);

        JSONArray phases = jsonObj.getJSONArray("phases");
        if (operatorId != null) {
            int length = phases.length();
            //封装干员面板信息（满级无潜能无信赖）
            JSONArray operatorPanel = phases.getJSONObject(length - 1).getJSONArray("attributesKeyFrames");
            JSONObject panelMax = operatorPanel.getJSONObject(operatorPanel.length() - 1).getJSONObject("data");
            OperatorData operatorData = new OperatorData();
            operatorData.setId(operatorId);
            operatorData.setAtk(panelMax.getInt("atk"));
            operatorData.setDef(panelMax.getInt("def"));
            operatorData.setMagicResistance(panelMax.getInt("magicResistance"));
            operatorData.setMaxHp(panelMax.getInt("maxHp"));
            operatorData.setBlockCnt(panelMax.getInt("blockCnt"));
            operatorData.setCost(panelMax.getInt("cost"));
            operatorData.setBaseAttackTime(panelMax.getDouble("baseAttackTime"));
            operatorData.setRespawnTime(panelMax.getInt("respawnTime"));
            updateMapper.updateOperatorData(operatorData);

            log.info("更新{}干员面板信息成功", name);
            //封装干员精英化花费
            for (int i = 1; i < length; i++) {
                JSONObject array = phases.getJSONObject(i);
                if (array.get("evolveCost") instanceof JSONArray) {
                    JSONArray evolveJson = array.getJSONArray("evolveCost");
                    for (int j = 0; j < evolveJson.length(); j++) {
                        JSONObject evolve = evolveJson.getJSONObject(j);
                        //精英i花费
                        OperatorEvolveInfo operatorEvolveInfo = new OperatorEvolveInfo();
                        operatorEvolveInfo.setOperatorId(operatorId);
                        operatorEvolveInfo.setEvolveLevel(i);
                        operatorEvolveInfo.setUseMaterialId(evolve.getInt("id"));
                        operatorEvolveInfo.setUseNumber(evolve.getInt("count"));
                        updateMapper.insertOperatorEvolve(operatorEvolveInfo);
                    }
                }
            }
            log.info("更新{}干员精英化材料成功", name);

            //封装干员天赋
            if (jsonObj.get("talents") instanceof JSONArray) {
                JSONArray talents = jsonObj.getJSONArray("talents");
                for (int i = 0; i < talents.length(); i++) {
                    JSONArray candidates = talents.getJSONObject(i).getJSONArray("candidates");
                    for (int j = 0; j < candidates.length(); j++) {
                        TalentInfo talentInfo = new TalentInfo();
                        JSONObject candidate = candidates.getJSONObject(j);
                        if (candidate.get("name") instanceof String) {
                            talentInfo.setTalentName(candidate.getString("name"));
                        }
                        Pattern pattern = Pattern.compile("<(.*?)>");
                        if (candidate.get("description") instanceof String) {
                            Matcher matcher = pattern.matcher(candidate.getString("description"));
                            talentInfo.setDescription(matcher.replaceAll(""));
                        }
                        talentInfo.setLevel(candidate.getJSONObject("unlockCondition").getInt("level"));
                        talentInfo.setPhase(candidate.getJSONObject("unlockCondition").getInt("phase"));
                        talentInfo.setPotential(candidate.getInt("requiredPotentialRank"));
                        talentInfo.setOperatorId(operatorId);
                        updateMapper.insertOperatorTalent(talentInfo);
                    }
                }
                log.info("更新{}干员天赋成功", name);
            }

            //封装干员技能
            JSONArray skills = jsonObj.getJSONArray("skills");
            for (int i = 0; i < skills.length(); i++) {
                OperatorSkillInfo operatorSkillInfo = new OperatorSkillInfo();
                operatorSkillInfo.setOperatorId(operatorId);
                operatorSkillInfo.setSkillIndex(i + 1);
                if (skills.getJSONObject(i).get("skillId") instanceof String) {
                    JSONObject skillJson = skillObj.getJSONObject(skills.getJSONObject(i).getString("skillId"));
                    String skillName = skillJson.getJSONArray("levels").getJSONObject(0).getString("name");
                    String skillIdYj = skills.getJSONObject(i).getString("skillId");
                    operatorSkillInfo.setSkillName(skillName);
                    if (skillJson.get("iconId") instanceof String) {
                        operatorSkillInfo.setSkillIdYj(skillJson.getString("iconId"));
                    } else {
                        Pattern skillIdPattern = Pattern.compile("\\[(0-9)\\]");
                        Matcher skillIdMatcher = skillIdPattern.matcher(skillIdYj);
                        operatorSkillInfo.setSkillIdYj(skillIdMatcher.replaceAll(""));
                    }
                    updateMapper.insertOperatorSkill(operatorSkillInfo);
                    Integer skillId = updateMapper.selectSkillIdByName(skillName);

                    JSONArray levels = skillJson.getJSONArray("levels");

                    for (int level = 0; level < levels.length(); level++) {
                        JSONObject skillDescJson = levels.getJSONObject(level);
                        SkillDesc skillDesc = new SkillDesc();
                        skillDesc.setSkillId(skillId);
                        skillDesc.setSkillLevel(level + 1);
                        skillDesc.setSkillType(skillDescJson.getInt("skillType"));

                        //获取key-value列表
                        Map<String, Double> parameters = new HashMap<>();
                        JSONArray mapList = skillDescJson.getJSONArray("blackboard");
                        for (int keyId = 0; keyId < mapList.length(); keyId++) {
                            parameters.put(mapList.getJSONObject(keyId).getString("key").toLowerCase(),
                                    mapList.getJSONObject(keyId).getDouble("value"));
                        }

                        skillDesc.setDescription(getValueByKeysFormatString(skillDescJson.getString("description"), parameters));

                        skillDesc.setSpType(skillDescJson.getJSONObject("spData").getInt("spType"));
                        skillDesc.setMaxCharge(skillDescJson.getJSONObject("spData").getInt("maxChargeTime"));
                        skillDesc.setSpCost(skillDescJson.getJSONObject("spData").getInt("spCost"));
                        skillDesc.setSpInit(skillDescJson.getJSONObject("spData").getInt("initSp"));
                        skillDesc.setDuration(skillDescJson.getInt("duration"));

                        updateMapper.updateSkillDecs(skillDesc);
                    }
                    log.info("更新{}干员技能{}信息成功", name, skillName);

                    //获取技能等级列表(专一专二专三)
                    JSONArray levelUpCostCond = skills.getJSONObject(i).getJSONArray("levelUpCostCond");
                    //该技能专j+1的花费
                    for (int j = 0; j < levelUpCostCond.length(); j++) {
                        JSONObject skillCostObj = levelUpCostCond.getJSONObject(j);
                        if (skillCostObj.get("levelUpCost") instanceof JSONArray) {
                            JSONArray levelUpCost = skillCostObj.getJSONArray("levelUpCost");
                            for (int k = 0; k < levelUpCost.length(); k++) {
                                SkillMaterInfo skillMaterInfo = new SkillMaterInfo();
                                skillMaterInfo.setSkillId(skillId);
                                skillMaterInfo.setMaterLevel(j + 1);
                                skillMaterInfo.setUseMaterialId(levelUpCost.getJSONObject(k).getInt("id"));
                                skillMaterInfo.setUseNumber(levelUpCost.getJSONObject(k).getInt("count"));
                                updateMapper.insertSkillMater(skillMaterInfo);
                            }
                        }
                    }
                    log.info("更新{}干员技能{}专精材料成功", name, skillName);
                }
            }

            //封装干员基建技能
            if (buildingObj.getJSONObject("chars").has(charId)) {
                JSONObject chars = buildingObj.getJSONObject("chars").getJSONObject(charId);
                JSONObject buffs = buildingObj.getJSONObject("buffs");
                if (chars.get("buffChar") instanceof JSONArray) {
                    JSONArray buildingData = chars.getJSONArray("buffChar");
                    for (int i = 0; i < buildingData.length(); i++) {
                        if (buildingData.getJSONObject(i).get("buffData") instanceof JSONArray) {
                            JSONArray build1 = buildingData.getJSONObject(i).getJSONArray("buffData");
                            for (int j = 0; j < build1.length(); j++) {
                                BuildingSkill buildingSkill = new BuildingSkill();
                                JSONObject buildObj = build1.getJSONObject(j);
                                String buffId = buildObj.getString("buffId");
                                buildingSkill.setOperatorId(operatorId);
                                buildingSkill.setPhase(buildObj.getJSONObject("cond").getInt("phase"));
                                buildingSkill.setLevel(buildObj.getJSONObject("cond").getInt("level"));
                                buildingSkill.setBuffName(buffs.getJSONObject(buffId).getString("buffName"));
                                buildingSkill.setRoomType(buffs.getJSONObject(buffId).getString("roomType"));
                                //正则表达式去除标签
                                Pattern pattern = Pattern.compile("<(.*?)>");
                                Matcher matcher = pattern.matcher(buffs.getJSONObject(buffId).getString("description"));
                                buildingSkill.setDescription(matcher.replaceAll(""));
                                buildingSkillMapper.insertBuildingSkill(buildingSkill);
                            }
                        }
                    }
                }
                log.info("更新{}干员基建技能成功", name);
            }
        }

        return operatorId;
    }

    public void updateOperatorEquipByJson(){
        log.info("开始更新模组数据");
        JSONObject equip = new JSONObject(getJsonStringFromFile("battle_equip_table.json"));
        JSONObject equipUnlock = new JSONObject(getJsonStringFromFile("uniequip_table.json"));

        List<String> equipId = equipMapper.selectAllEquipId();
        Iterator<String> keys = equip.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            if (!equipId.contains(key)) {
                EquipInfo equipInfo = new EquipInfo();

                JSONObject equipDict = equipUnlock.getJSONObject("equipDict").getJSONObject(key);
                equipInfo.setEquipId(equipDict.getString("uniEquipId"));
                equipInfo.setEquipName(equipDict.getString("uniEquipName"));
                equipInfo.setCharId(equipDict.getString("charId"));

                JSONArray phases = equip.getJSONObject(key).getJSONArray("phases");


                for (int i = 0; i < phases.length(); i++) {
                    equipInfo.setEquipLevel(i + 1);
                    JSONArray parts = phases.getJSONObject(i).getJSONArray("parts");

                    //天赋变化
                    StringBuilder additionalDescription = new StringBuilder();
                    StringBuilder overrideDescripton = new StringBuilder();

                    for (int j = 0; j < parts.length(); j++) {
                        JSONObject part = parts.getJSONObject(j);
                        JSONArray candidates;
                        switch (part.getString("target")) {
                            case "DISPLAY":
                                candidates = part.getJSONObject("overrideTraitDataBundle").getJSONArray("candidates");
                                for (int k = 0; k < candidates.length(); k++) {
                                    JSONObject candidate = candidates.getJSONObject(k);
                                    //获取key-value列表
                                    Map<String, Double> parameters = new HashMap<>();
                                    JSONArray mapList = candidate.getJSONArray("blackboard");
                                    for (int keyId = 0; keyId < mapList.length(); keyId++) {
                                        parameters.put(mapList.getJSONObject(keyId).getString("key").toLowerCase(),
                                                mapList.getJSONObject(keyId).getDouble("value"));
                                    }
                                    if (candidate.get("additionalDescription") instanceof String) {
                                        String additional = candidate.getString("additionalDescription");
                                        additionalDescription.append(getValueByKeysFormatString(additional, parameters));
                                    }
                                    if (candidate.get("overrideDescripton") instanceof String) {
                                        String override = candidate.getString("overrideDescripton");
                                        overrideDescripton.append(getValueByKeysFormatString(override, parameters));
                                    }
                                }
                                break;
                            case "TALENT_DATA_ONLY":
                            case "TALENT":
                                candidates = part.getJSONObject("addOrOverrideTalentDataBundle").getJSONArray("candidates");
                                for (int k = 0; k < candidates.length(); k++) {
                                    JSONObject candidate = candidates.getJSONObject(k);
                                    //获取key-value列表
                                    Map<String, Double> parameters = new HashMap<>();
                                    JSONArray mapList = candidate.getJSONArray("blackboard");
                                    for (int keyId = 0; keyId < mapList.length(); keyId++) {
                                        parameters.put(mapList.getJSONObject(keyId).getString("key").toLowerCase(),
                                                mapList.getJSONObject(keyId).getDouble("value"));
                                    }
                                    if (candidate.get("upgradeDescription") instanceof String) {
                                        String additional = candidate.getString("upgradeDescription");
                                        additionalDescription.append(getValueByKeysFormatString(additional, parameters));
                                    }
                                }
                                break;
                            case "TRAIT_DATA_ONLY":
                            case "TRAIT":
                                candidates = part.getJSONObject("overrideTraitDataBundle").getJSONArray("candidates");
                                for (int k = 0; k < candidates.length(); k++) {
                                    JSONObject candidate = candidates.getJSONObject(k);
                                    //获取key-value列表
                                    Map<String, Double> parameters = new HashMap<>();
                                    JSONArray mapList = candidate.getJSONArray("blackboard");
                                    for (int keyId = 0; keyId < mapList.length(); keyId++) {
                                        parameters.put(mapList.getJSONObject(keyId).getString("key").toLowerCase(),
                                                mapList.getJSONObject(keyId).getDouble("value"));
                                    }
                                    if (candidate.get("overrideDescripton") instanceof String) {
                                        String override = candidate.getString("overrideDescripton");
                                        overrideDescripton.append(getValueByKeysFormatString(override, parameters));
                                    }
                                }
                                break;
                        }
                    }

                    String addStr = additionalDescription.toString();
                    String overStr = overrideDescripton.toString();

                    if (addStr.equals("")) {
                        addStr = "无";
                    }
                    if (overStr.equals("")) {
                        overStr = "无";
                    }
                    String talentDesc = addStr + "|||" + overStr;
                    equipInfo.setDesc(talentDesc);
                    equipInfo.setLevel(parts.getJSONObject(0).
                            getJSONObject("overrideTraitDataBundle").getJSONArray("candidates").getJSONObject(0).getJSONObject("unlockCondition").getInt("level"));
                    equipInfo.setPhase(parts.getJSONObject(0).
                            getJSONObject("overrideTraitDataBundle").getJSONArray("candidates").getJSONObject(0).getJSONObject("unlockCondition").getInt("phase"));
                    equipMapper.insertEquipInfo(equipInfo);


                    JSONArray buffs = phases.getJSONObject(i).getJSONArray("attributeBlackboard");
                    for (int j = 0; j < buffs.length(); j++) {
                        String buffKey = buffs.getJSONObject(j).getString("key");
                        Double value = buffs.getJSONObject(j).getDouble("value");
                        equipMapper.insertEquipBuff(key, buffKey, value, i + 1);
                    }
                }

                JSONObject itemCost = equipDict.getJSONObject("itemCost");
                Iterator<String> keys1 = itemCost.keys();
                while (keys1.hasNext()) {
                    String level = keys1.next();
                    JSONArray cost = itemCost.getJSONArray(level);
                    for (int i = 0; i < cost.length(); i++) {
                        String materialId = cost.getJSONObject(i).getString("id");
                        Integer useNumber = cost.getJSONObject(i).getInt("count");
                        equipMapper.insertEquipCost(key, materialId, useNumber, Integer.parseInt(level));
                    }
                }

                JSONArray missionList = equipDict.getJSONArray("missionList");
                for (int i = 0; i < missionList.length(); i++) {
                    String missionId = missionList.getString(i);
                    String desc = equipUnlock.getJSONObject("missionList").getJSONObject(missionId).getString("desc");
                    equipMapper.insertEquipMission(key, missionId, desc);
                }
                log.info("{}模组信息更新成功", key);
            } else {
                log.info("已有{}模组信息", key);
            }
        }
        log.info("模组数据更新完毕");
    }

    public String getValueByKeysFormatString(String s, Map<String, Double> parameters){
        //使用正则表达式替换参数
        //代码可以运行不要乱改.jpg
        //这个正则已经不断进化成我看不懂的形式了
        Pattern pattern = Pattern.compile("<(.*?)>");
        Matcher matcher = pattern.matcher(s);
        Pattern p = Pattern.compile("(\\{-?([a-zA-Z/.\\]\\[0-9_@]+):?([0-9.]*)(%?)\\})");
        Matcher m = p.matcher(matcher.replaceAll(""));
        StringBuffer stringBuffer = new StringBuffer();

        while (m.find()) {
            String buffKey = m.group(2).toLowerCase();
            String percent = m.group(4);

            Double val = parameters.get(buffKey);
            String value;
            if (!percent.equals("")) {
                value = BigDecimal.valueOf(val).multiply(new BigDecimal(100)).toString() + "%";
            } else {
                value = FormatStringUtil.FormatDouble2String(val);
            }
            m.appendReplacement(stringBuffer, value);
        }
        return m.appendTail(stringBuffer).toString().replace("--", "-");
    }

    private void rebuildDatabase() {
        File file = new File("runFile/arknights.db");
        file.delete();
        try (InputStream is = new ClassPathResource("/database/arknights.db").getInputStream(); FileOutputStream fs = new FileOutputStream(file)) {
            boolean newFile = file.createNewFile();
            byte[] b = new byte[1024];
            while (is.read(b) != -1) {
                fs.write(b);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void clearMatrixData() {
        Integer integer = updateMapper.selectMatrixCount();
        for (int i = 0; i <= integer / 100; i++) {
            updateMapper.clearMatrixData();
        }
    }

    private void rebuildDir() {
        File dir = new File("runFile/download");
        File skin = new File("runFile/skin");

        File voice = new File("runFile/voice");
        File voice_cn = new File("runFile/voice_cn");
        File voice_custom = new File("runFile/voice_custom");
        File voice_en = new File("runFile/voice_en");
        File voice_kr = new File("runFile/voice_kr");

        File operatorPng = new File("runFile/operatorPng");
        File itemIcon = new File("runFile/itemIcon");
        File avatar = new File("runFile/avatar");
        File skill = new File("runFile/skill");
        mkOneDir(skin, voice, voice_cn, voice_custom, voice_en, voice_kr, operatorPng, itemIcon, dir, skill, avatar);
        for (File f : Objects.requireNonNull(dir.listFiles())) {
            f.delete();
        }
    }

    private void mkOneDir(File... files) {
        for (File f: files)
            if (!f.exists()) {
                f.mkdirs();
            }
    }
}
