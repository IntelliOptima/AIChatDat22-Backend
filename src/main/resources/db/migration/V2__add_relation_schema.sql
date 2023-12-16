CREATE TABLE user_relation (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT,
    friend_id       BIGINT,

    CONSTRAINT fk_UserUserRelation
        FOREIGN KEY (user_id)
        REFERENCES user (id),

    CONSTRAINT fk_FriendUserRelation
        FOREIGN KEY (user_id)
        REFERENCES user (id)
);

CREATE TABLE pending_relation_request (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    requester_id    BIGINT,
    receiver_id     BIGINT,

    CONSTRAINT fk_RequesterPending
        FOREIGN KEY (requester_id)
        REFERENCES user (id),

    CONSTRAINT fk_ReceiverPending
        FOREIGN KEY (receiver_id)
        REFERENCES user (id)
)