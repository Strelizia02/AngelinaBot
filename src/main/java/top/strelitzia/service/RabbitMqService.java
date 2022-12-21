package top.strelitzia.service;

import lombok.extern.slf4j.Slf4j;
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
    public void getNewData(Message message) {
        //游戏数据的更新入口
        log.info("接收到游戏更新MQ");
        DownloadOneFileInfo downloadInfo = new DownloadOneFileInfo();
        downloadInfo.setUseHost(false);
        downloadInfo.setForce(false);
        updateDataService.downloadDataFile(downloadInfo);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(),
            exchange = @Exchange(value = "PoolData",type = ExchangeTypes.FANOUT)
    ))
    public void getPoolData(List<AgentInfo> agentInfos) {
        //卡池数据更新入口
        log.info("开始更新抽卡卡池信息");
        agentMapper.insertAgentPool(agentInfos);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(),
            exchange = @Exchange(value = "NickName",type = ExchangeTypes.FANOUT)
    ))
    public void getNickNameData(List<NickName> nickNames) {
        //外号数据更新入口
        log.info("开始更新外号数据");
        nickNameMapper.insertNickName(nickNames);
    }
}
