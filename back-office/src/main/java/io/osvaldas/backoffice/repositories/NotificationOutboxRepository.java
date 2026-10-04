package io.osvaldas.backoffice.repositories;

import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.osvaldas.backoffice.repositories.entities.NotificationOutbox;

@Repository
public interface NotificationOutboxRepository extends JpaRepository<NotificationOutbox, Long> {

    @Query(value = """
        SELECT * FROM notification_outbox
        WHERE published_at IS NULL
        ORDER BY id
        LIMIT :limit
        FOR UPDATE SKIP LOCKED
        """, nativeQuery = true)
    List<NotificationOutbox> lockPending(int limit);

    long countByPublishedAtIsNull();

    @Modifying
    @Query("DELETE FROM NotificationOutbox o WHERE o.publishedAt < :publishedBefore")
    int deletePublishedBefore(ZonedDateTime publishedBefore);

}
