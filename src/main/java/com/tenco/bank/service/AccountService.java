package com.tenco.bank.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tenco.bank.dto.DepositFormDto;
import com.tenco.bank.dto.TransferFormDto;
import com.tenco.bank.dto.WithdrawFormDto;
import com.tenco.bank.dto.saveFormDto;
import com.tenco.bank.dto.response.HistoryDto;
import com.tenco.bank.handler.exception.CustomRestfullException;
import com.tenco.bank.repository.interfaces.AccountRepository;
import com.tenco.bank.repository.interfaces.HistoryRepository;
import com.tenco.bank.repository.model.Account;
import com.tenco.bank.repository.model.History;

@Service // IoC 대상 + 싱글톤 관리
public class AccountService {
	@Autowired
	private AccountRepository accountRepository;
	@Autowired
	private HistoryRepository historyRepository;

	/**
	 * 계좌 생성 기능
	 * 
	 * @param saveFormDto
	 * @param principalId
	 */
	@Transactional
	// 서비스 로직 만들기
	public void createAccount(saveFormDto saveFormDto, Integer principalId) {
		Account account = new Account();
		account.setNumber(saveFormDto.getNumber());
		account.setPassword(saveFormDto.getPassword());
		account.setBalance(saveFormDto.getBalance());
		account.setUserId(principalId);
		int resultRowCount = accountRepository.insert(account);
		if (resultRowCount != 1) {
			throw new CustomRestfullException("계좌 생성에 실패했어요", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	// 단일 계좌 검색 기능
	public Account readAccount(Integer id) {
		Account accountEntity = accountRepository.findById(id);
		if(accountEntity == null) {
			throw new CustomRestfullException("해당 계좌를 찾을 수 없습니다", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return accountEntity;
	}
	
	/**
	 * 
	 * @param type = [all, deposit, withdraw]
	 * @param id (account_id)
	 * @return 입금, 출금, 입출금 거래내역 
	 */
	public List<HistoryDto> readHistoryListByAccount(String type, Integer id){
		List<HistoryDto> historyDtos = historyRepository.findByIdHistoryType(type, id);
		historyDtos.forEach(e -> {
			System.out.println(e);
		});
		return historyDtos;
	}

	// 계좌 목록 보기 기능
	@Transactional
	public List<Account> readAccountList(Integer userId) {
		List<Account> list = accountRepository.findByUserId(userId);
		return list;
	}

	// 출금 기능
	// 1. 계좌 존재 여부 확인 ㅡ> select query
	// 2. 본인 계좌 확인
	// 3. 계좌 비번 확인
	// 4. 잔액 여부 확인
	// 5. 출금 처리 ㅡ> update query
	// 6. 거래 내역 등록 ㅡ> insert query
	@Transactional
	public void updateAccountWithdraw(WithdrawFormDto withdrawFormDto, Integer principalId) {
		Account accountEntity = accountRepository.findByNumber(withdrawFormDto.getWAccountNumber());
		// 계좌 존재 여부 확인
		if (accountEntity == null) {
			throw new CustomRestfullException("계좌가 없습니다", HttpStatus.BAD_REQUEST);
		}
		// 본인 계좌 확인
		if (accountEntity.getUserId() != principalId) {
			throw new CustomRestfullException("본인 소유 계좌가 아닙니다", HttpStatus.UNAUTHORIZED);
		}
		// 계좌 비번 확인
		if (accountEntity.getPassword().equals(withdrawFormDto.getWAccountPassword()) == false) {
			throw new CustomRestfullException("비밀번호가 틀렸습니다", HttpStatus.UNAUTHORIZED);
		}
		// 잔액 여부 확인
		if (accountEntity.getBalance() < withdrawFormDto.getAmount()) {
			throw new CustomRestfullException("출금 잔액이 부족합니다", HttpStatus.BAD_REQUEST);
		}
		// 출금 처리
		// accountEntity.setBalance(accountEntity.getBalance() -
		// withdrawFormDto.getAmount());
		accountEntity.withdraw(withdrawFormDto.getAmount());
		accountRepository.updateById(accountEntity);
		// 거래 내역 등록
		History history = new History();
		history.setAmount(withdrawFormDto.getAmount());
		history.setWBalance(accountEntity.getBalance()); // !!
		history.setDBalance(null);
		history.setWAccountId(accountEntity.getId());
		history.setDAccountId(null);
		int resultRowCount = historyRepository.insert(history);
		if (resultRowCount != 1) {
			throw new CustomRestfullException("정상 처리 되지 않았습니다", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// 입금 처리
	// 1. 계좌 존재 여부 확인 ㅡ> select query
	// 2. 입금 처리 ㅡ> update query
	// 3. 거래 내역 등록 처리 ㅡ> insert

	@Transactional
	public void updateAccountDeposit(DepositFormDto depositFormDto) {
		Account accountEntity = accountRepository.findByNumber(depositFormDto.getDAccountNumber());
		if (accountEntity == null) {
			throw new CustomRestfullException("존재하지 않는 계좌입니다", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		// 객체 상태값 변경
		accountEntity.deposit(depositFormDto.getAmount());
		accountRepository.updateById(accountEntity);

		// 거래 내역 등록 처리
		History history = new History();
		history.setAmount(depositFormDto.getAmount());
		history.setWBalance(null);
		history.setDBalance(accountEntity.getBalance());
		history.setWAccountId(null);
		history.setDAccountId(accountEntity.getId());

		int resultRowCount = historyRepository.insert(history);
		if (resultRowCount != 1) {
			throw new CustomRestfullException("정상 처리 되지 않았습니다", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// 이체 기능 만들기
	// 출금 계좌 존재 여부 확인 - select query
	// 입금 계좌 존재 여부 확인 - select query
	// 출금 계좌 본인 소유 확인 - 1번에서 뽑은 E = session 값 비교
	// 출금 계좌 비번 확인 - 1번에서 뽑은 E = Dto 비교
	// 출금 계좌 잔액 여부 확인 - 1번에서 뽑은 E = Dto 비교
	// 출금 계좌 잔액 변경 - update query
	// 입금 계좌 잔액 변경 - update query
	// 거래 내역 저장 - insert
	@Transactional
	public void updateAccountTransfer(TransferFormDto transferFormDto, Integer principalId) {
		// 출금 계좌 존재 여부 확인
		Account withdrawAccountEntity = accountRepository.findByNumber(transferFormDto.getWAccountNumber());
		if (withdrawAccountEntity == null) {
			throw new CustomRestfullException("출금 계좌가 존재하지 않습니다", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		// 입금 계좌 존재 여부 확인
		Account depositAccountEntity = accountRepository.findByNumber(transferFormDto.getDAccountNumber());
		if (depositAccountEntity == null) {
			throw new CustomRestfullException("입금 계좌가 존재하지 않습니다", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		// 출금 계좌 본인 소유 확인
		withdrawAccountEntity.checkOwner(principalId);
		// 출금 계좌 비밀번호 확인
		withdrawAccountEntity.checkPassword(transferFormDto.getWAccountPassword());
		// 출금 계좌 잔액 여부 확인
		withdrawAccountEntity.checkBalance(transferFormDto.getAmount());
		// 출금 계좌 잔액 변경 ( 객체 상태값 변경 - 계좌 잔액 수정 처리)
		withdrawAccountEntity.withdraw(transferFormDto.getAmount());
		accountRepository.updateById(withdrawAccountEntity); // 변경된 객체 상태값으로 update 처리
		// 입금 계좌 잔액 변경
		depositAccountEntity.deposit(transferFormDto.getAmount());
		accountRepository.updateById(depositAccountEntity);
		// 거래 내역 저장
		History history = new History();
		history.setAmount(transferFormDto.getAmount());
		history.setWAccountId(withdrawAccountEntity.getId());
		history.setDAccountId(depositAccountEntity.getId());
		history.setWBalance(withdrawAccountEntity.getBalance());
		history.setDBalance(depositAccountEntity.getBalance());

		int resultRowCount = historyRepository.insert(history);
		if (resultRowCount != 1) {
			throw new CustomRestfullException("이체가 정상처리 되지 않았습니다", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
