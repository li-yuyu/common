package com.lyle.common.lang.util;

import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.csvreader.CsvWriter;

/**
 * csv导出工具类
 */
public class ExportUtils {
    private static final Logger logger               = LoggerFactory.getLogger(ExportUtils.class);
    /**
     * CSV文件列分隔符
     */
    private static final char   CSV_COLUMN_SEPARATOR = ',';

    /**
     * .csv导出方法
     *
     * @param dataList 集合数据
     * @param colNames 表头部数据
     * @param mapKey   查找的对应数据
     *
     */
    public static boolean doExport(List<Map<String, Object>> dataList, String colNames,
                                   String mapKey, OutputStream os) {
        try {
            CsvWriter csvWriter = new CsvWriter(os, CSV_COLUMN_SEPARATOR, Charset.forName("UTF-8"));
            String[] colNameArr = null;
            String[] mapKeyArr = null;
            colNameArr = colNames.split(",");
            mapKeyArr = mapKey.split(",");
            //csv输出列头
            csvWriter.writeRecord(colNameArr, true);
            //csv输出数据值
            if (null != dataList) {
                for (int i = 0; i < dataList.size(); i++) {
                    String[] content = new String[colNameArr.length];
                    for (int j = 0; j < colNameArr.length; j++) {
                        if ("".equals(dataList.get(i).get(mapKeyArr[j]))
                            || dataList.get(i).get(mapKeyArr[j]) == null) {
                            content[j] = "";
                            continue;
                        } else {
                            String temp = dataList.get(i).get(mapKeyArr[j]).toString();
                            if (getType(dataList.get(i).get(mapKeyArr[j]))) {
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                temp = sdf.format(dataList.get(i).get(mapKeyArr[j]));
                                content[j] = temp + "\t";
                            } else {
                                content[j] = temp + "\t";
                            }
                        }
                    }
                    csvWriter.writeRecord(content, true);
                }
            }
            os.write(new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF });
            csvWriter.flush();
            csvWriter.close();
            os.flush();
            os.close();
            return true;
        } catch (Exception e) {
            logger.error("csv导出出错.", e);
            return false;
        }
    }

    /**
     * csv通过浏览器下载并处理乱码
     *
     * @throws UnsupportedEncodingException setHeader
     */
    public static void responseSetProperties(String fileName, HttpServletResponse response,
                                             HttpServletRequest request) throws UnsupportedEncodingException {
        //文件名
        String fn = fileName + ".csv";
        //读取字符编码
        String utf = "UTF-8";
        //设置浏览器保存响应并发起下载
        response.setContentType("application/ms-txt.numberformat:@");
        response.setCharacterEncoding(utf);
        response.setHeader("Pragma", "public");
        response.setHeader("Cache-Control", "max-age=30");
        if ("FF".equals(getBrowser(request))) { //火狐浏览器特殊处理
            fn = new String(fileName.getBytes("UTF-8"), "ISO8859-1");
            response.setHeader("Content-Disposition", "attachment; filename=" + fn + ".csv");
        } else {
            response.setHeader("Content-Disposition",
                "attachment; filename=" + new String(fileName.getBytes("gb2312"), "ISO8859-1")
                                                      + ".csv");
        }
    }

    /**
     * csv通过浏览器下载失败返回错误页面
     *
     * @throws UnsupportedEncodingException setHeader
     */
    public static void responseErrorProperties(HttpServletResponse response) {
        try {
            response.setContentType("text/html");//内容类型，解析为javascript代码或html代码
            response.setCharacterEncoding("utf-8");//内容编码，防止出现中文乱码
            response.getWriter().write("下载失败！");
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    /**
     * 判断变量的类型
     *
     * @Param Object
     * @return boolean
     */
    public static boolean getType(Object t) {
        if (t instanceof Date) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 以下为服务器端判断客户端浏览器类型的方法
     *
     * @Param request
     * @return String
     */
    public static String getBrowser(HttpServletRequest request) {
        String UserAgent = request.getHeader("USER-AGENT").toLowerCase();
        if (UserAgent != null) {
            if (UserAgent.indexOf("msie") >= 0)
                return "IE";
            if (UserAgent.indexOf("firefox") >= 0)
                return "FF";
            if (UserAgent.indexOf("safari") >= 0)
                return "SF";
        }
        return null;
    }
}