package top.strelitzia.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class RabbitMqService {

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(),
            exchange = @Exchange(value = "DataVersion",type = ExchangeTypes.FANOUT)
    ))
    public void getNewData(Message message) {
        //游戏数据的更新入口
        log.info("开始更新游戏数据");
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(),
            exchange = @Exchange(value = "PoolData",type = ExchangeTypes.FANOUT)
    ))
    public void getPoolData(Message message) {
        //卡池数据更新入口
        log.info("开始更新卡池信息");
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(),
            exchange = @Exchange(value = "NickName",type = ExchangeTypes.FANOUT)
    ))
    public void getNickNameData(Message message) {
        //外号数据更新入口
        log.info("开始更新外号数据");
    }
}
