package kr.java.springai_mcp;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * GitHub MCP Server를 호출하는 서비스
 */
@Service
@RequiredArgsConstructor
public class GithubMcpSearchService {

    private final List<McpSyncClient> mcpClients;
    private final AiModelRouter modelRouter;

    /**
     * (기존) 사용자가 준 키워드로 바로 검색 + 요약
     */
    public String searchRepository(String keyword) {

        McpSchema.CallToolRequest request = McpSchema.CallToolRequest.builder()
                .name("search_repositories")
                .arguments(Map.of("query", keyword, "limit", 5))
                .build();
        String rawResult = mcpClients.get(0).callTool(request).toString();

        return modelRouter.summarize(
                "다음 GitHub 레포 검색 결과를 간단히 요약해줘:\n" + rawResult
        );
    }

    /**
     * (추가) callForAction()으로 'GitHub 검색용 쿼리'를 먼저 만들고,
     * 그 쿼리로 MCP tool 호출 → 결과 요약(summarize)
     */
    public String searchRepositoryWithAction(String userInput) {

        // 1) OpenAI-style 모델로 '행동 지시용' 검색 쿼리 생성 (짧고 결정적으로)
        //    - 출력은 반드시 "한 줄"의 GitHub 검색 query 문자열만 나오게 강제
        String actionPrompt = """
            너는 GitHub repository 검색 쿼리 생성기다.
            사용자의 입력을 GitHub 검색 API(q) 스타일로 정규화해서 '쿼리 문자열 1줄'만 출력해라.
            조건:
            - 불필요한 설명 금지
            - 따옴표/코드블록 금지
            - 출력은 오직 한 줄 텍스트
            - 가능하면 in:name,description 을 포함
            - 가능하면 stars:>50 을 포함(너무 마이너해 보이면 제외 가능)

            사용자 입력: %s
            """.formatted(userInput);

        String normalizedQuery = modelRouter.callForAction(actionPrompt).trim();

        // 2) MCP Tool 호출 (GitHub MCP Server가 제공하는 search tool)
        McpSchema.CallToolRequest request = McpSchema.CallToolRequest.builder()
                .name("search_repositories")
                .arguments(Map.of("query", normalizedQuery, "limit", 5))
                .build();
        String rawResult = mcpClients.get(0).callTool(request).toString();

        // 3) Gemini로 보기 좋게 요약
        String summaryPrompt = """
            다음은 GitHub repository 검색 결과(raw)이다.
            아래 형식으로 5개 이내로 요약해라.

            형식:
            - repoFullName: (한 줄 설명) | stars: N | url: ...
            마지막 줄에 "추천 1개"를 이유 1문장으로 추가

            raw:
            %s
            """.formatted(rawResult);

        return modelRouter.summarize(summaryPrompt);
    }
}
