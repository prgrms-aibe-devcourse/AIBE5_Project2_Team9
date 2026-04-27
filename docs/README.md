<div align="center">
  <img src="../src/main/resources/static/images/logo.png" alt="PICKASSO 로고" width="120"/>

# PICKASSO

> 소중한 순간, 색감 있는 작가를 만나보세요

</div>

사용자와 전문 스냅 사진작가를 연결하는 맞춤형 O2O 매칭 플랫폼입니다.
사진작가는 포트폴리오와 서비스 플랜을 등록하고, 사용자는 지역·카테고리·가격·분위기 조건으로 작가를 찾아 예약할 수 있습니다.

---

## 목차

- [주요 기능](#주요-기능)
- [기술 스택](#기술-스택)
- [화면 구성](#화면-구성)
- [도메인 모델](#도메인-모델)
- [프로젝트 구조](#프로젝트-구조)
- [실행 방법](#실행-방법)
- [팀 소개](#-팀-소개)

---

## 주요 기능

### 일반 사용자
- 카카오 소셜 로그인 / 자체 회원가입
- 지역·카테고리·가격·날짜 기반 작가 검색 및 필터링
- AI 자연어 추천 기능 "야외 분위기 데이트 스냅 추천해줘"
- 캐시 충전 및 서비스 예약·취소
- 예약 완료 후 리뷰 작성

### 사진작가
- 포트폴리오·서비스 플랜(가격·촬영 시간·보정 수량·납품일) 등록 및 관리
- 예약 승인·거절, 주간 캘린더 대시보드
- 리뷰 현황 및 평점 분석
- 정산 현황 조회

---

## 기술 스택

| 분류       | 기술                                                                                                                                                                                                                                                                                                                                                                     |
|----------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Backend  | ![Springboot](https://img.shields.io/badge/Spring_boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white) ![Springsecurity](https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white) ![Springdatajpa](https://img.shields.io/badge/Spring_Data_JPA-6DB33F?style=for-the-badge&logo=spring&logoColor=white)|
| Frontend | ![Thymeleaf](https://img.shields.io/badge/Thymeleaf-%23005C0F.svg?style=for-the-badge&logo=Thymeleaf&logoColor=white) ![TailwindCSS](https://img.shields.io/badge/tailwindcss-%2338B2AC.svg?style=for-the-badge&logo=tailwind-css&logoColor=white)                                                                                                                     |
| DB       | ![mysql](https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white)                                                                                                                                                                                                                                                                     |
| Auth     | ![jwt](https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white)                                                                                                                                                                                                                                                                 |
| Storage  | ![Amazon S3](https://img.shields.io/badge/Amazon%20S3-FF9900?style=for-the-badge&logo=amazons3&logoColor=white)                                                                                                                                                                                                                                                        |
| AI       | ![Google Gemini](https://img.shields.io/badge/google%20gemini-8E75B2?style=for-the-badge&logo=google%20gemini&logoColor=white)                                                                                                                                                                                                                                                                                                                                                         |

---

## 화면 구성

| 영역 | 화면 |
|------|------|
| 공통 | 랜딩, 로그인, 회원가입 (사용자/작가), 계정 찾기 |
| 탐색 | 홈 (추천 서비스), 검색·필터, AI 추천, 서비스 상세 |
| 사용자 | 예약, 예약 완료, 마이페이지 (프로필·예약 현황), 리뷰 작성 |
| 작가 | 대시보드, 예약 관리, 서비스 등록·수정, 포트폴리오 편집, 리뷰·정산 현황 |

---

## 도메인 모델

| Entity | 설명 |
|--------|------|
| `Account` | 로그인 계정 (username, role) |
| `Member` | 일반 사용자 (이름, 이메일, 전화번호, 캐시 잔액) |
| `Photographer` | 사진작가 (Member 확장, 프로필·포트폴리오) |
| `Item` | 촬영 서비스 패키지 (카테고리, 설명, 취소 정책, 평점) |
| `Plan` | 서비스 가격 플랜 (촬영 시간, 원본/보정 수량, 납품일) |
| `Reservation` | 예약 (요청 → 승인/거절 → 완료 상태 흐름) |
| `Review` | 리뷰 (예약 완료 후 작성, 평점 자동 집계) |
| `CashHistory` | 캐시 충전·결제·환불 이력 |

---

## 프로젝트 구조

```
src/main/
├── java/com/pickkasso/pickkasso/
│   ├── global/          # 홈, AI 추천, 에러 처리, 공통 설정
│   ├── user/            # 회원가입, 로그인, 마이페이지, 예약, 캐시
│   ├── item/            # 서비스 등록·수정, 검색, 예약 가능 일정
│   └── review/          # 리뷰 작성 및 조회
└── resources/
    ├── templates/
    │   ├── layouts/     # 공통 레이아웃
    │   ├── fragments/   # 헤더, 푸터, 사이드바, 모달
    │   ├── common/      # 로그인, 회원가입, AI 추천
    │   ├── user/        # 사용자 마이페이지
    │   └── photographer/# 작가 대시보드
    └── static/
        ├── css/         # global.css (디자인 토큰, 폰트)
        ├── fonts/       # Pretendard, WhiteAngelica
        └── images/      # 로고 등 정적 이미지
```

---

## 실행 방법

### 사전 요구사항
- Java 17
- MySQL 8.x

### 환경 변수 설정

`application.properties` 또는 환경 변수로 아래 값을 설정해야 합니다.

```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/pickasso
spring.datasource.username=YOUR_DB_USERNAME
spring.datasource.password=YOUR_DB_PASSWORD

# 카카오 OAuth2
spring.security.oauth2.client.registration.kakao.client-id=YOUR_CLIENT_ID
spring.security.oauth2.client.registration.kakao.client-secret=YOUR_CLIENT_SECRET
spring.security.oauth2.client.registration.kakao.redirect-uri=YOUR_REDIRECT_URI

# AWS S3
cloud.aws.credentials.access-key=YOUR_ACCESS_KEY
cloud.aws.credentials.secret-key=YOUR_SECRET_KEY
cloud.aws.region.static=ap-southeast-2
cloud.aws.s3.bucket=YOUR_BUCKET_NAME

# Google Gemini
gemini.api.key=YOUR_GEMINI_API_KEY
```

### 빌드 및 실행

```bash
./gradlew bootRun
```

기본 포트: `80`

---

## 👤 팀 소개

<div align="center">

### 🚨 Team 9조대

<table>
<tr>
<td align="center">
<a href="https://github.com/Mi-no-Kim">
<img src="https://github.com/Mi-no-Kim.png" width="100px;" alt="김민호"/><br />
<sub><b>김민호</b></sub><br />
<sub>BE, DB</sub>
</a>
</td>
<td align="center">
<a href="https://github.com/Lee1sd">
<img src="https://github.com/Lee1sd.png" width="100px;" alt="이건희"/><br />
<sub><b>이건희</b></sub><br />
<sub>BE</sub>
</a>
</td>
<td align="center">
<a href="https://github.com/geunchanlee">
<img src="https://github.com/geunchanlee.png" width="100px;" alt="이근찬"/><br />
<sub><b>이근찬</b></sub><br />
<sub>FE</sub>
</a>
</td>
<td align="center">
<a href="https://github.com/jiyoungjae">
<img src="https://github.com/jiyoungjae.png" width="100px;" alt="지영재"/><br />
<sub><b>지영재</b></sub><br />
<sub>BE</sub>
</a>
</td>
</tr>
</table>

</div>
