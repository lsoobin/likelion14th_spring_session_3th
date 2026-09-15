# 3주차 전반부 실습 강의 대본

담당 범위: Part 0~4, 전반부 정리, 후반부 연결
예상 총 소요: 약 95분(환경 문제 대응 시간 10분 포함)

> 화면 공유 전에 IntelliJ에서 저장소 루트 `likelion14th_spring_session_3th`를 열고, MySQL과 Postman을 준비한다. 경로는 모두 프로젝트 루트 기준이다.

## 강의 전 체크

- JDK 17 선택
- Gradle JVM도 JDK 17
- MySQL 실행
- `likelion_blog` DB와 `likelion` 사용자 준비
- Run Configuration의 `DB_PASSWORD` 설정
- `import.sql` 제거
- Postman의 Content-Type이 application/json
- 강사용 최종 브랜치가 아니라 2주차 시작 상태에서 실습할 경우, 별도 복사본이나 커밋을 준비

---

# Part 0. 지난 코드 이어서 사용하기

예상 소요: 12분

## 화면과 클릭

1. IntelliJ 시작 화면에서 Open을 누른다.
2. `spring_session_3/likelion14th_spring_session_3th` 폴더를 선택한다.
3. 왼쪽 Project 창에서 `build.gradle`이 최상단에 보이는지 확인한다.
4. `src/main/java/com/likelion/springsession/post`를 펼친다.
5. `src/main/resources/application.yaml`을 연다.

## 내가 말할 문장

“오늘은 새 프로젝트를 만들지 않고 2주차 코드를 그대로 이어갑니다. 지금 왼쪽에 controller, dto, entity, repository, service가 보이죠. 지난 시간에는 DB의 게시글을 목록 DTO로 바꾸는 흐름까지 만들었고, 오늘은 그 위에 생성과 상세 조회를 붙일 거예요.”

“프로젝트 루트는 build.gradle과 gradlew가 보이는 이 폴더입니다. 상위의 spring_session_3 폴더만 열면 Gradle 프로젝트를 바로 인식하지 못할 수 있으니 꼭 이 위치를 확인해 주세요.”

## 2주차에서 확인할 파일

```text
src/main/java/com/likelion/springsession/post/
├── controller/PostController.java
├── dto/PostSummaryResponse.java
├── entity/Post.java
├── repository/PostRepository.java
└── service/PostService.java
```

“흐름을 한 번만 읽고 갈게요. HTTP 요청은 Controller가 받고, Service가 작업 순서를 정하고, Repository가 JPA를 통해 DB와 대화합니다. Entity는 posts 테이블과 연결되고, DTO는 요청과 응답 모양을 담당합니다.”

## application.yaml 변경

파일: `src/main/resources/application.yaml`

기존:

```yaml
password: <기존 평문 비밀번호>
```

변경:

```yaml
password: ${DB_PASSWORD}
```

기존:

```yaml
ddl-auto: create
```

변경:

```yaml
ddl-auto: update
```

최종 확인:

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

“비밀번호는 Git에 올라가면 안 되기 때문에 환경변수로 뺍니다. 중괄호 안의 DB_PASSWORD는 비밀번호 자체가 아니라 환경변수 이름이에요.”

“create는 앱을 켤 때 스키마를 다시 만들 수 있어서 기존 데이터가 사라질 수 있습니다. 오늘은 POST로 만든 데이터를 이어서 조회해야 하니 로컬 실습에서는 update를 씁니다. 다만 update가 운영 배포의 정답이라는 뜻은 아닙니다. 운영에서는 변경 이력을 명시적으로 관리하는 마이그레이션 도구를 사용합니다.”

## IntelliJ 환경변수

클릭:

1. 상단 실행 구성 드롭다운
2. Edit Configurations
3. `SpringsessionApplication`
4. Modify options → Operating System → Environment variables
5. `DB_PASSWORD=자신의 MySQL 비밀번호`
6. Apply → OK

