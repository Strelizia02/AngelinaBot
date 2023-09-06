package top.strelitzia.service;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import top.angelinaBot.annotation.AngelinaGroup;
import top.angelinaBot.model.FunctionType;
import top.angelinaBot.model.MessageInfo;
import top.angelinaBot.model.PermissionEnum;
import top.angelinaBot.model.ReplayInfo;
import top.angelinaBot.util.SendMessageUtil;
import top.strelitzia.dao.BiliMapper;
import top.strelitzia.dao.GroupAdminInfoMapper;
import top.strelitzia.dao.UserFoundMapper;
import top.strelitzia.model.BiliCount;
import top.strelitzia.model.DynamicDetail;

import java.util.List;
import java.util.Random;

/**
 * @author strelitzia
 * @Date 2022/05/03 14:38
 **/
@Service
@Slf4j
public class BiliListeningService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private UserFoundMapper userFoundMapper;

    @Autowired
    private GroupAdminInfoMapper groupAdminInfoMapper;

    @Autowired
    private BiliMapper biliMapper;

    @Autowired
    private SendMessageUtil sendMessageUtil;

    private boolean doingBiliUpdate = false;

    public void getDynamicList() throws InterruptedException {
        if (doingBiliUpdate) {
            log.warn("无法重复读取B站更新");
            return;
        }

        doingBiliUpdate = true;
        List<BiliCount> biliCountList = biliMapper.getBiliCountList();
        for (BiliCount bili : biliCountList) {
            try {
                String biliSpace = "https://space.bilibili.com/" + bili.getUid() + "/dynamic";
                String dynamicList = "https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/space_history?host_uid=";
                String dynamicListUrl = "&offset_dynamic_id=0&need_top=";
                String topDynamic = getStringByUrl(dynamicList + bili.getUid() + dynamicListUrl + 1);//1 -> 抓取置顶动态
                //解析动态列表json
                Long top = new JSONObject(topDynamic).getJSONObject("data").getJSONArray("cards").getJSONObject(0).getJSONObject("desc").getLong("dynamic_id");
                bili.setTop(top);
                //循环遍历每个被监听的账号
                String result;
                String url = dynamicList + bili.getUid() + dynamicListUrl + 0;//0 -> 不抓取置顶动态
                String s = getStringByUrl(url);
                JSONObject dynamicJson = new JSONObject(s);
                //解析动态列表json
                JSONArray dynamics = dynamicJson.getJSONObject("data").getJSONArray("cards");
                //获取当前的最新动态
                if (dynamics.length() <= 0) {
                    log.error("{}账号无动态", bili.getUid());
                    continue;
                }

                JSONObject firstObject = dynamics.getJSONObject(0);
                Long newId = firstObject.getJSONObject("desc").getLong("dynamic_id");
                //对比第一条动态
                Long first = bili.getFirst();
                if (first != null && first.equals(newId)) {
                    log.error("{}账号动态未更新", bili.getUid());
                    continue;
                }

                bili.setFirst(newId);
                //获取最新动态详情
                DynamicDetail newDetail = getDynamicDetail(firstObject);
                String name = newDetail.getName();
                bili.setName(name);
                biliMapper.updateNewDynamic(bili);
                result = name + "更新了一条" + newDetail.getType() + "动态\n" +
                        newDetail.getTitle() + "\n" +
                        newDetail.getText() + "\n" + biliSpace;
                log.info("{}有新动态，正在推送至关注群中", name);
                List<String> groups = userFoundMapper.selectCakeGroups(bili.getUid());
                String pic = newDetail.getPicUrl();
                ReplayInfo replayInfo = new ReplayInfo();
                replayInfo.setReplayMessage(result);
                if (pic != null) {
                    replayInfo.setReplayImg(pic);
                }

                replayInfo.setGroupId(groups);
                sendMessageUtil.sendGroupMsg(replayInfo);
            } catch (Exception e) {
                log.error(e.getMessage());
            }
            Thread.sleep(new Random().nextInt(5) * 1000 + 1000);
        }
        doingBiliUpdate = false;
    }

    public DynamicDetail getDynamicDetail(JSONObject detailJson) {
        DynamicDetail dynamicDetail = new DynamicDetail();
        //获取动态的Json消息
//        HttpEntity<String> httpEntity = new HttpEntity<>(new HttpHeaders());

//        夭寿啦，叔叔把接口关啦！
//        String dynamicDetailUrl = "https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/get_dynamic_detail?dynamic_id=";
//        String s = getStringByUrl(dynamicDetailUrl + DynamicId);
//        JSONObject detailJson = new JSONObject(s);

        int type = detailJson.getJSONObject("desc").getInt("type");
        String cardStr = detailJson.getString("card");
        JSONObject cardJson = new JSONObject(cardStr);
        String text = "";
        String name = detailJson.getJSONObject("desc").getJSONObject("user_profile").getJSONObject("info").getString("uname");
        String dType = "";
        String title = "";
        String pic = null;
        switch (type) {
            case 1:
                dType = "转发";
                title = "请点击链接查看转发动态详情";
                text = cardJson.getJSONObject("item").getString("content");
                break;
            case 2://普通动态有图
                dType = "图文";
                text = cardJson.getJSONObject("item").getString("description");
                pic = cardJson.getJSONObject("item").getJSONArray("pictures").getJSONObject(0).getString("img_src");
                break;
            case 64://专栏动态
                dType = "专栏";
                title = cardJson.getString("title");
                text = "https://www.bilibili.com/read/cv" + cardJson.getLong("id");
                pic = cardJson.getJSONArray("image_urls").getString(0);
                break;
            case 4://普通动态无图
                dType = "文字";
                text = cardJson.getJSONObject("item").getString("content");
                break;
            case 8://视频动态
                dType = "视频";
                title = cardJson.getString("title");
                pic = cardJson.getString("pic");
                text = "https://www.bilibili.com/video/" + detailJson.getJSONObject("desc").getString("bvid");
                break;
            default:
                title = "请点击链接查看最新动态";
                break;
        }
        dynamicDetail.setName(name);
        dynamicDetail.setTitle(title);
        dynamicDetail.setType(dType);
        dynamicDetail.setPicUrl(pic);
        dynamicDetail.setText(text);

        return dynamicDetail;
    }

    @AngelinaGroup(keyWords = {"投稿", "视频", "查看投稿", "最新视频"}, description = "查询某个up最新的投稿视频", funcClass = FunctionType.BiliDynamic)
    public ReplayInfo getVideo(MessageInfo messageInfo) {
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
        if (messageInfo.getArgs().size() <= 1) {
            replayInfo.setReplayMessage("请输入查询的up名称或UID");
            return replayInfo;
        }
        BiliCount bili = biliMapper.getOneDynamicByName(messageInfo.getArgs().get(1));
        String videoUrl = "&pn=1&ps=1&jsonp=jsonp";
        String videoHead = "https://api.bilibili.com/x/space/arc/search?mid=";
        String newBvstr = getStringByUrl(videoHead + bili.getUid() + videoUrl);
        JSONObject newBvJson = new JSONObject(newBvstr);
        String newBv = newBvJson.getJSONObject("data").getJSONObject("list").getJSONArray("vlist").getJSONObject(0).getString("bvid");
        replayInfo.setReplayMessage("https://www.bilibili.com/video/" + newBv);
        return replayInfo;
    }

