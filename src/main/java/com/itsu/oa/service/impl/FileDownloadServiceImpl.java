package com.itsu.oa.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itsu.oa.entity.FileDownload;
import com.itsu.oa.mapper.FileDownloadMapper;
import com.itsu.oa.service.FileDownloadService;
import org.springframework.stereotype.Service;

@Service
public class FileDownloadServiceImpl extends ServiceImpl<FileDownloadMapper, FileDownload> implements FileDownloadService {
}
