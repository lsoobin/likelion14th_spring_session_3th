# 3주차 수정본: JPA와 Lombok으로 게시글 CRUD API 완성하기

> 원본은 별도로 보존한다. 이 문서는 실제 저장소와 기술 검수 결과를 반영한 실습용 수정본이다. 이미지 없이도 진행할 수 있도록 핵심 코드를 모두 텍스트로 제공한다.

## 오늘의 진행 순서

| 구분 | 내용 |
| --- | --- |
| 전반부 | Lombok, Validation, JpaRepository, 게시글 생성·목록·상세 조회 |
| 후반부 | 영속성 컨텍스트, 트랜잭션, 변경 감지, 게시글 수정·삭제 |

완성할 API:

| 기능 | Method | URL | 성공 상태 |
| --- | --- | --- | --- |
| 생성 | POST | `/api/posts` | 201 Created |
| 목록 | GET | `/api/posts` | 200 OK |
| 상세 | GET | `/api/posts/{postId}` | 200 OK |
| 수정 | PUT | `/api/posts/{postId}` | 200 OK |
| 삭제 | DELETE | `/api/posts/{postId}` | 204 No Content |

잘못된 요청값은 400을 반환한다. 원본 자료처럼 인자 없는 `orElseThrow()`를 사용하는 현재 수업 코드는 존재하지 않는 게시글에서 500이 발생할 수 있다. 404 예외 응답 설계는 이번 필수 실습에 추가하지 않는다.

---

# Part 0. 지난 코드 이어서 사용하기

새 프로젝트를 만들지 않고 2주차 프로젝트를 그대로 연다. 실제 저장소 루트는 다음 파일이 보이는 폴더다.

```text
likelion14th_spring_session_3th/
├── build.gradle
├── gradlew
├── gradlew.bat
├── db-setup.sql
└── src/
```

2주차 게시글 구조:

```text
src/main/java/com/likelion/springsession/post/
├── controller/PostController.java
├── dto/PostSummaryResponse.java
├── entity/Post.java
├── repository/PostRepository.java
└── service/PostService.java
```

## DB 설정

`src/main/resources/application.yaml`

```yaml
spring:
  application:
    name: springsession
  datasource:
    url: jdbc:mysql://localhost:3306/likelion_blog
    username: likelion
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true
```

Windows PowerShell에서 현재 터미널에만 설정하려면:

```powershell
$env:DB_PASSWORD='자신의 MySQL 비밀번호'
.\gradlew.bat bootRun
```

IntelliJ에서는 Run → Edit Configurations → 애플리케이션 설정 → Environment variables에 `DB_PASSWORD=...`를 추가하고 애플리케이션을 완전히 재시작한다.

- 환경변수가 없으면 설정 또는 DataSource 초기화 중 애플리케이션 시작이 실패한다. 현재 환경의 실제 확인 결과는 MySQL `Access denied`였다.
- `Access denied`이면 사용자명·비밀번호·MySQL 계정의 host 권한을 확인한다.
- connection refused/communications link failure면 MySQL 실행 여부, 3306 포트, URL을 확인한다.
- `ddl-auto:update`는 로컬 실습용이다. 운영 환경의 마이그레이션 전략으로 권장하는 설정이 아니다.
- 2주차 `import.sql`은 제거한다. 오늘부터 POST로 직접 데이터를 만든다.

---

# Part 1. Lombok과 Validation

## 의존성

`build.gradle`의 `dependencies`에 다음을 둔다.

```groovy
implementation 'org.springframework.boot:spring-boot-starter-web'
implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
implementation 'org.springframework.boot:spring-boot-starter-validation'
compileOnly 'org.projectlombok:lombok'
annotationProcessor 'org.projectlombok:lombok'
runtimeOnly 'com.mysql:mysql-connector-j'
```

테스트 구성:

