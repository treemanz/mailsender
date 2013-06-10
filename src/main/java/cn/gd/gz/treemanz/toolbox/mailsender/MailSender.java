package cn.gd.gz.treemanz.toolbox.mailsender;

import java.util.Collection;

/**
 * @author Treeman
 */
public class MailSender {
    public static void send(ConnectionParams connectionParams,
            Collection<SendJob> sendJobs) {
        send(connectionParams, sendJobs, -1);
    }

    public static void send(Collection<SendJob> sendJobs) {
        ConnectionParams connectionParams = new ConnectionParams();
        send(connectionParams, sendJobs);
    }

    /**
     * @param connection
     * @param sendJobs
     * @param maxUsePerConnect
     *            一次连接最多可以发的邮件数
     */
    public static void send(ConnectionParams connectionParams,
            Collection<SendJob> sendJobs, int maxUsePerConnect) {
        if (null == connectionParams || null == sendJobs) {
            throw new IllegalArgumentException();
        }
        Connection connection = new Connection();
        connection.setConnectionParams(connectionParams);
        connection.connect();
        int used = 0;
        for (SendJob job: sendJobs) {
            if (maxUsePerConnect > 0) {
                if (used >= maxUsePerConnect) {
                    connection.close();
                    connection.connect();
                    used = 0;
                }
            }
            job.setConnection(connection);
            job.setConnectionKeepAlive(true);
            job.send();
            ++used;
        }
        connection.close();
    }
}
