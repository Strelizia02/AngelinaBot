package top.strelitzia.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.angelinaBot.model.Count;
import top.angelinaBot.service.SendDataService;
import top.strelitzia.dao.UserFoundMapper;

@Service
public class SendDataServiceImpl implements SendDataService {

    @Autowired
    private UserFoundMapper userFoundMapper;

    @Override
    public Count sendData() {
        Count count = new Count();
        count.setGroupCount(userFoundMapper.selectGroupCount());
        count.setQqCount(userFoundMapper.selectQqCount());
        return count;
    }
}
