package com.example.copyresult.service;

import com.example.copyresult.dao.first.ConfigMapper;
import com.example.copyresult.dao.first.FirstPk10Mapper;
import com.example.copyresult.dao.first.Pk10dataMapper;
import com.example.copyresult.dao.second.SecondPk10Mapper;
import com.example.copyresult.entity.Pk10;
import com.example.copyresult.entity.Pk10data;
import com.example.copyresult.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


@Configuration      //1.主要用于标记配置类，兼备Component的效果。
@EnableScheduling   // 2.开启定时任务
public class TaskService {

    @Autowired
    SecondPk10Mapper secondPk10Mapper;

    @Autowired
    FirstPk10Mapper firstPk10Mapper;
    @Autowired
    ConfigMapper configMapper;

    @Autowired
    Pk10dataMapper pk10dataMapper;

    String period = "";

    public static ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(1);


    @Scheduled(cron = "0/59 * * * * ? ")
    private void refreshCookie() {
        CollectDataPK10JSC.refreshCookie(configMapper);
    }

    @Scheduled(cron = "0/2 * * * * ? ")
    private void configureTasks() {
        Pk10 pk10 = secondPk10Mapper.selectLastOne();
        if (pk10 != null && !pk10.getPeriod().equals(period) && pk10.getAwardNum() == null) {
            period = pk10.getPeriod();
            Map<String, String> buyM = new HashMap<String, String>();
            Map<String, Object> jsonM = new HashMap<String, Object>();
            jsonM.put("lottery", "PK10JSC");// 彩票种类
            jsonM.put("drawNumber", period);// 购买期数
            jsonM.put("ignore", false);// 未知参数
            List<Map<String, Object>> bets = new ArrayList<Map<String, Object>>();
            Map<String, Object> m = new HashMap<String, Object>();
            m.put("game", "DX1");
            m.put("contents", pk10.getBuyNum().substring(pk10.getBuyNum().indexOf("_") + 1));
            Double beilv = Double.valueOf(configMapper.selectByPrimaryKey("beilv").getVal());
            Double jine = Double.valueOf(pk10.getBettingAmount());
            if (jine % 2 == 0) {
                m.put("amount", Math.round((jine / 2) * beilv));
            } else {
                m.put("amount", Math.round(jine * beilv));
            }
            m.put("odds", 1.995);
            bets.add(m);
            jsonM.put("bets", bets);
            buyM.put("jsonM", JsonUtils.objectToJson(jsonM));
            buyM.put("buyNum", pk10.getBuyNum());
            buyM.put("buyType", pk10.getBuyType());
            final Pk10 buy = CollectDataPK10JSC.buy(configMapper, buyM, pk10.getPeriod());
            firstPk10Mapper.insert(buy);
            final String drawNumberTem = pk10.getPeriod();
            scheduledThreadPool.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    try {
                        System.out.println(drawNumberTem + "等待更新");
                        boolean isUp = false;// 是否更新成功
                        Pk10data pk10data = pk10dataMapper.selectByPrimaryKey(drawNumberTem);
                        if (pk10data != null) {
                            String awardNum = pk10data.getAwardNum();
                            String date = pk10data.getData();
                            String openDate = pk10data.getOpenDate();
                            if (null != awardNum && !"".equals(awardNum)) {
                                Pk10 pk10 = firstPk10Mapper.selectByPrimaryKey(drawNumberTem);
                                pk10.setAwardNum(awardNum);
                                pk10.setData(date);
                                pk10.setOpenDate(openDate);
                                String buyNum = pk10.getBuyNum();
                                boolean isw = CollectDataPK10JSC.isWin(awardNum, buyNum);
                                pk10.setWin(isw ? "true" : "false");
                                firstPk10Mapper.updateByPrimaryKey(pk10);
                                isUp = true;
                                System.out.println(drawNumberTem + "更新状态线程更新成功!");
                            }
                        }
                        if (isUp) {// 如果更新成功，则中断更新
                            Thread.currentThread().stop();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println(drawNumberTem + "等待更新结果异常");
                    }
                }
            }, 5, 3, TimeUnit.SECONDS);
        }
    }
}
