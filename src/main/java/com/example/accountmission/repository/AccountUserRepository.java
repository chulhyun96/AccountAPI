package com.example.accountmission.repository;

import com.example.accountmission.domain.AccountUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface AccountUserRepository extends JpaRepository<AccountUser, Long> {
}
