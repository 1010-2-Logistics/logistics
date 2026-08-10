package com.logistics.hub.application.dto.command;

import java.math.BigDecimal;

public record HubUpdateCommand(
                               String hubName,
                               String hubAddress,
                               BigDecimal latitude,
                               BigDecimal longitude) {


}
