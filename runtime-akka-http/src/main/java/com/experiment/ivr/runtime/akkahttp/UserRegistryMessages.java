package com.experiment.ivr.runtime.akkahttp;

import java.io.Serializable;

public interface UserRegistryMessages {

    class GetUsers implements Serializable {
    }

    class ActionPerformed implements Serializable {
        private final String description;

        public ActionPerformed(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    class CreateUser implements Serializable {
        private final UserRegistryActor.User user;

        public CreateUser(UserRegistryActor.User user) {
            this.user = user;
        }

        public UserRegistryActor.User getUser() {
            return user;
        }
    }

    class GetUser implements Serializable {
        private final String name;

        public GetUser(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    class DeleteUser implements Serializable {
        private final String name;

        public DeleteUser(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}