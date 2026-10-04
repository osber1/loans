package io.osvaldas.backoffice.repositories

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager

import io.osvaldas.backoffice.repositories.entities.NotificationOutbox
import spock.lang.Subject

class NotificationOutboxRepositorySpec extends AbstractDatabaseSpec {

    @Subject
    @Autowired
    NotificationOutboxRepository repository

    @Autowired
    TestEntityManager entityManager

    void 'should lock only unpublished rows in insertion order up to the limit'() {
        given:
            NotificationOutbox published = save('published').tap { publishedAt = DATE }
            NotificationOutbox first = save('first')
            NotificationOutbox second = save('second')
            save('third')
            entityManager.flush()
            entityManager.clear()
        when:
            List<NotificationOutbox> pending = repository.lockPending(2)
        then:
            pending*.id == [first.id, second.id]
            !pending*.id.contains(published.id)
    }

    void 'should delete only rows published before the given time'() {
        given:
            save('old').tap { publishedAt = DATE }
            save('recent').tap { publishedAt = DATE.plusDays(10) }
            NotificationOutbox pending = save('pending')
            entityManager.flush()
            entityManager.clear()
        when:
            int deleted = repository.deletePublishedBefore(DATE.plusDays(1))
        then:
            deleted == 1
            repository.findAll()*.clientId.toSet() == ['recent', 'pending'] as Set
            repository.findById(pending.id).present
    }

    private NotificationOutbox save(String clientId) {
        entityManager.persist(new NotificationOutbox(clientId, 'Name Surname', 'user@mail.com'))
    }

}
