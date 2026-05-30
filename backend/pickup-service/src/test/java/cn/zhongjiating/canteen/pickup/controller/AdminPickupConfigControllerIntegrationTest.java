package cn.zhongjiating.canteen.pickup.controller;

import cn.zhongjiating.canteen.pickup.config.PickupUserContextInterceptor;
import cn.zhongjiating.canteen.pickup.config.WebMvcConfig;
import cn.zhongjiating.canteen.pickup.dto.UpdatePickupConfigRequest;
import cn.zhongjiating.canteen.pickup.mapper.*;
import cn.zhongjiating.canteen.pickup.service.PickupConfigService;
import cn.zhongjiating.canteen.pickup.vo.PickupConfigVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminPickupConfigController.class)
@Import({PickupUserContextInterceptor.class, WebMvcConfig.class})
class AdminPickupConfigControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PickupConfigService pickupConfigService;

    // Mock MyBatis mappers to override their bean definitions and avoid sqlSessionFactory initialization issues
    @MockBean
    private CallEventMapper callEventMapper;

    @MockBean
    private PickupConfigMapper pickupConfigMapper;

    @MockBean
    private PickupNumberSequenceMapper pickupNumberSequenceMapper;

    @MockBean
    private PickupQrcodeMapper pickupQrcodeMapper;

    @MockBean
    private PreparingQueueItemMapper preparingQueueItemMapper;

    @MockBean
    private VerifyRecordMapper verifyRecordMapper;

    @MockBean
    private WaitingQueueItemMapper waitingQueueItemMapper;

    @Test
    void list_AsAdmin_ReturnsList() throws Exception {
        PickupConfigVO vo = new PickupConfigVO("reset_rule", "DAILY", "SYSTEM", "Daily Reset", LocalDateTime.now(), 1000000L);
        List<PickupConfigVO> list = Collections.singletonList(vo);

        when(pickupConfigService.list()).thenReturn(list);

        mockMvc.perform(get("/api/admin/pickup/config")
                        .header("X-User-Id", "1000000")
                        .header("X-User-Role", "ADMIN")
                        .header("X-Username", "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].configKey").value("reset_rule"))
                .andExpect(jsonPath("$.data[0].configValue").value("DAILY"));

        verify(pickupConfigService).list();
    }

    @Test
    void list_AsUser_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/admin/pickup/config")
                        .header("X-User-Id", "1001")
                        .header("X-User-Role", "USER")
                        .header("X-Username", "user"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));

        verify(pickupConfigService, never()).list();
    }

    @Test
    void update_AsAdmin_ReturnsUpdatedConfig() throws Exception {
        UpdatePickupConfigRequest request = new UpdatePickupConfigRequest("reset_rule", "HOURLY");
        PickupConfigVO vo = new PickupConfigVO("reset_rule", "HOURLY", "SYSTEM", "Daily Reset", LocalDateTime.now(), 1000000L);

        when(pickupConfigService.update(eq(1000000L), any(UpdatePickupConfigRequest.class))).thenReturn(vo);

        mockMvc.perform(put("/api/admin/pickup/config")
                        .header("X-User-Id", "1000000")
                        .header("X-User-Role", "ADMIN")
                        .header("X-Username", "admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.configValue").value("HOURLY"));

        verify(pickupConfigService).update(eq(1000000L), any(UpdatePickupConfigRequest.class));
    }
}