“환경변수는 실행 프로세스가 시작될 때 읽습니다. 값을 바꿨다면 실행 중인 앱을 완전히 멈추고 다시 켜야 합니다.”

## import.sql 제거

파일: `src/main/resources/import.sql`

“2주차에는 시작 데이터를 자동으로 넣기 위해 import.sql을 썼습니다. 오늘은 우리가 POST 요청으로 직접 만들 거라 이 파일은 삭제하겠습니다. Git 파일이라 실수해도 버전 관리에서 복구할 수 있습니다.”

삭제할 기존 내용:

```sql
INSERT INTO posts (title, content, created_at) VALUES (...);
INSERT INTO posts (title, content, created_at) VALUES (...);
```

## 실행 시점

여기서는 코드를 바꾸기 전 환경만 확인한다. 애플리케이션을 한 번 실행해 DB 연결을 확인하고 종료한다.

내 멘트:

“오른쪽 위 실행 버튼을 눌러서 started 로그가 나오는지 보겠습니다. 실패하면 코드 작성 전에 DB 문제부터 분리할 수 있어요.”

## 빈칸 질문

“비밀번호 자리에 직접 값 대신 `${_____}`를 적었습니다. 환경변수 이름은 뭘까요?”
정답: `DB_PASSWORD`

“앱을 켤 때마다 테이블을 새로 만들 수 있는 설정은 create일까요, update일까요?”
정답: `create`

## 자주 나는 오류와 확인 순서

### DB_PASSWORD가 설정되지 않음

현장 멘트:

“로그에서 DB_PASSWORD 또는 placeholder라는 단어를 찾아볼게요. 환경변수 이름의 철자와 대소문자를 확인하고, Run Configuration에 넣은 뒤 앱을 완전히 재시작합니다.”

확인 순서:

1. `application.yaml`이 정확히 `${DB_PASSWORD}`인지
2. 현재 실행 중인 Run Configuration이 맞는지
3. Environment variables에 이름과 값이 있는지
4. 실행 프로세스를 종료하고 재실행했는지

### MySQL Access denied

현장 멘트:

“Access denied는 MySQL 서버까지는 도착했지만 로그인이 거절됐다는 뜻입니다. DB가 꺼진 문제와 구분해서 username, 비밀번호, 계정 권한을 보겠습니다.”

확인 순서:

1. `username: likelion`
2. `DB_PASSWORD` 실제 값
3. MySQL에서 해당 계정으로 직접 로그인
4. `'likelion'@'localhost'` 권한
5. 비밀번호 변경 후 Run Configuration도 같이 바꿨는지

### DB가 실행되지 않음

현장 멘트:

“Connection refused나 Communications link failure면 로그인 전 단계입니다. MySQL 서비스가 켜져 있는지와 3306 포트를 먼저 확인할게요.”

확인 순서:

1. MySQL 서비스/컨테이너 실행
2. 포트 3306
3. DB 이름 `likelion_blog`
4. datasource URL 오타

### import.sql 때문에 데이터가 생김

“우리가 POST하지 않았는데 행이 보이면 현재 프로젝트에 import.sql이 남아 있는지 검색합니다. 파일을 지워도 이미 DB에 들어간 행은 자동으로 없어지지 않으니, 기존 행인지도 구분해야 해요.”

## 다음 단계 연결 멘트

“DB와 2주차 코드가 준비됐습니다. 이제 기능을 더하기 전에 Getter와 생성자처럼 반복되는 코드를 Lombok으로 정리해볼게요.”

---

# Part 1. Lombok 적용

예상 소요: 22분

## 1-1. build.gradle

클릭: 프로젝트 루트 → `build.gradle` → `dependencies` 블록

추가:

```groovy
implementation 'org.springframework.boot:spring-boot-starter-validation'

compileOnly 'org.projectlombok:lombok'
annotationProcessor 'org.projectlombok:lombok'

testCompileOnly 'org.projectlombok:lombok'
testAnnotationProcessor 'org.projectlombok:lombok'
```

