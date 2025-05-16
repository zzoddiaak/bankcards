package example.bankcards.until;

import example.bankcards.service.impl.CardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CardStatusScheduler {

    private final CardService cardService;

    @Scheduled(cron = "0 0 1 * * ?")
    public void updateExpiredCards() {
        log.info("Проверка просроченных карт...");
        cardService.checkAndExpireCards();
    }
}

