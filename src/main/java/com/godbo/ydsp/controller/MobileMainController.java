package com.godbo.ydsp.controller;

import com.alibaba.fastjson.JSON;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiGettokenRequest;
import com.dingtalk.api.request.OapiUserGetRequest;
import com.dingtalk.api.request.OapiUserGetuserinfoRequest;
import com.dingtalk.api.response.OapiGettokenResponse;
import com.dingtalk.api.response.OapiUserGetResponse;
import com.dingtalk.api.response.OapiUserGetuserinfoResponse;
import com.godbo.ydsp.utils.SendPostUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * 移动审批中间站
 * @author 李海波
 */
@RequestMapping("/service")
@RestController
public class MobileMainController {

    @Autowired
    private Environment env;

    @CrossOrigin
    @RequestMapping("/MobileMainServlet")
    public void mobileMainServlet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setHeader("Access-Control-Allow-Origin",req.getHeader("origin"));
        res.setHeader("Access-Control-Allow-Credentials","true");
//		String accountCode = req.getParameter("accountCode");
        String module = req.getParameter("module");
        String transtype = req.getParameter("transtype");
        String para = req.getParameter("para");

//        para = new String(req.getParameter("para").getBytes("ISO-8859-1"),"UTF-8");

        String user_code = req.getParameter("user_code");
        // 审批流程图直接返回png图片
        if ("queryApproveImg".equals(transtype)) {
            res.setHeader("Content-Type", "image/jpeg");
        } else {
            res.setContentType("text/html;charset=UTF-8");
        }

        res.setCharacterEncoding("UTF-8");

        String result = null;

        try {
            Map<String,String> jsonMap = new HashMap<String,String>();
            jsonMap.put("module", module);
            jsonMap.put("user_code", user_code);
            jsonMap.put("transtype", transtype);
            jsonMap.put("para", para);
            String jsonParam = JSON.toJSONString(jsonMap);

//            Object obj = SendPostUtil.doPostTwo(env.getProperty("NCAddress")+"/service/MobileMainServlet",jsonParam);

            Object obj = SendPostUtil.doGet(env.getProperty("NCAddress")+"/service/MobileMainServlet",user_code,module,transtype,"",para);
//            Object obj = SendPostUtil.doPost(env.getProperty("NCAddress")+"/service/MobileMainServlet",user_code,module,transtype,"",para);
            if (null != obj) {
                result = obj.toString();
            } else {
                result = "{\"code\":\"9999\",\"msg\":\"service return null value\",\"data\":null}";
            }
        } catch (Exception e) {
            e.printStackTrace();
            PrintWriter out = res.getWriter();
            Map<String,String> map = new HashMap<String,String>();
            map.put("code", "999");
            map.put("msg", e.getMessage());
            map.put("datas", null);
            out.print(JSON.toJSONString(map));
            out.close();
            return ;
        }

