package com.dtcoder.hello.controller;

import com.dtcoder.hello.service.HelloService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * {@link HelloController} 单元测试。
 *
 * @author dtcoder
 */
@WebMvcTest(HelloController.class)
@DisplayName("HelloController 单元测试")
class HelloControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HelloService helloService;

    @Test
    @DisplayName("should return 200 with default greeting when no name param")
    void shouldReturn200WithDefaultGreetingWhenNoNameParam() throws Exception {
        // Arrange
        when(helloService.greet(null)).thenReturn("Hello, World!");

        // Act & Assert
        mockMvc.perform(get("/api/hello"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello, World!"));
    }

    @Test
    @DisplayName("should return 200 with personalized greeting when name param provided")
    void shouldReturn200WithPersonalizedGreetingWhenNameParamProvided() throws Exception {
        // Arrange
        String name = "DTCoder";
        when(helloService.greet(name)).thenReturn("Hello, DTCoder!");

        // Act & Assert
        mockMvc.perform(get("/api/hello").param("name", name))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello, DTCoder!"));
    }

    @Test
    @DisplayName("should return 200 with empty name param treated as blank")
    void shouldReturn200WithEmptyNameParamTreatedAsBlank() throws Exception {
        // Arrange
        when(helloService.greet("")).thenReturn("Hello, World!");

        // Act & Assert
        mockMvc.perform(get("/api/hello").param("name", ""))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello, World!"));
    }
}