# 3주차 세션 기술 검수 보고서

## 1. 검수 범위와 기준

- 원문: 사용자가 제공한 `0916_JPA와 Lombok으로 게시글 CRUD API 완성하기` 전체 1,616줄
- 기준 코드: 저장소의 `main` 브랜치, 커밋 `b5197bb`
- 실행 환경: Java 17.0.16, Spring Boot 4.1.0, Gradle Wrapper 9.5.1
- 검증 방식: 소스 대조, 공식 Spring 문서 확인, 컴파일, H2 기반 애플리케이션 컨텍스트 및 MockMvc 통합 테스트
- 이미지 상태: 첨부 폴더에는 텍스트 파일만 있고 원문이 참조하는 PNG/SVG는 없었다. 이미지 속 핵심 코드는 기존 코드, 단계별 설명, 원문의 전체 `PostService`와 서로 대조하여 텍스트 코드로 복원했다.

## 2. 기존 프로젝트 상태

- 실제 저장소 루트는 `spring_session_3/likelion14th_spring_session_3th`이다.
- 저장소별 `AGENTS.md`는 없다.
- 현재 기본 브랜치는 `main`이며 `origin/HEAD -> origin/main`이다.
- remote는 `origin=https://github.com/lsoobin/likelion14th_spring_session_3th.git` 하나뿐이고 `upstream`은 없다.
- 분석 시작 시 워킹 트리는 깨끗했다.
- 2주차에는 `Post`, `PostRepository`, `PostService.getPostSummaries()`, `PostSummaryResponse`, 목록 GET Controller가 있었다.
- `application.yaml`은 MySQL `likelion_blog`를 사용하고, 평문 비밀번호와 `ddl-auto:create`를 포함했다.
- `import.sql`은 시작 시 샘플 게시글 두 건을 넣도록 작성되어 있었다.
- 기존 테스트는 `@SpringBootTest contextLoads()` 하나이며 개인 MySQL 설정을 그대로 읽었다.

## 3. 반드시 수정

### 3.1 평문 DB 비밀번호

문제:

- `application.yaml`과 `db-setup.sql`에 동일한 실제 형태의 비밀번호가 추적되고 있었다.
- 해당 값은 현재 파일뿐 아니라 `5a95da3`, `b5197bb` 커밋에도 남아 있다.
- 원문에는 다른 비밀번호 예시가 있어 실제 저장소와도 일치하지 않았다.

수정:

- `application.yaml`: `password: ${DB_PASSWORD}`
- `db-setup.sql`: `YOUR_DB_PASSWORD` 자리표시자로 교체
- 실행 전 IntelliJ Run Configuration 또는 현재 셸에 `DB_PASSWORD`를 설정하도록 안내
- 이 환경에서 변수를 비우고 실제 `bootRun`한 결과, MySQL 서버에는 연결했지만 비밀번호가 유효하지 않아 `Access denied for user 'likelion'@'localhost'`로 기동이 실패했다.

추가 조치:

- Git 히스토리는 이 작업에서 임의로 재작성하지 않았다. 이미 사용한 비밀번호라면 반드시 교체해야 한다.
- 공개 원격에 올라갔다면 비밀번호 교체가 우선이고, 필요하면 저장소 관리자와 협의해 별도 히스토리 정리를 진행한다.

### 3.2 존재하지 않는 게시글이 500이 되는 문제

문제:

- 인자 없는 `orElseThrow()`는 `NoSuchElementException`을 던진다.
- 별도 매핑이 없으면 Spring MVC의 기본 응답은 404가 아니라 500이다.
- 원문은 해당 동작을 명확히 밝히지 않아 멘티가 “없는 리소스는 자동으로 404”라고 오해할 수 있었다.

수정:

- `PostNotFoundException`을 만들고 `@ResponseStatus(HttpStatus.NOT_FOUND)`를 적용했다.
- 조회·수정·삭제가 같은 `findPostById()`를 사용하여 없는 ID에 모두 404를 반환한다.

### 3.3 스크린샷에만 있는 핵심 코드

문제:

- Entity, Request/Response DTO, Controller, PUT/DELETE Service 코드가 다수의 로컬 이미지 참조에만 있었다.
- 이미지가 누락되면 실습의 핵심 구현을 재현할 수 없다.

수정:

- `docs/week3_session_reviewed.md`에 모든 핵심 클래스의 전체 코드를 텍스트 블록으로 넣었다.
- 대본에도 전반부에서 실제 입력할 전체 코드와 정확한 변경 부분을 넣었다.

### 3.4 테스트가 개인 MySQL에 종속됨

문제:

- 기존 `contextLoads()`는 메인 `application.yaml`을 읽어 MySQL과 비밀번호가 없으면 실패한다.
- 코드 결함과 개인 환경 문제를 구분할 수 없다.

수정:

- 테스트 런타임에만 H2를 추가했다.
- `src/test/resources/application.yaml`에서 인메모리 H2와 `create-drop`을 사용한다.
- CRUD, Validation, 404, 컨텍스트 로딩을 실제 HTTP 계층까지 통합 테스트한다.

