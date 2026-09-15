# 3주차 전반부 강의자료 필수 수정 검토

## 검토 기준

이번 재검토에서는 다음 기준만 사용했다.

> 사진을 포함한 강의자료를 멘티가 순서대로 따라 했을 때 컴파일이 되지 않거나, 애플리케이션이 실행되지 않거나, 정상 데이터로 CREATE/READ 실습을 할 수 없는가?

더 좋은 아키텍처, 실무식 예외 응답, 선택적인 리팩터링은 “반드시 수정”으로 분류하지 않았다.

검토 대상:

- 원문 전반부 21~890줄
- 사진 1~10
- 2주차 저장소 코드
- Java 17, Spring Boot 4.1.0, Gradle 9.5.1에서 실제 빌드와 H2 테스트

## 결론

전반부의 Lombok, Validation, Entity, DTO, Service, Controller, POST, 목록 GET, 상세 GET 코드는 정상 흐름을 막는 컴파일 오류가 없다.

반드시 수정할 부분은 1개다.

## 반드시 수정

### 사진 2의 고정 DB 비밀번호 예시

자료 위치:

- Part 0 「DB 비밀번호 설정 변경」
- 원문 88~137줄
- 사진 2

현재 사진:

```text
DB_PASSWORD=<사진 속 고정 비밀번호>
```

수정:

```text
DB_PASSWORD=본인의 MySQL 비밀번호
```

이유:

- 2주차에 실제로 설정한 MySQL 비밀번호와 사진의 값이 다르면 `Access denied`로 애플리케이션 실행이 실패한다.
- 특정 문자열을 모든 멘티가 복사할 정답처럼 보여주면 안 된다.
- 비밀번호 값 자체를 강의 화면과 저장소에 고정할 필요가 없다.

강의 멘트:

> “사진 속 값을 그대로 쓰는 것이 아니라, 2주차에 본인이 MySQL 계정에 설정한 비밀번호를 입력하세요.”

## 그대로 사용해도 되는 부분

### 사진 1 — application.yaml

자료 위치: Part 0, 원문 84~116줄

- `password: ${DB_PASSWORD}` 정상
- `ddl-auto:update` 정상
- MySQL URL과 DB 이름이 기존 프로젝트와 일치

### 사진 3 — Post Entity Lombok

자료 위치: Part 1, 원문 250~278줄

- `@Getter` 정상
- `@NoArgsConstructor(access = AccessLevel.PROTECTED)` 정상
- Entity에 전체 Setter가 없음
- JPA 기본 생성자 조건 충족

### 사진 4~5 — Response DTO

자료 위치: Part 1, 원문 279~302줄

- final 필드와 `@RequiredArgsConstructor` 조합 정상
- 목록 DTO에는 content가 없고 상세 DTO에는 content가 있음
- `@Getter`를 통한 JSON 응답 정상

### 사진 6 — PostCreateRequest

자료 위치: Part 3, 원문 433~445줄

- Spring Boot 4에 맞는 `jakarta.validation` 사용
- `@NotBlank`, `@Size` 정상
- Getter, Setter, 기본 생성자 조합 정상

### 사진 7 — Post 생성자

자료 위치: Part 3, 원문 446~465줄

- title과 content 설정 정상
- `createdAt = LocalDateTime.now()`로 저장 직후 응답에서도 작성 시각 사용 가능
- Lombok 기본 생성자와 게시글 생성용 생성자가 충돌하지 않음

### 사진 8~9 — Service 생성과 DTO 변환

자료 위치: Part 3, 원문 466~496줄

- `new Post(...)` 정상
- `postRepository.save(post)` 정상
- 저장 결과를 `PostDetailResponse`로 변환하는 코드 정상
- ID와 createdAt을 포함한 생성 응답 가능

### 사진 10 — Controller

자료 위치: Part 3, 원문 514~552줄

- 클래스 레벨 `@RequestMapping("/api/posts")` 정상
- 기존 목록 매핑을 `@GetMapping`으로 변경한 부분 정상
- `@Valid @RequestBody` 위치 정상
- `@ResponseStatus(HttpStatus.CREATED)`로 201 응답 정상
- Controller의 기존 직접 생성자를 유지해도 정상

주의:

> 클래스에 `@RequestMapping("/api/posts")`를 붙인 뒤 기존 `@GetMapping("/api/posts")`의 경로를 지우지 않으면 `/api/posts/api/posts`가 된다. 자료와 사진 10은 이미 올바르게 `@GetMapping`으로 변경되어 있다.

## 수정하지 않고 설명만 보완할 부분

### 없는 ID와 orElseThrow

자료 위치:

- Part 4 「게시글 하나 조회」
- 원문 641~675줄

자료의 다음 코드는 컴파일되고, 존재하는 ID 상세 조회는 정상 동작한다.

```java
return postRepository.findById(postId)
        .orElseThrow();
```

다만 없는 ID에서는 `NoSuchElementException`이 처리되지 않아 500이 발생할 수 있다. 이것은 정상 ID로 진행하는 전반부 CREATE/READ 실습을 막지는 않으므로 별도 404 예외 코드를 필수로 추가하지 않는다.

강의에서는 다음 한 문장만 덧붙인다.

> “오늘은 목록에서 확인한 실제 ID를 사용합니다. 없는 ID를 404로 반환하는 예외 응답 설계는 이후 확장 내용이고, 현재 단순 코드에서는 500이 날 수 있습니다.”

### Request DTO의 Setter

자료의 Getter, Setter, 기본 생성자 패턴은 정상 작동한다. “이번 실습에서는 Jackson이 기본 생성자와 Setter를 이용하는 단순한 방식으로 요청값을 채운다”라고 설명하면 충분하다. 다른 역직렬화 방식까지 전반부에 추가할 필요는 없다.

### ddl-auto:update

자료가 이미 “로컬 실습용이며 실제 배포에서는 별도 마이그레이션 도구를 사용한다”고 명시하므로 추가 수정이 필요 없다.

## 실제 검증

실행:

```powershell
.\gradlew.bat clean build
.\gradlew.bat test
```

확인:

- Java 컴파일 성공
- ApplicationContext 로딩 성공
- H2 기반 정상 POST/목록 GET/상세 GET/PUT/DELETE 성공
- POST 201
- GET 200
- Validation 실패 400
- DELETE 204

실제 MySQL에서는 비밀번호 없이 실행했을 때 `Access denied`가 발생하는 것을 확인했다. 올바른 개인 비밀번호를 사용한 수동 검증은 강의 직전에 진행한다.

## 강의 전 최종 판단

- 사진 2의 비밀번호 표현만 수정하면 전반부 자료의 정상 CRUD 실습을 막는 필수 오류는 없다.
- 사진 1, 3~10의 코드는 그대로 사용해도 된다.
- 추가적인 404 예외 클래스나 Controller Lombok 적용은 이번 전반부 필수 내용이 아니다.
- 상세 대본은 `docs/week3_front_script.md`에서 원문 Part와 줄 번호를 기준으로 제공한다.
