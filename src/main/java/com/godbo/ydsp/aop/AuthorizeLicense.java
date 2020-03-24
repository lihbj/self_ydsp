package com.godbo.ydsp.aop;

import com.alibaba.fastjson.JSON;
import com.godbo.ydsp.utils.*;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 授权处理
 * @author godbo
 */
@Component
@Aspect
public class AuthorizeLicense {

    @Around("execution(* com.godbo.ydsp.controller..*Servlet(..))")
    private Object checkAuthorizeCode(ProceedingJoinPoint pjp) throws Throwable {
        Object[] objs = pjp.getArgs();
        HttpServletResponse res = (HttpServletResponse) objs[1];
        HttpServletRequest req = (HttpServletRequest) objs[0];
        res.setHeader("Access-Control-Allow-Origin",req.getHeader("origin"));
        res.setHeader("Access-Control-Allow-Credentials","true");
        res.setContentType("text/html;charset=UTF-8");
        res.setCharacterEncoding("UTF-8");

        String auth_flag = System.getProperty(YDSPConsts.AUTH_FLAG);

        try {
            if (!"Y".equals(auth_flag)) {
                this.loadLicense();
            }
        } catch (Exception e) {
            PrintWriter out = res.getWriter();
            Map<String,String> map = new HashMap<String,String>();
            map.put("code", "999");
            map.put("msg", e.getMessage());
            map.put("datas", null);
            out.print(JSON.toJSONString(map));
            out.close();
            return null;
        }
        // 读取授权信息
        String auth_mac = System.getProperty(YDSPConsts.AUTH_MAC);
        String local_mac = System.getProperty(YDSPConsts.LOCAL_MAC);
        String auth_end_date = System.getProperty(YDSPConsts.AUTH_END_DATE);
        if (auth_mac == null || auth_end_date == null || "".equals(auth_mac) || "".equals(auth_end_date)) {
            PrintWriter out = res.getWriter();
            Map<String,String> map = new HashMap<String,String>();
            map.put("code", "999");
            map.put("msg", "license不存在,请先授权");
            map.put("datas", null);
            out.print(JSON.toJSONString(map));
            out.close();
            return null;
        }
        // 校验mac地址是否匹配
        if (!auth_mac.equals(local_mac)) {
            PrintWriter out = res.getWriter();
            Map<String,String> map = new HashMap<String,String>();
            map.put("code", "999");
            map.put("msg", "请先授权后再使用");
            map.put("datas", null);
            out.print(JSON.toJSONString(map));
            out.close();
            return null;
        }
        // 校验是否过期
        if (DateUtils.authorize_date(auth_end_date)) {
            PrintWriter out = res.getWriter();
            Map<String,String> map = new HashMap<String,String>();
            map.put("code", "999");
            map.put("msg", "授权过期");
            map.put("datas", null);
            out.print(JSON.toJSONString(map));
            out.close();
            return null;
        }

        /**
         * 1.读取license
         * 2.解密
         * 3.验证
         */
        /*String path = System.getProperty("user.dir");
        path = "I:\\license.txt";
        File file = new File(path);
        if(!file.exists()){
            return null; //如果授权文件都不存在，肯定还未授权直接返回
        }
        // 获取本机mac地址
        InetAddress ia = InetAddress.getLocalHost();
        String mac = LocalMacUtil.getLocalMac(ia);
        // 读取license
        String encoder = EncoderFile.myread(file).trim();
        encoder = DESUtils.decrypt(encoder);
        // 判断是否是本机mac地址
        if (!encoder.contains(mac)) {
            return null;
        }
        // 解析license
        String[] strs = encoder.split(",");
        // 校验授权结束日期是否超期
        String end_date = strs[2];
        if (DateUtils.authorize_date(end_date)) {
            return null;
        }*/

        pjp.proceed();
        return null;
    }

    private void loadLicense() throws Throwable{
        try {
            /**
             * 1.读取license
             * 2.解密
             * 3.验证
             */
            String path = System.getProperty("user.dir");
            path = path+File.separator+"license.txt";

            File file = new File(path);
            if(!file.exists()){
                throw new Exception("license不存在"+path);
            }
            // 读取license
            String encoder = EncoderFile.myread(file).trim();
            encoder = DESUtils.decrypt(encoder);
            // 解析license
            String[] strs = encoder.split(",");
            // 校验授权结束日期是否超期
            String end_date = strs[2];
            // 授权的mac地址
            String mac = strs[0];

            // 获取本机mac地址
            InetAddress ia = InetAddress.getLocalHost();
            String mac_local = LocalMacUtil.getLocalMac(ia);

            System.setProperty(YDSPConsts.AUTH_MAC, mac);
            System.setProperty(YDSPConsts.AUTH_END_DATE, end_date);
            System.setProperty(YDSPConsts.LOCAL_MAC, mac_local);
            System.setProperty(YDSPConsts.AUTH_FLAG, "Y");
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("license错误"+e.getMessage());
        }
    }

}
