package cn.gd.gz.treemanz.toolbox.mailsender;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.LinkedHashSet;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Header;
import javax.mail.Message.RecipientType;
import javax.mail.Multipart;
import javax.mail.Transport;
import javax.mail.event.TransportListener;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import com.sun.mail.smtp.SMTPMessage;

/**
 * @author Treeman
 */
public final class SendJob implements Runnable {
    private String id = this.hashCode() + "";

    private Contact from = null;

    private Collection<Contact> to = new LinkedHashSet<Contact>();

    private Collection<Contact> cc = new LinkedHashSet<Contact>();

    private Collection<Contact> bcc = new LinkedHashSet<Contact>();

    private Collection<Contact> realRecipients = new LinkedHashSet<Contact>();

    private Collection<Attachment> attachments = new LinkedHashSet<Attachment>();

    private Collection<Header> headers = new LinkedHashSet<Header>();

    private String subject = null;

    private String subjectEncoding = "UTF-8";

    private String content = null;

    private String contentEncoding = "UTF-8";

    // 邮件地址的字符集编码
    private String addressEncoding = "UTF-8";

    // 附件文件名字符集编码

    private String attachmentEncoding = "UTF-8";

    private boolean success = false;

    private boolean sendPartial = true;

    private TransportListener listener = null;

    private Exception exception = null;

    private Connection connection = null;

    private boolean connectionKeepAlive = false;

    public void run() {
        send();
    }

