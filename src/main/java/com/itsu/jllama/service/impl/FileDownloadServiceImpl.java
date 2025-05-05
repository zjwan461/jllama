package com.itsu.jllama.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itsu.jllama.entity.FileDownload;
import com.itsu.jllama.mapper.FileDownloadMapper;
import com.itsu.jllama.service.FileDownloadService;
import org.springframework.stereotype.Service;

@Service
public class FileDownloadServiceImpl extends ServiceImpl<FileDownloadMapper, FileDownload> implements FileDownloadService {
}
