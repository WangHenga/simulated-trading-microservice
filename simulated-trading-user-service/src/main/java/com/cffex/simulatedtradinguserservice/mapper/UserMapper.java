package com.cffex.simulatedtradinguserservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cffex.simulatedtradingmodel.entity.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
* @author 17204
* @description 针对表【user(用户信息表)】的数据库操作Mapper
* @createDate 2024-07-08 14:33:48
* @Entity com.cffex.SimulatedTrading.model.entity.User
*/
public interface UserMapper extends BaseMapper<User> {
    @Select("select * from user where account = #{account}")
    User getUserByAccount(@Param("account") String account);
}




