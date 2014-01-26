package cn.gd.gz.treemanz.toolbox.mailsender;

import java.io.PrintStream;

/**
 * @author Treeman
 */
public class ConnectionParams {
    private String protocol = "smtp";

    private String host = "allmfast.163.internal";

    private int port = 25;

    // 将出现在信头的return-path中(服务器认证用的用户名，退信也将发到这个Email)
    private String envelopeFrom = null;

    // 将出现在信头的received中
    private String heloName = null;

    // envelopeFrom的密码(如果服务器需要认证)
    private String password = null;

    // 服务器是否需要认证
    private boolean needAuth = false;

    // 是否将发送邮件过程中的debug信息输出到控制台中
    private boolean debug = false;

    // 连接超时（单位：毫秒）
    private long connectionTimeout = 5000;

    // Socket I/O超时（单位：毫秒）
    private long ioTimeout = 5000;

    private PrintStream debugOut = System.out;
    
    private String mailProvider = "163";

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getEnvelopeFrom() {
        return envelopeFrom;
    }

    /**
     * 设置envelopeFrom，默认为null<br/>
     * 该参数将出现在信头的return-path中，任何退信都将发送到该地址
     * 
     * @param envelopeFrom
     */
    public void setEnvelopeFrom(String envelopeFrom) {
        this.envelopeFrom = envelopeFrom;
    }

    /**
     * 设置HELO<br/>
     * 该参数将出现在信头的Received中
     * 
     * @param heloName
     */
    public String getHeloName() {
        return heloName;
    }

    public void setHeloName(String heloName) {
        this.heloName = heloName;
    }

    /**
     * 设置密码（若服务器需要认证）
     * 
     * @param password
     */
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isNeedAuth() {
        return needAuth;
    }

    /**
     * 服务器是否需要认证
     * 
     * @param needAuth
     */
    public void setNeedAuth(boolean needAuth) {
        this.needAuth = needAuth;
    }

    public boolean isDebug() {
        return debug;
    }

    /**
     * 设置是否输出调试信息
     * 
     * @param debug
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public PrintStream getDebugOut() {
        return debugOut;
    }

    /**
     * 设置调试信息输出的目的地，默认输出到控制台
     * 
     * @param debugOut
     */
    public void setDebugOut(PrintStream debugOut) {
        this.debugOut = debugOut;
    }

    public void setConnectionTimeout(long connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public long getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setIoTimeout(long ioTimeout) {
        this.ioTimeout = ioTimeout;
    }

    public long getIoTimeout() {
        return ioTimeout;
    }

    /**
     * @return the mailProvider
     */
    public String getMailProvider() {
        return mailProvider;
    }

    /**
     * @param mailProvider the mailProvider to set
     */
    public void setMailProvider(String mailProvider) {
        this.mailProvider = mailProvider;
    }
}
