export const OrderStatus = {
  PENDING_PAY: 'PENDING_PAY',
  PAID: 'PAID',
  PREPARING: 'PREPARING',
  READY: 'READY',
  COMPLETED: 'COMPLETED',
  CANCELLED: 'CANCELLED'
}

export const OrderStatusLabel = {
  [OrderStatus.PENDING_PAY]: 'Pending Payment',
  [OrderStatus.PAID]: 'Waiting for Accept',
  [OrderStatus.PREPARING]: 'Preparing',
  [OrderStatus.READY]: 'Ready for Pickup',
  [OrderStatus.COMPLETED]: 'Completed',
  [OrderStatus.CANCELLED]: 'Cancelled'
}

export const UserRole = {
  USER: 'USER',
  MERCHANT: 'MERCHANT',
  ADMIN: 'ADMIN'
}

export const MealPeriod = {
  BREAKFAST: 'BREAKFAST',
  LUNCH: 'LUNCH',
  DINNER: 'DINNER',
  SNACK: 'SNACK'
}

export const MealPeriodLabel = {
  [MealPeriod.BREAKFAST]: 'Breakfast',
  [MealPeriod.LUNCH]: 'Lunch',
  [MealPeriod.DINNER]: 'Dinner',
  [MealPeriod.SNACK]: 'Snack'
}

export const CallType = {
  AUTO: 'AUTO',
  MANUAL: 'MANUAL'
}

export const VerifyType = {
  SCAN: 'SCAN',
  MANUAL: 'MANUAL'
}

export const ErrorMessages = {
  UNAUTHORIZED: 'Token 无效或已过期',
  FORBIDDEN: '无权限访问',
  USER_EXISTS: '该账号已被注册',
  INVALID_CREDENTIALS: '账号或密码错误',
  ACCOUNT_DISABLED: '账号已被禁用',
  INVALID_VERIFY_CODE: '验证码无效',
  INVALID_PASSWORD: '密码不符合规则',
  INVALID_PHONE_FORMAT: '手机号格式不正确',
  PHONE_BOUND: '手机号已绑定其他账号',
  INVALID_STOCK: '库存不足',
  DISH_NOT_AVAILABLE: '菜品当前不可购买',
  ORDER_NOT_EXISTS: '订单不存在',
  ILLEGAL_STATUS: '当前状态不可执行此操作',
  ORDER_CANNOT_CANCEL: '当前订单不可取消',
  PAY_PASSWORD_ERROR: '支付密码错误',
  PAY_PASSWORD_LOCKED: '支付密码已锁定，请30分钟后重试',
  INVALID_PICKUP_CODE: '取餐码无效',
  ORDER_EXPIRED: '该订单已取消',
  WRONG_STALL: '请到对应窗口取餐',
  DUPLICATE_SUBMIT: '请勿重复提交'
}
