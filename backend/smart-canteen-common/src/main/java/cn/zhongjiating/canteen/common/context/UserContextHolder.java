package cn.zhongjiating.canteen.common.context;

import cn.zhongjiating.canteen.common.exception.UnauthorizedException;

import java.util.Optional;

public final class UserContextHolder {

    private static final ThreadLocal<UserContext> CONTEXT = new ThreadLocal<>();

    private UserContextHolder() {
    }

    public static void set(UserContext context) {
        CONTEXT.set(context);
    }

    public static UserContext get() {
        return CONTEXT.get();
    }

    public static Optional<UserContext> getOptional() {
        return Optional.ofNullable(CONTEXT.get());
    }

    public static UserContext require() {
        UserContext context = CONTEXT.get();
        if (context == null) {
            throw new UnauthorizedException("未获取到用户上下文");
        }
        return context;
    }

    public static Long requireUserId() {
        return require().getUserId();
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
