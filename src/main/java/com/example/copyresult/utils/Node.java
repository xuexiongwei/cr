package com.example.copyresult.utils;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

public class Node {


    public static boolean find = false;
    static boolean toFind = true;

    /**
     * 登录获取数据
     *
     * @return
     * @throws InterruptedException
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     */
    public static void getNode() throws InterruptedException {

        System.out.println("开始获取优质节点......");

        find = false;

//		String[] nodes = new String[] { "https://tb9903.com", "https://tb9904.com", "https://tb9905.com",
//				"https://tb9906.com", "https://tb9907.com", "https://tb9908.com" };
        String[] nodes = new String[]{"https://tb0091.com", "https://tb0092.com", "https://tb0096.com",
                "https://tb0094.com", "https://tb0095.com"};//"https://tb9906.com",
//		String[] nodes = new String[] { "https://tb0085.com"};//"https://tb9906.com",

        // 启用线程
        for (String node : nodes) {
            new Thread(new Runnable() {
                public void run() {
                    try {
                        Long s = System.currentTimeMillis();
                        String code = Restful.restful(node + "/home/", null, Restful.GET, null, 1000, 1000);
                        Long e = System.currentTimeMillis();
                        if (null != code) {
                            if (!find) {
                                find = true;
                                if (!node.equals(Cache.node)) {
                                    System.out.println("节点：" + node + "耗时：" + (e - s) / 1000 + "s");
                                    System.out.println("切换至节点：" + node + "！");
                                    Cache.message = null;// 下次购买时，需重新登录
                                    Cache.ssidStr = StringUtils.uuid32();
                                    Cache.node = node;
                                    if (node.equals(nodes[0])) {
                                        Cache.pid = "94b44c4cdcc5";// 03
                                        Cache.ssid = true;
                                    } else if (node.equals(nodes[1])) {// 04
                                        Cache.ssid = true;
                                        Cache.pid = "b6ac700e3fd8";
                                    } else if (node.equals(nodes[2])) {// 05
                                        Cache.ssid = true;
                                        Cache.pid = "eeca2688dfe8";
                                    } else if (node.equals(nodes[3])) {// 06
                                        Cache.ssid = true;
                                        Cache.pid = "a8665252aef3";
                                    } else if (node.equals(nodes[4])) {// 07
                                        Cache.ssid = true;
                                        Cache.pid = "adefeb5ad7f0";
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        }
    }
}
