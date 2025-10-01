package com.bank.bankservice.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bank.bankservice.aspect.VerifyBalanceManipulationAccess;
import com.bank.bankservice.aspect.VerifyCreatingAccess;
import com.bank.bankservice.aspect.VerifyListningAccess;
import com.bank.bankservice.domain.dto.AccountDto;
import com.bank.bankservice.domain.dto.AccountType;
import com.bank.bankservice.domain.dto.DepositResponse;
import com.bank.bankservice.domain.dto.RequestCreateAccount;
import com.bank.bankservice.domain.dto.TransferRequest;
import com.bank.bankservice.domain.dto.TransferResponse;
import com.bank.bankservice.domain.dto.WithdrawResponse;
import com.bank.bankservice.service.contract.AccountBalanceManipulationService;
import com.bank.bankservice.service.contract.AccountCreationService;
import com.bank.bankservice.service.contract.AccountListingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;



@RestController
@RequestMapping("api/accounts")
@RequiredArgsConstructor
@Tag(name = "Bank logic")
public class AccountController {
    private final AccountBalanceManipulationService accountBalanceManipulationService;
    private final AccountCreationService accountCreationService;
    private final AccountListingService accountListingService;

    @Operation(summary = "Creating a bank account")
    @VerifyCreatingAccess
    @PostMapping
    public ResponseEntity<AccountDto> create(@RequestBody @Valid RequestCreateAccount requestCreateAccount) {

        AccountDto accountDto = accountCreationService.create(AccountDto.builder() // этот код пахнет. Убери этот builder в сервисный слой
                                                                        .id(null) // если убрать эту строку, ничего не изменится. Поле id будет инициализировано как null(по-умолчанию)
                                                                        .number(requestCreateAccount.getNumber()) //Возможно стоит принимать другую DTO в RequestBody, чтобы не было маппинга DTO -> DTO -> Entity. Сделай одно преобразование в сервисном слое(DTO -> Entity)
                                                                        .userId(requestCreateAccount.getUserId())
                                                                        .accountType(requestCreateAccount.getAccountType())
                                                                        .balance(new BigDecimal(0)) // стоит подумать над значением по-умолчанию на уровне БД. Если баланс не может быть null, тогда ставь constraint на колонке в таблице(not-null у тебя уже стоит). + добавь значение по-умолчанию для этого столбца(0)
                                                                        .build());
        return ResponseEntity.ok(accountDto);
    } 

    @Operation(summary = "Getting a specific user's bank account")
    @VerifyListningAccess
    @GetMapping("/{number}")
    public ResponseEntity<AccountDto> getAccount(@RequestParam Long userId, @PathVariable Long number) {

        AccountDto accountDto = accountListingService.getUserAccount(userId, number);
        return ResponseEntity.ok(accountDto);
    }

    @Operation(summary = "Getting all bank accounts of a specific user")
    @VerifyListningAccess
    @GetMapping
    public ResponseEntity<List<AccountDto>> getAccounts(@RequestParam Long userId) {

        List<AccountDto> accountsDto = accountListingService.getUserAccounts(userId);
        return ResponseEntity.ok(accountsDto);
    }

    @Operation(summary = "Getting accounts of a certain type of a specific user")
    @VerifyListningAccess
    @GetMapping("/types/{accountType}")
    public ResponseEntity<List<AccountDto>> getByType(@RequestParam Long userId, @PathVariable AccountType accountType) {

        List<AccountDto> accountsDto = accountListingService.getUserAccountsByType(userId, accountType);
        return ResponseEntity.ok(accountsDto);
    }

    @Operation(summary = "Adding money to a specific bank account")
    @PostMapping("/{payeeNumber}/deposits")
    public ResponseEntity<DepositResponse> deposit(@PathVariable Long payeeNumber, @RequestBody BigDecimal amount) {

        DepositResponse depositResponse = accountBalanceManipulationService.deposit(payeeNumber, amount);
        return ResponseEntity.ok(depositResponse);
    }

    @Operation(summary = "Debiting money from specific bank account")
    @VerifyBalanceManipulationAccess
    @PostMapping("/{sourceMoneyNumber}/withdrawals")
    public ResponseEntity<WithdrawResponse> withdraw(@PathVariable Long sourceMoneyNumber, @RequestBody BigDecimal amount) {

        WithdrawResponse withdrawResponse = accountBalanceManipulationService.withdraw(sourceMoneyNumber, amount);
        return ResponseEntity.ok(withdrawResponse);
    }

    @Operation(summary = "Trasfering money from one bank account to another")
    @VerifyBalanceManipulationAccess
    @PostMapping("/transfers")
    public ResponseEntity<TransferResponse> transfer(@RequestBody TransferRequest transferRequest) {
        
        TransferResponse transferResponse = accountBalanceManipulationService.transfer(transferRequest);
        return ResponseEntity.ok(transferResponse);
    }
}
