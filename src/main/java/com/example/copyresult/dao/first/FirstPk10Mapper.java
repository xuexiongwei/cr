package com.example.copyresult.dao.first;

import com.example.copyresult.entity.Pk10;

public interface FirstPk10Mapper {
    int deleteByPrimaryKey(String period);

    int insert(Pk10 record);

    int insertSelective(Pk10 record);

    Pk10 selectByPrimaryKey(String period);

    int updateByPrimaryKeySelective(Pk10 record);

    int updateByPrimaryKey(Pk10 record);

}