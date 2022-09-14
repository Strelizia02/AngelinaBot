package top.strelitzia.model;

public class HintsInfo {
    private HintsType hintsType;

    private String text;

    private String img;

    private String voice;

    private boolean isSendVoice;

    public HintsInfo() {

    }

    public HintsInfo(HintsType hintsType) {
        this.hintsType = hintsType;
    }

    public HintsType getHintsType() {
        return hintsType;
    }

    public void setHintsType(HintsType hintsType) {
        this.hintsType = hintsType;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getVoice() {
        return voice;
    }

    public void setVoice(String voice) {
        this.voice = voice;
    }

    public boolean getIsSendVoice() {
        return isSendVoice;
    }

    public void setSendVoice(boolean sendVoice) {
        isSendVoice = sendVoice;
    }
}