//    @AngelinaGroup(keyWords = {"动态", "B站动态", "查询动态", "查看动态"}, description = "查询某个up最新的动态", funcClass = FunctionType.BiliDynamic)
//    public ReplayInfo getDynamic(MessageInfo messageInfo) {
//        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
//        if (messageInfo.getArgs().size() <= 1) {
//            replayInfo.setReplayMessage("请输入查询的up名称或UID");
//            return replayInfo;
//        }
//
//        BiliCount dynamics = biliMapper.getOneDynamicByName(messageInfo.getArgs().get(1));
//        if (dynamics == null) {
//            replayInfo.setReplayMessage("机器人尚未监听该账号，请联系管理员监听");
//            return replayInfo;
//        }
//
//        DynamicDetail d = getDynamicDetail(dynamics.getFirst());
//        replayInfo.setReplayMessage(d.getName() + "的" + d.getType() + "动态\n" + d.getTitle() + "\n" + d.getText());
//        if (d.getPicUrl() != null) {
//            replayInfo.setReplayImg(d.getPicUrl());
//        }
//        return replayInfo;
//    }

    @AngelinaGroup(keyWords = {"关注列表"}, description = "查看本群关注的所有UID", funcClass = FunctionType.BiliDynamic)
    public ReplayInfo getBiliList(MessageInfo messageInfo) {
        List<BiliCount> bilis = biliMapper.getBiliCountListByGroupId(messageInfo.getGroupId());
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
        if (bilis.size() == 0) {
            replayInfo.setReplayMessage("本群暂时还没有关注up哦~");
            return replayInfo;
        }

        StringBuilder s = new StringBuilder();
        for (BiliCount bili : bilis) {
            s.append("\n用户：").append(bili.getName()).append("\tUid:").append(bili.getUid());
        }
        replayInfo.setReplayMessage(s.substring(1));
        return replayInfo;
    }

    @AngelinaGroup(keyWords = {"关注"}, description = "关注某个Uid", funcClass = FunctionType.BiliDynamic)
    public ReplayInfo setGroupBiliRel(MessageInfo messageInfo) {
        String groupId = messageInfo.getGroupId();
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);

        if (messageInfo.getArgs().size() <= 1) {
            replayInfo.setReplayMessage("请输入关注Uid");
            return replayInfo;
        }

        String biliId = messageInfo.getArgs().get(1);
        Integer integer = groupAdminInfoMapper.existGroupId(groupId);
        if (integer == 0) {
            groupAdminInfoMapper.insertGroupId(groupId);
        }
        Long uid = Long.parseLong(biliId);
        if (biliMapper.existBiliUid(uid) == 0) {
            replayInfo.setReplayMessage("只能关注bot已监听的账号");
            return replayInfo;
        }

        Integer relation = biliMapper.selectGroupBiliRel(groupId, uid);
        if (relation > 0) {
            replayInfo.setReplayMessage("本群已经关注了这个uid");
            return replayInfo;
        }

        if (biliMapper.getBiliCountListByGroupId(groupId).size() > 5) {
            replayInfo.setReplayMessage("本群关注数已超过上限5个");
            return replayInfo;
        }

        biliMapper.insertGroupBiliRel(groupId, uid);
        replayInfo.setReplayMessage("关注成功");
        return replayInfo;
    }

    @AngelinaGroup(keyWords = {"取消关注", "取关"}, description = "取消关注某个Uid", funcClass = FunctionType.BiliDynamic, permission = PermissionEnum.GroupAdministrator)
    public ReplayInfo removeGroupBiliRel(MessageInfo messageInfo) {
        String groupId = messageInfo.getGroupId();
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);

        if (messageInfo.getArgs().size() <= 1) {
            replayInfo.setReplayMessage("请输入需要取关的Uid");
            return replayInfo;
        }

        String biliId = messageInfo.getArgs().get(1);
        Long uid = Long.parseLong(biliId);
        biliMapper.deleteGroupBiliRel(groupId, uid);
        replayInfo.setReplayMessage("取消关注成功");
        return replayInfo;
    }

    @AngelinaGroup(keyWords = {"监听列表"}, description = "查看Bot监听的所有UID", funcClass = FunctionType.BiliDynamic)
    public ReplayInfo monitoredList(MessageInfo messageInfo) {
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
        List<BiliCount> bilis = biliMapper.getBiliCountList();

        StringBuilder s = new StringBuilder("以下是Bot正在监听的用户：");
        for (BiliCount bili : bilis) {
            s.append("\n用户：").append(bili.getName()).append("\tUid:").append(bili.getUid());
        }
        replayInfo.setReplayMessage(s.substring(1));
        return replayInfo;
    }

    @AngelinaGroup(keyWords = {"监听"}, description = "为Bot添加一个监听", funcClass = FunctionType.BiliDynamic, permission = PermissionEnum.Administrator)
    public ReplayInfo monitored(MessageInfo messageInfo) {
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
        if (messageInfo.getArgs().size() <= 1) {
            replayInfo.setReplayMessage("请输入需要监听的Uid");
            return replayInfo;
        }
        biliMapper.insertBiliUid(Long.parseLong(messageInfo.getArgs().get(1)));
        replayInfo.setReplayMessage("监听成功，若本群需要推送动态，请再关注该账号");
        return replayInfo;
    }

    @AngelinaGroup(keyWords = {"取消监听"}, description = "查看本群关注的所有UID", funcClass = FunctionType.BiliDynamic, permission = PermissionEnum.Administrator)
    public ReplayInfo stopMonitored(MessageInfo messageInfo) {
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
        if (messageInfo.getArgs().size() <= 1) {
            replayInfo.setReplayMessage("请输入需要取消监听的Uid");
            return replayInfo;
        }
        Long uid = Long.parseLong(messageInfo.getArgs().get(1));
        List<String> groupList = biliMapper.selectGroupByUid(uid);
        for (String groupId: groupList) {
            biliMapper.deleteGroupBiliRel(groupId, uid);
        }

        biliMapper.deleteUid(uid);
        replayInfo.setReplayMessage("取消监听成功，关注过该账号的群会自动取消关注");
        return replayInfo;
    }

    public String getStringByUrl(String url) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/106.0.0.0 Safari/537.36");
        httpHeaders.set("cookie", "l=v; buvid3=626FC5CF-837D-619F-4E55-6D12087A5E2571622infoc; b_nut=1665411071; i-wanna-go-back=-1; _uuid=67AB95B2-FFE10-8368-2CB3-BBBE3453ADA386498infoc; buvid4=5D4DE9D7-8C19-5C42-44A2-D7A09963753472506-022101022-F5qpizBMaJqaij7UugK2Sw%3D%3D; buvid_fp_plain=undefined; DedeUserID=13794497; DedeUserID__ckMd5=a071de84de9f9543; nostalgia_conf=-1; b_ut=5; rpdid=|(u~km)k)m)u0J'uYYlRukR~m; CURRENT_BLACKGAP=0; fingerprint=20b5d1fa93b9198e6574ae1e9e4e22df; buvid_fp=15e1071d0aa0e44bc130d02de0a39be2; bp_video_offset_13794497=719507932525363300; PVID=5; CURRENT_FNVAL=16; innersign=0; b_lsid=FF10D64AB_183FDB6F736; SESSDATA=911a5f0f%2C1681960954%2C16491%2Aa2; bili_jct=a0860bd71d60e1912759fe8f88f2c7f5; sid=pcia4v68");
        return restTemplate
                .exchange(url, HttpMethod.GET, new HttpEntity<>(httpHeaders), String.class).getBody();
    }
}
