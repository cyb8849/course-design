package com.lotus.controller;

import com.lotus.common.ResultVO;
import com.lotus.dto.LoginDTO;
import com.lotus.dto.RegisterDTO;
import com.lotus.dto.UserDTO;
import com.lotus.entity.SysUser;
import com.lotus.service.UserService;
import com.lotus.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 * 处理用户登录、注册、Token验证等认证相关请求
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtUtils jwtUtils;

    /**
     * 用户登录
     * @param loginDTO 登录信息（用户名、密码）
     * @return 登录结果，包含Token和用户信息
     */
    @PostMapping("/login")
    public ResultVO<Map<String, Object>> login(@RequestBody LoginDTO loginDTO) {
        log.info("用户登录: username={}, role={}", loginDTO.getUsername(), loginDTO.getRole());
        
        SysUser user = userService.login(loginDTO.getUsername(), loginDTO.getPassword());
        if (user == null) {
            log.warn("登录失败: 用户名或密码错误, username={}", loginDTO.getUsername());
            return ResultVO.error(401, "用户名或密码错误");
        }
        
        if (user.getStatus() == 0) {
            log.warn("登录失败: 用户已被禁用, username={}", loginDTO.getUsername());
            return ResultVO.error(401, "用户已被禁用");
        }
        
        // 验证角色是否匹配
        if (loginDTO.getRole() != null && !loginDTO.getRole().equals(user.getRole())) {
            log.warn("登录失败: 角色不匹配, username={}, expectedRole={}, actualRole={}", 
                    loginDTO.getUsername(), loginDTO.getRole(), user.getRole());
            return ResultVO.error(401, "角色不匹配");
        }
        
        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole());
        
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", convertToDTO(user));
        
        log.info("用户登录成功: username={}, role={}", user.getUsername(), user.getRole());
        return ResultVO.success("登录成功", data);
    }

    /**
     * 用户注册
     * @param registerDTO 注册信息
     * @return 注册结果
     */
    @PostMapping("/register")
    public ResultVO<Void> register(@RequestBody RegisterDTO registerDTO) {
        log.info("用户注册: username={}, role={}", registerDTO.getUsername(), registerDTO.getRole());
        
        // 检查用户名是否存在
        SysUser existUser = userService.getByUsername(registerDTO.getUsername());
        if (existUser != null) {
            log.warn("注册失败: 用户名已存在, username={}", registerDTO.getUsername());
            return ResultVO.error("用户名已存在");
        }
        
        // 创建新用户
        SysUser user = new SysUser();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(registerDTO.getPassword());
        user.setName(registerDTO.getName());
        user.setPhone(registerDTO.getPhone());
        user.setRole(registerDTO.getRole() != null ? registerDTO.getRole() : "CUSTOMER");
        user.setStatus(1);
        
        userService.register(user);
        
        log.info("用户注册成功: username={}", user.getUsername());
        return ResultVO.success("注册成功");
    }

    /**
     * 获取当前用户信息
     * @param token Authorization Token
     * @return 当前用户信息
     */
    @GetMapping("/currentUser")
    @PreAuthorize("isAuthenticated()")
    public ResultVO<UserDTO> getCurrentUser(@RequestHeader("Authorization") String token) {
        try {
            String jwt = token.substring(7);
            Claims claims = jwtUtils.parseToken(jwt);
            Long userId = claims.get("userId", Long.class);
            
            SysUser user = userService.getById(userId);
            if (user == null) {
                return ResultVO.notFound();
            }
            
            return ResultVO.success(convertToDTO(user));
        } catch (Exception e) {
            log.error("获取当前用户信息失败: ", e);
            return ResultVO.unauthorized();
        }
    }

    /**
     * 验证Token是否有效
     * @param token Authorization Token
     * @return 验证结果
     */
    @GetMapping("/verify")
    public ResultVO<Map<String, Boolean>> verifyToken(@RequestHeader("Authorization") String token) {
        try {
            String jwt = token.substring(7);
            jwtUtils.parseToken(jwt);
            
            Map<String, Boolean> result = new HashMap<>();
            result.put("valid", true);
            return ResultVO.success(result);
        } catch (Exception e) {
            Map<String, Boolean> result = new HashMap<>();
            result.put("valid", false);
            return ResultVO.success(result);
        }
    }

    /**
     * 实体转DTO
     */
    private UserDTO convertToDTO(SysUser user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setName(user.getName());
        dto.setPhone(user.getPhone());
        dto.setRole(user.getRole());
        dto.setStatus(user.getStatus());
        dto.setCreateTime(user.getCreateTime());
        dto.setUpdateTime(user.getUpdateTime());
        return dto;
    }
}