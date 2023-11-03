create table users
(
    id INT AUTO_INCREMENT PRIMARY KEY
);


CREATE TABLE message (
    id                          INT AUTO_INCREMENT PRIMARY KEY,
    user_id                     INT,
    message                     TEXT NOT NULL,
    chatroom_id                 INT NOT NULL,
    created_date                TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6),
    last_modified_date          TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)

    -- CONSTRAINT fk_UserMessage
       -- FOREIGN KEY (user_id)
       -- REFERENCES users (id)
);
