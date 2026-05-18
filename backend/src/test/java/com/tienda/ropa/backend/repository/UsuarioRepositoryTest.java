package com.tienda.ropa.backend.repository;

import com.tienda.ropa.backend.domain.Usuario;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageImpl;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("UsuarioRepository – Tests de persistencia JPA")
class UsuarioRepositoryTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @BeforeEach
    void setUp() {
        usuarioRepository.deleteAll();
    }

    @Test
    @DisplayName("Debe persistir y recuperar un Usuario correctamente (mapeo JPA OK)")
    void debeGuardarYRecuperarUsuario() {
        // Arrange
        Usuario u = new Usuario();
        u.setNombre("Juan Perez");
        u.setCorreo("juan@example.com");
        u.setContrasena("secreto");
        u.setRol("USER");
        u.setActive(true);

        // Act
        Usuario guardado = usuarioRepository.save(u);
        Usuario encontrado = usuarioRepository.findById(guardado.getId()).orElse(null);

        // Assert
        assertThat(encontrado).isNotNull();
        assertThat(encontrado.getNombre()).isEqualTo("Juan Perez");
        assertThat(encontrado.getCorreo()).isEqualTo("juan@example.com");
        assertThat(encontrado.getRol()).isEqualTo("USER");
        assertThat(encontrado.getActive()).isTrue();
    }

    @Test
    @DisplayName("findByCorreo debe encontrar usuario por correo")
    void debeBuscarUsuarioPorCorreo() {
        // Arrange
        crearUsuario("Ana", "ana@example.com");

        // Act
        var opt = usuarioRepository.findByCorreo("ana@example.com");

        // Assert
        assertThat(opt).isPresent();
        assertThat(opt.orElseThrow().getNombre()).isEqualTo("Ana");
    }

    @Test
    @DisplayName("existsByCorreo debe indicar si un correo ya existe")
    void existsByCorreoIndicaExistencia() {
        // Arrange
        crearUsuario("Pedro", "pedro@example.com");

        // Act & Assert
        assertThat(usuarioRepository.existsByCorreo("pedro@example.com")).isTrue();
        assertThat(usuarioRepository.existsByCorreo("noexiste@example.com")).isFalse();
    }

    @Test
    @DisplayName("findByNombreContainingIgnoreCase debe encontrar usuarios por nombre parcial")
    void debeBuscarUsuariosPorNombreParcial() {
        // Arrange – persistir 3 usuarios
        crearUsuario("María Gomez", "maria@example.com");
        crearUsuario("Mariano López", "mariano@example.com");
        crearUsuario("Carlos Ruiz", "carlos@example.com");

        // Act – buscar "mar" debe encontrar 2 (María y Mariano)
        Page<Usuario> resultado = usuarioRepository
            .findByNombreContainingIgnoreCase("mar", PageRequest.of(0, 10));

        // Assert
        assertThat(resultado.getTotalElements()).isEqualTo(2);
        assertThat(resultado.getContent())
                .extracting(Usuario::getNombre)
                .containsExactlyInAnyOrder("María Gomez", "Mariano López");
    }

    @Test
    @DisplayName("findByNombre exacto debe encontrar usuario por nombre")
    void debeBuscarUsuarioPorNombreExacto() {
        // Arrange
        crearUsuario("Lucia", "lucia@example.com");

        // Act
        var opt = usuarioRepository.findByNombre("Lucia");

        // Assert
        assertThat(opt).isPresent();
        assertThat(opt.orElseThrow().getCorreo()).isEqualTo("lucia@example.com");
    }

    @Test
    @DisplayName("Eliminar por id debe remover el usuario")
    void eliminarPorIdRemueveUsuario() {
        // Arrange
        Usuario u = new Usuario();
        u.setNombre("Borrar");
        u.setCorreo("borrar@example.com");
        u.setContrasena("pw");
        u.setRol("USER");
        u.setActive(true);
        Usuario guardado = usuarioRepository.save(u);

        // Act
        usuarioRepository.deleteById(guardado.getId());

        // Assert
        assertThat(usuarioRepository.findById(guardado.getId())).isEmpty();
    }

    @Test
    @DisplayName("No permite persistir dos usuarios con el mismo correo (unicidad)")
    void noPermiteCorreosDuplicados() {
        // Arrange
        crearUsuario("Uno", "dup@example.com");

        Usuario u2 = new Usuario();
        u2.setNombre("Dos");
        u2.setCorreo("dup@example.com");
        u2.setContrasena("pw");
        u2.setRol("USER");

        // Act & Assert - fallará por constraint de unicidad
        org.junit.jupiter.api.Assertions.assertThrows(
                DataIntegrityViolationException.class,
                () -> usuarioRepository.saveAndFlush(u2)
        );
    }

    @Test
    @DisplayName("Paginación: page size 1 y página fuera de rango")
    void paginacionLimites() {
        // Arrange
        crearUsuario("U1", "u1@example.com");
        crearUsuario("U2", "u2@example.com");
        crearUsuario("U3", "u3@example.com");

        // Act page size 1
        var p0 = usuarioRepository.findByNombreContainingIgnoreCase("u", PageRequest.of(0, 1));
        var p1 = usuarioRepository.findByNombreContainingIgnoreCase("u", PageRequest.of(1, 1));
        var p2 = usuarioRepository.findByNombreContainingIgnoreCase("u", PageRequest.of(2, 1));
        var p3 = usuarioRepository.findByNombreContainingIgnoreCase("u", PageRequest.of(3, 1)); // fuera de rango

        // Assert
        assertThat(p0.getTotalElements()).isEqualTo(3);
        assertThat(p0.getContent()).hasSize(1);
        assertThat(p1.getContent()).hasSize(1);
        assertThat(p2.getContent()).hasSize(1);
        assertThat(p3.getContent()).isEmpty();
    }

    @Test
    @DisplayName("findByNombreContainingIgnoreCase retorna vacío si no hay coincidencias")
    void buscarSinResultadosRetornaVacio() {
        // Arrange
        crearUsuario("Persona", "p@example.com");

        // Act
        PageImpl<Usuario> resultado = (PageImpl<Usuario>) usuarioRepository
                .findByNombreContainingIgnoreCase("zzzz", PageRequest.of(0, 10));

        // Assert
        assertThat(resultado.getTotalElements()).isZero();
        assertThat(resultado.getContent()).isEmpty();
    }

    @Test
    @DisplayName("La configuración de H2 para tests está OK (CRUD básico)")
    void configuracionH2EstaOk() {
        // Act
        List<Usuario> inicial = usuarioRepository.findAll();
        assertThat(inicial).isEmpty();

        crearUsuario("Test Usuario", "test@example.com");

        List<Usuario> despues = usuarioRepository.findAll();
        assertThat(despues).hasSize(1);
    }

    // Helper
    private void crearUsuario(String nombre, String correo) {
        Usuario u = new Usuario();
        u.setNombre(nombre);
        u.setCorreo(correo);
        u.setContrasena("pw");
        u.setRol("USER");
        u.setActive(true);
        usuarioRepository.save(u);
    }
}
