package com.autoparts.aop;

import com.autoparts.annotation.LogOperation;
import com.autoparts.model.User;
import com.autoparts.model.OperationLog;
import com.autoparts.repository.OperationLogMapper;
import com.autoparts.repository.UserMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 操作日志 AOP 切面
 * 自动记录标注了 @LogOperation 注解的方法调用
 */
@Aspect
@Component
public class OperationLogAspect {

    private static final Logger log = LoggerFactory.getLogger(OperationLogAspect.class);

    private final OperationLogMapper operationLogMapper;
    private final UserMapper userMapper;

    public OperationLogAspect(OperationLogMapper operationLogMapper, UserMapper userMapper) {
        this.operationLogMapper = operationLogMapper;
        this.userMapper = userMapper;
    }

    @Around("@annotation(logOp)")
    public Object logOperation(ProceedingJoinPoint joinPoint, LogOperation logOp) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = null;
        boolean success = true;

        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable e) {
            success = false;
            throw e;
        } finally {
            try {
                saveLog(logOp, success, result);
            } catch (Exception e) {
                log.warn("记录操作日志失败: {}", e.getMessage());
            }
        }
    }

    private void saveLog(LogOperation logOp, boolean success, Object result) {
        OperationLog opLog = new OperationLog();
        opLog.setModule(logOp.module());
        opLog.setAction(logOp.action());

        // 构建操作内容
        String desc = logOp.description();
        if (!desc.isEmpty()) {
            opLog.setContent(desc + (success ? "" : " (失败)"));
        }

        // 获取当前登录用户
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Integer userId) {
            opLog.setOperatorId(userId);
            User user = userMapper.selectById(userId);
            if (user != null) {
                opLog.setOperatorName(user.getRealName());
            }
        }

        opLog.setCreatedAt(LocalDateTime.now());
        operationLogMapper.insert(opLog);
    }
}