### 3.5 Spring Boot 4 테스트 모듈

문제:

- Spring Boot 4에서는 MVC 테스트 자동 구성이 별도 모듈로 분리되어 있다.

수정:

- `testImplementation 'org.springframework.boot:spring-boot-starter-webmvc-test'`를 추가했다.
- `AutoConfigureMockMvc`는 Boot 4 패키지인 `org.springframework.boot.webmvc.test.autoconfigure`를 사용했다.

### 3.6 공통 URL 중복

문제:

- 클래스에 `@RequestMapping("/api/posts")`를 추가한 뒤 기존 `@GetMapping("/api/posts")`를 그대로 두면 실제 URL이 `/api/posts/api/posts`가 된다.

수정:

- 클래스 레벨에 공통 경로를 두고 메서드에는 `@GetMapping`, `@PostMapping`, `@GetMapping("/{postId}")`처럼 상대 경로만 사용했다.

## 4. 수정 권장

### 4.1 Request DTO의 Setter 설명

원문의 “JSON 값을 객체에 담기 위해 Setter를 사용한다”는 이번 코드 패턴의 설명으로는 이해하기 쉽지만, Jackson이 항상 Setter를 요구한다는 뜻으로 읽힐 수 있다.

수정본에서는 다음처럼 한정했다.

> 이번 실습에서는 기본 생성자로 객체를 만들고 Setter로 값을 채우는 단순한 JavaBean 방식을 선택한다. Jackson은 생성자 바인딩 등 다른 방식도 지원하므로 Setter가 유일한 방법은 아니다.

### 4.2 `@RequiredArgsConstructor`의 기준

“final 필드 생성자”만으로 설명하면 `@NonNull` 필드가 빠진다. 정확히는 초기화되지 않은 `final` 필드와 `@NonNull` 필드를 받는 생성자를 만든다. 이번 코드에는 `final postRepository`와 Response DTO의 `final` 필드만 해당한다.

### 4.3 JPA 기본 생성자

“JPA가 row를 객체로 만들 때 사용”은 입문 설명으로 가능하지만 다음 조건이 빠졌다.

- JPA 명세상 Entity에는 public 또는 protected 매개변수 없는 생성자가 필요하다.
- 애플리케이션 코드에서 함부로 호출하지 못하게 protected로 제한한다.
- `@NoArgsConstructor(access = AccessLevel.PROTECTED)`가 이를 생성한다.

### 4.4 변경 감지 표현

원문의 “달라진 부분만 DB에 반영”은 기본 Hibernate가 변경된 컬럼만 포함한 SQL을 만든다는 뜻으로 오해될 수 있다. 변경 감지는 변경 여부를 찾아 UPDATE를 예약하지만, 기본 UPDATE SQL에는 변경하지 않은 컬럼도 포함될 수 있다. 수정본은 “변경된 Entity의 상태를 DB에 반영한다”로 고쳤다.

또한 변경 감지는 아무 Entity에나 “무조건” 일어나지 않는다. 쓰기 트랜잭션 안에서 영속 상태인 Entity가 실제로 변경되고 flush가 일어날 때 반영된다.

### 4.5 flush와 commit

- flush는 영속성 컨텍스트의 변경을 SQL로 DB와 동기화하지만 트랜잭션을 확정하지 않는다.
- commit 전에 보통 flush가 일어나며, commit 성공 후에야 변경이 확정된다.
- RuntimeException/Error는 기본 rollback 대상이지만 checked exception은 기본적으로 그렇지 않다.
- flush 뒤에도 rollback될 수 있다.

### 4.6 영속성 컨텍스트 비유

“메모장”, “계속 지켜본다”는 비유는 도입용으로 유지하되 정확한 설명을 바로 붙였다.

- 영속성 컨텍스트는 EntityManager가 Entity의 동일성과 생명주기를 관리하는 논리적 영역이다.
- Hibernate가 모든 필드 변경 이벤트를 실시간 감시한다기보다, flush 시 스냅샷과 현재 상태를 비교해 변경을 감지하는 방식으로 이해한다.

### 4.7 `save()`와 생성 시점

`JpaRepository.save()`는 Entity 상태에 따라 내부적으로 persist 또는 merge를 선택한다. 이번 새 `Post`는 ID가 없으므로 신규 Entity로 저장된다. “save는 언제나 INSERT”라고 일반화하지 않는다.

`createdAt`은 `Post(title, content)` 생성자에서 `LocalDateTime.now()`로 만든다. 따라서 INSERT 이후 응답을 만들 때도 null이 아니며, 클라이언트가 작성 시각을 주입하지 않는다.

### 4.8 삭제 트랜잭션

`delete()` 자체는 Repository에서 트랜잭션으로 실행된다. 하지만 이 실습은 “조회하여 없으면 404 → 같은 Entity 삭제”를 하나의 작업 단위로 묶기 때문에 Service의 `deletePost()`에 `@Transactional`을 둔다.

### 4.9 `ddl-auto:update`

