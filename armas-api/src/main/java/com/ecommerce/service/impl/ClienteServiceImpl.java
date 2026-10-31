package com.ecommerce.service.impl;

import com.ecommerce.dto.ClienteRequestDTO;
import com.ecommerce.dto.ClienteResponseDTO;
import com.ecommerce.dto.ProdutoImagemDTO;
import com.ecommerce.exception.ClienteFormException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.Cliente;
import com.ecommerce.model.ClienteFoto;
import com.ecommerce.model.ProdutoImagem;
import com.ecommerce.model.Estado;
import com.ecommerce.model.Municipio;
import com.ecommerce.model.Pedido;
import com.ecommerce.model.Usuario;
import com.ecommerce.exception.CadastroException;
import com.ecommerce.repository.ClienteRepository;
import com.ecommerce.repository.EstadoRepository;
import com.ecommerce.repository.MunicipioRepository;
import com.ecommerce.service.ClienteService;
import com.ecommerce.service.ProdutoImagemValidator;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Locale;

@ApplicationScoped
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository repository;
    private final EstadoRepository estadoRepository;
    private final MunicipioRepository municipioRepository;
    private final ProdutoImagemValidator imagemValidator;

    public ClienteServiceImpl(ClienteRepository repository, EstadoRepository estadoRepository,
            MunicipioRepository municipioRepository, ProdutoImagemValidator imagemValidator) {
        this.repository = repository;
        this.estadoRepository = estadoRepository;
        this.municipioRepository = municipioRepository;
        this.imagemValidator = imagemValidator;
    }

    @Override
    public List<ClienteResponseDTO> listar() {
        return repository.listAll().stream()
                .map(ClienteResponseDTO::from)
                .toList();
    }

    @Override
    public List<ClienteResponseDTO> sugerir(String nome) {
        if (nome == null || nome.trim().length() < 2) {
            return List.of();
        }
        return repository.sugerirPorNome(nome).stream()
                .map(ClienteResponseDTO::from)
                .toList();
    }

    @Override
    public ClienteResponseDTO buscarPorId(Long id) {
        return ClienteResponseDTO.from(encontrarCliente(id));
    }

    @Override
    @Transactional
    public ProdutoImagemDTO buscarFoto(Long id) {
        ClienteFoto foto = encontrarCliente(id).foto;
        if (foto == null) {
            throw new ResourceNotFoundException("Foto do cliente nao encontrada");
        }
        return new ProdutoImagemDTO(foto.conteudo, foto.tipo);
    }

    @Override
    @Transactional
    public ClienteResponseDTO cadastrar(ClienteRequestDTO dados) {
        Cliente cliente = new Cliente();
        preencher(cliente, dados);
        repository.persist(cliente);
        confirmarAlteracoes();
        return ClienteResponseDTO.from(cliente);
    }

    @Override
    @Transactional
    public ClienteResponseDTO editar(Long id, ClienteRequestDTO dados) {
        Cliente cliente = encontrarCliente(id);
        preencher(cliente, dados);
        Usuario conta = Usuario.find("cliente.id", id).firstResult();
        if (conta != null) conta.email = cliente.email;
        confirmarAlteracoes();
        return ClienteResponseDTO.from(cliente);
    }

    @Override
    @Transactional
    public void excluir(Long id) {
        Cliente cliente = encontrarCliente(id);
        if (Pedido.count("cliente.id", id) > 0) {
            throw new CadastroException("O cliente possui pedidos. Preserve o historico de compras.", 409);
        }
        Usuario.delete("cliente.id", id);
        repository.delete(cliente);
    }

    private Cliente encontrarCliente(Long id) {
        return repository.findByIdOptional(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente nao encontrado"));
    }

    private void preencher(Cliente cliente, ClienteRequestDTO dados) {
        ProdutoImagem imagemValidada = imagemValidator.validar(dados.fotoBase64(), dados.removerFoto());
        String email = dados.email().trim().toLowerCase(Locale.ROOT);
        String telefone = dados.telefone().trim();
        String cpf = dados.cpf().trim();

        if (!cpfValido(cpf)) {
            throw new ClienteFormException("cpf", "Informe um CPF valido.", 400);
        }
        if (repository.existeOutroCom("email", email, cliente.id)) {
            throw new ClienteFormException("email", "Este e-mail ja esta cadastrado.", 409);
        }
        if (repository.existeOutroCom("telefone", telefone, cliente.id)) {
            throw new ClienteFormException("telefone", "Este telefone ja esta cadastrado.", 409);
        }
        if (repository.existeOutroCom("cpf", cpf, cliente.id)) {
            throw new ClienteFormException("cpf", "Este CPF ja esta cadastrado.", 409);
        }

        Municipio municipio = obterMunicipio(dados);
        cliente.nome = dados.nome().trim();
        cliente.email = email;
        cliente.telefone = telefone;
        cliente.cpf = cpf;
        cliente.cep = dados.cep() == null || dados.cep().isBlank() ? null : dados.cep();
        cliente.logradouro = dados.logradouro().trim();
        cliente.numero = dados.numero().trim();
        cliente.complemento = dados.complemento() == null ? null : dados.complemento().trim();
        cliente.bairro = dados.bairro().trim();
        cliente.municipio = municipio;
        cliente.endereco = cliente.logradouro + ", " + cliente.numero + " - "
                + municipio.nome + "/" + municipio.estado.sigla;
        if (dados.removerFoto()) {
            cliente.foto = null;
        } else if (imagemValidada != null) {
            ClienteFoto foto = new ClienteFoto();
            foto.conteudo = imagemValidada.conteudo;
            foto.tipo = imagemValidada.tipo;
            cliente.foto = foto;
        }
    }

    private Municipio obterMunicipio(ClienteRequestDTO dados) {
        Municipio existente = municipioRepository.find("codigoIbge", dados.municipioCodigoIbge()).firstResult();
        if (existente != null) {
            if (!existente.estado.sigla.equals(dados.estadoSigla())) {
                throw new ClienteFormException("municipioNome", "O municipio nao pertence a UF informada.", 400);
            }
            return existente;
        }

        Estado estado = estadoRepository.find("sigla", dados.estadoSigla()).firstResult();
        if (estado == null) {
            estado = new Estado();
            estado.sigla = dados.estadoSigla();
            estado.nome = dados.estadoNome().trim();
            estadoRepository.persist(estado);
        }
        Municipio municipio = new Municipio();
        municipio.codigoIbge = dados.municipioCodigoIbge();
        municipio.nome = dados.municipioNome().trim();
        municipio.estado = estado;
        municipioRepository.persist(municipio);
        return municipio;
    }

    private void confirmarAlteracoes() {
        try {
            repository.flush();
        } catch (RuntimeException erro) {
            Throwable causa = erro;
            while (causa != null) {
                if (causa instanceof org.hibernate.exception.ConstraintViolationException violacao) {
                    String nome = violacao.getConstraintName();
                    if (nome != null) {
                        nome = nome.toLowerCase(Locale.ROOT);
                        if (nome.contains("uk_cliente_email")) {
                            throw new ClienteFormException("email", "Este e-mail ja esta cadastrado.", 409);
                        }
                        if (nome.contains("uk_cliente_telefone")) {
                            throw new ClienteFormException("telefone", "Este telefone ja esta cadastrado.", 409);
                        }
                        if (nome.contains("uk_cliente_cpf")) {
                            throw new ClienteFormException("cpf", "Este CPF ja esta cadastrado.", 409);
                        }
                    }
                }
                causa = causa.getCause();
            }
            throw erro;
        }
    }

    private boolean cpfValido(String cpf) {
        if (!cpf.matches("\\d{11}") || cpf.chars().distinct().count() == 1) {
            return false;
        }
        for (int tamanho = 9; tamanho <= 10; tamanho++) {
            int soma = 0;
            for (int i = 0; i < tamanho; i++) {
                soma += (cpf.charAt(i) - '0') * (tamanho + 1 - i);
            }
            int digito = (soma * 10) % 11;
            if (digito == 10) {
                digito = 0;
            }
            if (digito != cpf.charAt(tamanho) - '0') {
                return false;
            }
        }
        return true;
    }
}