```groovy
testImplementation 'org.springframework.boot:spring-boot-starter-test'
testImplementation 'org.springframework.boot:spring-boot-starter-webmvc-test'
testCompileOnly 'org.projectlombok:lombok'
testAnnotationProcessor 'org.projectlombok:lombok'
testRuntimeOnly 'com.h2database:h2'
testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
```

저장한 뒤 IntelliJ 오른쪽 Gradle 창에서 Reload All Gradle Projects를 누른다.

## 어떤 Lombok을 어디에 쓰나요?

| 클래스 | Lombok | 이유 |
| --- | --- | --- |
| Entity | `@Getter`, protected `@NoArgsConstructor` | 조회용 Getter와 JPA 기본 생성자 |
| Response DTO | `@Getter`, `@RequiredArgsConstructor` | JSON 직렬화 Getter와 final 필드 생성자 |
| Request DTO | `@Getter`, `@Setter`, `@NoArgsConstructor` | 이번 실습의 JavaBean 역직렬화 방식 |
| Service | `@RequiredArgsConstructor` | final 의존성 생성자 주입 |

`@RequiredArgsConstructor`는 초기화되지 않은 final 필드와 `@NonNull` 필드를 받는 생성자를 만든다. 이번 코드에서는 final 필드가 대상이다. Controller는 사진 10과 같이 기존 직접 생성자를 유지한다.

## Post Entity 전체 코드

`src/main/java/com/likelion/springsession/post/entity/Post.java`

```java
package com.likelion.springsession.post.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 2000)
    private String content;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Post(String title, String content) {
        this.title = title;
        this.content = content;
        this.createdAt = LocalDateTime.now();
    }

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
```

JPA Entity는 public 또는 protected 기본 생성자가 필요하다. protected로 제한하면 JPA는 사용할 수 있고 일반 애플리케이션 코드의 실수는 줄일 수 있다. Entity 전체 Setter는 열지 않고, 후반부에서 “게시글을 수정한다”는 의미가 드러나는 `update()`를 사용한다.

작성 시각은 생성자에서 서버 시간이 들어간다. 그래서 저장 직후 만드는 응답에도 `createdAt`이 존재한다.

## Response DTO 전체 코드

`PostSummaryResponse.java`

```java
package com.likelion.springsession.post.dto;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PostSummaryResponse {

    private final Long id;
    private final String title;
    private final LocalDateTime createdAt;
}
```

`PostDetailResponse.java`

```java
package com.likelion.springsession.post.dto;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PostDetailResponse {

    private final Long id;
    private final String title;
    private final String content;
    private final LocalDateTime createdAt;
}
```

목록에는 본문을 빼고, 생성·상세·수정 응답에는 본문을 포함한다. Entity를 API 응답으로 직접 노출하지 않아 DB 모델과 API 계약을 분리한다.

---

# Part 2. JpaRepository와 CRUD

`src/main/java/com/likelion/springsession/post/repository/PostRepository.java`

```java
package com.likelion.springsession.post.repository;

import com.likelion.springsession.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
}
```

`JpaRepository<Post, Long>`에서 `Post`는 관리할 Entity, `Long`은 ID 타입이다.

| 기능 | 사용할 메서드 | 정확한 의미 |
| --- | --- | --- |
| 생성 | `save(post)` | Entity 상태에 따라 persist 또는 merge |
| 전체 조회 | `findAll()` | `List<Post>` 반환 |
| ID 조회 | `findById(id)` | 없을 가능성을 담은 `Optional<Post>` 반환 |
| 삭제 | `delete(post)` | 전달한 Entity 삭제 |
| 수정 | 변경 감지 | 영속 상태 Entity 변경을 flush 때 반영 |

이번 새 `Post`는 ID가 없는 신규 Entity이므로 `save()` 결과는 INSERT다. `save()`를 언제나 INSERT라고 일반화하지 않는다.

---

# Part 3. CREATE

## 생성 Request DTO

`src/main/java/com/likelion/springsession/post/dto/PostCreateRequest.java`

