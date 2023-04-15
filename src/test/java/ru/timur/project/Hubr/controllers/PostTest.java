package ru.timur.project.Hubr.controllers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import ru.timur.project.Hubr.models.User;

import java.util.Arrays;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.logout;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PostTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void postPageTest() throws Exception {
        this.mockMvc.perform(get("/posts/all"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(model().attribute("url", "/posts/all"))
                .andExpect(view().name("post/posts"));
    }

    @Test
    public void notExistPost() throws Exception {
        this.mockMvc.perform(get("/posts/{id}", 1).accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void existPost() throws Exception {
        this.mockMvc.perform(get("/posts/{id}", 53).accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("post/post"));
    }

    @Test
    public void anonymousRequestToPublication() throws Exception {
        this.mockMvc.perform(get("/posts/publication").with(anonymous()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/auth/login"));
    }

    @Test
    @WithMockUser()
    public void userRequestToPublication() throws Exception {
        this.mockMvc.perform(get("/posts/publication"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("post/publication"));
    }

    @Test
    public void anonymousRequestToFeed() throws Exception {
        this.mockMvc.perform(get("/posts/feed").with(anonymous()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/auth/login"));
    }

}