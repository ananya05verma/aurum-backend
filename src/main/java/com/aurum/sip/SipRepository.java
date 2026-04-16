package com.aurum.sip;

import com.aurum.portfolio.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SipRepository extends JpaRepository<Sip, Long> {

    List<Sip> findByPortfolio(Portfolio portfolio);
}