package io.hhplus.tdd;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// 통합 테스트
// 각 API URL이 정상적으로 모든 계층을 거쳐 결과를 만드는지 확인

/**
 * [ 예시 시나리오 ]
 * 1. 조회: 포인트가 없을 때 0 반환
 * 2. 충전: 양수 금액 충전 시 정상 동작 / 0·음수 금액 충전 시 400 오류
 * 3. 사용: 잔고 충분 → 정상 차감, 잔고 부족 → 409 오류
 * 4. 내역 조회: 충전/사용 시 기록이 쌓이는지 확인
 */

@SpringBootTest
@AutoConfigureMockMvc
class PointIntegrationTest {

  @Autowired
  MockMvc mockMvc;

  @Autowired
  UserPointTable userPointTable;

  @Autowired
  PointHistoryTable pointHistoryTable;

  @BeforeEach
  void clearData() throws Exception {
    // Reflection으로 내부 자료구조 직접 비움
    Field userTableField = UserPointTable.class.getDeclaredField("table");
    userTableField.setAccessible(true);
    ((Map<?, ?>) userTableField.get(userPointTable)).clear();

    Field historyTableField = PointHistoryTable.class.getDeclaredField("table");
    historyTableField.setAccessible(true);
    ((List<?>) historyTableField.get(pointHistoryTable)).clear();
  }

  @Test
  @DisplayName("조회: 포인트가 없을 때 0 반환")
  // 신규 유저에 대해 포인트 조회 API 호출 시, 기본값 0이 정확히 반환되는지 검증
  void 포인트조회_없는유저_0원반환() throws Exception {
    mockMvc.perform(get("/point/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.point", is(0)));
  }

  @Test
  @DisplayName("충전: 양수 금액 충전 시 정상 동작")
  // 유효한 양수 금액 충전 요청 시 포인트가 정상적으로 증가하고,
  // 충전 내역이 올바르게 기록되어 조회되는지 검증
  void 포인트충전_성공() throws Exception {
    mockMvc.perform(patch("/point/1/charge")
        .contentType(MediaType.APPLICATION_JSON)
        .content("1000"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.point", is(1000)));

    // 내역에 CHARGE 기록 1건 추가 확인
    mockMvc.perform(get("/point/1/histories"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()", is(1)))
        .andExpect(jsonPath("$[0].amount", is(1000)))
        .andExpect(jsonPath("$[0].type", is("CHARGE")));
  }

  @Test
  @DisplayName("충전: 금액이 0이면 400 오류")
  // 0원 충전 요청은 비정상 입력으로 판단하여
  // 400 Bad Request 응답 및 적절한 오류 메시지 반환 확인
  void 포인트충전_금액0_실패() throws Exception {
    mockMvc.perform(patch("/point/1/charge")
        .contentType(MediaType.APPLICATION_JSON)
        .content("0"))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("금액은 0보다 큰 정수여야 합니다."));
  }

  @Test
  @DisplayName("충전: 금액이 음수면 400 오류")
  // 음수 충전 요청 시에도 동일하게 400 오류 발생 및 메시지 검증
  void 포인트충전_음수금액_실패() throws Exception {
    mockMvc.perform(patch("/point/1/charge")
        .contentType(MediaType.APPLICATION_JSON)
        .content("-100"))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("금액은 0보다 큰 정수여야 합니다."));
  }

  @Test
  @DisplayName("사용: 잔고 충분 → 정상 차감")
  // 사전 포인트 충전 후, 잔액 내에서 사용 요청 시
  // 잔액 정상 차감 및 사용 내역 추가, API 응답 정상 확인
  void 포인트사용_성공() throws Exception {
    // 사전 충전
    userPointTable.insertOrUpdate(1L, 1000);

    mockMvc.perform(patch("/point/1/use")
        .contentType(MediaType.APPLICATION_JSON)
        .content("500"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.point", is(500)));

    // 사용 내역이 1건 추가 되었는지 조회로 검증
    mockMvc.perform(get("/point/1/histories"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()", is(1)))
        .andExpect(jsonPath("$[0].amount", is(500)))
        .andExpect(jsonPath("$[0].type", is("USE")));
  }

  @Test
  @DisplayName("사용: 잔고 부족 → 409 오류")
  // 잔액보다 큰 금액 사용 요청 시,
  // 409 Conflict 상태와 '잔액 부족' 메시지 반환 확인
  void 포인트사용_잔고부족_실패() throws Exception {
    userPointTable.insertOrUpdate(1L, 100);

    mockMvc.perform(patch("/point/1/use")
        .contentType(MediaType.APPLICATION_JSON)
        .content("200"))
        .andExpect(status().isConflict())
        .andExpect(content().string("잔액 부족"));
  }

  @Test
  @DisplayName("내역 조회: 충전/사용 시 기록이 쌓이는지 확인")
  // 충전과 사용 내역을 직접 삽입 후,
  // GET /histories API 호출 시 정확한 내역이 모두 조회되는지 검증
  void 포인트내역_조회() throws Exception {
    userPointTable.insertOrUpdate(1L, 0);
    pointHistoryTable.insert(1L, 500, TransactionType.CHARGE, System.currentTimeMillis());
    pointHistoryTable.insert(1L, 200, TransactionType.USE, System.currentTimeMillis());

    mockMvc.perform(get("/point/1/histories"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()", is(2)))
        .andExpect(jsonPath("$[0].type", is("CHARGE")))
        .andExpect(jsonPath("$[1].type", is("USE")));
  }
}