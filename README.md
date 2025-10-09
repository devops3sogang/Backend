# 🎓 교내식당 & 학교 근처 맛집 리뷰 사이트 (Backend)

## 📝 프로젝트 소개
서강대학교 학생들을 위한 교내 식당 및 학교 주변 맛집 정보/리뷰 웹 서비스의 백엔드 API 서버입니다. 흩어져 있는 맛집 정보를 통합하여 제공하고, 학생들의 솔직한 피드백을 공유하는 플랫폼을 목표로 합니다.

이 프로젝트는 Spring Boot와 MongoDB를 기반으로 구축되었으며, JWT를 이용한 현대적인 인증 방식을 적용했습니다.

--- 

## ✨ 주요 기능
- 사용자 인증: JWT(JSON Web Token) 기반의 회원가입 및 로그인 API

- 맛집 정보 관리: 교내/외 식당 정보 등록 및 목록/상세 조회

- 리뷰 관리: 특정 맛집에 대한 리뷰 작성 및 조회

- 교내 식당 메뉴: 특정 날짜의 교내 식당 주간 메뉴 조회

- API 문서화: Swagger UI를 통한 API 명세 자동화 및 테스트 환경 제공

---

## 🛠️ 기술 스택
- 언어: Java 21

- 프레임워크: Spring Boot 3.x

- 데이터베이스: MongoDB

- 인증: Spring Security, JWT (jjwt 라이브러리)

- 라이브러리:

   - Spring Data MongoDB

   - Lombok

   - Springdoc OpenAPI (Swagger UI)

- 빌드 도구: Gradle

---

## 🚀 시작하기
### 사전 요구사항
- Java 21 (JDK)

- Gradle

- MongoDB

### 설치 및 실행
#### 1. 리포지토리 클론
```
Bash

git clone https://github.com/your-username/your-repository-name.git
cd your-repository-name
```

#### 2. application.properties 설정
- src/main/resources/application.properties 파일을 열어 본인의 로컬 MongoDB 환경에 맞게 URI를 설정하고, 안전한 JWT 시크릿 키를 발급받아 입력합니다.

```
Properties

# MongoDB 연결 설정
spring.data.mongodb.uri=mongodb://localhost:27017/sogang-db

# JWT 시크-릿 키 (반드시 길고 복잡한 랜덤 문자열로 교체)
jwt.secret=n2r5u8x/A?D(G+KbPeShVmYp3s6v9y$B&E)H@McQfTjWnZq4t7w!z%C*F-JaNdRg
```

#### 3. 애플리케이션 빌드 및 실행
```
Bash

./gradlew build
java -jar build/libs/backend-0.0.1-SNAPSHOT.jar
또는 IntelliJ IDEA에서 DevOps3SogangApplication.java 파일을 직접 실행합니다.
```

---

## 🌐 맛집 목록 확인 - HTML 뷰 (Thymeleaf)

서버 실행 후 브라우저에서 아래 주소로 접속: <br>
http://localhost:8080/restaurants-view

예시 화면:

|ID|	이름|	타입|	카테고리| 	주소     |
|:---|:---|:---|:---|:--------|
|68e785be844b4ecbcd4ec6e6|	거구장|	OFF_CAMPUS|	한식| string  |
|68e7ada66a3f6293fb9662e6|	서강곱창|	OFF_CAMPUS|	한식| 	string |
|68e7adac6a3f6293fb9662e7|	핵밥|	OFF_CAMPUS|	한식| 	string |
|68e7adbe6a3f6293fb9662e8|	서브웨이|	OFF_CAMPUS|	양식| 	string |

---

## 📖 API 사용법
애플리케이션 실행 후, 아래 두 가지 방법으로 API를 테스트할 수 있습니다.

### 1. Swagger UI

- 브라우저에서 http://localhost:8080/swagger-ui/index.html 로 접속하여 모든 API를 GUI 환경에서 테스트할 수 있습니다.

- 인증이 필요한 API는 Authorize 버튼을 눌러 로그인 후 받은 토큰을 Bearer [토큰] 형식으로 등록해야 합니다.

### 2. Postman 등 API 테스트 도구

- http://localhost:8080을 기본 URL로 하여 아래의 주요 API들을 호출할 수 있습니다.

### 주요 API 엔드포인트

|Method| URL                                     | 설명               |인증 필요|
|:---|:----------------------------------------|:-----------------|:---|
|POST| /api/auth/signup                        |	회원가입|	X|
|POST	| /api/auth/login	                        |로그인 (JWT 토큰 발급)	|X
|POST	| /api/restaurants	                       |맛집 등록	|O
|GET	| /api/restaurants	                       |맛집 목록 조회 (필터링 가능)	|O
|GET	| /api/restaurants/{id}	                  |맛집 상세 정보 조회	|O
|POST	| /api/restaurants/{restaurantId}/reviews	|특정 맛집에 리뷰 작성	|O
|GET	| /api/restaurants/{restaurantId}/reviews	|특정 맛집의 리뷰 목록 조회	|O
|GET	| /api/on-campus-menus?date={YYYY-MM-DD}	 |특정 날짜의 교내 식단 조회	|O

___