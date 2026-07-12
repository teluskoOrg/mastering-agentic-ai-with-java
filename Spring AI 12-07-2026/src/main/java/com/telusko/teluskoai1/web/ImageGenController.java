package com.telusko.teluskoai1.web;

import org.springframework.ai.chat.client.ChatClient;

import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.openai.OpenAiImageModel;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

import static javax.swing.Spring.width;

@RestController
@RequestMapping("/image")
public class ImageGenController
{
    private final ChatClient chatClient;
    private final OpenAiImageModel imageModel;
    public ImageGenController(OpenAiImageModel imageModel, ChatClient.Builder builder)
    {
        this.chatClient = builder.build();
        this.imageModel=imageModel;
    }
    @GetMapping("/generate-image/{prompt}")
    public String generateImage(@PathVariable String prompt)throws Exception
    {
        ImagePrompt imagePrompt = new ImagePrompt(prompt,
                OpenAiImageOptions.builder()
                        .quality("high")
                        .height(1024)
                        .width(1024)
                        .build());
        ImageResponse imageResponse = imageModel.call(imagePrompt);

//        String url=imageResponse.getResult().getOutput().getUrl();
//        System.out.println(url);
        String base64Json=imageResponse.getResult().getOutput().getB64Json();
        byte[] bytesImage = Base64.getDecoder().decode(base64Json);
        Files.write(Paths.get("ai-generated-image.png"),bytesImage);
        return "Image is generated successfully";
    }
    @GetMapping(value = "/api/image/{prompt}", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> generateImage2(@PathVariable String prompt) {

        ImagePrompt imagePrompt = new ImagePrompt(
                prompt,
                OpenAiImageOptions.builder()
                        .quality("high")
                        .width(1024)
                        .height(1024)
                        .build());

        ImageResponse response = imageModel.call(imagePrompt);

        String b64 = response.getResult().getOutput().getB64Json();

        byte[] imageBytes = Base64.getDecoder().decode(b64);

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(imageBytes);
    }

    @PostMapping("/describe-image")
    public String DescribeImage(@RequestParam String prompt,
                                @RequestParam MultipartFile file)
    {
        return chatClient.prompt()
                .user(us -> us.text(prompt)
                        .media(MimeTypeUtils.IMAGE_JPEG, file.getResource()))
                .
                call()
                .content();
    }
}
