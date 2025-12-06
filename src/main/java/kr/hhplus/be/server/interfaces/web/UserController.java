package kr.hhplus.be.server.interfaces.web;

import kr.hhplus.be.server.application.UserService;
import kr.hhplus.be.server.domain.model.User;
import kr.hhplus.be.server.interfaces.web.dto.UserBalanceResponse;
import kr.hhplus.be.server.interfaces.web.dto.ChargeBalanceRequest;
import kr.hhplus.be.server.interfaces.web.dto.CreateUserRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserBalanceResponse> createUser(@RequestBody @Valid CreateUserRequest request) {
        User user = userService.createUser(request.userId(), request.initialBalance());
        return ResponseEntity.ok(UserBalanceResponse.from(user));
    }

    @GetMapping("/{userId}/balance")
    public ResponseEntity<UserBalanceResponse> getUserBalance(@PathVariable String userId) {
        User user = userService.getUserBalance(userId);
        return ResponseEntity.ok(UserBalanceResponse.from(user));
    }

    @POST
    @RequestMapping("/{userId}/balance/charge")
    public ResponseEntity<UserBalanceResponse> chargeBalance(
            @PathVariable String userId,
            @RequestBody @Valid ChargeBalanceRequest request) {
        User user = userService.chargeBalance(userId, request.amount());
        return ResponseEntity.ok(UserBalanceResponse.from(user));
    }
}