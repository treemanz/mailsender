package cn.gd.gz.treemanz.toolbox.mailsender.helper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.gd.gz.treemanz.toolbox.mailsender.Connection;
import cn.gd.gz.treemanz.toolbox.mailsender.ConnectionParams;
import cn.gd.gz.treemanz.toolbox.mailsender.SendJob;

/**
 * 连接池
 * 
 * @author Treeman
 */
public class ConnectionPool implements Runnable {

    private List<ConnectionWrapper> conns;

    ConnectionParams connectionParams;

    private int maxUsePerConn = 100;

    private int maxPoolSize = 5;

    private int timeout = 60000;

    private int initPoolSize = 2;

    private long waitingTimeout = 60000;// 在阻塞时等待获取连接的超时时间

    private boolean isInit = false;

    /**
     * @return the isInit
     */
    public boolean isInit() {
        return isInit;
    }

    public void init() {
        conns = new ArrayList<ConnectionWrapper>();
        for (int i = 0; i < initPoolSize; i++) {
            this.scaleOut();
        }
        Thread monitor = new Thread(this);
        monitor.start();
        this.isInit = true;
    }

    /**
     * @param waitingTimeout
     *            the waitingTimeout to set
     */
    public void setWaitingTimeout(long waitingTimeout) {
        this.waitingTimeout = waitingTimeout;
    }

    /**
     * @param initPoolSize
     *            the initPoolSize to set
     */
    public void setInitPoolSize(int initPoolSize) {
        this.initPoolSize = initPoolSize;
    }

    /**
     * @param connectionParams
     *            设置连接参数，此连接池内的所有连接都将使用这个参数来创建
     */
    public void setConnectionParams(ConnectionParams params) {
        this.connectionParams = params;
    }

    /**
     * @param maxUsePerConn
     *            设置每个连接最多使用的次数，超过则重新连接
     */
    public void setMaxUsePerConn(int maxUsePerConn) {
        this.maxUsePerConn = maxUsePerConn;
    }

    /**
     * @param maxPoolSize
     *            设置连接池的最大连接数，如果超出，再对连接池进行扩容的时候会抛出运行时异常。
     */
    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    /**
     * @param timeout
     *            连接池内每个连接的超时时间，池初始化的时候会开启一个线程每 "1/2 超时时间" 扫描一次池中所有的连接
     *            如果这个连接处于正在使用的状态但他的使用时间已经超出超时时间
     *            （一般是由于没有及时调用这个连接的ConnectionWrapper.free()方法释放掉） 则关闭这个连接再重新连接。
     */
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int poolSize() {
        return this.conns.size();
    }

    protected ConnectionWrapper buildConnectionWrapper() {
        Connection conn = new Connection();
        conn.setConnectionParams(connectionParams);
        ConnectionWrapper newWrapper = new ConnectionWrapper(conn);
        return newWrapper;
    }

    /**
     * 获取连接，如果无法获得连接则会阻塞，如果阻塞时间超出waitingTimeout的话抛出异常。
     * 
     * @return
     */
    public ConnectionWrapper getConnection() {
        ConnectionWrapper conn = null;
        long ts = (new Date()).getTime();
        // int i=0;
        while (null == (conn = this.getConnectionInternel())) {
            long te = (new Date()).getTime();
            // i++;
            // System.out.println(Thread.getName()+" waiting[#"+i+"] "+(te-ts));
            //
            if ((te - ts) >= waitingTimeout) {
                throw new RuntimeException("timeout[" + (te - ts)
                        + " ms] when getting connection");
            }
            //
            synchronized (lock) {
                try {
                    lock.wait(waitingTimeout / 2);
                } catch (Exception ex) {
                    // throw new
                    // RuntimeException("timeout["+(waitingTimeout)+" ms] when waiting notifing");
                }
            }
        }
        return conn;
    }

    protected synchronized ConnectionWrapper getConnectionInternel() {
        for (ConnectionWrapper conn: conns) {
            if (null == conn || !conn.isFree()) {
                continue;
            }
            if (conn.getUseTime() >= this.maxUsePerConn) {
                conn.reconnect();
            }
            conn.lock();
            return conn;
        }
        ConnectionWrapper conn = this.scaleOut();
        if (null != conn) {
            conn.lock();
        }
        return conn;

    }

    private Object lock = new Object();

    /**
     * 扩容，添加一个ConnectionWrapper到pool中 如果当前的连接数已经达到maxPoolSize则返回null
     * 
     * @return
     */
    private ConnectionWrapper scaleOut() {
        // System.out.println("scaleOuting");
        int poolSize = this.poolSize();
        if (maxPoolSize <= poolSize) {
            return null;
            // throw new
            // RuntimeException("too many connections have been required, maxPoolSize = "+maxPoolSize);
        }
        ConnectionWrapper wrapper = this.buildConnectionWrapper();
        this.conns.add(wrapper);
        return wrapper;
    }

    /**
     * 用于维护连接池里面的连接
     */
    public void run() {
        while (true) {
            long now = (new Date()).getTime();
            int size = conns.size();
            for (int i = 0; i < size; i++) {
                ConnectionWrapper conn = conns.get(i);
                if (null == conn || conn.isFree()) {
                    continue;
                }
                long lut = conn.getLastUseTime();
                if (timeout <= now - lut || !conn.getConnection().isConnected()) {
                    // System.out.println(Thread.getName()+" reconnecting the timeout connection["+conn+"]");
                    conn.reconnect();
                }
            }

            try {
                Thread.sleep(timeout / 2);
            } catch (Exception ex) {

            }
        }

    }

    public class ConnectionWrapper {

        private Connection conn;

        private int useTime = 0;

        boolean isFree = true;

        private long lastUseTime;

        public long getLastUseTime() {
            return this.lastUseTime;
        }

        public Connection getConnection() {
            return conn;
        }

        /**
         * @param conn
         */
        public ConnectionWrapper(Connection conn) {
            if (null == conn) {
                throw new IllegalArgumentException(
                        "need a Connection to instance the ConnectionWrapper");
            }
            this.conn = conn;
            this.conn.connect();
        }

        public int getUseTime() {
            return useTime;
        }

        public void use() {
            useTime++;
            this.lastUseTime = (new Date()).getTime();
        }

        public boolean isFree() {
            return isFree;
        }

        public void lock() {
            isFree = false;
        }

        public void free() {
            isFree = true;
            synchronized (lock) {
                lock.notifyAll();
            }

        }

        public synchronized void reconnect() {
            conn.close();
            conn.connect();
            useTime = 0;
            isFree = true;
            synchronized (lock) {
                lock.notifyAll();
            }
            System.out.println(Thread.currentThread().getName()
                    + " is reconnecting ");
        }

        public SendJob applyToSendJob(SendJob job) {
            job.setConnection(this.conn);
            this.use();
            return job;
        }

    }

}
