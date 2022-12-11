package top.strelitzia.service;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import top.angelinaBot.annotation.AngelinaGroup;
import top.angelinaBot.container.AngelinaEventSource;
import top.angelinaBot.container.AngelinaListener;
import top.angelinaBot.model.FunctionType;
import top.angelinaBot.model.MessageInfo;
import top.angelinaBot.model.ReplayInfo;
import top.angelinaBot.util.SendMessageUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class OpenAIService {

    @Autowired
    private SendMessageUtil sendMessageUtil;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${openai}")
    private String token;


    @AngelinaGroup(keyWords = {"聊天", "ai", "对话"}, description = "openAI对话", funcClass = FunctionType.Others, author = "OpenAI")
    public ReplayInfo ToBeast(MessageInfo messageInfo) {
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
        String input;
        if (token.equals("")) {
            replayInfo.setReplayMessage("当前无合适的openai账号，无法进行对话");
            return replayInfo;
        }
        if (messageInfo.getArgs().size() > 1) {
            input = messageInfo.getArgs().get(1);
        } else {
            replayInfo.setReplayMessage("请在30秒内输入想要问我的内容：");
            sendMessageUtil.sendGroupMsg(replayInfo);
            AngelinaListener angelinaListener = new AngelinaListener() {
                @Override
                public boolean callback(MessageInfo message) {
                    return message.getGroupId().equals(messageInfo.getGroupId()) && message.getQq().equals(messageInfo.getQq());
                }
            };

            angelinaListener.setGroupId(messageInfo.getGroupId());
            MessageInfo recall = AngelinaEventSource.waiter(angelinaListener).getMessageInfo();
            if (recall == null) {
                replayInfo.setReplayMessage("本次聊天超时终止。");
                return replayInfo;
            }

            input = recall.getText();
        }

        if (input.length() > 50) {
            replayInfo.setReplayMessage("请输入小于50字");
            return replayInfo;
        }

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Authorization", "Bearer " + token);
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "text-davinci-003");
        requestBody.put("prompt", input);
        requestBody.put("temperature", 0);
        requestBody.put("max_tokens", 200);

        HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(requestBody, httpHeaders);

        String body = restTemplate.postForEntity("https://api.openai.com/v1/completions", httpEntity, String.class).getBody();

        if (body == null) {
            replayInfo.setReplayMessage("接口请求超时");
            return replayInfo;
        }
        String output = new JSONObject(body).getJSONArray("choices").getJSONObject(0).getString("text");
        replayInfo.setReplayMessage(output);
        return replayInfo;
    }
}
