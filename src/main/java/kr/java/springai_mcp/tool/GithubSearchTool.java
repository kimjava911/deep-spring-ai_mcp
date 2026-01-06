package kr.java.springai_mcp.tool;

import kr.java.springai_mcp.service.GithubApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GithubSearchTool {

    private final GithubApiService githubApiService;

    /**
     * 외부 MCP Client & 내부 ToolCalling에서 공통 사용
     */
    @Tool(
            name = "search_github_repositories",
            description = "GitHub repository를 검색한다"
    )
    public String search(String query, Integer limit) {
        int size = (limit == null) ? 5 : Math.min(limit, 20);
        return githubApiService.search(query, size);
    }
}