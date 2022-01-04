package com.uncledavecode.notificator.repository;

import com.uncledavecode.notificator.model.AccessRequest;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccessRequestRepository extends JpaRepository<AccessRequest, Long>{
    public AccessRequest findByChatId(Long chatId);

    public Long deleteByChatId(Long chatId);
}
