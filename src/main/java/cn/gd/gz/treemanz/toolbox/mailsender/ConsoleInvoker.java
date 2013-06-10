/**
 * @(#)ConsoleInvoker.java, 2010-7-30. Copyright 2010 Effiker, Inc. All rights
 *                          reserved. EFFIKER PROPRIETARY/CONFIDENTIAL. Use is
 *                          subject to license terms.
 */
package cn.gd.gz.treemanz.toolbox.mailsender;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Treeman
 */
public class ConsoleInvoker {
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            showHelpInfo();
            System.exit(-1);
        }
        Map<String, List<String>> params = new HashMap<String, List<String>>();
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("-") && i + 1 < args.length) {
                String key = args[i].substring(1);
                List<String> values = params.get(key);
                if (null == values) {
                    values = new LinkedList<String>();
                    params.put(key, values);
                }
                values.add(args[i + 1]);
                i++;
            }
        }
        System.out.println(params);
        String subjectEncoding = getFirst(params.get("subjectEncoding"));
        String contentEncoding = getFirst(params.get("contentEncoding"));
        String addressEncoding = getFirst(params.get("addressEncoding"));
        String subject = getFirst(params.get("subject"));
        String content = getFirst(params.get("content"));
        String from = getFirst(params.get("from"));
        String host = getFirst(params.get("host"));
        String portStr = getFirst(params.get("port"));
        String user = getFirst(params.get("user"));
        String heloName = getFirst(params.get("heloName"));
        String password = getFirst(params.get("password"));
        String needAuthStr = getFirst(params.get("needAuth"));
        String debugStr = getFirst(params.get("debug"));
        String connectionTimeoutStr = getFirst(params.get("connectionTimeout"));
        String ioTimeoutStr = getFirst(params.get("ioTimeout"));
        String sendPartialStr = getFirst(params.get("sendPartial"));

        List<String> to = params.get("to");
        List<String> cc = params.get("cc");
        List<String> bcc = params.get("bcc");
        List<String> recipient = params.get("recipient");
        List<String> attachment = params.get("attachment");

        if (null == subject
                || null == content
                || (null == to && null == cc && null == bcc && null == recipient)) {
            showHelpInfo();
            System.exit(-1);
        }
        ConnectionParams connectionParams = new ConnectionParams();
        if (null != host) {
            connectionParams.setHost(host);
        }
        if (null != portStr) {
            connectionParams.setPort(Integer.parseInt(portStr));
        }
        if (null != heloName) {
            connectionParams.setHeloName(heloName);
        }
        if (null != connectionTimeoutStr) {
            connectionParams.setConnectionTimeout(Long
                    .parseLong(connectionTimeoutStr));
        }
        if (null != ioTimeoutStr) {
            connectionParams.setIoTimeout(Long.parseLong(ioTimeoutStr));
        }
        if (null != user) {
            connectionParams.setEnvelopeFrom(user);
        }
        if (null != password) {
            connectionParams.setPassword(password);
        }
        if (null != needAuthStr) {
            connectionParams.setNeedAuth(Boolean.parseBoolean(needAuthStr));
        }
        if (null != debugStr) {
            connectionParams.setDebug(Boolean.parseBoolean(debugStr));
        }
        Connection connection = new Connection();
        connection.setConnectionParams(connectionParams);
        SendJob job = new SendJob();
        job.setConnection(connection);
        job.setSubject(subject);
        job.setContent(content);
        if (null != from) {
            job.setFrom(from);
        }
        if (null != addressEncoding) {
            job.setAddressEncoding(addressEncoding);
        }
        if (null != subjectEncoding) {
            job.setSubjectEncoding(subjectEncoding);
        }
        if (null != contentEncoding) {
            job.setContentEncoding(contentEncoding);
        }
        if (null != sendPartialStr) {
            job.setSendPartial(Boolean.parseBoolean(sendPartialStr));
        }
        if (null != to) {
            for (String t: to) {
                job.addTo(t);
            }
        }
        if (null != cc) {
            for (String c: cc) {
                job.addCc(c);
            }
        }
        if (null != bcc) {
            for (String b: bcc) {
                job.addBcc(b);
            }
        }
        if (null != recipient) {
            for (String r: recipient) {
                job.addRealRecipient(r);
            }
        }
        if (null != attachment) {
            for (String a: attachment) {
                File f = new File(a);
                if (f.exists()) {
                    job.addAttachment(f.getName(), f.getAbsolutePath());
                } else {
                    throw new FileNotFoundException("File [" + a
                            + "] not found");
                }
            }
        }
        job.send();
        System.out.println(job);
    }

    private static String getFirst(List<String> list) {
        if (null == list || list.size() == 0) {
            return null;
        }
        return list.get(0);
    }

    public static void showHelpInfo() {
        StringBuilder sb = new StringBuilder(500);
        sb.append("Usage: ").append("java SendJob ").append("[-to $email]")
                .append("[-cc $email] ").append("[-bcc $email] ").append(
                        "[-recipient $recipient]").append("[-from $email] ")
                .append("[-subject $subject] ").append("[-content $content] ")
                .append("[-attachment $attachment]").append(
                        "[-subjectEncoding $encoding] ").append(
                        "[-contentEncoding $encoding] ").append(
                        "[-addressEncoding $encoding] ").append(
                        "[-host $host] ").append("[-port $port] ").append(
                        "[-connectionTimeout $millseconds] ").append(
                        "[-ioTimeout $millseconds] ").append(
                        "[-needAuth $isNeedAuth] ").append("[-user $user] ")
                .append("[-password $password]").append(
                        "[-heloName $heloName] ").append(
                        "[-debug $isDebugEnable] ").append(
                        "[-sendPartial $isSendPartialEnable] ");
        System.out.println(sb);
    }
}
