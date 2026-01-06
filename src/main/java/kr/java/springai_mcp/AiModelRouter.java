package kr.java.springai_mcp;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Component;

/**
 * 요청 성격에 따라 LLM을 선택하는 라우터
 */

@Component
@RequiredArgsConstructor
public class AiModelRouter {

    private final OpenAiChatModel openAiChatModel;
    private final GoogleGenAiChatModel geminiChatModel;

        /**
         * 도구 호출 중심 (짧은 응답)
         * - “검색 쿼리 생성/정규화” 같은 액션 지시를 만들 때 사용
         */
        public String callForAction(String prompt) {
            return openAiChatModel.call(prompt);
        }

        /**
         * 결과 요약/정리
         */
        public String summarize(String prompt) {
            return geminiChatModel.call(prompt);
        }
    }