내가 말할 문장:

“Validation은 요청값을 검사하는 기능이고 Lombok은 반복되는 Java 코드를 컴파일할 때 만들어주는 도구입니다. 역할이 다르기 때문에 의존성도 따로 추가합니다.”

“compileOnly는 실행 결과물에 Lombok 자체를 넣지 않고 컴파일할 때만 쓰겠다는 뜻이고, annotationProcessor는 Lombok 어노테이션을 실제 코드로 처리하게 합니다.”

## Gradle Reload

클릭: IntelliJ 오른쪽 Gradle 탭 → Reload All Gradle Projects

“이 단계는 저장만 해서는 부족할 수 있습니다. Gradle Reload가 끝난 뒤 External Libraries에 Lombok이 들어왔는지 확인하고 다음으로 넘어갈게요.”

## 1-2. Post Entity

경로: `src/main/java/com/likelion/springsession/post/entity/Post.java`

추가 import:

```java
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
```

클래스 위에 추가:

```java
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
```

삭제:

```java
protected Post() {
}

public Long getId() { ... }
public String getTitle() { ... }
public String getContent() { ... }
public LocalDateTime getCreatedAt() { ... }
```

이 시점의 전체 코드:

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
}
```

내 멘트:

“JPA Entity에는 public 또는 protected 기본 생성자가 필요합니다. 우리는 일반 코드에서 막 만들지 못하도록 protected를 선택합니다. Getter는 읽기만 열고, Entity 전체 Setter는 열지 않을게요. 후반부에서 게시글 수정이라는 의미가 드러나는 메서드를 따로 만듭니다.”

“2주차와 동작은 같고 직접 쓴 반복 코드만 Lombok이 대신합니다.”

## 1-3. 목록 Response DTO

경로: `src/main/java/com/likelion/springsession/post/dto/PostSummaryResponse.java`

추가 import:

```java
import lombok.Getter;
import lombok.RequiredArgsConstructor;
```

추가 어노테이션:

```java
@Getter
@RequiredArgsConstructor
```

직접 쓴 생성자와 Getter를 삭제한 전체 코드:

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

“RequiredArgsConstructor는 이번 클래스의 초기화되지 않은 final 필드를 모두 받는 생성자를 만듭니다. final을 빼면 우리가 원하는 생성자가 만들어지지 않는다는 점을 기억해 주세요.”

## 1-4. 상세 Response DTO

클릭: `post/dto` 우클릭 → New → Java Class → `PostDetailResponse`

경로: `src/main/java/com/likelion/springsession/post/dto/PostDetailResponse.java`

전체 코드:

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

“목록은 빠르게 훑는 화면이라 본문을 빼고, 상세는 본문까지 포함합니다. Entity를 그대로 반환하지 않고 API 목적에 맞는 DTO를 나누는 예시입니다.”

## 1-5. PostService

경로: `src/main/java/com/likelion/springsession/post/service/PostService.java`

추가 import:

```java
import lombok.RequiredArgsConstructor;
```

클래스 위:

```java
@Service
@RequiredArgsConstructor
```

삭제:

```java
public PostService(PostRepository postRepository) {
    this.postRepository = postRepository;
}
```

“생성자 주입을 없앤 게 아닙니다. final 필드를 받는 같은 생성자를 Lombok이 만들어줍니다.”

## 빈칸 질문

“JPA용 protected 기본 생성자를 만드는 어노테이션은 `@NoArgsConstructor(access = AccessLevel._____)`입니다.”
정답: `PROTECTED`

“RequiredArgsConstructor가 이번 코드에서 생성자 매개변수로 삼는 필드의 키워드는?”
정답: `final`

## Lombok 현장 대응

### import가 빨간색

현장 멘트:

“빨간색이면 어노테이션을 지우기 전에 Gradle이 의존성을 받았는지부터 확인합니다.”

순서:

1. `build.gradle`의 의존성이 `dependencies` 안인지
2. 오타와 따옴표 확인
3. Gradle Reload
4. Gradle 창의 Dependencies 또는 External Libraries 확인
5. Project SDK/Gradle JVM이 17인지
6. IntelliJ Lombok 플러그인 상태
7. Settings → Build, Execution, Deployment → Compiler → Annotation Processors → Enable annotation processing
8. Build → Rebuild Project

### Reload를 하지 않음

“코드는 맞는데 import가 안 잡히면 오른쪽 Gradle의 새로고침 아이콘을 눌렀는지 먼저 물어볼게요. IDE가 새 의존성을 아직 모르는 상태일 수 있습니다.”

### annotation processing 문제

“Gradle 빌드는 성공하는데 IDE만 빨갛거나, 반대로 IDE는 괜찮은데 컴파일에서 Getter가 없다고 하면 annotation processor 설정과 Gradle 빌드 결과를 따로 봅니다. 최종 판단은 실제 Gradle 빌드입니다.”

## 실행 시점

Gradle Reload 후 Build → Build Project. DB 재실행은 아직 필수가 아니다.

## 다음 단계 연결 멘트

“반복 코드를 줄였으니 이제 Repository가 이미 제공하는 CRUD 도구를 확인하고, 직접 SQL 없이 생성과 조회를 연결해보겠습니다.”

---

# Part 2. JpaRepository와 CRUD

예상 소요: 10분

## 화면과 코드

경로: `src/main/java/com/likelion/springsession/post/repository/PostRepository.java`

```java
package com.likelion.springsession.post.repository;

