package io.hhplus.tdd.point;

import org.springframework.web.bind.annotation.*;

import io.hhplus.tdd.point.domain.PointHistory;
import io.hhplus.tdd.point.domain.UserPoint;

import java.util.List;

@RestController
@RequestMapping("/point")
public class PointController {

  private final PointService pointService;

  public PointController(PointService pointService) {
    this.pointService = pointService;
  }

  @GetMapping("/{id}")
  public UserPoint getPoint(@PathVariable long id) {
    return pointService.get(id);
  }

  @GetMapping("/{id}/histories")
  public List<PointHistory> getHistories(@PathVariable long id) {
    return pointService.histories(id);
  }

  // 순수 문자열 바디를 숫자로 판정 (비어있음/공백/숫자아님 → null)
  private static Long parseAmount(String body) {
    if (body == null)
      return null;
    String s = body.trim();
    if (s.isEmpty())
      return null;
    try {
      return Long.parseLong(s);
    } catch (NumberFormatException e) {
      return null;
    }
  }

  @PatchMapping("/{id}/charge")
  public UserPoint charge(@PathVariable long id, @RequestBody(required = false) String body) {
    Long amount = parseAmount(body);
    if (amount == null || amount <= 0) {
      // 전역 핸들러가 400 + 이 메시지로 내려줌
      throw new IllegalArgumentException("금액은 0보다 큰 정수여야 합니다.");
    }
    // 오버플로 시 서비스가 IllegalArgumentException("허용 범위를 초과합니다.") 던짐
    return pointService.charge(id, amount);
  }

  @PatchMapping("/{id}/use")
  public UserPoint use(@PathVariable long id, @RequestBody(required = false) String body) {
    Long amount = parseAmount(body);
    if (amount == null || amount <= 0) {
      throw new IllegalArgumentException("금액은 0보다 큰 정수여야 합니다.");
    }
    // 잔액 부족 시 서비스가 IllegalStateException("잔액 부족") 던짐
    return pointService.use(id, amount);
  }
}