package io.hhplus.tdd.point.presentation;

import io.hhplus.tdd.common.pagination.PageRequest;
import io.hhplus.tdd.point.application.PointService;
import io.hhplus.tdd.point.domain.PointHistory;
import io.hhplus.tdd.point.domain.UserPoint;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/point")
@Slf4j
@Validated
@RequiredArgsConstructor
public class PointController {

	private final PointService pointService;

	@GetMapping("{id}")
	public UserPoint point(
		@PathVariable @PositiveOrZero long id
	) {
		return pointService.findUserPointById(id);
	}

    @GetMapping("{id}/histories")
    public List<PointHistory> history(
            @PathVariable @PositiveOrZero long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return pointService.findPointHistoriesByUserId(id, new PageRequest(page, size));
    }

    @PatchMapping("{id}/charge")
    public UserPoint charge(
            @PathVariable @PositiveOrZero long id,
            @RequestBody @Positive long amount
    ) {
        return pointService.chargeUserPoint(id, amount);
    }

    @PatchMapping("{id}/use")
    public UserPoint use(
            @PathVariable @PositiveOrZero long id,
            @RequestBody @Positive long amount
    ) {
        return pointService.useUserPoint(id, amount);
    }
}
