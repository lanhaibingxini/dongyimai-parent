package com.offcn.pay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeCancelRequest;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeCancelResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.offcn.pay.service.AliPayService;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 支付宝支付接口实现类
 */
@Service
public class AliPayServiceImpl implements AliPayService {
    //注入支付客户端接口
    @Autowired
    private AlipayClient alipayClient;

    /**
     * 生成支付宝支付二维码
     * @param out_trade_no 订单号
     * @param total_fee 金额（分）
     * @return
     */
    @Override
    public Map createNative(String out_trade_no, String total_fee) {
        //创建一个map对象
        Map<String,String> map=new HashMap<String, String>();
        //创建预下单请求对象
        AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
        //转换下单金额，传入的下单金额是String类型，转换成与数据库匹配的Long类型
        long total = Long.parseLong(total_fee);
        //再设置成金融格式BigDecimal，单位为“分”
        BigDecimal bigTotal = BigDecimal.valueOf(total);
        //将BigDecimal的单位设置成“元”
        BigDecimal cs = BigDecimal.valueOf(100d);
        //将传入的金额的单位设置为“元”
        BigDecimal bigYuan = bigTotal.divide(cs);
        System.out.println("预下单金额:"+bigYuan.doubleValue());

        //支付宝客户端要求的用法，设置要求的内容
        request.setBizContent("{" +
                "    \"out_trade_no\":\""+out_trade_no+"\"," +
                "    \"total_amount\":\""+bigYuan.doubleValue()+"\"," +
                "    \"subject\":\"测试购买商品001\"," +
                "    \"store_id\":\"xa_001\"," +
                "    \"timeout_express\":\"5m\"}");//设置业务参数

        try {
            //发出预下单业务请求
            AlipayTradePrecreateResponse response = alipayClient.execute(request);
            //从相应对象读取相应结果
            String code = response.getCode();
            System.out.println("响应码:"+code);
            //全部的响应结果
            String body = response.getBody();
            System.out.println("返回结果:"+body);

            //响应码等于10000就意味着预下单成功
            if(code.equals("10000")){
                //此时就传入支付二维码、订单id、支付金额
                map.put("qrcode", response.getQrCode());
                map.put("out_trade_no", response.getOutTradeNo());
                map.put("total_fee",total_fee);
                System.out.println("qrcode:"+response.getQrCode());
                System.out.println("out_trade_no:"+response.getOutTradeNo());
                System.out.println("total_fee:"+total_fee);
            }else{
                System.out.println("预下单接口调用失败:"+body);
            }
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        return map;
    }

    /**
     * 获取指定订单的交易状态
     * @param out_trade_no
     * @return
     */
    @Override
    public Map queryPayStatus(String out_trade_no) {
        Map<String,String> map=new HashMap<String, String>();
        //创建订单查询请求对象
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        //查询请求对象中的两个参数至少必填其中一个
        request.setBizContent("{" +
                "    \"out_trade_no\":\""+out_trade_no+"\"," +
                "    \"trade_no\":\"\"}"); //设置业务参数

        try {
            //发出请求
            AlipayTradeQueryResponse response = alipayClient.execute(request);
            //获得请求发出后的状态码
            String code=response.getCode();
            System.out.println("返回值1:"+response.getBody());
            //如果状态码等于10000，那么就将订单号、查询到的状态、商品编号存入map对象中
            if(code.equals("10000")){
                //System.out.println("返回值2:"+response.getBody());
                map.put("out_trade_no", out_trade_no);
                map.put("tradestatus", response.getTradeStatus());
                map.put("trade_no",response.getTradeNo());
            }
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        return map;
    }

    /**
     * 关闭订单接口
     */
    @Override
    public Map closePay(String out_trade_no) {
        Map<String,String> map=new HashMap<String, String>();
        //撤销交易请求对象
        AlipayTradeCancelRequest request = new AlipayTradeCancelRequest();
        request.setBizContent("{" +
                "    \"out_trade_no\":\""+out_trade_no+"\"," +
                "    \"trade_no\":\"\"}"); //设置业务参数

        try {
            AlipayTradeCancelResponse response = alipayClient.execute(request);
            String code=response.getCode();

            if(code.equals("10000")){

                System.out.println("返回值:"+response.getBody());
                map.put("code", code);
                map.put("out_trade_no", out_trade_no);
                return map;
            }
        } catch (AlipayApiException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }
}
