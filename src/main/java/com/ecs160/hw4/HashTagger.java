package com.ecs160.hw4;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.ollama4j.OllamaAPI;
import io.github.ollama4j.models.response.OllamaResult;
import io.github.ollama4j.utils.Options;

public class HashTagger {
    private static final String modelName = "llama3.2";
    private static final String promptPrefix = "Please generate a hashtag for this social media post: ";
    private final OllamaAPI api = new OllamaAPI("http://localhost:11434");

    public String getOllamaResposne(String postContent) {
        Map<String, Object> opts = new HashMap<>();
        // Set a temperature parameter (for randomness in the output)
        opts.put("temperature", 0.7);
        // Set max tokens (limit the length of output)
        opts.put("max_tokens", 100);
        // Optionally set top_p if supported (nucleus sampling probability)
        opts.put("top_p", 0.9);
        Options options = new Options(opts);
        OllamaResult result = null;
        String hashTag = "";
        try {
            result = api.generate(modelName, promptPrefix + postContent, false, options);
            if (result != null) {
                System.out.println("Model result: " + result.getResponse());
                hashTag = extractHashtag(result.getResponse());
            }
            System.out.println("Extracted hashTag : " + hashTag);
            if (hashTag.isEmpty()) {
                hashTag = generateDefaultHashtag(postContent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hashTag;
    }

    public static String extractHashtag(String modelOutput) {
        // This regex looks for a hashtag (a '#' followed by word characters) inside quotes
        //Pattern pattern = Pattern.compile("\"(#[A-Za-z0-9]+)\"");
        //Pattern pattern = Pattern.compile("#[^\\s\"]+");
        //Pattern pattern = Pattern.compile("#\\S+");
        //Matcher matcher = pattern.matcher(modelOutput);
        Pattern pattern = Pattern.compile("(#\\S+)");
        Matcher matcher = pattern.matcher(modelOutput);
        if (matcher.find()) {
            return matcher.group(1); // This returns only the hashtag, e.g., "#ATProtoFeed"
        }
        return ""; // or you can return a default value
    }

    private String generateDefaultHashtag(String content) {
        // Example rule: if content mentions "security", tag as #security; otherwise, default.
        if (content.toLowerCase().contains("security")) {
            return "#security";
        } else if (content.toLowerCase().contains("vacation")) {
            return "#vacation";
        } else if (content.toLowerCase().contains("happy")) {
            return "#happy";
        }
        // sophisticated logic or LLM integration here.
        return "#bskypost";
    }

    public static void main(String[] args) {
        String output = "Here is a possible hashtag for your social media post:\n\n\"#ATProtoFeed\"\n\nAlternatively, you could also use \"#ATProtopage\" or \"#ATProtostatus\" depending on the context of your post. Let me know if you'd like me to suggest more options!\nHere is a possible hashtag for your social media post:\n\n\"#ATProtoFeed\"";
        String hashtag = extractHashtag(output);
        System.out.println("Extracted Hashtag: " + hashtag);
    }
}
