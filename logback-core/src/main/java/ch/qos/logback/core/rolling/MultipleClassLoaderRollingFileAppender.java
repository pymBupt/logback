package ch.qos.logback.core.rolling;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 扩容RollingFileAppender功能,加强Prudent模式
 * Prudent模式,不会生成实时文件,日志直接打印到归档文件里
 * 当前类优化点:创建一个软连接指向这个归档文件,这样Prudent模式日志的结构就和非Prudent模式一样了
 *
 * @author bingo
 */
public class MultipleClassLoaderRollingFileAppender extends RollingFileAppender {

    private String baseFileName;

    /**
     * Prudent模式下默认的fileName会被清理掉,所以我们需要保存一下
     *
     * @param file
     */
    private void putBaseFileName(String file) {
        if (this.baseFileName == null) {
            this.baseFileName = file;
        }
    }

    /**
     * 创建一个软连接,指向这个归档文件
     */
    private void createSymbolicLink() {
        if (isPrudent()) {
            try {
                synchronized (this.baseFileName.intern()) {
                    File temp = new File(this.baseFileName);
                    if (temp.exists()) {
                        boolean symbolicLink = Files.isSymbolicLink(temp.toPath());
                        if (symbolicLink) {
                            Path path = Files.readSymbolicLink(temp.toPath());
                            if (path.toFile().getPath().equals(this.currentlyActiveFile.getPath())) {
                                addInfo("ignore createSymbolicLink , symbolicLink already exist");
                                return;
                            } else {
                                temp.delete();
                            }
                        } else {
                            temp.delete();
                        }
                    }
                    Files.createSymbolicLink(temp.toPath(), this.currentlyActiveFile.toPath());
                    addInfo("createSymbolicLink " + temp.getPath() + " > " + this.currentlyActiveFile.getPath());
                }
            } catch (Exception ex) {
                addError("createSymbolicLink error", ex);
            }
        }
    }

    @Override
    public void start() {
        super.start();
        this.createSymbolicLink();
    }

    @Override
    public void setFile(String file) {
        super.setFile(file);
        this.putBaseFileName(file);
    }

    @Override
    public void rollover() {
        super.rollover();
        this.createSymbolicLink();
    }
}
