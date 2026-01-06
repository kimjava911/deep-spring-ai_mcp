# Spring AI MCP - GitHub Repository Search Demo

Spring AI와 MCP(Model Context Protocol)를 활용한 GitHub 레포지토리 검색 데모 프로젝트입니다.

## 기술 스택

- **Backend**: Spring Boot 3.5, Spring AI 1.1.2
- **LLM**: Google Gemini, OpenAI (Groq 호환)
- **MCP**: Model Context Protocol (Client & Server)
- **Frontend**: Thymeleaf, Bootstrap 5

## 아키텍처 개요

```
┌─────────────────────────────────────────────────────────────────┐
│                        Spring Boot App                          │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────────────┐ │
│  │   Direct    │    │   Action    │    │    ToolCalling      │ │
│  │   Search    │    │   Search    │    │      (Agent)        │ │
│  └──────┬──────┘    └──────┬──────┘    └──────────┬──────────┘ │
│         │                  │                      │             │
│         │           ┌──────▼──────┐        ┌──────▼──────┐     │
│         │           │   OpenAI    │        │  ChatClient │     │
│         │           │ (callForAction)      │  + @Tool    │     │
│         │           └──────┬──────┘        └──────┬──────┘     │
│         │                  │                      │             │
│         ▼                  ▼                      ▼             │
│  ┌─────────────────────────────────────────────────────────────┐│
│  │                    MCP Client (STDIO)                       ││
│  │              @modelcontextprotocol/server-github            ││
│  └─────────────────────────────────────────────────────────────┘│
│         │                  │                      │             │
│         ▼                  ▼                      ▼             │
│  ┌─────────────────────────────────────────────────────────────┐│
│  │                      GitHub API                             ││
│  └─────────────────────────────────────────────────────────────┘│
│         │                  │                                    │
│         ▼                  ▼                                    │
│  ┌─────────────┐    ┌─────────────┐                            │
│  │   Gemini    │    │   Gemini    │       결과 요약             │
│  │ (summarize) │    │ (summarize) │                            │
│  └─────────────┘    └─────────────┘                            │
│                                                                 │
├─────────────────────────────────────────────────────────────────┤
│                     MCP Server (SSE)                            │
│              외부 클라이언트용 Tool 노출                          │
└─────────────────────────────────────────────────────────────────┘
```

## 사전 준비

### 1. API 키 발급

