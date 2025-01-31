package no.fintlabs.flyt.authorization.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.flyt.authorization.user.kafka.UserPermission;
import no.fintlabs.flyt.authorization.user.kafka.UserPermissionEntityProducerService;
import no.fintlabs.flyt.authorization.user.model.User;
import no.fintlabs.flyt.authorization.user.model.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static java.util.stream.Collectors.toMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserPermissionEntityProducerService userPermissionEntityProducerService;

    public void publishUsers() {
        log.info("Starting publishing users");
        try {
            List<UserPermission> userPermissions = this.userRepository
                    .findAll()
                    .stream()
                    .map(this::mapFromEntityToUserPermission)
                    .toList();

            log.info("Retrieved and mapped {} user entities", userPermissions.size());
            userPermissions.forEach(userPermissionEntityProducerService::send);
            log.info("Successfully published users");

        } catch (Exception e) {
            log.error("Error while publishing users", e);
        }
    }

    public void save(User user) {
        UserEntity userEntity = this.userRepository.save(mapFromUser(user));
        userPermissionEntityProducerService.send(mapFromEntityToUserPermission(userEntity));
    }

    public Optional<User> find(UUID objectIdentifier) {
        return this.userRepository.findByObjectIdentifier(objectIdentifier)
                .map(this::mapFromEntity);
    }

    public Page<User> getAll(Pageable pageable) {
        return this.userRepository.findAll(pageable)
                .map(this::mapFromEntity);
    }

    public void putAll(List<User> users) {
        Map<UUID, List<Long>> sourceApplicationIdsPerObjectIdentifier = users.stream()
                .collect(toMap(
                        User::getObjectIdentifier,
                        User::getSourceApplicationIds
                ));

        List<UserEntity> entities = userRepository.findAllByObjectIdentifierIn(
                users
                        .stream()
                        .map(User::getObjectIdentifier)
                        .toList()
        );

        entities.forEach(entity -> entity.setSourceApplicationIds(
                sourceApplicationIdsPerObjectIdentifier.get(entity.getObjectIdentifier())
        ));

        userRepository.saveAll(entities);
    }

    private UserEntity mapFromUser(User user) {
        return UserEntity
                .builder()
                .objectIdentifier(user.getObjectIdentifier())
                .name(user.getName())
                .email(user.getEmail())
                .sourceApplicationIds(user.getSourceApplicationIds())
                .build();
    }

    private User mapFromEntity(UserEntity userEntity) {
        return User
                .builder()
                .objectIdentifier(userEntity.getObjectIdentifier())
                .name(userEntity.getName())
                .email(userEntity.getEmail())
                .sourceApplicationIds(userEntity.getSourceApplicationIds())
                .build();
    }

    private UserPermission mapFromEntityToUserPermission(UserEntity userEntity) {
        return UserPermission
                .builder()
                .objectIdentifier(userEntity.getObjectIdentifier())
                .sourceApplicationIds(userEntity.getSourceApplicationIds())
                .build();
    }

}
