# 3주차 전반부 강의 대본 — 원본 강의자료 순서 기준

## 대본 사용 기준

- 기준 자료: 사용자가 제공한 `0916_JPA와 Lombok으로 게시글 CRUD API 완성하기` 1,616줄
- 담당 범위: 원문 21~890줄
- 줄 번호는 첨부된 `pasted-text.txt`의 줄 번호다.
- 사진 번호는 이번에 추가로 전달받은 `사진 1.jpg`~`사진 10.jpg`다.
- 목표는 원본 강의자료의 흐름을 유지하면서 화면 공유 중 그대로 읽고 실습하는 것이다.
- 예상 시간: 약 90분

## 전반부에서 반드시 고칠 자료 1개

### 자료 위치

- Part 0, 「DB 비밀번호 설정 변경」: 원문 88~137줄
- 사진 2: IntelliJ Environment variables 입력 화면

### 고칠 내용

사진 2의 고정 값:

```text
DB_PASSWORD=<사진 속 고정 비밀번호>
```

다음처럼 바꾼다.

```text
DB_PASSWORD=본인의 MySQL 비밀번호
```

### 반드시 고치는 이유

2주차에 각자 설정한 MySQL 비밀번호와 사진 속 값이 다르면 애플리케이션이 `Access denied`로 실행되지 않는다. 특정 비밀번호를 정답처럼 보여주지 말고, 각자 만든 비밀번호를 넣도록 안내해야 한다.

이 항목을 제외하면 사진 1~10의 전반부 코드는 정상적인 CREATE/READ 실습을 막는 컴파일 오류가 없다.

---

# 오프닝

예상 소요: 2분

### 자료 위치

- 「오늘의 진행 순서」: 원문 14~20줄
- 「3주차 전반부 학습 목표」: 원문 21~30줄

### 내가 말할 문장

“오늘은 지난 시간에 만든 게시글 코드를 그대로 이어서 사용합니다. 전반부에서는 Lombok으로 반복 코드를 줄이고, JpaRepository의 `save()`, `findAll()`, `findById()`를 이용해서 게시글 생성과 조회를 완성할 거예요.”

“후반부에서는 지금 조회한 Entity를 수정하는 원리를 배우고 PUT과 DELETE까지 이어갑니다. 우선 전반부에서는 POST와 GET이 실제로 동작하는 데 집중하겠습니다.”

---

# Part 0. 지난 코드 이어서 사용하기

예상 소요: 10분

## 0-1. 기존 프로젝트 열기

### 자료 위치

- Part 0 시작: 원문 31줄
- 「기존 프로젝트 열기」: 원문 33~55줄
- 「오늘 전반부에서 바뀌는 부분」: 원문 56~83줄

### IntelliJ 화면 조작

1. IntelliJ 시작 화면에서 Open을 누른다.
2. `spring_session_3/likelion14th_spring_session_3th` 폴더를 선택한다.
3. 프로젝트 왼쪽에서 `src/main/java/com/likelion/springsession/post`를 펼친다.
4. controller, dto, entity, repository, service가 있는지 확인한다.

### 내가 말할 문장

“새 프로젝트는 만들지 않습니다. 1~2주차에 쓰던 프로젝트를 그대로 열어주세요.”

“프로젝트 루트는 `build.gradle`, `gradlew`, `src`가 바로 보이는 `likelion14th_spring_session_3th` 폴더입니다. 그 위 폴더를 잘못 열면 Gradle 프로젝트 인식이 안 될 수 있어요.”

“지난 시간에는 `Post`, `PostRepository`, `PostService`, 목록용 `PostSummaryResponse`, 그리고 목록 Controller까지 만들었습니다. 오늘은 이 구조를 바꾸지 않고 코드를 추가합니다.”

### 멘티 질문

“요청을 가장 먼저 받는 계층은 Controller, Service, Repository 중 어디일까요?”

정답: Controller

### 다음 내용 연결

“코드를 수정하기 전에 오늘 만든 게시글이 앱을 재실행해도 남도록 DB 설정부터 확인하겠습니다.”