import com.likelion.springsession.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
}
```

내가 말할 문장:

“여기 메서드를 한 줄도 안 썼는데 save, findAll, findById, delete를 쓸 수 있습니다. Post는 이 Repository가 관리할 Entity이고 Long은 Post의 ID 타입입니다.”

“save는 이름 때문에 무조건 INSERT라고 외우면 안 됩니다. Entity가 새것인지 기존 것인지에 따라 내부 동작이 달라지고, 오늘 새 Post는 ID가 없으니 INSERT됩니다.”

“findById는 Post가 아니라 Optional<Post>를 돌려줍니다. 요청한 ID가 없을 수 있다는 사실을 타입으로 표현한 거예요.”

## 표로 판서

```text
CREATE  save()
READ    findAll(), findById()
UPDATE  후반부: 영속 상태 Entity + 변경 감지
DELETE  후반부: delete()
```

## 빈칸 질문

```java
public interface PostRepository extends JpaRepository<____, ____> {
}
```

정답: `Post`, `Long`

“ID 하나로 찾는 메서드 이름은?”
정답: `findById`

## 자주 나는 오류

- `JpaRepository` import가 다른 패키지: `org.springframework.data.jpa.repository.JpaRepository`
- ID 타입을 `long` 또는 `Integer`로 잘못 적음: Entity의 `Long id`와 맞춘다.
- 직접 구현 클래스를 만들려고 함: Spring Data JPA가 런타임 구현체를 제공한다.

## 다음 단계 연결 멘트

“Repository 준비는 이미 끝나 있었습니다. 이제 클라이언트가 보낸 제목과 내용을 받을 DTO부터 만들고 save를 호출해볼게요.”

---

# Part 3. CREATE

예상 소요: 25분

## 3-1. PostCreateRequest

클릭: `post/dto` 우클릭 → New → Java Class → `PostCreateRequest`

경로: `src/main/java/com/likelion/springsession/post/dto/PostCreateRequest.java`

전체 코드:

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
    @Size(max = 100, message = "제목은 100자 이하여야 합니다.")
    private String title;

    @NotBlank(message = "내용은 필수입니다.")
    @Size(max = 2000, message = "내용은 2000자 이하여야 합니다.")
    private String content;
}
```

내 멘트:

