package com.common.util;

import org.apache.commons.io.FilenameUtils;
import org.csource.common.MyException;
import org.csource.common.NameValuePair;
import org.csource.fastdfs.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
 
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
 
import static org.springframework.util.StringUtils.getFilename;


/**
 * FastDFS工具类【实现文件上传、下载、删除、查询】
 * @author zxy
 *
 */
public class FastDFSClient {
    private static final Logger logger = LoggerFactory.getLogger(FastDFSClient.class);
    private  static TrackerClient trackerClient = null;
    private  static TrackerServer trackerServer = null;
    private  static StorageServer storageServer = null;
    private  static StorageClient1 storageClient = null;
 
    //private static final String FAST_DFS_CONF_FILE = FastDFSClient.class.getResource("/").getPath() + "/fast_client.properties";
    //ClientGloble 读配置文件
    private static ClassPathResource resource = new ClassPathResource("fast_client.conf");
    static {
        try {
            ClientGlobal.init(resource.getClassLoader().getResource("fast_client.conf").getPath());
            //ClientGlobal.initByProperties("/fastdfs-client.properties");      
            trackerClient = new TrackerClient();
            trackerServer = trackerClient.getConnection();
            storageClient = new StorageClient1(trackerServer, storageServer);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("FastDFS工具类初始化失败:"+e.getMessage());
        }
    }
 
 
    /**
     * 上传文件方法
     * <p>Title: uploadFile</p>
     * <p>Description: </p>
     * @param fileName 文件全路径
     * @param extName 文件扩展名，不包含（.）
     * @param metas 文件扩展信息
     * @return
     * @throws Exception
     */
    public static String uploadFile(byte[] pic ,String fileName,long size) {
        String path=null;
        //图片11.jpg  根据图片名称得到图片后缀    jpg
        String ext = FilenameUtils.getExtension(fileName);
 
        NameValuePair[] meta_list = new NameValuePair[3];
        meta_list[0] = new NameValuePair("fileName",fileName);
        meta_list[1] = new NameValuePair("fileExt",ext);
        meta_list[2] = new NameValuePair("fileSize",String.valueOf(size));
        //  group1/M00/00/01/wKjIgFWOYc6APpjAAAD-qk29i78248.jpg
        try {
            path = storageClient.upload_file1(pic, ext, meta_list);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("文件上传失败:"+e.getMessage());
        }
        return path;
    }
 
  /**
     * 上传图片至指定分组
     * @param group_name分组名
     * @param pic 文件
     * @param fileName文件名
     * @param size
     * @return
     */
    public static String uploadFileToGroup(String group_name,byte[] pic ,String fileName,long size) {
        String path=null;
        //图片11.jpg  根据图片名称得到图片后缀    jpg
        String ext = FilenameUtils.getExtension(fileName);
 
        NameValuePair[] meta_list = new NameValuePair[3];
        meta_list[0] = new NameValuePair("fileName",fileName);
        meta_list[1] = new NameValuePair("fileExt",ext);
        meta_list[2] = new NameValuePair("fileSize",String.valueOf(size));
        //  group1/M00/00/01/wKjIgFWOYc6APpjAAAD-qk29i78248.jpg
        try {
            path = storageClient.upload_file1(group_name,pic, ext, meta_list);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("文件上传失败:"+e.getMessage());
        }
        return path;
    }
 
    /**
     * 上传文件方法
     * <p>Title: uploadFile</p>
     * <p>Description: </p>
     * @param fileContent 文件的内容，字节数组
     * @param extName 文件扩展名
     * @param metas 文件扩展信息
     * @return
     * @throws Exception
     */
    public static String uploadFile(byte[] fileContent, String extName, NameValuePair[] metas) {
        String result=null;
        try {
            result = storageClient.upload_file1(fileContent, extName, metas);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("文件上传失败:"+e.getMessage());
        }
        return result;
    }
 
    /**
     * 上传文件
     * @param fileContent  文件的字节数组
     * @param extName  文件的扩展名 如 txt  jpg png 等
     * @return null为失败
     */
    public static String uploadFile(byte[] fileContent, String extName) {
        return uploadFile(fileContent, extName, null);
    }
 
