package cn.zhongjiating.canteen.pickup.service.impl;

import cn.zhongjiating.canteen.common.enums.ErrorCode;
import cn.zhongjiating.canteen.common.exception.BusinessException;
import cn.zhongjiating.canteen.pickup.entity.PickupNumberSequence;
import cn.zhongjiating.canteen.pickup.mapper.PickupNumberSequenceMapper;
import cn.zhongjiating.canteen.pickup.service.PickupNumberService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class PickupNumberServiceImpl implements PickupNumberService {

    private static final Long SEQUENCE_ID = 1L;
    private static final String LOCK_KEY = "lock:pickup_seq";
    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end",
            Long.class);

    private final PickupNumberSequenceMapper sequenceMapper;
    private final StringRedisTemplate redisTemplate;

    public PickupNumberServiceImpl(PickupNumberSequenceMapper sequenceMapper, StringRedisTemplate redisTemplate) {
        this.sequenceMapper = sequenceMapper;
        this.redisTemplate = redisTemplate;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String generatePickupNo() {
        String lockValue = UUID.randomUUID().toString();
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(LOCK_KEY, lockValue, 3, TimeUnit.SECONDS);
        if (!Boolean.TRUE.equals(locked)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "取餐号生成繁忙，请重试");
        }
        try {
            return next();
        } finally {
            redisTemplate.execute(UNLOCK_SCRIPT, List.of(LOCK_KEY), lockValue);
        }
    }

    private String next() {
        PickupNumberSequence sequence = sequenceMapper.selectById(SEQUENCE_ID);
        if (sequence == null) {
            sequence = initialSequence();
            sequenceMapper.insert(sequence);
        }
        if (shouldReset(sequence)) {
            sequence.setCurrentLetter("A");
            sequence.setCurrentNumber(1);
            sequence.setLastResetTime(LocalDateTime.now());
        }
        String pickupNo = String.format("%s%03d", sequence.getCurrentLetter(), sequence.getCurrentNumber());
        advance(sequence);
        sequenceMapper.updateById(sequence);
        return pickupNo;
    }

    private PickupNumberSequence initialSequence() {
        PickupNumberSequence sequence = new PickupNumberSequence();
        sequence.setId(SEQUENCE_ID);
        sequence.setCurrentLetter("A");
        sequence.setCurrentNumber(1);
        sequence.setResetRule("NONE");
        return sequence;
    }

    private boolean shouldReset(PickupNumberSequence sequence) {
        LocalDateTime lastResetTime = sequence.getLastResetTime();
        if ("NONE".equals(sequence.getResetRule())) {
            return false;
        }
        if (lastResetTime == null) {
            return true;
        }
        LocalDateTime now = LocalDateTime.now();
        if ("DAILY".equals(sequence.getResetRule())) {
            return !lastResetTime.toLocalDate().equals(now.toLocalDate());
        }
        if ("WEEKLY".equals(sequence.getResetRule())) {
            return weekStart(lastResetTime).isBefore(weekStart(now));
        }
        return false;
    }

    private LocalDateTime weekStart(LocalDateTime time) {
        return time.toLocalDate().with(DayOfWeek.MONDAY).atStartOfDay();
    }

    private void advance(PickupNumberSequence sequence) {
        int number = sequence.getCurrentNumber();
        char letter = sequence.getCurrentLetter().charAt(0);
        if (number < 999) {
            sequence.setCurrentNumber(number + 1);
        } else if (letter < 'Z') {
            sequence.setCurrentLetter(String.valueOf((char) (letter + 1)));
            sequence.setCurrentNumber(1);
        } else {
            sequence.setCurrentLetter("A");
            sequence.setCurrentNumber(1);
        }
    }
}
