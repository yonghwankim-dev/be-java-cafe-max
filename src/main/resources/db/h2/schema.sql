drop table IF EXISTS users CASCADE;
drop table IF EXISTS question CASCADE;
drop table IF EXISTS comment CASCADE;

create TABLE users
(
    id       bigint auto_increment,
    USERID   VARCHAR(255) NOT NULL,
    NAME     VARCHAR(255) NOT NULL,
    PASSWORD VARCHAR(255) NOT NULL,
    EMAIL    VARCHAR(255) NOT NULL,
    primary key (id)
);


create TABLE question
(
    id         bigint auto_increment,
    title      VARCHAR(255) NOT NULL,
    content    TEXT         NOT NULL,
    createTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    modifyTime TIMESTAMP DEFAULT NULL ON update CURRENT_TIMESTAMP,
    deleted    boolean   DEFAULT false,
    userId     bigint       NOT NULL,
    primary key (id),
    foreign key (userId) references users (id) ON delete CASCADE
);


create TABLE comment
(
    ID         bigint auto_increment,
    CONTENT    VARCHAR(3000) NOT NULL,
    CREATETIME TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    MODIFYTIME TIMESTAMP DEFAULT NULL ON update CURRENT_TIMESTAMP,
    DELETED    boolean   DEFAULT false,
    USERID     bigint        NOT NULL,
    QUESTIONID bigint        NOT NULL,
    primary key (id),
    foreign key (USERID) references users (id) ON delete CASCADE,
    foreign key (QUESTIONID) references question (id) ON delete CASCADE
);
