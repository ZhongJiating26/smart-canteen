package cn.zhongjiating.canteen.user.config;

import cn.zhongjiating.canteen.common.context.UserContext;
import cn.zhongjiating.canteen.common.context.UserContextHolder;
import cn.zhongjiating.canteen.common.enums.RoleEnum;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class UserContextInterceptor implements HandlerInterceptor {

    private static final String USER_ID_HEADER = "X-User-Id";

    private static final String USER_ROLE_HEADER = "X-User-Role";

    private static final String USERNAME_HEADER = "X-Username";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String userId = request.getHeader(USER_ID_HEADER);
        String role = request.getHeader(USER_ROLE_HEADER);
        String username = request.getHeader(USERNAME_HEADER);
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
