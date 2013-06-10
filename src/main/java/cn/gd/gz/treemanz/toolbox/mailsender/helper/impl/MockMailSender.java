/**
 * @(#)MockMailSender.java, 2011-9-26. Copyright 2011 Effiker, Inc. All rights
 *                          reserved. EFFIKER PROPRIETARY/CONFIDENTIAL. Use is
 *                          subject to license terms.
 */
package cn.gd.gz.treemanz.toolbox.mailsender.helper.impl;

import cn.gd.gz.treemanz.toolbox.mailsender.SendJob;
import cn.gd.gz.treemanz.toolbox.mailsender.helper.ConnectionPool;
import cn.gd.gz.treemanz.toolbox.mailsender.helper.MailSender;

/**
 * @author Treeman
 */
public class MockMailSender implements MailSender {

    public void setConnectionPool(ConnectionPool connectionPool) {
        // null
    }

    public void send(SendJob sendJob) throws Exception {
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public boolean isMock() {
        return true;
    }

}
