package by.timazaytsev.messengerproject.infrastructure.repository;

import by.timazaytsev.messengerproject.infrastructure.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {
}
