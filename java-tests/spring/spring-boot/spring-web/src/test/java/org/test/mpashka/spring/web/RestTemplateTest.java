package org.test.mpashka.spring.web;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureMockRestServiceServer;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@AutoConfigureMockRestServiceServer
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RestTemplateTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    @LocalServerPort
    private int port;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    @Qualifier("web-client")
    private RestTemplate restTemplate;

//    @Autowired
//    private MockRestServiceServer mockRestServiceServer;

    @Test
    public void testPostData() throws Exception {
        mockMvc.perform(get("/testPostData")
                        .content(objectMapper.writeValueAsString(HelloController.MyPostData.builder()
                                .aaa("My-string")
                                .bbb(10)
                                .build()))
                        .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(status().is2xxSuccessful())
                .andExpect(content().string("Ok"));
    }

    @Test
    public void testRestClient() throws Exception {
        MockRestServiceServer mockRestServiceServer = MockRestServiceServer.bindTo(restTemplate).build();
        mockRestServiceServer.expect(requestTo("https://www.google.com"))
                .andRespond(withSuccess("G-resp", MediaType.APPLICATION_JSON));

        mockMvc.perform(get("/testRestClient"))
                .andExpect(status().is2xxSuccessful());
    }
}
