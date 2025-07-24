package com.gov.core.repository;

import com.gov.core.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {

    /**
     * 전화번호로 사용자 조회
     */
    Optional<User> findByPhone(String phone);

    /**
     * 사용자명으로 검색
     */
    Optional<User> findByName(String name);

}
