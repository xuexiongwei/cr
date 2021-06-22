package com.example.copyresult.dao.first;

import com.example.copyresult.entity.Config;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ConfigMapper {
    int deleteByPrimaryKey(String ky);

    int insert(Config record);

    int insertSelective(Config record);

    Config selectByPrimaryKey(String ky);

    int updateByPrimaryKeySelective(Config record);

    int updateByPrimaryKey(Config record);
}