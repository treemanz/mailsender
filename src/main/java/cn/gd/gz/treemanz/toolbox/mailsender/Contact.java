package cn.gd.gz.treemanz.toolbox.mailsender;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

/**
 * @author Treeman
 */
public class Contact {
    private String name;

    private String email;

    public Contact() {}

    public Contact(String contact) {
        InternetAddress ia = null;
        try {
            ia = new InternetAddress(contact);
            setName(ia.getPersonal());
            setEmail(ia.getAddress());
        } catch (AddressException e) {
            throw new IllegalArgumentException("Illegal email address", e);
        }
    }

    public Contact(String name, String email) {
        setName(name);
        setEmail(email);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    /**
     * @param email
     * @throws IllegalArgumentException
     */
    public void setEmail(String email) {
        if (EmailPattern.isLegalEmail(email)) {
            this.email = email;
        } else {
            throw new IllegalArgumentException("Illegal email address");
        }
    }

    public String toString() {
        return "\"" + name + "\" <" + email + ">";
    }

}