- `create`: 시작할 때 기존 스키마를 버리고 다시 만든다. 데이터 손실 위험이 있다.
- `update`: 기존 스키마와 Entity 차이를 보고 가능한 변경을 시도하며 기존 데이터를 보통 유지한다.
- `update`는 로컬 실습 편의를 위한 설정이지 운영 배포 권장안이 아니다.
- 운영에서는 Flyway/Liquibase 같은 명시적 마이그레이션을 검토한다.

### 4.10 상태 코드와 Validation

- POST 성공은 `@ResponseStatus(HttpStatus.CREATED)`로 201이다.
- DELETE 성공은 본문 없는 204이며 반환 타입은 `void`이다.
- Request DTO의 `@NotBlank`, `@Size`는 `@Valid @RequestBody`가 있어야 요청 단계에서 실행된다.
- Entity의 `@Column(nullable=false)`는 DB 스키마 제약이며 HTTP 요청 검증을 대신하지 않는다.

## 5. 선택 개선

이번 입문 세션에서는 다음을 의도적으로 추가하지 않았다.

- 전역 예외 응답 DTO와 `@RestControllerAdvice`
- 페이지네이션과 정렬
- 생성 응답의 `Location` 헤더
- `@CreatedDate` 기반 JPA Auditing
- Entity/DTO 매퍼 클래스
- 인증·인가
- PATCH 부분 수정
- Testcontainers 기반 실제 MySQL 통합 테스트

이들은 유용하지만 3주차 학습 흐름을 크게 넓힌다.

## 6. 코드에 반영했지만 강의에서 설명이 더 필요한 내용

1. `PostNotFoundException`은 HTTP 404를 의도적으로 매핑하기 위한 최소 예외다.
2. H2는 자동 테스트를 격리하기 위한 테스트 전용 DB다. 실제 MySQL 호환성을 완전히 보장하지는 않는다.
3. 테스트 소스의 `application.yaml`이 메인 설정을 덮어써서 테스트에는 `DB_PASSWORD`가 필요 없다.
4. Boot 4에서는 MVC 테스트 스타터와 `AutoConfigureMockMvc` 패키지가 Boot 3 자료와 다를 수 있다.
5. Git 히스토리에 남은 기존 비밀번호는 파일 수정만으로 사라지지 않는다.

## 7. 강의 중 주의할 부분

- Gradle Reload 후에도 Lombok이 빨갛다면 JDK 17, Gradle JVM, Lombok 플러그인/annotation processing 순서로 확인한다.
- `DB_PASSWORD` 누락은 placeholder 해석 또는 DataSource 초기화 단계에서 애플리케이션 시작 실패로 나타난다.
- `Access denied`는 서버 실행 여부가 아니라 계정·비밀번호·host 권한 문제다.
- `Communications link failure` 또는 connection refused는 MySQL 프로세스, 포트 3306, URL을 먼저 본다.
- 목록 `[]`은 오류 응답이 아니라 현재 연결된 DB에 행이 없다는 정상 200 응답이다.
- `ddl-auto:update`로 바꾼 뒤 ID가 1부터 시작한다고 가정하지 않는다.
- POST와 GET이 다른 결과를 보이면 같은 실행 인스턴스·DB URL·schema를 보는지 확인한다.
- 없는 ID는 이 최종 코드에서 404다. 원문의 인자 없는 `orElseThrow()` 상태라면 500이었음을 구분한다.
- 400은 요청 형식/검증, 404는 매핑 또는 리소스 부재, 500은 처리되지 않은 서버 예외로 먼저 분류한다.

## 8. 검증 결과

실행 명령:

```powershell
$env:GRADLE_USER_HOME='C:\Users\82108\Desktop\spring_session_3\.gradle-task'
.\gradlew.bat test
```

결과:

- Java 컴파일 성공
- 테스트 컴파일 성공
- Spring ApplicationContext 로딩 성공
- 8개 테스트 성공
- H2에서 POST/목록/상세/PUT/DELETE, Validation 400, 없는 ID 404 확인

실제 MySQL 서버가 실행 중이고 환경변수 누락 시 `Access denied`로 실패하는 것까지 확인했다. 올바른 사용자 비밀번호를 제공한 실제 MySQL CRUD 검증은 사용자의 `DB_PASSWORD`가 필요하므로 자동 테스트 결과와 별도로 수행해야 한다.

## 9. 공식 문서 대조

- [Spring Boot 4.1 시스템 요구사항](https://docs.spring.io/spring-boot/system-requirements.html): Java 17 이상, Gradle 8.14+ 또는 9.x
- [Spring Data JPA 트랜잭션](https://docs.spring.io/spring-data/data-jpa/reference/4.0/jpa/transactions.html): Repository CRUD 메서드의 기본 트랜잭션과 Service 경계
- [Spring Boot 4.1 AutoConfigureMockMvc API](https://docs.spring.io/spring-boot/api/java/org/springframework/boot/webmvc/test/autoconfigure/AutoConfigureMockMvc.html)
