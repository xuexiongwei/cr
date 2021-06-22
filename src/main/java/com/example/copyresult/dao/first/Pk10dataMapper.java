package com.example.copyresult.dao.first;

import com.example.copyresult.entity.Pk10data;

public interface Pk10dataMapper {
    int deleteByPrimaryKey(String period);

    int insert(Pk10data record);

    int insertSelective(Pk10data record);

    Pk10data selectByPrimaryKey(String period);

    int updateByPrimaryKeySelective(Pk10data record);

    int updateByPrimaryKey(Pk10data record);
}