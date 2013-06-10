package cn.gd.gz.treemanz.toolbox.mailsender;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

/**
 * @author Treeman
 */
public class SimpleAuthenticator extends Authenticator {
    private String username;

    private String password;

    public SimpleAuthenticator(String username, String password) {
        this.username = username;
        this.password = password;
    }

    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(username, password);
    }
}
