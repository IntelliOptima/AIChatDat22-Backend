ALTER TABLE test_chataidb.user
    ADD CONSTRAINT unique_email UNIQUE (email);