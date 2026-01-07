package io.hhplus.tdd;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.GlobalPointExceptionHandler;
import io.hhplus.tdd.point.PointController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PointControllerTest {

  private MockMvc mockMvc;
  private UserPointTable userPointTable;
  private PointHistoryTable pointHistoryTable;

  @BeforeEach
  void setup() {
    userPointTable = new UserPointTable();
    pointHistoryTable = new PointHistoryTable();
    PointController controller = new PointController(userPointTable, pointHistoryTable);
    mockMvc = MockMvcBuilders.standaloneSetup(controller)
        .setControllerAdvice(new GlobalPointExceptionHandler())
        .build();
  }

  /**
   * 시나리오: 포인트 잔고 조회
   * 1. 잔고가 없는 신규 유저의 포인트 조회
   * 2. 응답 포인트가 0인지 검증
   */
  @Test
  @DisplayName("포인트 조회 - 잔고 없으면 0")
  void testGetPointEmpty() throws Exception {
    mockMvc.perform(get("/point/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.point", is(0)));
  }

  @Test
  @DisplayName("포인트 충전 성공")
  void testChargePoint() throws Exception {
    mockMvc.perform(patch("/point/1/charge")
        .contentType(MediaType.APPLICATION_JSON)
        .content("1000"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.point", is(1000)));
  }

  /**
   * 시나리오: 0 이하 금액 충전 실패
   * 1. 금액이 0원인 충전 요청
   * 2. 400 Bad Request와 "금액은 0보다 큰 정수여야 합니다." 메시지 확인
   */
  @Test
  @DisplayName("금액이 0 이하일 때 충전 실패")
  void testChargeInvalidAmount() throws Exception {
    var result = mockMvc.perform(patch("/point/1/charge")
        .contentType(MediaType.APPLICATION_JSON)
        .content("0"))
        .andReturn();
    System.out.println("status = " + result.getResponse().getStatus());
    System.out.println("body   = " + result.getResponse().getContentAsString());

    mockMvc.perform(patch("/point/1/charge")
        .contentType(MediaType.APPLICATION_JSON)
        .content("0"))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("금액은 0보다 큰 정수여야 합니다."));
  }

  @Test
  @DisplayName("포인트 사용 성공")
  void testUsePoint() throws Exception {
    userPointTable.insertOrUpdate(1L, 1000);
    mockMvc.perform(patch("/point/1/use")
        .contentType(MediaType.APPLICATION_JSON)
        .content("500"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.point", is(500)));
  }

  /**
   * 시나리오: 잔고 부족 사용 실패
   * 1. 잔고 100원 세팅
   * 2. 200원 사용 요청
   * 3. 409 Conflict와 "잔액 부족" 메시지 확인
   */
  @Test
  @DisplayName("잔고 부족 시 사용 실패")
  void testUseInsufficient() throws Exception {
    userPointTable.insertOrUpdate(1L, 100);
    var result = mockMvc.perform(patch("/point/1/use")
        .contentType(MediaType.APPLICATION_JSON)
        .content("200"))
        .andReturn();
    System.out.println("status(use) = " + result.getResponse().getStatus());
    System.out.println("body(use)   = " + result.getResponse().getContentAsString());

    mockMvc.perform(patch("/point/1/use")
        .contentType(MediaType.APPLICATION_JSON)
        .content("200"))
        .andExpect(status().isConflict())
        .andExpect(content().string("잔액 부족"));
  }

  /**
   * 시나리오: 포인트 내역 조회
   * 1. 내역이 없는 유저의 기록 조회
   * 2. 결과 배열 길이가 0인지 확인
   */
  @Test
  @DisplayName("포인트 내역 조회")
  void testHistories() throws Exception {
    mockMvc.perform(get("/point/1/histories"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()", is(0)));
  }

  @Test
  @DisplayName("충전 - 숫자가 아닌 충전 요청 400")
  void testChargeNotNumber() throws Exception {
    mockMvc.perform(patch("/point/1/charge")
        .contentType(MediaType.APPLICATION_JSON)
        .content("abc"))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("금액은 0보다 큰 정수여야 합니다."));
  }

  @Test
  @DisplayName("충전 - 공백이면 400")
  void testChargeBlank() throws Exception {
    mockMvc.perform(patch("/point/1/charge")
        .contentType(MediaType.APPLICATION_JSON)
        .content("   "))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("금액은 0보다 큰 정수여야 합니다."));
  }

  /**
   * 시나리오: 오버플로 방지 검증
   * 1. 잔고를 Long.MAX_VALUE - 5로 세팅
   * 2. 10원 충전 요청 → 허용 범위 초과 에러
   */
  @Test
  @DisplayName("충전 - Long.MAX_VALUE 근처에서 오버플로 금지")
  void testChargeOverflow() throws Exception {
    userPointTable.insertOrUpdate(1L, Long.MAX_VALUE - 5);
    mockMvc.perform(patch("/point/1/charge")
        .contentType(MediaType.APPLICATION_JSON)
        .content("10"))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("허용 범위를 초과합니다."));
  }

  /**
   * 시나리오: 내역 기록 정확성 검증
   * 1. 1000원 충전
   * 2. 400원 사용
   * 3. 내역 배열 길이 2, 순서대로 CHARGE/USE와 금액 검증
   */
  @Test
  @DisplayName("내역 기록 - 충전/사용 시 정확히 2건, 타입/금액 검증")
  void testHistoryCorrectness() throws Exception {
    mockMvc.perform(patch("/point/1/charge")
        .contentType(MediaType.APPLICATION_JSON)
        .content("1000"))
        .andExpect(status().isOk());

    mockMvc.perform(patch("/point/1/use")
        .contentType(MediaType.APPLICATION_JSON)
        .content("400"))
        .andExpect(status().isOk());

    mockMvc.perform(get("/point/1/histories"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()", is(2)))
        .andExpect(jsonPath("$[0].type", is("CHARGE")))
        .andExpect(jsonPath("$[0].amount", is(1000)))
        .andExpect(jsonPath("$[1].type", is("USE")))
        .andExpect(jsonPath("$[1].amount", is(400)));
  }
}