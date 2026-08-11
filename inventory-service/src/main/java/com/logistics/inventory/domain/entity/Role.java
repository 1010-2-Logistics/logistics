package com.logistics.inventory.domain.entity;

public enum Role {
    MASTER,

    HUB_MANAGER,

    HUB_DELIVERY_MANAGER,

    COMPANY_MANAGER,

    COMPANY_DELIVERY_MANAGER,

    ORDER_SERVICE
    ;

    public boolean isMaster() {
        return this == MASTER;
    }

    public boolean isHubManager() {
        return this == HUB_MANAGER;
    }

    public boolean isCompanyManager() {
        return this == COMPANY_MANAGER;
    }
}
