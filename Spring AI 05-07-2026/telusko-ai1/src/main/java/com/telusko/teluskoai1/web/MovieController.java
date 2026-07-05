package com.telusko.teluskoai1.web;

import com.telusko.teluskoai1.model.Movie;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.converter.ListOutputConverter;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
public class MovieController
{
    private ChatClient chatClient;
    public MovieController(OpenAiChatModel chatModel)
    {
        this.chatClient=ChatClient.create(chatModel);
    }
    @GetMapping("/get-movies")
    public List<String> getMovies(@RequestParam String name)
    {
        List<String> movies = chatClient.prompt()
                .user(u -> u.text("List top 5 movies {name}")
                        .param("name", name))
                .call()
                .entity(new ListOutputConverter(new DefaultConversionService()));
         return movies;
    }
   @GetMapping("/get-movie-details")
    public Movie getMovieInfo(@RequestParam String name)
    {
        Movie movie = chatClient.prompt()
                .user(u -> u.text("Give me details of the movie {name}")
                        .param("name", name))
                .call()
                .entity(new BeanOutputConverter<Movie>(Movie.class));
            return movie;
    }
    @GetMapping("/get-movies-list")
    public List<Movie> getMoviesList(@RequestParam String name) {
        List<Movie> moviesList = chatClient.prompt()
                .user(u -> u.text("Give me top 5 movies of {name}")
                        .param("name", name))
                .call()
                .entity(new BeanOutputConverter<List<Movie>>(new ParameterizedTypeReference<List<Movie>>(

                ) {
                }));
        return moviesList;
    }          
    


}
