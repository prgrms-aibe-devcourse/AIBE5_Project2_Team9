# Git Convention - PICKASSO (9조대)

## Branch 규칙

### 네이밍 규칙

- **소문자 케밥 케이스** 사용 (`delicious-kebab`)
- `/`로 계층 구분 (`feature/search-filter`)
- 기능 단위로 브랜치를 생성하고, 해당 브랜치에만 관련 커밋을 쌓는다

### 브랜치 종류

| 접두사     | 용도                                   | 예시                   |
| ---------- | -------------------------------------- | ---------------------- |
| `main`     | 배포 가능한 메인 브랜치                | `main`                 |
| `feature/` | 새로운 기능 개발                       | `feature/search`       |
| `fix/`     | 버그 수정                              | `fix/issue#19`         |
| `docs/`    | 문서 수정 (README, 주석 등)            | `docs/readme`          |
| `test/`    | 테스트 코드 작성                       | `test/search-service`  |
| `refactor/`| 기능 변화 없이 코드 구조 개선          | `refactor/login-logic` |
| `chore/`   | 빌드·환경 설정, 기타 자잘한 수정       | `chore/install`        |
| `design/`  | 디자인(UI)만 수정                      | `design/main-page`     |

### 브랜치 워크플로우

```
1. main 브랜치에서 feature/기능명 브랜치 생성
2. 작업 완료 후 GitHub에서 Pull Request 요청
3. 팀원 코드 리뷰 후 main 브랜치로 Merge
4. Merge 완료된 브랜치는 삭제
```

> ⚠️ **main 브랜치에 직접 push 금지** — 반드시 PR을 통해 Merge한다.

---

## Commit 규칙

### 메시지 형식

```
<type>: <subject>
```

- `type`은 브랜치 접두사에 대응하는 키워드를 사용한다
- `subject`는 **한글로 간결하게** 작성한다 (마침표 생략)

### 타입 목록

| 타입       | 설명                         | 예시                              |
| ---------- | ---------------------------- | --------------------------------- |
| `feat`     | 새로운 기능 추가             | `feat: 검색 기능 구현`            |
| `fix`      | 버그 수정                    | `fix: 검색 결과 페이징 오류 수정` |
| `docs`     | 문서 수정                    | `docs: README 업데이트`           |
| `test`     | 테스트 코드 작성·수정        | `test: 검색 서비스 단위 테스트 추가` |
| `refactor` | 리팩토링 (기능 변화 없음)    | `refactor: 로그인 로직 분리`      |
| `chore`    | 빌드·설정 등 기타 작업       | `chore: Tailwind 설치`            |
| `design`   | UI/디자인 변경               | `design: 메인 페이지 레이아웃 수정` |

### 브랜치 ↔ 커밋 타입 대응

| 브랜치                  | 커밋 메시지                          |
| ----------------------- | ------------------------------------ |
| `feature/search`        | `feat: 검색 기능 구현`               |
| `fix/issue#19`          | `fix: 이미지 업로드 오류 수정`       |
| `docs/readme`           | `docs: README 업데이트`              |
| `refactor/login-logic`  | `refactor: 로그인 로직 분리`         |
| `design/main-page`      | `design: 메인 페이지 헤더 수정`      |

---

## 참고: PR(Pull Request) 작성 가이드

### PR 제목

커밋 메시지와 동일한 형식을 따른다.

```
feat: 작가 검색 필터 기능 구현
```

### PR 본문 템플릿

```markdown
## 작업 내용
- 어떤 기능을 구현/수정했는지 간략히 설명

## 변경 사항
- 변경된 파일이나 로직 요약

## 테스트
- [ ] 로컬에서 정상 동작 확인
- [ ] 관련 페이지 렌더링 확인

## 참고
- 관련 이슈 번호나 참고 링크
```
