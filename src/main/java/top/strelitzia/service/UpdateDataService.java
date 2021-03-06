package top.strelitzia.service;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
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
import top.strelitzia.dao.*;
import top.strelitzia.model.*;
import top.strelitzia.util.AdminUtil;
import top.strelitzia.util.FormatStringUtil;
import top.strelitzia.util.XPathUtil;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
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
    private UserFoundMapper userFoundMapper;

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
//    private String url = "https://raw.githubusercontent.com/Kengxxiao/ArknightsGameData/master/zh_CN/gamedata/";
//    private String url = "http://vivien8261.gitee.io/arknights-bot-resource/gamedata/";
    private String url = "https://raw.fastgit.org/yuanyan3060/Arknights-Bot-Resource/master/";

    @AngelinaGroup(keyWords = {"??????"}, description = "??????????????????")
    @AngelinaFriend(keyWords = {"??????"}, description = "??????????????????")
    public ReplayInfo downloadDataFile(MessageInfo messageInfo) {
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
        List<AdminUserInfo> admins = adminUserMapper.selectAllAdmin();
        if (!AdminUtil.getSqlAdmin(messageInfo.getQq(), admins)) {
            replayInfo.setReplayMessage("??????????????????");
        } else {
            downloadDataFile(false);
//            updateOperatorVoice();
            replayInfo.setReplayMessage("???????????????????????????????????????????????????");
        }
        return replayInfo;
    }

    @AngelinaGroup(keyWords = {"????????????"}, description = "????????????????????????")
    @AngelinaFriend(keyWords = {"????????????"}, description = "????????????????????????")
    public ReplayInfo downloadDataFileForce(MessageInfo messageInfo) {
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
        List<AdminUserInfo> admins = adminUserMapper.selectAllAdmin();
        if (!AdminUtil.getSqlAdmin(messageInfo.getQq(), admins)) {
            replayInfo.setReplayMessage("??????????????????");
        } else {
            rebuildDatabase();
            downloadDataFile(true);
            replayInfo.setReplayMessage("???????????????????????????????????????????????????");
        }
        return replayInfo;
    }

    @AngelinaGroup(keyWords = {"????????????", "????????????", "????????????"}, description = "??????????????????")
    @AngelinaFriend(keyWords = {"????????????", "????????????", "????????????"}, description = "??????????????????")
    public ReplayInfo updateImgFile(MessageInfo messageInfo) {
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
        List<AdminUserInfo> admins = adminUserMapper.selectAllAdmin();
        if (!AdminUtil.getSqlAdmin(messageInfo.getQq(), admins)) {
            replayInfo.setReplayMessage("??????????????????");
        } else {
//            updateSkin();
            updateItemIcon();
            updateOperatorPng();
            updateOperatorSkillPng();
            replayInfo.setReplayMessage("??????????????????");
        }
        return replayInfo;
    }

    public void downloadDataFile(boolean force) {
        String koKoDaYoKeyUrl = url + "gamedata/excel/data_version.txt";
        String charKey = getJsonStringFromUrl(koKoDaYoKeyUrl);
        Integer versionStatus = updateMapper.getVersionStatus();
        File dataVersionFile = new File("runFile/download/data_version.txt");
        //??????????????????????????????
        if (versionStatus == 0) {
            boolean canDownload = true;
            //version???????????????????????????????????????
            if (dataVersionFile.exists()) {
                String dataVersion = getJsonStringFromFile("data_version.txt");
                //version????????????????????????version?????????????????????????????????
                if (dataVersion.replace("\n", "").equals(charKey.replace("\n", ""))) {
                    log.info("??????????????????????????????????????????????????????");
                    canDownload = false;
                } else {
                    log.info("??????????????????????????????????????????????????????");
                }
            } else {
                log.info("????????????????????????????????????");
            }
            if (canDownload || force) {
                File dir = new File("runFile/download");
                File skin = new File("runFile/skin");
                File voice = new File("runFile/voice");
                File operatorPng = new File("runFile/operatorPng");
                File itemIcon = new File("runFile/itemIcon");
                File avatar = new File("runFile/avatar");
                File skill = new File("runFile/skill");
                if (!skin.exists()) {
                    skin.mkdirs();
                }
                if (!voice.exists()) {
                    voice.mkdirs();
                }
                if (!operatorPng.exists()) {
                    operatorPng.mkdirs();
                }
                if (!itemIcon.exists()) {
                    itemIcon.mkdirs();
                }
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                if (!skill.exists()) {
                    skill.mkdirs();
                }
                if (!avatar.exists()) {
                    avatar.mkdirs();
                }
                for (File f : dir.listFiles()) {
                    f.delete();
                }
                try {
                    log.info("????????????????????????");
                    updateMapper.doingDownloadVersion();
                    downloadOneFile("runFile/download/character_table.json", url + "gamedata/excel/character_table.json");
                    downloadOneFile("runFile/download/gacha_table.json", url + "gamedata/excel/gacha_table.json");
                    downloadOneFile("runFile/download/skill_table.json", url + "gamedata/excel/skill_table.json");
                    downloadOneFile("runFile/download/building_data.json", url + "gamedata/excel/building_data.json");
                    downloadOneFile("runFile/download/handbook_info_table.json", url + "gamedata/excel/handbook_info_table.json");
                    downloadOneFile("runFile/download/char_patch_table.json", url + "gamedata/excel/char_patch_table.json");
                    downloadOneFile("runFile/download/item_table.json", url + "gamedata/excel/item_table.json");
                    downloadOneFile("runFile/download/skin_table.json", url + "gamedata/excel/skin_table.json");
                    downloadOneFile("runFile/download/battle_equip_table.json", url + "gamedata/excel/battle_equip_table.json");
                    downloadOneFile("runFile/download/uniequip_table.json", url + "gamedata/excel/uniequip_table.json");
                    downloadOneFile("runFile/download/enemy_database.json", url + "gamedata/levels/enemydata/enemy_database.json");
                    downloadOneFile("runFile/download/data_version.txt", url + "gamedata/excel/data_version.txt");
                    log.info("????????????????????????");
                    updateMapper.doneUpdateVersion();
                } catch (IOException e) {
                    log.error("????????????????????????");
                }
            }
            updateAllData();
        } else if (versionStatus == 1) {
            log.info("?????????????????????????????????????????????????????????????????????");
        } else {
            log.info("??????????????????????????????????????????????????????????????????????????????");
        }
    }

    private void downloadOneFile(String fileName, String url) throws IOException {
        URL u = new URL(url);
        HttpURLConnection httpUrl = (HttpURLConnection) u.openConnection();
        httpUrl.connect();
        try (InputStream is = httpUrl.getInputStream();FileOutputStream fs = new FileOutputStream(fileName)){
            byte[] buffer = new byte[1024];
            int len;
            while ((len = is.read(buffer)) != -1) {
                fs.write(buffer, 0, len);
            }
        }
        log.info("??????{}????????????", fileName);
        httpUrl.disconnect();
    }

    public void updateAllData() {
        String charKey = getJsonStringFromFile("data_version.txt");
        String dataVersion = updateMapper.getVersion();
        Integer versionStatus = updateMapper.getVersionStatus();

        if (versionStatus == 0) {
            if(!charKey.equals(dataVersion)) { 
                log.info("???????????????????????????????????????????????????????????????");
                updateMapper.doingUpdateVersion();
                //??????????????????(?????????????????????char_id??????????????????????????????)
                log.info("??????????????????");
                updateMapper.clearOperatorData();
                updateAllOperator();
                updateAllEnemy();
                updateMapAndItem();
//                updateSkin();
                updateItemIcon();
                updateOperatorPng();
                updateOperatorSkillPng();
//                updateOperatorVoice();
                updateMapper.updateVersion(charKey);
                updateMapper.doneUpdateVersion();
                log.info("????????????????????????--");
            } else {
                log.info("???????????????????????????????????????????????????");
            }
        } else if (versionStatus == 1) {
            log.info("?????????????????????????????????????????????????????????????????????");
        } else {
            log.info("??????????????????????????????????????????????????????????????????????????????");
        }
    }

    /**
     * ??????????????????????????????
     */
    public void updateAllOperator() {
        //??????????????????????????????
        JSONObject operatorObj = new JSONObject(getJsonStringFromFile("character_table.json"));
        //??????????????????????????????
        log.info("????????????????????????");
        String recruit = new JSONObject(getJsonStringFromFile("gacha_table.json")).getString("recruitDetail");
        Pattern pattern = Pattern.compile("<(.*?)>");
        Matcher matcher = pattern.matcher(recruit);
        String replaceAll = matcher.replaceAll("").replace(" ","");
        String[] split = replaceAll.split("\n");
        //??????????????????????????????
        List<String> gachaCharList = new ArrayList<>();
        for (String s : split) {
            if (s.startsWith("???")) {
                String[] chars = s.replace("???", "").replace("\\n", "").split("/");
                gachaCharList.addAll(Arrays.asList(chars));
            }
        }
        //??????????????????????????????
        log.info("??????????????????????????????");
        JSONObject skillObj = new JSONObject(getJsonStringFromFile("skill_table.json"));
        //??????????????????????????????
        log.info("??????????????????????????????");
        JSONObject buildingObj = new JSONObject(getJsonStringFromFile("building_data.json"));
        //??????????????????????????????
        log.info("??????????????????????????????");
        JSONObject infoTableObj = new JSONObject(getJsonStringFromFile("handbook_info_table.json")).getJSONObject("handbookDict");
        log.info("??????????????????????????????");
        Iterator<String> keys = operatorObj.keys();
        while (keys.hasNext()){
            String key = keys.next();
            JSONObject operator = operatorObj.getJSONObject(key);

            String name = operator.getString("name").trim();
            // ??????????????????????????????????????????
            if (gachaCharList.contains(name)) {
                updateOperatorTag(operator);
            }

            Integer operatorNum = updateOperatorByJson(key, operator, skillObj, buildingObj);

            if (infoTableObj.has(key)) {
                JSONObject jsonObject = infoTableObj.getJSONObject(key);
                updateOperatorInfoById(key, operatorNum, jsonObject);
            }
        }
        //??????????????????
        updateOperatorEquipByJson();

        //????????????????????????
        JSONObject amiya2Json = new JSONObject(getJsonStringFromFile("char_patch_table.json")).getJSONObject("patchChars").getJSONObject("char_1001_amiya2");
        Integer operatorNum = updateOperatorByJson("char_1001_amiya2", amiya2Json, skillObj, buildingObj);
        JSONObject amiyaInfo = infoTableObj.getJSONObject("char_002_amiya");
        updateOperatorInfoById("char_1001_amiya2", operatorNum, amiyaInfo);

        log.info("????????????????????????");
    }

    /**
     * ????????????????????????????????????????????????????????????
     *
     * @param operatorId  ??????char_id
     * @param operatorNum ?????????????????????Id
     */
    private void updateOperatorInfoById(String operatorId, Integer operatorNum, JSONObject infoJsonObj) {
        OperatorBasicInfo operatorBasicInfo = new OperatorBasicInfo();
            operatorBasicInfo.setOperatorId(operatorNum);
            operatorBasicInfo.setCharId(operatorId);
            operatorBasicInfo.setDrawName(infoJsonObj.getString("drawName"));
            operatorBasicInfo.setInfoName(infoJsonObj.getString("infoName"));

            JSONArray storyTextAudio = infoJsonObj.getJSONArray("storyTextAudio");
            for (int i = 0; i < storyTextAudio.length(); i++) {
                JSONObject story = storyTextAudio.getJSONObject(i);
                String storyText = story.getJSONArray("stories").getJSONObject(0).getString("storyText");
                String storyTitle = story.getString("storyTitle");
                switch (storyTitle) {
                    case "????????????":
                        String[] split = storyText.split("\n");
                        operatorBasicInfo.setInfection(split[split.length - 1]);
                        for (String s : split) {
                            if (s.length() < 1) {
                                break;
                            }
                            String[] basicText = s.substring(1).split("???");
                            switch (basicText[0]) {
                                case "??????":
                                    operatorBasicInfo.setCodeName(basicText[1]);
                                    break;
                                case "??????":
                                    operatorBasicInfo.setSex(basicText[1]);
                                    break;
                                case "?????????":
                                    operatorBasicInfo.setComeFrom(basicText[1]);
                                    break;
                                case "??????":
                                    operatorBasicInfo.setBirthday(basicText[1]);
                                    break;
                                case "??????":
                                    operatorBasicInfo.setRace(basicText[1]);
                                    break;
                                case "??????":
                                    String str = basicText[1];
                                    StringBuilder str2 = new StringBuilder();
                                    if (str != null && !"".equals(str)) {
                                        for (int j = 0; j < str.length(); j++) {
                                            if (str.charAt(j) >= 48 && str.charAt(j) <= 57) {
                                                str2.append(str.charAt(j));
                                            }
                                        }
                                    }
                                    operatorBasicInfo.setHeight(Integer.parseInt(str2.toString()));
                                    break;
                            }
                        }
                        break;
                    case "??????????????????":
                        operatorBasicInfo.setComprehensiveTest(storyText);
                        break;
                    case "????????????":
                        operatorBasicInfo.setObjectiveResume(storyText);
                        break;
                    case "??????????????????":
                        operatorBasicInfo.setClinicalDiagnosis(storyText);
                        break;
                    case "???????????????":
                        operatorBasicInfo.setArchives1(storyText);
                        break;
                    case "???????????????":
                        operatorBasicInfo.setArchives2(storyText);
                        break;
                    case "???????????????":
                        operatorBasicInfo.setArchives3(storyText);
                        break;
                    case "???????????????":
                        operatorBasicInfo.setArchives4(storyText);
                        break;
                    case "????????????":
                    case "????????????":
                        operatorBasicInfo.setPromotionInfo(storyText);
                        break;
                }
            updateMapper.updateOperatorInfo(operatorBasicInfo);
        }
    }

    /**
     * ?????????????????????tag
     * @param operator ??????Json??????
     */
    private void updateOperatorTag(JSONObject operator) {
        String name = operator.getString("name");
        List<String> agentTagsInfos = agentTagsMapper.selectAgentNameAll();
        if (agentTagsInfos.contains(name)) {
            log.info("??????{}????????????tag", name);
            return;
        }
        JSONArray tags = operator.getJSONArray("tagList");
        int rarity = operator.getInt("rarity") + 1;
        StringBuilder position = new StringBuilder(operator.getString("position").equals("MELEE") ? "?????????" : "?????????");

        for (int i = 0; i < tags.length(); i++) {
            position.append(",").append(tags.getString(i));
        }

        if (rarity == 5) {
            position.append(",????????????");
        } else if (rarity == 6) {
            position.append(",??????????????????");
        } else if (rarity == 1)
        {
            position.append(",????????????");
        }

        String profession = operator.getString("profession");

        Map<String, String> operatorClass = new HashMap<>(8);
        operatorClass.put("PIONEER", "????????????");
        operatorClass.put("WARRIOR", "????????????");
        operatorClass.put("TANK", "????????????");
        operatorClass.put("SNIPER", "????????????");
        operatorClass.put("CASTER", "????????????");
        operatorClass.put("SUPPORT", "????????????");
        operatorClass.put("MEDIC", "????????????");
        operatorClass.put("SPECIAL", "????????????");

        position.append(",").append(operatorClass.get(profession));

        updateMapper.updateTags(name, rarity, position.toString());
        log.info("{}??????tag??????????????????", name);
    }

    /**
     * ??????????????????????????????
     */
    public void updateAllEnemy() {
        log.info("????????????????????????");
        //????????????????????????
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
                    //?????????????????????????????????????????????????????????
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
        log.info("???????????????????????????????????????{}???????????????", length);
    }

    /**
     * ?????????????????????????????????
     */
    public void updateMapAndItem() {

        log.info("?????????????????????????????????????????????");
        //????????????
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

        log.info("????????????{}???", newMap);

        //????????????
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

        log.info("????????????{}???", newZone);

        updateItemAndFormula();

        //?????????????????????
        String matrixListUrl = "https://penguin-stats.cn/PenguinStats/api/v2/_private/result/matrix/CN/global";

        //??????????????????????????????
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
                //??????????????????
            }
        }
        log.info("??????????????????????????????--");
    }

    /**
     * ????????????????????????????????????
     */
    public void updateItemAndFormula() {
        //????????????
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
                //????????????
                if (!ids.contains(id)) {
                    String name = itemObj.getString("name");
                    String icon = itemObj.getString("iconId");
                    updateMapper.updateItemData(id, name, icon);
                    //??????????????????
                    updateItemFormula(id);
                    newItem++;
                }
            }
            log.info("??????????????????????????????--");
            log.info("????????????{}???", newItem);
        }
    }

    /**
     * ????????????Id??????????????????
     *
     * @param itemId ??????Id
     */
    public void updateItemFormula(String itemId) {
        //????????????id???????????????????????????
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
            log.info("{}??????????????????????????????", itemId);
        }
    }

    /**
     * ????????????????????????
     */
    public void updateSkin() {
        log.info("??????????????????");
        JSONObject skinJson = new JSONObject(getJsonStringFromFile("skin_table.json")).getJSONObject("charSkins");
        //???????????????????????????
        List<String> skinNames = skinInfoMapper.selectAllNames();

        Iterator<String> keys = skinJson.keys();
        while (keys.hasNext()) {
            JSONObject skinObj = skinJson.getJSONObject(keys.next());
            if(skinObj.getJSONObject("displaySkin").get("skinName") instanceof String) {
                String name = skinObj.getJSONObject("displaySkin").getString("skinName");
                if (!skinNames.contains(name)) {
                    log.info("???????????????" + name);
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
                        downloadOneFile(fileName, url + "skin/" + split[0] + "_" + split[1] + ".png");
                        skinInfo.setSkinBase64(fileName);
                        skinInfoMapper.insertBySkinInfo(skinInfo);
                    } catch (IOException e) {
                        log.error("??????{}????????????", name);
                    }
                }
            }
        }
        log.info("????????????{}??????????????????{}???", skinNames.size(), skinJson.length());
        log.info("????????????????????????--");
    }

    /**
     * ??????????????????????????????????????????update???????????????base64?????????
     */
    public void updateItemIcon() {
        log.info("??????????????????????????????");
        List<String> maters = materialMadeMapper.selectAllMaterId();
        for (String id : maters) {
            String picBase64 = materialMadeMapper.selectMaterialPicById(id);
            if (picBase64 == null) {
                String iconId = materialMadeMapper.selectAllMaterIconId(id);
                try {
                    String fileName = "runFile/itemIcon/" + iconId + ".png";
                    downloadOneFile(fileName, url + "item/" + iconId + ".png");
                    materialMadeMapper.updateBase64ById(fileName, id);
                } catch (IOException e) {
                    log.error("??????{}??????????????????", id);
                }
            }
        }
        log.info("????????????????????????--");
    }

    /**
     * ????????????????????????????????????
     */
    public void updateOperatorPng() {
        log.info("???????????????????????????");
        List<String> allOperatorId = operatorInfoMapper.getAllOperatorId();
        for (String id : allOperatorId) {
            String base = operatorInfoMapper.selectOperatorPngById(id);
            if (base == null) {
                log.info(id + "?????????????????????");
                try {
                    String fileName = "runFile/operatorPng/" + id + "_1.png";
                    downloadOneFile(fileName, url + "portrait/" + id + "_1.png");

                    operatorInfoMapper.insertOperatorPngById(id, fileName);
                } catch (IOException e) {
                    log.error("??????{}?????????????????????", id);
                }
            }
            String avatar = operatorInfoMapper.selectOperatorAvatarPngById(id);
            if (avatar == null) {
                log.info(id + "??????????????????");
                try {
                    String avatarFile = "runFile/avatar/" + id + ".png";
                    downloadOneFile(avatarFile, url + "avatar/" + id + ".png");
                    operatorInfoMapper.insertOperatorAvatarPngById(id, avatarFile);
                } catch (IOException e) {
                    log.error("??????{}??????????????????", id);
                }
            }
        }
        log.info("???????????????????????????--");
    }

    /**
     * ????????????????????????
     */
    public void updateOperatorSkillPng() {
        log.info("??????????????????????????????");
        List<SkillInfo> skillInfo = skillDescMapper.selectAllSkillPng();
        for (SkillInfo skill : skillInfo) {
            String png = skill.getSkillPng();
            if (png == null) {
                log.info(skill.getSkillName() + "????????????????????????");
                try {
                    String fileName = "runFile/skill/skill_icon_" + skill.getSkillIdYj() + ".png";
                    downloadOneFile(fileName, url + "skill/skill_icon_" + skill.getSkillIdYj() + ".png");
                    operatorInfoMapper.insertOperatorSkillPngById(skill.getSkillId(), fileName);
                } catch (IOException e) {
                    log.error("??????{}????????????????????????", skill.getSkillName());
                }
            }
        }
        log.info("??????????????????????????????--");
    }

    /**
     * ?????????????????????????????????
     */
    public void updateOperatorVoice() {
        log.info("????????????????????????");
        List<OperatorName> allOperatorId = operatorInfoMapper.getAllOperatorIdAndName();
        for (OperatorName name : allOperatorId) {
            String html = XPathUtil.getHtmlByUrl("https://prts.wiki/w/" + name.getOperatorName() + "/????????????");
            Document document = Jsoup.parse(html);
            Elements as = document.select("a[download]");
            for (Element a: as){
                String url = a.attr("href");
                String[] split = url.split("/");
                String fileName = XPathUtil.decodeUnicode(split[split.length - 1].substring(0,split[split.length - 1].length() - 4));
                String path = "runFile/voice/" + name.getCharId() + "/" + fileName + ".mp3";
                try {
                    downloadOneFile(path, url);
                } catch (IOException e) {
                    log.error("??????{}????????????", name.getCharId() + "/" + fileName);
                }
            }
        }
        log.info("????????????????????????--");
    }

    /**
     * ??????url???get??????????????????json?????????
     * @param url url
     * @return ????????????String
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
     * ??????????????????????????????
     * @param fileName url
     * @return ????????????String
     */
    public String getJsonStringFromFile(String fileName) {
        File file = new File("runFile/download/" + fileName);
        StringBuilder laststr = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))){
            // System.out.println("?????????????????????????????????????????????????????????");
            String tempString;
            // ?????????????????????????????????null???????????????
            while ((tempString = reader.readLine()) != null) {
                laststr.append(tempString);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return laststr.toString();
    }

    /**
     * ???????????????????????????????????????????????????
     *
     * @param jsonObj ??????????????????json
     * @return ??????????????????
     */
    public Integer updateOperatorByJson(String charId, JSONObject jsonObj, JSONObject skillObj, JSONObject buildingObj) {
        Integer id = operatorInfoMapper.getOperatorIdByChar(charId);
        if (id != null) {
            log.info("??????{}?????????", charId);
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
        //?????????????????????
        if (jsonObj.getJSONArray("phases").getJSONObject(0).getString("characterPrefabKey").equals("char_1001_amiya2")) {
            name = "???????????????";
        }
        int rarity = jsonObj.getInt("rarity") + 1;
        boolean isNotObtainable = jsonObj.getBoolean("isNotObtainable");

        //??????????????????
        OperatorInfo operatorInfo = new OperatorInfo();
        operatorInfo.setOperator_name(name);
        operatorInfo.setOperator_rarity(rarity);
        if (!isNotObtainable) {
            operatorInfo.setAvailable(1);
        } else {
            operatorInfo.setAvailable(0);
        }
        operatorInfo.setIn_limit(0);
        operatorInfo.setOperator_class(operatorClass.get(jsonObj.getString("profession")));

        updateMapper.insertOperator(operatorInfo);
        log.info("????????????{}??????????????????", name);
        Integer operatorId = updateMapper.selectOperatorIdByName(name);

        JSONArray phases = jsonObj.getJSONArray("phases");
        if (operatorId != null) {
            int length = phases.length();
            //??????????????????????????????????????????????????????
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

            log.info("??????{}????????????????????????", name);
            //???????????????????????????
            for (int i = 1; i < length; i++) {
                JSONObject array = phases.getJSONObject(i);
                if (array.get("evolveCost") instanceof JSONArray) {
                    JSONArray evolveJson = array.getJSONArray("evolveCost");
                    for (int j = 0; j < evolveJson.length(); j++) {
                        JSONObject evolve = evolveJson.getJSONObject(j);
                        //??????i??????
                        OperatorEvolveInfo operatorEvolveInfo = new OperatorEvolveInfo();
                        operatorEvolveInfo.setOperatorId(operatorId);
                        operatorEvolveInfo.setEvolveLevel(i);
                        operatorEvolveInfo.setUseMaterialId(evolve.getInt("id"));
                        operatorEvolveInfo.setUseNumber(evolve.getInt("count"));
                        updateMapper.insertOperatorEvolve(operatorEvolveInfo);
                    }
                }
            }
            log.info("??????{}???????????????????????????", name);

            //??????????????????
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
                log.info("??????{}??????????????????", name);
            }

            //??????????????????
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

                        //??????key-value??????
                        Map<String, Double> parameters = new HashMap<>();
                        JSONArray mapList = skillDescJson.getJSONArray("blackboard");
                        for (int keyId = 0; keyId < mapList.length(); keyId++) {
                            parameters.put(mapList.getJSONObject(keyId).getString("key").toLowerCase(),
                                    mapList.getJSONObject(keyId).getDouble("value"));
                        }

                        Pattern pattern = Pattern.compile("<(.*?)>");
                        Matcher matcher = pattern.matcher(skillDescJson.getString("description"));

                        //?????????????????????????????????
                        Pattern p = Pattern.compile("(\\{-?([a-zA-Z/.\\]\\[0-9_@]+):?([0-9.]*)(%?)\\})");
                        //??????????????????????????????.jpg
                        //?????????????????????????????????????????????????????????
                        Matcher m = p.matcher(matcher.replaceAll(""));
                        StringBuffer stringBuffer = new StringBuffer();

                        while (m.find()) {
                            String key = m.group(2).toLowerCase();
                            String percent = m.group(4);

                            Double val = parameters.get(key);
                            String value;

                            if (val != null) {
                                if (!percent.equals("")) {
                                    val = val * 100;
                                }
                                value = FormatStringUtil.FormatDouble2String(val) + percent;
                            } else {
                                try {
                                    value = "" + skillDescJson.getInt(key);
                                } catch (Exception e) {
                                    value = key;
                                }

                            }
                            m.appendReplacement(stringBuffer, value);
                        }

                        skillDesc.setDescription(m.appendTail(stringBuffer).toString().replace("--", "-"));

                        skillDesc.setSpType(skillDescJson.getJSONObject("spData").getInt("spType"));
                        skillDesc.setMaxCharge(skillDescJson.getJSONObject("spData").getInt("maxChargeTime"));
                        skillDesc.setSpCost(skillDescJson.getJSONObject("spData").getInt("spCost"));
                        skillDesc.setSpInit(skillDescJson.getJSONObject("spData").getInt("initSp"));
                        skillDesc.setDuration(skillDescJson.getInt("duration"));

                        updateMapper.updateSkillDecs(skillDesc);
                    }
                    log.info("??????{}????????????{}????????????", name, skillName);

                    //????????????????????????(??????????????????)
                    JSONArray levelUpCostCond = skills.getJSONObject(i).getJSONArray("levelUpCostCond");
                    //????????????j+1?????????
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
                    log.info("??????{}????????????{}??????????????????", name, skillName);
                }
            }

            //????????????????????????
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
                                //???????????????????????????
                                Pattern pattern = Pattern.compile("<(.*?)>");
                                Matcher matcher = pattern.matcher(buffs.getJSONObject(buffId).getString("description"));
                                buildingSkill.setDescription(matcher.replaceAll(""));
                                buildingSkillMapper.insertBuildingSkill(buildingSkill);
                            }
                        }
                    }
                }
                log.info("??????{}????????????????????????", name);
            }
        }

        return operatorId;
    }

    public void updateOperatorEquipByJson(){
        log.info("????????????????????????");
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

                    //????????????
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
                                    //??????key-value??????
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
                                candidates = part.getJSONObject("addOrOverrideTalentDataBundle").getJSONArray("candidates");
                                for (int k = 0; k < candidates.length(); k++) {
                                    JSONObject candidate = candidates.getJSONObject(k);
                                    //??????key-value??????
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
                                candidates = part.getJSONObject("overrideTraitDataBundle").getJSONArray("candidates");
                                for (int k = 0; k < candidates.length(); k++) {
                                    JSONObject candidate = candidates.getJSONObject(k);
                                    //??????key-value??????
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
                        addStr = "???";
                    }
                    if (overStr.equals("")) {
                        overStr = "???";
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
                log.info("{}????????????????????????", key);
            } else {
                log.info("??????{}????????????", key);
            }
        }
        log.info("????????????????????????");
    }

    public String getValueByKeysFormatString(String s, Map<String, Double> parameters){
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
                val = val * 100;
            }
            value = FormatStringUtil.FormatDouble2String(val) + percent;
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
}
