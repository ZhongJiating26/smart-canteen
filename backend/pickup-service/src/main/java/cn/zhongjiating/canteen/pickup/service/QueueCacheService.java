package cn.zhongjiating.canteen.pickup.service;

import cn.zhongjiating.canteen.pickup.entity.PreparingQueueItem;
import cn.zhongjiating.canteen.pickup.entity.WaitingQueueItem;
import cn.zhongjiating.canteen.pickup.mapper.PreparingQueueItemMapper;
import cn.zhongjiating.canteen.pickup.mapper.WaitingQueueItemMapper;
import cn.zhongjiating.canteen.pickup.vo.QueueItemVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QueueCacheService {

    private final PreparingQueueItemMapper preparingQueueItemMapper;
    private final WaitingQueueItemMapper waitingQueueItemMapper;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public QueueCacheService(PreparingQueueItemMapper preparingQueueItemMapper,
                             WaitingQueueItemMapper waitingQueueItemMapper,
                             StringRedisTemplate redisTemplate,
                             ObjectMapper objectMapper) {
        this.preparingQueueItemMapper = preparingQueueItemMapper;
        this.waitingQueueItemMapper = waitingQueueItemMapper;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public List<QueueItemVO> preparing(Long merchantId) {
        return readOrRebuild(preparingKey(merchantId), () -> rebuildPreparing(merchantId));
    }

    public List<QueueItemVO> waiting(Long merchantId) {
        return readOrRebuild(waitingKey(merchantId), () -> rebuildWaiting(merchantId));
    }

    public List<QueueItemVO> rebuildPreparing(Long merchantId) {
        List<QueueItemVO> queue = preparingQueueItemMapper.selectList(new LambdaQueryWrapper<PreparingQueueItem>()
                        .eq(PreparingQueueItem::getMerchantId, merchantId)
                        .orderByAsc(PreparingQueueItem::getPosition)
                        .orderByAsc(PreparingQueueItem::getEnterTime))
                .stream()
                .map(item -> new QueueItemVO(item.getPickupNo(), item.getPosition()))
                .toList();
        write(preparingKey(merchantId), queue);
        return queue;
    }

    public List<QueueItemVO> rebuildWaiting(Long merchantId) {
        List<QueueItemVO> queue = waitingQueueItemMapper.selectList(new LambdaQueryWrapper<WaitingQueueItem>()
                        .eq(WaitingQueueItem::getMerchantId, merchantId)
                        .orderByAsc(WaitingQueueItem::getPosition)
                        .orderByAsc(WaitingQueueItem::getEnterTime))
                .stream()
                .map(item -> new QueueItemVO(item.getPickupNo(), item.getPosition()))
                .toList();
        write(waitingKey(merchantId), queue);
        return queue;
    }

    private List<QueueItemVO> readOrRebuild(String key, QueueLoader loader) {
        List<String> cached = redisTemplate.opsForList().range(key, 0, -1);
        if (cached == null || cached.isEmpty()) {
            return loader.load();
        }
        return cached.stream().map(this::readItem).toList();
    }

    private QueueItemVO readItem(String json) {
        try {
            return objectMapper.readValue(json, QueueItemVO.class);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("队列缓存内容无法解析", ex);
        }
    }

    private void write(String key, List<QueueItemVO> queue) {
        redisTemplate.delete(key);
        if (!queue.isEmpty()) {
            redisTemplate.opsForList().rightPushAll(key, queue.stream().map(this::writeItem).toList());
        }
    }

    private String writeItem(QueueItemVO item) {
        try {
            return objectMapper.writeValueAsString(item);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("队列缓存内容无法序列化", ex);
        }
    }

    private String preparingKey(Long merchantId) {
        return "queue:preparing:" + merchantId;
    }

    private String waitingKey(Long merchantId) {
        return "queue:waiting:" + merchantId;
    }

    @FunctionalInterface
    private interface QueueLoader {
        List<QueueItemVO> load();
    }
}
