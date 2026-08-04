package com.logistics.company.domain.entity;

import java.util.UUID;

import com.logistics.company.global.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "p_company")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Company extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "company_id")
  private UUID companyId;

  @Column(name = "hub_id", nullable = false, updatable = false)
  private UUID hubId;
  
  @Column(name = "company_name", nullable = false)
  private String companyName;
  
  @Column(name = "company_address", nullable = false, updatable = false)
  private String companyAddress;
  
  @Column(name = "company_type", nullable = false, updatable = false)
  @Enumerated(EnumType.STRING)
  private CompanyType companyType;
  
  public static Company create(UUID hubId, String companyName, String companyAddress, CompanyType companyType) {
  	Company company = new Company();
  	
  	company.hubId = hubId;
  	company.companyName = companyName;
  	company.companyAddress = companyAddress;
  	company.companyType = companyType;
  	
  	return company;
  }
  
  public void updateCompanyName(String companyName) {
  	this.companyName = companyName;
  }
  
}
