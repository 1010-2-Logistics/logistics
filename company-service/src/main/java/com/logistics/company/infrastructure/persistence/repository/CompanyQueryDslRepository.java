package com.logistics.company.infrastructure.persistence.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.logistics.company.domain.entity.Company;
import com.logistics.company.domain.entity.CompanyType;
import com.logistics.company.domain.entity.QCompany;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CompanyQueryDslRepository {

	private final JPAQueryFactory queryFactory;
	
	private final QCompany company = QCompany.company;
	
	public Page<Company> searchCompany(
			String companyName,
			UUID hubId,
			CompanyType companyType,
			Pageable pageable) {
		
		List<Company> content = queryFactory
				.selectFrom(company)
				.where(
						hubIdEquals(hubId),
						companyTypeEquals(companyType),
						companyNameContains(companyName),
						company.deletedAt.isNull()
						)
				.orderBy(getOrder(pageable))
				.offset(pageable.getOffset())
				.limit(pageable.getPageSize())
				.fetch();
		
		Long count = queryFactory
				.select(company.count())
				.from(company)
				.where(
						hubIdEquals(hubId),
						companyTypeEquals(companyType),
						companyNameContains(companyName),
						company.deletedAt.isNull()
						)
				.fetchOne();
		
		long total = count == null
				? 0L
				: count;
		
		return new PageImpl<Company>(content, pageable, total);
	}
	
	private BooleanExpression hubIdEquals(UUID hubId) {
		return hubId != null
				? company.hubId.eq(hubId)
				: null;
	}
	
	private BooleanExpression companyTypeEquals(CompanyType companyType) {
		return companyType != null
				? company.companyType.eq(companyType)
				: null;
	}
	
	private BooleanExpression companyNameContains(String companyName) {
		return StringUtils.hasText(companyName)
				? company.companyName.containsIgnoreCase(companyName)
				: null;
	}
	
	private OrderSpecifier<?> getOrder(Pageable pageable) {
		Sort.Order orderBy = pageable.getSort()
				.getOrderFor("createdAt");
		
		if(orderBy == null || orderBy.isDescending()) {
			return company.createdAt.desc();
		}
		
		return company.createdAt.asc();
	}
	
}
