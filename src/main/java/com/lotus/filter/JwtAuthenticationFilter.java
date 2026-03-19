package com.lotus.filter;

import com.lotus.common.ResultVO;
import com.lotus.dto.LoginDTO;
import com.lotus.entity.SysUser;
import com.lotus.service.UserService;
import com.lotus.utils.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 认证过滤器
 * 处理用户登录认证，成功后返回 JWT 令牌
 */
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final UserService userService;
    private final JwtUtils jwtUtils;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private AuthenticationManager authenticationManager;
    
    @Autowired
   public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    {      setFilterProcessesUrl("/auth/login");
    }
    
    @Override
 public void afterPropertiesSet() {
      // 跳过父类的 authenticationManager 检查，因为 Spring 会在之后注入
       // 不调用 super.afterPropertiesSet()
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) 
            throws AuthenticationException {
        try {
            LoginDTO loginDTO = objectMapper.readValue(request.getInputStream(), LoginDTO.class);
            
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    loginDTO.getUsername(),
                    loginDTO.getPassword()
            );
            
            return authenticationManager.authenticate(authenticationToken);
        } catch (IOException e) {
            log.error("登录请求解析失败: ", e);
            throw new RuntimeException("登录请求解析失败", e);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain chain, Authentication authResult) throws IOException, ServletException {
        String username = authResult.getName();
        SysUser user = userService.getByUsername(username);
        
        if (user == null) {
            renderJson(response, ResultVO.error("用户不存在"));
            return;
        }
        
        if (user.getStatus() == 0) {
            renderJson(response, ResultVO.error("用户已被禁用"));
            return;
        }
        
        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole());
        
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", user);
        
        log.info("用户登录成功: username={}, role={}", username, user.getRole());
        renderJson(response, ResultVO.success("登录成功", data));
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                              AuthenticationException exception) throws IOException, ServletException {
        log.warn("登录失败: {}", exception.getMessage());
        renderJson(response, ResultVO.error(401, "用户名或密码错误"));
    }

    private void renderJson(HttpServletResponse response, ResultVO<?> resultVO) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(resultVO));
    }
}