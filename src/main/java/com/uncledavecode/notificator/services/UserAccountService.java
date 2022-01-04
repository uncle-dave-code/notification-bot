package com.uncledavecode.notificator.services;

import java.util.List;

import com.uncledavecode.notificator.model.UserAccount;
import com.uncledavecode.notificator.repository.UserAccountRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserAccountService {
    private final UserAccountRepository userAccountRepository;

    public UserAccountService(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    public UserAccount getByChatId(Long chatId) {
        if (chatId != null) {
            return this.userAccountRepository.findByChatId(chatId);
        } else {
            throw new IllegalArgumentException("chatId cannot be null");
        }
    }

    public UserAccount updateUserAccount(UserAccount userAccount) {
        
        if (userAccount != null) {
            UserAccount user = getByChatId(userAccount.getChatId());
            if(user != null){
                user.setEmail(userAccount.getEmail());
                user.setName(userAccount.getName());
                user.setLastname(userAccount.getLastname());
                user.setActive(userAccount.getActive());
            }else{
                user =  userAccount;
            }

            return this.userAccountRepository.save(user);
        } else {
            throw new IllegalArgumentException("userAccount cannot be null");
        }
    }

    public List<UserAccount> getAllUserAccounts() {
        return this.userAccountRepository.findAll();
    }

    @Transactional
    public Long deleteByChatId(Long chatId){
       return this.userAccountRepository.deleteByChatId(chatId);
    }
}
