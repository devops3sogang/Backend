# 🎓 교내식당 & 학교 근처 맛집 리뷰 사이트 (Backend)

- 서강대학교 학생들을 위한 교내 식당 및 학교 주변 맛집 정보/리뷰 웹 서비스의 백엔드 API 서버입니다.
- 흩어져 있는 맛집 정보를 통합하여 제공하고, 학생들의 솔직한 피드백을 공유하는 플랫폼을 목표로 합니다.

---

## ✨ 주요 기능

- **사용자 인증**: JWT(JSON Web Token) 기반의 회원가입 및 로그인
- **사용자 프로필 관리**: 로그인한 사용자의 프로필 조회, 수정(닉네임/비밀번호), 회원 탈퇴
- **맛집 정보 관리**: 교내/외 식당 정보 등록 및 목록/상세 조회
- **리뷰 관리**: 특정 맛집에 대한 리뷰 작성, 조회 및 **좋아요(Like)** 기능
- **교내 식당 메뉴**: 특정 날짜의 교내 식당 주간 메뉴 조회
- **메뉴 자동 업데이트**: Python 크롤러를 이용한 주간 메뉴 자동 크롤링 및 DB 저장 (`@Scheduled`)
- **API 문서화**: Swagger UI를 통한 API 명세 자동화 및 테스트 환경 제공

---

## 🛠️ 기술 스택

- **Language**: Java 21
- **Framework**: Spring Boot 3.x
- **Database**: MongoDB
- **Authentication**: Spring Security, JWT (jjwt 라이브러리)
- **Libraries**:
  - Spring Data MongoDB
  - Lombok
  - Springdoc OpenAPI (Swagger UI)
- **Build Tool**: Gradle
- **External Script**: Python (for crawling)

---

## 🚀 시작하기

### 사전 요구사항

- Java 21 (JDK)
- Gradle
- MongoDB
- Python

### 설치 및 실행

#### 1. 리포지토리 클론

```bash
git clone https://github.com/devops3sogang/Backend.git
cd Backend
```

#### 2. Python 환경 설정 (크롤러용)

```bash
# 1. 가상 환경 생성
python -m venv venv

# 2. 가상 환경 활성화 (Windows)
.\venv\Scripts\activate
# (macOS/Linux의 경우: source venv/bin/activate)

# 3. 필요한 라이브러리 설치
pip install -r requirements.txt
```

#### 3. application.properties 설정

- src/main/resources/application.properties 파일을 열어 본인의 로컬 MongoDB 환경에 맞게 URI를 설정하고, 안전한 JWT 시크릿 키를 발급받아 입력합니다.

```Properties
# MongoDB 연결 설정
spring.data.mongodb.uri=mongodb://localhost:27017/campus_food

# JWT 시크릿 키 (반드시 길고 복잡한 랜덤 문자열로 교체)
jwt.secret=n2r5u8x/A?D(G+KbPeShVmYp3s6v9y$B&E)H@McQfTjWnZq4t7w!z%C*F-JaNdRg
```

#### 4. 애플리케이션 빌드 및 실행

- IntelliJ IDEA에서 DevOps3SogangApplication.java 파일을 직접 실행하거나, 아래 명령어를 사용합니다.

```Bash
1. ./gradlew build

2. 터미널에서 바로 실행:
java -jar build/libs/backend-0.0.1-SNAPSHOT.jar

또는 Gradle로 실행:
./gradlew bootRun
```

---

## 🌐 맛집 목록 확인 - HTML 뷰 (Thymeleaf)

서버 실행 후 브라우저에서 아래 주소로 접속: <br>
http://localhost:8080/api/restaurants-view

예시 화면:

