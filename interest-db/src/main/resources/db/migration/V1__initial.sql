create sequence loans_seq start 1 increment 1;
create sequence postpone_seq start 1 increment 1;

create table client
(
    id            varchar(255) not null,
    created_at    timestamp,
    first_name    varchar(255) not null,
    last_name     varchar(255) not null,
    personal_code int8         not null,
    updated_at    timestamp,
    primary key (id)
);

create table client_loans
(
    clientdao_id varchar(255) not null,
    loans_id     int8         not null,
    primary key (clientdao_id, loans_id)
);

create table loan
(
    id             int8           not null,
    amount         numeric(19, 2) not null,
    interest_rate  numeric(19, 2),
    return_date    timestamp,
    term_in_months int4           not null,
    primary key (id)
);

create table loan_loan_postpones
(
    loan_id           int8 not null,
    loan_postpones_id int8 not null,
    primary key (loan_id, loan_postpones_id)
);

create table loan_postpone
(
    id                int8 not null,
    new_interest_rate numeric(19, 2),
    new_return_date   timestamp,
    primary key (id)
);

alter table client_loans
    add constraint UK_g2owf9h13n3c2vjgxs1qjppj9 unique (loans_id);

alter table loan_loan_postpones
    add constraint UK_ft6ocdio773aw91oslrovuq1p unique (loan_postpones_id);

alter table client_loans
    add constraint FK6yl9l2n0vjjvn1gg9tal5tt5x
        foreign key (loans_id)
            references loan;

alter table client_loans
    add constraint FK2bclseepbs1htoogi28tid9yr
        foreign key (clientdao_id)
            references client;

alter table loan_loan_postpones
    add constraint FKcgmdduagb7ow8ee938qylghed
        foreign key (loan_postpones_id)
            references loan_postpone;

alter table loan_loan_postpones
    add constraint FK8mt3ryb4ctvdfjugpph71tk8x
        foreign key (loan_id)
            references loan;