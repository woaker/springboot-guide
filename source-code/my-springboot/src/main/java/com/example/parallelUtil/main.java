package com.example.parallelUtil;

import com.example.util.ParallelUtils;
import lombok.Value;
import one.util.streamex.StreamEx;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author yongjun.xiao
 * @Description TODO
 * @createTime 2023年01月05日
 */

public class main {

    @Resource(name = "taskExecutor")
    private ThreadPoolTaskExecutor taskExecutor;

    private Map<Long, String> queryUserLatestLoginCity(List<Long> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return Collections.emptyMap();
        }

        @Value
        class Wrapper {
            Long userId;
            Optional<String> cityOptional;
        }

        List<Wrapper> cities = ParallelUtils.executeParallel(
                taskExecutor.getThreadPoolExecutor(),
                userIds,
                userId -> new Wrapper(userId, queryUserLatestLogin(userId))
        );

        return StreamEx.of(cities)
                .filter(wrapper -> wrapper.getCityOptional().isPresent())
                .toMap(Wrapper::getUserId, wrapper -> wrapper.getCityOptional().orElse(""));
    }

    public Optional<String> queryUserLatestLogin(Long userId) {
    /*    if (ObjectUtil.isNull(userId)) return Optional.empty();

        UserDeviceRecordResult recordResult;
        try {
            recordResult = userDeviceService.listOnlineDevices(userId, "");
            return CollectionUtils.isEmpty(recordResult.getUserDeviceRecords())
                    ? Optional.empty()
                    : Optional.of(recordResult.getUserDeviceRecords().get(0).getLastLoginCityName());
        } catch (TException e) {
            log.warn("查询用户: {}最近登录城市出错.", userId, e);
            return Optional.empty();
        }
        */
        return Optional.empty();
    }
}
