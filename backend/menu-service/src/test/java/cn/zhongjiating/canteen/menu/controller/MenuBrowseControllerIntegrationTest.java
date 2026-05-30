package cn.zhongjiating.canteen.menu.controller;

import cn.zhongjiating.canteen.menu.config.MenuUserContextInterceptor;
import cn.zhongjiating.canteen.menu.config.WebMvcConfig;
import cn.zhongjiating.canteen.menu.mapper.*;
import cn.zhongjiating.canteen.menu.service.MenuBrowseService;
import cn.zhongjiating.canteen.menu.vo.AvailableMenuVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = MenuBrowseController.class)
@Import({MenuUserContextInterceptor.class, WebMvcConfig.class})
class MenuBrowseControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MenuBrowseService menuBrowseService;

    // Mock MyBatis mappers to override their bean definitions and avoid sqlSessionFactory initialization issues
    @MockBean
    private DailyMenuItemMapper dailyMenuItemMapper;

    @MockBean
    private DailyMenuMapper dailyMenuMapper;

    @MockBean
    private DishCategoryMapper dishCategoryMapper;

    @MockBean
    private DishMapper dishMapper;

    @MockBean
    private InventoryLogMapper inventoryLogMapper;

    @MockBean
    private MenuConfigMapper menuConfigMapper;

    @MockBean
    private MerchantMapper merchantMapper;

    @Test
    void available_ReturnsMenu() throws Exception {
        AvailableMenuVO vo = new AvailableMenuVO(Collections.emptyList());

        when(menuBrowseService.available(any(), any(), any())).thenReturn(vo);

        mockMvc.perform(get("/api/menu/available")
                        .param("mealPeriod", "LUNCH")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(menuBrowseService).available(any(), eq("LUNCH"), any());
    }
}
