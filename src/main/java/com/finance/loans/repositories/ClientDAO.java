package com.finance.loans.repositories;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "client")
public class ClientDAO {

    @Id
    private String id;

    @NotNull
    private String firstName;

    @NotNull
    private String lastName;

    @NotNull
    @Column(length = 11)
    private Long personalCode;

    @OneToMany(cascade = CascadeType.ALL)
    private Set<Loan> loans = new HashSet<>();

    @CreationTimestamp
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    private ZonedDateTime updatedAt;

    public static ClientDAO buildNewClientDAO(Client client) {
        ClientDAO clientDao = new ClientDAO();
        clientDao.setId(UUID.randomUUID().toString());
        clientDao.setFirstName(client.getFirstName());
        clientDao.setLastName(client.getLastName());
        clientDao.setPersonalCode(client.getPersonalCode());
        return clientDao;
    }

    public void addLoan(Loan loan) {
        loans.add(loan);
    }
}
