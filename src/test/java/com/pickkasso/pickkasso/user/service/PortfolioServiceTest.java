package com.pickkasso.pickkasso.user.service;

import com.pickkasso.pickkasso.user.dto.portfolio.PortfolioDto;
import com.pickkasso.pickkasso.user.entity.Account;
import com.pickkasso.pickkasso.user.entity.Gender;
import com.pickkasso.pickkasso.user.entity.Photographer;
import com.pickkasso.pickkasso.user.entity.Portfolio;
import com.pickkasso.pickkasso.user.entity.PortfolioProjectType;
import com.pickkasso.pickkasso.user.entity.Role;
import com.pickkasso.pickkasso.user.repository.AccountRepository;
import com.pickkasso.pickkasso.user.repository.PhotographerRepository;
import com.pickkasso.pickkasso.user.repository.PortfolioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class PortfolioServiceTest {

    @Autowired
    private PortfolioService portfolioService;

    @Autowired
    private PortfolioRepository portfolioRepository;

    @Autowired
    private PhotographerRepository photographerRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Test
    @DisplayName("포트폴리오가 생성된다")
    void createPortfolio() {
        // given
        Photographer photographer = createTestPhotographer();

        PortfolioDto dto = new PortfolioDto(
                "감성 데이트 스냅",
                "따뜻한 분위기의 포트폴리오",
                null,
                null,
                null,
                null,
                PortfolioProjectType.COMMERCIAL
        );

        // when
        Long portfolioId = portfolioService.createPortfolio(photographer.getId(), dto);

        // then
        Portfolio portfolio = portfolioRepository.findById(portfolioId).orElseThrow();

        assertThat(portfolio.getName()).isEqualTo("감성 데이트 스냅");
        assertThat(portfolio.getDescription()).isEqualTo("따뜻한 분위기의 포트폴리오");
        assertThat(portfolio.getProjectType()).isEqualTo(PortfolioProjectType.COMMERCIAL);
    }

    private Photographer createTestPhotographer() {
        Account account = Account.createAccount(
                "portfolioUser",
                "encoded-password",
                Role.PHOTOGRAPHER
        );
        accountRepository.save(account);

        Photographer photographer = Photographer.createMember(
                account,
                "portfolio@example.com",
                "김작가",
                Gender.MALE,
                "010-0000-0000",
                0
        );

        return photographerRepository.save(photographer);
    }
}