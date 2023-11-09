CREATE TABLE user
(
    id                          BIGINT AUTO_INCREMENT PRIMARY KEY,
    email                       VARCHAR(100) NOT NULL,
    full_name                   VARCHAR(100) NOT NULL,
    created_date                TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6),
    last_modified_date          TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6),
    version                     INT
);

CREATE TABLE chatroom (
  id                            VARCHAR(36) NOT NULL PRIMARY KEY,
  chatroom_user_creator_id      BIGINT,
  created_date                  TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6),
  last_modified_date            TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6),
  version                       INT,

    CONSTRAINT fk_ChatroomUser
    FOREIGN KEY (chatroom_user_creator_id)
    REFERENCES user (id)
);


CREATE TABLE chatroom_users_relation (
    id                          BIGINT AUTO_INCREMENT PRIMARY KEY,
    chatroom_id                 VARCHAR(36) NOT NULL,
    user_id                     BIGINT,

    CONSTRAINT fk_ChatroomMessageRelation
        FOREIGN KEY (chatroom_id)
        REFERENCES chatroom(id),

    CONSTRAINT fk_ChatroomUserRelation
        FOREIGN KEY (user_id)
        REFERENCES user (id)
);


CREATE TABLE message (
  id                            BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id                       BIGINT,
  text_message                  TEXT NOT NULL,
  chatroom_id                   VARCHAR(36) NOT NULL,
  created_date                  TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6),
  last_modified_date            TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6),
  version                       INT,

    CONSTRAINT fk_UserMessage
      FOREIGN KEY (user_id)
      REFERENCES user (id),

    CONSTRAINT fk_ChatroomMessage
        FOREIGN KEY (chatroom_id)
        REFERENCES chatroom (id)
);


