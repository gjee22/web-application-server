# 실습을 위한 개발 환경 세팅
* https://github.com/slipp/web-application-server 프로젝트를 자신의 계정으로 Fork한다. Github 우측 상단의 Fork 버튼을 클릭하면 자신의 계정으로 Fork된다.
* Fork한 프로젝트를 eclipse 또는 터미널에서 clone 한다.
* Fork한 프로젝트를 eclipse로 import한 후에 Maven 빌드 도구를 활용해 eclipse 프로젝트로 변환한다.(mvn eclipse:clean eclipse:eclipse)
* 빌드가 성공하면 반드시 refresh(fn + f5)를 실행해야 한다.

# 웹 서버 시작 및 테스트
* webserver.WebServer 는 사용자의 요청을 받아 RequestHandler에 작업을 위임하는 클래스이다.
* 사용자 요청에 대한 모든 처리는 RequestHandler 클래스의 run() 메서드가 담당한다.
* WebServer를 실행한 후 브라우저에서 http://localhost:8080으로 접속해 "Hello World" 메시지가 출력되는지 확인한다.

# 각 요구사항별 학습 내용 정리
* 구현 단계에서는 각 요구사항을 구현하는데 집중한다. 
* 구현을 완료한 후 구현 과정에서 새롭게 알게된 내용, 궁금한 내용을 기록한다.
* 각 요구사항을 구현하는 것이 중요한 것이 아니라 구현 과정을 통해 학습한 내용을 인식하는 것이 배움에 중요하다. 

### 요구사항 1 - http://localhost:8080/index.html로 접속시 응답
## 소캣 기본 원리
1. ServerSocket은 클라이언트가 처음 웹사이트를 접속하는 것을 기다린다.
2. 클라이언트가 접속하면 해당 클라이언트에게 할당된 새로운 Socket을 만들어서 RequestHandler로 요청을 처리한다.
3. 한 클라이언트마다 새로운 Socket이 만들어지며 해당 연결에 대해 Thread 두 개가 만들어진다 - 클라이언트와 서버 각각 하나씩.

## 구현 방식
1. 클라이언트가 웹사이트에 접속하면 해당 요청을 분해한다.
2. index.html를 요청할 시에 해당 HTML 파일의 내용을 보낸다. 그 외의 요청들은 Hello World로 답한다.

### 요구사항 2 - get 방식으로 회원가입
* 

### 요구사항 3 - post 방식으로 회원가입
* 

### 요구사항 4 - redirect 방식으로 이동
HTTP Response 헤더의 Location 필드를 이용하면 이동하는 페이지를 지정할 수 있다.

### 요구사항 5 - cookie
* 

### 요구사항 6 - stylesheet 적용
* 

### heroku 서버에 배포 후
* 