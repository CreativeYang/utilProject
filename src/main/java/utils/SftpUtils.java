package utils;

import com.jcraft.jsch.*;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.Properties;

/**
 * @ClassName SftpUtils
 * @Description FTP工具类，实现FTP文件下载
 * @Author yangsd
 * @Date 2021/1/27 10:20
 */
@Slf4j
public class SftpUtils {

    private static Session sshSession;
    private static ChannelSftp channelSftp;

    /**
     * 功能：根据文件路径及名称，下载文件流
     *
     * @param fileDir  文件地址
     * @param fileName 文件名
     * @param ip       ftp主机ip
     * @param port     ftp主机端口号
     * @param userName ftp主机用户名
     * @param passWord ftp主机用户密码
     */
    public static void downloadFile(String ip, int port, String userName, String passWord, String fileDir, String fileName, HttpServletResponse response) {

        //连接到sftp服务器
        sftpConnection(ip, port, userName, passWord);
        // 设置文件ContentType类型，这样设置，浏览器会自动判断下载文件类型
        response.setContentType("application/x-msdownload");
        // 设置文件头：最后一个参数是设置下载的文件名并编码为UTF-8
        try {
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
           // log.error("encoding失败！");
            e.printStackTrace();
        }
        // ftp文件获取文件
        InputStream is = null;
        BufferedInputStream bis = null;
        try {
            channelSftp.cd(fileDir);
            is = channelSftp.get(fileName);
            bis = new BufferedInputStream(is);
            OutputStream out = response.getOutputStream();
            byte[] buf = new byte[2048];
            int len = 0;
            while ((len = bis.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.flush();
        } catch (SftpException e) {
           // log.error("从FTP服务器下载文件失败！");
            e.printStackTrace();
        } catch (IOException e) {
           // log.error("IO异常！");
            e.printStackTrace();
        } finally {
            disconnect();
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * 连接到sftp服务器
     *
     * @param ip       主机地址
     * @param port     主机端口
     * @param userName 主机用户名
     * @param passWord 主机用户密码
     */
    private static void sftpConnection(String ip, int port, String userName, String passWord) {
        try {
            JSch jsch = new JSch();
            jsch.getSession(userName, ip, port);
            sshSession = jsch.getSession(userName, ip, port);
//            if (log.isInfoEnabled()) {
//                log.info("Session创建成功");
//            }
            sshSession.setPassword(passWord);
            Properties sshConfig = new Properties();
            sshConfig.put("StrictHostKeyChecking", "no");
            sshSession.setConfig(sshConfig);

            sshSession.connect();
//            if (log.isInfoEnabled()) {
//                log.info("Session连接成功");
//            }
            Channel channel = sshSession.openChannel("sftp");
            channel.connect();
//            if (log.isInfoEnabled()) {
//                log.info("开启SFTP通道");
//            }
            channelSftp = (ChannelSftp) channel;
//            if (log.isInfoEnabled()) {
//                log.info("连接到 " + ip);
//            }
        } catch (JSchException e) {
            //log.error("连接失败", e);
            e.printStackTrace();
        }
    }

    /**
     * 关闭连接
     */
    public static void disconnect() {
        if (channelSftp != null) {
            if (channelSftp.isConnected()) {
                channelSftp.disconnect();
//                if (log.isInfoEnabled()) {
//                    log.info("sftp已经关闭");
//                }
            }
        }
        if (sshSession != null) {
            if (sshSession.isConnected()) {
                sshSession.disconnect();
//                if (log.isInfoEnabled()) {
//                    log.info("sshSession已经关闭");
//                }
            }
        }
    }


}

