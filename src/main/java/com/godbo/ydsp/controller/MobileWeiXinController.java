package com.godbo.ydsp.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.godbo.ydsp.utils.GetRequestJsonUtils;
import com.godbo.ydsp.utils.SHAUtil;
import com.godbo.ydsp.utils.SendPostUtil;
import com.godbo.ydsp.utils.WeiXinUrlUtil;
import com.godbo.ydsp.utils.sendmessage.WeChatMsgSend;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;

/**
 * 移动审批企业微信中间站
 */
@RequestMapping("/weixin/service")
@RestController
public class MobileWeiXinController {

    public static final Map<String, String> tokenMap = new HashMap<String,String>();
    public static final Map<String, String> ticketMap = new HashMap<String,String>();

    @Autowired
    private Environment env;

    @CrossOrigin
    @RequestMapping("/MobileMainServlet")
    public void mobileMainServlet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setHeader("Access-Control-Allow-Origin",req.getHeader("origin"));
        res.setHeader("Access-Control-Allow-Credentials","true");
        String module = req.getParameter("module");
        String transtype = req.getParameter("transtype");
        String para = req.getParameter("para");

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

        if ("queryApproveImg".equals(transtype) || "downloadMobileFile2".equals(transtype)) {
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
            accessToken = this.getAccessToken(env.getProperty("weixin.corpid"),env.getProperty("weixin.ydsp.secret"));
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
     * 获取jsapiTicket
     * @param req
     * @param res
     */
    @CrossOrigin
    @RequestMapping(value="/getJsapiTicket", method = RequestMethod.GET)
    public void getJsapiTicket(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setHeader("Access-Control-Allow-Origin",req.getHeader("origin"));
        res.setHeader("Access-Control-Allow-Credentials","true");

        res.setContentType("text/html;charset=UTF-8");
        res.setCharacterEncoding("UTF-8");

        String url = req.getParameter("url");
        url = URLDecoder.decode(url,"UTF-8");

        //accessToken
        String accessToken = "";
        //ticket
        String ticket = "";

        try {
            // 获取accessToken
            accessToken = this.getAccessToken(env.getProperty("weixin.corpid"),env.getProperty("weixin.ydsp.secret"));
            // 获取ticket
            ticket = this.getTicket(accessToken, url);

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
        //将参数排序并拼接字符串
        String str = "jsapi_ticket="+ticketMap.get("ticket")+"&noncestr="+ticketMap.get("noncestr")+"&timestamp="+ticketMap.get("timestamp")+"&url="+ticketMap.get("url");

        // 将字符串进行sha1加密
        String signature = SHAUtil.SHA1(str);

        // 返回给前端
        PrintWriter out = res.getWriter();
        Map<String,String> map = new HashMap<String,String>();
        map.put("code", "0");
        map.put("msg", "成功");
        map.put("timestamp",ticketMap.get("timestamp"));
        map.put("nonceStr",ticketMap.get("noncestr"));
        map.put("signature",signature);
        map.put("ticket",ticketMap.get("ticket"));
        map.put("accessToken",accessToken);
        map.put("url",ticketMap.get("url"));
        map.put("str",str);
//        map.put("datas", map2);
        out.print(JSON.toJSONString(map));

        out.flush();
        out.close();
    }

    /**
     * 获取AccessToken
     * @return
     */
    private synchronized String getAccessToken(String corpid, String corpsecret) throws Exception {
        if (tokenMap != null && tokenMap.get(corpsecret) != null) {
            String access_token = tokenMap.get("access_token");
            Long last = new Long(tokenMap.get("last_time"));
            Long current = System.currentTimeMillis();
            Long sub = current - last;
            // 如果accessToken还在有效期内直接返回，默认有效期为7200秒，这里用3600秒，防止提前过期
            if (sub < 3600000) {
                return access_token;
            }
        }

        String requestUrl = "https://qyapi.weixin.qq.com/cgi-bin/gettoken?";
        List<NameValuePair> nameValuePairs = new LinkedList<>();
        nameValuePairs.add(new BasicNameValuePair("corpid", corpid)); //开发者设置中的appId
        nameValuePairs.add(new BasicNameValuePair("corpsecret", corpsecret)); //开发者设置中的appSecret

//        Map<String,String> requestUrlParam = new HashMap<String,String>();
//        requestUrlParam.put("corpid", corpid);	//开发者设置中的appId
//        requestUrlParam.put("corpsecret", corpsecret);	//开发者设置中的appSecret
        JSONObject jsonObject = JSON.parseObject(WeiXinUrlUtil.sendHttpsPost(requestUrl, nameValuePairs));

        String errcode = jsonObject.getString("errcode");
        String errmsg = jsonObject.getString("errmsg");
        if (!"0".equals(errcode)) {
            throw new Exception("获取企业微信移动审批accessToken出错,errcode:"+errcode+",errmsg:"+errmsg);
        }
        String access_token = jsonObject.getString("access_token");
        tokenMap.put(corpsecret,corpsecret);
        tokenMap.put("access_token",access_token);
        tokenMap.put("last_time",System.currentTimeMillis()+"");

        return access_token;
    }

    /**
     * 获取用户信息
     * @param code  免登陆授权码
     * @return
     *
     * @throws Exception
     *
     */
    private String getUserinfo(String code, String accessToken) throws Exception {
        // 获取userId
        String requestUrl = "https://qyapi.weixin.qq.com/cgi-bin/user/getuserinfo?";
        List<NameValuePair> nameValuePairs = new LinkedList<>();
        nameValuePairs.add(new BasicNameValuePair("access_token", accessToken));
        nameValuePairs.add(new BasicNameValuePair("code", code));

//        Map<String,String> requestUrlParam = new HashMap<String,String>();
//        requestUrlParam.put("access_token", accessToken);
//        requestUrlParam.put("code", code);
        JSONObject jsonObject = JSON.parseObject(WeiXinUrlUtil.sendHttpsPost(requestUrl, nameValuePairs));

        String errcode = jsonObject.getString("errcode");
        String errmsg = jsonObject.getString("errmsg");
        if (errcode != null && !"0".equals(errcode)) {
            throw new Exception("获取企业微信userId出错,errcode:"+errcode+",errmsg:"+errmsg);
        }
        String userId = jsonObject.getString("UserId");

        // 获取用户详情
        String requestUrl_info = "https://qyapi.weixin.qq.com/cgi-bin/user/get?";
        List<NameValuePair> nameValuePairs_info = new LinkedList<>();
        nameValuePairs_info.add(new BasicNameValuePair("access_token", accessToken));
        nameValuePairs_info.add(new BasicNameValuePair("userid", userId));

//        Map<String,String> requestUrlParam_info = new HashMap<String,String>();
//        requestUrlParam_info.put("access_token", accessToken);
//        requestUrlParam_info.put("userid", userId);
        JSONObject jsonObject_info = JSON.parseObject(WeiXinUrlUtil.sendHttpsPost(requestUrl_info, nameValuePairs_info));

        String errcode_info = jsonObject_info.getString("errcode");
        String errmsg_info = jsonObject_info.getString("errmsg");
        if (errcode_info != null && !"0".equals(errcode_info)) {
            throw new Exception("获取企业微信userinfo出错,errcode:"+errcode_info+",errmsg:"+errmsg_info);
        }
        String mobile = jsonObject_info.getString("mobile");;// 手机号
        return mobile;
    }

    /**
     * 获取Ticket
     * @return
     */
    private synchronized String getTicket(String accessToken, String url) throws Exception {
        if (ticketMap != null && ticketMap.get("ticket") != null) {
            String ticket = ticketMap.get("ticket");
            Long last = new Long(ticketMap.get("last_time"));
            Long current = System.currentTimeMillis();
            Long sub = current - last;
            // 如果Ticket还在有效期内直接返回，默认有效期为7200秒，这里用3600秒，防止提前过期
            if (sub < 3600000) {
                return ticket;
            }
        }

        String requestUrl = "https://qyapi.weixin.qq.com/cgi-bin/get_jsapi_ticket?";
        List<NameValuePair> nameValuePairs = new LinkedList<>();
        nameValuePairs.add(new BasicNameValuePair("access_token", accessToken));
        nameValuePairs.add(new BasicNameValuePair("type", "jsapi"));

        JSONObject jsonObject = JSON.parseObject(WeiXinUrlUtil.sendHttpsPost(requestUrl, nameValuePairs));

        String errcode = jsonObject.getString("errcode");
        String errmsg = jsonObject.getString("errmsg");
        if (!"0".equals(errcode)) {
            throw new Exception("获取企业微信jsapi_ticket出错,errcode:"+errcode+",errmsg:"+errmsg);
        }
        // 拿到ticket
        String ticket = jsonObject.getString("ticket");

        //随机字符串
        String noncestr = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        //时间戳
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        ticketMap.put("ticket",ticket);
        ticketMap.put("noncestr",noncestr);
        ticketMap.put("access_token",accessToken);
        ticketMap.put("timestamp",timestamp);
        ticketMap.put("last_time",System.currentTimeMillis()+"");
        ticketMap.put("url",url);

        return ticket;
    }

    /**
     * 给企业微信发送消息
     *
     * @param req
     * @param res
     * @throws IOException
     */
    @CrossOrigin
    @RequestMapping("/sendMessage")
    public void sendMessage(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setHeader("Access-Control-Allow-Origin",req.getHeader("origin"));
        res.setHeader("Access-Control-Allow-Credentials","true");
        res.setContentType("text/html;charset=UTF-8");
        res.setCharacterEncoding("UTF-8");


        JSONObject json = GetRequestJsonUtils.getRequestJsonObject(req);

        String touser = json.getString("touser");
        String content = json.getString("content");

        WeChatMsgSend swx = new WeChatMsgSend();

        try {
            // 获取accessToken
            String accessToken = this.getAccessToken(env.getProperty("weixin.corpid"),env.getProperty("weixin.ydsp.secret"));

            // 发送消息url
            String requestUrl = "https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token="+accessToken;

            String postdata = swx.createPostData(touser, "text", Integer.parseInt(env.getProperty("weixin.ydsp.agentid")), "content", content);
//            String resp = swx.post("utf-8", WeChatMsgSend.CONTENT_TYPE, requestUrl, postdata, accessToken);

            JSONObject jsonObject = JSON.parseObject(WeiXinUrlUtil.sendHttpsPost2(requestUrl, postdata));

            String errcode = jsonObject.getString("errcode");
            String errmsg = jsonObject.getString("errmsg");
            if (!"0".equals(errcode)) {
                throw new Exception("发送消息失败,errcode:"+errcode+",errmsg:"+errmsg);
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

        PrintWriter out = res.getWriter();
        Map<String,String> map = new HashMap<String,String>();
        map.put("code", "0");
        map.put("msg", "ok");
        map.put("datas", null);
        out.print(JSON.toJSONString(map));
        out.flush();
        out.close();

    }

}
