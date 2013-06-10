package cn.gd.gz.treemanz.toolbox.mailsender;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.URLName;
import javax.mail.internet.MimeUtility;

/**
 * @author Treeman
 */
public final class Connection {
    private Session session;

    private Transport transport;

    private ConnectionParams connectionParams;

    private static final ConnectionParams DEFAULT_CONNECTION_PARAMS = new ConnectionParams();

    public void setConnectionParams(ConnectionParams connectionParams) {
        this.connectionParams = connectionParams;
    }

    public ConnectionParams getConnectionParams() {
        return connectionParams;
    }

    private Authenticator getAuthenticator() {
        if (connectionParams.isNeedAuth()) {
            return new SimpleAuthenticator(connectionParams.getEnvelopeFrom(),
                    connectionParams.getPassword());
        } else {
            return null;
        }
    }

    /**
     * 连接服务器
     * 
     * @throws ConnectionException
     */
    public void connect() {
        close();
        if (null == connectionParams) {
            connectionParams = DEFAULT_CONNECTION_PARAMS;
        }
        Properties properties = new Properties();
        // 设置服务器host
        properties.put("mail.smtp.host", connectionParams.getHost());
        // 设置HeloName和ElhoName
        try {
            properties.put("mail.smtp.localhost", MimeUtility
                    .encodeWord(connectionParams.getHeloName()));
        } catch (UnsupportedEncodingException e) {
            properties.put("mail.smtp.localhost", connectionParams
                    .getHeloName());
        }
        // 设置服务器是否需要认证
        properties.put("mail.smtp.auth", connectionParams.isNeedAuth());
        properties.put("mail.from", connectionParams.getEnvelopeFrom());
        properties.put("mail.smtp.connectiontimeout", connectionParams
                .getConnectionTimeout());
        properties.put("mail.smtp.timeout", connectionParams.getIoTimeout());

        session = Session.getInstance(properties, getAuthenticator());
        // 设置是否将发送邮件过程中的debug信息输出
        session.setDebug(connectionParams.isDebug());
        if (connectionParams.isDebug()
                && null != connectionParams.getDebugOut()) {
            session.setDebugOut(connectionParams.getDebugOut());
        }
        URLName urlName = new URLName(connectionParams.getProtocol(),
                connectionParams.getHost(), connectionParams.getPort(), null,
                connectionParams.getEnvelopeFrom(), connectionParams
                        .getPassword());
        // 连接服务器
        try {
            transport = session.getTransport(urlName);
            transport.connect();
        } catch (MessagingException e) {
            throw new ConnectionException(e);
        }
    }

    public boolean isConnected() {
        return (null != transport && transport.isConnected());
    }

    /**
     * 关闭连接
     */
    public void close() {
        if (isConnected()) {
            try {
                transport.close();
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        }
        transport = null;
        session = null;
    }

    public Session getSession() {
        return session;
    }

    public Transport getTransport() {
        return transport;
    }
}
