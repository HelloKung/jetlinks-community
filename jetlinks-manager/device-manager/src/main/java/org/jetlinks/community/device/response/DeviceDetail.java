package org.jetlinks.community.device.response;

import lombok.Getter;
import lombok.Setter;
import org.jetlinks.community.device.enums.DeviceType;
import org.jetlinks.core.device.DeviceOperator;
import org.jetlinks.community.device.entity.DeviceInstanceEntity;
import org.jetlinks.community.device.entity.DeviceProductEntity;
import org.jetlinks.community.device.entity.DeviceTagEntity;
import org.jetlinks.community.device.enums.DeviceState;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@Setter
public class DeviceDetail {

    //设备ID
    private String id;

    //设备名称
    private String name;

    //消息协议标识
    private String protocol;

    //通信协议
    private String transport;

    //所属机构ID
    private String orgId;

    //所属机构名称
    private String orgName;

    //型号ID
    private String productId;

    //型号名称
    private String productName;

    //设备状态
    private DeviceState state;

    //设备类型
    private DeviceType deviceType;

    //客户端地址 /id:port
    private String address;

    //上线时间
    private long onlineTime;

    //离线时间
    private long offlineTime;

    //创建时间
    private long createTime;

    //注册时间
    private long registerTime;

    //设备元数据
    private String metadata;

    //设备配置信息
    private Map<String, Object> configuration;

    //标签
    private List<DeviceTagEntity> tags = new ArrayList<>();

    public Mono<DeviceDetail> with(DeviceOperator operator) {
        return Mono.zip(
            operator.getAddress().defaultIfEmpty("/"),
            operator.getOnlineTime().defaultIfEmpty(0L),
            operator.getOfflineTime().defaultIfEmpty(0L),
            operator.getMetadata()
        ).doOnNext(tp -> {
            setOnlineTime(tp.getT2());
            setOfflineTime(tp.getT3());
            setAddress(tp.getT1());
            with(tp.getT4()
                .getTags()
                .stream()
                .map(DeviceTagEntity::of)
                .collect(Collectors.toList()));
        }).thenReturn(this);
    }

    public synchronized DeviceDetail with(List<DeviceTagEntity> tags) {

        Map<String, DeviceTagEntity> map = Stream
            .concat(tags.stream(), this.tags.stream())
            .collect(
                Collectors.toMap(
                    DeviceTagEntity::getKey,
                    Function.identity(),
                    (_1, _2) -> StringUtils.hasText(_1.getValue()) ? _1 : _2));

        this.tags = new ArrayList<>(map.values());
        this.tags.sort(Comparator.comparing(DeviceTagEntity::getCreateTime));

        return this;
    }

    public DeviceDetail with(DeviceProductEntity productEntity) {
        if (StringUtils.isEmpty(metadata)) {
            setMetadata(productEntity.getMetadata());
        }
        if (CollectionUtils.isEmpty(configuration)) {
            setConfiguration(productEntity.getConfiguration());
        }
        setProtocol(productEntity.getMessageProtocol());
        setTransport(productEntity.getTransportProtocol());

        setProductId(productEntity.getId());
        setProductName(productEntity.getName());
        setDeviceType(productEntity.getDeviceType());
        return this;
    }

    public DeviceDetail with(DeviceInstanceEntity device) {

        setId(device.getId());
        setName(device.getName());
        setState(device.getState());
        setRegisterTime(device.getRegistryTime());
        setCreateTime(device.getCreateTime());

        if (!CollectionUtils.isEmpty(device.getConfiguration())) {
            setConfiguration(device.getConfiguration());
        }
        if (StringUtils.hasText(device.getDeriveMetadata())) {
            setMetadata(device.getDeriveMetadata());
        }

        return this;
    }

}
