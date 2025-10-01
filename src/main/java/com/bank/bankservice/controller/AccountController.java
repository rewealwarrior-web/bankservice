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



@RestController //Используя RestController у нас есть возможность не оборачивать DTO в ResponseEntity и не писать аннотацию ResponseBody над исходящими DTO
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
    public AccountDto create(@RequestBody @Valid RequestCreateAccount requestCreateAccount) {

        AccountDto accountDto = accountCreationService.create(AccountDto.builder()
                                                                        .id(null)
                                                                        .number(requestCreateAccount.getNumber())
                                                                        .userId(requestCreateAccount.getUserId())
                                                                        .accountType(requestCreateAccount.getAccountType())
                                                                        .balance(new BigDecimal(0))
                                                                        .build());
        return accountDto;
    } 

    @Operation(summary = "Getting a specific user's bank account")
    @VerifyListningAccess
    @GetMapping("/{number}")
    public AccountDto getAccount(@RequestParam Long userId, @PathVariable Long number) {

        AccountDto accountDto = accountListingService.getUserAccount(userId, number);
        return accountDto;
    }

    @Operation(summary = "Getting all bank accounts of a specific user")
    @VerifyListningAccess
    @GetMapping
    public List<AccountDto> getAccounts(@RequestParam Long userId) {

        List<AccountDto> accountsDto = accountListingService.getUserAccounts(userId);
        return accountsDto;
    }

    @Operation(summary = "Getting accounts of a certain type of a specific user")
    @VerifyListningAccess
    @GetMapping("/types/{accountType}")
    public List<AccountDto> getByType(@RequestParam Long userId, @PathVariable AccountType accountType) {

        List<AccountDto> accountsDto = accountListingService.getUserAccountsByType(userId, accountType);
        return accountsDto;
    }

    @Operation(summary = "Adding money to a specific bank account")
    @PostMapping("/{payeeNumber}/deposits")
    public DepositResponse deposit(@PathVariable Long payeeNumber, @RequestBody BigDecimal amount) {

        DepositResponse depositResponse = accountBalanceManipulationService.deposit(payeeNumber, amount);
        return depositResponse;
    }

    @Operation(summary = "Debiting money from specific bank account")
    @VerifyBalanceManipulationAccess
    @PostMapping("/{sourceMoneyNumber}/withdrawals")
    public WithdrawResponse withdraw(@PathVariable Long sourceMoneyNumber, @RequestBody BigDecimal amount) {

        WithdrawResponse withdrawResponse = accountBalanceManipulationService.withdraw(sourceMoneyNumber, amount);
        return withdrawResponse;
    }

    @Operation(summary = "Trasfering money from one bank account to another")
    @VerifyBalanceManipulationAccess
    @PostMapping("/transfers")
    public TransferResponse transfer(@RequestBody TransferRequest transferRequest) {
        
        TransferResponse transferResponse = accountBalanceManipulationService.transfer(transferRequest);
        return transferResponse;
    }
}
