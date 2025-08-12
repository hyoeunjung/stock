✨ 주식 배당금 정보 API 서비스

🚀 프로젝트 소개

미국 주식의 배당금 정보를 제공하는 백엔드 API 서비스입니다. Yahoo Finance의 데이터를 스크래핑하여 자체 DB에 저장하고, 사용자에게 다양한 API를 통해 정보를 제공합니다. 효율적인 데이터 관리와 빠른 응답 속도를 위해 Redis 캐싱과 Trie 자료구조를 활용했으며, JWT 기반의 인증 시스템을 구축하여 보안을 강화했습니다.

🌟 주요 기능

📈 배당금 정보 조회: 회사명(companyName)을 통해 해당 회사의 배당금 정보를 조회합니다. (Redis 캐싱 적용)

🔍 회사 정보 관리:

ticker를 입력받아 Yahoo Finance에서 회사 정보와 배당금 정보를 스크래핑하여 저장합니다.

등록된 회사 목록 조회 (페이징 처리)

특정 회사를 삭제하면 관련 배당금 정보와 캐시 데이터도 함께 삭제합니다.

✍️ 자동완성 기능: 회사명 검색 시, 입력한 키워드에 해당하는 회사명 목록을 제공합니다. (Trie 자료구조)

🔑 인증 및 권한 관리:

회원가입: 비밀번호 암호화 및 중복 ID 체크

로그인: JWT(JSON Web Token) 발급

권한: 특정 API는 WRITE 권한이 있는 사용자만 접근 가능하도록 설정

⏰ 스케줄링: 주기적으로 등록된 회사들의 배당금 정보를 스크래핑하여 최신 데이터를 유지합니다.


### 🛠️ 기술 스택

| 분류 | 기술 스택 |
| :--- | :--- |
| **백엔드** | `Spring Boot 2.x`, `Java 11` |
| **데이터베이스** | `H2 Database`, `JPA` (Hibernate) |
| **스크래핑** | `Jsoup` |
| **캐싱** | `Redis` |
| **보안** | `Spring Security`, `JWT` |
| **빌드/환경**| `Gradle`, `Docker`, `Lombok` |

---

### 📝 최종 구현 API

| Method | Endpoint | 설명 |
| :--- | :--- | :--- |
| `GET` | `/finance/dividend/{companyName}` | 회사 이름으로 메타 정보와 배당금 정보 반환 |
| `GET` | `/company/autocomplete` | 회사명 자동완성 리스트 반환 |
| `GET` | `/company` | 모든 회사 목록 반환 (페이징 적용) |
| `POST`| `/company` | 새로운 회사 정보 추가 |
| `DELETE`| `/company/{ticker}` | 특정 회사 정보 삭제 |
| `POST`| `/auth/signup` | 회원가입 |
| `POST`| `/auth/signin` | 로그인 및 JWT 토큰 발급 |
