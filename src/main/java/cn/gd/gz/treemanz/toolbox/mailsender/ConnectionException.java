package cn.gd.gz.treemanz.toolbox.mailsender;

/**
 * @author Treeman
 */
public class ConnectionException extends RuntimeException {
    private static final long serialVersionUID = 864192622540079470L;

    public ConnectionException() {
        super();
    }

    public ConnectionException(String msg) {
        super(msg);
    }

    public ConnectionException(Throwable t) {
        super(t);
    }

    public ConnectionException(String msg, Throwable t) {
        super(msg, t);
    }

}
