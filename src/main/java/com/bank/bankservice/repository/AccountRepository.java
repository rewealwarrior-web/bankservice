package com.bank.bankservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bank.bankservice.domain.model.Account;


public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByUserId(Long clientId);  //метод называется userId, а ищет по некому clientId - не совсем понятно.

    Account findByNumber(Long number); //Поизучай Optional - очень удобный инструмент, часто используемый на настоящих проектах.
}
