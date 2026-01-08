package io.hhplus.tdd;

// E2E 테스트
// 실제 사용자 시나리오를 큰 흐름 중심으로 검증

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.TransactionType;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PointE2ETest {

  @Autowired
  TestRestTemplate restTemplate;

  @Autowired
  UserPointTable userPointTable;

  @Autowired
  PointHistoryTable pointHistoryTable;

  @BeforeEach
  void resetTables() throws Exception {
    // 매 테스트 전 in-memory 테이블 초기화
    Field userTable = UserPointTable.class.getDeclaredField("table");
    userTable.setAccessible(true);
    ((Map<?, ?>) userTable.get(userPointTable)).clear();

    Field historyTable = PointHistoryTable.class.getDeclaredField("table");
    historyTable.setAccessible(true);
    ((List<?>) historyTable.get(pointHistoryTable)).clear();
  }

  @BeforeEach
  void setUpRestTemplate() {
    // PATCH 등 멱등하지 않은 메서드 지원
    restTemplate.getRestTemplate()
        .setRequestFactory(new HttpComponentsClientHttpRequestFactory(HttpClients.createDefault()));
  }

  /**
   * [ 시나리오 1 - 기본 흐름 (정상 케이스) ]
   * 초기조건 : 기존 유저 잔고 500원, 사용 내역 없음
   * 1. 포인트 조회 (500원)
   * 2. 포인트 1000원 충전 -> 잔액 1500원
   * 3. 포인트 500원 사용 -> 잔액 1000원
   * 4. 내역 조회 -> CHARGE(500), CHARGE(1000), USE(500)
   */
  @Test
  @DisplayName("시나리오1: 기존 유저 정상 흐름")
  void 기존유저_기본흐름() {
    // 초기 데이터 세팅
    userPointTable.insertOrUpdate(1L, 500L);
    pointHistoryTable.insert(1L, 500L, TransactionType.CHARGE, System.currentTimeMillis());

    // 1) 조회
    ResponseEntity<String> 조회 = restTemplate.getForEntity("/point/1", String.class);
    assertThat(조회.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(조회.getBody()).contains("\"point\":500");

    // 2) 1000 충전
    ResponseEntity<String> 충전결과 = restTemplate.exchange(
        "/point/1/charge", HttpMethod.PATCH,
        new HttpEntity<>(1000L, jsonHeaders()), String.class);
    assertThat(충전결과.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(충전결과.getBody()).contains("\"point\":1500");

    // 3) 500 사용
    ResponseEntity<String> 사용결과 = restTemplate.exchange(
        "/point/1/use", HttpMethod.PATCH,
        new HttpEntity<>(500L, jsonHeaders()), String.class);
    assertThat(사용결과.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(사용결과.getBody()).contains("\"point\":1000");

    // 4) 내역 검증 (금액 + 타입)
    List<PointHistory> histories = pointHistoryTable.selectAllByUserId(1L);
    assertThat(histories).hasSize(3);

    assertThat(histories.get(0).amount()).isEqualTo(500);
    assertThat(histories.get(0).type()).isEqualTo(TransactionType.CHARGE);

    assertThat(histories.get(1).amount()).isEqualTo(1000);
    assertThat(histories.get(1).type()).isEqualTo(TransactionType.CHARGE);

    assertThat(histories.get(2).amount()).isEqualTo(500);
    assertThat(histories.get(2).type()).isEqualTo(TransactionType.USE);
  }

  /**
   * [ 시나리오 2 - 잔고 부족 실패 ]
   * 초기조건 : 기존 유저 잔고 200원, 사용 내역 없음
   * 1. 포인트 500원 사용 시도
   * 2. 잔고 부족으로 409 Conflict 응답
   * 3. 응답 메시지 "잔액 부족" 확인
   */
  @Test
  @DisplayName("시나리오2: 잔고 부족 사용 실패")
  void 잔고부족_실패() {
    userPointTable.insertOrUpdate(1L, 200L);
    pointHistoryTable.insert(1L, 200L, TransactionType.CHARGE, System.currentTimeMillis());

    ResponseEntity<String> fail = restTemplate.exchange(
        "/point/1/use", HttpMethod.PATCH,
        new HttpEntity<>(500L, jsonHeaders()), String.class);

    assertThat(fail.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    assertThat(fail.getBody()).contains("잔액 부족");
  }

  /**
   * [ 시나리오 3 - 0원 충전 요청 실패 ]
   * 독립 실행
   * 0원 충전 시 잘못된 요청으로 실패
   * 1. 포인트 0원 충전 요청
   * 2. 400 Bad Request 응답
   * 3. 응답 메시지 "금액은 0보다 큰 정수여야 합니다." 확인
   */
  @Test
  @DisplayName("시나리오3: 0원 충전 요청 실패")
  void 금액0_충전실패() {
    ResponseEntity<String> zeroCharge = restTemplate.exchange(
        "/point/1/charge", HttpMethod.PATCH,
        new HttpEntity<>(0L, jsonHeaders()), String.class);

    assertThat(zeroCharge.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(zeroCharge.getBody()).contains("금액은 0보다 큰 정수여야 합니다.");
  }

  /**
   * [ 시나리오 4 - 음수 금액 사용 요청 실패 ]
   * 독립 실행
   * 음수 금액 사용 시 잘못된 요청으로 실패
   * 1. 포인트 -100원 사용 요청
   * 2. 400 Bad Request 응답
   * 3. 응답 메시지 "금액은 0보다 큰 정수여야 합니다." 확인
   */
  @Test
  @DisplayName("시나리오4: 음수 사용 요청 실패")
  void 음수금액_사용실패() {
    ResponseEntity<String> minusUse = restTemplate.exchange(
        "/point/1/use", HttpMethod.PATCH,
        new HttpEntity<>(-100L, jsonHeaders()), String.class);

    assertThat(minusUse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(minusUse.getBody()).contains("금액은 0보다 큰 정수여야 합니다.");
  }

  private HttpHeaders jsonHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    return headers;
  }
}
