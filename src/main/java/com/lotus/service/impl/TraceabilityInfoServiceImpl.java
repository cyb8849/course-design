package com.lotus.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lotus.entity.TraceabilityInfo;
import com.lotus.mapper.TraceabilityInfoMapper;
import com.lotus.service.TraceabilityInfoService;
import org.springframework.stereotype.Service;

/**
 * 溯源信息服务实现
 */
@Service
public class TraceabilityInfoServiceImpl extends ServiceImpl<TraceabilityInfoMapper, TraceabilityInfo> implements TraceabilityInfoService {
}
