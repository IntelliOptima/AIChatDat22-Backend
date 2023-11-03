CREATE TABLE user
(
    id                          INT AUTO_INCREMENT PRIMARY KEY,
    email                       VARCHAR(100) NOT NULL,
    full_name                   VARCHAR(100) NOT NULL
);



CREATE TABLE chatroom (
  id                            INT AUTO_INCREMENT PRIMARY KEY,
  chatroom_user_creator_id      INT,
  created_date                  TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6),

    CONSTRAINT fk_ChatroomUser
    FOREIGN KEY (chatroom_user_creator_id)
    REFERENCES user (id)
);


CREATE TABLE message (
  id                          INT AUTO_INCREMENT PRIMARY KEY,
  user_id                     INT,
  message                     TEXT NOT NULL,
  chatroom_id                 INT NOT NULL,
  created_date                TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6),

    -- CONSTRAINT fk_UserMessage
    -- FOREIGN KEY (user_id)
    -- REFERENCES users (id)

    CONSTRAINT fk_ChatroomMessage
        FOREIGN KEY (chatroom_id)
        REFERENCES chatroom (id)
);


CREATE TABLE chatroom_users_relation (
    id                          INT AUTO_INCREMENT PRIMARY KEY,
    chatroom_id                 INT,
    user_id                     INT,

    CONSTRAINT fk_ChatroomMessageRelation
        FOREIGN KEY (chatroom_id)
        REFERENCES chatroom (id),

    CONSTRAINT fk_ChatroomUserRelation
        FOREIGN KEY (user_id)
        REFERENCES user (id)
);