## 0-2. application.yaml과 환경변수

### 자료 위치

- 「application.yaml 확인」: 원문 84~87줄
- 「DB 비밀번호 설정 변경」: 원문 88~143줄
- 사진 1: `${DB_PASSWORD}`, `ddl-auto:update`
- 사진 2: IntelliJ Edit Configurations

### IntelliJ 이동

`src/main/resources/application.yaml`을 연다.

### 변경할 코드

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

### 내가 말할 문장

“비밀번호를 Java 코드나 YAML에 직접 적지 않고 `DB_PASSWORD`라는 환경변수에서 읽도록 바꾸겠습니다. 중괄호 안은 실제 비밀번호가 아니라 환경변수 이름입니다.”

“`ddl-auto:create`는 애플리케이션을 실행할 때 스키마를 다시 만들 수 있습니다. 오늘은 POST로 만든 데이터를 이어서 조회해야 하므로 로컬 실습에서는 `update`로 바꿉니다.”

“사진 1의 설명처럼 `update`는 오늘 로컬 실습을 위한 설정입니다. 실제 운영 환경에서는 별도의 DB 마이그레이션 방식을 사용합니다.”

### IntelliJ 환경변수 입력

1. 상단 실행 구성 이름 클릭
2. Edit Configurations
3. `SpringsessionApplication` 선택
4. Environment variables 입력
5. `DB_PASSWORD=본인의 MySQL 비밀번호`
6. Apply → OK

### 이때 반드시 말할 문장

“사진에는 예시 비밀번호가 보이지만 그 값을 그대로 복사하지 마세요. 2주차에 본인이 MySQL 계정에 설정한 비밀번호를 넣어야 합니다.”

“환경변수는 실행할 때 읽기 때문에 값을 추가한 다음 실행 중인 애플리케이션을 완전히 종료하고 다시 실행해야 합니다.”

### import.sql

Project 창에서 `src/main/resources/import.sql`이 남아 있다면 삭제한다.

“2주차에는 예제 데이터를 자동으로 넣었지만 오늘은 POST 요청으로 직접 게시글을 생성할 겁니다. 예상하지 않은 글이 생기지 않도록 `import.sql`은 삭제합니다.”

### 실행 확인

애플리케이션을 한 번 실행한다.

정상 기준:

```text
Started SpringsessionApplication
```

### 현장 오류 대응

#### DB_PASSWORD가 없을 때

“로그에 `DB_PASSWORD`, `Access denied`, DataSource 관련 메시지가 보이면 Run Configuration의 환경변수부터 확인하겠습니다.”

확인 순서:

1. YAML이 `${DB_PASSWORD}`인지
2. 실행 중인 Run Configuration이 맞는지
3. 환경변수 이름의 대소문자가 정확한지
4. 값을 넣은 뒤 앱을 재시작했는지

#### Access denied

“Access denied는 MySQL 서버에는 도착했지만 로그인이 실패한 상태입니다. username과 본인의 실제 비밀번호를 확인할게요.”

확인 순서:

1. `username: likelion`
2. 2주차에서 설정한 MySQL 비밀번호
3. Environment variables 값
4. MySQL 계정 권한

#### DB가 꺼졌을 때

“Connection refused 또는 Communications link failure라면 비밀번호보다 먼저 MySQL이 실행 중인지, 3306 포트를 쓰는지 확인합니다.”

#### import.sql 데이터가 보일 때

“파일을 지금 삭제해도 전에 DB에 들어간 행은 남아 있을 수 있습니다. `import.sql`이 현재 존재하는지와 기존 DB 데이터를 구분해서 보겠습니다.”

### 멘티 질문

“앱을 실행할 때마다 테이블을 다시 만들 수 있는 설정은 `create`와 `update` 중 무엇일까요?”

정답: `create`

### 다음 내용 연결

“실행 환경이 준비됐으니 이제 기존 Getter와 생성자 코드를 Lombok으로 줄여보겠습니다.”