    /**
     * 文件下载到磁盘
     * @param path 图片路径
     * @param output 输出流 中包含要输出到磁盘的路径
     * @return -1失败,0成功
     */
    public static int download_file(String path,BufferedOutputStream output) {
        int result=-1;
        try {
            byte[] b = storageClient.download_file1(path);
            try{
                if(b != null){
                    output.write(b);
                    result=0;
                }
            }catch (Exception e){} //用户可能取消了下载
            finally {
                if (output != null){
                    try {
                        output.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("文件下载失败:"+e.getMessage());
        }
        return result;
    }
    /**
     * 获取文件数组
     * @param path 文件的路径 如group1/M00/00/00/wKgRsVjtwpSAXGwkAAAweEAzRjw471.jpg
     * @return
     */
    public byte[] download_bytes(String path) {
        byte[] b=null;
        try {
            b = storageClient.download_file1(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return b;
    }
 
    /**
     * 删除文件
     * @param group 组名 如：group1
     * @param storagePath 不带组名的路径名称 如：M00/00/00/wKgRsVjtwpSAXGwkAAAweEAzRjw471.jpg
     * @return -1失败,0成功
     */
    public static Integer delete_file(String group ,String storagePath){
        int result=-1;
        try {
            result = storageClient.delete_file(group, storagePath);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("文件删除失败:"+e.getMessage());
        }
        return  result;
    }
    /**
     *
     * @param storagePath  文件的全部路径 如：group1/M00/00/00/wKgRsVjtwpSAXGwkAAAweEAzRjw471.jpg
     * @return -1失败,0成功
     * @throws IOException
     * @throws Exception
     */
    public static Integer delete_file(String storagePath){
        int result=-1;
        try {
            result = storageClient.delete_file1(storagePath);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("文件删除失败:"+e.getMessage());
        }
        return  result;
    }
 
    /**
     * 获取远程服务器文件资源信息
     * @param groupName   文件组名 如：group1
     * @param remoteFileName M00/00/00/wKgRsVjtwpSAXGwkAAAweEAzRjw471.jpg
     * @return
     */
    public static FileInfo getFile(String groupName,String remoteFileName){
        try {
            return storageClient.get_file_info(groupName, remoteFileName);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("获取文件失败:"+e.getMessage());
        }
        return null;
    }
 
    /**
     * 获取访问服务器的token，拼接到地址后面
     *
     * @param filepath 文件路径 group1/M00/00/00/wKgzgFnkTPyAIAUGAAEoRmXZPp876.jpeg
     * @param httpSecretKey 密钥
     * @return 返回token，如： token=078d370098b03e9020b82c829c205e1f&ts=1508141521
     */
    public static String getToken(String filepath, String httpSecretKey){
        // unix seconds
        int ts = (int) Instant.now().getEpochSecond();
        // token
        String token = "null";
        try {
            token = ProtoCommon.getToken(getFilename(filepath), ts, httpSecretKey);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            logger.error("编码格式不对:"+e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            logger.error("验证出错:"+e.getMessage());
        } catch (MyException e) {
            e.printStackTrace();
            logger.error("无效的:"+e.getMessage());
        }
        StringBuilder sb = new StringBuilder();
        sb.append("token=").append(token);
        sb.append("&ts=").append(ts);
 
        return sb.toString();
    }
 
    /**
     * 根据外网url传到fastDEFS
     * @param oldUrl 外网url
     * @return
     */
    public static String getFileAndUploadFile(String oldUrl){
        InputStream in = null;
        HttpURLConnection conn = null;
        try {
            URL url = new URL(oldUrl);
            conn = (HttpURLConnection)url.openConnection();
            //模拟浏览器访问，防止网站屏蔽
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36");
            //通过输入流获取图片数据
            in = conn.getInputStream();
            if (in == null || in.available() <= 0) {
                return null;
            }
            //转换
            byte[] bytes =toByteArray(in);
            return uploadFile(bytes,oldUrl.substring(oldUrl.lastIndexOf(".")+1));
        } catch (IOException e) {
            logger.error("根据外网url传到fastDEFS失败:"+e.getMessage());
        }finally {
            if(conn!=null){
                conn.disconnect();
            }
            if(in!=null){
                try {
                    in.close();
                } catch (IOException e) {
                    logger.error("关闭流失败:"+e.getMessage());
                }
            }
        }
        return null;
    }
 
    /**
     * 流到byte
     * @param input
     * @return
     * @throws IOException
     */
    public static byte[] toByteArray(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
        }
        output.flush();
        output.close();
        return output.toByteArray();
    }
 
    public static void main(String[] args) {
        String old ="http://192.168.1.19:8080/xxx/xxxx.jpg";//能直接访问的
        String newUrl =getFileAndUploadFile(old);
        System.out.println(newUrl);
    }
}