“Spring Boot 4에서는 validation import가 javax가 아니라 jakarta로 시작합니다. 제목과 내용은 공백만 보내도 안 되도록 NotBlank, Entity 컬럼 길이를 넘기 전에 400으로 알려주도록 Size를 둡니다.”

“이번 실습에서는 Jackson이 기본 생성자로 DTO를 만들고 Setter로 JSON 값을 채우는 단순한 방식을 선택합니다. Setter가 Jackson의 유일한 방식이라는 뜻은 아닙니다.”

“Request DTO와 Entity를 나누면 클라이언트가 id나 createdAt을 마음대로 보내는 구조를 피할 수 있습니다.”

## 3-2. Post 생성자

경로: `src/main/java/com/likelion/springsession/post/entity/Post.java`

필드 아래에 추가:

```java
public Post(String title, String content) {
    this.title = title;
    this.content = content;
    this.createdAt = LocalDateTime.now();
}
```

필요 import는 기존에 있는:

```java
import java.time.LocalDateTime;
```

“Lombok이 만든 기본 생성자는 JPA용이고, 지금 직접 쓰는 생성자는 새 게시글을 만들기 위한 애플리케이션용입니다. 작성 시각은 서버가 여기서 결정하므로 저장 직후 응답에도 값이 들어갑니다.”

## 3-3. PostService 생성 로직

경로: `src/main/java/com/likelion/springsession/post/service/PostService.java`

추가 import:

```java
import com.likelion.springsession.post.dto.PostCreateRequest;
import com.likelion.springsession.post.dto.PostDetailResponse;
```

클래스 내부에 추가:

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

내 멘트:

“요청 DTO로 Entity를 만들고, save 결과를 다시 받습니다. ID는 저장 과정에서 생기므로 응답은 원래 post보다 savedPost를 기준으로 만드는 게 의도가 선명합니다.”

“Entity를 그대로 반환하지 않고 상세 Response DTO로 바꾸는 코드는 생성과 상세 조회에서 같이 쓸 거라 메서드로 뺐습니다.”

빈칸:

```java
Post savedPost = postRepository._____(post);
```

정답: `save`

## 3-4. PostController 공통 경로와 POST

경로: `src/main/java/com/likelion/springsession/post/controller/PostController.java`

추가 import:

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

클래스 위:

```java
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {
```

기존 목록 매핑 변경:

```java
// 변경 전
@GetMapping("/api/posts")

// 변경 후
@GetMapping
```

POST 추가:

```java
@PostMapping
@ResponseStatus(HttpStatus.CREATED)
public PostDetailResponse createPost(
        @Valid @RequestBody PostCreateRequest request
) {
    return postService.createPost(request);
}
```

“클래스의 공통 경로와 메서드 경로는 합쳐집니다. 그래서 기존 GetMapping에 /api/posts를 남겨두면 /api/posts/api/posts가 되어버립니다. 반드시 기존 경로를 비웁니다.”

“RequestBody가 JSON을 DTO로 바꾸고 Valid가 제약을 실행합니다. ResponseStatus로 생성 성공을 201로 명시합니다.”

## 실행 및 Postman

1. Build → Build Project
2. 애플리케이션 실행
3. 콘솔에서 Started 확인
4. Postman → New HTTP Request
5. POST, URL 입력
6. Body → raw → JSON

```http
POST http://localhost:8080/api/posts
```

```json
{
  "title": "JPA CRUD",
  "content": "게시글을 생성해봅니다."
}
```

정상:

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

“ID와 시간은 제 화면과 달라도 정상입니다. title, content가 보낸 값과 같고 상태가 201인지 확인합니다. 조회를 위해 제목을 바꿔 두세 건 더 만들어주세요.”

Validation 실패 실습:

```json
{
  "title": " ",
  "content": ""
}
```

예상: `400 Bad Request`

## CREATE 오류 확인 순서

