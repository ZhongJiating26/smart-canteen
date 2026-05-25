package cn.zhongjiating.canteen.order.feign;

import cn.zhongjiating.canteen.common.dto.Result;
import cn.zhongjiating.canteen.common.exception.BusinessException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class OrderFeignConfig {

    @Bean
    public ErrorDecoder orderFeignErrorDecoder(ObjectMapper objectMapper) {
        return new BusinessResultErrorDecoder(objectMapper);
    }

    private static class BusinessResultErrorDecoder implements ErrorDecoder {

        private final ObjectMapper objectMapper;

        private BusinessResultErrorDecoder(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
        }

        @Override
        public Exception decode(String methodKey, Response response) {
            String message = response.reason() == null || response.reason().isBlank()
                    ? "远程服务调用失败" : response.reason();
            Result<?> result = readResult(response);
            if (result != null) {
                return RemoteResultErrors.toBusinessException(result, message);
            }
            return new BusinessException(RemoteResultErrors.errorCode(response.status(), message), message);
        }

        private Result<?> readResult(Response response) {
            if (response.body() == null) {
                return null;
            }
            try {
                return objectMapper.readValue(response.body().asInputStream(), new TypeReference<Result<Object>>() {
                });
            } catch (IOException ex) {
                return null;
            }
        }
    }
}