```java
package com.likelion.springsession.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PostCreateRequest {

    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 100, message = "제목은 100자 이하로 작성해주세요.")
    private String title;

    @NotBlank(message = "본문은 필수입니다.")
    @Size(max = 2000, message = "본문은 2000자 이하로 작성해주세요.")
    private String content;
}
```

Spring Boot 4에서도 Validation import는 `jakarta.validation`이다. 이번에는 Jackson이 기본 생성자로 DTO를 만들고 Setter로 값을 채우는 단순한 방식을 쓴다. Jackson은 생성자 바인딩도 지원하므로 Setter가 유일한 역직렬화 방법은 아니다.

`@NotBlank`는 null, 빈 문자열, 공백만 있는 문자열을 거부한다. `@Size`는 Entity의 컬럼 길이와 맞춘다. 이 검증은 Controller의 `@Valid`가 있어야 실행된다.

## Service 생성 로직

```java
public PostDetailResponse createPost(PostCreateRequest request) {
    Post post = new Post(request.getTitle(), request.getContent());
    Post savedPost = postRepository.save(post);
    return toDetailResponse(savedPost);
}
```

## Controller 생성 로직

```java
@PostMapping
@ResponseStatus(HttpStatus.CREATED)
public PostDetailResponse createPost(
        @Valid @RequestBody PostCreateRequest request
) {
    return postService.createPost(request);
}
```

`@RequestBody`가 JSON을 DTO로 바꾸고, `@Valid`가 DTO 제약을 검사한다. 성공하면 201이다.

Postman:

```http
POST http://localhost:8080/api/posts
Content-Type: application/json
```

```json
{
  "title": "JPA CRUD",
  "content": "게시글을 생성해봅니다."
}
```

예상 응답:

```json
{
  "id": 1,
  "title": "JPA CRUD",
  "content": "게시글을 생성해봅니다.",
  "createdAt": "2026-09-16T19:30:00.123456"
}
```

ID와 시각은 실행 환경에 따라 달라진다.

---

# Part 4. READ

## 목록 조회

```java
public List<PostSummaryResponse> getPostSummaries() {
    List<Post> posts = postRepository.findAll();
    List<PostSummaryResponse> responses = new ArrayList<>();

    for (Post post : posts) {
        responses.add(new PostSummaryResponse(
                post.getId(),
                post.getTitle(),
                post.getCreatedAt()
        ));
    }

    return responses;
}
```

```java
@GetMapping
public List<PostSummaryResponse> getPosts() {
    return postService.getPostSummaries();
}
```

```http
GET http://localhost:8080/api/posts
```

게시글이 없으면 200과 `[]`가 정상이다. 있으면 content가 없는 요약 배열을 반환한다.

## 상세 조회

```java
public PostDetailResponse getPost(Long postId) {
    return toDetailResponse(findPostById(postId));
}

private Post findPostById(Long postId) {
    return postRepository.findById(postId)
            .orElseThrow();
}
```

```java
@GetMapping("/{postId}")
public PostDetailResponse getPost(@PathVariable Long postId) {
    return postService.getPost(postId);
}
```

`Optional<Post>`는 값의 부재 가능성을 타입으로 드러낸다. 이번 자료는 가장 단순한 `orElseThrow()`를 사용한다. 존재하지 않는 ID에서는 처리되지 않은 예외로 500이 발생할 수 있으며, 404 응답을 위한 예외 매핑은 후속 개선 범위다.

```http
GET http://localhost:8080/api/posts/1
```

존재하면 content를 포함한 200을 반환한다. 실습에서는 목록에서 확인한 실제 ID를 사용한다.

---

# Part 5. 영속성 컨텍스트·변경 감지·트랜잭션