---

# Part 1. Lombok 적용하기

예상 소요: 22분

## 1-1. Lombok 소개

### 자료 위치

- Part 1 시작: 원문 144줄
- 「Lombok」: 원문 146~165줄
- 「왜 Lombok을 사용할까요?」: 원문 166~210줄

### 내가 말할 문장

“기존에는 Getter와 생성자를 직접 작성했습니다. 이 코드는 필요하지만 필드가 많아질수록 반복이 커집니다. Lombok은 이런 반복 코드를 어노테이션으로 생성해줍니다.”

“Lombok이 비즈니스 기능까지 대신 만드는 건 아닙니다. 뒤에서 작성할 게시글 생성자처럼 의미가 있는 코드는 직접 작성합니다.”

## 1-2. 의존성 추가와 Gradle Reload

### 자료 위치

- 「의존성 추가」: 원문 211~238줄
- 「오늘 사용할 Lombok」: 원문 239~249줄

### IntelliJ 이동

프로젝트 루트의 `build.gradle` → `dependencies` 블록

### 추가할 코드

```groovy
implementation 'org.springframework.boot:spring-boot-starter-validation'

compileOnly 'org.projectlombok:lombok'
annotationProcessor 'org.projectlombok:lombok'

testCompileOnly 'org.projectlombok:lombok'
testAnnotationProcessor 'org.projectlombok:lombok'
```

### 내가 말할 문장

“Validation 의존성은 `@NotBlank`, `@Size`, `@Valid`를 쓰기 위해 추가합니다. Lombok은 Getter와 생성자 같은 반복 코드를 만들기 위해 추가합니다.”

“저장한 다음 오른쪽 Gradle 탭의 Reload 버튼을 꼭 눌러주세요. 의존성 파일을 수정했지만 Reload하지 않으면 Lombok import가 빨갛게 보일 수 있습니다.”

### Lombok 빨간색 대응

확인 순서:

1. 의존성을 `dependencies` 블록 안에 적었는지
2. Gradle Reload를 눌렀는지
3. Project SDK와 Gradle JVM이 Java 17인지
4. Settings → Compiler → Annotation Processors → Enable annotation processing
5. Build → Rebuild Project

말할 문장:

“IDE의 빨간 줄과 실제 Gradle 컴파일 결과가 다를 수 있습니다. 마지막 판단은 Gradle Build 결과로 하겠습니다.”

## 1-3. Post Entity에 Lombok 적용

### 자료 위치

- 「Post Entity 수정」: 원문 250~278줄
- 사진 3

### IntelliJ 이동

`src/main/java/com/likelion/springsession/post/entity/Post.java`

### 삭제할 코드

- 직접 작성한 protected 기본 생성자
- `getId()`, `getTitle()`, `getContent()`, `getCreatedAt()`

### 추가 import

```java
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
```

### 클래스 위에 추가

```java
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
```

