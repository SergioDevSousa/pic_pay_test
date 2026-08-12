package br.com.picpay.mapper;

import br.com.picpay.dto.response.UserResponse;
import br.com.picpay.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getFullName(), user.getDocument(),
                user.getEmail(), user.getBalance(), user.getUserType());
    }
}
