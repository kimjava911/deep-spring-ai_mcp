package kr.java.springai_mcp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class GitHubAgentController {

    private final ChatClient chatClient;

    @GetMapping("/github2")
    public String page() {
        return "search2";
    }

    @GetMapping("/github2/agent")
    public String agent(@RequestParam String q, Model model) {

        String answer = chatClient.prompt()
                .user("""
                    GitHub에서 레포 검색이 필요하면
                    반드시 search_github_repositories 도구를 호출해서 답해라.
                    질문: %s
                    """.formatted(q))
                .call()
                .content();

        model.addAttribute("query", q);
        model.addAttribute("answer", answer);
        return "search2";
    }
}

