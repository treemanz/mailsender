package cn.gd.gz.treemanz.toolbox.mailsender;

import java.io.File;

public class Attachment {
    private String name;

    private File file;

    /**
     * 是否设置为内联的资源
     */
    private boolean isDispositionInline = false;

    /**
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the file
     */
    public File getFile() {
        return this.file;
    }

    /**
     * @param file
     *            the file to set
     */
    public void setFile(File file) {
        this.file = file;
    }

    /**
     * @return the isDispositionInline
     */
    public boolean getIsDispositionInline() {
        return isDispositionInline;
    }

    /**
     * @param isDispositionInline
     *            the isDispositionInline to set
     */
    public void setIsDispositionInline(boolean isDispositionInline) {
        this.isDispositionInline = isDispositionInline;
    }

}
