package com.example.copyresult.service;


import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.example.copyresult.dao.first.ConfigMapper;
import com.example.copyresult.entity.Pk10;
import com.example.copyresult.utils.Cache;

import com.example.copyresult.utils.JsonUtils;
import com.example.copyresult.utils.Node;
import com.example.copyresult.utils.Restful;


public class CollectDataPK10JSC {

    private static String module = CollectDataPK10JSC.class.getName();
    public static Map<String, Boolean> cacheM = new ConcurrentHashMap<String, Boolean>();// 存放系统缓存变量

    /**
     * 登录获取数据
     *
     * @return
     */
    public static boolean login(String userName, String password) {
        try {
            if (null != Cache.message) {
                return true;
            }

            Node.getNode();
            Thread.sleep(1000 * 4);
            while (!Node.find) {
                Node.getNode();
                Thread.sleep(1000 * 3);
            }

            String str = "";//getCode();
            if (null != str) {
                System.out.println("登录请求：account=" + userName + " ,password=" + password);
                String json = Restful.restful(Cache.node + "/web/rest/cashlogin?account=" + userName + "&password="
                        + password, null, Restful.GET, null, null, null);
                System.out.println("登录返回：json=" + json);
                if (null != json) {
                    Map<String, Object> map = JsonUtils.jsonToPojo(json, Map.class);
                    Boolean success = (Boolean) map.get("success");
                    String msg = (String) map.get("message");
                    if (success) {
                        System.out.println("登录成功");
                        Cache.message = msg;
                        return true;
                    } else {
                        System.out.println("登录失败！！");
                        // 清空，并重新登录
                        System.out.println("重新登录........！");
                        Cache.message = null;
                        try {
                            Thread.sleep(1000 * 3);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        login(userName, password);
                    }
                } else {
                    Cache.message = null;
                    System.out.println("登录异常！");
                }
            } else {
                System.out.println("登录失败，获取验证码失败！");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static int buyCount = 0;

    /**
     * 购买
     *
     * @return
     */
    public synchronized static Pk10 buy(ConfigMapper configMapper, Map<String, String> buyM, String drawNumber) {
        try {
            if (null == Cache.message) {
                String user = configMapper.selectByPrimaryKey("user").getVal();
                String paswd = configMapper.selectByPrimaryKey("paswd").getVal();
                //登录成功后才购买
                login(user, paswd);
            }

            if (null != Cache.message) {
                System.out.println("投注数据请求：buyM=" + JsonUtils.objectToJson(buyM) + " ,drawNumber=" + drawNumber);

                String _OLID_ = Cache.message.split("=")[1];

                String json = null;
                Map<String, Object> jsonM = new HashMap<String, Object>();
                String buyFlag = configMapper.selectByPrimaryKey("buyFlag").getVal();// 是否真实购买true，只分析购买数据false
                System.out.println("是否真实购买：buyFlag=" + buyFlag);
                if (!Boolean.parseBoolean(buyFlag)) {// 虚拟购买时，只做账户查询
                    json = Restful.restful(Cache.node + "/member/account",
                            Cache.pid + "=" + _OLID_ + (Cache.ssid ? "; ssid1=" + Cache.ssidStr + ";" : ""), Restful.GET,
                            null, null, null);
                } else {// 真实购买发送购买请求
                    json = Restful.restful(Cache.node + "/member/bet",
                            Cache.pid + "=" + _OLID_ + (Cache.ssid ? "; ssid1=" + Cache.ssidStr + ";" : ""), Restful.POST,
                            buyM.get("jsonM"), null, null);
                }
                System.out.println("投注数据返回：json=" + json);
                if (null != json) {
                    buyCount = 0;
                    jsonM = JsonUtils.jsonToPojo(json, Map.class);
                    Double balance = 0d;
                    Double result = 0d;
                    Double betting = 0d;
                    if (Boolean.parseBoolean(buyFlag)) {
                        Integer status = (Integer) jsonM.get("status");
                        if (0 == status) {// 购买成功
                            Map<String, Object> account = (Map<String, Object>) jsonM.get("account");
                            balance = (Double) account.get("balance"); // 可用余额
                            result = (Double) account.get("result"); // 盈利金额
                            betting = (Double) account.get("betting"); // 未结算金额
                        } else if (3 == status) {
                            System.out.println("购买额度不足");
                        }
                    } else {
                        balance = (Double) jsonM.get("balance"); // 可用余额
                        result = (Double) jsonM.get("result"); // 盈利金额
                        betting = (Double) jsonM.get("betting"); // 未结算金额
                    }

                    Pk10 pk10 = new Pk10();
                    pk10.setPeriod(drawNumber);
                    pk10.setBuyNum(buyM.get("buyNum"));
                    pk10.setBalance(balance + "");
                    pk10.setWinAmount((result == null ? 0.0 : result) + "");
                    pk10.setBettingAmount(betting + "");
                    pk10.setBuyType(buyM.get("buyType"));
                    // 清空，下次购买重新获取节点，以免同一节点不同时段延时，导致购买延时
                    // Cache.message = null;
                    return pk10;
                } else {
					/*
					 * if(buyCount>3) {
						Debug.info("购买失败3次，【" + drawNumber + "】,重新登录购买", module);
						//登录成功后才购买
						String user = configService.qbk("user");
						String paswd = configService.qbk("paswd");
						if(login(user, paswd)) {
							Debug.info("购买失败，重新购买【" + drawNumber + "】", module);
							buy(configService,buyM,drawNumber);
							if(buyCount>6) {
								Debug.info("购买失败6次，【" + drawNumber + "】不购买", module);
								Cache.message = null;
								return null;
							}
						}
						
					}
					buyCount++;*/
                    System.out.println("购买失败，重新购买【" + drawNumber + "】");
                    //清空缓存，重新登录购买
                    Cache.message = null;
                    buy(configMapper, buyM, drawNumber);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 判断本期是否中
     *
     * @return
     */
    public static boolean isWin(String awardNum, String buyNum) {

        String[] as = awardNum.split(",");
        if (null == buyNum || "".equals(buyNum)) {
            return false;
        }

        if (buyNum.indexOf("_") > -1) {// 两面盘规则

            String[] gz = buyNum.split("_");

            Integer hez = Integer.parseInt(as[0]) + Integer.parseInt(as[1]);// 冠军位+亚军位
            if (buyNum.startsWith("DS")) {// 单双
                String b = gz[0].substring(2, gz[0].length());
                if (gz[1].equals("D")) {// 单
                    String w = as[Integer.parseInt(b) - 1];
                    if (Integer.parseInt(w) % 2 != 0) {
                        return true;
                    }
                } else {// 双
                    String w = as[Integer.parseInt(b) - 1];
                    if (Integer.parseInt(w) % 2 == 0) {
                        return true;
                    }
                }
            } else if (buyNum.startsWith("DX")) {// 大小
                String b = gz[0].substring(2, gz[0].length());
                if (gz[1].equals("D")) {// 大
                    String w = as[Integer.parseInt(b) - 1];
                    if (Integer.parseInt(w) >= 6) {
                        return true;
                    }
                } else {// 小
                    String w = as[Integer.parseInt(b) - 1];
                    if (Integer.parseInt(w) <= 5) {
                        return true;
                    }
                }
            } else if (buyNum.startsWith("LH")) {// 龙虎
                String b = gz[0].substring(2, gz[0].length());
                int b_ = 11 - Integer.parseInt(b);// 对称位
                String w = as[Integer.parseInt(b) - 1];
                String w_ = as[b_ - 1];

                if (gz[1].equals("L")) {// 龙
                    if (Integer.parseInt(w) > Integer.parseInt(w_)) {
                        return true;
                    }
                } else {// 虎
                    if (Integer.parseInt(w) < Integer.parseInt(w_)) {
                        return true;
                    }
                }
            } else if (buyNum.startsWith("GDS")) {// 冠亚单双
                if (gz[1].equals("D")) {// 单
                    if (hez % 2 != 0) {
                        return true;
                    }
                } else {// 双
                    if (hez % 2 == 0) {
                        return true;
                    }
                }
            } else if (buyNum.startsWith("GDX")) {// 冠亚大小
                if (gz[1].equals("D")) {// 大
                    if (hez > 11) {
                        return true;
                    }
                } else {// 小
                    if (hez <= 11) {
                        return true;
                    }
                }
            } else {
                return false;
            }
        } else {
            String[] bs = buyNum.split(",");
            for (int i = 0; i < 10; i++) {
                if (bs[i].contains("-")) {
                    String[] bss = bs[i].split("-");
                    for (String bss_ : bss) {
                        bss_ = bss_.replace("(", "");
                        bss_ = bss_.replace(")", "");
                        if (bss_.equals(as[i])) {
                            return true;
                        }
                    }
                } else {
                    if (bs[i].equals(as[i])) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static void refreshCookie(ConfigMapper configMapper) {
        if (null == Cache.message) {
            String user = configMapper.selectByPrimaryKey("user").getVal();
            String paswd = configMapper.selectByPrimaryKey("paswd").getVal();
            //登录成功后才购买
            login(user, paswd);
        }

        if (null != Cache.message) {
            String _OLID_ = Cache.message.split("=")[1];
            try {
                Restful.restful(Cache.node + "/member/account",
                        Cache.pid + "=" + _OLID_ + (Cache.ssid ? "; ssid1=" + Cache.ssidStr + ";" : ""), Restful.GET,
                        null, null, null);
            } catch (Exception e) {
                String user = configMapper.selectByPrimaryKey("user").getVal();
                String paswd = configMapper.selectByPrimaryKey("paswd").getVal();
                //登录成功后才购买
                login(user, paswd);
                login(user, paswd);
            }
        }
    }

}
