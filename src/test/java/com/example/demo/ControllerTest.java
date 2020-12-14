package com.example.demo;

import com.example.demo.ExceptionHandling.CustomException;
import com.example.demo.controllers.UserController;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.UserOrder;
import com.example.demo.model.persistence.repositories.CartRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import com.example.demo.model.requests.CreateUserRequest;
import com.example.demo.model.requests.ModifyCartRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;
import utils.TestingUtils;

import java.util.HashMap;
import java.util.Map;


import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ControllerTest {

    @Autowired
    private MockMvc mvc;

    UserController userController;

    CartRepository cartRepository=mock(CartRepository.class);

    UserRepository userRepository=mock(UserRepository.class);

    RestTemplate restTemplate=mock(RestTemplate.class);

    BCryptPasswordEncoder cryptPasswordEncode=mock(BCryptPasswordEncoder.class);

    @Before
    public void setup()
    {
        userController=new UserController();
        TestingUtils.setTarget(userController,"userRepository",userRepository);
        TestingUtils.setTarget(userController,"cartRepository",cartRepository);
        TestingUtils.setTarget(userController,"passwordEncoder",cryptPasswordEncode);

    }

    @Test
    public void verify_createUser() throws CustomException {
        CreateUserRequest createUserRequest=getUser();
        ResponseEntity<User> user =userController.createUser(createUserRequest);
        Assert.assertEquals(200,user.getStatusCodeValue());
        Assert.assertEquals(user.getBody().getPassword(),"Ghansham",user.getBody().getUsername());
    }

    @Test
    public void verify_createUserWithInvalidPassword() {
        CreateUserRequest createUserRequest=getUser();
        createUserRequest.setPassword("12335");
        createUserRequest.setConfirmPassword("12335");
        ResponseEntity<User> user = null;
        try {
            user = userController.createUser(createUserRequest);
        } catch (CustomException e) {
            Assert.assertEquals(e.getMessage(),"Password length should greater than or equal to 7");
        }

    }
    @Test
    public void verify_login() throws Exception
    {
        MvcResult createUser= mvc.perform(MockMvcRequestBuilders.post("/api/user/create").content(new ObjectMapper().writeValueAsString(getUser())).
                contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();
       MvcResult result= mvc.perform(MockMvcRequestBuilders.post("/login").content(new ObjectMapper().writeValueAsString(getUser())))
                .andExpect(status().isOk()).andReturn();
       Assert.assertNotNull(result.getResponse().getHeader("Authorization"));
    }

    @Test
    public  void verify_submitAddToCart() throws Exception
    {
        CreateUserRequest user=new CreateUserRequest("Test","1234567","1234567");
        MvcResult createUser= mvc.perform(MockMvcRequestBuilders.post("/api/user/create").content(new ObjectMapper().writeValueAsString(user)).
                contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();
        MvcResult loginResult= mvc.perform(MockMvcRequestBuilders.post("/login").content(new ObjectMapper().writeValueAsString(user)))
                .andExpect(status().isOk()).andReturn();
        MvcResult addToCartResult=mvc.perform(MockMvcRequestBuilders.post("/api/cart/addToCart")
                  .content(new ObjectMapper().writeValueAsString(getCart("Test")))
                  .accept(MediaType.APPLICATION_JSON)
                  .contentType(MediaType.APPLICATION_JSON)
                  .header("Authorization",loginResult.getResponse().getHeader("Authorization")))
                  .andExpect(status().isOk()).andReturn();
    }
    @Test
    public void verify_UnauthorizedAcess() throws Exception
    {
        CreateUserRequest user=new CreateUserRequest("Test2","1234567","1234567");
        MvcResult createUser= mvc.perform(MockMvcRequestBuilders.post("/api/user/create").content(new ObjectMapper().writeValueAsString(user)).
                contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();
        MvcResult loginResult= mvc.perform(MockMvcRequestBuilders.post("/login").content(new ObjectMapper().writeValueAsString(user)))
                .andExpect(status().isOk()).andReturn();
        MvcResult addToCartResult=mvc.perform(MockMvcRequestBuilders.post("/api/cart/addToCart")
                .content(new ObjectMapper().writeValueAsString(getCart("Test")))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))  //Without Authorization header
                .andExpect(status().is4xxClientError()).andReturn();
    }

    @Test
    public  void verify_submitOrderAndGetOrderHistory() throws Exception
    {
        CreateUserRequest user=new CreateUserRequest("Test1","1234567","1234567");
        MvcResult createUser= mvc.perform(MockMvcRequestBuilders.post("/api/user/create").content(new ObjectMapper().writeValueAsString(user)).
                contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        MvcResult loginResult= mvc.perform(MockMvcRequestBuilders.post("/login").content(new ObjectMapper().writeValueAsString(user)))
                .andExpect(status().isOk()).andReturn();
        MvcResult addToCartResult=mvc.perform(MockMvcRequestBuilders.post("/api/cart/addToCart")
                .content(new ObjectMapper().writeValueAsString(getCart("Test1")))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization",loginResult.getResponse().getHeader("Authorization")))
                .andExpect(status().isOk()).andReturn();
        MvcResult submitOrderResult=mvc.perform(MockMvcRequestBuilders.post("/api/order/submit/Test1")
                .header("Authorization",loginResult.getResponse().getHeader("Authorization")))
                .andExpect(status().isOk()).andReturn();
        ObjectMapper mapper=new ObjectMapper();
        UserOrder order=new ObjectMapper().readValue(submitOrderResult.getResponse().getContentAsString(),UserOrder.class);
        Assert.assertEquals(order.getUser().getUsername(),"Test1");
        Assert.assertNotNull(order.getItems());
        MvcResult orderHistory=mvc.perform(MockMvcRequestBuilders.get("/api/order/history/Test1")
                .header("Authorization",loginResult.getResponse().getHeader("Authorization")))
                .andExpect(status().isOk()).andReturn();
    }
    public static CreateUserRequest getUser()
    {
        CreateUserRequest createUserRequest=new CreateUserRequest();
        createUserRequest.setUsername("Ghansham");
        createUserRequest.setPassword("Ghate12345");
        createUserRequest.setConfirmPassword("Ghate12345");
        return createUserRequest;
    }

    public static ModifyCartRequest getCart(String username)
    {
        ModifyCartRequest cartRequest=new ModifyCartRequest();
        cartRequest.setItemId(1);
        cartRequest.setUsername(username);
        cartRequest.setQuantity(1);
        return cartRequest;
    }

}
