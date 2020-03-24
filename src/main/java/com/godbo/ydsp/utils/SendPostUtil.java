package com.godbo.ydsp.utils;

import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

/**
 * 调用NC接口入口类
 * @author 李海波
 */
public class SendPostUtil {

    /**
     * POST---有参(普通参数)
     *
     * @date 2019年12月10日14:28:37
     */
    public static Object doPost(String url, String user_code, String module,String transtype,String mobile,String para) throws Exception {

        // 获得Http客户端(可以理解为:你得先有一个浏览器;注意:实际上HttpClient与浏览器是不一样的)
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();

        // 参数
        StringBuffer params = new StringBuffer();
        // 字符数据最好encoding以下;这样一来，某些特殊字符才能传过去(如:某人的名字就是“&”,不encoding的话,传不过去)
        params.append("user_code=" + user_code);
        params.append("&");
        params.append("module="+module);
        params.append("&");
        params.append("transtype="+transtype);
        params.append("&");
        params.append("mobile="+mobile);
        params.append("&");
        params.append("para="+para);

        // 创建Post请求
        HttpPost httpPost = new HttpPost(url + "?" + params);

        // 设置ContentType(注:如果只是传普通参数的话,ContentType不一定非要用application/json)
        httpPost.setHeader("Content-Type", "application/json;charset=utf8");

        // 响应模型
        CloseableHttpResponse response = null;
        String obj = "";
        try {
            // 由客户端执行(发送)Post请求
            response = httpClient.execute(httpPost);
            // 从响应模型中获取响应实体
            HttpEntity responseEntity = response.getEntity();

            /*System.out.println("响应状态为:" + response.getStatusLine());
            if (responseEntity != null) {
                System.out.println("响应内容长度为:" + responseEntity.getContentLength());
                System.out.println("响应内容为:" + EntityUtils.toString(responseEntity));
            }*/
            obj = EntityUtils.toString(responseEntity);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        } finally {
            try {
                // 释放资源
                if (httpClient != null) {
                    httpClient.close();
                }
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return obj;
    }

    /**
     * post -- json字符串参数
     * @param url
     * @param jsonParam
     * @return
     * @throws Exception
     */
    public static Object doPostTwo(String url ,String jsonParam) throws Exception {
        CloseableHttpClient closeableHttpClient = HttpClients.createDefault();
        CloseableHttpResponse closeableHttpResponse = null;

        String obj = "";
        try {
            HttpPost httpPost = new HttpPost(url);
            httpPost.addHeader("Content-Type", "application/json;charset=UTF-8");
            StringEntity entity = new StringEntity(jsonParam, ContentType.create("text/json", "UTF-8"));
            httpPost.setEntity(entity);
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(2000).setConnectTimeout(2000).build();
            httpPost.setConfig(requestConfig);

            closeableHttpResponse = closeableHttpClient.execute(httpPost);

            int statusCode = closeableHttpResponse.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                //TODO:状态码非200代表没有正常返回,此处处理你的业务
            }

            HttpEntity httpEntity = closeableHttpResponse.getEntity();
            obj = EntityUtils.toString(httpEntity);

        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        } finally {
            try {
                // 释放资源
                if (closeableHttpClient != null) {
                    closeableHttpClient.close();
                }
                if (closeableHttpResponse != null) {
                    closeableHttpResponse.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return obj;
    }

    /**
     * Get---有参(普通参数)
     *
     * @date 2019年12月10日21:37:05
     */
    public static Object doGet(String url, String user_code, String module,String transtype,String mobile,String para) throws Exception {
        List<NameValuePair> nameValuePairs = new LinkedList<>();
        nameValuePairs.add(new BasicNameValuePair("module", module));
        nameValuePairs.add(new BasicNameValuePair("transtype", transtype));
        nameValuePairs.add(new BasicNameValuePair("mobile", mobile));
        nameValuePairs.add(new BasicNameValuePair("user_code", user_code));
        nameValuePairs.add(new BasicNameValuePair("para", para));
        CloseableHttpClient closeableHttpClient = HttpClients.createDefault();
        CloseableHttpResponse closeableHttpResponse = null;
        String paramStr = "";
        String obj = "";
        try {
            paramStr = EntityUtils.toString(new UrlEncodedFormEntity(nameValuePairs, Consts.UTF_8));
            HttpPost httpPost = new HttpPost(appendString(url, paramStr));

            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(10000).setConnectTimeout(10000).build();
            httpPost.setConfig(requestConfig);
            //sendPostURL.
            closeableHttpResponse = closeableHttpClient.execute(httpPost);
            //            //HttpCode
            int statusCode = closeableHttpResponse.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {

            }
            //此处处理状态码200
            HttpEntity httpEntity = closeableHttpResponse.getEntity();
            obj = EntityUtils.toString(httpEntity);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        } finally {
            try {
                // 释放资源
                if (closeableHttpClient != null) {
                    closeableHttpClient.close();
                }
                if (closeableHttpResponse != null) {
                    closeableHttpResponse.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return obj;
    }


    /**
     * 把请求参数通过?拼接
     * 示例: 1.1.1.1:80/?uname=1&token=1
     */
    public static String appendString(String url, String paramStr) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(url);
        stringBuffer.append("?");
        stringBuffer.append(paramStr);
        return stringBuffer.toString();
    }
}
