package top.strelitzia.service;

import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.contact.MemberPermission;
import org.json.JSONArray;
import org.json.JSONException;
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
import top.strelitzia.model.BiliCount;
import top.strelitzia.model.DynamicDetail;
import top.strelitzia.util.AdminUtil;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
    private AdminUserMapper adminUserMapper;

    @Autowired
    private GroupAdminInfoMapper groupAdminInfoMapper;

    @Autowired
    private BiliMapper biliMapper;

    @Autowired
    private SendMessageUtil sendMessageUtil;



    @Autowired
    private MiraiFrameUtil miraiFrameUtil;

    private boolean doingBiliUpdate = false;

    public void getDynamicList() {
        if (!doingBiliUpdate) {
            doingBiliUpdate = true;
            List<BiliCount> biliCountList = biliMapper.getBiliCountList();
            Map<Long,Long> map = MiraiFrameUtil.messageIdMap;
            for (BiliCount bili : biliCountList) {
                String biliSpace = "https://space.bilibili.com/" + bili.getUid() + "/dynamic";
                String dynamicList = "https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/space_history?host_uid=";
                //String dynamicListUrl = "&offset_dynamic_id=0&need_top=";
                //String topDynamic = getStringByUrl(dynamicList + bili.getUid() + dynamicListUrl + 1);
                /*offset_dynamic_id为页码，need_top为获取置顶状态（1以置顶为顶，0按时间编排）
                 * 抓取置顶也难以区分出首条是设置的置顶还是最新动态，不再获取置顶
                 */
                String dynamic = getStringByUrl(dynamicList + bili.getUid());
                //解析动态列表json
                try {
                    //循环遍历每个被监听的账号
                    String result;
                    JSONObject dynamicJson = new JSONObject(dynamic);
                    //解析动态列表json
                    JSONArray dynamics = dynamicJson.getJSONObject("data").getJSONArray("cards");
                    //获取当前的最新动态
                    if (dynamics.length() > 0) {
                        //对比第一条动态的时间
                        Integer topTime = bili.getTopTime();
                        Integer timestamp = new JSONObject(dynamic).getJSONObject("data").getJSONArray("cards").getJSONObject(0).getJSONObject("desc").getInt("timestamp");
                        bili.setTopTime(timestamp);
                        String newId = dynamics.getJSONObject(0).getJSONObject("desc").getString("dynamic_id_str");
                        bili.setTop(newId);
                        if (topTime == null || topTime < timestamp) {
                            //获取最新动态详情
                            DynamicDetail newDetail = getDynamicDetail(newId);
                            String name = newDetail.getName();
                            bili.setName(name);
                            biliMapper.updateNewDynamic(bili);
                            result = name + "更新了一条" + newDetail.getType() + "动态\n" +
                                    newDetail.getTitle() + "\n" +
                                    newDetail.getText() + "\n" + biliSpace;
                            log.info("{}有新动态", name);
                            List<Long> groups = biliMapper.selectGroupByUid(bili.getUid());
                            for (Iterator<Long> it = groups.listIterator(); it.hasNext();){
                                Long group = it.next();
                                if(!map.containsKey(group)) {
                                    biliMapper.deleteGroupBiliRel(group,bili.getUid());
                                    it.remove();
                                }
                            }
                            String pic = newDetail.getPicUrl();
                            ReplayInfo replayInfo = new ReplayInfo();
                            replayInfo.setReplayMessage(result);
                            if (pic != null) {
                                replayInfo.setReplayImg(pic);
                            }
                            replayInfo.setGroupId(groups);
                            sendMessageUtil.sendGroupMsg(replayInfo);
                        }else if (topTime > timestamp){
                            log.error("{}首条动态时间减少，可能为删除或丢包，已忽略该变更", bili.getName());
                        }
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                }
            }
            doingBiliUpdate = false;
        } else {
            log.warn("无法重读读取B站更新");
        }
    }

    public DynamicDetail getDynamicDetail(String DynamicId) {
        DynamicDetail dynamicDetail = new DynamicDetail();
        //获取动态的Json消息
        //HttpEntity<String> httpEntity = new HttpEntity<>(new HttpHeaders());
        String dynamicDetailUrl = "https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/get_dynamic_detail?dynamic_id=";
        String s = getStringByUrl(dynamicDetailUrl + DynamicId);
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
                text = "https://www.bilibili.com/video/" + detailJson.getJSONObject("data").getJSONObject("card").getJSONObject("desc").getString("bvid");
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

    @AngelinaGroup(keyWords = {"投稿", "视频", "查看投稿", "最新视频"}, description = "查询某个up最新的投稿视频")
    public ReplayInfo getVideo(MessageInfo messageInfo) {
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
        if (messageInfo.getArgs().size() > 1) {
            BiliCount bili = biliMapper.getOneDynamicByName(messageInfo.getArgs().get(1));
            String videoUrl = "&pn=1&ps=1&jsonp=jsonp";
            String videoHead = "https://api.bilibili.com/x/space/arc/search?mid=";
            String newBvstr = getStringByUrl(videoHead + bili.getUid() + videoUrl);
            JSONObject newBvJson = new JSONObject(newBvstr);
            String newBv = newBvJson.getJSONObject("data").getJSONObject("list").getJSONArray("vlist").getJSONObject(0).getString("bvid");
            replayInfo.setReplayMessage("https://www.bilibili.com/video/" + newBv);
        } else {
            replayInfo.setReplayMessage("请输入查询的up名称或UID");
        }
        return replayInfo;
    }

    @AngelinaGroup(keyWords = {"动态", "B站动态", "查询动态", "查看动态"}, description = "查询某个up最新的动态")
    public ReplayInfo getDynamic(MessageInfo messageInfo) {
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
        if (messageInfo.getArgs().size() > 1) {
            BiliCount dynamics = biliMapper.getOneDynamicByName(messageInfo.getArgs().get(1));
            if (dynamics == null) {
                replayInfo.setReplayMessage("机器人尚未监听该账号，请联系管理员监听");
            } else {
                DynamicDetail d = getDynamicDetail(dynamics.getTop());
                replayInfo.setReplayMessage(d.getName() + "的" + d.getType() + "动态\n" + d.getTitle() + "\n" + d.getText());
                if (d.getPicUrl() != null) {
                    replayInfo.setReplayImg(d.getPicUrl());
                }
            }
        } else {
            replayInfo.setReplayMessage("请输入查询的up名称或UID");
        }
        return replayInfo;
    }

    @AngelinaGroup(keyWords = {"关注列表"}, description = "查看本群关注的所有UID")
    public ReplayInfo getBiliList(MessageInfo messageInfo) {
        List<BiliCount> bilis = biliMapper.getBiliCountListByGroupId(messageInfo.getGroupId());
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
        if (bilis.size() > 0) {
            StringBuilder s = new StringBuilder();
            for (BiliCount bili : bilis) {
                s.append("\n用户：").append(bili.getName()).append("\tUid:").append(bili.getUid());
            }
            replayInfo.setReplayMessage(s.substring(1));
        }else {
            replayInfo.setReplayMessage("本群暂时还没有关注up哦~");
        }
        return replayInfo;
    }

    @AngelinaGroup(keyWords = {"关注"}, description = "关注某个Uid")
    public ReplayInfo setGroupBiliRel(MessageInfo messageInfo) {
        Long groupId = messageInfo.getGroupId();
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);

        if (messageInfo.getArgs().size() > 1) {
            String biliId = messageInfo.getArgs().get(1);
            boolean sqlAdmin = AdminUtil.getSqlAdmin(messageInfo.getQq(), adminUserMapper.selectAllAdmin());
            if (messageInfo.getUserAdmin().equals(MemberPermission.MEMBER) && !sqlAdmin) {
                replayInfo.setReplayMessage("您不是本群管理员，无权进行本群的关注操作");
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
                        replayInfo.setReplayMessage("本群关注数已超过上限5个");
                    } else {
                        biliMapper.insertGroupBiliRel(groupId, uid);
                        replayInfo.setReplayMessage("关注成功");
                    }
                } else {
                    replayInfo.setReplayMessage("本群已经关注了这个uid");
                }
            }
        } else {
            replayInfo.setReplayMessage("请输入关注Uid");
        }
        return replayInfo;
    }

    @AngelinaGroup(keyWords = {"取消关注", "取关"}, description = "取消关注某个Uid")
    public ReplayInfo removeGroupBiliRel(MessageInfo messageInfo) {
        Long groupId = messageInfo.getGroupId();
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);

        if (messageInfo.getArgs().size() > 1) {
            String biliId = messageInfo.getArgs().get(1);
            boolean sqlAdmin = AdminUtil.getSqlAdmin(messageInfo.getQq(), adminUserMapper.selectAllAdmin());
            if (messageInfo.getUserAdmin().equals(MemberPermission.MEMBER) && !sqlAdmin) {
                replayInfo.setReplayMessage("您不是本群管理员，无权进行本群的关注操作");
            } else {
                Long uid = Long.parseLong(biliId);
                biliMapper.deleteGroupBiliRel(groupId, uid);
                replayInfo.setReplayMessage("取消关注成功");
            }
        } else {
            replayInfo.setReplayMessage("请输入需要取关的Uid");
        }
        return replayInfo;
    }

    public String getStringByUrl(String url) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/106.0.0.0 Safari/537.36");
        httpHeaders.set("cookie", "l=v; buvid3=626FC5CF-837D-619F-4E55-6D12087A5E2571622infoc; b_nut=1665411071; i-wanna-go-back=-1; _uuid=67AB95B2-FFE10-8368-2CB3-BBBE3453ADA386498infoc; buvid4=5D4DE9D7-8C19-5C42-44A2-D7A09963753472506-022101022-F5qpizBMaJqaij7UugK2Sw%3D%3D; buvid_fp_plain=undefined; DedeUserID=13794497; DedeUserID__ckMd5=a071de84de9f9543; nostalgia_conf=-1; b_ut=5; rpdid=|(u~km)k)m)u0J'uYYlRukR~m; CURRENT_BLACKGAP=0; fingerprint=20b5d1fa93b9198e6574ae1e9e4e22df; buvid_fp=15e1071d0aa0e44bc130d02de0a39be2; bp_video_offset_13794497=719507932525363300; PVID=5; CURRENT_FNVAL=16; innersign=0; b_lsid=FF10D64AB_183FDB6F736; SESSDATA=911a5f0f%2C1681960954%2C16491%2Aa2; bili_jct=a0860bd71d60e1912759fe8f88f2c7f5; sid=pcia4v68");
        return restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(httpHeaders), String.class).getBody();
    }
}