package com.itsu.oa.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itsu.oa.entity.User;
import com.itsu.oa.mapper.UserMapper;
import com.itsu.oa.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