    public void send() {
        if (null == connection) {
            connection = new Connection();
        }
        if (!connection.isConnected()) {
            connection.connect();
        }

        Transport transport = connection.getTransport();
        TransportListener listener = this.listener;

        if (null == this.from) {
            this.from = new Contact();
            this.from.setName(connection.getConnectionParams().getHeloName());
            this.from.setEmail(connection.getConnectionParams()
                    .getEnvelopeFrom());
        }
        Contact from = this.from;
        InternetAddress addressFrom = null;
        Collection<Address> to = new LinkedHashSet<Address>();
        Collection<Address> cc = new LinkedHashSet<Address>();
        Collection<Address> bcc = new LinkedHashSet<Address>();
        SMTPMessage message = null;
        try {
            addressFrom = new InternetAddress(from.getEmail(), from.getName(),
                    this.addressEncoding);
            for (Contact c: this.to) {
                to.add(new InternetAddress(c.getEmail(), c.getName(),
                        this.addressEncoding));
            }
            for (Contact c: this.cc) {
                cc.add(new InternetAddress(c.getEmail(), c.getName(),
                        this.addressEncoding));
            }
            for (Contact c: this.bcc) {
                bcc.add(new InternetAddress(c.getEmail(), c.getName(),
                        this.addressEncoding));
            }

            message = new SMTPMessage(connection.getSession());
            // 当部分地址失效时是否对有效地址进行发送
            message.setSendPartial(this.sendPartial);
            // 将出现在信头的return-path中
            message.setEnvelopeFrom(connection.getConnectionParams()
                    .getEnvelopeFrom());
            // 发件人
            message.setFrom(addressFrom);
            // 将出现在信头的sender中
            message.setSender(addressFrom);
            // 添加收件人
            message.setRecipients(RecipientType.TO,
                    to.toArray(new Address[] {}));
            // 添加抄送
            message.setRecipients(RecipientType.CC,
                    cc.toArray(new Address[] {}));
            // 添加密送
            message.setRecipients(RecipientType.BCC,
                    bcc.toArray(new Address[] {}));
            // 设置邮件主题
            message.setSubject(this.subject, this.subjectEncoding);

            // 设置Header
            for (Header h: this.headers) {
                message.addHeader(h.getName(), h.getValue());
            }

            if (attachments.size() == 0) {// 如果不包含附件
                // 设置邮件正文
                message.setText(this.content, this.contentEncoding, "html");
            } else {// 如果包含附件
                Multipart multipart = new MimeMultipart();
                BodyPart mdpContent = new MimeBodyPart();
                mdpContent.setContent(this.content, "text/html;charset="
                        + this.contentEncoding);
                multipart.addBodyPart(mdpContent);
                for (Attachment attachment: attachments) {
                    if (null != attachment.getFile()) {
                        BodyPart mdp = new MimeBodyPart();
                        FileDataSource fds = new FileDataSource(
                                attachment.getFile());
                        DataHandler dh = new DataHandler(fds);
                        mdp.setDataHandler(dh);
                        String fileName = attachment.getName();
                        if (null == fileName) {
                            fileName = attachment.getFile().getName();
                        }
                        mdp.setFileName(MimeUtility.encodeText(fileName,
                                attachmentEncoding, "B"));// 使用base64编码
                        // 判断是否支持内联
                        if (attachment.getIsDispositionInline()) {
                            mdp.setDisposition(BodyPart.INLINE);
                            mdp.setHeader("Content-ID",
                                    "<" + attachment.getName() + ">");
                        }
                        //
                        multipart.addBodyPart(mdp);
                    }
                }
                message.setContent(multipart);
            }

            if (null != listener) {
                // 添加对投递状态的监听器
                transport.addTransportListener(listener);
            }
            Address[] rcptTos = null;
            if (realRecipients.size() > 0) {
                Collection<Address> tos = new LinkedHashSet<Address>();
                for (Contact c: realRecipients) {
                    tos.add(new InternetAddress(c.getEmail(), c.getName(),
                            this.addressEncoding));
                }
                rcptTos = tos.toArray(new Address[] {});
            } else {
                rcptTos = message.getAllRecipients();
            }
            transport.sendMessage(message, rcptTos);
            this.success = true;
            this.exception = null;
        } catch (Exception e) {
            this.exception = e;
        } finally {
            if (null != listener) {
                transport.removeTransportListener(listener);
            }
            if (!connectionKeepAlive) {
                connection.close();
            }
        }
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public Connection getConnection() {
        return connection;
    }

    public boolean isConnectionKeepAlive() {
        return connectionKeepAlive;
    }

    public void setConnectionKeepAlive(boolean connectionKeepAlive) {
        this.connectionKeepAlive = connectionKeepAlive;
    }

    public Contact getFrom() {
        return from;
    }

    /**
     * 设置发件人
     * 
     * @param contact
     * @throws IllegalArgumentException
     *             发信人地址不合法
     */
    public void setFrom(String contact) {
        from = new Contact(contact);
    }

    public void setFrom(String name, String email) {
        from = new Contact(name, email);
    }

    public Collection<Contact> getTo() {
        return to;
    }

    /**
     * 添加收件人
     * 
     * @param contact
     * @throws IllegalArgumentException
     *             收信人地址不合法
     */
    public void addTo(String contact) {
        Contact c = new Contact(contact);
        to.add(c);
    }

    public void addTo(String name, String email) {
        Contact c = new Contact(name, email);
        to.add(c);
    }

    /**
     * 添加附件
     * 
     * @param name
     * @param path
     * @param isDispositionInline
     * @throws FileNotFoundException
     */
    public void addAttachment(String name, String filePath,
            boolean isDispositionInline) throws FileNotFoundException {
        File f = new File(filePath);
        if (!f.exists()) {
            throw new FileNotFoundException("File [" + filePath + "] not found");
        }
        Attachment attachment = new Attachment();
        attachment.setFile(f);
        attachment.setName(name);
        attachment.setIsDispositionInline(isDispositionInline);
        attachments.add(attachment);
    }

    /**
     * 添加附件
     * 
     * @param name
     * @param path
     * @throws FileNotFoundException
     */
    public void addAttachment(String name, String filePath)
            throws FileNotFoundException {
        addAttachment(name, filePath, false);
    }

    /**
     * 添加附件
     * 
     * @param filePath
     * @throws FileNotFoundException
     */
    public void addAttachment(String filePath) throws FileNotFoundException {
        addAttachment(null, filePath);
    }

    /**
     * 直接添加附件
     * 
     * @param attachment
     * @throws FileNotFoundException
     */
    public void addAttachment(Attachment attachment) {
        if (null != attachment) {
            attachments.add(attachment);
        }
    }

    /**
     * 添加信头
     * 
     * @param name
     * @param value
     */
    public void addHeader(String name, String value) {
        headers.add(new Header(name, value));
    }

    public void clearAttachments() {
        attachments.clear();
    }

    /**
     * 设置附件文件名编码方式，默认UTF-8
     * 
     * @param attachmentEncoding
     */
    public void setAttachmentEncoding(String attachmentEncoding) {
        this.attachmentEncoding = attachmentEncoding;

    }

    /**
     * 设置附件文件名编码方式
     * 
     * @param attachmentEncoding
     */
    public String getAttachmentEncoding() {
        return this.attachmentEncoding;

    }

    /**
     * 删除所有收件人
     */
    public void clearTo() {
        to.clear();
    }

    public Collection<Contact> getCc() {
        return cc;
    }

    /**
     * 添加抄送人
     * 
     * @param contact
     * @throws IllegalArgumentException
     *             抄送人地址不合法
     */
    public void addCc(String contact) {
        Contact c = new Contact(contact);
        cc.add(c);
    }

    public void addCc(String name, String email) {
        Contact c = new Contact(name, email);
        cc.add(c);
    }

    /**
     * 清除所有抄送人
     */
    public void clearCc() {
        cc.clear();
    }

    public Collection<Contact> getBcc() {
        return bcc;
    }

    /**
     * 添加密送人
     * 
     * @param contact
     * @throws IllegalArgumentException
     *             密送人地址不合法
     */
    public void addBcc(String contact) {
        Contact c = new Contact(contact);
        bcc.add(c);
    }

    public void addBcc(String name, String email) {
        Contact c = new Contact(name, email);
        bcc.add(c);
    }

    /**
     * 清除所有密送人
     */
    public void clearBcc() {
        bcc.clear();
    }

    /**
     * 添加真实的收件人（RCPT TO命令中指定的收件人）<br/>
     * 若无真实的收件人，则以邮件Content中指定的To、CC、BCC为收件人
     * 
     * @param contact
     * @throws IllegalArgumentException
     *             密送人地址不合法
     */
    public void addRealRecipient(String contact) {
        Contact c = new Contact(contact);
        realRecipients.add(c);
    }

    public void addRealRecipient(String name, String email) {
        Contact c = new Contact(name, email);
        realRecipients.add(c);
    }

    public void clearRealRecipient() {
        realRecipients.clear();
    }

    public Collection<Contact> getAllRealRecipients() {
        return realRecipients;
    }

    public Collection<Contact> getAllRecipients() {
        Collection<Contact> result = new LinkedHashSet<Contact>();
        result.addAll(to);
        result.addAll(cc);
        result.addAll(bcc);
        return result;
    }

    public void clearAllRecipients() {
        to.clear();
        cc.clear();
        bcc.clear();
    }

    public String getSubject() {
        return subject;
    }

    /**
     * 设置邮件主题
     * 
     * @param subject
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getSubjectEncoding() {
        return subjectEncoding;
    }

    /**
     * 设置邮件主题的编码，默认UTF-8
     * 
     * @param subjectEncoding
     */
    public void setSubjectEncoding(String subjectEncoding) {
        this.subjectEncoding = subjectEncoding;
    }

    public String getContent() {
        return content;
    }

    /**
     * 设置邮件正文
     * 
     * @param content
     */
    public void setContent(String content) {
        this.content = content;
    }

    public String getContentEncoding() {
        return contentEncoding;
    }

    /**
     * 设置邮件正文编码，默认UTF-8
     * 
     * @param contentEncoding
     */
    public void setContentEncoding(String contentEncoding) {
        this.contentEncoding = contentEncoding;
    }

    /**
     * 设置邮件地址编码，默认UTF-8
     * 
     * @param contentEncoding
     */
    public void setAddressEncoding(String addressEncoding) {
        this.addressEncoding = addressEncoding;
    }

    /**
     * 仅当所有Recepient都投递成功才返回true
     * 
     * @return
     */
    public boolean isSuccess() {
        return success;
    }

    void setSuccess(boolean success) {
        this.success = success;
    }

    public Exception getException() {
        return exception;
    }

    void setException(Exception exception) {
        this.exception = exception;
    }

    /**
     * 当部分地址失效时是否对有效地址进行发送
     * 
     * @return
     */
    public boolean isSendPartial() {
        return sendPartial;
    }

    /**
     * 当部分地址失效时是否对有效地址进行发送
     * 
     * @param sendPartial
     */
    public void setSendPartial(boolean sendPartial) {
        this.sendPartial = sendPartial;
    }

    public TransportListener getListener() {
        return listener;
    }

    /**
     * 监听投递状态
     * 
     * @param listener
     */
    public void setListener(TransportListener listener) {
        this.listener = listener;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("id:").append(id).append(",from:").append(from)
                .append(";,to:");
        for (Contact cTo: to) {
            sb.append(cTo).append(";");
        }
        sb.append(",cc:");
        for (Contact cCc: cc) {
            sb.append(cCc).append(";");
        }
        sb.append(",bcc:");
        for (Contact cBcc: bcc) {
            sb.append(cBcc).append(";");
        }
        sb.append(",subject:").append(subject).append(",success:")
                .append(success);
        return sb.toString();
    }

}