영속성 컨텍스트는 EntityManager가 Entity의 동일성, 상태, 생명주기를 관리하는 논리적 영역이다. “메모장”은 첫 비유로만 사용한다. Hibernate가 모든 변경 이벤트를 실시간 감시한다기보다, 일반적인 스냅샷 기반 변경 감지는 flush 때 최초 상태와 현재 상태를 비교한다고 이해한다.

```text
쓰기 트랜잭션 시작
  → findById(): 영속 상태 Post 조회, 스냅샷 보관
  → post.update(): Java 객체 상태 변경
  → 정상 종료
  → flush: 변경 감지, UPDATE SQL 실행
  → commit: DB 트랜잭션 확정
```

- 영속 상태이고 실제 값이 바뀌어야 UPDATE 대상이 된다.
- flush와 commit은 다르다. flush 뒤에도 rollback될 수 있다.
- 기본 Hibernate UPDATE SQL은 변경하지 않은 컬럼도 포함할 수 있다.
- RuntimeException/Error는 Spring 기본 rollback 대상이다. checked exception은 기본 규칙이 다르다.
- Service 메서드의 `@Transactional`로 조회와 변경을 하나의 작업 단위로 묶는다.

---

# Part 6. UPDATE와 DELETE

## 수정 Request DTO

`PostUpdateRequest.java`는 생성 DTO와 같은 제약을 사용한다.

```java
package com.likelion.springsession.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PostUpdateRequest {

    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 100, message = "제목은 100자 이하로 작성해주세요.")
    private String title;

    @NotBlank(message = "본문은 필수입니다.")
    @Size(max = 2000, message = "본문은 2000자 이하로 작성해주세요.")
    private String content;
}
```

## Service 수정·삭제

```java
@Transactional
public PostDetailResponse updatePost(Long postId, PostUpdateRequest request) {
    Post post = findPostById(postId);
    post.update(request.getTitle(), request.getContent());
    return toDetailResponse(post);
}

@Transactional
public void deletePost(Long postId) {
    Post post = findPostById(postId);
    postRepository.delete(post);
}
```

수정에서는 조회한 Entity가 같은 트랜잭션 안에서 영속 상태이므로 JPA 관점에서 `save()`를 다시 부르지 않아도 변경 감지로 반영된다. 삭제에서는 대상 조회와 삭제를 하나의 Service 트랜잭션으로 묶는다.

## Controller 수정·삭제

```java
@PutMapping("/{postId}")
public PostDetailResponse updatePost(
        @PathVariable Long postId,
        @Valid @RequestBody PostUpdateRequest request
) {
    return postService.updatePost(postId, request);
}

@DeleteMapping("/{postId}")
@ResponseStatus(HttpStatus.NO_CONTENT)
public void deletePost(@PathVariable Long postId) {
    postService.deletePost(postId);
}
```

PUT:

```http
PUT http://localhost:8080/api/posts/1
Content-Type: application/json
```

```json
{
  "title": "수정된 제목",
  "content": "수정된 내용입니다."
}
```

성공 시 수정된 상세 응답과 200이다.

DELETE:

```http
DELETE http://localhost:8080/api/posts/1
```

성공 시 본문 없는 204다.

---

# 완성된 PostService

```java
package com.likelion.springsession.post.service;

import com.likelion.springsession.post.dto.PostCreateRequest;
import com.likelion.springsession.post.dto.PostDetailResponse;
import com.likelion.springsession.post.dto.PostSummaryResponse;
import com.likelion.springsession.post.dto.PostUpdateRequest;
import com.likelion.springsession.post.entity.Post;
import com.likelion.springsession.post.repository.PostRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

    public List<PostSummaryResponse> getPostSummaries() {
        List<Post> posts = postRepository.findAll();
        List<PostSummaryResponse> responses = new ArrayList<>();

        for (Post post : posts) {
            responses.add(new PostSummaryResponse(
                    post.getId(),
                    post.getTitle(),
                    post.getCreatedAt()
            ));
        }
        return responses;
    }

    public PostDetailResponse getPost(Long postId) {
        return toDetailResponse(findPostById(postId));
    }

    public PostDetailResponse createPost(PostCreateRequest request) {
        Post post = new Post(request.getTitle(), request.getContent());
        Post savedPost = postRepository.save(post);
        return toDetailResponse(savedPost);
    }

    @Transactional
    public PostDetailResponse updatePost(Long postId, PostUpdateRequest request) {
        Post post = findPostById(postId);
        post.update(request.getTitle(), request.getContent());
        return toDetailResponse(post);
    }

    @Transactional
    public void deletePost(Long postId) {
        Post post = findPostById(postId);
        postRepository.delete(post);
    }

    private Post findPostById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow();
    }

    private PostDetailResponse toDetailResponse(Post post) {
        return new PostDetailResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getCreatedAt()
        );
    }
}
```

