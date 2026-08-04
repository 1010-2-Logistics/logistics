package com.logistics.company.infrastructure.persistence.repository;

import org.springframework.stereotype.Repository;

import com.logistics.company.domain.repository.CompanyQueryRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CompanyQueryRepositoryImpl implements CompanyQueryRepository {

  private final CompanyJpaRepository jpaRepository;
  
  
}
