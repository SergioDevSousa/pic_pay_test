package br.com.picpay.service;

import br.com.picpay.dto.request.UserRequest;
import br.com.picpay.dto.response.UserResponse;
import br.com.picpay.entity.User;
import br.com.picpay.exception.BusinessException;
import br.com.picpay.exception.ResourceNotFoundException;
import br.com.picpay.mapper.UserMapper;
import br.com.picpay.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    public UserResponse create(UserRequest request) {
        if (userRepository.existsByDocumentOrEmail(request.document(), request.email()))
            throw new BusinessException("Documento ou e-mail já cadastrado");

        User user = new User(request.fullName(), request.document(), request.email(),
                passwordEncoder.encode(request.password()), request.balance(), request.userType());
        return userMapper.toResponse(userRepository.save(user));
    }

    public UserResponse findById(Long id) {
        return userRepository.findById(id).map(userMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
    }

    public List<UserResponse> findAll() {
        return userRepository.findAll().stream().map(userMapper::toResponse).toList();
    }
}