| ID                       | 이름     | 타입       | 카테고리 | 주소                            |
| :----------------------- | :------- | :--------- | :------- | :------------------------------ |
| 68e785be844b4ecbcd4ec6e6 | 거구장   | OFF_CAMPUS | 한식     | 서울특별시 마포구 백범로 17     |
| 68e7ada66a3f6293fb9662e6 | 서강곱창 | OFF_CAMPUS | 한식     | 서울특별시 마포구 신수동 93-2   |
| 68e7adac6a3f6293fb9662e7 | 핵밥     | OFF_CAMPUS | 한식     | 서울특별시 마포구 백범로 28     |
| 68e7adbe6a3f6293fb9662e8 | 서브웨이 | OFF_CAMPUS | 양식     | 서울특별시 마포구 서강로16길 48 |

---

## 📖 API 사용법

애플리케이션 실행 후, 아래 두 가지 방법으로 API를 테스트할 수 있습니다.

### 1. Swagger UI

- 브라우저에서 http://localhost:8080/api/swagger-ui/index.html 로 접속하여 모든 API를 GUI 환경에서 테스트할 수 있습니다.

- 인증이 필요한 API는 Authorize 버튼을 눌러 로그인 후 받은 토큰을 Bearer [토큰] 형식으로 등록해야 합니다.

### 2. Postman 등 API 테스트 도구

- http://localhost:8080/api을 기본 URL로 하여 아래의 주요 API들을 호출할 수 있습니다.

### 주요 API 엔드포인트

### 🔐 인증 (Auth)

| Method | URL            | 설명                   | 인증 필요 |
| :----- | :------------- | :--------------------- | :-------- |
| POST   | /auth/register | 회원가입               | X         |
| POST   | /auth/login    | 로그인 (JWT 토큰 발급) | X         |
| POST   | /auth/logout   | 로그아웃               | X         |

### 👤 사용자 (Users)

| Method | URL       | 설명                               | 인증 필요 |
| :----- | :-------- | :--------------------------------- | :-------- |
| GET    | /users/me | 내 프로필 조회 (리뷰, 좋아요 포함) | O         |
| PUT    | /users/me | 내 프로필 수정                     | O         |
| DELETE | /users/me | 유저 데이터 삭제                   | O         |

### 👑 관리자 (Admin)

| Method | URL                               | 설명                              | 인증 필요 |
| :----- | :-------------------------------- | :-------------------------------- | :-------- |
| DELETE | /admin/restaurants/{restaurantId} | 맛집 삭제 (관련 리뷰/좋아요 포함) | O (ADMIN) |

### 🍽️ 맛집 (Restaurants)

| Method | URL                         | 설명                         | 인증 필요 |
| :----- | :-------------------------- | :--------------------------- | :-------- |
| POST   | /restaurants                | 맛집 등록 (관리자만 가능)    | O (ADMIN) |
| GET    | /restaurants                | 맛집 목록 조회 (필터링 가능) | X         |
| GET    | /restaurants/{restaurantId} | 맛집 상세 정보 조회          | X         |

### ✍️ 리뷰 (Reviews)

| Method | URL                                  | 설명                           | 인증 필요 |
| :----- | :----------------------------------- | :----------------------------- | :-------- |
| POST   | /restaurants/{restaurantId}/reviews  | 특정 맛집에 리뷰 작성          | O         |
| GET    | /reviews?restaurantId={restaurantId} | 특정 맛집의 리뷰 목록 조회     | X         |
| PUT    | /reviews/{reviewId}                  | 리뷰 수정 (본인만 가능)        | O         |
| DELETE | /reviews/{reviewId}                  | 리뷰 삭제 (본인/관리자만 가능) | O         |
| POST   | /reviews/{reviewId}/like             | 리뷰 '좋아요' 토글             | O         |

### 🏫 교내식당 (On-Campus Menus)

| Method | URL                                | 설명                       | 인증 필요 |
| :----- | :--------------------------------- | :------------------------- | :-------- |
| GET    | /on-campus-menus?date={YYYY-MM-DD} | 특정 날짜의 교내 식단 조회 | X         |
