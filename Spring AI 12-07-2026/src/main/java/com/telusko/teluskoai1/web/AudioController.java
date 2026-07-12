package com.telusko.teluskoai1.web;

import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.tts.TextToSpeechPrompt;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiAudioSpeechModel;
import org.springframework.ai.openai.OpenAiAudioSpeechOptions;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/audio")
public class AudioController
{
    private OpenAiAudioTranscriptionModel audioModel;
    private OpenAiAudioSpeechModel speechModel;
    private final ChatClient chatClient;
    public AudioController(OpenAiAudioTranscriptionModel audioModel, OpenAiAudioSpeechModel speechModel
    , ChatClient.Builder builder)
    {
        this.audioModel=audioModel;
        this.speechModel=speechModel;
        this.chatClient=builder.build();
    }
    @GetMapping("/speech-to-text")
    public String speechToText(@RequestParam MultipartFile file)
    {
        AudioTranscriptionPrompt audioResource = new AudioTranscriptionPrompt(file.getResource());
        String response= audioModel.call(audioResource)
                .getResult()
                .getOutput();
        return chatClient.prompt()
                .user("Translate the following to Kannada"+ response)
                .call()
                .content();
    }
    @PostMapping("/text-to-speech")
    public byte[] textToSpeech(@RequestParam String prompt)
    {
        OpenAiAudioSpeechOptions options = OpenAiAudioSpeechOptions
                .builder()
                .speed(1.5)
                .voice(OpenAiAudioApi.SpeechRequest.Voice.FABLE)
                .build();
        TextToSpeechPrompt finalPrompt = new TextToSpeechPrompt(prompt, options);
        return speechModel.call(finalPrompt)
                .getResult()
                .getOutput();
    }
}