1. HTTP method가 POST인지
2. URL이 정확히 `/api/posts`인지
3. Body raw JSON인지
4. Content-Type application/json인지
5. JSON 쉼표·따옴표 문법
6. Controller에 `@RequestBody`
7. `@Valid`와 Request DTO의 jakarta Validation import
8. Request DTO 기본 생성자/Setter
9. Service의 DTO 생성자 필드 순서
10. 서버 로그의 첫 예외

### DTO 생성자 오류

“Response DTO 생성자가 없다고 나오면 네 필드가 final인지, RequiredArgsConstructor가 붙었는지, Gradle Reload가 됐는지 순서로 봅니다. Request DTO의 기본 생성자 오류라면 NoArgsConstructor를 확인합니다.”

### POST는 성공했는데 조회 결과가 다름

“지금 POST 응답만 성공한 건지, 실제 DB에 INSERT가 됐는지 SQL 로그를 봅니다. 다음으로 앱이 재시작되며 create로 테이블을 지우지 않았는지, POST와 GET이 같은 서버와 같은 DB URL을 보는지 확인합니다.”

## 다음 단계 연결 멘트

“이제 DB에 데이터가 생겼습니다. 같은 Repository의 findAll과 findById로 목록과 상세를 나눠서 읽어보겠습니다.”

---

# Part 4. READ

예상 소요: 24분

## 4-1. 목록 조회

경로: `src/main/java/com/likelion/springsession/post/service/PostService.java`

2주차 코드 확인:

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

경로: `src/main/java/com/likelion/springsession/post/controller/PostController.java`

```java
@GetMapping
public List<PostSummaryResponse> getPosts() {
    return postService.getPostSummaries();
}
```

내 멘트:

“findAll 결과는 List<Post>입니다. API에는 Entity를 그대로 내보내지 않고 반복문으로 요약 DTO 목록을 만듭니다. 목록용 DTO라 content는 의도적으로 없습니다.”

Postman:

```http
GET http://localhost:8080/api/posts
```

Body 없음. 정상 상태 `200 OK`.

예시:

```json
[
  {
    "id": 1,
    "title": "JPA CRUD",
    "createdAt": "2026-09-16T19:30:00.123456"
  }
]
```

### 목록이 []로 나옴

현장 멘트:

“대괄호만 나온 건 서버 오류가 아니라 ‘현재 게시글 0개’라는 정상 200 응답입니다. POST 응답이 201이었는지, 앱 재시작으로 데이터가 사라졌는지, 같은 DB를 보고 있는지 확인할게요.”

확인 순서:

1. 상태가 200인지
2. POST 201을 실제로 받았는지
3. Hibernate INSERT 로그
4. `ddl-auto`가 아직 create인지
5. datasource URL/DB 이름
6. DB에서 `SELECT * FROM posts;`

## 4-2. 상세 조회

경로: `src/main/java/com/likelion/springsession/post/service/PostService.java`

추가:

```java
public PostDetailResponse getPost(Long postId) {
    Post post = findPostById(postId);
    return toDetailResponse(post);
}

private Post findPostById(Long postId) {
    return postRepository.findById(postId)
            .orElseThrow(() -> new PostNotFoundException(postId));
}
```

추가 import:

```java
import com.likelion.springsession.post.exception.PostNotFoundException;
```

예외 파일 생성:

경로: `src/main/java/com/likelion/springsession/post/exception/PostNotFoundException.java`

```java
package com.likelion.springsession.post.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class PostNotFoundException extends RuntimeException {

    public PostNotFoundException(Long postId) {
        super("게시글을 찾을 수 없습니다. id=" + postId);
    }
}
```

내 멘트:

“findById는 값이 없을 수 있어서 Optional<Post>를 반환합니다. 원문처럼 인자 없는 orElseThrow만 쓰면 없는 ID가 자동으로 404가 되지 않고 처리되지 않은 NoSuchElementException 때문에 500이 됩니다. 그래서 작은 예외 클래스로 404를 명시하겠습니다.”

