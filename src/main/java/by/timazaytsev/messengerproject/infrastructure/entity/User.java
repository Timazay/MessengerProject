package by.timazaytsev.messengerproject.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
@NamedEntityGraph(
        name = "User.withRoles",
        attributeNodes = {
                @NamedAttributeNode("roles")
        }
)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 20)
    private String username;

    @Column(nullable = false, length = 50)
    private String password;

    private boolean isOnline;

    @Column(nullable = false, unique = true, length = 100)
    private String mail;

    private boolean verified;

    @Column(length = 50)
    private String name;

    @Column(length = 50)
    private String surname;

    private UUID cityId;

    @Column(length = 30)
    private String quote;

    private String avatarUrl; // Будет хранить ссылку/путь к файлу в MinIO

    @ManyToMany
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private List<Role> roles;

    @OneToMany(mappedBy = "user")
    private List<RefreshToken> refreshTokens;

    @OneToMany(mappedBy = "contact")
    private List<Contact> contactOf;
}
