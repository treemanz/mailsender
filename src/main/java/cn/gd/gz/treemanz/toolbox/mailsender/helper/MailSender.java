/**
 * @(#)MailSender.java, 2010-8-11. Copyright 2010 Effiker, Inc. All rights
 *                      reserved. EFFIKER PROPRIETARY/CONFIDENTIAL. Use is
 *                      subject to license terms.
 */
package cn.gd.gz.treemanz.toolbox.mailsender.helper;

import cn.gd.gz.treemanz.toolbox.mailsender.SendJob;

/**
 * @author Treeman
 */
public interface MailSender {

    boolean isMock();

    void send(SendJob sendJob) throws Exception;

}
