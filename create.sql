create sequence hibernate_sequence start 1 increment 1;
create sequence loans_seq start 1 increment 1;
create sequence postpone_seq start 1 increment 1;

    create table client (
       id varchar(255) not null,
        created_at timestamp not null,
        email varchar(255) not null,
        first_name varchar(255) not null,
        last_name varchar(255) not null,
        personal_code varchar(11) not null,
        phone_number varchar(255) not null,
        status varchar(255),
        updated_at timestamp,
        version int8 not null,
        primary key (id)
    );

    create table client_aud (
       id varchar(255) not null,
        rev int4 not null,
        revtype int2,
        created_at timestamp,
        email varchar(255),
        first_name varchar(255),
        last_name varchar(255),
        personal_code varchar(11),
        phone_number varchar(255),
        status varchar(255),
        updated_at timestamp,
        primary key (id, rev)
    );

    create table loan (
       id int8 not null,
        amount numeric(19, 2) not null,
        created_at timestamp not null,
        interest_rate numeric(19, 2),
        return_date timestamp,
        status varchar(255),
        term_in_months int4 not null,
        client_id varchar(255),
        primary key (id)
    );

    create table loan_aud (
       id int8 not null,
        rev int4 not null,
        revtype int2,
        amount numeric(19, 2),
        created_at timestamp,
        interest_rate numeric(19, 2),
        return_date timestamp,
        status varchar(255),
        term_in_months int4,
        client_id varchar(255),
        primary key (id, rev)
    );

    create table loan_loan_postpones_aud (
       rev int4 not null,
        loan_id int8 not null,
        loan_postpones_id int8 not null,
        revtype int2,
        primary key (rev, loan_id, loan_postpones_id)
    );

    create table loan_loan_postpones (
       loan_id int8 not null,
        loan_postpones_id int8 not null,
        primary key (loan_id, loan_postpones_id)
    );

    create table loan_postpone_aud (
       id int8 not null,
        rev int4 not null,
        revtype int2,
        interest_rate numeric(19, 2),
        return_date timestamp,
        primary key (id, rev)
    );

    create table loan_postpone (
       id int8 not null,
        interest_rate numeric(19, 2),
        return_date timestamp,
        primary key (id)
    );

    create table revinfo (
       rev int4 not null,
        revtstmp int8,
        primary key (rev)
    );

    alter table if exists loan_loan_postpones 
       add constraint UK_ft6ocdio773aw91oslrovuq1p unique (loan_postpones_id);

    alter table if exists client_aud 
       add constraint FKq7rlntwn6l0k20fxnu2ro82h6 
       foreign key (rev) 
       references revinfo;

    alter table if exists loan 
       add constraint FK62s5k229ouak16t2k5pvq4n16 
       foreign key (client_id) 
       references client;

    alter table if exists loan_aud 
       add constraint FKbfkqegitcyn916sb39dr1516j 
       foreign key (rev) 
       references revinfo;

    alter table if exists loan_loan_postpones_aud 
       add constraint FK7fx522ogw2tx49h0h6fn8v2nh 
       foreign key (rev) 
       references revinfo;

    alter table if exists loan_loan_postpones 
       add constraint FKcgmdduagb7ow8ee938qylghed 
       foreign key (loan_postpones_id) 
       references loan_postpone;

    alter table if exists loan_loan_postpones 
       add constraint FK8mt3ryb4ctvdfjugpph71tk8x 
       foreign key (loan_id) 
       references loan;

    alter table if exists loan_postpone_aud 
       add constraint FKi11mvh4xh7v3cx5340wredmal 
       foreign key (rev) 
       references revinfo;
