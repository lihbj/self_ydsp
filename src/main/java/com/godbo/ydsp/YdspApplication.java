package com.godbo.ydsp;

import com.godbo.ydsp.utils.DESUtils;
import com.godbo.ydsp.utils.EncoderFile;
import com.godbo.ydsp.utils.LocalMacUtil;
import com.godbo.ydsp.utils.YDSPConsts;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import java.io.File;
import java.net.InetAddress;
import java.util.Properties;

@SpringBootApplication
public class YdspApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(YdspApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(YdspApplication.class);
    }

}
