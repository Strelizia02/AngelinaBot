package top.strelitzia.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.strelitzia.service.UpdateDataService;

@Service
public class InitAngelinaService implements top.angelinaBot.service.InitAngelinaService {

    @Autowired
    private UpdateDataService updateDataService;

    @Override
    public void init() {
        updateDataService.downloadFile();
    }
}