        if ("queryApproveImg".equals(transtype)) {
            OutputStream out = res.getOutputStream();
            out.write(new sun.misc.BASE64Decoder().decodeBuffer(result));
            out.flush();
            out.close();
        } else if ("downloadMobileFile2".equals(transtype)) {
            String aFileName = req.getParameter("fileName");
            aFileName = "单据WEB模板复制为移动模板脚本.txt";
            String agent = req.getHeader("User-Agent").toUpperCase();
            if ((agent.indexOf("MSIE") > 0)
                    || ((agent.indexOf("RV") != -1) && (agent
                    .indexOf("FIREFOX") == -1))) {
                aFileName = URLEncoder.encode(aFileName, "UTF-8");
            }
            else {
                aFileName = new String(aFileName.getBytes("UTF-8"), "ISO8859-1");
            }

            res.reset();// 设置为没有缓存
            // 弹出下载窗口
            res.setContentType("application/x-download;charset=UTF-8");
            res.setHeader("Cache-Control", "");
            // 显示下载文件名称
            res.setHeader("Content-Disposition", "attachment;filename="+aFileName);
            OutputStream out = res.getOutputStream();
            out.write(new sun.misc.BASE64Decoder().decodeBuffer(result));
            out.flush();
            out.close();
        } else {
            PrintWriter out = res.getWriter();
            out.print(result);
            out.flush();
            out.close();
        }
    }

    /**
     * 登录
     * @param req
     * @param res
     */
    @CrossOrigin
    @RequestMapping(value="/MobileUserLoginServlet", method = RequestMethod.GET)
    public void mobileUserLoginServlet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setHeader("Access-Control-Allow-Origin",req.getHeader("origin"));
        res.setHeader("Access-Control-Allow-Credentials","true");
        String usercode = req.getParameter("usercode");
        String password = req.getParameter("userpwd");
        String module = req.getParameter("module");

        res.setContentType("text/html;charset=UTF-8");
        res.setCharacterEncoding("UTF-8");

        String mobile = "";// 手机号
        String code = ""; // 免登陆授权码
        String accessToken = ""; //accessToken

        code = req.getParameter("code");

        try {
            // 获取accessToken
            accessToken = this.getAccessToken(code);
            // 获取员工手机号
            mobile = this.getUserinfo(code, accessToken);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String,String> map = new HashMap<String,String>();
            map.put("code", "999");
            map.put("msg", e.getMessage());
            map.put("datas", null);
            PrintWriter out = res.getWriter();
            out.print(JSON.toJSONString(map));
            out.close();
            return ;
        }

        PrintWriter out = res.getWriter();
        try {
            Object obj = SendPostUtil.doGet(env.getProperty("NCAddress")+"/service/MobileUserLoginServlet",usercode,module,"",mobile,"");
            out.print(obj.toString());
        } catch (Exception e) {
            e.printStackTrace();
            Map<String,String> map = new HashMap<String,String>();
            map.put("code", "999");
            map.put("msg", e.getMessage());
            map.put("datas", null);
            out.print(JSON.toJSONString(map));
            out.close();
            return ;
        }

        out.flush();
        out.close();
    }

    /**
     * 附件下载
     * @param req
     * @param res
     */
    @CrossOrigin
    @RequestMapping(value="/downLoadFile", method = RequestMethod.GET)
    public void downLoadFile(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setHeader("Access-Control-Allow-Origin",req.getHeader("origin"));
        res.setHeader("Access-Control-Allow-Credentials","true");
        res.setCharacterEncoding("UTF-8");
        res.reset();// 设置为没有缓存
        // 弹出下载窗口
        res.setContentType("application/x-download;charset=UTF-8");
        res.setHeader("Cache-Control", "");


        String pk_doc = req.getParameter("pk_doc");// 附件主键
        String isView = req.getParameter("isView");// 是否预览
        String user_code = req.getParameter("user_code");
        String module = "A0F00100";
        String transtype = "downloadMobileFile2";
        String para = "{\"pk_doc\":\""+pk_doc+"\"}";

        String result = null;

        try {
            Map<String,String> jsonMap = new HashMap<String,String>();
            jsonMap.put("module", module);
            jsonMap.put("user_code", user_code);
            jsonMap.put("transtype", transtype);
            jsonMap.put("para", para);
            String jsonParam = JSON.toJSONString(jsonMap);
            Object obj = SendPostUtil.doGet(env.getProperty("NCAddress")+"/service/MobileMainServlet",user_code,module,transtype,"",para);
            if (null != obj) {
                result = obj.toString();
            } else {
                result = "{\"code\":\"9999\",\"msg\":\"service return null value\",\"data\":null}";
            }
        } catch (Exception e) {
            e.printStackTrace();
            PrintWriter out = res.getWriter();
            Map<String,String> map = new HashMap<String,String>();
            map.put("code", "999");
            map.put("msg", e.getMessage());
            map.put("datas", null);
            out.print(JSON.toJSONString(map));
            out.close();
            return ;
        }

        String aFileName = req.getParameter("fileName");
        String agent = req.getHeader("User-Agent").toUpperCase();
        if ((agent.indexOf("MSIE") > 0)
                || ((agent.indexOf("RV") != -1) && (agent
                .indexOf("FIREFOX") == -1))) {
            aFileName = URLEncoder.encode(aFileName, "UTF-8");
        }
        else {
            aFileName = new String(aFileName.getBytes("UTF-8"), "ISO8859-1");
        }
        // 显示下载文件名称
        if (isView != null && "true".equals(isView)) {
            res.setHeader("Content-Disposition", "inline;filename="+aFileName);
        } else {
            res.setHeader("Content-Disposition", "attachment;filename="+aFileName);
        }

        OutputStream out = res.getOutputStream();
        out.write(new sun.misc.BASE64Decoder().decodeBuffer(result));
        out.flush();
        out.close();
    }

    /**
     * 获取AccessToken
     * @param code  免登陆授权码
     * @return
     *
     * 钉钉开发手册：
     * https://ding-doc.dingtalk.com/doc#/serverapi2/clotub
     * https://ding-doc.dingtalk.com/doc#/serverapi2/eev437
     * @throws Exception
     *
     */
    private String getAccessToken(String code) throws Exception {
        // 获取access_token
        DefaultDingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/gettoken");
        OapiGettokenRequest request = new OapiGettokenRequest();
        request.setAppkey(env.getProperty("AppKey"));
        request.setAppsecret(env.getProperty("AppSecret"));
        request.setHttpMethod("GET");
        OapiGettokenResponse response = client.execute(request);

        Long errcode = response.getErrcode();
        String errmsg = response.getErrmsg();
        if (errcode != 0) {
            throw new Exception("获取钉钉accessToken出错,errcode:"+errcode+",errmsg:"+errmsg);
        }
        return response.getAccessToken();
    }

    /**
     * 获取用户信息
     * @param code  免登陆授权码
     * @return
     *
     * 钉钉开发手册：
     * https://ding-doc.dingtalk.com/doc#/serverapi2/clotub
     * https://ding-doc.dingtalk.com/doc#/serverapi2/eev437
     * @throws Exception
     *
     */
    private String getUserinfo(String code, String accessToken) throws Exception {
        // 获取userId
        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/user/getuserinfo");
        OapiUserGetuserinfoRequest request = new OapiUserGetuserinfoRequest();
        request.setCode(code);
        request.setHttpMethod("GET");
        OapiUserGetuserinfoResponse response = client.execute(request, accessToken);
        String userId = response.getUserid();

        Long errcode = response.getErrcode();
        String errmsg = response.getErrmsg();
        if (errcode != 0) {
            throw new Exception("获取钉钉userId出错,errcode:"+errcode+",errmsg:"+errmsg);
        }

        // 获取用户详情
        DingTalkClient client_info = new DefaultDingTalkClient("https://oapi.dingtalk.com/user/get");
        OapiUserGetRequest request_info = new OapiUserGetRequest();
        request_info.setUserid(userId);
        request_info.setHttpMethod("GET");
        OapiUserGetResponse response_info = client_info.execute(request_info, accessToken);
        Long errcode_info = response_info.getErrcode();
        String errmsg_info = response_info.getErrmsg();
        if (errcode_info != 0) {
            throw new Exception("获取钉钉userinfo出错,errcode:"+errcode_info+",errmsg:"+errmsg_info);
        }
        String mobile = response_info.getMobile();// 手机号
        return mobile;
    }
}
