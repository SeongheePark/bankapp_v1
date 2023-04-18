package com.tenco.bank.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tenco.bank.dto.SignInFormDto;
import com.tenco.bank.dto.SignUpFormDto;
import com.tenco.bank.handler.exception.CustomRestfullException;
import com.tenco.bank.repository.interfaces.UserRepository;
import com.tenco.bank.repository.model.User;

@Service // IoC 대상
public class UserService {
	// DAO 만들지 않아도 된다.. 인터페이스 가져오기
	@Autowired // DI 처리 (객체 생성 시 의존 주의 처리)
	private UserRepository userRepository;
	
	@Transactional 
	// 메서드 호출이 시작될 때 트랜잭션 시작
	// 메서드 종료 시 트랜잭션 종료 (commit)
	public void signUp(SignUpFormDto signUpFormDto) {
		// DB 접근 처리
		// User
		int result = userRepository.insert(signUpFormDto);
		if (result != 1) {
			throw new CustomRestfullException("회원가입 실패", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	// 로그인 서비스
	// userEntity 응답
	public User signIn(SignInFormDto signInFormDto) {
		User userEntity = userRepository.findByUsernameAndPassword(signInFormDto);
		if(userEntity == null) {
			throw new CustomRestfullException("아이디 혹은 비밀번호를 잘못 입력하였습니다", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return userEntity;
	}
}