### 사진 3 기준 이 시점의 전체 코드

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

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "posts")
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
}
```

### 내가 말할 문장

“`@Getter`가 기존 Getter를 대신합니다. `@NoArgsConstructor`는 매개변수 없는 생성자를 만들고, protected로 제한해 JPA는 사용할 수 있지만 일반 코드에서 함부로 부르기는 어렵게 합니다.”

“Entity에는 `@Setter`를 붙이지 않습니다. 후반부에서 게시글을 수정한다는 의미가 드러나는 메서드를 따로 추가할 예정입니다.”

### 멘티 질문

“Entity의 기본 생성자 접근 범위를 protected로 만드는 값은 `AccessLevel._____`입니다.”

정답: `PROTECTED`

## 1-4. Response DTO에 Lombok 적용

### 자료 위치

- 「기존 PostSummaryResponse 수정」: 원문 279~288줄
- 사진 4
- 「새로운 PostDetailResponse 만들기」: 원문 289~302줄
- 사진 5

### PostSummaryResponse

경로: `src/main/java/com/likelion/springsession/post/dto/PostSummaryResponse.java`

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

### PostDetailResponse

경로: `src/main/java/com/likelion/springsession/post/dto/PostDetailResponse.java`

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

### 내가 말할 문장

“목록용 DTO에는 본문이 없고 상세 DTO에는 `content`가 있습니다. 같은 Entity라도 API 목적에 따라 필요한 응답 모양이 다르기 때문입니다.”

“`@RequiredArgsConstructor`가 이번 DTO의 final 필드를 모두 받는 생성자를 만들어줍니다. final을 빠뜨리면 우리가 기대한 생성자가 생기지 않으니 확인해 주세요.”

## 1-5. PostService에 Lombok 적용

### 자료 위치

- 「PostService에도 Lombok 적용」: 원문 303~337줄
- 「클래스마다 사용하는 Lombok이 다릅니다」: 원문 338~367줄

### 변경

`PostService.java`에서 직접 작성한 생성자를 삭제하고 다음을 추가한다.

```java
import lombok.RequiredArgsConstructor;
```

```java
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
}
```

### 내가 말할 문장

“생성자 주입 방식을 없앤 것이 아닙니다. `postRepository`가 final이기 때문에 Lombok이 기존과 같은 생성자를 만들어줍니다.”

“사진 10의 Controller는 기존 직접 생성자를 그대로 사용합니다. 오늘 자료에서 Lombok으로 바꾸는 Service와 구분해 주세요.”

### DTO 생성자 오류 대응

“`new PostSummaryResponse(...)`에서 생성자를 찾을 수 없다고 나오면 DTO 필드가 final인지, `@RequiredArgsConstructor`가 붙었는지, Gradle Reload가 됐는지 순서로 확인합니다.”

### 다음 내용 연결

“반복 코드를 정리했으니 이제 2주차에 만든 Repository가 어떤 CRUD 메서드를 이미 제공하는지 확인하겠습니다.”

---

# Part 2. JpaRepository와 CRUD

예상 소요: 8분

### 자료 위치

- Part 2 시작: 원문 368줄
- JpaRepository 코드와 CRUD 표: 원문 368~395줄
- 「왜 직접 구현하지 않을까요?」: 원문 396~430줄

### IntelliJ 이동

`src/main/java/com/likelion/springsession/post/repository/PostRepository.java`

### 화면에 보여줄 코드

```java
package com.likelion.springsession.post.repository;

