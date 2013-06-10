/**
 * @(#)PooledMailSender.java, 2010-8-11. Copyright 2010 Effiker, Inc. All rights
 *                            reserved. EFFIKER PROPRIETARY/CONFIDENTIAL. Use is
 *                            subject to license terms.
 */
package cn.gd.gz.treemanz.toolbox.mailsender.helper.impl;

import java.util.ArrayList;
import java.util.List;

import cn.gd.gz.treemanz.toolbox.mailsender.ConnectionParams;
import cn.gd.gz.treemanz.toolbox.mailsender.SendJob;
import cn.gd.gz.treemanz.toolbox.mailsender.helper.ConnectionPool;
import cn.gd.gz.treemanz.toolbox.mailsender.helper.ConnectionPool.ConnectionWrapper;
import cn.gd.gz.treemanz.toolbox.mailsender.helper.MailSender;

/**
 * @author Treeman
 */
public class PooledMailSender implements MailSender {

    private ConnectionPool connectionPool;

    /**
     * @param connectionPool
     *            the connectionPool to set
     */
    public void setConnectionPool(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
        if (!this.connectionPool.isInit()) {
            this.connectionPool.init();
        }
    }

    /**
     * 单个发送
     * 
     * @param sendJob
     */
    public void send(SendJob sendJob) throws Exception {

        ConnectionWrapper conn = connectionPool.getConnection();
        conn.applyToSendJob(sendJob);
        try {
            sendJob.setConnectionKeepAlive(true);
            // System.out.println("\n\n An email just sened :\nsubject:"+sendJob.getSubject()+"\ncontent:"+sendJob.getContent());
            sendJob.send();
            if (sendJob.getException() != null) {
                throw sendJob.getException(); // debug
            }
            // String s = this.printParams(connectionPool.connectionParams);
            // System.out.println("a sendjob[" + sendJob.getSubject()
            // + "] is done by connection connectionParams[" + s + "]");
        } catch (Exception ex) {
            throw ex;
        } finally {
            conn.free();
            conn = null;
        }

    }

    /**
     * 批量发送
     * 
     * @param sendJob
     * @param recvList
     * @return failedUids
     */
    public List<String> batchSend(SendJob sendJob, List<String> recvList) {

        List<String> failedUids = new ArrayList<String>();

        ConnectionWrapper conn = connectionPool.getConnection();
        conn.applyToSendJob(sendJob);

        for (String uid: recvList) {
            try {
                sendJob.clearAllRecipients();
                sendJob.addTo(uid);
                sendJob.setConnectionKeepAlive(true);
                sendJob.send();
                if (sendJob.getException() != null) {
                    failedUids.add(uid);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                failedUids.add(uid);
                conn.free();
                conn = connectionPool.getConnection();
                conn.applyToSendJob(sendJob);
            }
        }
        conn.free();
        conn = null;

        return failedUids;
    }

    @SuppressWarnings("unused")
    private String printParams(ConnectionParams params) {
        return params.getEnvelopeFrom() + ";" + params.getHeloName() + ";"
                + params.getHost() + ";" + params.getPort() + ";"
                + params.getPassword();
    }

    public boolean isMock() {
        return false;
    }

}
