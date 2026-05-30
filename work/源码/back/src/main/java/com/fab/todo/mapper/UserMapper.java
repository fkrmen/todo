package com.fab.todo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fab.todo.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
