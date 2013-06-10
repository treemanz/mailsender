package cn.gd.gz.treemanz.toolbox.mailsender;

import java.util.regex.Pattern;

/**
 * @author Treeman
 */
public class EmailPattern {
    private static final Pattern domainPattern = Pattern
            .compile("([A-Za-z0-9-])+(\\.[A-Za-z0-9-]+)*((\\.[A-Za-z0-9]{2,})|(\\.[A-Za-z0-9]{2,}\\.[A-Za-z0-9]{2,}))$\\b");

    public static boolean isLegalEmail(String email) {
        if (null == email) {
            return false;
        }
        int pos = email.indexOf("@");
        if (-1 == pos) {
            return false;
        }
        String username = email.substring(0, pos);
        if (username.equals("")) {
            return false;
        }
        String domain = email.substring(pos + 1);
        return domainPattern.matcher(domain).matches();
    }
}
