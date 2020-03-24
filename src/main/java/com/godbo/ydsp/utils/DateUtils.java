package com.godbo.ydsp.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 2020年2月24日19:58:46
 */
public class DateUtils {

    public static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

    public static String currenttime = DateUtils.formatter.format(new Date());

    public static boolean authorize_date(String date){
        if(1 == compare_date(currenttime,date) || 0 == compare_date(currenttime,date)){
            return true;
        }
        return false;
    }


    public static int compare_date(String date1, String date2) {
        try {
            Date dt1 = formatter.parse(date1);
            Date dt2 = formatter.parse(date2);
            if (dt1.getTime() > dt2.getTime()) {
//                System.out.println("dt1 在dt2前");
                return 1;
            } else if (dt1.getTime() < dt2.getTime()) {
//                System.out.println("dt1在dt2后");
                return -1;
            } else {
                return 0;
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return 0;
    }


}
