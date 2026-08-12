package com.logistics.company.application.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.logistics.company.application.dto.query.CompanySearchQuery;
import com.logistics.company.application.dto.result.OrderedCompanyInfoResultDto;
import com.logistics.company.domain.OrderedCompanyInfo;
import com.logistics.company.domain.entity.Company;
import com.logistics.company.domain.repository.CompanyQueryRepository;
import com.logistics.company.global.exception.CompanyErrorCode;
import com.logistics.company.global.exception.CompanyException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompanyQueryService {

	private final CompanyQueryRepository companyQueryRepository;
	
	public Company findByCompany(UUID companyId) {
		return companyQueryRepository.findByCompanyIdAndDeletedAtIsNull(companyId)
				.orElseThrow(() -> new CompanyException(CompanyErrorCode.COMPANY_NOT_FOUND));
	}
	
	public Optional<Company> findOptionalByCompany(UUID companyId) {
		return companyQueryRepository.findByCompanyIdAndDeletedAtIsNull(companyId);
	}
	
	public Company findByCompanyAllStatus(UUID companyId) {
		return companyQueryRepository.findByCompanyId(companyId)
				.orElseThrow(() -> new CompanyException(CompanyErrorCode.COMPANY_NOT_FOUND));
	}
	
	/**
	 * 주문 시 출발 업체와 도착 업체의 정보를 조회합니다.
	 * 출발 업체의 경우 생산 업체만 가능하며, 도착 업체의 경우 수령 업체만 가능합니다.
	 * 
	 * 업체 타입이 맞지 않는 경우 COMPANY_TYPE_FOR_ORDER 예외가 발생합니다.
	 * 
	 * @param startCompanyId: 출발업체 ID
	 * @param endCompanyId: 도착업체 ID
	 * @return {
	 * 		"startCompanyId": "출발업체 - UUID",
	 * 		"startHubId": "출발업체 소속 허브 - UUID",
	 * 		"startCompanyAddress": "출발업체 주소",
	 * 		"endCompanyId": "도착업체 - UUID",
	 * 		"endHubId": "도착업체 소속 허브 - UUID",
	 * 		"endCompanyAddress": "도착업체 주소"
	 * }
	 */
	public OrderedCompanyInfoResultDto findOrderedCompanyInfo(UUID startCompanyId, UUID endCompanyId) {
		OrderedCompanyInfo orderedCompanyInfo = companyQueryRepository.findOrderedCompanyInfo(startCompanyId, endCompanyId)
				.orElseThrow(() -> new CompanyException(CompanyErrorCode.COMPANY_NOT_FOUND));
		
		return OrderedCompanyInfoResultDto.from(orderedCompanyInfo);
	}
	
	public List<UUID> findIdsByHubId(UUID hubId) {
		return companyQueryRepository.findIdsByHubId(hubId);
	}

	public Page<Company> searchCompany(CompanySearchQuery query) {
		return companyQueryRepository.searchCompany(
				query.companyName(),
				query.hubId(),
				query.companyType(),
				query.pageable()
		);
	}

	public boolean checkCompanyNameForCreate(UUID hubId, String companyName) {
		return companyQueryRepository.existsByHubIdAndCompanyNameAndDeletedAtIsNull(hubId, companyName);
	}
	
	public boolean checkCompanyNameForUpdate(UUID hubId, String companyName, UUID companyId) {
		return companyQueryRepository.existsByHubIdAndCompanyNameAndCompanyIdAndDeletedAtIsNull(hubId, companyName, companyId);
	}
	
}
