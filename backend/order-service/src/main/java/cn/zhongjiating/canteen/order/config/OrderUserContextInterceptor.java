package cn.zhongjiating.canteen.order.config;

import cn.zhongjiating.canteen.common.context.UserContext;
import cn.zhongjiating.canteen.common.context.UserContextHolder;
import cn.zhongjiating.canteen.common.enums.RoleEnum;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class OrderUserContextInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String userId = request.getHeader(UserContext.HEADER_USER_ID);
        String role = request.getHeader(UserContext.HEADER_USER_ROLE);
        String username = request.getHeader(UserContext.HEADER_USERNAME);
        if (userId != null && !userId.isBlank() && role != null && !role.isBlank()) {
            UserContextHolder.set(new UserContext(Long.valueOf(userId), username, RoleEnum.fromCode(role)));
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContextHolder.clear();
    }
}