import com.likelion.springsession.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
}
```

### 내가 말할 문장

“메서드를 직접 만들지 않았지만 `JpaRepository<Post, Long>`을 상속해서 기본 CRUD 메서드를 사용할 수 있습니다. Post는 관리할 Entity이고 Long은 ID 타입입니다.”

“전반부에서는 생성의 `save()`, 조회의 `findAll()`과 `findById()`를 씁니다. 수정과 삭제는 후반부에서 이어갑니다.”

### 멘티 질문

```java
public interface PostRepository extends JpaRepository<____, ____> {
}
```

정답: `Post`, `Long`

### 흔한 오류

- `JpaRepository` import는 `org.springframework.data.jpa.repository.JpaRepository`
- Entity ID가 `Long`이므로 Repository 두 번째 타입도 `Long`
- Repository 구현 클래스를 직접 만들 필요 없음

### 다음 내용 연결

“이제 클라이언트가 보낸 제목과 본문을 받을 Request DTO를 만들고 실제로 `save()`를 호출하겠습니다.”

---

# Part 3. CREATE — 게시글 생성

예상 소요: 25분

## 3-1. PostCreateRequest

### 자료 위치

- Part 3 시작: 원문 431줄
- 「PostCreateRequest 만들기」: 원문 433~445줄
- 사진 6

### IntelliJ 조작

`post/dto` 우클릭 → New → Java Class → `PostCreateRequest`

경로: `src/main/java/com/likelion/springsession/post/dto/PostCreateRequest.java`

### 입력할 전체 코드

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

### 내가 말할 문장

“생성 요청에서 받을 값은 제목과 본문입니다. Spring Boot 4에서는 Validation import가 `jakarta.validation`으로 시작합니다.”

“`@NotBlank`는 null, 빈 문자열, 공백만 있는 문자열을 막고, `@Size`는 최대 길이를 검사합니다. 실제 검사는 잠시 뒤 Controller 매개변수에 `@Valid`를 붙였을 때 실행됩니다.”

“Request DTO에는 이번 실습 방식대로 Getter, Setter, 기본 생성자를 사용합니다.”

## 3-2. Post 생성자 추가

### 자료 위치

- 「Post에 생성자 추가」: 원문 446~465줄
- 사진 7

### IntelliJ 이동

`src/main/java/com/likelion/springsession/post/entity/Post.java`

### 필드 아래에 추가

```java
public Post(String title, String content) {
    this.title = title;
    this.content = content;
    this.createdAt = LocalDateTime.now();
}
```

### 내가 말할 문장

“`@NoArgsConstructor`가 만든 생성자는 JPA가 사용하고, 지금 직접 만든 `Post(title, content)`는 새 게시글을 만들 때 사용합니다.”

“작성 시각은 요청으로 받지 않고 서버에서 `LocalDateTime.now()`로 만듭니다.”

## 3-3. Service 생성 기능과 응답 변환

### 자료 위치

- 「Service에 게시글 생성 기능 추가」: 원문 466~471줄
- 사진 8
- 「Entity를 Response DTO로 변환하기」: 원문 472~496줄
- 사진 8~9
- 빈칸 문제: 원문 497~513줄

### PostService 추가 import

```java
import com.likelion.springsession.post.dto.PostCreateRequest;
import com.likelion.springsession.post.dto.PostDetailResponse;
```

### 클래스 내부에 추가

```java
public PostDetailResponse createPost(PostCreateRequest request) {
    Post post = new Post(
            request.getTitle(),
            request.getContent()
    );

    Post savedPost = postRepository.save(post);
    return toDetailResponse(savedPost);
}

private PostDetailResponse toDetailResponse(Post post) {
    return new PostDetailResponse(
            post.getId(),
            post.getTitle(),
            post.getContent(),
            post.getCreatedAt()
    );
}
```

### 내가 말할 문장

“Request DTO에서 제목과 본문을 꺼내 Post를 만들고, Repository의 `save()`로 저장합니다.”

“저장된 Entity 자체를 Controller로 반환하지 않고 `PostDetailResponse`로 바꿉니다. 이 변환 메서드는 뒤의 상세 조회에서도 다시 사용할 겁니다.”

“저장 결과인 `savedPost`에는 DB가 생성한 ID가 들어 있으므로 응답에 ID도 포함됩니다. 작성 시각은 Post 생성자에서 이미 만들었습니다.”

### 빈칸 질문

```java
Post savedPost = postRepository._____(post);
```

정답: `save`

## 3-4. Controller에 POST 연결

### 자료 위치

- 「Controller에 생성 기능 연결」: 원문 514~552줄
- 사진 10

### 먼저 공통 경로 변경

기존:

```java
@RestController
public class PostController {

    @GetMapping("/api/posts")
```

변경:

```java
@RestController
@RequestMapping("/api/posts")
public class PostController {

