package top.strelitzia.model;

public class DownloadOneFileInfo {
    //是否全量更新
    private boolean force;
    //下载文件名
    private String fileName;
    //下载路径
    private String url;
    //超时判定
    private int second = 300;
    //是否使用代理
    private boolean useHost;
    //代理IP
    private String hostname;
    //代理端口
    private int port;

    public boolean isForce() { return force; }

    public void setForce(boolean force) { this.force = force; }

    public String getFileName() { return fileName; }

    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getUrl() { return url; }

    public void setUrl(String url) { this.url = url; }

    public int getSecond() { return second; }

    public void setSecond(int second) { this.second = second; }

    public boolean isUseHost() { return useHost; }

    public void setUseHost(boolean useHost) { this.useHost = useHost; }

    public String getHostname() { return hostname; }

    public void setHostname(String hostname) { this.hostname = hostname; }

    public int getPort() { return port; }

    public void setPort(int port) { this.port = port; }
}