# 완성된 PostController

```java
package com.likelion.springsession.post.controller;

import com.likelion.springsession.post.dto.PostCreateRequest;
import com.likelion.springsession.post.dto.PostDetailResponse;
import com.likelion.springsession.post.dto.PostSummaryResponse;
import com.likelion.springsession.post.dto.PostUpdateRequest;
import com.likelion.springsession.post.service.PostService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public List<PostSummaryResponse> getPosts() {
        return postService.getPostSummaries();
    }

    @GetMapping("/{postId}")
    public PostDetailResponse getPost(@PathVariable Long postId) {
        return postService.getPost(postId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PostDetailResponse createPost(@Valid @RequestBody PostCreateRequest request) {
        return postService.createPost(request);
    }

    @PutMapping("/{postId}")
    public PostDetailResponse updatePost(
            @PathVariable Long postId,
            @Valid @RequestBody PostUpdateRequest request
    ) {
        return postService.updatePost(postId, request);
    }

    @DeleteMapping("/{postId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePost(@PathVariable Long postId) {
        postService.deletePost(postId);
    }
}
```

# 오류 대응

| 상태/증상 | 먼저 확인할 것 |
| --- | --- |
| Lombok import 빨간색 | Gradle Reload → 의존성 위치 → JDK/Gradle JVM → annotation processing |
| 앱 시작 전 placeholder 오류 | `DB_PASSWORD` 환경변수 |
| MySQL Access denied | username/password/계정 host 권한 |
| DB 연결 거절 | MySQL 실행, 3306, datasource URL |
| 예상치 않은 샘플 데이터 | 이전 DB 행인지 확인; `import.sql` 제거 여부 |
| 목록 `[]` | 정상 200인지, 같은 DB에 POST했는지 |
| `findById()` 타입 오류 | 반환은 `Optional<Post>` |
| DTO 생성자 오류 | Response 필드가 final인지, `@RequiredArgsConstructor`와 Reload |
| URL 404 | 클래스/메서드 경로를 합쳐 확인; 중복 `/api/posts/api/posts` 금지 |
| 상세에 content 없음 | `PostDetailResponse` 사용 여부 |
| 400 | JSON 문법, Content-Type, `@Valid`, `@NotBlank`, `@Size` |
| 404 | 요청 URL 자체가 Controller에 매핑되지 않음 |
| 없는 ID의 500 | 현재 단순 `orElseThrow()` 동작; 목록에서 실제 ID 사용 |
| 500 | 서버 로그의 첫 원인 예외; DB, 처리되지 않은 예외, 코드 오류 |

# 자동 테스트

테스트는 개인 MySQL 대신 H2를 사용한다.

```powershell
.\gradlew.bat clean build
```

실제 MySQL 수동 확인:

```powershell
$env:DB_PASSWORD='자신의 MySQL 비밀번호'
.\gradlew.bat bootRun
```

H2 테스트 통과는 Controller·Service·Repository·JPA 흐름을 검증하지만 MySQL 서버 상태, 계정 권한, 드라이버와 실제 DB 연결까지 대신 검증하지는 않는다.
