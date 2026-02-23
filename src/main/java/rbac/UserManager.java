package rbac;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class UserManager implements Repository<User> {

    // Хранилище пользователей: ключ - username, значение - объект пользователя
    private final Map<String, User> users = new HashMap<>();

    @Override
    public void add(User user) {
        if (user == null) {
            throw new IllegalArgumentException("Пользователь не может быть null");
        }

        String username = user.username().toLowerCase();

        if (users.containsKey(username)) {
            throw new IllegalArgumentException("Пользователь с именем '" + username + "' уже существует");
        }

        users.put(username, user);
    }

    @Override
    public boolean remove(User user) {
        if (user == null) {
            return false;
        }

        return users.remove(user.username().toLowerCase()) != null;
    }



    @Override
    public Optional<User> findById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return Optional.empty();
        }

        return Optional.ofNullable(users.get(id.toLowerCase()));
    }


    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public int count() {
        return users.size();
    }

    @Override
    public void clear() {
        users.clear();
    }


    public Optional<User> findByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return Optional.empty();
        }

        return Optional.ofNullable(users.get(username.toLowerCase()));
    }


    public Optional<User> findByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return Optional.empty();
        }

        String emailLower = email.toLowerCase();
        return users.values().stream()
                .filter(user -> user.email().equals(emailLower))
                .findFirst();
    }


    public List<User> findByFilter(UserFilter filter) {
        if (filter == null) {
            return new ArrayList<>(users.values());
        }

        return users.values().stream()
                .filter(filter)
                .collect(Collectors.toList());
    }


    public List<User> findAll(UserFilter filter, Comparator<User> sorter) {
        if (filter == null && sorter == null) {
            return new ArrayList<>(users.values());
        }

        List<User> result = users.values().stream()
                .filter(filter != null ? filter : user -> true)
                .collect(Collectors.toList());

        if (sorter != null) {
            result.sort(sorter);
        }

        return result;
    }

    /**
     * Проверить существование пользователя с указанным именем
     * @param username имя пользователя
     * @return true если пользователь существует, иначе false
     */
    public boolean exists(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }

        return users.containsKey(username.toLowerCase());
    }

    /**
     * Обновить данные пользователя
     * @param username имя пользователя для обновления
     * @param newFullName новое полное имя
     * @param newEmail новый адрес электронной почты
     * @throws IllegalArgumentException если пользователь не найден или данные невалидны
     */
    public void update(String username, String newFullName, String newEmail) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Имя пользователя не может быть пустым");
        }

        String usernameLower = username.toLowerCase();
        User existingUser = users.get(usernameLower);

        if (existingUser == null) {
            throw new IllegalArgumentException("Пользователь с именем '" + username + "' не найден");
        }

        // Валидация новых данных через метод validate
        User updatedUser = User.validate(username, newFullName, newEmail);

        // Обновляем пользователя в хранилище
        users.put(usernameLower, updatedUser);
    }


    public Map<String, User> getUsers() {
        return Collections.unmodifiableMap(users);
    }
}