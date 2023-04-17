package com.tenco.bank.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tenco.bank.dto.SignInFormDto;
import com.tenco.bank.dto.SignUpFormDto;

@Controller
@RequestMapping("/user")
public class UserController {

	// http://localhost:8080/user/sign-up
	@GetMapping("/sign-up")
	public String signUp() {
		// prefix, suffix
		return "/user/signUp";
	}
	
	/**
	 * 회원가입 처리
	 * @param signUpFormDto
	 * @return 리다이렉트 로그인 페이지
	 */
	// MIME TYPE : x-www-form-urlencoded
	// form : Query String 방식으로 들어온다
	// dto : object mapper 처리
	@PostMapping("/sign-up")
	public String signUpProc(SignUpFormDto signUpFormDto) {
		return "redirect:/user/sign-in";
	}
	
	/**
	 * 로그인 폼
	 * @return 로그인 페이지 리턴
	 */
	@GetMapping("/sign-in")
	public String signIn() {
		return "/user/signIn";
	}
	
	/**
	 * 로그인 처리
	 * @param signInFormDto
	 * @return 메인 페이지 이동 (수정 예정)
	 */ 
	@PostMapping("/sign-in")
	public String signInProc(SignInFormDto signInFormDto) {
		return "/test/main";
	}
	

}