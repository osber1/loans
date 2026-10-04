package io.osvaldas.backoffice.repositories.entities;

import java.time.ZonedDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class NotificationOutbox {

    @Id
    @GeneratedValue(generator = "NOTIFICATION_OUTBOX_SEQ", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "NOTIFICATION_OUTBOX_SEQ", sequenceName = "NOTIFICATION_OUTBOX_SEQ", allocationSize = 1)
    private long id;

    @Column(nullable = false)
    private String clientId;

    private String fullName;

    @Column(nullable = false)
    private String email;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    private ZonedDateTime publishedAt;

    public NotificationOutbox(String clientId, String fullName, String email) {
        this.clientId = clientId;
        this.fullName = fullName;
        this.email = email;
    }

}
