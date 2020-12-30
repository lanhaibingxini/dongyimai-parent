package com.offcn.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.offcn.pojo.TbSeller;
import com.offcn.sellergoods.service.SellerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;

/**
 * 认证类
 */
public class UserDetailsServiceImpl implements UserDetailsService {
    private SellerService sellerService;

    public SellerService getSellerService() {
        return sellerService;
    }

    public void setSellerService(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("ROLE_SELLER") );
        //1.首先根据用户的名称 其实就是 sellerId   查询用户的信息
        TbSeller seller = sellerService.findOne(username);
        //2. 判断用户是否存在
        if(seller!=null) {
            //2.1 如果存在
            //2.1.1 判断是否审核通过
            if("1".equals(seller.getStatus())){//审核通过
                // 开始用springSecurity验证密码是否相等
                return new User(username,seller.getPassword(),authorities);
            }else{
                return null;
            }
        }else {
            //2.2. 返回一个null
            return null;
        }
    }
}
