package com.uncledavecode.notificator.repository;

import com.uncledavecode.notificator.model.UserAccount;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAccountRepository extends JpaRepository<UserAccount, Long>{
    public UserAccount findByChatId(Long chatId);

    public Long deleteByChatId(Long chatId);
}
