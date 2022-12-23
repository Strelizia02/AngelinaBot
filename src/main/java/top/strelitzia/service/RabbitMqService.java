package top.strelitzia.service;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.strelitzia.dao.AgentMapper;
import top.strelitzia.dao.NickNameMapper;
import top.strelitzia.model.AgentInfo;
import top.strelitzia.model.DownloadOneFileInfo;
import top.strelitzia.model.NickName;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class RabbitMqService {

    @Autowired
    private UpdateDataService updateDataService;

    @Autowired
    private AgentMapper agentMapper;

    @Autowired
    private NickNameMapper nickNameMapper;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(),
            exchange = @Exchange(value = "DataVersion",type = ExchangeTypes.FANOUT)
    ))
    public void getNewData() {
        //游戏数据的更新入口
        log.info("接收到游戏更新MQ");
        try {
            DownloadOneFileInfo downloadInfo = new DownloadOneFileInfo();
            downloadInfo.setUseHost(false);
            downloadInfo.setForce(false);
            updateDataService.downloadDataFile(downloadInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(),
            exchange = @Exchange(value = "PoolData",type = ExchangeTypes.FANOUT)
    ))
    public void getPoolData(String str) {
        //卡池数据更新入口
        log.info("开始更新抽卡卡池信息");
        try {
            List<AgentInfo> agentInfos = new ArrayList<>();
            JSONArray arr = new JSONArray(str);
            for (int i = 0; i < arr.length(); i++) {
                AgentInfo a = new AgentInfo();
                a.setPool(arr.getJSONObject(i).getString("pool"));
                a.setName(arr.getJSONObject(i).getString("name"));
                a.setLimit(arr.getJSONObject(i).getInt("limit"));
                a.setStar(arr.getJSONObject(i).getInt("star"));
                a.setVersion(arr.getJSONObject(i).getInt("version"));
                agentInfos.add(a);
            }

            agentMapper.insertAgentPool(agentInfos);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(),
            exchange = @Exchange(value = "NickName",type = ExchangeTypes.FANOUT)
    ))
    public void getNickNameData(String str) {
        //外号数据更新入口
        log.info("开始更新外号数据");
        try {
            List<NickName> nickNames = new ArrayList<>();
            JSONArray arr = new JSONArray(str);
            for (int i = 0; i < arr.length(); i++) {
                NickName n = new NickName();
                n.setNickName(arr.getJSONObject(i).getString("nickName"));
                n.setName(arr.getJSONObject(i).getString("name"));
                n.setVersion(arr.getJSONObject(i).getInt("version"));
                nickNames.add(n);
            }
            nickNameMapper.insertNickName(nickNames);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