Controller 추가:

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

“중괄호 postId와 매개변수 postId 이름을 맞춥니다. 클래스 공통 경로와 합쳐져 GET /api/posts/{postId}가 됩니다.”

Postman:

```http
GET http://localhost:8080/api/posts/1
```

정상 예시:

```text
200 OK
```

```json
{
  "id": 1,
  "title": "JPA CRUD",
  "content": "게시글을 생성해봅니다.",
  "createdAt": "2026-09-16T19:30:00.123456"
}
```

없는 ID:

```http
GET http://localhost:8080/api/posts/999999
```

예상: `404 Not Found`

## 빈칸 질문

```java
List<Post> posts = postRepository._____();
```

정답: `findAll`

```java
return postRepository._____(postId)
        .orElseThrow(...);
```

정답: `findById`

“목록 응답과 상세 응답의 가장 눈에 띄는 필드 차이는?”
정답: 상세에는 `content`가 있다.

## READ 오류 대응

### findById 타입 오류

“Post post = repository.findById라고 바로 쓰면 타입이 안 맞습니다. 오른쪽은 Optional<Post>이기 때문이에요. orElseThrow로 값이 있는 경우 Post를 꺼내고 없는 경우를 따로 처리합니다.”

### /api/posts/api/posts 중복

“Controller 클래스의 RequestMapping과 메서드의 GetMapping을 눈으로 더해봅니다. 클래스에 /api/posts가 있으면 목록 메서드는 @GetMapping만 남겨야 합니다.”

### 존재하지 않는 ID

“먼저 목록에서 실제 ID를 복사했는지 확인합니다. update 설정에서는 ID가 꼭 1부터 시작하지 않습니다. 실제로 없는 ID라면 이 코드에서는 404가 정상입니다.”

### 응답에 content가 없음

“목록 GET인지 상세 GET인지 URL부터 봅니다. 상세 Service가 PostSummaryResponse가 아니라 PostDetailResponse를 반환하는지, toDetailResponse에서 content를 넣었는지 확인합니다.”

### 400, 404, 500 구분 멘트

“400은 JSON 문법이나 Validation처럼 요청이 조건을 못 맞춘 경우부터 봅니다. 404는 URL 매핑이 없거나 요청한 게시글 자체가 없는 경우입니다. 500은 서버 안에서 처리하지 못한 예외라 콘솔 로그의 첫 번째 원인을 확인해야 합니다.”

확인 순서:

1. HTTP 상태
2. method와 URL
3. 요청 JSON/Content-Type
4. Controller 매핑
5. 실제 DB ID
6. 서버 콘솔의 첫 예외와 `Caused by`

## 실행 시점

상세 조회 코드를 저장한 뒤 애플리케이션을 재시작한다. 목록 → 실제 ID 확인 → 상세 → 없는 ID 순서로 요청한다.

## 다음 단계 연결 멘트

“전반부에서 새 Entity를 save로 저장했고, findAll과 findById로 읽었습니다. 이제 조회한 Entity의 값을 바꾸면 JPA가 어떻게 UPDATE를 만드는지가 다음 질문입니다.”

---

# 전반부 정리

예상 소요: 7분

## 화면

IntelliJ에서 다음 파일을 차례로 Ctrl+클릭해 짧게 훑는다.

1. `build.gradle`
2. `post/entity/Post.java`
3. `post/dto/PostCreateRequest.java`
4. `post/dto/PostSummaryResponse.java`
5. `post/dto/PostDetailResponse.java`
6. `post/service/PostService.java`
7. `post/controller/PostController.java`

## 내가 말할 문장

“오늘 전반부의 흐름은 JSON이 PostCreateRequest가 되고, Service가 Post를 만든 뒤 Repository.save로 저장하고, 다시 Response DTO로 바꿔 반환하는 것이었습니다.”

```text
POST JSON
→ PostCreateRequest
→ Post
→ postRepository.save()
→ PostDetailResponse
→ 201 Created
```

