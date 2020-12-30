package com.offcn.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 检验手机格式的工具类
 */
public class PhoneFormatCheckUtils {

    /**
     * 判断手机号码格式是否正确
     * @param phone
     * @return
     */
    public static boolean isPhoneLegal(String phone){
        Pattern p = null;
        Matcher m = null;
        boolean b = false;
        //第一位数字必须是1，第二位数字必须是3、4、5、7、8，其余数字都在0-9之间
        p = Pattern.compile("^[1][3,4,5,7,8][0-9]{9}$"); // 验证手机号
        m = p.matcher(phone);
        b = m.matches();
        return b;
   }
}
