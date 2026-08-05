package com.logistics.product.domain.entity;

import java.util.Arrays;
import java.util.List;

public enum Role {
	MASTER,

  HUB_MANAGER,

  HUB_DELIVERY_MANAGER,

  COMPANY_MANAGER,

  COMPANY_DELIVERY_MANAGER

  ;
	
  public static final List<String> roleList() {
		return Arrays.stream(Role.values())
				.map(role -> role.name())
				.toList();
	}
  
}
