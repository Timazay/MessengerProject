package by.timazaytsev.messengerproject.infrastructure.repository;

import by.timazaytsev.messengerproject.infrastructure.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    @EntityGraph("User.withRoles")
    Optional<User> findUserByUsername(String username);

    @EntityGraph("User.withRoles")
    Optional<User> findUserByMail(String mail);
}
