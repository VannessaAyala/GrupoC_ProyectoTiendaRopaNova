package com.tienda.ropa.backend.service.impl;

import com.tienda.ropa.backend.dto.usuario.UsuarioCreateRequest;
import com.tienda.ropa.backend.dto.usuario.UsuarioResponse;
import com.tienda.ropa.backend.dto.usuario.UsuarioUpdateRequest;
import com.tienda.ropa.backend.domain.Usuario;
import com.tienda.ropa.backend.repository.UsuarioRepository;
import com.tienda.ropa.backend.web.advice.ConflictException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import org.springframework.data.domain.PageImpl;

import java.util.List;
import java.util.Arrays;

/**
 * Test de Servicio – UsuarioServiceImpl
 *
 * Laboratorio 2 - Paso 6b:
 *   Demuestra CI con lógica de negocio
 *   Valida la regla de correo único (ConflictException si ya existe)
 *   Usa Mockito: no toca base de datos real
 *
 * @ExtendWith(MockitoExtension.class) inicializa los mocks automáticamente.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioServiceImpl – Tests de lógica de negocio")
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    private UsuarioCreateRequest requestValido;

    @BeforeEach
    void setUp() {
        requestValido = new UsuarioCreateRequest();
        requestValido.setNombre("Irving Martinez");
        requestValido.setCorreo("irving@gmail.com");
        requestValido.setContrasena("pass1234");
        requestValido.setRol("CLIENTE");
    }

    // ── Test 1: Crear usuario exitosamente ────────────────────────────────────

    @Test
    @DisplayName("Debe crear un usuario cuando el correo no existe")
    void debeCrearUsuarioCuandoCorreoNoExiste() {
        // Arrange – el correo NO existe en la BD
        when(usuarioRepository.existsByCorreo(requestValido.getCorreo()))
                .thenReturn(false);

        Usuario usuarioGuardado = buildUsuario(1L, requestValido);
        when(usuarioRepository.save(any(Usuario.class)))
                .thenReturn(usuarioGuardado);

        // Act
        UsuarioResponse response = usuarioService.create(requestValido);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getNombre()).isEqualTo("Irving Martinez");
        assertThat(response.getCorreo()).isEqualTo("irving@gmail.com");
        assertThat(response.getRol()).isEqualTo("CLIENTE");
        assertThat(response.getActive()).isTrue();

        // Verifica que sí se llamó a save() exactamente una vez
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    // ── Test 2: Regla de correo único ─────────────────────────────────────────

    @Test
    @DisplayName("Debe lanzar ConflictException cuando el correo ya está registrado")
    void debeLanzarConflictExceptionSiCorreoYaExiste() {
        // Arrange – el correo YA existe en la BD
        when(usuarioRepository.existsByCorreo(requestValido.getCorreo()))
                .thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> usuarioService.create(requestValido))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("correo");  // El mensaje menciona "correo"

        // Verifica que NO se llamó a save() (no debe persistir nada)
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    // ── Test 3: Desactivar usuario ────────────────────────────────────────────

    @Test
    @DisplayName("Debe desactivar un usuario cambiando active a false")
    void debeDesactivarUsuario() {
        // Arrange
        Usuario usuario = buildUsuario(1L, requestValido);
        when(usuarioRepository.findById(1L))
                .thenReturn(java.util.Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // Act
        UsuarioResponse response = usuarioService.deactivate(1L);

        // Assert
        assertThat(response.getActive()).isFalse();
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    // ── Test 5: Obtener por ID exitosamente ──────────────────────────────────

    @Test
    @DisplayName("Debe obtener un usuario por id cuando existe")
    void debeObtenerUsuarioPorId() {
        // Arrange
        Usuario usuario = buildUsuario(5L, requestValido);
        when(usuarioRepository.findById(5L))
                .thenReturn(java.util.Optional.of(usuario));

        // Act
        UsuarioResponse r = usuarioService.getById(5L);

        // Assert
        assertThat(r).isNotNull();
        assertThat(r.getId()).isEqualTo(5L);
        assertThat(r.getCorreo()).isEqualTo(requestValido.getCorreo());
    }

    // ── Test 6: Actualizar usuario exitosamente ──────────────────────────────

    @Test
    @DisplayName("Debe actualizar campos permitidos cuando el usuario existe")
    void debeActualizarUsuarioCuandoExiste() {
        // Arrange
        Usuario existente = buildUsuario(2L, requestValido);
        when(usuarioRepository.findById(2L))
                .thenReturn(java.util.Optional.of(existente));
        when(usuarioRepository.save(any(Usuario.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        UsuarioUpdateRequest upd = new UsuarioUpdateRequest();
        upd.setNombre("Irving Updated");

        // Act
        UsuarioResponse res = usuarioService.update(2L, upd);

        // Assert
        assertThat(res.getNombre()).isEqualTo("Irving Updated");
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Debe lanzar NotFoundException al actualizar id inexistente")
    void debeLanzarNotFoundAlActualizarNoExistente() {
        // Arrange
        when(usuarioRepository.findById(99L))
                .thenReturn(java.util.Optional.empty());

        UsuarioUpdateRequest upd = new UsuarioUpdateRequest();
        upd.setNombre("No Existe");

        // Act & Assert
        assertThatThrownBy(() -> usuarioService.update(99L, upd))
                .isInstanceOf(com.tienda.ropa.backend.web.advice.NotFoundException.class);
    }

    @Test
    @DisplayName("list debe retornar lista mapeada de usuarios")
    void listRetornaUsuarios() {
        // Arrange
        Usuario u1 = buildUsuario(10L, requestValido);
        Usuario u2 = buildUsuario(11L, requestValido);
        u2.setNombre("Otro");
        when(usuarioRepository.findAll()).thenReturn(Arrays.asList(u1, u2));

        // Act
        List<UsuarioResponse> lista = usuarioService.list();

        // Assert
        assertThat(lista).hasSize(2);
        assertThat(lista).extracting(UsuarioResponse::getId).contains(10L, 11L);
    }

    @Test
    @DisplayName("searchByName debe retornar página con resultados mapeados")
    void searchByNamePaginado() {
        // Arrange
        Usuario u1 = buildUsuario(20L, requestValido);
        u1.setNombre("Irving One");
        Usuario u2 = buildUsuario(21L, requestValido);
        u2.setNombre("Irving Two");

        when(usuarioRepository.findByNombreContainingIgnoreCase(eq("irving"), any()))
                .thenReturn(new PageImpl<>(List.of(u1, u2)));

        // Act
        var page = usuarioService.searchByName("irving", 0, 10);

        // Assert
        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).extracting(UsuarioResponse::getNombre)
                .containsExactlyInAnyOrder("Irving One", "Irving Two");
    }

    // ── Test 4: Obtener usuario por ID inexistente ────────────────────────────

    @Test
    @DisplayName("Debe lanzar NotFoundException si el usuario no existe")
    void debeLanzarNotFoundSiUsuarioNoExiste() {
        // Arrange
        when(usuarioRepository.findById(99L))
                .thenReturn(java.util.Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> usuarioService.getById(99L))
                .isInstanceOf(com.tienda.ropa.backend.web.advice.NotFoundException.class)
                .hasMessageContaining("Usuario");
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private Usuario buildUsuario(Long id, UsuarioCreateRequest req) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setNombre(req.getNombre());
        u.setCorreo(req.getCorreo());
        u.setContrasena(req.getContrasena());
        u.setRol(req.getRol());
        u.setActive(true);
        return u;
    }
}