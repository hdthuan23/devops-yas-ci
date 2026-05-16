package com.yas.order.mapper;

import com.yas.order.model.csv.OrderItemCsv;
import com.yas.order.viewmodel.order.OrderBriefVm;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    @Mapping(target = "phone", source = "billingAddressVm.phone")
    @Mapping(target = "id", source = "id")
    OrderItemCsv toCsv(OrderBriefVm orderItem);
}
