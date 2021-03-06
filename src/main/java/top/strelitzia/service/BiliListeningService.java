package top.strelitzia.service;

import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.contact.MemberPermission;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import top.angelinaBot.annotation.AngelinaGroup;
import top.angelinaBot.model.MessageInfo;
import top.angelinaBot.model.ReplayInfo;
import top.angelinaBot.util.MiraiFrameUtil;
import top.angelinaBot.util.SendMessageUtil;
import top.strelitzia.dao.AdminUserMapper;
import top.strelitzia.dao.BiliMapper;
import top.strelitzia.dao.GroupAdminInfoMapper;
import top.strelitzia.dao.UserFoundMapper;
import top.strelitzia.model.BiliCount;
import top.strelitzia.model.DynamicDetail;
import top.strelitzia.util.AdminUtil;

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

    @Autowired
    private AdminUserMapper adminUserMapper;

    public boolean getDynamicList() {
        List<BiliCount> biliCountList = biliMapper.getBiliCountList();
        boolean b = false;
        for (BiliCount bili : biliCountList) {
            try {
                String biliSpace = "https://space.bilibili.com/" + bili.getUid() + "/dynamic";
                String dynamicList = "https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/space_history?host_uid=";
                String dynamicListUrl = "&offset_dynamic_id=0&need_top=";
                String topDynamic = restTemplate
                        .exchange(dynamicList + bili.getUid() + dynamicListUrl + 1, HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class).getBody();//1 -> ??????????????????
                //??????????????????json
                Long top = new JSONObject(topDynamic).getJSONObject("data").getJSONArray("cards").getJSONObject(0).getJSONObject("desc").getLong("dynamic_id");
                bili.setTop(top);
                //????????????????????????????????????
                String result;
                String url = dynamicList + bili.getUid() + dynamicListUrl + 0;//0 -> ?????????????????????
                HttpEntity<String> httpEntity = new HttpEntity<>(new HttpHeaders());
                String s = restTemplate
                        .exchange(url, HttpMethod.GET, httpEntity, String.class).getBody();
                JSONObject dynamicJson = new JSONObject(s);
                //??????????????????json
                JSONArray dynamics = dynamicJson.getJSONObject("data").getJSONArray("cards");
                //???????????????????????????
                if (dynamics.length() > 0) {
                    Long newId = dynamics.getJSONObject(0).getJSONObject("desc").getLong("dynamic_id");
                    //?????????????????????
                    Long first = bili.getFirst();
                    if (first == null || !first.equals(newId)) {
                        bili.setFirst(newId);
                        //????????????????????????
                        DynamicDetail newDetail = getDynamicDetail(newId);
                        String name = newDetail.getName();
                        bili.setName(name);
                        biliMapper.updateNewDynamic(bili);
                        result = name + "???????????????" + newDetail.getType() + "??????\n" +
                                newDetail.getTitle() + "\n" +
                                newDetail.getText() + "\n" + biliSpace;
                        log.info("{}????????????", name);
                        b = true;
                        List<Long> groups = userFoundMapper.selectCakeGroups(bili.getUid());
                        String pic = newDetail.getPicUrl();
                        ReplayInfo replayInfo = new ReplayInfo();
                        replayInfo.setReplayMessage(result);
                        if (pic != null) {
                            replayInfo.setReplayImg(pic);
                        }
                        for (Long groupId: groups) {
                            replayInfo.setGroupId(groupId);
                            replayInfo.setLoginQQ(MiraiFrameUtil.messageIdMap.get(groupId));
                            sendMessageUtil.sendGroupMsg(replayInfo);
                            Thread.sleep(new Random().nextInt(5) * 100);
                        }
                    }
                }
            }catch(Exception e){
                log.error(e.getMessage());
            }
        }
        return b;
    }

    public DynamicDetail getDynamicDetail(Long DynamicId) {
        DynamicDetail dynamicDetail = new DynamicDetail();
        //???????????????Json??????
        HttpEntity<String> httpEntity = new HttpEntity<>(new HttpHeaders());
        String dynamicDetailUrl = "https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/get_dynamic_detail?dynamic_id=";
        String s = restTemplate
                .exchange(dynamicDetailUrl + DynamicId, HttpMethod.GET, httpEntity, String.class).getBody();
        JSONObject detailJson = new JSONObject(s);
        int type = detailJson.getJSONObject("data").getJSONObject("card").getJSONObject("desc").getInt("type");
        String cardStr = detailJson.getJSONObject("data").getJSONObject("card").getString("card");
        JSONObject cardJson = new JSONObject(cardStr);
        String text = "";
        String name = detailJson.getJSONObject("data").getJSONObject("card").getJSONObject("desc").getJSONObject("user_profile").getJSONObject("info").getString("uname");
        String dType = "";
        String title = "";
        String pic = null;
        switch (type) {
            case 1:
                dType = "??????";
                title = "???????????????????????????????????????";
                text = cardJson.getJSONObject("item").getString("content");
                break;
            case 2://??????????????????
                dType = "??????";
                text = cardJson.getJSONObject("item").getString("description");
                pic = cardJson.getJSONObject("item").getJSONArray("pictures").getJSONObject(0).getString("img_src");
                break;
            case 64://????????????
                dType = "??????";
                title = cardJson.getString("title");
                text = "https://www.bilibili.com/read/cv" + cardJson.getLong("id");
                pic = cardJson.getJSONArray("image_urls").getString(0);
                break;
            case 4://??????????????????
                dType = "??????";
                text = cardJson.getJSONObject("item").getString("content");
                break;
            case 8://????????????
                dType = "??????";
                title = cardJson.getString("title");
                pic = cardJson.getString("pic");
                text = "https://www.bilibili.com/video/" + detailJson.getJSONObject("data").getJSONObject("card").getJSONObject("desc").getString("bvid");
                break;
            default:
                title = "?????????????????????????????????";
                break;
        }
        dynamicDetail.setName(name);
        dynamicDetail.setTitle(title);
        dynamicDetail.setType(dType);
        dynamicDetail.setPicUrl(pic);
        dynamicDetail.setText(text);

        return dynamicDetail;
    }

    @AngelinaGroup(keyWords = {"??????", "??????", "????????????", "????????????"}, description = "????????????up?????????????????????")
    public ReplayInfo getVideo(MessageInfo messageInfo) {
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
        if (messageInfo.getArgs().size() > 1) {
            BiliCount bili = biliMapper.getOneDynamicByName(messageInfo.getArgs().get(1));
            String videoUrl = "&pn=1&ps=1&jsonp=jsonp";
            String videoHead = "https://api.bilibili.com/x/space/arc/search?mid=";
            String newBvstr = restTemplate
                    .exchange(videoHead + bili.getUid() + videoUrl, HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class).getBody();
            JSONObject newBvJson = new JSONObject(newBvstr);
            String newBv = newBvJson.getJSONObject("data").getJSONObject("list").getJSONArray("vlist").getJSONObject(0).getString("bvid");
            replayInfo.setReplayMessage("https://www.bilibili.com/video/" + newBv);
        } else {
            replayInfo.setReplayMessage("??????????????????up?????????UID");
        }
        return replayInfo;
    }

    @AngelinaGroup(keyWords = {"??????", "B?????????", "????????????", "????????????"}, description = "????????????up???????????????")
    public ReplayInfo getDynamic(MessageInfo messageInfo) {
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
        if (messageInfo.getArgs().size() > 1) {
            BiliCount dynamics = biliMapper.getOneDynamicByName(messageInfo.getArgs().get(1));
            if (dynamics == null) {
                replayInfo.setReplayMessage("?????????????????????????????????????????????????????????");
            } else {
                DynamicDetail d = getDynamicDetail(dynamics.getFirst());
                replayInfo.setReplayMessage(d.getName() + "???" + d.getType() + "??????\n" + d.getTitle() + "\n" + d.getText());
                if (d.getPicUrl() != null) {
                    replayInfo.setReplayImg(d.getPicUrl());
                }
            }
        } else {
            replayInfo.setReplayMessage("??????????????????up?????????UID");
        }
        return replayInfo;
    }

    @AngelinaGroup(keyWords = {"????????????"}, description = "???????????????????????????UID")
    public ReplayInfo getBiliList(MessageInfo messageInfo) {
        List<BiliCount> bilis = biliMapper.getBiliCountListByGroupId(messageInfo.getGroupId());
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
        if (bilis.size() > 0) {
            StringBuilder s = new StringBuilder("");
            for (BiliCount bili : bilis) {
                s.append("\n?????????").append(bili.getName()).append("\tUid:").append(bili.getUid());
            }
            replayInfo.setReplayMessage(s.substring(1));
        }else {
            replayInfo.setReplayMessage("???????????????????????????up???~");
        }
        return replayInfo;
    }

    @AngelinaGroup(keyWords = {"??????"}, description = "????????????Uid")
    public ReplayInfo setGroupBiliRel(MessageInfo messageInfo) {
        Long groupId = messageInfo.getGroupId();
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);

        if (messageInfo.getArgs().size() > 1) {
            String biliId = messageInfo.getArgs().get(1);
            boolean sqlAdmin = AdminUtil.getSqlAdmin(messageInfo.getQq(), adminUserMapper.selectAllAdmin());
            if (messageInfo.getUserAdmin().equals(MemberPermission.MEMBER) && !sqlAdmin) {
                replayInfo.setReplayMessage("????????????????????????????????????????????????????????????");
            } else {
                Integer integer = groupAdminInfoMapper.existGroupId(groupId);
                if (integer == 0) {
                    groupAdminInfoMapper.insertGroupId(groupId);
                }
                Long uid = Long.parseLong(biliId);
                if (biliMapper.existBiliUid(uid) == 0) {
                    biliMapper.insertBiliUid(uid);
                }

                Integer relation = biliMapper.selectGroupBiliRel(groupId, uid);
                if (relation == 0) {
                    if (biliMapper.getBiliCountListByGroupId(groupId).size() > 5) {
                        replayInfo.setReplayMessage("??????????????????????????????5???");
                    } else {
                        biliMapper.insertGroupBiliRel(groupId, uid);
                        replayInfo.setReplayMessage("????????????");
                    }
                } else {
                    replayInfo.setReplayMessage("???????????????????????????uid");
                }
            }
        } else {
            replayInfo.setReplayMessage("???????????????Uid");
        }
        return replayInfo;
    }

    @AngelinaGroup(keyWords = {"????????????", "??????"}, description = "??????????????????Uid")
    public ReplayInfo removeGroupBiliRel(MessageInfo messageInfo) {
        Long groupId = messageInfo.getGroupId();
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);

        if (messageInfo.getArgs().size() > 1) {
            String biliId = messageInfo.getArgs().get(1);
            boolean sqlAdmin = AdminUtil.getSqlAdmin(messageInfo.getQq(), adminUserMapper.selectAllAdmin());
            if (messageInfo.getUserAdmin().equals(MemberPermission.MEMBER) && !sqlAdmin) {
                replayInfo.setReplayMessage("????????????????????????????????????????????????????????????");
            } else {
                Long uid = Long.parseLong(biliId);
                biliMapper.deleteGroupBiliRel(groupId, uid);
                replayInfo.setReplayMessage("??????????????????");
            }
        } else {
            replayInfo.setReplayMessage("????????????????????????Uid");
        }
        return replayInfo;
    }
}