    @GetMapping
```

### 추가 import

```java
import com.likelion.springsession.post.dto.PostCreateRequest;
import com.likelion.springsession.post.dto.PostDetailResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
```

### 사진 10 기준 Controller

```java
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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PostDetailResponse createPost(
            @Valid @RequestBody PostCreateRequest request
    ) {
        return postService.createPost(request);
    }
}
```

### 내가 말할 문장

“게시글 API의 공통 주소 `/api/posts`를 클래스 위로 올립니다. 그다음 기존 `@GetMapping("/api/posts")`에서는 경로를 꼭 지워서 `@GetMapping`만 남깁니다.”

“이걸 지우지 않으면 클래스 경로와 메서드 경로가 합쳐져 `/api/posts/api/posts`가 됩니다.”

“`@RequestBody`가 JSON을 Request DTO로 변환하고, `@Valid`가 DTO의 검증 조건을 실행합니다. `@ResponseStatus(HttpStatus.CREATED)`로 생성 성공 상태를 201로 반환합니다.”

## 3-5. Postman 생성 확인

### 자료 위치

- 「Postman으로 확인」: 원문 553~586줄

### 요청

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

### 정상 결과

```text
201 Created
```

```json
{
  "id": 1,
  "title": "JPA CRUD",
  "content": "게시글을 생성해봅니다.",
  "createdAt": "2026-09-16T19:30:00.123456"
}
```

“ID와 시간은 사람마다 다를 수 있습니다. 상태가 201이고 제목과 본문이 보낸 값과 같은지 확인합니다. 목록 조회를 위해 제목을 바꿔 두세 개 더 만들어주세요.”

### Validation 실패도 한 번 확인

```json
{
  "title": " ",
  "content": ""
}
```

예상: `400 Bad Request`

### POST 오류 확인 순서

1. POST method
2. URL `/api/posts`
3. Body → raw → JSON
4. Content-Type application/json
5. JSON 쉼표와 따옴표
6. `@RequestBody`
7. `@Valid`
8. Request DTO의 Getter/Setter/기본 생성자
9. 서버 콘솔의 첫 번째 오류

### POST 성공 후 조회 결과가 다를 때

“POST 상태가 실제로 201이었는지, Hibernate INSERT 로그가 나왔는지, 앱을 다시 켜면서 `ddl-auto:create`로 데이터를 지우지 않았는지 확인합니다. POST와 GET이 같은 서버와 같은 DB를 보는지도 확인할게요.”

### 다음 내용 연결

“이제 우리가 직접 만든 데이터가 DB에 들어갔습니다. 같은 Repository로 목록과 상세를 조회해보겠습니다.”

---

# Part 4. READ — 게시글 조회

예상 소요: 20분

## 4-1. 목록 조회

### 자료 위치

- Part 4 시작: 원문 587줄
- 「게시글 목록 조회」: 원문 589~624줄
- 「PostController에 목록 조회 연결」: 원문 625~640줄
- 목록 Postman: 원문 701~724줄

### PostService에서 확인할 기존 코드

```java
public List<PostSummaryResponse> getPostSummaries() {
    List<Post> posts = postRepository.findAll();
    List<PostSummaryResponse> responses = new ArrayList<>();

    for (Post post : posts) {
        PostSummaryResponse response = new PostSummaryResponse(
                post.getId(),
                post.getTitle(),
                post.getCreatedAt()
        );

        responses.add(response);
    }

    return responses;
}
```

### Controller

```java
@GetMapping
public List<PostSummaryResponse> getPosts() {
    return postService.getPostSummaries();
}
```

### 내가 말할 문장

“`findAll()`은 모든 Post를 `List<Post>`로 가져옵니다. API에서는 Entity 목록을 그대로 반환하지 않고 반복문으로 `PostSummaryResponse` 목록을 만듭니다.”

“목록용 DTO에는 content가 없기 때문에 응답에도 본문이 나오지 않는 것이 정상입니다.”

### Postman

```http
GET http://localhost:8080/api/posts
```

정상: `200 OK`

```json
[
  {
    "id": 1,
    "title": "JPA CRUD",
    "createdAt": "2026-09-16T19:30:00.123456"
  }
]
```

### 목록이 []일 때

“빈 배열은 오류가 아니라 현재 연결된 DB에 게시글이 없다는 정상 200 응답입니다.”

확인 순서:

1. POST가 201이었는지
2. INSERT SQL이 출력됐는지
3. `ddl-auto`가 update인지
4. 같은 `likelion_blog` DB를 보는지

## 4-2. 상세 조회

### 자료 위치

- 「게시글 하나 조회」: 원문 641~658줄
- 「PostController에 상세 조회 연결」: 원문 659~675줄
- 빈칸 문제: 원문 676~700줄
- 상세 Postman: 원문 725~748줄

### PostService에 추가

```java
public PostDetailResponse getPost(Long postId) {
    Post post = findPostById(postId);
    return toDetailResponse(post);
}

private Post findPostById(Long postId) {
    return postRepository.findById(postId)
            .orElseThrow();
}
```

### Controller에 추가

```java
@GetMapping("/{postId}")
public PostDetailResponse getPost(@PathVariable Long postId) {
    return postService.getPost(postId);
}
```

필요 import:

```java
import org.springframework.web.bind.annotation.PathVariable;
```

### 내가 말할 문장

“`findById()`는 게시글이 없을 수도 있으므로 `Optional<Post>`를 반환합니다. 오늘 자료에서는 가장 단순하게 `orElseThrow()`로 값이 없을 때 예외를 발생시킵니다.”

“URL의 `{postId}`를 `@PathVariable`로 받아 Service에 전달합니다. 상세 응답에는 목록과 달리 content도 포함됩니다.”

### 꼭 덧붙일 정확한 설명

“현재처럼 인자 없는 `orElseThrow()`만 사용하면 없는 ID를 요청했을 때 자동으로 404가 되는 것은 아닙니다. 처리되지 않은 예외가 되어 500이 나올 수 있습니다. 오늘 CREATE/READ 핵심 실습에서는 실제 존재하는 ID를 사용하고, 404 응답을 다듬는 예외 처리는 이후 확장 내용으로 남기겠습니다.”

이 설명은 코드를 추가로 바꾸지 않고 현재 동작을 정확히 알려주기 위한 것이다.

### 빈칸 질문

```java
List<Post> posts = postRepository._____();
```

정답: `findAll`

```java
return postRepository._____(postId)
        .orElseThrow();
```

정답: `findById`

### Postman

```http
GET http://localhost:8080/api/posts/1
```

실제로 생성된 ID를 목록에서 확인해 사용한다.

정상: `200 OK`

```json
{
  "id": 1,
  "title": "JPA CRUD",
  "content": "게시글을 생성해봅니다.",
  "createdAt": "2026-09-16T19:30:00.123456"
}
```

### 상세 조회 오류 대응

#### findById 타입 오류

“`findById()`의 반환 타입은 Post가 아니라 `Optional<Post>`입니다. 바로 Post 변수에 넣지 말고 자료처럼 `orElseThrow()`까지 작성했는지 확인합니다.”

#### URL이 /api/posts/api/posts가 됨

“클래스의 `@RequestMapping("/api/posts")`와 메서드 경로는 합쳐집니다. 목록은 `@GetMapping`, 상세는 `@GetMapping("/{postId}")`만 적습니다.”

#### 존재하지 않는 ID

“ID가 꼭 1이라고 가정하지 말고 목록 응답에서 실제 ID를 복사합니다. 존재하지 않는 ID라면 현재 단순 예외 처리에서는 500이 날 수 있습니다.”

#### 응답에 content가 없음

“목록 URL이 아니라 `/api/posts/{실제 ID}`로 요청했는지, Service가 `PostDetailResponse`를 반환하는지 확인합니다.”

#### 400·404·500 구분

- 400: JSON 형식 오류 또는 Validation 실패
- 404: 요청 URL 자체가 매핑되지 않음
- 500: 현재 코드에서 없는 게시글처럼 처리되지 않은 서버 예외

“없는 게시글을 404로 만들려면 별도 예외 매핑이 필요하지만 오늘 전반부 필수 구현에는 추가하지 않습니다.”

## 4-3. 완성된 전반부 Service 확인

### 자료 위치

- 「Part 4-1. 여기까지 PostService」: 원문 749~823줄
- 「자주 발생하는 오류」: 원문 824~835줄

### 화면 진행

자료의 전체 `PostService` 코드와 IntelliJ 파일을 나란히 두고 다음 메서드가 있는지 확인한다.

```text
getPostSummaries()
getPost()
createPost()
findPostById()
toDetailResponse()
```

### 내가 말할 문장

“여기까지 자료의 전체 Service와 우리 파일을 비교하겠습니다. 메서드 순서는 조금 달라도 괜찮지만 이름, 매개변수, 반환 타입이 같은지 확인해 주세요.”

### 확인 순서

1. DTO import
2. `@Service`, `@RequiredArgsConstructor`
3. `final PostRepository`
4. 목록 조회
5. 상세 조회
6. 생성
7. 공통 조회 메서드
8. 상세 DTO 변환 메서드

---

# 전반부 정리

예상 소요: 5분

### 자료 위치

- 「전반부 정리 및 복습」: 원문 836~874줄
- 「다음 파트」: 원문 875~890줄

### 내가 말할 문장

“전반부에서 사용한 JpaRepository 메서드는 세 개입니다. 생성은 `save()`, 전체 조회는 `findAll()`, 하나 조회는 `findById()`입니다.”

```text
CREATE → save()
READ 전체 → findAll()
READ 하나 → findById()
```

“Lombok으로 Getter, JPA 기본 생성자, final 필드 생성자, Service 생성자를 줄였습니다. 모든 클래스에 같은 Lombok을 붙인 것이 아니라 역할에 맞춰 다르게 사용했습니다.”

“API는 POST `/api/posts`, GET `/api/posts`, GET `/api/posts/{postId}`까지 완성했습니다.”

### 최종 질문

1. “`findById()`가 Optional을 반환하는 이유는 무엇인가요?”
2. “목록 DTO와 상세 DTO에서 다른 필드는 무엇인가요?”
3. “POST 성공을 201로 만든 어노테이션은 무엇인가요?”
4. “Entity에 Setter를 붙이지 않은 이유는 무엇인가요?”
5. “Validation을 실행하게 하는 Controller 어노테이션은 무엇인가요?”

### 기대 답

1. 해당 ID의 게시글이 없을 수 있어서
2. 상세 DTO에는 content가 있음
3. `@ResponseStatus(HttpStatus.CREATED)`
4. 아무 곳에서나 Entity 상태를 바꾸지 않기 위해
5. `@Valid`

---

# 후반부 강사 연결 멘트

### 자료 위치

- 「다음 파트」: 원문 875~890줄
- 후반부 시작: 원문 891줄

### 그대로 읽을 멘트

“여기까지 새 Post는 `save()`로 저장했고, `findAll()`과 `findById()`로 조회했습니다.”

“이제 후반부에서는 방금 `findById()`로 조회한 Post의 제목과 본문을 바꿔볼 건데요. 수정할 때는 `save()`를 다시 호출하지 않아도 UPDATE가 실행됩니다.”

“JPA가 어떻게 변경을 알아차리는지 이해하려면 영속성 컨텍스트, 변경 감지, flush와 commit, 그리고 `@Transactional`을 알아야 합니다. 이 원리를 먼저 살펴본 다음 PUT과 DELETE까지 완성하겠습니다. 이제 후반부 멘토님께 넘기겠습니다.”

---

# 강의 직전 3분 체크리스트

1. 사진 2의 고정 비밀번호를 “본인의 MySQL 비밀번호”로 수정했는가?
2. IntelliJ Run Configuration에 실제 `DB_PASSWORD`를 넣었는가?
3. MySQL이 실행 중인가?
4. `application.yaml`의 DB 이름이 `likelion_blog`인가?
5. `ddl-auto:update`인가?
6. `import.sql`을 제거했는가?
7. Gradle Reload를 했는가?
8. 애플리케이션이 Started 상태인가?
9. Postman JSON의 Content-Type이 application/json인가?
10. 상세 조회에는 목록에서 확인한 실제 ID를 사용하는가?

## 최종 Postman 순서

1. 정상 POST → 201
2. 빈 제목 POST → 400
3. 목록 GET → 200
4. 실제 ID 상세 GET → 200, content 확인
5. 없는 ID 요청은 현재 자료 코드에서 500 가능성이 있음을 설명
