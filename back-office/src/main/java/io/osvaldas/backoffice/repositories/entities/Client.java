package io.osvaldas.backoffice.repositories.entities;

import static io.osvaldas.api.clients.Status.REGISTERED;
import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.LAZY;
import static java.util.Comparator.comparing;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.envers.Audited;

import io.osvaldas.api.clients.Status;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "client")
@Audited
public class Client {

    @Id
    private String id;

    @NotNull
    private String firstName;

    @NotNull
    private String lastName;

    @NotNull
    private String email;

    @NotNull
    private String phoneNumber;

    @Enumerated(STRING)
    private Status status = REGISTERED;

    @NotNull
    @Column(length = 11)
    private String personalCode;

    @OneToMany(mappedBy = "client", cascade = ALL, fetch = LAZY)
    private Set<Loan> loans = new HashSet<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    private ZonedDateTime updatedAt;

    @Version
    private long version;

    public void addLoan(Loan loan) {
        loans.add(loan);
    }

    public void setRandomId() {
        setId(UUID.randomUUID().toString());
    }

    public String getFullName() {
        return this.firstName + " " + this.lastName;
    }

    public Optional<Loan> getLastLoan() {
        return this.getLoans().stream()
            .max(comparing(Loan::getId));
    }
}
