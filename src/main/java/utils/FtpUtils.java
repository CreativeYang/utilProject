package utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.commons.net.ftp.FTPReply;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;

/**
 * @ClassName FtpOperation
 * @Description FTP工具类，实现FTP文件下载
 * @Author yangsd
 * @Date 2021/1/27 10:20
 */
@Slf4j
public class FtpUtils {


    private static FTPClient ftpClient = new FTPClient();
    /**
     * 功能：根据文件路径及名称，下载文件流
     * @param fileDir 文件地址
     * @param fileName 文件名
     * @param ip ftp主机ip
     * @param port ftp主机端口号
     * @param userName ftp主机用户名
     * @param passWord ftp主机用户密码
     * @return
     * @throws IOException
     */
    public static  void download(String ip, int port, String userName, String passWord, String fileDir, String fileName, HttpServletResponse response) throws IOException {
        try {
            //连接到FTP服务器
            connectToServer(ip,port,userName,passWord);
            // 设置文件ContentType类型，这样设置，浏览器会自动判断下载文件类型
            response.setContentType("application/x-msdownload");
            // 设置文件头：最后一个参数是设置下载的文件名并编码为UTF-8
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
            // 采用被动模式
            ftpClient.enterLocalPassiveMode();
            // 设置传输二进制文件
            ftpClient.changeWorkingDirectory(fileDir);

            String newPath = new String(fileName.getBytes("UTF-8"),"ISO-8859-1");
            // ftp文件获取文件
            InputStream is = null;
            BufferedInputStream bis = null;
            try {
                is = ftpClient.retrieveFileStream(newPath);
                bis = new BufferedInputStream(is);
                OutputStream out = response.getOutputStream();
                byte[] buf = new byte[1024];
                int len = 0;
                while ((len = bis.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                out.flush();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                closeConnect();
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
        } catch (FTPConnectionClosedException e) {
            //log.error("ftp连接被关闭！",e);
            throw e;
        } catch (Exception e) {
           // log.error("ERR : 从ftp服务器下载文件 "+ fileName+ " 失败!", e);
        }
    }

    /**
     * 连接到ftp主机
     * @param ip  主机ip
     * @param port  主机端口
     * @param userName  主机用户名
     * @param passWord  主机用户密码
     * @throws Exception
     */
    private static void connectToServer(String ip,int port,String userName,String passWord) throws Exception {
        if (!ftpClient.isConnected()) {
            int reply;
            try {
                ftpClient=new FTPClient();
                ftpClient.connect(ip,port);
                ftpClient.login(userName,passWord);
                reply = ftpClient.getReplyCode();

                if (!FTPReply.isPositiveCompletion(reply)) {
                    ftpClient.disconnect();
                    //log.info("FTP服务器拒绝连接");
                }

            }catch(FTPConnectionClosedException ex){
                //log.error("服务器:IP："+ip+"没有连接数！现在有太多连接,请稍后再试", ex);
                throw ex;
            }catch (Exception e) {
               // log.error("登录ftp服务器【"+ip+"】失败", e);
                throw e;
            }
        }
    }

    /**
     *
     * 功能：关闭ftp连接
     */
    private static void closeConnect() {
        try {
            if (ftpClient != null) {
                ftpClient.logout();
                ftpClient.disconnect();
            }
        } catch (Exception e) {
            //log.error("ftp连接关闭失败！", e);
        }
    }


}

