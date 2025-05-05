package com.itsu.jllama.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itsu.jllama.entity.User;
import com.itsu.jllama.mapper.UserMapper;
import com.itsu.jllama.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
