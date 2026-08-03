package com.logistics.template.infrastructure.persistence.repository;

import org.springframework.stereotype.Repository;

import com.logistics.template.domain.repository.CompanyQueryRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CompanyQueryRepositoryImpl implements CompanyQueryRepository {

  private final CompanyJpaRepository jpaRepository;
  
  
}
