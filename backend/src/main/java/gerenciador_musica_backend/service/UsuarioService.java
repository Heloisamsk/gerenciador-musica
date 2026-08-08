package gerenciador_musica_backend.service;

import gerenciador_musica_backend.dto.UsuarioListagemDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import gerenciador_musica_backend.dto.UsuarioRequestDTO;
import gerenciador_musica_backend.dto.UsuarioResponseDTO;
import gerenciador_musica_backend.model.Usuario;
import gerenciador_musica_backend.repository.UsuarioRepository;
import gerenciador_musica_backend.exception.EmailJaCadastradoException;

import java.util.ArrayList;
import java.util.List;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public UsuarioResponseDTO cadastrarUsuario(UsuarioRequestDTO request) {
        
        // 1. Regra de negócio: Verificar se o e-mail já está em uso
        if (usuarioRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new EmailJaCadastradoException(
                    "Já existe um usuário cadastrado com este e-mail."
            );
        }

        // 2. Converter DTO de Entrada (Request) em Entidade
        Usuario novoUsuario = new Usuario();
        novoUsuario.setNome(request.getNome());
        novoUsuario.setEmail(request.getEmail());
        
        // Criptografando a senha antes de salvar!
        novoUsuario.setSenha(passwordEncoder.encode(request.getSenha()));
        
        novoUsuario.setRole(request.getRole());

        // 3. Salvar o usuário no banco de dados
        Usuario usuarioSalvo = usuarioRepository.save(novoUsuario);

        // 4. Converter Entidade salva em DTO de Saída (Response)
        UsuarioResponseDTO response = new UsuarioResponseDTO();
        response.setId(usuarioSalvo.getId());
        response.setNome(usuarioSalvo.getNome());
        response.setEmail(usuarioSalvo.getEmail());
        response.setRole(usuarioSalvo.getRole());
        
        // Sem senha no DTO de resposta por questões de segurança.

        return response;
    }

    public List<UsuarioListagemDTO> listarUsuarios() {

        List<Usuario> usuarios = usuarioRepository.findAll();
        List<UsuarioListagemDTO> listagem = new ArrayList<>();

        for (Usuario usuario : usuarios) {
            UsuarioListagemDTO dto = new UsuarioListagemDTO();
            dto.setId(usuario.getId());
            dto.setNome(usuario.getNome());
            dto.setEmail(usuario.getEmail());
            dto.setRole(usuario.getRole());

            listagem.add(dto);
        }
        return listagem;
    }
}