| 서비스 | 용도 | 발급 링크 |
|--------|------|----------|
| GitHub Personal Access Token | API 호출 | [github.com/settings/tokens](https://github.com/settings/personal-access-tokens) |
| Google AI Studio | Gemini 모델 | [aistudio.google.com](https://aistudio.google.com/) |
| Groq | OpenAI 호환 API | [groq.com](https://groq.com/) |

### 2. 환경 설정

```bash
# 설정 파일 복사
cp src/main/resources/application-dev-sample.yml src/main/resources/application-dev.yml
```

`application-dev.yml` 수정:

```yaml
spring:
  ai:
    openai:
      api-key: {GROQ_API_KEY}
    google:
      genai:
        api-key: {GEMINI_API_KEY}
    mcp:
      client:
        stdio:
          connections:
            github:
              env:
                GITHUB_TOKEN: {GITHUB_TOKEN}

github:
  token: {GITHUB_TOKEN}
```

### 3. Node.js 설치 확인

MCP Client가 `npx`를 사용하므로 Node.js가 필요합니다.

```bash
node --version  # v18 이상 권장
npx --version
```

---

## 시연 플로우

### 시연 1: Direct Search (MCP 직접 호출)

사용자 입력을 그대로 MCP Tool에 전달하는 가장 단순한 방식입니다.

#### 흐름

```
사용자 입력 → MCP Tool(search_repositories) → Gemini(요약) → 결과 출력
```

#### Step 1: 애플리케이션 실행

```bash
# dev 프로파일로 실행 (MCP Client 활성화)
./gradlew bootRun --args='--spring.profiles.active=dev'
```

#### Step 2: 검색 화면 접속

브라우저에서 `http://localhost:8080` 또는 `http://localhost:8080/github` 접속

#### Step 3: 검색 실행

1. 검색어 입력: `spring ai mcp`
2. **Direct Search** 버튼 클릭
3. 결과 확인:
    - MCP가 GitHub API를 호출하여 레포지토리 검색
    - Gemini가 결과를 요약하여 출력

#### 예상 결과

```
- spring-projects/spring-ai: Spring AI 프로젝트 | stars: 3.2k | url: ...
- anthropics/courses: MCP 관련 예제 | stars: 1.5k | url: ...
...

추천 1개: spring-projects/spring-ai - 공식 Spring AI 프로젝트로 MCP 지원이 내장되어 있음
```

---

### 시연 2: Action Search (LLM 기반 쿼리 최적화)

사용자의 자연어 입력을 LLM이 GitHub 검색에 최적화된 쿼리로 변환합니다.

#### 흐름

```
사용자 입력 → OpenAI(쿼리 정규화) → MCP Tool → Gemini(요약) → 결과 출력
```

#### Step 1: Direct Search 수행 후 Action Search 클릭

1. 검색어 입력: `자바로 만든 채팅 서버 오픈소스`
2. **Direct Search** 버튼으로 먼저 검색 (비교용)
3. **Action Search (callForAction)** 버튼 클릭

#### Step 2: 결과 비교

| 방식 | 실제 GitHub 쿼리 | 결과 품질 |
|------|-----------------|----------|
| Direct | `자바로 만든 채팅 서버 오픈소스` | 한글 검색으로 결과 적음 |
| Action | `java chat server in:name,description stars:>50` | 최적화된 영문 쿼리로 정확한 결과 |

#### 내부 동작 확인 (로그)

```
DEBUG o.s.ai.mcp - Normalized query: java chat server in:name,description stars:>50
DEBUG o.s.ai.mcp - MCP Tool called: search_repositories
```

---

### 시연 3: ToolCalling (ChatClient Agent 방식)

ChatClient가 자동으로 Tool 호출 여부를 판단하는 Agent 패턴입니다.

#### 흐름

```
사용자 질문 → ChatClient → (필요시) @Tool 자동 호출 → 응답 생성
```

#### Step 1: Agent 화면 접속

브라우저에서 `http://localhost:8080/github2` 접속

#### Step 2: 자연어 질문

1. 입력: `spring ai mcp 관련 레포지토리 찾아줘`
2. **실행** 버튼 클릭

#### Step 3: Tool 호출 과정 관찰

ChatClient가 프롬프트를 분석하여 `search_github_repositories` Tool을 자동 호출합니다.

```java
// GitHubAgentController.java
String answer = chatClient.prompt()
    .user("""
        GitHub에서 레포 검색이 필요하면
        반드시 search_github_repositories 도구를 호출해서 답해라.
        질문: %s
        """.formatted(q))
    .call()
    .content();
```

#### Step 4: 다양한 질문 테스트

| 질문 | Tool 호출 여부 |
|------|---------------|
| `spring ai mcp 레포 검색해줘` | O |
| `GitHub에서 인기 있는 Java 프로젝트는?` | O |
| `오늘 날씨 어때?` | X (Tool 불필요) |

---

### 시연 4: MCP Server 엔드포인트 확인

이 애플리케이션은 MCP Server로도 동작하여 외부 클라이언트가 Tool을 호출할 수 있습니다.

#### SSE 엔드포인트

```
GET /sse
```

#### Message 엔드포인트

```
POST /mcp/message
```

#### 테스트 (curl)

```bash
# SSE 연결 테스트
curl -N http://localhost:8080/sse

# Tool 목록 확인 (MCP Inspector 등 사용)
```

---

## 주요 엔드포인트 정리

### 페이지 라우팅

| URL | 설명 | 프로파일 |
|-----|------|---------|
| `/` | 메인 (리다이렉트) | dev |
| `/github` | MCP 검색 화면 | dev |
| `/github2` | ToolCalling 화면 | 전체 |

### REST API

| Method | URL | 설명 | 프로파일 |
|--------|-----|------|---------|
| GET | `/github/search?q=` | Direct 검색 (JSON) | dev |
| GET | `/github/search/action?q=` | Action 검색 (JSON) | dev |
| GET | `/github/search/view?q=` | Direct 검색 (HTML) | dev |
| GET | `/github/search/action/view?q=` | Action 검색 (HTML) | dev |
| GET | `/github2/agent?q=` | ToolCalling 검색 | 전체 |

### MCP Server

| URL | 설명 |
|-----|------|
| `/sse` | MCP SSE 엔드포인트 |
| `/mcp/message` | MCP 메시지 엔드포인트 |

---

## 프로파일별 동작 차이

| 기능 | dev | prod |
|------|-----|------|
| MCP Client (GitHub) | ✅ | ❌ |
| MCP Server (SSE) | ✅ | ✅ |
| Direct/Action Search | ✅ | ❌ |
| ToolCalling (/github2) | ✅ | ✅ |

```bash
# dev 모드 (전체 기능)
./gradlew bootRun --args='--spring.profiles.active=dev'

# prod 모드 (MCP Server + ToolCalling만)
./gradlew bootRun
```

---

## 핵심 코드 설명

### AiModelRouter - 모델 역할 분리

```java
@Component
public class AiModelRouter {
    
    // 짧은 응답, 도구 호출용 (빠른 모델)
    public String callForAction(String prompt) {
        return openAiChatModel.call(prompt);
    }
    
    // 긴 텍스트 요약용 (품질 우선)
    public String summarize(String prompt) {
        return geminiChatModel.call(prompt);
    }
}
```

### GitHubSearchTool - 공용 Tool 정의

```java
@Component
public class GitHubSearchTool {

    @Tool(
        name = "search_github_repositories",
        description = "GitHub repository를 검색한다"
    )
    public String search(String query, Integer limit) {
        return githubApiService.search(query, size);
    }
}
```

이 Tool은 ChatClient의 ToolCalling과 MCP Server 양쪽에서 사용됩니다.

---

## 트러블슈팅

### MCP Client 연결 실패

```
Error: Cannot find module '@modelcontextprotocol/server-github'
```

→ Node.js 설치 확인 및 네트워크 연결 확인

### GitHub API Rate Limit

```
403 Forbidden: API rate limit exceeded
```

→ GitHub Personal Access Token 설정 확인

### Gemini API 오류

```
400 Bad Request: API key not valid
```

→ `application-dev.yml`의 `google.genai.api-key` 확인