“조회에서는 findAll의 List<Post>를 목록 DTO들로 바꿨고, findById의 Optional은 없는 경우를 404로 처리했습니다.”

```text
GET /api/posts          → List<PostSummaryResponse>
GET /api/posts/{id}     → PostDetailResponse 또는 404
```

“Validation의 400과 DB 컬럼의 nullable=false는 같은 역할이 아닙니다. 전자는 HTTP 요청 입구에서 친절하게 막고, 후자는 DB 무결성의 마지막 제약입니다.”

## 최종 질문

1. “Entity에 전체 Setter를 붙이지 않은 이유는?”
   기대 답: 아무 곳에서나 상태를 바꾸지 않고 의미 있는 변경 메서드로 관리하기 위해서.
2. “findById가 Optional인 이유는?”
   기대 답: 해당 ID가 없을 가능성을 타입으로 표현하기 위해서.
3. “POST가 201을 반환하게 한 어노테이션은?”
   기대 답: `@ResponseStatus(HttpStatus.CREATED)`
4. “목록과 상세 DTO를 나눈 이유는?”
   기대 답: API 목적에 필요한 필드만 반환하고 Entity와 API 계약을 분리하기 위해서.
5. “Request DTO 검증을 실제로 실행시키는 Controller 어노테이션은?”
   기대 답: `@Valid`

---

# 후반부 강사에게 넘기는 연결 멘트

“여기까지는 새 Post를 save해서 영속화하고, findAll과 findById로 조회했습니다. 그런데 수정에서는 이미 조회한 Post의 제목과 본문을 바꾼 뒤 save를 다시 호출하지 않을 예정입니다.”

“어떻게 JPA가 값이 바뀐 걸 알고 UPDATE를 보낼까요? 그리고 조회부터 수정까지를 왜 하나의 작업으로 묶어야 할까요? 이 질문을 풀려면 영속성 컨텍스트, 스냅샷, flush와 commit, 그리고 @Transactional이 필요합니다.”

“이제 후반부에서 조회한 Entity가 영속 상태일 때 변경 감지가 어떻게 동작하는지 살펴보고, 그 원리로 PUT과 DELETE까지 완성하겠습니다. 선우 멘토님께 넘기겠습니다.”

---

# 강사용 비상 점검표

## Lombok

1. Gradle 의존성 위치
2. Gradle Reload
3. JDK/Gradle JVM 17
4. annotation processing
5. Lombok 플러그인
6. 실제 `gradlew.bat compileJava` 결과

## DB

1. MySQL 프로세스
2. URL과 3306
3. DB `likelion_blog`
4. 사용자 `likelion`
5. `DB_PASSWORD`
6. 계정 host 권한
7. `ddl-auto:update`
8. 남아 있는 `import.sql`과 기존 DB 행 구분

## API

1. 앱 Started 로그
2. method
3. 전체 URL
4. Content-Type
5. JSON 문법
6. Controller 경로 중복
7. `@RequestBody`와 `@Valid`
8. 실제 ID
9. DTO 종류와 필드
10. 서버 로그의 최초 원인

## 강의 직전 자동 확인

```powershell
.\gradlew.bat clean build
```

실제 MySQL:

```powershell
$env:DB_PASSWORD='실습 DB 비밀번호'
.\gradlew.bat bootRun
```

Postman 순서:

1. 잘못된 POST → 400
2. 정상 POST → 201, 생성된 ID 기록
3. 목록 GET → 200, content 없음
4. 상세 GET → 200, content 있음
5. 없는 ID GET → 404

후반부 담당자와 공유할 확인 사항:

- 생성된 실제 ID
- `ddl-auto:update` 적용 여부
- `Post.update()`, `PostUpdateRequest`는 후반부 시작 시 추가할지 최종 코드에서 미리 보여줄지
- PUT 200, DELETE 204, 없는 ID 404 규격
