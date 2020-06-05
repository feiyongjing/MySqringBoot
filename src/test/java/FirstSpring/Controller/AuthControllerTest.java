package FirstSpring.Controller;

import FirstSpring.Service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MockMvcBuilder;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.servlet.http.HttpSession;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.*;;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SpringExtension.class)
class AuthControllerTest {
    private MockMvc mockMvc;
    @Mock
    private UserService userService;
    @Mock
    private AuthenticationManager authenticationManager;

    private BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(userService, authenticationManager)).build();
    }

    @Test
    void retultNotLonginByDefault() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/auth")).andExpect(status().isOk()).andExpect(mvcResult -> {
//                System.out.println(mvcResult.getResponse().getContentAsString(UTF_8));
            Assertions.assertTrue(mvcResult.getResponse().getContentAsString(UTF_8).contains("用户没有登录"));
        });
    }

    @Test
    void testLongin() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/auth")).andExpect(status().isOk()).andExpect(mvcResult -> {
            Assertions.assertTrue(mvcResult.getResponse().getContentAsString(UTF_8).contains("用户没有登录"));
        });
        Map<String, String> usernamePassword = new HashMap<>();
        usernamePassword.put("username", "MyUser");
        usernamePassword.put("password", "MyPassword");


        Mockito.when(userService.loadUserByUsername("MyUser")).thenReturn(new User("MyUser", bCryptPasswordEncoder.encode("MyPassword"), Collections.emptyList()));
        Mockito.when(userService.getUserByUsername("MyUser")).thenReturn(new FirstSpring.entity.User(123, "MyUser", bCryptPasswordEncoder.encode("MyPassword")));
        MvcResult mvcResult1 = mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(usernamePassword)))
                .andExpect(status().isOk())
                .andExpect(new ResultMatcher() {
                    @Override
                    public void match(MvcResult mvcResult) throws Exception {
                        Assertions.assertTrue(mvcResult.getResponse().getContentAsString(UTF_8).contains("登录成功"));
                    }
                })
                .andReturn();
        HttpSession session = mvcResult1.getRequest().getSession();
        mockMvc.perform(MockMvcRequestBuilders.get("/auth").session((MockHttpSession) session)).andExpect(status().isOk())
                .andExpect(mvcResult -> {
                    Assertions.assertTrue(mvcResult.getResponse().getContentAsString(UTF_8).contains("MyUser"));
                });
    }
}