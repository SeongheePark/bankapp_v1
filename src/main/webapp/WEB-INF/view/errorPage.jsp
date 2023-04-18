<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page isErrorPage="true"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
</head>
<body>
	<h1> 에러 페이지 </h1>
		<p>에러 코드 : ${statusCode}</p>
		<p>에러 메세지 : ${message}</p>
</body>
</html>